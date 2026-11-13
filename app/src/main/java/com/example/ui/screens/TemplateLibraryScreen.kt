package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.DocumentTemplate
import com.example.domain.model.TemplateCatalog
import com.example.domain.model.TemplateCategory
import com.example.ui.components.CopyrightFooter
import com.example.ui.components.parseHexColor
import com.example.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateLibraryScreen(
    initialFilter: String,
    onNavigateBack: () -> Unit,
    onSelectTemplate: (DocumentTemplate) -> Unit,
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val homeState by homeViewModel.uiState.collectAsStateWithLifecycle()
    var typeFilter by remember { mutableStateOf(initialFilter.ifBlank { "ALL" }) }
    var styleFilter by remember { mutableStateOf<TemplateCategory?>(null) }

    val filteredTemplates = remember(typeFilter, styleFilter) {
        val byType = when (typeFilter) {
            "CV" -> TemplateCatalog.cvTemplates
            "ID" -> TemplateCatalog.idTemplates
            "BIODATA" -> TemplateCatalog.biodataTemplates
            else -> TemplateCatalog.allTemplates
        }
        if (styleFilter == null) byType else byType.filter { it.styleCategory == styleFilter }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Template Library (${filteredTemplates.size})") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.minimumInteractiveComponentSize()) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 880.dp)
                    .fillMaxSize()
            ) {
                // Document Type Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "ALL" to "All (${TemplateCatalog.allTemplates.size})",
                        "CV" to "CV & Resume (${TemplateCatalog.cvTemplates.size})",
                        "ID" to "ID Cards (${TemplateCatalog.idTemplates.size})",
                        "BIODATA" to "Biodata (${TemplateCatalog.biodataTemplates.size})"
                    ).forEach { (key, label) ->
                        FilterChip(
                            selected = typeFilter == key,
                            onClick = { typeFilter = key },
                            label = { Text(label) },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        )
                    }
                }

                // Style Category Filters
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = styleFilter == null,
                        onClick = { styleFilter = null },
                        label = { Text("All Styles") }
                    )
                    TemplateCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = styleFilter == cat,
                            onClick = { styleFilter = if (styleFilter == cat) null else cat },
                            label = { Text(cat.displayName) }
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredTemplates, key = { it.id }) { tpl ->
                        val accent = parseHexColor(tpl.accentHex)
                        val isFav = tpl.id in homeState.favoriteTemplateIds
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("library_tpl_${tpl.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Mini layout swatch preview
                                Box(
                                    modifier = Modifier
                                        .size(width = 58.dp, height = 74.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(parseHexColor(tpl.backgroundHex, Color.White))
                                        .padding(4.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(14.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(accent)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(0.75f)
                                                .height(5.dp)
                                                .background(accent.copy(alpha = 0.35f))
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(4.dp)
                                                .background(Color.Gray.copy(alpha = 0.25f))
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(0.85f)
                                                .height(4.dp)
                                                .background(Color.Gray.copy(alpha = 0.25f))
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = tpl.targetCategory.displayName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = accent,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "• ${tpl.styleCategory.displayName}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = tpl.name,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = tpl.subtitle,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { onSelectTemplate(tpl) },
                                            modifier = Modifier.minimumInteractiveComponentSize()
                                        ) {
                                            Text("Use Template")
                                        }
                                        IconButton(
                                            onClick = { homeViewModel.toggleFavoriteTemplate(tpl.id) },
                                            modifier = Modifier.minimumInteractiveComponentSize()
                                        ) {
                                            Icon(
                                                imageVector = if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                                contentDescription = "Favorite template",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    item {
                        CopyrightFooter()
                    }
                }
            }
        }
    }
}
