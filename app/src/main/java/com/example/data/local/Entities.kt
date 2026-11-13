package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val category: String,
    val subCategory: String = "",
    val templateId: String,
    val payloadJson: String,
    val designConfigJson: String,
    val isFavorite: Boolean = false,
    val isDraft: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "student_records")
data class StudentRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val batchName: String = "Default Batch",
    val schoolName: String = "",
    val studentName: String,
    val fatherName: String = "",
    val motherName: String = "",
    val className: String = "",
    val section: String = "",
    val rollNumber: String = "",
    val admissionNumber: String = "",
    val dateOfBirth: String = "",
    val address: String = "",
    val emergencyContact: String = "",
    val bloodGroup: String = "",
    val academicSession: String = "2026-27",
    val validityDate: String = "",
    val photoUri: String = "",
    val logoUri: String = "",
    val isSelected: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "export_history")
data class ExportHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val documentId: Long = 0L,
    val documentTitle: String,
    val category: String,
    val fileName: String,
    val filePath: String,
    val pageSize: String,
    val pageCount: Int,
    val fileSizeBytes: Long,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorite_templates")
data class FavoriteTemplateEntity(
    @PrimaryKey val templateId: String,
    val addedAt: Long = System.currentTimeMillis()
)
