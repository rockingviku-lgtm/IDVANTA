package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
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
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.StudentRecordEntity
import com.example.domain.model.TemplateCatalog
import com.example.ui.components.CopyrightFooter
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.PhotoPickerRow
import com.example.ui.viewmodel.BulkIdGeneratorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulkIdGeneratorScreen(
    onNavigateBack: () -> Unit,
    onPdfExported: (exportId: Long, filePath: String, title: String) -> Unit,
    viewModel: BulkIdGeneratorViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddStudentDialog by remember { mutableStateOf(false) }

    val csvPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { viewModel.importCsvFromUri(it) } }

    val logoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { viewModel.attachBatchLogo(it) } }

    LaunchedEffect(state.feedbackMessage) {
        state.feedbackMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearFeedback()
        }
    }

    val selectedCount = state.students.count { it.isSelected }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Bulk Student ID Generator") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.minimumInteractiveComponentSize()) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showAddStudentDialog = true },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("bulk_add_student_button")
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Add student manually")
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
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 880.dp)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = state.batchInstitutionName,
                        onValueChange = viewModel::updateInstitutionName,
                        label = { Text("Default School / Institution Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    PhotoPickerRow(
                        label = "School Logo (Shared for Batch)",
                        currentPath = state.batchLogoUri,
                        onPickPhoto = {
                            logoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        testTag = "bulk_pick_logo"
                    )
                }

                item {
                    Text("Card Template", style = MaterialTheme.typography.titleMedium)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TemplateCatalog.idTemplates.take(8).forEach { tpl ->
                            FilterChip(
                                selected = state.selectedTemplateId == tpl.id,
                                onClick = { viewModel.selectTemplate(tpl.id) },
                                label = { Text(tpl.name) },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { csvPicker.launch(arrayOf("text/*", "text/csv", "application/csv")) },
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("bulk_import_csv_button")
                        ) {
                            Icon(Icons.Filled.UploadFile, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Import CSV")
                        }
                        OutlinedButton(
                            onClick = viewModel::loadSampleCsv,
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("bulk_load_sample_button")
                        ) {
                            Text("Load Sample CSV")
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Include Back-Side Print Pages", style = MaterialTheme.typography.bodyLarge)
                        Switch(
                            checked = state.includeBackPages,
                            onCheckedChange = viewModel::setIncludeBackPages
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Institution-Authorized (No SAMPLE Watermark)", style = MaterialTheme.typography.bodyLarge)
                        Switch(
                            checked = state.institutionAuthorized,
                            onCheckedChange = viewModel::setInstitutionAuthorized
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Students ($selectedCount / ${state.students.size} selected)",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Row {
                            TextButton(onClick = { viewModel.selectAll(true) }) { Text("Select All") }
                            TextButton(onClick = { viewModel.selectAll(false) }) { Text("Clear") }
                        }
                    }
                }

                if (state.students.isEmpty()) {
                    item {
                        EmptyStateCard(
                            icon = Icons.Filled.Groups,
                            title = "No student records yet",
                            message = "Add students manually, import a CSV roster, or load the built-in Hindi & English sample batch.",
                            actionLabel = "Load Sample Batch",
                            onAction = viewModel::loadSampleCsv
                        )
                    }
                } else {
                    items(state.students, key = { it.id }) { student ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = student.isSelected,
                                    onCheckedChange = { checked -> viewModel.toggleStudentSelected(student.id, checked) }
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = student.studentName,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "Class ${student.className}-${student.section} • Roll ${student.rollNumber} • Adm ${student.admissionNumber}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.generateSingleStudentPdf(student, onPdfExported) },
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
                                    Icon(Icons.Filled.PictureAsPdf, contentDescription = "Generate individual ID PDF")
                                }
                                IconButton(
                                    onClick = { viewModel.deleteStudent(student.id) },
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete student")
                                }
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = { viewModel.generateBatchPdf(onPdfExported) },
                            enabled = selectedCount > 0 && !state.isExporting,
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("bulk_generate_batch_pdf_button")
                        ) {
                            Icon(Icons.Filled.PictureAsPdf, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (state.isExporting) "Generating Batch PDF..."
                                else "Export Multi-Page Batch PDF ($selectedCount IDs)"
                            )
                        }
                    }
                }
                item {
                    CopyrightFooter()
                }
            }
        }
    }

    if (showAddStudentDialog) {
        var name by remember { mutableStateOf("") }
        var father by remember { mutableStateOf("") }
        var mother by remember { mutableStateOf("") }
        var cls by remember { mutableStateOf("") }
        var sec by remember { mutableStateOf("") }
        var roll by remember { mutableStateOf("") }
        var adm by remember { mutableStateOf("") }
        var dob by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var blood by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddStudentDialog = false },
            title = { Text("Add Student Record") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Student Name *") })
                    OutlinedTextField(value = father, onValueChange = { father = it }, label = { Text("Father's Name") })
                    OutlinedTextField(value = mother, onValueChange = { mother = it }, label = { Text("Mother's Name") })
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(value = cls, onValueChange = { cls = it }, label = { Text("Class") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = sec, onValueChange = { sec = it }, label = { Text("Sec") }, modifier = Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(value = roll, onValueChange = { roll = it }, label = { Text("Roll No") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = adm, onValueChange = { adm = it }, label = { Text("Adm No") }, modifier = Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(value = dob, onValueChange = { dob = it }, label = { Text("DOB") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = blood, onValueChange = { blood = it }, label = { Text("Blood") }, modifier = Modifier.weight(1f))
                    }
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Emergency Contact") })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.addManualStudent(
                        StudentRecordEntity(
                            studentName = name.trim(),
                            fatherName = father.trim(),
                            motherName = mother.trim(),
                            className = cls.trim(),
                            section = sec.trim(),
                            rollNumber = roll.trim(),
                            admissionNumber = adm.trim(),
                            dateOfBirth = dob.trim(),
                            emergencyContact = phone.trim(),
                            bloodGroup = blood.trim()
                        )
                    )
                    showAddStudentDialog = false
                }) { Text("Save Student") }
            },
            dismissButton = { TextButton(onClick = { showAddStudentDialog = false }) { Text("Cancel") } }
        )
    }
}
