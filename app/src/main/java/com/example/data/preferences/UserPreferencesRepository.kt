package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.theme.ThemeMode
import com.example.util.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "idvanta_settings")

enum class DefaultExportFormat(val label: String) {
    A4("A4 Print-Ready (210 × 297 mm)"),
    CR80("CR80 Standard Card (85.6 × 53.98 mm)")
}

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val defaultExportFormat: DefaultExportFormat = DefaultExportFormat.A4,
    val defaultInstitutionName: String = "",
    val highResImages: Boolean = true,
    val institutionAuthorizedDefault: Boolean = false
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LANGUAGE = stringPreferencesKey("language")
        val EXPORT_FORMAT = stringPreferencesKey("export_format")
        val INSTITUTION_NAME = stringPreferencesKey("institution_name")
        val HIGH_RES_IMAGES = booleanPreferencesKey("high_res_images")
        val INSTITUTION_AUTHORIZED = booleanPreferencesKey("institution_authorized")
    }

    val preferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            themeMode = prefs[Keys.THEME_MODE]?.let {
                runCatching { ThemeMode.valueOf(it) }.getOrNull()
            } ?: ThemeMode.SYSTEM,
            language = prefs[Keys.LANGUAGE]?.let {
                runCatching { AppLanguage.valueOf(it) }.getOrNull()
            } ?: AppLanguage.ENGLISH,
            defaultExportFormat = prefs[Keys.EXPORT_FORMAT]?.let {
                runCatching { DefaultExportFormat.valueOf(it) }.getOrNull()
            } ?: DefaultExportFormat.A4,
            defaultInstitutionName = prefs[Keys.INSTITUTION_NAME].orEmpty(),
            highResImages = prefs[Keys.HIGH_RES_IMAGES] ?: true,
            institutionAuthorizedDefault = prefs[Keys.INSTITUTION_AUTHORIZED] ?: false
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { it[Keys.LANGUAGE] = language.name }
    }

    suspend fun setDefaultExportFormat(format: DefaultExportFormat) {
        context.dataStore.edit { it[Keys.EXPORT_FORMAT] = format.name }
    }

    suspend fun setDefaultInstitutionName(name: String) {
        context.dataStore.edit { it[Keys.INSTITUTION_NAME] = name }
    }

    suspend fun setHighResImages(enabled: Boolean) {
        context.dataStore.edit { it[Keys.HIGH_RES_IMAGES] = enabled }
    }

    suspend fun setInstitutionAuthorizedDefault(authorized: Boolean) {
        context.dataStore.edit { it[Keys.INSTITUTION_AUTHORIZED] = authorized }
    }
}
