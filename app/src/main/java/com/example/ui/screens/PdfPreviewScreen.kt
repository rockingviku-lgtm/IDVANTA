package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.data.repository.DocumentRepository
import com.example.pdf.PdfShareAndPrintHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class PdfPreviewViewModel @Inject constructor(
    private val repository: DocumentRepository
) : androidx.lifecycle.ViewModel() {

    suspend fun renameExport(exportId: Long, newName: String): String? {
        return repository.renameExportFile(exportId, newName)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfPreviewScreen(
    exportId: Long,
    initialFilePath: String,
    initialTitle: String,
    onNavigateBack: () -> Unit,
    viewModel: PdfPreviewViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var currentFilePath by remember { mutableStateOf(initialFilePath) }
    var currentTitle by remember { mutableStateOf(initialTitle) }
    var renderedPages by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showRenameDialog by remember { mutableStateOf(false) }

    val saveAsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val ok = withContext(Dispatchers.IO) {
                    PdfShareAndPrintHelper.copyPdfToUri(context, currentFilePath, uri)
                }
                snackbarHostState.showSnackbar(
                    if (ok) "PDF saved to selected folder" else "Failed to save PDF"
                )
            }
        }
    }

    LaunchedEffect(currentFilePath) {
        isLoading = true
        renderedPages = withContext(Dispatchers.IO) {
            val file = File(currentFilePath)
            if (!file.exists()) return@withContext emptyList()
            runCatching {
                val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                fd.use { pfd ->
                    PdfRenderer(pfd).use { renderer ->
                        (0 until renderer.pageCount).map { pageIndex ->
                            renderer.openPage(pageIndex).use { page ->
                                val scale = 2
                                val bmp = Bitmap.createBitmap(
                                    (page.width * scale).coerceAtLeast(1),
                                    (page.height * scale).coerceAtLeast(1),
                                    Bitmap.Config.ARGB_8888
                                )
                                bmp.eraseColor(android.graphics.Color.WHITE)
                                page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                                bmp
                            }
                        }
                    }
                }
            }.getOrDefault(emptyList())
        }
        isLoading = false
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(currentTitle.ifBlank { "PDF Preview" }) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.minimumInteractiveComponentSize()) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (exportId > 0L) {
                        IconButton(
                            onClick = { showRenameDialog = true },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("pdf_rename_button")
                        ) {
                            Icon(Icons.Filled.DriveFileRenameOutline, contentDescription = "Rename PDF")
                        }
                    }
                    IconButton(
                        onClick = { PdfShareAndPrintHelper.sharePdf(context, currentFilePath, currentTitle) },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("pdf_share_button")
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = "Share PDF")
                    }
                    IconButton(
                        onClick = { PdfShareAndPrintHelper.printPdf(context, currentFilePath, currentTitle) },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("pdf_print_button")
                    ) {
                        Icon(Icons.Filled.Print, contentDescription = "Print PDF")
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { PdfShareAndPrintHelper.printPdf(context, currentFilePath, currentTitle) },
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                    ) {
                        Icon(Icons.Filled.Print, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Print")
                    }
                    Button(
                        onClick = {
                            val suggested = File(currentFilePath).name.ifBlank { "IDVANTA_Document.pdf" }
                            saveAsLauncher.launch(suggested)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("pdf_save_device_button")
                    ) {
                        Icon(Icons.Filled.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save PDF")
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (renderedPages.isEmpty()) {
                Text("Unable to render PDF file.", style = MaterialTheme.typography.bodyLarge)
            } else {
                LazyColumn(
                    modifier = Modifier
                        .widthIn(max = 820.dp)
                        .fillMaxSize()
                        .testTag("pdf_preview_pages"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(renderedPages) { index, bmp ->
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Page ${index + 1} of ${renderedPages.size}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                shadowElevation = 4.dp,
                                color = Color.White,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(bmp.width.toFloat() / bmp.height.toFloat())
                            ) {
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = "PDF page ${index + 1}",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRenameDialog) {
        var newName by remember(currentFilePath) {
            mutableStateOf(File(currentFilePath).nameWithoutExtension)
        }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Exported PDF") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("File Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showRenameDialog = false
                    scope.launch {
                        val updatedPath = viewModel.renameExport(exportId, newName)
                        if (updatedPath != null) {
                            currentFilePath = updatedPath
                            currentTitle = File(updatedPath).nameWithoutExtension
                            snackbarHostState.showSnackbar("File renamed to ${File(updatedPath).name}")
                        }
                    }
                }) { Text("Rename") }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) { Text("Cancel") }
            }
        )
    }
}
