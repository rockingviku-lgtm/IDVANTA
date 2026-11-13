package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.ads.AdsConsentState
import com.example.ads.ConsentAndAdsManager
import com.example.domain.model.DocumentCategory
import com.example.domain.model.IdCardType
import com.example.ui.components.AnchoredAdaptiveBannerAd
import com.example.ui.navigation.AboutRoute
import com.example.ui.navigation.BiodataEditorRoute
import com.example.ui.navigation.BulkIdGeneratorRoute
import com.example.ui.navigation.ContactUsRoute
import com.example.ui.navigation.CookiesRoute
import com.example.ui.navigation.CvEditorRoute
import com.example.ui.navigation.DisclaimerRoute
import com.example.ui.navigation.HomeRoute
import com.example.ui.navigation.IdCardEditorRoute
import com.example.ui.navigation.MyDocumentsRoute
import com.example.ui.navigation.PdfPreviewRoute
import com.example.ui.navigation.PrivacyPolicyRoute
import com.example.ui.navigation.SettingsRoute
import com.example.ui.navigation.SplashRoute
import com.example.ui.navigation.TemplateLibraryRoute
import com.example.ui.navigation.TermsRoute
import com.example.ui.screens.AboutIdvantaScreen
import com.example.ui.screens.BiodataMakerScreen
import com.example.ui.screens.BulkIdGeneratorScreen
import com.example.ui.screens.ContactUsScreen
import com.example.ui.screens.CookiesScreen
import com.example.ui.screens.CvMakerScreen
import com.example.ui.screens.DisclaimerScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.IdCardMakerScreen
import com.example.ui.screens.MyDocumentsScreen
import com.example.ui.screens.PdfPreviewScreen
import com.example.ui.screens.PrivacyPolicyScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.TemplateLibraryScreen
import com.example.ui.screens.TermsAndConditionsScreen
import com.example.ui.theme.IdvantaTheme
import com.example.ui.viewmodel.AppSettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var consentAndAdsManager: ConsentAndAdsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        consentAndAdsManager.requestConsentOnLaunch(this)
        setContent {
            val settingsViewModel: AppSettingsViewModel = hiltViewModel()
            val prefs by settingsViewModel.preferences.collectAsStateWithLifecycle()
            val consentState by consentAndAdsManager.state.collectAsStateWithLifecycle()

            IdvantaTheme(themeMode = prefs.themeMode) {
                IdvantaAppRoot(
                    settingsViewModel = settingsViewModel,
                    consentState = consentState,
                    onOpenPrivacyOptions = {
                        consentAndAdsManager.showPrivacyOptionsForm(this@MainActivity)
                    }
                )
            }
        }
    }
}

@Composable
private fun IdvantaAppRoot(
    settingsViewModel: AppSettingsViewModel,
    consentState: AdsConsentState,
    onOpenPrivacyOptions: () -> Unit
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDest = backStackEntry?.destination
    val prefs by settingsViewModel.preferences.collectAsStateWithLifecycle()

    val showChrome = currentDest != null && (
        currentDest.hasRoute<HomeRoute>() ||
            currentDest.hasRoute<TemplateLibraryRoute>() ||
            currentDest.hasRoute<MyDocumentsRoute>() ||
            currentDest.hasRoute<BulkIdGeneratorRoute>() ||
            currentDest.hasRoute<SettingsRoute>()
        )

    fun navigateTopLevel(route: Any) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 640.dp

        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (showChrome && !useRail) {
                    Column {
                        AnchoredAdaptiveBannerAd(consentState = consentState)
                        NavigationBar {
                            NavigationBarItem(
                                selected = currentDest?.hasRoute<HomeRoute>() == true,
                                onClick = { navigateTopLevel(HomeRoute) },
                                icon = {
                                    Icon(
                                        if (currentDest?.hasRoute<HomeRoute>() == true) Icons.Filled.Home else Icons.Outlined.Home,
                                        contentDescription = "Home"
                                    )
                                },
                                label = { Text("Home") }
                            )
                            NavigationBarItem(
                                selected = currentDest?.hasRoute<TemplateLibraryRoute>() == true,
                                onClick = { navigateTopLevel(TemplateLibraryRoute("ALL")) },
                                icon = {
                                    Icon(
                                        if (currentDest?.hasRoute<TemplateLibraryRoute>() == true) Icons.Filled.Style else Icons.Outlined.Style,
                                        contentDescription = "Templates"
                                    )
                                },
                                label = { Text("Templates") }
                            )
                            NavigationBarItem(
                                selected = currentDest?.hasRoute<BulkIdGeneratorRoute>() == true,
                                onClick = { navigateTopLevel(BulkIdGeneratorRoute) },
                                icon = {
                                    Icon(
                                        if (currentDest?.hasRoute<BulkIdGeneratorRoute>() == true) Icons.Filled.Groups else Icons.Outlined.Groups,
                                        contentDescription = "Bulk ID"
                                    )
                                },
                                label = { Text("Bulk ID") }
                            )
                            NavigationBarItem(
                                selected = currentDest?.hasRoute<MyDocumentsRoute>() == true,
                                onClick = { navigateTopLevel(MyDocumentsRoute("ALL")) },
                                icon = {
                                    Icon(
                                        if (currentDest?.hasRoute<MyDocumentsRoute>() == true) Icons.Filled.FolderOpen else Icons.Outlined.FolderOpen,
                                        contentDescription = "Documents"
                                    )
                                },
                                label = { Text("Docs") }
                            )
                            NavigationBarItem(
                                selected = currentDest?.hasRoute<SettingsRoute>() == true,
                                onClick = { navigateTopLevel(SettingsRoute) },
                                icon = {
                                    Icon(
                                        if (currentDest?.hasRoute<SettingsRoute>() == true) Icons.Filled.Settings else Icons.Outlined.Settings,
                                        contentDescription = "Settings"
                                    )
                                },
                                label = { Text("Settings") }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (showChrome && useRail) {
                    NavigationRail {
                        NavigationRailItem(
                            selected = currentDest?.hasRoute<HomeRoute>() == true,
                            onClick = { navigateTopLevel(HomeRoute) },
                            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                            label = { Text("Home") }
                        )
                        NavigationRailItem(
                            selected = currentDest?.hasRoute<TemplateLibraryRoute>() == true,
                            onClick = { navigateTopLevel(TemplateLibraryRoute("ALL")) },
                            icon = { Icon(Icons.Filled.Style, contentDescription = "Templates") },
                            label = { Text("Templates") }
                        )
                        NavigationRailItem(
                            selected = currentDest?.hasRoute<BulkIdGeneratorRoute>() == true,
                            onClick = { navigateTopLevel(BulkIdGeneratorRoute) },
                            icon = { Icon(Icons.Filled.Groups, contentDescription = "Bulk ID") },
                            label = { Text("Bulk ID") }
                        )
                        NavigationRailItem(
                            selected = currentDest?.hasRoute<MyDocumentsRoute>() == true,
                            onClick = { navigateTopLevel(MyDocumentsRoute("ALL")) },
                            icon = { Icon(Icons.Filled.FolderOpen, contentDescription = "Documents") },
                            label = { Text("Docs") }
                        )
                        NavigationRailItem(
                            selected = currentDest?.hasRoute<SettingsRoute>() == true,
                            onClick = { navigateTopLevel(SettingsRoute) },
                            icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                            label = { Text("Settings") }
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    NavHost(
                        navController = navController,
                        startDestination = SplashRoute,
                        modifier = Modifier.weight(1f)
                    ) {
                        composable<SplashRoute> {
                            SplashScreen(
                                onFinished = {
                                    navController.navigate(HomeRoute) {
                                        popUpTo<SplashRoute> { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable<HomeRoute> {
                            HomeScreen(
                                language = prefs.language,
                                onCreateCv = { cat ->
                                    navController.navigate(CvEditorRoute(initialCategory = cat.name))
                                },
                                onCreateIdCard = { type ->
                                    navController.navigate(IdCardEditorRoute(cardType = type.name))
                                },
                                onCreateBiodata = {
                                    navController.navigate(BiodataEditorRoute())
                                },
                                onOpenBulkGenerator = {
                                    navigateTopLevel(BulkIdGeneratorRoute)
                                },
                                onOpenTemplateLibrary = { filter ->
                                    navController.navigate(TemplateLibraryRoute(filter))
                                },
                                onOpenMyDocuments = { tab ->
                                    navController.navigate(MyDocumentsRoute(tab))
                                },
                                onOpenExistingDocument = { doc ->
                                    val cat = runCatching { DocumentCategory.valueOf(doc.category) }
                                        .getOrDefault(DocumentCategory.CV)
                                    when {
                                        cat.isCvOrResume -> navController.navigate(
                                            CvEditorRoute(documentId = doc.id, initialCategory = cat.name)
                                        )
                                        cat.isIdCard -> navController.navigate(
                                            IdCardEditorRoute(documentId = doc.id, cardType = doc.subCategory.ifBlank { "SCHOOL" })
                                        )
                                        else -> navController.navigate(BiodataEditorRoute(documentId = doc.id))
                                    }
                                },
                                onUseTemplate = { tpl ->
                                    when {
                                        tpl.targetCategory.isCvOrResume -> navController.navigate(
                                            CvEditorRoute(initialCategory = tpl.targetCategory.name, templateId = tpl.id)
                                        )
                                        tpl.targetCategory.isIdCard -> {
                                            val type = when (tpl.targetCategory) {
                                                DocumentCategory.COLLEGE_ID -> IdCardType.COLLEGE
                                                DocumentCategory.EMPLOYEE_ID -> IdCardType.EMPLOYEE
                                                DocumentCategory.TEACHER_ID -> IdCardType.TEACHER
                                                else -> IdCardType.SCHOOL
                                            }
                                            navController.navigate(
                                                IdCardEditorRoute(cardType = type.name, templateId = tpl.id)
                                            )
                                        }
                                        else -> navController.navigate(BiodataEditorRoute(templateId = tpl.id))
                                    }
                                },
                                onOpenSettings = { navigateTopLevel(SettingsRoute) },
                                onOpenAbout = { navController.navigate(AboutRoute) }
                            )
                        }

                        composable<CvEditorRoute> { entry ->
                            val route = entry.toRoute<CvEditorRoute>()
                            CvMakerScreen(
                                documentId = route.documentId,
                                initialCategory = route.initialCategory,
                                initialTemplateId = route.templateId,
                                onNavigateBack = { navController.popBackStack() },
                                onPdfExported = { exportId, path, title ->
                                    navController.navigate(PdfPreviewRoute(exportId, path, title))
                                }
                            )
                        }

                        composable<IdCardEditorRoute> { entry ->
                            val route = entry.toRoute<IdCardEditorRoute>()
                            IdCardMakerScreen(
                                documentId = route.documentId,
                                cardType = route.cardType,
                                initialTemplateId = route.templateId,
                                onNavigateBack = { navController.popBackStack() },
                                onPdfExported = { exportId, path, title ->
                                    navController.navigate(PdfPreviewRoute(exportId, path, title))
                                }
                            )
                        }

                        composable<BiodataEditorRoute> { entry ->
                            val route = entry.toRoute<BiodataEditorRoute>()
                            BiodataMakerScreen(
                                documentId = route.documentId,
                                initialTemplateId = route.templateId,
                                onNavigateBack = { navController.popBackStack() },
                                onPdfExported = { exportId, path, title ->
                                    navController.navigate(PdfPreviewRoute(exportId, path, title))
                                }
                            )
                        }

                        composable<BulkIdGeneratorRoute> {
                            BulkIdGeneratorScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onPdfExported = { exportId, path, title ->
                                    navController.navigate(PdfPreviewRoute(exportId, path, title))
                                }
                            )
                        }

                        composable<TemplateLibraryRoute> { entry ->
                            val route = entry.toRoute<TemplateLibraryRoute>()
                            TemplateLibraryScreen(
                                initialFilter = route.initialFilter,
                                onNavigateBack = { navController.popBackStack() },
                                onSelectTemplate = { tpl ->
                                    when {
                                        tpl.targetCategory.isCvOrResume -> navController.navigate(
                                            CvEditorRoute(initialCategory = tpl.targetCategory.name, templateId = tpl.id)
                                        )
                                        tpl.targetCategory.isIdCard -> {
                                            val type = when (tpl.targetCategory) {
                                                DocumentCategory.COLLEGE_ID -> IdCardType.COLLEGE
                                                DocumentCategory.EMPLOYEE_ID -> IdCardType.EMPLOYEE
                                                DocumentCategory.TEACHER_ID -> IdCardType.TEACHER
                                                else -> IdCardType.SCHOOL
                                            }
                                            navController.navigate(
                                                IdCardEditorRoute(cardType = type.name, templateId = tpl.id)
                                            )
                                        }
                                        else -> navController.navigate(BiodataEditorRoute(templateId = tpl.id))
                                    }
                                }
                            )
                        }

                        composable<MyDocumentsRoute> { entry ->
                            val route = entry.toRoute<MyDocumentsRoute>()
                            MyDocumentsScreen(
                                initialTab = route.initialTab,
                                onNavigateBack = { navController.popBackStack() },
                                onOpenDocument = { doc ->
                                    val cat = runCatching { DocumentCategory.valueOf(doc.category) }
                                        .getOrDefault(DocumentCategory.CV)
                                    when {
                                        cat.isCvOrResume -> navController.navigate(
                                            CvEditorRoute(documentId = doc.id, initialCategory = cat.name)
                                        )
                                        cat.isIdCard -> navController.navigate(
                                            IdCardEditorRoute(documentId = doc.id, cardType = doc.subCategory.ifBlank { "SCHOOL" })
                                        )
                                        else -> navController.navigate(BiodataEditorRoute(documentId = doc.id))
                                    }
                                },
                                onOpenPdfPreview = { exportId, path, title ->
                                    navController.navigate(PdfPreviewRoute(exportId, path, title))
                                }
                            )
                        }

                        composable<PdfPreviewRoute> { entry ->
                            val route = entry.toRoute<PdfPreviewRoute>()
                            PdfPreviewScreen(
                                exportId = route.exportId,
                                initialFilePath = route.filePath,
                                initialTitle = route.title,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable<SettingsRoute> {
                            SettingsScreen(
                                settingsViewModel = settingsViewModel,
                                consentState = consentState,
                                onNavigateBack = { navController.popBackStack() },
                                onOpenPrivacyPolicy = { navController.navigate(PrivacyPolicyRoute) },
                                onOpenDisclaimer = { navController.navigate(DisclaimerRoute) },
                                onOpenTerms = { navController.navigate(TermsRoute) },
                                onOpenCookies = { navController.navigate(CookiesRoute) },
                                onOpenAbout = { navController.navigate(AboutRoute) },
                                onOpenContactUs = { navController.navigate(ContactUsRoute) },
                                onOpenPrivacyOptions = onOpenPrivacyOptions
                            )
                        }

                        composable<PrivacyPolicyRoute> {
                            PrivacyPolicyScreen(onNavigateBack = { navController.popBackStack() })
                        }

                        composable<DisclaimerRoute> {
                            DisclaimerScreen(onNavigateBack = { navController.popBackStack() })
                        }

                        composable<TermsRoute> {
                            TermsAndConditionsScreen(onNavigateBack = { navController.popBackStack() })
                        }

                        composable<CookiesRoute> {
                            CookiesScreen(onNavigateBack = { navController.popBackStack() })
                        }

                        composable<AboutRoute> {
                            AboutIdvantaScreen(onNavigateBack = { navController.popBackStack() })
                        }

                        composable<ContactUsRoute> {
                            ContactUsScreen(onNavigateBack = { navController.popBackStack() })
                        }
                    }

                    if (showChrome && useRail) {
                        AnchoredAdaptiveBannerAd(
                            consentState = consentState,
                            modifier = Modifier.navigationBarsPadding()
                        )
                    }
                }
            }
        }
    }
}
