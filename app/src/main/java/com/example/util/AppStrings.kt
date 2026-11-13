package com.example.util

enum class AppLanguage(val code: String, val label: String) {
    ENGLISH("en", "English"),
    HINDI("hi", "हिन्दी (Hindi)")
}

data class LocalizedLabels(
    val quickActions: String,
    val recentDocuments: String,
    val favoriteTemplates: String,
    val categories: String,
    val searchPlaceholder: String,
    val createCv: String,
    val resumeMaker: String,
    val schoolIdCard: String,
    val collegeIdCard: String,
    val employeeIdCard: String,
    val teacherIdCard: String,
    val personalBiodata: String,
    val bulkIdGenerator: String,
    val templateLibrary: String,
    val myDocuments: String,
    val saveDraft: String,
    val exportPdf: String,
    val noDocumentsYet: String,
    val noDocumentsTip: String
)

object AppStrings {
    fun forLanguage(language: AppLanguage): LocalizedLabels {
        return when (language) {
            AppLanguage.ENGLISH -> LocalizedLabels(
                quickActions = "Create & Manage",
                recentDocuments = "Recently Edited",
                favoriteTemplates = "Favorite Templates",
                categories = "Categories",
                searchPlaceholder = "Search documents...",
                createCv = "Create CV",
                resumeMaker = "Resume Maker",
                schoolIdCard = "School ID Card",
                collegeIdCard = "College ID Card",
                employeeIdCard = "Employee ID Card",
                teacherIdCard = "Teacher ID Card",
                personalBiodata = "Personal Biodata",
                bulkIdGenerator = "Bulk ID Generator",
                templateLibrary = "Template Library",
                myDocuments = "My Documents",
                saveDraft = "Save Draft",
                exportPdf = "Export PDF",
                noDocumentsYet = "No saved documents yet",
                noDocumentsTip = "Choose a document creator above to start designing."
            )
            AppLanguage.HINDI -> LocalizedLabels(
                quickActions = "बनाएँ और प्रबंधित करें",
                recentDocuments = "हाल ही में संपादित",
                favoriteTemplates = "पसंदीदा टेम्पलेट्स",
                categories = "श्रेणियाँ",
                searchPlaceholder = "दस्तावेज़ खोजें...",
                createCv = "सीवी बनाएँ",
                resumeMaker = "रिज्यूमे मेकर",
                schoolIdCard = "स्कूल आईडी कार्ड",
                collegeIdCard = "कॉलेज आईडी कार्ड",
                employeeIdCard = "कर्मचारी आईडी कार्ड",
                teacherIdCard = "शिक्षक आईडी कार्ड",
                personalBiodata = "व्यक्तिगत बायोडाटा",
                bulkIdGenerator = "बल्क आईडी जनरेटर",
                templateLibrary = "टेम्पलेट लाइब्रेरी",
                myDocuments = "मेरे दस्तावेज़",
                saveDraft = "ड्राफ्ट सहेजें",
                exportPdf = "पीडीएफ निर्यात",
                noDocumentsYet = "अभी कोई सहेजा गया दस्तावेज़ नहीं है",
                noDocumentsTip = "दस्तावेज़ बनाना शुरू करने के लिए ऊपर कोई विकल्प चुनें।"
            )
        }
    }
}
