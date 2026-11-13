package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.BuildConfig
import com.example.ui.components.CopyrightFooter
import com.example.ui.components.DeveloperUnityCredit
import com.example.ui.components.IdvantaBrandMark
import com.example.util.CopyrightUtils
import kotlinx.coroutines.launch

private data class LegalSection(
    val title: String,
    val body: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LegalDocumentScaffold(
    topTitle: String,
    testTag: String,
    sections: List<LegalSection>,
    onNavigateBack: () -> Unit,
    extraBottomContent: (@Composable () -> Unit)? = null
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(topTitle) },
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
                    .widthIn(max = 780.dp)
                    .fillMaxSize()
                    .testTag(testTag),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sections) { sec ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = sec.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = sec.body,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                if (extraBottomContent != null) {
                    item { extraBottomContent() }
                }
                item { CopyrightFooter() }
            }
        }
    }
}

@Composable
fun PrivacyPolicyScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val sections = remember {
        listOf(
            LegalSection(
                "1. Personal Information You Enter",
                "IDVANTA lets you create CVs, resumes, school/college/employee/teacher ID cards, personal biodata, and bulk student ID batches. Information you type—such as names, contact numbers, email addresses, addresses, academic qualifications, employment history, family details, and student records—is entered directly by you."
            ),
            LegalSection(
                "2. Profile Photographs & Institution Logos",
                "When you attach a profile photo or institution logo using the Android Photo Picker, IDVANTA copies the selected image into the app's private internal storage on your device so it remains available for live previews and PDF rendering. IDVANTA does not upload your photographs or logos to our servers."
            ),
            LegalSection(
                "3. Local Database & Device Storage",
                "All document drafts, CV sections, student batch records, favorite templates, and exported PDF history are stored locally on your device using Android Room Database and DataStore Preferences. Document authoring and PDF rendering work offline on your device."
            ),
            LegalSection(
                "4. Document Export & Sharing",
                "Generated PDFs are saved inside local app storage until you choose to save a copy via Android's Storage Access Framework, share via another app using FileProvider, or print via Android PrintManager. Once you share or save a PDF externally, that copy is governed by the destination app or storage provider."
            ),
            LegalSection(
                "5. User-Controlled Data Deletion",
                "You can delete individual documents, student records, or exported PDFs inside My Documents and Bulk ID Generator, clear exported PDF caches in Settings, or uninstall IDVANTA to remove local app data."
            ),
            LegalSection(
                "6. Third-Party Advertising & Google AdMob",
                "To support the application, IDVANTA integrates the Google Mobile Ads SDK (play-services-ads 23.6.0) and Google User Messaging Platform SDK (user-messaging-platform 3.1.0). Unlike your locally stored documents, advertising requires internet connectivity and involves third-party data processing by Google and its ad partners."
            ),
            LegalSection(
                "7. Device Identifiers & Ad Data Processing",
                "When ads are requested after consent eligibility is verified, Google AdMob may process device identifiers (such as the Android Advertising ID), IP address, general device/app metadata, and ad interaction signals to deliver, measure, and prevent fraud in advertisements."
            ),
            LegalSection(
                "8. Consent & Privacy Choices",
                "At startup, IDVANTA requests updated consent status through Google UMP and displays consent forms where legally required. Where privacy options are required, you can review or update your advertising consent choices anytime in Settings → Legal & Information → Privacy Options."
            ),
            LegalSection(
                "9. Third-Party Privacy Policy & Contact",
                "Google's data practices for advertising services are governed by Google's Privacy Policy: https://policies.google.com/privacy\n\nFor privacy questions about IDVANTA, contact Seven Ink Digital Developers Team at seveninkdigitaldevelopers@gmail.com."
            )
        )
    }

    LegalDocumentScaffold(
        topTitle = "Privacy Policy",
        testTag = "privacy_policy_screen",
        sections = sections,
        onNavigateBack = onNavigateBack,
        extraBottomContent = {
            OutlinedButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://policies.google.com/privacy"))
                    runCatching { context.startActivity(intent) }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .testTag("google_privacy_link_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open Google Privacy Policy")
            }
        }
    )
}

@Composable
fun DisclaimerScreen(onNavigateBack: () -> Unit) {
    val sections = remember {
        listOf(
            LegalSection(
                "Document Creation Utility",
                "IDVANTA is a document creation, formatting, and PDF layout utility designed to help teachers, students, and working professionals prepare CVs, resumes, biodata, and identity cards."
            ),
            LegalSection(
                "User Responsibility & Verification",
                "Users are solely responsible for reviewing and verifying the accuracy, completeness, spelling, and legality of all information entered into generated CVs, resumes, biodata, and ID cards prior to submission or printing."
            ),
            LegalSection(
                "Authorized Issuance of Official ID Cards",
                "Official school, college, teacher, and employee identity cards may only be issued and authorized by the respective institution or employer. Personal demonstration or practice cards created without institutional authorization must carry a visible SAMPLE watermark."
            ),
            LegalSection(
                "No Verification or Outcome Guarantee",
                "IDVANTA, Unity, and Seven Ink Digital Developers Team do not independently verify educational degrees, employment records, identity numbers, or institutional affiliation, and provide no guarantee of job placement, academic admission, or official institutional acceptance."
            )
        )
    }

    LegalDocumentScaffold(
        topTitle = "Disclaimer",
        testTag = "disclaimer_screen",
        sections = sections,
        onNavigateBack = onNavigateBack
    )
}

@Composable
fun TermsAndConditionsScreen(onNavigateBack: () -> Unit) {
    val sections = remember {
        listOf(
            LegalSection(
                "1. Acceptance of Terms",
                "By installing or using IDVANTA, you agree to comply with these Terms and Conditions. If you do not agree, please discontinue use and uninstall the application."
            ),
            LegalSection(
                "2. Permitted Application Usage",
                "You may use IDVANTA for lawful personal, academic, teaching, administrative, and authorized organizational document preparation."
            ),
            LegalSection(
                "3. User Responsibilities",
                "You are responsible for ensuring that all text, photographs, logos, and CSV student rosters you supply are accurate and that you hold permission to use them."
            ),
            LegalSection(
                "4. Document Ownership",
                "You retain ownership of the documents, personal records, photographs, and exported PDFs you create with IDVANTA."
            ),
            LegalSection(
                "5. Prohibited Misuse",
                "You must not use IDVANTA to forge, counterfeit, impersonate, or fabricate unauthorized official, government, school, college, or corporate identity credentials."
            ),
            LegalSection(
                "6. Intellectual Property",
                "The IDVANTA name, application design, original templates, source code, and developer emblems are the intellectual property of Seven Ink Digital Developers Team (Made by Unity). Unity is an independent developer credit and is not affiliated with Unity Technologies."
            ),
            LegalSection(
                "7. Third-Party Services & Advertising",
                "IDVANTA incorporates Google Mobile Ads SDK and Google User Messaging Platform for banner advertising and consent management. Use of those services is subject to Google's applicable terms and policies."
            ),
            LegalSection(
                "8. Disclaimer of Warranties",
                "IDVANTA is provided on an \"AS IS\" and \"AS AVAILABLE\" basis without warranties of any kind, express or implied, regarding uninterrupted availability or fitness for a specific regulatory requirement."
            ),
            LegalSection(
                "9. Limitation of Liability",
                "To the maximum extent permitted by applicable law, Seven Ink Digital Developers Team shall not be liable for indirect, incidental, or consequential damages arising from document content errors, device storage loss, or misuse of exported PDFs."
            ),
            LegalSection(
                "10. Updates & Modifications",
                "We may update features, templates, advertising integrations, or these Terms from time to time. Continued use of updated versions constitutes acceptance of revised Terms."
            ),
            LegalSection(
                "11. Contact Information",
                "Questions regarding these Terms may be sent to seveninkdigitaldevelopers@gmail.com."
            )
        )
    }

    LegalDocumentScaffold(
        topTitle = "Terms & Conditions",
        testTag = "terms_screen",
        sections = sections,
        onNavigateBack = onNavigateBack
    )
}

@Composable
fun CookiesScreen(onNavigateBack: () -> Unit) {
    val sections = remember {
        listOf(
            LegalSection(
                "Native App Cookie Disclosure",
                "IDVANTA is a native Android application and does not operate a web browser interface or set traditional first-party browser HTTP cookies for document editing."
            ),
            LegalSection(
                "Third-Party SDK Identifiers & Similar Technologies",
                "IDVANTA integrates Google Mobile Ads (AdMob) and Google User Messaging Platform (UMP). These third-party SDKs may use mobile advertising identifiers (such as Google Advertising ID), app-set identifiers, local SDK storage, or similar technologies to serve ads, cap frequency, prevent invalid traffic, and store your consent preferences."
            ),
            LegalSection(
                "Managing Advertising & Consent Preferences",
                "IDVANTA requests updated consent information through Google UMP on launch. When privacy options are applicable in your region, you can modify your ad consent choices inside Settings → Legal & Information → Privacy Options, or adjust Android system ad settings."
            )
        )
    }

    LegalDocumentScaffold(
        topTitle = "Cookies & Identifiers",
        testTag = "cookies_screen",
        sections = sections,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutIdvantaScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About Us") },
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
                    .widthIn(max = 760.dp)
                    .fillMaxSize()
                    .testTag("about_screen"),
                contentPadding = PaddingValues(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item { IdvantaBrandMark(sizeDp = 76) }
                item {
                    Text(
                        text = "IDVANTA",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
                item {
                    Text(
                        text = "Your Identity. Your Documents. Your Style.",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                item { DeveloperUnityCredit(iconSizeDp = 28) }
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AboutFieldRow("Application Name", "IDVANTA")
                            AboutFieldRow("Tagline", "Your Identity. Your Documents. Your Style.")
                            AboutFieldRow("Developer Credit", "Made by Unity")
                            AboutFieldRow("Developed by", "Seven Ink Digital Developers Team")
                            AboutFieldRow("Origin", "Made in India 🇮🇳")
                            AboutFieldRow("Target Audience", "For School Teachers, Students and Working Professionals.")
                            AboutFieldRow("Copyright", CopyrightUtils.copyrightText())
                            AboutFieldRow("App Version", BuildConfig.VERSION_NAME)
                            AboutFieldRow(
                                "Advertising Disclosure",
                                if (BuildConfig.IS_TEST_AD_CONFIG) {
                                    "Google AdMob & UMP SDK integrated (Google Test Ad Configuration active in debug build)."
                                } else {
                                    "Google AdMob & UMP SDK integrated for banner advertising."
                                }
                            )
                        }
                    }
                }
                item { CopyrightFooter() }
            }
        }
    }
}

@Composable
private fun AboutFieldRow(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactUsScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val supportEmail = "seveninkdigitaldevelopers@gmail.com"

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Contact Us") },
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
                    .widthIn(max = 760.dp)
                    .fillMaxSize()
                    .testTag("contact_us_screen"),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Seven Ink Digital Developers Team",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            DeveloperUnityCredit(iconSizeDp = 24)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Email: $supportEmail",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Button(
                                onClick = {
                                    val mailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:$supportEmail")
                                        putExtra(Intent.EXTRA_EMAIL, arrayOf(supportEmail))
                                        putExtra(Intent.EXTRA_SUBJECT, "IDVANTA Support Request")
                                    }
                                    val launched = runCatching {
                                        context.startActivity(mailIntent)
                                    }.isSuccess
                                    if (!launched) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("No email app found. Email: $supportEmail")
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .minimumInteractiveComponentSize()
                                    .testTag("contact_support_button")
                            ) {
                                Icon(Icons.Filled.Email, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Contact Support")
                            }
                        }
                    }
                }
                item { CopyrightFooter() }
            }
        }
    }
}
