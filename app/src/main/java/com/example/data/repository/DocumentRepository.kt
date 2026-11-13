package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.local.DocumentEntity
import com.example.data.local.ExportHistoryEntity
import com.example.data.local.FavoriteTemplateEntity
import com.example.data.local.IdvantaDao
import com.example.data.local.StudentRecordEntity
import com.example.domain.model.BiodataData
import com.example.domain.model.CvDocumentData
import com.example.domain.model.DocumentCategory
import com.example.domain.model.DocumentDesignConfig
import com.example.domain.model.IdCardData
import com.example.domain.model.TemplateCatalog
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Singleton
class DocumentRepository @Inject constructor(
    private val dao: IdvantaDao,
    @ApplicationContext private val context: Context
) {
    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        coerceInputValues = true
    }

    val allDocuments: Flow<List<DocumentEntity>> = dao.observeAllDocuments()
    val recentDocuments: Flow<List<DocumentEntity>> = dao.observeRecentDocuments(10)
    val allStudents: Flow<List<StudentRecordEntity>> = dao.observeAllStudents()
    val exportHistory: Flow<List<ExportHistoryEntity>> = dao.observeExportHistory()
    val favoriteTemplateIds: Flow<List<String>> = dao.observeFavoriteTemplateIds()

    suspend fun getDocumentById(id: Long): DocumentEntity? = dao.getDocumentById(id)

    suspend fun saveCvDocument(
        id: Long,
        title: String,
        category: DocumentCategory,
        data: CvDocumentData,
        design: DocumentDesignConfig,
        isDraft: Boolean,
        isFavorite: Boolean = false
    ): Long {
        val now = System.currentTimeMillis()
        val existing = if (id > 0L) dao.getDocumentById(id) else null
        val entity = DocumentEntity(
            id = if (id > 0L) id else 0L,
            title = title.ifBlank { data.fullName.ifBlank { "Untitled ${category.displayName}" } },
            category = category.name,
            subCategory = data.cvSubCategory.name,
            templateId = design.templateId,
            payloadJson = json.encodeToString(data),
            designConfigJson = json.encodeToString(design),
            isFavorite = existing?.isFavorite ?: isFavorite,
            isDraft = isDraft,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now
        )
        return dao.insertDocument(entity)
    }

    suspend fun saveIdCardDocument(
        id: Long,
        title: String,
        data: IdCardData,
        design: DocumentDesignConfig,
        isDraft: Boolean
    ): Long {
        val now = System.currentTimeMillis()
        val existing = if (id > 0L) dao.getDocumentById(id) else null
        val category = data.cardType.docCategory
        val entity = DocumentEntity(
            id = if (id > 0L) id else 0L,
            title = title.ifBlank {
                data.personName.ifBlank { "Untitled ${data.cardType.displayName}" }
            },
            category = category.name,
            subCategory = data.cardType.name,
            templateId = design.templateId,
            payloadJson = json.encodeToString(data),
            designConfigJson = json.encodeToString(design),
            isFavorite = existing?.isFavorite ?: false,
            isDraft = isDraft,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now
        )
        return dao.insertDocument(entity)
    }

    suspend fun saveBiodataDocument(
        id: Long,
        title: String,
        data: BiodataData,
        design: DocumentDesignConfig,
        isDraft: Boolean
    ): Long {
        val now = System.currentTimeMillis()
        val existing = if (id > 0L) dao.getDocumentById(id) else null
        val entity = DocumentEntity(
            id = if (id > 0L) id else 0L,
            title = title.ifBlank { data.name.ifBlank { "Untitled Biodata" } },
            category = DocumentCategory.BIODATA.name,
            subCategory = "",
            templateId = design.templateId,
            payloadJson = json.encodeToString(data),
            designConfigJson = json.encodeToString(design),
            isFavorite = existing?.isFavorite ?: false,
            isDraft = isDraft,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now
        )
        return dao.insertDocument(entity)
    }

    fun decodeCvData(payloadJson: String): CvDocumentData {
        return runCatching { json.decodeFromString<CvDocumentData>(payloadJson) }
            .getOrElse { CvDocumentData() }
    }

    fun decodeIdCardData(payloadJson: String): IdCardData {
        return runCatching { json.decodeFromString<IdCardData>(payloadJson) }
            .getOrElse { IdCardData() }
    }

    fun decodeBiodataData(payloadJson: String): BiodataData {
        return runCatching { json.decodeFromString<BiodataData>(payloadJson) }
            .getOrElse { BiodataData() }
    }

    fun decodeDesignConfig(designJson: String): DocumentDesignConfig {
        return runCatching { json.decodeFromString<DocumentDesignConfig>(designJson) }
            .getOrElse { DocumentDesignConfig() }
    }

    suspend fun duplicateDocument(id: Long): Long? {
        val original = dao.getDocumentById(id) ?: return null
        val now = System.currentTimeMillis()
        return dao.insertDocument(
            original.copy(
                id = 0L,
                title = "${original.title} (Copy)",
                isDraft = true,
                createdAt = now,
                updatedAt = now
            )
        )
    }

    suspend fun renameDocument(id: Long, newTitle: String) {
        if (newTitle.isNotBlank()) {
            dao.renameDocument(id, newTitle.trim())
        }
    }

    suspend fun toggleFavoriteDocument(id: Long, favorite: Boolean) {
        dao.setDocumentFavorite(id, favorite)
    }

    suspend fun deleteDocument(id: Long) {
        dao.deleteDocumentById(id)
    }

    suspend fun toggleFavoriteTemplate(templateId: String, currentlyFavorite: Boolean) {
        if (currentlyFavorite) {
            dao.removeFavoriteTemplate(templateId)
        } else {
            dao.addFavoriteTemplate(FavoriteTemplateEntity(templateId))
        }
    }

    suspend fun seedInitialFavoriteTemplatesIfNeeded(currentFavorites: List<String>) {
        if (currentFavorites.isEmpty()) {
            dao.addFavoriteTemplate(FavoriteTemplateEntity(TemplateCatalog.cvTemplates[0].id))
            dao.addFavoriteTemplate(FavoriteTemplateEntity(TemplateCatalog.idTemplates[0].id))
            dao.addFavoriteTemplate(FavoriteTemplateEntity(TemplateCatalog.biodataTemplates[0].id))
        }
    }

    // Student Bulk Management with Duplicate Detection
    suspend fun isDuplicateStudent(candidate: StudentRecordEntity, excludeId: Long = 0L): Boolean {
        val existing = dao.getAllStudentsOnce()
        val adm = candidate.admissionNumber.trim().lowercase()
        val roll = candidate.rollNumber.trim().lowercase()
        val cls = candidate.className.trim().lowercase()
        val sec = candidate.section.trim().lowercase()
        val name = candidate.studentName.trim().lowercase()

        return existing.any { item ->
            if (item.id == excludeId) return@any false
            val sameAdm = adm.isNotBlank() && item.admissionNumber.trim().lowercase() == adm
            val sameRollInClass = roll.isNotBlank() && cls.isNotBlank() &&
                item.rollNumber.trim().lowercase() == roll &&
                item.className.trim().lowercase() == cls &&
                item.section.trim().lowercase() == sec
            val exactNameAndDob = name.isNotBlank() &&
                item.studentName.trim().lowercase() == name &&
                candidate.dateOfBirth.isNotBlank() &&
                item.dateOfBirth.trim() == candidate.dateOfBirth.trim()
            sameAdm || sameRollInClass || exactNameAndDob
        }
    }

    suspend fun addStudentIfUnique(student: StudentRecordEntity): Result<Long> {
        if (student.studentName.isBlank()) {
            return Result.failure(IllegalArgumentException("Student name is required."))
        }
        if (isDuplicateStudent(student, student.id)) {
            return Result.failure(
                IllegalStateException("Duplicate student detected (matching Admission No, Roll No in class, or Name + DOB).")
            )
        }
        val id = dao.insertStudent(student)
        return Result.success(id)
    }

    suspend fun importStudentsFilteringDuplicates(students: List<StudentRecordEntity>): Pair<Int, Int> {
        val existing = dao.getAllStudentsOnce().toMutableList()
        val uniqueToInsert = mutableListOf<StudentRecordEntity>()
        var duplicatesSkipped = 0

        for (candidate in students) {
            val adm = candidate.admissionNumber.trim().lowercase()
            val roll = candidate.rollNumber.trim().lowercase()
            val cls = candidate.className.trim().lowercase()
            val sec = candidate.section.trim().lowercase()
            val name = candidate.studentName.trim().lowercase()

            val dup = existing.any { item ->
                val sameAdm = adm.isNotBlank() && item.admissionNumber.trim().lowercase() == adm
                val sameRoll = roll.isNotBlank() && cls.isNotBlank() &&
                    item.rollNumber.trim().lowercase() == roll &&
                    item.className.trim().lowercase() == cls &&
                    item.section.trim().lowercase() == sec
                val sameName = name.isNotBlank() && item.studentName.trim().lowercase() == name &&
                    candidate.fatherName.isNotBlank() &&
                    item.fatherName.trim().lowercase() == candidate.fatherName.trim().lowercase()
                sameAdm || sameRoll || sameName
            }
            if (dup) {
                duplicatesSkipped++
            } else {
                uniqueToInsert.add(candidate)
                existing.add(candidate)
            }
        }
        if (uniqueToInsert.isNotEmpty()) {
            dao.insertStudents(uniqueToInsert)
        }
        return uniqueToInsert.size to duplicatesSkipped
    }

    suspend fun setStudentSelected(id: Long, selected: Boolean) = dao.setStudentSelected(id, selected)
    suspend fun setAllStudentsSelected(selected: Boolean) = dao.setAllStudentsSelected(selected)
    suspend fun deleteStudent(id: Long) = dao.deleteStudentById(id)
    suspend fun clearAllStudents() = dao.clearAllStudents()
    suspend fun getSelectedStudents(): List<StudentRecordEntity> = dao.getSelectedStudents()

    // Export History
    suspend fun recordExport(entry: ExportHistoryEntity): Long = dao.insertExportHistory(entry)
    suspend fun getExportById(id: Long): ExportHistoryEntity? = dao.getExportById(id)

    suspend fun renameExportFile(id: Long, newDisplayName: String): String? = withContext(Dispatchers.IO) {
        val entry = dao.getExportById(id) ?: return@withContext null
        val sanitized = newDisplayName
            .replace(Regex("[^A-Za-z0-9_\\- \\u0900-\\u097F]"), "_")
            .trim()
            .ifBlank { "idvanta_document" }
        val finalName = if (sanitized.endsWith(".pdf", ignoreCase = true)) sanitized else "$sanitized.pdf"
        val oldFile = File(entry.filePath)
        val newFile = File(oldFile.parentFile ?: File(context.filesDir, "exports"), finalName)
        if (oldFile.exists() && oldFile.absolutePath != newFile.absolutePath) {
            val renamed = oldFile.renameTo(newFile)
            if (renamed) {
                dao.updateExportFile(id, finalName, newFile.absolutePath)
                return@withContext newFile.absolutePath
            }
        }
        entry.filePath
    }

    suspend fun deleteExport(id: Long) = withContext(Dispatchers.IO) {
        val entry = dao.getExportById(id)
        if (entry != null) {
            runCatching { File(entry.filePath).delete() }
            dao.deleteExportById(id)
        }
    }

    suspend fun clearExportCache() = withContext(Dispatchers.IO) {
        val dir = File(context.filesDir, "exports")
        if (dir.exists()) {
            dir.listFiles()?.forEach { it.delete() }
        }
        dao.clearExportHistory()
    }

    suspend fun getStorageStats(): Pair<Int, Long> = withContext(Dispatchers.IO) {
        val exportsDir = File(context.filesDir, "exports")
        val files = exportsDir.listFiles()?.filter { it.isFile }.orEmpty()
        val mediaDir = File(context.filesDir, "media")
        val mediaFiles = mediaDir.listFiles()?.filter { it.isFile }.orEmpty()
        val totalBytes = files.sumOf { it.length() } + mediaFiles.sumOf { it.length() }
        files.size to totalBytes
    }

    suspend fun copyUriToInternalMedia(uri: Uri, prefix: String = "img"): String = withContext(Dispatchers.IO) {
        val mediaDir = File(context.filesDir, "media").apply { mkdirs() }
        val outFile = File(mediaDir, "${prefix}_${UUID.randomUUID()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            outFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        if (outFile.exists() && outFile.length() > 0L) outFile.absolutePath else uri.toString()
    }
}
