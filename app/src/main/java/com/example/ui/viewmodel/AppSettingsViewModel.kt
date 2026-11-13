package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.preferences.DefaultExportFormat
import com.example.data.preferences.UserPreferences
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.DocumentRepository
import com.example.ui.theme.ThemeMode
import com.example.util.AppLanguage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class AppSettingsViewModel @Inject constructor(
    private val prefsRepo: UserPreferencesRepository,
    private val docRepo: DocumentRepository
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = prefsRepo.preferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences())

    private val _storageStats = MutableStateFlow(0 to 0L)
    val storageStats: StateFlow<Pair<Int, Long>> = _storageStats.asStateFlow()

    init {
        refreshStorageStats()
    }

    fun refreshStorageStats() {
        viewModelScope.launch {
            _storageStats.value = docRepo.getStorageStats()
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { prefsRepo.setThemeMode(mode) }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch { prefsRepo.setLanguage(language) }
    }

    fun setDefaultExportFormat(format: DefaultExportFormat) {
        viewModelScope.launch { prefsRepo.setDefaultExportFormat(format) }
    }

    fun setDefaultInstitutionName(name: String) {
        viewModelScope.launch { prefsRepo.setDefaultInstitutionName(name) }
    }

    fun setHighResImages(enabled: Boolean) {
        viewModelScope.launch { prefsRepo.setHighResImages(enabled) }
    }

    fun setInstitutionAuthorizedDefault(authorized: Boolean) {
        viewModelScope.launch { prefsRepo.setInstitutionAuthorizedDefault(authorized) }
    }

    fun clearExportCache() {
        viewModelScope.launch {
            docRepo.clearExportCache()
            refreshStorageStats()
        }
    }
}
