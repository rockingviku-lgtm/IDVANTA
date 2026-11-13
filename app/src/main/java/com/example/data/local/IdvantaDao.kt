package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface IdvantaDao {

    @Query("SELECT * FROM documents ORDER BY updatedAt DESC")
    fun observeAllDocuments(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents ORDER BY updatedAt DESC LIMIT :limit")
    fun observeRecentDocuments(limit: Int = 8): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun getDocumentById(id: Long): DocumentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: DocumentEntity): Long

    @Update
    suspend fun updateDocument(document: DocumentEntity)

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun deleteDocumentById(id: Long)

    @Query("UPDATE documents SET isFavorite = :favorite, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setDocumentFavorite(id: Long, favorite: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE documents SET title = :title, updatedAt = :updatedAt WHERE id = :id")
    suspend fun renameDocument(id: Long, title: String, updatedAt: Long = System.currentTimeMillis())

    // Student Records (Bulk ID Generator)
    @Query("SELECT * FROM student_records ORDER BY className ASC, section ASC, rollNumber ASC, id DESC")
    fun observeAllStudents(): Flow<List<StudentRecordEntity>>

    @Query("SELECT * FROM student_records WHERE isSelected = 1 ORDER BY className ASC, section ASC, rollNumber ASC")
    suspend fun getSelectedStudents(): List<StudentRecordEntity>

    @Query("SELECT * FROM student_records")
    suspend fun getAllStudentsOnce(): List<StudentRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<StudentRecordEntity>)

    @Update
    suspend fun updateStudent(student: StudentRecordEntity)

    @Query("UPDATE student_records SET isSelected = :selected WHERE id = :id")
    suspend fun setStudentSelected(id: Long, selected: Boolean)

    @Query("UPDATE student_records SET isSelected = :selected")
    suspend fun setAllStudentsSelected(selected: Boolean)

    @Query("DELETE FROM student_records WHERE id = :id")
    suspend fun deleteStudentById(id: Long)

    @Query("DELETE FROM student_records")
    suspend fun clearAllStudents()

    // Export History (My PDFs)
    @Query("SELECT * FROM export_history ORDER BY createdAt DESC")
    fun observeExportHistory(): Flow<List<ExportHistoryEntity>>

    @Query("SELECT * FROM export_history WHERE id = :id")
    suspend fun getExportById(id: Long): ExportHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExportHistory(entry: ExportHistoryEntity): Long

    @Query("UPDATE export_history SET fileName = :fileName, filePath = :filePath WHERE id = :id")
    suspend fun updateExportFile(id: Long, fileName: String, filePath: String)

    @Query("DELETE FROM export_history WHERE id = :id")
    suspend fun deleteExportById(id: Long)

    @Query("DELETE FROM export_history")
    suspend fun clearExportHistory()

    // Favorite Templates
    @Query("SELECT templateId FROM favorite_templates ORDER BY addedAt DESC")
    fun observeFavoriteTemplateIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavoriteTemplate(entity: FavoriteTemplateEntity)

    @Query("DELETE FROM favorite_templates WHERE templateId = :templateId")
    suspend fun removeFavoriteTemplate(templateId: String)
}
