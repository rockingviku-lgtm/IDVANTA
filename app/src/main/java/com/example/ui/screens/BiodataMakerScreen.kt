package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.TemplateCatalog
import com.example.ui.components.DesignEditorPanel
import com.example.ui.components.PhotoPickerRow
import com.example.ui.components.UndoRedoControls
import com.example.ui.preview.BiodataLivePreviewCard
import com.example.ui.viewmodel.BiodataMakerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BiodataMakerScreen(
    documentId: Long,
    initialTemplateId: String,
    onNavigateBack: () -> Unit,
    onPdfExported: (exportId: Long, filePath: String, title: String) -> Unit,
    viewModel: BiodataMakerViewModel = hiltViewModel()
) {
    LaunchedEffect(documentId, initialTemplateId) {
        viewModel.initialize(documentId, initialTemplateId)
    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var activeTab by remember { mutableIntStateOf(0) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { viewModel.attachPhoto(it) } }

    LaunchedEffect(state.statusMessage) {
        state.statusMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Personal Biodata") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.minimumInteractiveComponentSize()) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    UndoRedoControls(
                        canUndo = state.canUndo,
                        canRedo = state.canRedo,
                        onUndo = viewModel::undo,
                        onRedo = viewModel::redo
                    )
                    IconButton(onClick = viewModel::saveDraft, modifier = Modifier.minimumInteractiveComponentSize()) {
                        Icon(Icons.Filled.Save, contentDescription = "Save draft")
                    }
                    IconButton(
                        onClick = { viewModel.exportPdf(onPdfExported) },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("biodata_export_pdf_button")
                    ) {
                        Icon(Icons.Filled.PictureAsPdf, contentDescription = "Export PDF")
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
                TabRow(selectedTabIndex = activeTab) {
                    listOf("Details", "Design", "Preview").forEachIndexed { idx, label ->
                        Tab(
                            selected = activeTab == idx,
                            onClick = { activeTab = idx },
                            text = { Text(label) }
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (activeTab) {
                        0 -> {
                            item {
                                OutlinedTextField(
                                    value = state.title,
                                    onValueChange = viewModel::updateTitle,
                                    label = { Text("Document Title") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            item {
                                PhotoPickerRow(
                                    label = "Portrait Photograph",
                                    currentPath = state.data.photoUri,
                                    onPickPhoto = {
                                        photoPicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    onClearPhoto = { viewModel.updateData { it.copy(photoUri = "") } },
                                    testTag = "biodata_pick_photo"
                                )
                            }
                            item {
                                OutlinedTextField(
                                    value = state.data.name,
                                    onValueChange = { v -> viewModel.updateData { it.copy(name = v) } },
                                    label = { Text("Full Name (English or हिन्दी)") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("biodata_name_input")
                                )
                            }
                            item {
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = state.data.dateOfBirth,
                                        onValueChange = { v -> viewModel.updateData { it.copy(dateOfBirth = v) } },
                                        label = { Text("Date of Birth") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = state.data.maritalStatus,
                                        onValueChange = { v -> viewModel.updateData { it.copy(maritalStatus = v) } },
                                        label = { Text("Marital Status") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                            item {
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = state.data.phone,
                                        onValueChange = { v -> viewModel.updateData { it.copy(phone = v) } },
                                        label = { Text("Phone Number") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = state.data.email,
                                        onValueChange = { v -> viewModel.updateData { it.copy(email = v) } },
                                        label = { Text("Email") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                            item {
                                OutlinedTextField(
                                    value = state.data.address,
                                    onValueChange = { v -> viewModel.updateData { it.copy(address = v) } },
                                    label = { Text("Address") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            item {
                                OutlinedTextField(
                                    value = state.data.introduction,
                                    onValueChange = { v -> viewModel.updateData { it.copy(introduction = v) } },
                                    label = { Text("Personal Introduction") },
                                    minLines = 3,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            item {
                                OutlinedTextField(
                                    value = state.data.educationSummary,
                                    onValueChange = { v -> viewModel.updateData { it.copy(educationSummary = v) } },
                                    label = { Text("Education Summary") },
                                    minLines = 2,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            item {
                                OutlinedTextField(
                                    value = state.data.occupation,
                                    onValueChange = { v -> viewModel.updateData { it.copy(occupation = v) } },
                                    label = { Text("Occupation / Profession") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            item {
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = state.data.fatherName,
                                        onValueChange = { v -> viewModel.updateData { it.copy(fatherName = v) } },
                                        label = { Text("Father's Name") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = state.data.fatherOccupation,
                                        onValueChange = { v -> viewModel.updateData { it.copy(fatherOccupation = v) } },
                                        label = { Text("Father's Occupation") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                            item {
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = state.data.motherName,
                                        onValueChange = { v -> viewModel.updateData { it.copy(motherName = v) } },
                                        label = { Text("Mother's Name") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = state.data.motherOccupation,
                                        onValueChange = { v -> viewModel.updateData { it.copy(motherOccupation = v) } },
                                        label = { Text("Mother's Occupation") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                            item {
                                OutlinedTextField(
                                    value = state.data.siblingsInfo,
                                    onValueChange = { v -> viewModel.updateData { it.copy(siblingsInfo = v) } },
                                    label = { Text("Siblings Information") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            item {
                                var langText by remember(state.data.languages) {
                                    mutableStateOf(state.data.languages.joinToString(", "))
                                }
                                OutlinedTextField(
                                    value = langText,
                                    onValueChange = { raw ->
                                        langText = raw
                                        viewModel.updateData {
                                            it.copy(languages = raw.split(",").map { s -> s.trim() }.filter { s -> s.isNotBlank() })
                                        }
                                    },
                                    label = { Text("Languages (comma-separated)") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            item {
                                var skillsText by remember(state.data.skills) {
                                    mutableStateOf(state.data.skills.joinToString(", "))
                                }
                                OutlinedTextField(
                                    value = skillsText,
                                    onValueChange = { raw ->
                                        skillsText = raw
                                        viewModel.updateData {
                                            it.copy(skills = raw.split(",").map { s -> s.trim() }.filter { s -> s.isNotBlank() })
                                        }
                                    },
                                    label = { Text("Skills (comma-separated)") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        1 -> {
                            item {
                                DesignEditorPanel(
                                    design = state.design,
                                    templates = TemplateCatalog.biodataTemplates,
                                    showSectionReorder = false,
                                    showIdCardControls = false,
                                    onDesignChanged = viewModel::updateDesign,
                                    onTemplateSelected = viewModel::applyTemplate
                                )
                            }
                        }

                        else -> {
                            item {
                                BiodataLivePreviewCard(data = state.data, design = state.design)
                            }
                            item {
                                Button(
                                    onClick = { viewModel.exportPdf(onPdfExported) },
                                    enabled = !state.isExporting,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .minimumInteractiveComponentSize()
                                ) {
                                    Icon(Icons.Filled.PictureAsPdf, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(if (state.isExporting) "Rendering PDF..." else "Generate & Preview Biodata PDF")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
