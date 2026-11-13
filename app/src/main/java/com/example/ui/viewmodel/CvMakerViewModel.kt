package com.example.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.DocumentRepository
import com.example.domain.model.CertificationEntry
import com.example.domain.model.CvDocumentData
import com.example.domain.model.CvSubCategory
import com.example.domain.model.DocumentCategory
import com.example.domain.model.DocumentDesignConfig
import com.example.domain.model.DocumentTemplate
import com.example.domain.model.EducationEntry
import com.example.domain.model.ProjectEntry
import com.example.domain.model.ReferenceEntry
import com.example.domain.model.TemplateCatalog
import com.example.domain.model.WorkExperienceEntry
import com.example.pdf.PdfGeneratorEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CvSnapshot(
    val title: String,
    val category: DocumentCategory,
    val data: CvDocumentData,
    val design: DocumentDesignConfig
)

data class CvEditorUiState(
    val documentId: Long = 0L,
    val title: String = "My Professional Resume",
    val category: DocumentCategory = DocumentCategory.RESUME,
    val data: CvDocumentData = CvDocumentData(),
    val design: DocumentDesignConfig = DocumentDesignConfig(),
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val isSaving: Boolean = false,
    val isExporting: Boolean = false,
    val statusMessage: String? = null,
    val lastSavedTimestamp: Long? = null
)

@HiltViewModel
class CvMakerViewModel @Inject constructor(
    private val repository: DocumentRepository,
    private val pdfEngine: PdfGeneratorEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(CvEditorUiState())
    val uiState: StateFlow<CvEditorUiState> = _uiState.asStateFlow()

    private val undoStack = ArrayDeque<CvSnapshot>()
    private val redoStack = ArrayDeque<CvSnapshot>()
    private var autosaveJob: Job? = null
    private var initialized = false

    fun initialize(documentId: Long, initialCategoryName: String, templateId: String) {
        if (initialized && (documentId <= 0L || _uiState.value.documentId == documentId)) return
        initialized = true
        viewModelScope.launch {
            if (documentId > 0L) {
                val entity = repository.getDocumentById(documentId)
                if (entity != null) {
                    val cat = runCatching { DocumentCategory.valueOf(entity.category) }
                        .getOrDefault(DocumentCategory.RESUME)
                    val data = repository.decodeCvData(entity.payloadJson)
                    val design = repository.decodeDesignConfig(entity.designConfigJson)
                    _uiState.value = CvEditorUiState(
                        documentId = entity.id,
                        title = entity.title,
                        category = cat,
                        data = data,
                        design = design,
                        lastSavedTimestamp = entity.updatedAt
                    )
                    return@launch
                }
            }
            val cat = runCatching { DocumentCategory.valueOf(initialCategoryName) }
                .getOrDefault(DocumentCategory.RESUME)
            val tpl = if (templateId.isNotBlank()) {
                TemplateCatalog.findById(templateId)
            } else {
                TemplateCatalog.defaultTemplateFor(cat)
            }
            val defaultSub = if (cat == DocumentCategory.CV) CvSubCategory.ACADEMIC_CV else CvSubCategory.PROFESSIONAL_RESUME
            _uiState.value = CvEditorUiState(
                title = "Untitled ${cat.displayName}",
                category = cat,
                data = CvDocumentData(cvSubCategory = defaultSub),
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
        val current = _uiState.value
        undoStack.addLast(CvSnapshot(current.title, current.category, current.data, current.design))
        if (undoStack.size > 30) undoStack.removeFirst()
        redoStack.clear()
    }

    private fun mutate(transform: (CvEditorUiState) -> CvEditorUiState) {
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
        redoStack.addLast(CvSnapshot(cur.title, cur.category, cur.data, cur.design))
        _uiState.update {
            it.copy(
                title = prev.title,
                category = prev.category,
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
        undoStack.addLast(CvSnapshot(cur.title, cur.category, cur.data, cur.design))
        _uiState.update {
            it.copy(
                title = next.title,
                category = next.category,
                data = next.data,
                design = next.design,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
        scheduleAutosave()
    }

    fun updateTitle(title: String) = mutate { it.copy(title = title) }
    fun updateData(transform: (CvDocumentData) -> CvDocumentData) = mutate { it.copy(data = transform(it.data)) }
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

    fun attachProfilePhoto(uri: Uri) {
        viewModelScope.launch {
            val savedPath = repository.copyUriToInternalMedia(uri, "cv_photo")
            updateData { it.copy(photoUri = savedPath) }
        }
    }

    fun addEducation(entry: EducationEntry) {
        updateData { it.copy(education = it.education + entry.copy(id = UUID.randomUUID().toString())) }
    }

    fun removeEducation(id: String) {
        updateData { it.copy(education = it.education.filterNot { item -> item.id == id }) }
    }

    fun addExperience(entry: WorkExperienceEntry) {
        updateData { it.copy(experience = it.experience + entry.copy(id = UUID.randomUUID().toString())) }
    }

    fun removeExperience(id: String) {
        updateData { it.copy(experience = it.experience.filterNot { item -> item.id == id }) }
    }

    fun addProject(entry: ProjectEntry) {
        updateData { it.copy(projects = it.projects + entry.copy(id = UUID.randomUUID().toString())) }
    }

    fun removeProject(id: String) {
        updateData { it.copy(projects = it.projects.filterNot { item -> item.id == id }) }
    }

    fun addCertification(entry: CertificationEntry) {
        updateData { it.copy(certifications = it.certifications + entry.copy(id = UUID.randomUUID().toString())) }
    }

    fun removeCertification(id: String) {
        updateData { it.copy(certifications = it.certifications.filterNot { item -> item.id == id }) }
    }

    fun addReference(entry: ReferenceEntry) {
        updateData { it.copy(references = it.references + entry.copy(id = UUID.randomUUID().toString())) }
    }

    fun removeReference(id: String) {
        updateData { it.copy(references = it.references.filterNot { item -> item.id == id }) }
    }

    private fun scheduleAutosave() {
        autosaveJob?.cancel()
        autosaveJob = viewModelScope.launch {
            delay(650L)
            persistDocument(isDraft = true, showBanner = false)
        }
    }

    fun saveDraft() {
        viewModelScope.launch {
            persistDocument(isDraft = true, showBanner = true)
        }
    }

    private suspend fun persistDocument(isDraft: Boolean, showBanner: Boolean): Long {
        val state = _uiState.value
        _uiState.update { it.copy(isSaving = true) }
        val resolvedTitle = state.title.ifBlank { state.data.fullName.ifBlank { "Untitled ${state.category.displayName}" } }
        val id = repository.saveCvDocument(
            id = state.documentId,
            title = resolvedTitle,
            category = state.category,
            data = state.data,
            design = state.design,
            isDraft = isDraft
        )
        _uiState.update {
            it.copy(
                documentId = id,
                isSaving = false,
                lastSavedTimestamp = System.currentTimeMillis(),
                statusMessage = if (showBanner) "Draft saved offline" else it.statusMessage
            )
        }
        return id
    }

    fun exportPdf(onExported: (exportId: Long, filePath: String, title: String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, statusMessage = null) }
            val docId = persistDocument(isDraft = false, showBanner = false)
            val state = _uiState.value
            val result = pdfEngine.generateCvPdf(
                documentId = docId,
                title = state.title,
                category = state.category,
                data = state.data,
                design = state.design
            )
            val exportId = repository.recordExport(result.exportHistoryEntry)
            _uiState.update { it.copy(isExporting = false) }
            onExported(exportId, result.file.absolutePath, result.exportHistoryEntry.documentTitle)
        }
    }
}
