package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.domain.model.CertificationEntry
import com.example.domain.model.CvSubCategory
import com.example.domain.model.EducationEntry
import com.example.domain.model.ProjectEntry
import com.example.domain.model.ReferenceEntry
import com.example.domain.model.TemplateCatalog
import com.example.domain.model.WorkExperienceEntry
import com.example.ui.components.DesignEditorPanel
import com.example.ui.components.PhotoPickerRow
import com.example.ui.components.UndoRedoControls
import com.example.ui.preview.CvLivePreviewCard
import com.example.ui.viewmodel.CvMakerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CvMakerScreen(
    documentId: Long,
    initialCategory: String,
    initialTemplateId: String,
    onNavigateBack: () -> Unit,
    onPdfExported: (exportId: Long, filePath: String, title: String) -> Unit,
    viewModel: CvMakerViewModel = hiltViewModel()
) {
    LaunchedEffect(documentId, initialCategory, initialTemplateId) {
        viewModel.initialize(documentId, initialCategory, initialTemplateId)
    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var activeTab by remember { mutableIntStateOf(0) }
    var showEduDialog by remember { mutableStateOf(false) }
    var showExpDialog by remember { mutableStateOf(false) }
    var showProjDialog by remember { mutableStateOf(false) }
    var showCertDialog by remember { mutableStateOf(false) }
    var showRefDialog by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.attachProfilePhoto(it) }
    }

    LaunchedEffect(state.statusMessage) {
        state.statusMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(state.category.displayName + " Studio") },
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
                    IconButton(
                        onClick = viewModel::saveDraft,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("cv_save_draft_button")
                    ) {
                        Icon(Icons.Filled.Save, contentDescription = "Save draft")
                    }
                    IconButton(
                        onClick = { viewModel.exportPdf(onPdfExported) },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("cv_export_pdf_button")
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
                val tabs = listOf("Personal", "Sections", "Design", "Preview")
                TabRow(selectedTabIndex = activeTab) {
                    tabs.forEachIndexed { idx, label ->
                        Tab(
                            selected = activeTab == idx,
                            onClick = { activeTab = idx },
                            text = { Text(label) },
                            modifier = Modifier.testTag("cv_tab_$idx")
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
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("cv_doc_title_input")
                                )
                            }
                            item {
                                Text("CV / Resume Category", style = MaterialTheme.typography.titleMedium)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CvSubCategory.entries.forEach { sub ->
                                        FilterChip(
                                            selected = state.data.cvSubCategory == sub,
                                            onClick = { viewModel.updateData { it.copy(cvSubCategory = sub) } },
                                            label = { Text(sub.displayName) },
                                            modifier = Modifier.minimumInteractiveComponentSize()
                                        )
                                    }
                                }
                            }
                            item {
                                PhotoPickerRow(
                                    label = "Profile Photograph",
                                    currentPath = state.data.photoUri,
                                    onPickPhoto = {
                                        photoPicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    onClearPhoto = { viewModel.updateData { it.copy(photoUri = "") } },
                                    testTag = "cv_pick_photo_button"
                                )
                            }
                            item {
                                OutlinedTextField(
                                    value = state.data.fullName,
                                    onValueChange = { v -> viewModel.updateData { it.copy(fullName = v) } },
                                    label = { Text("Full Name (English or हिन्दी)") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("cv_full_name_input")
                                )
                            }
                            item {
                                OutlinedTextField(
                                    value = state.data.professionalHeadline,
                                    onValueChange = { v -> viewModel.updateData { it.copy(professionalHeadline = v) } },
                                    label = { Text("Professional Headline / Target Role") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            item {
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = state.data.phone,
                                        onValueChange = { v -> viewModel.updateData { it.copy(phone = v) } },
                                        label = { Text("Phone Number") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = state.data.email,
                                        onValueChange = { v -> viewModel.updateData { it.copy(email = v) } },
                                        label = { Text("Email") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                            item {
                                OutlinedTextField(
                                    value = state.data.address,
                                    onValueChange = { v -> viewModel.updateData { it.copy(address = v) } },
                                    label = { Text("Address / City") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            item {
                                OutlinedTextField(
                                    value = state.data.linkedInUrl,
                                    onValueChange = { v -> viewModel.updateData { it.copy(linkedInUrl = v) } },
                                    label = { Text("LinkedIn / Portfolio URL") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            item {
                                OutlinedTextField(
                                    value = state.data.careerObjective,
                                    onValueChange = { v -> viewModel.updateData { it.copy(careerObjective = v) } },
                                    label = { Text("Career Objective / Summary") },
                                    minLines = 3,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            item {
                                OutlinedTextField(
                                    value = state.data.signatureText,
                                    onValueChange = { v -> viewModel.updateData { it.copy(signatureText = v) } },
                                    label = { Text("Signature Name (Optional)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        1 -> {
                            // Education
                            item {
                                SectionListHeader("Education (${state.data.education.size})") {
                                    showEduDialog = true
                                }
                            }
                            items(state.data.education.size) { idx ->
                                val edu = state.data.education[idx]
                                EntryCard(
                                    title = "${edu.qualification} — ${edu.institution}",
                                    subtitle = listOf(edu.boardOrUniversity, edu.passingYear, edu.scoreOrCgpa)
                                        .filter { it.isNotBlank() }.joinToString(" • "),
                                    onDelete = { viewModel.removeEducation(edu.id) }
                                )
                            }

                            // Experience
                            item {
                                SectionListHeader("Work Experience (${state.data.experience.size})") {
                                    showExpDialog = true
                                }
                            }
                            items(state.data.experience.size) { idx ->
                                val exp = state.data.experience[idx]
                                EntryCard(
                                    title = "${exp.jobTitle} at ${exp.companyName}",
                                    subtitle = "${exp.duration}\n${exp.responsibilities}",
                                    onDelete = { viewModel.removeExperience(exp.id) }
                                )
                            }

                            // Projects
                            item {
                                SectionListHeader("Projects (${state.data.projects.size})") {
                                    showProjDialog = true
                                }
                            }
                            items(state.data.projects.size) { idx ->
                                val prj = state.data.projects[idx]
                                EntryCard(
                                    title = prj.title,
                                    subtitle = "${prj.roleOrTech}\n${prj.description}",
                                    onDelete = { viewModel.removeProject(prj.id) }
                                )
                            }

                            // Certifications
                            item {
                                SectionListHeader("Certifications (${state.data.certifications.size})") {
                                    showCertDialog = true
                                }
                            }
                            items(state.data.certifications.size) { idx ->
                                val cert = state.data.certifications[idx]
                                EntryCard(
                                    title = cert.name,
                                    subtitle = "${cert.issuer} (${cert.year})",
                                    onDelete = { viewModel.removeCertification(cert.id) }
                                )
                            }

                            // Comma-separated Skills, Languages, Achievements, Hobbies
                            item {
                                CommaListField(
                                    label = "Skills (comma-separated)",
                                    items = state.data.skills,
                                    onSave = { list -> viewModel.updateData { it.copy(skills = list) } }
                                )
                            }
                            item {
                                CommaListField(
                                    label = "Languages (comma-separated, e.g. English, हिन्दी)",
                                    items = state.data.languages,
                                    onSave = { list -> viewModel.updateData { it.copy(languages = list) } }
                                )
                            }
                            item {
                                CommaListField(
                                    label = "Achievements (comma-separated)",
                                    items = state.data.achievements,
                                    onSave = { list -> viewModel.updateData { it.copy(achievements = list) } }
                                )
                            }
                            item {
                                CommaListField(
                                    label = "Hobbies (comma-separated)",
                                    items = state.data.hobbies,
                                    onSave = { list -> viewModel.updateData { it.copy(hobbies = list) } }
                                )
                            }

                            // References
                            item {
                                SectionListHeader("References (${state.data.references.size})") {
                                    showRefDialog = true
                                }
                            }
                            items(state.data.references.size) { idx ->
                                val ref = state.data.references[idx]
                                EntryCard(
                                    title = ref.name,
                                    subtitle = "${ref.designationAndOrg} • ${ref.contact}",
                                    onDelete = { viewModel.removeReference(ref.id) }
                                )
                            }
                        }

                        2 -> {
                            item {
                                DesignEditorPanel(
                                    design = state.design,
                                    templates = TemplateCatalog.cvTemplates,
                                    showSectionReorder = true,
                                    showIdCardControls = false,
                                    onDesignChanged = viewModel::updateDesign,
                                    onTemplateSelected = viewModel::applyTemplate
                                )
                            }
                        }

                        else -> {
                            item {
                                CvLivePreviewCard(
                                    data = state.data,
                                    design = state.design
                                )
                            }
                            item {
                                Button(
                                    onClick = { viewModel.exportPdf(onPdfExported) },
                                    enabled = !state.isExporting,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .minimumInteractiveComponentSize()
                                        .testTag("cv_bottom_export_pdf_button")
                                ) {
                                    Icon(Icons.Filled.PictureAsPdf, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(if (state.isExporting) "Rendering A4 PDF..." else "Generate & Preview A4 PDF")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEduDialog) {
        var inst by remember { mutableStateOf("") }
        var qual by remember { mutableStateOf("") }
        var board by remember { mutableStateOf("") }
        var year by remember { mutableStateOf("") }
        var score by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showEduDialog = false },
            title = { Text("Add Education") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = qual, onValueChange = { qual = it }, label = { Text("Qualification / Degree") })
                    OutlinedTextField(value = inst, onValueChange = { inst = it }, label = { Text("Institution") })
                    OutlinedTextField(value = board, onValueChange = { board = it }, label = { Text("Board / University") })
                    OutlinedTextField(value = year, onValueChange = { year = it }, label = { Text("Passing Year") })
                    OutlinedTextField(value = score, onValueChange = { score = it }, label = { Text("Percentage / CGPA") })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (qual.isNotBlank() || inst.isNotBlank()) {
                        viewModel.addEducation(EducationEntry(institution = inst, qualification = qual, boardOrUniversity = board, passingYear = year, scoreOrCgpa = score))
                    }
                    showEduDialog = false
                }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showEduDialog = false }) { Text("Cancel") } }
        )
    }

    if (showExpDialog) {
        var title by remember { mutableStateOf("") }
        var company by remember { mutableStateOf("") }
        var duration by remember { mutableStateOf("") }
        var resp by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showExpDialog = false },
            title = { Text("Add Work Experience") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Job Title") })
                    OutlinedTextField(value = company, onValueChange = { company = it }, label = { Text("Company / School") })
                    OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text("Duration (e.g. 2022–2026)") })
                    OutlinedTextField(value = resp, onValueChange = { resp = it }, label = { Text("Responsibilities") }, minLines = 2)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (title.isNotBlank() || company.isNotBlank()) {
                        viewModel.addExperience(WorkExperienceEntry(jobTitle = title, companyName = company, duration = duration, responsibilities = resp))
                    }
                    showExpDialog = false
                }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showExpDialog = false }) { Text("Cancel") } }
        )
    }

    if (showProjDialog) {
        var title by remember { mutableStateOf("") }
        var tech by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showProjDialog = false },
            title = { Text("Add Project") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Project Title") })
                    OutlinedTextField(value = tech, onValueChange = { tech = it }, label = { Text("Role / Tech Stack") })
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, minLines = 2)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (title.isNotBlank()) {
                        viewModel.addProject(ProjectEntry(title = title, roleOrTech = tech, description = desc))
                    }
                    showProjDialog = false
                }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showProjDialog = false }) { Text("Cancel") } }
        )
    }

    if (showCertDialog) {
        var name by remember { mutableStateOf("") }
        var issuer by remember { mutableStateOf("") }
        var year by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCertDialog = false },
            title = { Text("Add Certification") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Certificate Name") })
                    OutlinedTextField(value = issuer, onValueChange = { issuer = it }, label = { Text("Issuing Body") })
                    OutlinedTextField(value = year, onValueChange = { year = it }, label = { Text("Year") })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isNotBlank()) {
                        viewModel.addCertification(CertificationEntry(name = name, issuer = issuer, year = year))
                    }
                    showCertDialog = false
                }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showCertDialog = false }) { Text("Cancel") } }
        )
    }

    if (showRefDialog) {
        var name by remember { mutableStateOf("") }
        var org by remember { mutableStateOf("") }
        var contact by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showRefDialog = false },
            title = { Text("Add Reference") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Reference Name") })
                    OutlinedTextField(value = org, onValueChange = { org = it }, label = { Text("Designation & Organization") })
                    OutlinedTextField(value = contact, onValueChange = { contact = it }, label = { Text("Phone / Email") })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isNotBlank()) {
                        viewModel.addReference(ReferenceEntry(name = name, designationAndOrg = org, contact = contact))
                    }
                    showRefDialog = false
                }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showRefDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun SectionListHeader(title: String, onAdd: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        OutlinedButton(onClick = onAdd, modifier = Modifier.minimumInteractiveComponentSize()) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Add")
        }
    }
}

@Composable
private fun EntryCard(
    title: String,
    subtitle: String,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.minimumInteractiveComponentSize()) {
                Icon(Icons.Filled.Delete, contentDescription = "Remove entry")
            }
        }
    }
}

@Composable
private fun CommaListField(
    label: String,
    items: List<String>,
    onSave: (List<String>) -> Unit
) {
    var text by remember(items) { mutableStateOf(items.joinToString(", ")) }
    OutlinedTextField(
        value = text,
        onValueChange = { raw ->
            text = raw
            onSave(raw.split(",").map { it.trim() }.filter { it.isNotBlank() })
        },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth()
    )
}
