package com.example.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.DocumentRepository
import com.example.domain.model.DocumentDesignConfig
import com.example.domain.model.DocumentTemplate
import com.example.domain.model.IdCardData
import com.example.domain.model.IdCardType
import com.example.domain.model.TemplateCatalog
import com.example.pdf.IdPdfOutputFormat
import com.example.pdf.PdfGeneratorEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class IdCardSnapshot(
    val title: String,
    val data: IdCardData,
    val design: DocumentDesignConfig
)

data class IdCardEditorUiState(
    val documentId: Long = 0L,
    val title: String = "Identity Card",
    val data: IdCardData = IdCardData(),
    val design: DocumentDesignConfig = DocumentDesignConfig(),
    val showBackSide: Boolean = false,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val isSaving: Boolean = false,
    val isExporting: Boolean = false,
    val statusMessage: String? = null
)

@HiltViewModel
class IdCardMakerViewModel @Inject constructor(
    private val repository: DocumentRepository,
    private val prefsRepo: UserPreferencesRepository,
    private val pdfEngine: PdfGeneratorEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(IdCardEditorUiState())
    val uiState: StateFlow<IdCardEditorUiState> = _uiState.asStateFlow()

    private val undoStack = ArrayDeque<IdCardSnapshot>()
    private val redoStack = ArrayDeque<IdCardSnapshot>()
    private var autosaveJob: Job? = null
    private var initialized = false

    fun initialize(documentId: Long, cardTypeName: String, templateId: String) {
        if (initialized && (documentId <= 0L || _uiState.value.documentId == documentId)) return
        initialized = true
        viewModelScope.launch {
            if (documentId > 0L) {
                val entity = repository.getDocumentById(documentId)
                if (entity != null) {
                    val data = repository.decodeIdCardData(entity.payloadJson)
                    val design = repository.decodeDesignConfig(entity.designConfigJson)
                    _uiState.value = IdCardEditorUiState(
                        documentId = entity.id,
                        title = entity.title,
                        data = data,
                        design = design
                    )
                    return@launch
                }
            }
            val type = runCatching { IdCardType.valueOf(cardTypeName) }.getOrDefault(IdCardType.SCHOOL)
            val prefs = prefsRepo.preferencesFlow.first()
            val tpl = if (templateId.isNotBlank()) {
                TemplateCatalog.findById(templateId)
            } else {
                TemplateCatalog.defaultTemplateFor(type.docCategory)
            }
            _uiState.value = IdCardEditorUiState(
                title = "New ${type.displayName}",
                data = IdCardData(
                    cardType = type,
                    institutionName = prefs.defaultInstitutionName,
                    academicSession = "2026-27"
                ),
                design = DocumentDesignConfig(
                    templateId = tpl.id,
                    accentColorHex = tpl.accentHex,
                    backgroundColorHex = tpl.backgroundHex,
                    textColorHex = tpl.textHex,
                    fontStyle = tpl.fontOption,
                    institutionAuthorized = prefs.institutionAuthorizedDefault
                )
            )
        }
    }

    private fun pushSnapshot() {
        val cur = _uiState.value
        undoStack.addLast(IdCardSnapshot(cur.title, cur.data, cur.design))
        if (undoStack.size > 30) undoStack.removeFirst()
        redoStack.clear()
    }

    private fun mutate(transform: (IdCardEditorUiState) -> IdCardEditorUiState) {
        pushSnapshot()
        _uiState.update { state ->
            transform(state).copy(
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty(),
                statusMessage = null
            )
        }
        scheduleAutosave()
    }

    fun undo() {
        val prev = undoStack.removeLastOrNull() ?: return
        val cur = _uiState.value
        redoStack.addLast(IdCardSnapshot(cur.title, cur.data, cur.design))
        _uiState.update {
            it.copy(
                title = prev.title,
                data = prev.data,
                design = prev.design,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
        scheduleAutosave()
    }

    fun redo() {
        val next = redoStack.removeLastOrNull() ?: return
        val cur = _uiState.value
        undoStack.addLast(IdCardSnapshot(cur.title, cur.data, cur.design))
        _uiState.update {
            it.copy(
                title = next.title,
                data = next.data,
                design = next.design,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
        scheduleAutosave()
    }

    fun toggleSide() {
        _uiState.update { it.copy(showBackSide = !it.showBackSide) }
    }

    fun updateTitle(title: String) = mutate { it.copy(title = title) }
    fun updateData(transform: (IdCardData) -> IdCardData) = mutate { it.copy(data = transform(it.data)) }
    fun updateDesign(design: DocumentDesignConfig) = mutate { it.copy(design = design) }

    fun applyTemplate(template: DocumentTemplate) {
        mutate { state ->
            state.copy(
                design = state.design.copy(
                    templateId = template.id,
                    accentColorHex = template.accentHex,
                    backgroundColorHex = template.backgroundHex,
                    textColorHex = template.textHex,
                    fontStyle = template.fontOption
                )
            )
        }
    }

    fun attachPersonPhoto(uri: Uri) {
        viewModelScope.launch {
            val saved = repository.copyUriToInternalMedia(uri, "id_person")
            updateData { it.copy(photoUri = saved) }
        }
    }

    fun attachInstitutionLogo(uri: Uri) {
        viewModelScope.launch {
            val saved = repository.copyUriToInternalMedia(uri, "id_logo")
            updateData { it.copy(logoUri = saved, useBundledCrest = false) }
        }
    }

    private fun scheduleAutosave() {
        autosaveJob?.cancel()
        autosaveJob = viewModelScope.launch {
            delay(650L)
            persist(isDraft = true, showMsg = false)
        }
    }

    fun saveDraft() {
        viewModelScope.launch {
            persist(isDraft = true, showMsg = true)
        }
    }

    private suspend fun persist(isDraft: Boolean, showMsg: Boolean): Long {
        val state = _uiState.value
        _uiState.update { it.copy(isSaving = true) }
        val resolvedTitle = state.title.ifBlank { state.data.personName.ifBlank { state.data.cardType.displayName } }
        val id = repository.saveIdCardDocument(
            id = state.documentId,
            title = resolvedTitle,
            data = state.data,
            design = state.design,
            isDraft = isDraft
        )
        _uiState.update {
            it.copy(
                documentId = id,
                isSaving = false,
                statusMessage = if (showMsg) "ID card saved offline" else it.statusMessage
            )
        }
        return id
    }

    fun exportPdf(
        outputFormat: IdPdfOutputFormat,
        onExported: (exportId: Long, filePath: String, title: String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            val docId = persist(isDraft = false, showMsg = false)
            val state = _uiState.value
            val result = pdfEngine.generateIdCardPdf(
                documentId = docId,
                title = state.title,
                data = state.data,
                design = state.design,
                outputFormat = outputFormat
            )
            val exportId = repository.recordExport(result.exportHistoryEntry)
            _uiState.update { it.copy(isExporting = false) }
            onExported(exportId, result.file.absolutePath, result.exportHistoryEntry.documentTitle)
        }
    }
}
