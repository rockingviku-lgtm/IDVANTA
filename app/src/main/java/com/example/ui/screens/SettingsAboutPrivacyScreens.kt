package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.BuildConfig
import com.example.ads.AdsConsentState
import com.example.data.preferences.DefaultExportFormat
import com.example.ui.components.CopyrightFooter
import com.example.ui.theme.ThemeMode
import com.example.ui.viewmodel.AppSettingsViewModel
import com.example.util.AppLanguage

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    settingsViewModel: AppSettingsViewModel,
    consentState: AdsConsentState = AdsConsentState(),
    onNavigateBack: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onOpenDisclaimer: () -> Unit,
    onOpenTerms: () -> Unit,
    onOpenCookies: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenContactUs: () -> Unit,
    onOpenPrivacyOptions: () -> Unit = {}
) {
    val prefs by settingsViewModel.preferences.collectAsStateWithLifecycle()
    val stats by settingsViewModel.storageStats.collectAsStateWithLifecycle()
    var showClearDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        settingsViewModel.refreshStorageStats()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 840.dp)
                    .fillMaxSize()
                    .testTag("settings_screen"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text("Appearance Theme", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            ThemeMode.SYSTEM to "System Default",
                            ThemeMode.LIGHT to "Light Mode",
                            ThemeMode.DARK to "Dark Mode"
                        ).forEach { (mode, label) ->
                            FilterChip(
                                selected = prefs.themeMode == mode,
                                onClick = { settingsViewModel.setThemeMode(mode) },
                                label = { Text(label) },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("theme_${mode.name.lowercase()}")
                            )
                        }
                    }
                }

                item {
                    Text("Language / भाषा", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AppLanguage.entries.forEach { lang ->
                            FilterChip(
                                selected = prefs.language == lang,
                                onClick = { settingsViewModel.setLanguage(lang) },
                                label = { Text(lang.label) },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("lang_${lang.code}")
                            )
                        }
                    }
                }

                item {
                    Text("Default Export Settings", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DefaultExportFormat.entries.forEach { fmt ->
                            FilterChip(
                                selected = prefs.defaultExportFormat == fmt,
                                onClick = { settingsViewModel.setDefaultExportFormat(fmt) },
                                label = { Text(fmt.name) },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = prefs.defaultInstitutionName,
                        onValueChange = settingsViewModel::setDefaultInstitutionName,
                        label = { Text("Default School / Organization Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Institution-Authorized IDs by Default",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = prefs.institutionAuthorizedDefault,
                            onCheckedChange = settingsViewModel::setInstitutionAuthorizedDefault
                        )
                    }
                }

                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Local Storage Information", style = MaterialTheme.typography.titleMedium)
                            val kb = (stats.second / 1024L).coerceAtLeast(0L)
                            Text(
                                text = "Exported PDF files: ${stats.first}  •  Local media & exports: $kb KB",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedButton(
                                onClick = { showClearDialog = true },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Text("Clear Exported PDF Cache")
                            }
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Legal & Information",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.testTag("settings_legal_section_header")
                        )

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                LegalMenuItem(
                                    icon = Icons.Filled.Policy,
                                    title = "Privacy Policy",
                                    subtitle = "Local document storage & AdMob/UMP disclosure",
                                    testTag = "settings_privacy_row",
                                    onClick = onOpenPrivacyPolicy
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                                LegalMenuItem(
                                    icon = Icons.Filled.Gavel,
                                    title = "Disclaimer",
                                    subtitle = "Document accuracy & authorized ID usage",
                                    testTag = "settings_disclaimer_row",
                                    onClick = onOpenDisclaimer
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                                LegalMenuItem(
                                    icon = Icons.Filled.Description,
                                    title = "Terms and Conditions",
                                    subtitle = "Permitted use, ownership & liability terms",
                                    testTag = "settings_terms_row",
                                    onClick = onOpenTerms
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                                LegalMenuItem(
                                    icon = Icons.Filled.Cookie,
                                    title = "Cookies",
                                    subtitle = "Native app disclosure & advertising identifiers",
                                    testTag = "settings_cookies_row",
                                    onClick = onOpenCookies
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                                LegalMenuItem(
                                    icon = Icons.Filled.Info,
                                    title = "About Us",
                                    subtitle = "Version ${BuildConfig.VERSION_NAME} • Seven Ink Digital Developers Team",
                                    testTag = "settings_about_row",
                                    onClick = onOpenAbout
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                                LegalMenuItem(
                                    icon = Icons.Filled.Email,
                                    title = "Contact Us",
                                    subtitle = "seveninkdigitaldevelopers@gmail.com",
                                    testTag = "settings_contact_row",
                                    onClick = onOpenContactUs
                                )

                                if (consentState.isPrivacyOptionsRequired) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                                    LegalMenuItem(
                                        icon = Icons.Filled.PrivacyTip,
                                        title = "Privacy Options",
                                        subtitle = "Review or update your ad consent preferences",
                                        testTag = "settings_privacy_options_row",
                                        onClick = onOpenPrivacyOptions
                                    )
                                }
                            }
                        }

                        if (BuildConfig.IS_TEST_AD_CONFIG) {
                            Text(
                                text = "Test Build: Google AdMob test ad unit is active for verification.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 4.dp)
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

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear Exported PDFs?") },
            text = { Text("Your saved CV, ID Card, and Biodata drafts in the database will remain safe.") },
            confirmButton = {
                TextButton(onClick = {
                    settingsViewModel.clearExportCache()
                    showClearDialog = false
                }) { Text("Clear") }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun LegalMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
