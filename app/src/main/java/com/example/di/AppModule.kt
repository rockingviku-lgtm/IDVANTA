package com.example.di

import android.content.Context
import androidx.room.Room
import com.example.data.local.IdvantaDao
import com.example.data.local.IdvantaDatabase
import com.example.data.preferences.UserPreferencesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideIdvantaDatabase(@ApplicationContext context: Context): IdvantaDatabase {
        return Room.databaseBuilder(
            context,
            IdvantaDatabase::class.java,
            "idvanta_studio.db"
        ).fallbackToDestructiveMigration(dropAllTables = false)
            .build()
    }

    @Provides
    fun provideIdvantaDao(database: IdvantaDatabase): IdvantaDao {
        return database.idvantaDao()
    }

    @Provides
    @Singleton
    fun provideUserPreferencesRepository(
        @ApplicationContext context: Context
    ): UserPreferencesRepository {
        return UserPreferencesRepository(context)
    }
}
