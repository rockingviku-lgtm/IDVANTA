package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DocumentEntity
import com.example.data.local.ExportHistoryEntity
import com.example.data.repository.DocumentRepository
import com.example.domain.model.DocumentCategory
import com.example.pdf.IdPdfOutputFormat
import com.example.pdf.PdfGeneratorEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class DocumentsTab(val label: String) {
    ALL("All"),
    CVS("My CVs"),
    RESUMES("My Resumes"),
    ID_CARDS("My ID Cards"),
    BIODATA("My Biodata"),
    PDFS("My PDFs"),
    DRAFTS("Drafts"),
    FAVORITES("Favorites"),
    RECENT("Recently Edited")
}

data class MyDocumentsUiState(
    val selectedTab: DocumentsTab = DocumentsTab.ALL,
    val searchQuery: String = "",
    val documents: List<DocumentEntity> = emptyList(),
    val exports: List<ExportHistoryEntity> = emptyList(),
    val statusMessage: String? = null
)

@HiltViewModel
class MyDocumentsViewModel @Inject constructor(
    private val repository: DocumentRepository,
    private val pdfEngine: PdfGeneratorEngine
) : ViewModel() {

    private val selectedTab = MutableStateFlow(DocumentsTab.ALL)
    private val searchQuery = MutableStateFlow("")
    private val statusMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<MyDocumentsUiState> = combine(
        selectedTab,
        searchQuery,
        repository.allDocuments,
        repository.exportHistory,
        statusMessage
    ) { tab, query, docs, exports, msg ->
        val q = query.trim().lowercase()
        val filteredDocs = docs.filter { doc ->
            val catMatch = when (tab) {
                DocumentsTab.ALL, DocumentsTab.RECENT -> true
                DocumentsTab.CVS -> doc.category == DocumentCategory.CV.name
                DocumentsTab.RESUMES -> doc.category == DocumentCategory.RESUME.name
                DocumentsTab.ID_CARDS -> doc.category in listOf(
                    DocumentCategory.SCHOOL_ID.name,
                    DocumentCategory.COLLEGE_ID.name,
                    DocumentCategory.EMPLOYEE_ID.name,
                    DocumentCategory.TEACHER_ID.name
                )
                DocumentsTab.BIODATA -> doc.category == DocumentCategory.BIODATA.name
                DocumentsTab.DRAFTS -> doc.isDraft
                DocumentsTab.FAVORITES -> doc.isFavorite
                DocumentsTab.PDFS -> false
            }
            val queryMatch = q.isEmpty() || doc.title.lowercase().contains(q) || doc.category.lowercase().contains(q)
            catMatch && queryMatch
        }
        val filteredExports = exports.filter { exp ->
            q.isEmpty() || exp.documentTitle.lowercase().contains(q) || exp.fileName.lowercase().contains(q)
        }
        MyDocumentsUiState(
            selectedTab = tab,
            searchQuery = query,
            documents = filteredDocs,
            exports = filteredExports,
            statusMessage = msg
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MyDocumentsUiState())

    fun selectTab(tab: DocumentsTab) {
        selectedTab.value = tab
    }

    fun selectInitialTab(tabName: String) {
        val tab = runCatching { DocumentsTab.valueOf(tabName) }.getOrDefault(DocumentsTab.ALL)
        selectedTab.value = tab
    }

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun clearStatusMessage() {
        statusMessage.value = null
    }

    fun toggleFavorite(doc: DocumentEntity) {
        viewModelScope.launch {
            repository.toggleFavoriteDocument(doc.id, !doc.isFavorite)
        }
    }

    fun duplicate(doc: DocumentEntity) {
        viewModelScope.launch {
            repository.duplicateDocument(doc.id)
            statusMessage.value = "Document duplicated"
        }
    }

    fun rename(docId: Long, newTitle: String) {
        viewModelScope.launch {
            repository.renameDocument(docId, newTitle)
            statusMessage.value = "Document renamed"
        }
    }

    fun deleteDocument(docId: Long) {
        viewModelScope.launch {
            repository.deleteDocument(docId)
            statusMessage.value = "Document deleted"
        }
    }

    fun deleteExport(exportId: Long) {
        viewModelScope.launch {
            repository.deleteExport(exportId)
            statusMessage.value = "PDF deleted"
        }
    }

    fun exportDocumentToPdf(
        doc: DocumentEntity,
        onExported: (exportId: Long, filePath: String, title: String) -> Unit
    ) {
        viewModelScope.launch {
            val cat = runCatching { DocumentCategory.valueOf(doc.category) }.getOrDefault(DocumentCategory.CV)
            val design = repository.decodeDesignConfig(doc.designConfigJson)
            val result = when {
                cat.isCvOrResume -> pdfEngine.generateCvPdf(
                    documentId = doc.id,
                    title = doc.title,
                    category = cat,
                    data = repository.decodeCvData(doc.payloadJson),
                    design = design
                )
                cat.isIdCard -> pdfEngine.generateIdCardPdf(
                    documentId = doc.id,
                    title = doc.title,
                    data = repository.decodeIdCardData(doc.payloadJson),
                    design = design,
                    outputFormat = IdPdfOutputFormat.A4_PRINT_SHEET
                )
                else -> pdfEngine.generateBiodataPdf(
                    documentId = doc.id,
                    title = doc.title,
                    data = repository.decodeBiodataData(doc.payloadJson),
                    design = design
                )
            }
            val exportId = repository.recordExport(result.exportHistoryEntry)
            onExported(exportId, result.file.absolutePath, result.exportHistoryEntry.documentTitle)
        }
    }
}
