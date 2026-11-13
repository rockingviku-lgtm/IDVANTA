package com.example.pdf

import android.content.Context
import android.graphics.pdf.PdfDocument
import com.example.data.local.ExportHistoryEntity
import com.example.data.local.StudentRecordEntity
import com.example.domain.model.BiodataData
import com.example.domain.model.CvDocumentData
import com.example.domain.model.DocumentCategory
import com.example.domain.model.DocumentDesignConfig
import com.example.domain.model.IdCardData
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class IdPdfOutputFormat {
    CR80_FRONT_BACK,
    A4_PRINT_SHEET
}

data class GeneratedPdfResult(
    val file: File,
    val pageCount: Int,
    val fileSizeBytes: Long,
    val exportHistoryEntry: ExportHistoryEntity
)

@Singleton
class PdfGeneratorEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cvRenderer: CvPdfRenderer,
    private val idCardRenderer: IdCardPdfRenderer,
    private val biodataRenderer: BiodataPdfRenderer
) {
    private fun exportsDir(): File = File(context.filesDir, "exports").apply { mkdirs() }

    private fun buildUniqueFile(baseName: String): File {
        val clean = baseName
            .replace(Regex("[^A-Za-z0-9_\\- \\u0900-\\u097F]"), "_")
            .trim()
            .ifBlank { "IDVANTA_Document" }
        return File(exportsDir(), "${clean}_${System.currentTimeMillis()}.pdf")
    }

    suspend fun generateCvPdf(
        documentId: Long,
        title: String,
        category: DocumentCategory,
        data: CvDocumentData,
        design: DocumentDesignConfig
    ): GeneratedPdfResult = withContext(Dispatchers.IO) {
        val outFile = buildUniqueFile(title.ifBlank { data.fullName.ifBlank { "Resume" } })
        val pdf = PdfDocument()
        val pages = try {
            val count = cvRenderer.render(pdf, data, design)
            FileOutputStream(outFile).use { pdf.writeTo(it) }
            count
        } finally {
            pdf.close()
        }
        val size = outFile.length()
        GeneratedPdfResult(
            file = outFile,
            pageCount = pages,
            fileSizeBytes = size,
            exportHistoryEntry = ExportHistoryEntity(
                documentId = documentId,
                documentTitle = title.ifBlank { data.fullName.ifBlank { category.displayName } },
                category = category.name,
                fileName = outFile.name,
                filePath = outFile.absolutePath,
                pageSize = "A4",
                pageCount = pages,
                fileSizeBytes = size
            )
        )
    }

    suspend fun generateIdCardPdf(
        documentId: Long,
        title: String,
        data: IdCardData,
        design: DocumentDesignConfig,
        outputFormat: IdPdfOutputFormat
    ): GeneratedPdfResult = withContext(Dispatchers.IO) {
        val outFile = buildUniqueFile(title.ifBlank { data.personName.ifBlank { data.cardType.displayName } })
        val pdf = PdfDocument()
        val pages = try {
            val count = when (outputFormat) {
                IdPdfOutputFormat.CR80_FRONT_BACK -> idCardRenderer.renderStandaloneCr80(pdf, data, design)
                IdPdfOutputFormat.A4_PRINT_SHEET -> idCardRenderer.renderSingleA4Sheet(pdf, data, design)
            }
            FileOutputStream(outFile).use { pdf.writeTo(it) }
            count
        } finally {
            pdf.close()
        }
        val size = outFile.length()
        GeneratedPdfResult(
            file = outFile,
            pageCount = pages,
            fileSizeBytes = size,
            exportHistoryEntry = ExportHistoryEntity(
                documentId = documentId,
                documentTitle = title.ifBlank { data.personName.ifBlank { data.cardType.displayName } },
                category = data.cardType.docCategory.name,
                fileName = outFile.name,
                filePath = outFile.absolutePath,
                pageSize = if (outputFormat == IdPdfOutputFormat.CR80_FRONT_BACK) "CR80" else "A4 Sheet",
                pageCount = pages,
                fileSizeBytes = size
            )
        )
    }

    suspend fun generateBulkStudentIdPdf(
        batchTitle: String,
        students: List<StudentRecordEntity>,
        baseData: IdCardData,
        design: DocumentDesignConfig,
        includeBackPages: Boolean
    ): GeneratedPdfResult = withContext(Dispatchers.IO) {
        val outFile = buildUniqueFile(batchTitle.ifBlank { "Bulk_Student_IDs" })
        val pdf = PdfDocument()
        val pages = try {
            val count = idCardRenderer.renderBulkStudentsA4(pdf, students, baseData, design, includeBackPages)
            FileOutputStream(outFile).use { pdf.writeTo(it) }
            count
        } finally {
            pdf.close()
        }
        val size = outFile.length()
        GeneratedPdfResult(
            file = outFile,
            pageCount = pages,
            fileSizeBytes = size,
            exportHistoryEntry = ExportHistoryEntity(
                documentId = 0L,
                documentTitle = "$batchTitle (${students.size} IDs)",
                category = DocumentCategory.SCHOOL_ID.name,
                fileName = outFile.name,
                filePath = outFile.absolutePath,
                pageSize = "A4 Bulk CR80",
                pageCount = pages,
                fileSizeBytes = size
            )
        )
    }

    suspend fun generateBiodataPdf(
        documentId: Long,
        title: String,
        data: BiodataData,
        design: DocumentDesignConfig
    ): GeneratedPdfResult = withContext(Dispatchers.IO) {
        val outFile = buildUniqueFile(title.ifBlank { data.name.ifBlank { "Personal_Biodata" } })
        val pdf = PdfDocument()
        val pages = try {
            val count = biodataRenderer.render(pdf, data, design)
            FileOutputStream(outFile).use { pdf.writeTo(it) }
            count
        } finally {
            pdf.close()
        }
        val size = outFile.length()
        GeneratedPdfResult(
            file = outFile,
            pageCount = pages,
            fileSizeBytes = size,
            exportHistoryEntry = ExportHistoryEntity(
                documentId = documentId,
                documentTitle = title.ifBlank { data.name.ifBlank { "Personal Biodata" } },
                category = DocumentCategory.BIODATA.name,
                fileName = outFile.name,
                filePath = outFile.absolutePath,
                pageSize = "A4",
                pageCount = pages,
                fileSizeBytes = size
            )
        )
    }
}
