package com.example.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.StudentRecordEntity
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.DocumentRepository
import com.example.domain.model.DocumentDesignConfig
import com.example.domain.model.IdCardData
import com.example.domain.model.IdCardType
import com.example.domain.model.TemplateCatalog
import com.example.pdf.IdPdfOutputFormat
import com.example.pdf.PdfGeneratorEngine
import com.example.util.CsvParser
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class BulkIdUiState(
    val students: List<StudentRecordEntity> = emptyList(),
    val batchInstitutionName: String = "",
    val batchSession: String = "2026-27",
    val batchLogoUri: String = "",
    val includeBackPages: Boolean = true,
    val institutionAuthorized: Boolean = false,
    val selectedTemplateId: String = "id_classic_navy_crest",
    val accentHex: String = "#121A3F",
    val isExporting: Boolean = false,
    val feedbackMessage: String? = null
)

@HiltViewModel
class BulkIdGeneratorViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: DocumentRepository,
    private val prefsRepo: UserPreferencesRepository,
    private val pdfEngine: PdfGeneratorEngine
) : ViewModel() {

    private val localControls = MutableStateFlow(BulkIdUiState())

    val uiState: StateFlow<BulkIdUiState> = combine(
        repository.allStudents,
        localControls
    ) { students, controls ->
        controls.copy(students = students)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BulkIdUiState())

    init {
        viewModelScope.launch {
            val prefs = prefsRepo.preferencesFlow.first()
            localControls.update {
                it.copy(
                    batchInstitutionName = prefs.defaultInstitutionName,
                    institutionAuthorized = prefs.institutionAuthorizedDefault
                )
            }
        }
    }

    fun updateInstitutionName(name: String) = localControls.update { it.copy(batchInstitutionName = name) }
    fun updateSession(session: String) = localControls.update { it.copy(batchSession = session) }
    fun setIncludeBackPages(include: Boolean) = localControls.update { it.copy(includeBackPages = include) }
    fun setInstitutionAuthorized(auth: Boolean) = localControls.update { it.copy(institutionAuthorized = auth) }
    fun clearFeedback() = localControls.update { it.copy(feedbackMessage = null) }

    fun selectTemplate(templateId: String) {
        val tpl = TemplateCatalog.findById(templateId)
        localControls.update { it.copy(selectedTemplateId = tpl.id, accentHex = tpl.accentHex) }
    }

    fun attachBatchLogo(uri: Uri) {
        viewModelScope.launch {
            val path = repository.copyUriToInternalMedia(uri, "batch_logo")
            localControls.update { it.copy(batchLogoUri = path, feedbackMessage = "Institution logo updated") }
        }
    }

    fun addManualStudent(student: StudentRecordEntity) {
        viewModelScope.launch {
            val resolved = student.copy(
                schoolName = student.schoolName.ifBlank { localControls.value.batchInstitutionName },
                academicSession = student.academicSession.ifBlank { localControls.value.batchSession }
            )
            val result = repository.addStudentIfUnique(resolved)
            localControls.update {
                it.copy(
                    feedbackMessage = result.fold(
                        onSuccess = { "Student added to batch" },
                        onFailure = { err -> err.message ?: "Validation error" }
                    )
                )
            }
        }
    }

    fun importCsvFromUri(uri: Uri) {
        viewModelScope.launch {
            val text = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                }.getOrNull()
            }
            if (text.isNullOrBlank()) {
                localControls.update { it.copy(feedbackMessage = "Could not read CSV file") }
                return@launch
            }
            importCsvText(text)
        }
    }

    fun loadSampleCsv() {
        importCsvText(CsvParser.sampleCsvContent)
    }

    private fun importCsvText(csvText: String) {
        viewModelScope.launch {
            val parsed = CsvParser.parseStudentsCsv(csvText, localControls.value.batchInstitutionName)
            if (parsed.records.isEmpty()) {
                localControls.update {
                    it.copy(feedbackMessage = parsed.errors.firstOrNull() ?: "No valid student rows found")
                }
                return@launch
            }
            val (inserted, dupSkipped) = repository.importStudentsFilteringDuplicates(parsed.records)
            val msg = buildString {
                append("Imported $inserted student(s)")
                if (dupSkipped > 0) append(", skipped $dupSkipped duplicate(s)")
                if (parsed.skippedRows > 0) append(", ${parsed.skippedRows} invalid row(s)")
            }
            localControls.update { it.copy(feedbackMessage = msg) }
        }
    }

    fun toggleStudentSelected(id: Long, selected: Boolean) {
        viewModelScope.launch { repository.setStudentSelected(id, selected) }
    }

    fun selectAll(selected: Boolean) {
        viewModelScope.launch { repository.setAllStudentsSelected(selected) }
    }

    fun deleteStudent(id: Long) {
        viewModelScope.launch { repository.deleteStudent(id) }
    }

    fun clearAllStudents() {
        viewModelScope.launch { repository.clearAllStudents() }
    }

    fun generateSingleStudentPdf(
        student: StudentRecordEntity,
        onExported: (exportId: Long, filePath: String, title: String) -> Unit
    ) {
        viewModelScope.launch {
            localControls.update { it.copy(isExporting = true) }
            val state = localControls.value
            val tpl = TemplateCatalog.findById(state.selectedTemplateId)
            val cardData = IdCardData(
                cardType = IdCardType.SCHOOL,
                institutionName = student.schoolName.ifBlank { state.batchInstitutionName },
                logoUri = student.logoUri.ifBlank { state.batchLogoUri },
                personName = student.studentName,
                photoUri = student.photoUri,
                fatherName = student.fatherName,
                motherName = student.motherName,
                className = student.className,
                section = student.section,
                rollNumber = student.rollNumber,
                admissionNumber = student.admissionNumber,
                dateOfBirth = student.dateOfBirth,
                address = student.address,
                emergencyContact = student.emergencyContact,
                bloodGroup = student.bloodGroup,
                academicSession = student.academicSession,
                validityDate = student.validityDate
            )
            val design = DocumentDesignConfig(
                templateId = tpl.id,
                accentColorHex = tpl.accentHex,
                backgroundColorHex = tpl.backgroundHex,
                textColorHex = tpl.textHex,
                fontStyle = tpl.fontOption,
                institutionAuthorized = state.institutionAuthorized
            )
            val result = pdfEngine.generateIdCardPdf(
                documentId = 0L,
                title = "${student.studentName}_ID",
                data = cardData,
                design = design,
                outputFormat = IdPdfOutputFormat.A4_PRINT_SHEET
            )
            val exportId = repository.recordExport(result.exportHistoryEntry)
            localControls.update { it.copy(isExporting = false) }
            onExported(exportId, result.file.absolutePath, result.exportHistoryEntry.documentTitle)
        }
    }

    fun generateBatchPdf(
        onExported: (exportId: Long, filePath: String, title: String) -> Unit
    ) {
        viewModelScope.launch {
            val selected = repository.getSelectedStudents()
            if (selected.isEmpty()) {
                localControls.update { it.copy(feedbackMessage = "Select at least one student to generate batch PDF") }
                return@launch
            }
            localControls.update { it.copy(isExporting = true) }
            val state = localControls.value
            val tpl = TemplateCatalog.findById(state.selectedTemplateId)
            val baseData = IdCardData(
                cardType = IdCardType.SCHOOL,
                institutionName = state.batchInstitutionName,
                logoUri = state.batchLogoUri,
                academicSession = state.batchSession
            )
            val design = DocumentDesignConfig(
                templateId = tpl.id,
                accentColorHex = tpl.accentHex,
                backgroundColorHex = tpl.backgroundHex,
                textColorHex = tpl.textHex,
                fontStyle = tpl.fontOption,
                institutionAuthorized = state.institutionAuthorized
            )
            val result = pdfEngine.generateBulkStudentIdPdf(
                batchTitle = state.batchInstitutionName.ifBlank { "Bulk_Student_IDs" },
                students = selected,
                baseData = baseData,
                design = design,
                includeBackPages = state.includeBackPages
            )
            val exportId = repository.recordExport(result.exportHistoryEntry)
            localControls.update { it.copy(isExporting = false) }
            onExported(exportId, result.file.absolutePath, result.exportHistoryEntry.documentTitle)
        }
    }
}
