package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DocumentEntity
import com.example.data.repository.DocumentRepository
import com.example.domain.model.DocumentTemplate
import com.example.domain.model.TemplateCatalog
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val searchQuery: String = "",
    val recentDocuments: List<DocumentEntity> = emptyList(),
    val favoriteTemplates: List<DocumentTemplate> = emptyList(),
    val favoriteTemplateIds: Set<String> = emptySet(),
    val totalDocumentCount: Int = 0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: DocumentRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    val uiState: StateFlow<HomeUiState> = combine(
        searchQuery,
        repository.allDocuments,
        repository.favoriteTemplateIds
    ) { query, docs, favIds ->
        val trimmed = query.trim().lowercase()
        val filteredDocs = if (trimmed.isEmpty()) {
            docs.take(8)
        } else {
            docs.filter {
                it.title.lowercase().contains(trimmed) ||
                    it.category.lowercase().contains(trimmed) ||
                    it.subCategory.lowercase().contains(trimmed)
            }
        }
        val favTemplates = favIds.mapNotNull { id ->
            TemplateCatalog.allTemplates.firstOrNull { it.id == id }
        }
        HomeUiState(
            searchQuery = query,
            recentDocuments = filteredDocs,
            favoriteTemplates = favTemplates,
            favoriteTemplateIds = favIds.toSet(),
            totalDocumentCount = docs.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    init {
        viewModelScope.launch {
            val currentFavs = repository.favoriteTemplateIds.first()
            repository.seedInitialFavoriteTemplatesIfNeeded(currentFavs)
        }
    }

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun toggleFavoriteTemplate(templateId: String) {
        viewModelScope.launch {
            val isFav = templateId in uiState.value.favoriteTemplateIds
            repository.toggleFavoriteTemplate(templateId, isFav)
        }
    }
}
