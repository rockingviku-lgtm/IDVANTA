package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        DocumentEntity::class,
        StudentRecordEntity::class,
        ExportHistoryEntity::class,
        FavoriteTemplateEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class IdvantaDatabase : RoomDatabase() {
    abstract fun idvantaDao(): IdvantaDao
}
