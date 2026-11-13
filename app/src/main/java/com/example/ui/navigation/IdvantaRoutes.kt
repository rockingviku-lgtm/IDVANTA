package com.example.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object SplashRoute

@Serializable
object HomeRoute

@Serializable
data class CvEditorRoute(
    val documentId: Long = -1L,
    val initialCategory: String = "CV",
    val templateId: String = ""
)

@Serializable
data class IdCardEditorRoute(
    val documentId: Long = -1L,
    val cardType: String = "SCHOOL",
    val templateId: String = ""
)

@Serializable
data class BiodataEditorRoute(
    val documentId: Long = -1L,
    val templateId: String = ""
)

@Serializable
object BulkIdGeneratorRoute

@Serializable
data class TemplateLibraryRoute(
    val initialFilter: String = "ALL"
)

@Serializable
data class MyDocumentsRoute(
    val initialTab: String = "ALL"
)

@Serializable
data class PdfPreviewRoute(
    val exportId: Long = -1L,
    val filePath: String = "",
    val title: String = ""
)

@Serializable
object SettingsRoute

@Serializable
object PrivacyPolicyRoute

@Serializable
object DisclaimerRoute

@Serializable
object TermsRoute

@Serializable
object CookiesRoute

@Serializable
object AboutRoute

@Serializable
object ContactUsRoute
