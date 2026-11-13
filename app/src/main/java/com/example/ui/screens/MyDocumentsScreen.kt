package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.DocumentEntity
import com.example.data.local.ExportHistoryEntity
import com.example.pdf.PdfShareAndPrintHelper
import com.example.ui.components.CopyrightFooter
import com.example.ui.components.EmptyStateCard
import com.example.ui.viewmodel.DocumentsTab
import com.example.ui.viewmodel.MyDocumentsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDocumentsScreen(
    initialTab: String,
    onNavigateBack: () -> Unit,
    onOpenDocument: (DocumentEntity) -> Unit,
    onOpenPdfPreview: (exportId: Long, filePath: String, title: String) -> Unit,
    viewModel: MyDocumentsViewModel = hiltViewModel()
) {
    LaunchedEffect(initialTab) {
        viewModel.selectInitialTab(initialTab)
    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var docPendingDelete by remember { mutableStateOf<DocumentEntity?>(null) }
    var exportPendingDelete by remember { mutableStateOf<ExportHistoryEntity?>(null) }
    var docPendingRename by remember { mutableStateOf<DocumentEntity?>(null) }

    LaunchedEffect(state.statusMessage) {
        state.statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("My Documents") },
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
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = viewModel::updateSearchQuery,
                    placeholder = { Text("Search saved documents or PDFs...") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DocumentsTab.entries.forEach { tab ->
                        FilterChip(
                            selected = state.selectedTab == tab,
                            onClick = { viewModel.selectTab(tab) },
                            label = { Text(tab.label) },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (state.selectedTab == DocumentsTab.PDFS) {
                        if (state.exports.isEmpty()) {
                            item {
                                EmptyStateCard(
                                    icon = Icons.Filled.PictureAsPdf,
                                    title = "No exported PDFs yet",
                                    message = "Export any CV, ID card, Biodata, or Bulk batch to view, share, or print it here."
                                )
                            }
                        } else {
                            items(state.exports, key = { it.id }) { exp ->
                                val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                                    .format(Date(exp.createdAt))
                                Surface(
                                    onClick = { onOpenPdfPreview(exp.id, exp.filePath, exp.documentTitle) },
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.PictureAsPdf,
                                            contentDescription = exp.fileName,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = exp.fileName, style = MaterialTheme.typography.titleMedium)
                                            Text(
                                                text = "${exp.pageSize} • ${exp.pageCount} page(s) • ${(exp.fileSizeBytes / 1024).coerceAtLeast(1)} KB • $dateStr",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        IconButton(
                                            onClick = { PdfShareAndPrintHelper.sharePdf(context, exp.filePath, exp.documentTitle) },
                                            modifier = Modifier.minimumInteractiveComponentSize()
                                        ) {
                                            Icon(Icons.Filled.Share, contentDescription = "Share PDF")
                                        }
                                        IconButton(
                                            onClick = { exportPendingDelete = exp },
                                            modifier = Modifier.minimumInteractiveComponentSize()
                                        ) {
                                            Icon(Icons.Filled.Delete, contentDescription = "Delete PDF")
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        if (state.documents.isEmpty()) {
                            item {
                                EmptyStateCard(
                                    icon = Icons.Filled.FolderOpen,
                                    title = "No documents in ${state.selectedTab.label}",
                                    message = "Documents you create or autosave will appear here."
                                )
                            }
                        } else {
                            items(state.documents, key = { it.id }) { doc ->
                                var menuOpen by remember { mutableStateOf(false) }
                                val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                                    .format(Date(doc.updatedAt))
                                Surface(
                                    onClick = { onOpenDocument(doc) },
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("my_doc_${doc.id}")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = doc.title, style = MaterialTheme.typography.titleMedium)
                                            Text(
                                                text = "${doc.category.replace('_', ' ')} • $dateStr",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        IconButton(
                                            onClick = { viewModel.toggleFavorite(doc) },
                                            modifier = Modifier.minimumInteractiveComponentSize()
                                        ) {
                                            Icon(
                                                imageVector = if (doc.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                                contentDescription = "Favorite",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Box {
                                            IconButton(
                                                onClick = { menuOpen = true },
                                                modifier = Modifier.minimumInteractiveComponentSize()
                                            ) {
                                                Icon(Icons.Filled.MoreVert, contentDescription = "Document actions")
                                            }
                                            DropdownMenu(
                                                expanded = menuOpen,
                                                onDismissRequest = { menuOpen = false }
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text("Open & Edit") },
                                                    leadingIcon = { Icon(Icons.Filled.Edit, null) },
                                                    onClick = {
                                                        menuOpen = false
                                                        onOpenDocument(doc)
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("Duplicate") },
                                                    leadingIcon = { Icon(Icons.Filled.ContentCopy, null) },
                                                    onClick = {
                                                        menuOpen = false
                                                        viewModel.duplicate(doc)
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("Rename") },
                                                    leadingIcon = { Icon(Icons.Filled.Edit, null) },
                                                    onClick = {
                                                        menuOpen = false
                                                        docPendingRename = doc
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("Export PDF") },
                                                    leadingIcon = { Icon(Icons.Filled.PictureAsPdf, null) },
                                                    onClick = {
                                                        menuOpen = false
                                                        viewModel.exportDocumentToPdf(doc, onOpenPdfPreview)
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("Delete") },
                                                    leadingIcon = { Icon(Icons.Filled.Delete, null) },
                                                    onClick = {
                                                        menuOpen = false
                                                        docPendingDelete = doc
                                                    }
                                                )
                                            }
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

    docPendingRename?.let { target ->
        var newName by remember(target) { mutableStateOf(target.title) }
        AlertDialog(
            onDismissRequest = { docPendingRename = null },
            title = { Text("Rename Document") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Document Title") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.rename(target.id, newName)
                    docPendingRename = null
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { docPendingRename = null }) { Text("Cancel") }
            }
        )
    }

    docPendingDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { docPendingDelete = null },
            title = { Text("Delete \"${target.title}\"?") },
            text = { Text("This will permanently remove this document from local device storage.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteDocument(target.id)
                    docPendingDelete = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { docPendingDelete = null }) { Text("Cancel") }
            }
        )
    }

    exportPendingDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { exportPendingDelete = null },
            title = { Text("Delete \"${target.fileName}\"?") },
            text = { Text("This will permanently delete the generated PDF file from storage.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteExport(target.id)
                    exportPendingDelete = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { exportPendingDelete = null }) { Text("Cancel") }
            }
        )
    }
}
