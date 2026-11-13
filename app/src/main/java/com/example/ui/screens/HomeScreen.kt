package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.DocumentEntity
import com.example.domain.model.DocumentCategory
import com.example.domain.model.DocumentTemplate
import com.example.domain.model.IdCardType
import com.example.ui.components.CopyrightFooter
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.IdvantaBrandMark
import com.example.ui.components.QuickActionTile
import com.example.ui.components.SectionHeaderLabel
import com.example.ui.components.parseHexColor
import com.example.ui.theme.DeepNavy
import com.example.ui.viewmodel.HomeViewModel
import com.example.util.AppLanguage
import com.example.util.AppStrings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    language: AppLanguage,
    onCreateCv: (DocumentCategory) -> Unit,
    onCreateIdCard: (IdCardType) -> Unit,
    onCreateBiodata: () -> Unit,
    onOpenBulkGenerator: () -> Unit,
    onOpenTemplateLibrary: (String) -> Unit,
    onOpenMyDocuments: (String) -> Unit,
    onOpenExistingDocument: (DocumentEntity) -> Unit,
    onUseTemplate: (DocumentTemplate) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val labels = AppStrings.forLanguage(language)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        IdvantaBrandMark(sizeDp = 32)
                        Text(
                            text = "IDVANTA",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onOpenAbout,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("home_about_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "About Us"
                        )
                    }
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("home_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 900.dp)
                    .fillMaxSize()
                    .testTag("home_screen"),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Search Bar
                item {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = viewModel::updateSearchQuery,
                        placeholder = { Text(labels.searchPlaceholder) },
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = "Search documents")
                        },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Clear search")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_search_input")
                    )
                }

                // Category Chips
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionHeaderLabel(title = labels.categories)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AssistChip(
                                onClick = { onOpenMyDocuments("CVS") },
                                label = { Text("CVs") },
                                leadingIcon = { Icon(Icons.Filled.Description, null, modifier = Modifier.size(18.dp)) }
                            )
                            AssistChip(
                                onClick = { onOpenMyDocuments("RESUMES") },
                                label = { Text("Resumes") },
                                leadingIcon = { Icon(Icons.Filled.Work, null, modifier = Modifier.size(18.dp)) }
                            )
                            AssistChip(
                                onClick = { onOpenMyDocuments("ID_CARDS") },
                                label = { Text("ID Cards") },
                                leadingIcon = { Icon(Icons.Filled.Badge, null, modifier = Modifier.size(18.dp)) }
                            )
                            AssistChip(
                                onClick = { onOpenMyDocuments("BIODATA") },
                                label = { Text("Biodata") },
                                leadingIcon = { Icon(Icons.Filled.Person, null, modifier = Modifier.size(18.dp)) }
                            )
                            AssistChip(
                                onClick = { onOpenMyDocuments("PDFS") },
                                label = { Text("Exported PDFs") },
                                leadingIcon = { Icon(Icons.Filled.FolderOpen, null, modifier = Modifier.size(18.dp)) }
                            )
                        }
                    }
                }

                // Responsive 10-Module Grid (no text truncation)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SectionHeaderLabel(title = labels.quickActions)

                        val actionItems = listOf(
                            Triple(labels.createCv, "Academic & Student CVs", Icons.Filled.Description) to { onCreateCv(DocumentCategory.CV) },
                            Triple(labels.resumeMaker, "20 ATS & Modern layouts", Icons.Filled.Work) to { onCreateCv(DocumentCategory.RESUME) },
                            Triple(labels.schoolIdCard, "CR80 Front & Back + QR", Icons.Filled.School) to { onCreateIdCard(IdCardType.SCHOOL) },
                            Triple(labels.collegeIdCard, "Campus & Student pass", Icons.Filled.Badge) to { onCreateIdCard(IdCardType.COLLEGE) },
                            Triple(labels.employeeIdCard, "Corporate staff IDs", Icons.Filled.Badge) to { onCreateIdCard(IdCardType.EMPLOYEE) },
                            Triple(labels.teacherIdCard, "Faculty & educator IDs", Icons.Filled.School) to { onCreateIdCard(IdCardType.TEACHER) },
                            Triple(labels.personalBiodata, "Hindi & English dossiers", Icons.Filled.Person) to onCreateBiodata,
                            Triple(labels.bulkIdGenerator, "CSV import & batch PDF", Icons.Filled.Groups) to onOpenBulkGenerator,
                            Triple(labels.templateLibrary, "41 distinct layouts", Icons.Filled.Style) to { onOpenTemplateLibrary("ALL") },
                            Triple(labels.myDocuments, "Drafts, PDFs & Favorites", Icons.Filled.FolderOpen) to { onOpenMyDocuments("ALL") }
                        )

                        val palette = listOf(
                            MaterialTheme.colorScheme.primary,
                            DeepNavy,
                            Color(0xFF0D7377),
                            Color(0xFF4C35B5),
                            Color(0xFF1E3A5F),
                            Color(0xFF6A1B4D),
                            Color(0xFF1B6B4A),
                            Color(0xFF3949AB),
                            Color(0xFF7B1E3C),
                            Color(0xFF283593)
                        )

                        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                            val columns = when {
                                maxWidth >= 680.dp -> 3
                                maxWidth < 280.dp -> 1
                                else -> 2
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                actionItems.chunked(columns).forEachIndexed { rowIdx, rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        rowItems.forEachIndexed { colIdx, (meta, action) ->
                                            val idx = rowIdx * columns + colIdx
                                            QuickActionTile(
                                                title = meta.first,
                                                subtitle = meta.second,
                                                icon = meta.third,
                                                tint = palette[idx % palette.size],
                                                testTag = "home_action_$idx",
                                                onClick = action,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        repeat(columns - rowItems.size) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Favorite Templates Carousel
                if (uiState.favoriteTemplates.isNotEmpty()) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            SectionHeaderLabel(
                                title = labels.favoriteTemplates,
                                trailingAction = {
                                    TextButton(onClick = { onOpenTemplateLibrary("ALL") }) {
                                        Text("Browse All")
                                    }
                                }
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(uiState.favoriteTemplates, key = { it.id }) { tpl ->
                                    val accent = parseHexColor(tpl.accentHex)
                                    ElevatedCard(
                                        onClick = { onUseTemplate(tpl) },
                                        modifier = Modifier
                                            .width(210.dp)
                                            .testTag("fav_tpl_${tpl.id}"),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.elevatedCardColors(
                                            containerColor = MaterialTheme.colorScheme.surface
                                        )
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(accent.copy(alpha = 0.14f))
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text(
                                                        text = tpl.targetCategory.displayName,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = accent
                                                    )
                                                }
                                                IconButton(
                                                    onClick = { viewModel.toggleFavoriteTemplate(tpl.id) },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (tpl.id in uiState.favoriteTemplateIds) {
                                                            Icons.Filled.Favorite
                                                        } else {
                                                            Icons.Outlined.FavoriteBorder
                                                        },
                                                        contentDescription = "Toggle favorite template",
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = tpl.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = tpl.subtitle,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Recently Edited Documents
                item {
                    SectionHeaderLabel(
                        title = labels.recentDocuments,
                        trailingAction = {
                            TextButton(onClick = { onOpenMyDocuments("ALL") }) {
                                Text("View All")
                            }
                        }
                    )
                }

                if (uiState.recentDocuments.isEmpty()) {
                    item {
                        EmptyStateCard(
                            icon = Icons.Outlined.Description,
                            title = labels.noDocumentsYet,
                            message = labels.noDocumentsTip,
                            actionLabel = "Create Resume",
                            onAction = { onCreateCv(DocumentCategory.RESUME) }
                        )
                    }
                } else {
                    items(uiState.recentDocuments, key = { it.id }) { doc ->
                        val dateText = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                            .format(Date(doc.updatedAt))
                        Surface(
                            onClick = { onOpenExistingDocument(doc) },
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("recent_doc_${doc.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Description,
                                        contentDescription = doc.title,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = doc.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${doc.category.replace('_', ' ')} • $dateText",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (doc.isDraft) {
                                    Surface(
                                        shape = RoundedCornerShape(100.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer
                                    ) {
                                        Text(
                                            text = "Draft",
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
