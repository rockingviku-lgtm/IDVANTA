package com.example.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.DocumentRepository
import com.example.domain.model.BiodataData
import com.example.domain.model.DocumentCategory
import com.example.domain.model.DocumentDesignConfig
import com.example.domain.model.DocumentTemplate
import com.example.domain.model.TemplateCatalog
import com.example.pdf.PdfGeneratorEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BiodataSnapshot(
    val title: String,
    val data: BiodataData,
    val design: DocumentDesignConfig
)

data class BiodataEditorUiState(
    val documentId: Long = 0L,
    val title: String = "Personal Biodata",
    val data: BiodataData = BiodataData(),
    val design: DocumentDesignConfig = DocumentDesignConfig(templateId = "bio_classic_ivory"),
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val isSaving: Boolean = false,
    val isExporting: Boolean = false,
    val statusMessage: String? = null
)

@HiltViewModel
class BiodataMakerViewModel @Inject constructor(
    private val repository: DocumentRepository,
    private val pdfEngine: PdfGeneratorEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(BiodataEditorUiState())
    val uiState: StateFlow<BiodataEditorUiState> = _uiState.asStateFlow()

    private val undoStack = ArrayDeque<BiodataSnapshot>()
    private val redoStack = ArrayDeque<BiodataSnapshot>()
    private var autosaveJob: Job? = null
    private var initialized = false

    fun initialize(documentId: Long, templateId: String) {
        if (initialized && (documentId <= 0L || _uiState.value.documentId == documentId)) return
        initialized = true
        viewModelScope.launch {
            if (documentId > 0L) {
                val entity = repository.getDocumentById(documentId)
                if (entity != null) {
                    _uiState.value = BiodataEditorUiState(
                        documentId = entity.id,
                        title = entity.title,
                        data = repository.decodeBiodataData(entity.payloadJson),
                        design = repository.decodeDesignConfig(entity.designConfigJson)
                    )
                    return@launch
                }
            }
            val tpl = if (templateId.isNotBlank()) {
                TemplateCatalog.findById(templateId)
            } else {
                TemplateCatalog.defaultTemplateFor(DocumentCategory.BIODATA)
            }
            _uiState.value = BiodataEditorUiState(
                title = "Personal Biodata",
                design = DocumentDesignConfig(
                    templateId = tpl.id,
                    accentColorHex = tpl.accentHex,
                    backgroundColorHex = tpl.backgroundHex,
                    textColorHex = tpl.textHex,
                    fontStyle = tpl.fontOption
                )
            )
        }
    }

    private fun pushSnapshot() {
        val cur = _uiState.value
        undoStack.addLast(BiodataSnapshot(cur.title, cur.data, cur.design))
        if (undoStack.size > 30) undoStack.removeFirst()
        redoStack.clear()
    }

    private fun mutate(transform: (BiodataEditorUiState) -> BiodataEditorUiState) {
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
        redoStack.addLast(BiodataSnapshot(cur.title, cur.data, cur.design))
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
        undoStack.addLast(BiodataSnapshot(cur.title, cur.data, cur.design))
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

    fun updateTitle(title: String) = mutate { it.copy(title = title) }
    fun updateData(transform: (BiodataData) -> BiodataData) = mutate { it.copy(data = transform(it.data)) }
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

    fun attachPhoto(uri: Uri) {
        viewModelScope.launch {
            val saved = repository.copyUriToInternalMedia(uri, "biodata_photo")
            updateData { it.copy(photoUri = saved) }
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
        val id = repository.saveBiodataDocument(
            id = state.documentId,
            title = state.title.ifBlank { state.data.name.ifBlank { "Personal Biodata" } },
            data = state.data,
            design = state.design,
            isDraft = isDraft
        )
        _uiState.update {
            it.copy(
                documentId = id,
                isSaving = false,
                statusMessage = if (showMsg) "Biodata saved offline" else it.statusMessage
            )
        }
        return id
    }

    fun exportPdf(onExported: (exportId: Long, filePath: String, title: String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            val docId = persist(isDraft = false, showMsg = false)
            val state = _uiState.value
            val result = pdfEngine.generateBiodataPdf(
                documentId = docId,
                title = state.title,
                data = state.data,
                design = state.design
            )
            val exportId = repository.recordExport(result.exportHistoryEntry)
            _uiState.update { it.copy(isExporting = false) }
            onExported(exportId, result.file.absolutePath, result.exportHistoryEntry.documentTitle)
        }
    }
}
