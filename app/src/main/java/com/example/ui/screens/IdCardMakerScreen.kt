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
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.IdCardType
import com.example.domain.model.TemplateCatalog
import com.example.pdf.IdPdfOutputFormat
import com.example.ui.components.DesignEditorPanel
import com.example.ui.components.PhotoPickerRow
import com.example.ui.components.UndoRedoControls
import com.example.ui.preview.IdCardLivePreview
import com.example.ui.viewmodel.IdCardMakerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdCardMakerScreen(
    documentId: Long,
    cardType: String,
    initialTemplateId: String,
    onNavigateBack: () -> Unit,
    onPdfExported: (exportId: Long, filePath: String, title: String) -> Unit,
    viewModel: IdCardMakerViewModel = hiltViewModel()
) {
    LaunchedEffect(documentId, cardType, initialTemplateId) {
        viewModel.initialize(documentId, cardType, initialTemplateId)
    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var activeTab by remember { mutableIntStateOf(0) }

    val personPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { viewModel.attachPersonPhoto(it) } }

    val logoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { viewModel.attachInstitutionLogo(it) } }

    LaunchedEffect(state.statusMessage) {
        state.statusMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(state.data.cardType.displayName) },
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
                        onClick = { viewModel.exportPdf(IdPdfOutputFormat.A4_PRINT_SHEET, onPdfExported) },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("id_export_pdf_button")
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
                    listOf("Details", "Design", "Card Preview").forEachIndexed { idx, label ->
                        Tab(
                            selected = activeTab == idx,
                            onClick = { activeTab = idx },
                            text = { Text(label) },
                            modifier = Modifier.testTag("id_tab_$idx")
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
                                    label = { Text("Card Record Title") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            item {
                                OutlinedTextField(
                                    value = state.data.institutionName,
                                    onValueChange = { v -> viewModel.updateData { it.copy(institutionName = v) } },
                                    label = {
                                        Text(
                                            when (state.data.cardType) {
                                                IdCardType.SCHOOL, IdCardType.TEACHER -> "School / Institution Name"
                                                IdCardType.COLLEGE -> "College / University Name"
                                                IdCardType.EMPLOYEE -> "Organization / Company Name"
                                            }
                                        )
                                    },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("id_institution_input")
                                )
                            }
                            item {
                                PhotoPickerRow(
                                    label = "Institution / Company Logo",
                                    currentPath = state.data.logoUri,
                                    onPickPhoto = {
                                        logoPicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    onClearPhoto = { viewModel.updateData { it.copy(logoUri = "", useBundledCrest = true) } },
                                    testTag = "id_pick_logo_button"
                                )
                            }
                            item {
                                PhotoPickerRow(
                                    label = "Card Holder Photograph",
                                    currentPath = state.data.photoUri,
                                    onPickPhoto = {
                                        personPhotoPicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    onClearPhoto = { viewModel.updateData { it.copy(photoUri = "") } },
                                    testTag = "id_pick_person_photo_button"
                                )
                            }
                            item {
                                OutlinedTextField(
                                    value = state.data.personName,
                                    onValueChange = { v -> viewModel.updateData { it.copy(personName = v) } },
                                    label = { Text("Full Name (English or हिन्दी)") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("id_person_name_input")
                                )
                            }

                            when (state.data.cardType) {
                                IdCardType.SCHOOL -> {
                                    item {
                                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            OutlinedTextField(
                                                value = state.data.fatherName,
                                                onValueChange = { v -> viewModel.updateData { it.copy(fatherName = v) } },
                                                label = { Text("Father's Name") },
                                                modifier = Modifier.weight(1f)
                                            )
                                            OutlinedTextField(
                                                value = state.data.motherName,
                                                onValueChange = { v -> viewModel.updateData { it.copy(motherName = v) } },
                                                label = { Text("Mother's Name") },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                    item {
                                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            OutlinedTextField(
                                                value = state.data.className,
                                                onValueChange = { v -> viewModel.updateData { it.copy(className = v) } },
                                                label = { Text("Class") },
                                                modifier = Modifier.weight(1f)
                                            )
                                            OutlinedTextField(
                                                value = state.data.section,
                                                onValueChange = { v -> viewModel.updateData { it.copy(section = v) } },
                                                label = { Text("Section") },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                    item {
                                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            OutlinedTextField(
                                                value = state.data.rollNumber,
                                                onValueChange = { v -> viewModel.updateData { it.copy(rollNumber = v) } },
                                                label = { Text("Roll Number") },
                                                modifier = Modifier.weight(1f)
                                            )
                                            OutlinedTextField(
                                                value = state.data.admissionNumber,
                                                onValueChange = { v -> viewModel.updateData { it.copy(admissionNumber = v) } },
                                                label = { Text("Admission Number") },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
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
                                                value = state.data.bloodGroup,
                                                onValueChange = { v -> viewModel.updateData { it.copy(bloodGroup = v) } },
                                                label = { Text("Blood Group (Optional)") },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }

                                IdCardType.COLLEGE -> {
                                    item {
                                        OutlinedTextField(
                                            value = state.data.registrationNumber,
                                            onValueChange = { v -> viewModel.updateData { it.copy(registrationNumber = v) } },
                                            label = { Text("Registration Number") },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                    item {
                                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            OutlinedTextField(
                                                value = state.data.course,
                                                onValueChange = { v -> viewModel.updateData { it.copy(course = v) } },
                                                label = { Text("Course") },
                                                modifier = Modifier.weight(1f)
                                            )
                                            OutlinedTextField(
                                                value = state.data.department,
                                                onValueChange = { v -> viewModel.updateData { it.copy(department = v) } },
                                                label = { Text("Department") },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }

                                IdCardType.EMPLOYEE -> {
                                    item {
                                        OutlinedTextField(
                                            value = state.data.employeeNumber,
                                            onValueChange = { v -> viewModel.updateData { it.copy(employeeNumber = v) } },
                                            label = { Text("Employee ID") },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                    item {
                                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            OutlinedTextField(
                                                value = state.data.designation,
                                                onValueChange = { v -> viewModel.updateData { it.copy(designation = v) } },
                                                label = { Text("Designation") },
                                                modifier = Modifier.weight(1f)
                                            )
                                            OutlinedTextField(
                                                value = state.data.department,
                                                onValueChange = { v -> viewModel.updateData { it.copy(department = v) } },
                                                label = { Text("Department") },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                    item {
                                        OutlinedTextField(
                                            value = state.data.joiningDate,
                                            onValueChange = { v -> viewModel.updateData { it.copy(joiningDate = v) } },
                                            label = { Text("Joining Date") },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }

                                IdCardType.TEACHER -> {
                                    item {
                                        OutlinedTextField(
                                            value = state.data.employeeNumber,
                                            onValueChange = { v -> viewModel.updateData { it.copy(employeeNumber = v) } },
                                            label = { Text("Employee Number") },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                    item {
                                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            OutlinedTextField(
                                                value = state.data.designation,
                                                onValueChange = { v -> viewModel.updateData { it.copy(designation = v) } },
                                                label = { Text("Designation") },
                                                modifier = Modifier.weight(1f)
                                            )
                                            OutlinedTextField(
                                                value = state.data.department,
                                                onValueChange = { v -> viewModel.updateData { it.copy(department = v) } },
                                                label = { Text("Department") },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                    item {
                                        OutlinedTextField(
                                            value = state.data.qualification,
                                            onValueChange = { v -> viewModel.updateData { it.copy(qualification = v) } },
                                            label = { Text("Qualification") },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }

                            item {
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = state.data.contactNumber,
                                        onValueChange = { v -> viewModel.updateData { it.copy(contactNumber = v) } },
                                        label = { Text("Contact Number") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = state.data.emergencyContact,
                                        onValueChange = { v -> viewModel.updateData { it.copy(emergencyContact = v) } },
                                        label = { Text("Emergency Contact") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                            item {
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = state.data.academicSession,
                                        onValueChange = { v -> viewModel.updateData { it.copy(academicSession = v) } },
                                        label = { Text("Academic Session / Year") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = state.data.validityDate,
                                        onValueChange = { v -> viewModel.updateData { it.copy(validityDate = v) } },
                                        label = { Text("Validity Date") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                            item {
                                OutlinedTextField(
                                    value = state.data.address,
                                    onValueChange = { v -> viewModel.updateData { it.copy(address = v) } },
                                    label = { Text("Residential / Campus Address") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Institution-Authorized Official ID", style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            text = if (state.design.institutionAuthorized) {
                                                "Official card mode (no SAMPLE watermark)"
                                            } else {
                                                "Personal demonstration mode (shows SAMPLE watermark)"
                                            },
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Switch(
                                        checked = state.design.institutionAuthorized,
                                        onCheckedChange = { auth ->
                                            viewModel.updateDesign(state.design.copy(institutionAuthorized = auth))
                                        }
                                    )
                                }
                            }
                        }

                        1 -> {
                            item {
                                DesignEditorPanel(
                                    design = state.design,
                                    templates = TemplateCatalog.idTemplates,
                                    showSectionReorder = false,
                                    showIdCardControls = true,
                                    onDesignChanged = viewModel::updateDesign,
                                    onTemplateSelected = viewModel::applyTemplate
                                )
                            }
                        }

                        else -> {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (state.showBackSide) "Back Side (CR80)" else "Front Side (CR80)",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    OutlinedButton(
                                        onClick = viewModel::toggleSide,
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .testTag("id_flip_side_button")
                                    ) {
                                        Icon(Icons.Filled.Flip, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (state.showBackSide) "Show Front" else "Show Back")
                                    }
                                }
                            }
                            item {
                                IdCardLivePreview(
                                    data = state.data,
                                    design = state.design,
                                    showBackSide = state.showBackSide
                                )
                            }
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { viewModel.exportPdf(IdPdfOutputFormat.CR80_FRONT_BACK, onPdfExported) },
                                        enabled = !state.isExporting,
                                        modifier = Modifier
                                            .weight(1f)
                                            .minimumInteractiveComponentSize()
                                            .testTag("id_export_cr80_button")
                                    ) {
                                        Text("CR80 Card PDF")
                                    }
                                    Button(
                                        onClick = { viewModel.exportPdf(IdPdfOutputFormat.A4_PRINT_SHEET, onPdfExported) },
                                        enabled = !state.isExporting,
                                        modifier = Modifier
                                            .weight(1f)
                                            .minimumInteractiveComponentSize()
                                            .testTag("id_export_a4_button")
                                    ) {
                                        Text("A4 Print Sheet PDF")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
