package com.example

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.ads.AdsConsentState
import com.example.data.local.IdvantaDatabase
import com.example.data.local.StudentRecordEntity
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.DocumentRepository
import com.example.domain.model.BiodataData
import com.example.domain.model.CvDocumentData
import com.example.domain.model.DocumentCategory
import com.example.domain.model.DocumentDesignConfig
import com.example.domain.model.EducationEntry
import com.example.domain.model.FontStyleOption
import com.example.domain.model.IdCardData
import com.example.domain.model.IdCardType
import com.example.domain.model.WorkExperienceEntry
import com.example.pdf.BiodataPdfRenderer
import com.example.pdf.CvPdfRenderer
import com.example.pdf.IdCardPdfRenderer
import com.example.pdf.IdPdfOutputFormat
import com.example.pdf.PdfFontHelper
import com.example.pdf.PdfGeneratorEngine
import com.example.pdf.QrCodeGenerator
import com.example.ui.components.CopyrightFooter
import com.example.ui.screens.AboutIdvantaScreen
import com.example.ui.screens.ContactUsScreen
import com.example.ui.screens.CookiesScreen
import com.example.ui.screens.DisclaimerScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PrivacyPolicyScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.TermsAndConditionsScreen
import com.example.ui.theme.IdvantaTheme
import com.example.ui.viewmodel.AppSettingsViewModel
import com.example.ui.viewmodel.HomeViewModel
import com.example.util.AppLanguage
import com.example.util.CopyrightUtils
import java.io.OutputStream
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements

@Implements(PdfDocument::class)
class TestShadowPdfDocument {
    private val finishedPages = mutableListOf<PdfDocument.PageInfo>()
    private var currentPage: PdfDocument.Page? = null
    private var closed = false

    @Implementation
    fun __constructor__() {
        closed = false
    }

    @Implementation
    fun startPage(pageInfo: PdfDocument.PageInfo): PdfDocument.Page {
        check(!closed) { "document is closed!" }
        val bmp = Bitmap.createBitmap(pageInfo.pageWidth, pageInfo.pageHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val ctor = PdfDocument.Page::class.java.getDeclaredConstructor(
            Canvas::class.java,
            PdfDocument.PageInfo::class.java
        )
        ctor.isAccessible = true
        return ctor.newInstance(canvas, pageInfo).also { currentPage = it }
    }

    @Implementation
    fun finishPage(page: PdfDocument.Page) {
        finishedPages.add(page.info)
        currentPage = null
    }

    @Implementation
    fun getPages(): List<PdfDocument.PageInfo> = finishedPages.toList()

    @Implementation
    fun writeTo(out: OutputStream) {
        val header = "%PDF-1.4\n%IDVANTA-TEST-${finishedPages.size}-PAGES\n".toByteArray()
        out.write(header)
        out.write(ByteArray(512) { 0x20 })
        out.write("\n%%EOF\n".toByteArray())
    }

    @Implementation
    fun close() {
        closed = true
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], shadows = [TestShadowPdfDocument::class])
class IdvantaEngineRobolectricTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var context: Context
    private lateinit var db: IdvantaDatabase
    private lateinit var repository: DocumentRepository
    private lateinit var prefsRepository: UserPreferencesRepository
    private lateinit var pdfEngine: PdfGeneratorEngine

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, IdvantaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = DocumentRepository(db.idvantaDao(), context)
        prefsRepository = UserPreferencesRepository(context)
        val fontHelper = PdfFontHelper(context)
        val qrGenerator = QrCodeGenerator()
        pdfEngine = PdfGeneratorEngine(
            context = context,
            cvRenderer = CvPdfRenderer(fontHelper),
            idCardRenderer = IdCardPdfRenderer(fontHelper, qrGenerator),
            biodataRenderer = BiodataPdfRenderer(fontHelper)
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `qr code generator builds bitmap for bilingual payload`() {
        val qr = QrCodeGenerator().generateQrBitmap("IDVANTA|आरव शर्मा|ADM-2026-001", 180)
        assertEquals(180, qr.width)
        assertEquals(180, qr.height)
    }

    @Test
    fun `pdf engine generates valid pdf bytes for cv with hindi and english`() = runBlocking {
        val cv = CvDocumentData(
            fullName = "Priya Sharma / प्रिया शर्मा",
            professionalHeadline = "Senior Mathematics Teacher",
            phone = "+91 98765 43210",
            email = "priya@unityschool.edu",
            careerObjective = "Dedicated educator with 8+ years of bilingual Hindi & English instruction.",
            education = listOf(EducationEntry("1", "Delhi University", "M.Sc. Mathematics", "DU", "2018", "84%")),
            experience = listOf(WorkExperienceEntry("1", "PGT Mathematics", "Unity Public School", "2019–2026", "Curriculum design & board mentoring.")),
            skills = listOf("Mathematics", "शिक्षण कौशल", "Curriculum Planning"),
            languages = listOf("English", "हिन्दी")
        )
        val design = DocumentDesignConfig(
            templateId = "cv_prestige_hindi",
            fontStyle = FontStyleOption.DEVANAGARI
        )
        val result = pdfEngine.generateCvPdf(1L, "Priya_CV", DocumentCategory.CV, cv, design)
        assertTrue(result.file.exists())
        assertTrue(result.fileSizeBytes > 500L)
        assertTrue(result.pageCount >= 1)
        val header = result.file.inputStream().use { stream ->
            ByteArray(5).also { stream.read(it) }.decodeToString()
        }
        assertEquals("%PDF-", header)
    }

    @Test
    fun `pdf engine generates valid cr80 and bulk student id pdfs`() = runBlocking {
        val cardData = IdCardData(
            cardType = IdCardType.SCHOOL,
            institutionName = "Unity Public School",
            personName = "Rohan Verma",
            className = "10",
            section = "A",
            rollNumber = "12",
            admissionNumber = "ADM-101",
            emergencyContact = "+91 98111 00000"
        )
        val cr80 = pdfEngine.generateIdCardPdf(
            documentId = 2L,
            title = "Rohan_ID",
            data = cardData,
            design = DocumentDesignConfig(templateId = "id_classic_navy_crest", institutionAuthorized = false),
            outputFormat = IdPdfOutputFormat.CR80_FRONT_BACK
        )
        assertEquals(2, cr80.pageCount)
        assertTrue(cr80.file.exists())

        val students = (1..10).map { idx ->
            StudentRecordEntity(
                schoolName = "Unity Public School",
                studentName = "Student $idx",
                className = "9",
                section = "B",
                rollNumber = idx.toString(),
                admissionNumber = "ADM-90$idx"
            )
        }
        val bulk = pdfEngine.generateBulkStudentIdPdf(
            batchTitle = "Class_9B",
            students = students,
            baseData = cardData,
            design = DocumentDesignConfig(templateId = "id_classic_navy_crest", institutionAuthorized = true),
            includeBackPages = true
        )
        // 10 students at 8 cards per page = 2 front pages + 2 back pages = 4 pages
        assertEquals(4, bulk.pageCount)
        assertTrue(bulk.file.exists())
    }

    @Test
    fun `pdf engine generates valid biodata pdf`() = runBlocking {
        val bio = BiodataData(
            name = "अमित कुमार",
            dateOfBirth = "12/04/1996",
            phone = "+91 99999 88888",
            introduction = "प्रौद्योगिकी और शिक्षा में सक्रिय पेशेवर।",
            educationSummary = "B.Tech Computer Science",
            fatherName = "श्री रमेश कुमार",
            languages = listOf("हिन्दी", "English")
        )
        val result = pdfEngine.generateBiodataPdf(
            documentId = 3L,
            title = "Amit_Biodata",
            data = bio,
            design = DocumentDesignConfig(templateId = "bio_hindi_heritage", fontStyle = FontStyleOption.DEVANAGARI)
        )
        assertTrue(result.file.exists())
        assertEquals(1, result.pageCount)
    }

    @Test
    fun `repository detects duplicate students accurately`() = runBlocking {
        val s1 = StudentRecordEntity(
            studentName = "Kavya Singh",
            className = "8",
            section = "A",
            rollNumber = "15",
            admissionNumber = "ADM-500"
        )
        val first = repository.addStudentIfUnique(s1)
        assertTrue(first.isSuccess)

        val dup = repository.addStudentIfUnique(s1.copy(studentName = "Kavya S."))
        assertTrue(dup.isFailure)
    }

    @Test
    fun `debug build uses official google test ad configuration`() {
        assertTrue(BuildConfig.IS_TEST_AD_CONFIG)
        assertEquals("ca-app-pub-3940256099942544/9214589741", BuildConfig.ADMOB_BANNER_AD_UNIT_ID)
    }

    @Test
    fun `splash screen displays minimalist idvanta and made by unity without promo text`() {
        composeRule.setContent {
            IdvantaTheme {
                SplashScreen(onFinished = {})
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("IDVANTA").assertIsDisplayed()
        composeRule.onNodeWithText("Made by Unity").assertIsDisplayed()
        composeRule.onNodeWithText("Professional Document Creator").assertDoesNotExist()
        composeRule.onNodeWithText("Your Identity. Your Documents. Your Style.").assertDoesNotExist()
    }

    @Test
    fun `copyright footer displays required india seven ink unity and dynamic year copy`() {
        composeRule.setContent {
            IdvantaTheme {
                CopyrightFooter()
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("IDVANTA").assertIsDisplayed()
        composeRule.onNodeWithText("Made in India 🇮🇳").assertIsDisplayed()
        composeRule.onNodeWithText("Developed by").assertIsDisplayed()
        composeRule.onNodeWithText("Seven Ink Digital Developers Team").assertIsDisplayed()
        composeRule.onNodeWithText("Made by Unity").assertIsDisplayed()
        composeRule.onNodeWithText(CopyrightUtils.copyrightText()).assertIsDisplayed()
    }

    @Test
    fun `home screen hides removed promo elements and shows full module labels`() {
        val homeVm = HomeViewModel(repository)
        composeRule.setContent {
            IdvantaTheme {
                HomeScreen(
                    language = AppLanguage.ENGLISH,
                    onCreateCv = {},
                    onCreateIdCard = {},
                    onCreateBiodata = {},
                    onOpenBulkGenerator = {},
                    onOpenTemplateLibrary = {},
                    onOpenMyDocuments = {},
                    onOpenExistingDocument = {},
                    onUseTemplate = {},
                    onOpenSettings = {},
                    onOpenAbout = {},
                    viewModel = homeVm
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Document Studio").assertDoesNotExist()
        composeRule.onNodeWithText("100% Offline & Private").assertDoesNotExist()
        composeRule.onNodeWithText("Your Identity. Your Documents. Your Style.").assertDoesNotExist()

        composeRule.onNodeWithTag("home_screen").performScrollToNode(hasText("Create CV"))
        listOf(
            "Create CV",
            "Resume Maker",
            "School ID Card",
            "College ID Card",
            "Employee ID Card",
            "Teacher ID Card",
            "Personal Biodata",
            "Bulk ID Generator",
            "Template Library",
            "My Documents"
        ).forEach { label ->
            composeRule.onNodeWithText(label).performScrollTo().assertIsDisplayed()
        }
    }

    @Test
    fun `privacy policy screen renders admob disclosure and google privacy link`() {
        composeRule.setContent {
            IdvantaTheme {
                PrivacyPolicyScreen(onNavigateBack = {})
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Privacy Policy").assertIsDisplayed()
        composeRule.onNodeWithTag("privacy_policy_screen")
            .performScrollToNode(hasText("6. Third-Party Advertising & Google AdMob"))
        composeRule.onNodeWithText("6. Third-Party Advertising & Google AdMob").assertIsDisplayed()
        composeRule.onNodeWithTag("privacy_policy_screen")
            .performScrollToNode(hasTestTag("google_privacy_link_button"))
        composeRule.onNodeWithTag("google_privacy_link_button").assertIsDisplayed()
    }

    @Test
    fun `contact us screen renders developer team email and contact support button`() {
        composeRule.setContent {
            IdvantaTheme {
                ContactUsScreen(onNavigateBack = {})
            }
        }
        composeRule.waitForIdle()
        composeRule.onAllNodesWithText("Seven Ink Digital Developers Team").onFirst().assertIsDisplayed()
        composeRule.onNodeWithText("Email: seveninkdigitaldevelopers@gmail.com").assertIsDisplayed()
        composeRule.onNodeWithTag("contact_support_button").assertIsDisplayed()
    }

    @Test
    fun `about screen renders brand details audience and footer`() {
        composeRule.setContent {
            IdvantaTheme {
                AboutIdvantaScreen(onNavigateBack = {})
            }
        }
        composeRule.waitForIdle()
        composeRule.onAllNodesWithText("Your Identity. Your Documents. Your Style.").onFirst().assertIsDisplayed()
        composeRule.onNodeWithTag("about_screen")
            .performScrollToNode(hasText("For School Teachers, Students and Working Professionals."))
        composeRule.onNodeWithText("For School Teachers, Students and Working Professionals.")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onAllNodesWithText("Seven Ink Digital Developers Team")
            .onFirst()
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun `settings screen shows legal section and conditional privacy options`() {
        val settingsVm = AppSettingsViewModel(prefsRepository, repository)
        composeRule.setContent {
            IdvantaTheme {
                SettingsScreen(
                    settingsViewModel = settingsVm,
                    consentState = AdsConsentState(canRequestAds = true, isPrivacyOptionsRequired = true),
                    onNavigateBack = {},
                    onOpenPrivacyPolicy = {},
                    onOpenDisclaimer = {},
                    onOpenTerms = {},
                    onOpenCookies = {},
                    onOpenAbout = {},
                    onOpenContactUs = {},
                    onOpenPrivacyOptions = {}
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings_screen")
            .performScrollToNode(hasTestTag("settings_legal_section_header"))
        listOf(
            "settings_legal_section_header",
            "settings_privacy_row",
            "settings_disclaimer_row",
            "settings_terms_row",
            "settings_cookies_row",
            "settings_about_row",
            "settings_contact_row",
            "settings_privacy_options_row"
        ).forEach { tag ->
            composeRule.onNodeWithTag(tag).performScrollTo().assertIsDisplayed()
        }
    }

    @Test
    fun `disclaimer screen displays authorized id issuance notice`() {
        composeRule.setContent {
            IdvantaTheme {
                DisclaimerScreen(onNavigateBack = {})
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Disclaimer").assertIsDisplayed()
        composeRule.onNodeWithTag("disclaimer_screen")
            .performScrollToNode(hasText("Authorized Issuance of Official ID Cards"))
        composeRule.onNodeWithText("Authorized Issuance of Official ID Cards").assertIsDisplayed()
    }

    @Test
    fun `terms screen displays all standard sections through contact info`() {
        composeRule.setContent {
            IdvantaTheme {
                TermsAndConditionsScreen(onNavigateBack = {})
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Terms & Conditions").assertIsDisplayed()
        composeRule.onNodeWithTag("terms_screen")
            .performScrollToNode(hasText("11. Contact Information"))
        composeRule.onNodeWithText("11. Contact Information").assertIsDisplayed()
    }

    @Test
    fun `cookies screen displays native app disclosure`() {
        composeRule.setContent {
            IdvantaTheme {
                CookiesScreen(onNavigateBack = {})
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Cookies & Identifiers").assertIsDisplayed()
        composeRule.onNodeWithText("Native App Cookie Disclosure").assertIsDisplayed()
    }
}
