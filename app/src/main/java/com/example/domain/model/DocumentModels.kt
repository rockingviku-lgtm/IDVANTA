package com.example.domain.model

import kotlinx.serialization.Serializable

enum class DocumentCategory(val displayName: String) {
    CV("CV"),
    RESUME("Resume"),
    SCHOOL_ID("School ID Card"),
    COLLEGE_ID("College ID Card"),
    EMPLOYEE_ID("Employee ID Card"),
    TEACHER_ID("Teacher ID Card"),
    BIODATA("Personal Biodata");

    val isCvOrResume: Boolean get() = this == CV || this == RESUME
    val isIdCard: Boolean
        get() = this == SCHOOL_ID || this == COLLEGE_ID || this == EMPLOYEE_ID || this == TEACHER_ID
}

enum class CvSubCategory(val displayName: String) {
    STUDENT_CV("Student CV"),
    FRESHER_CV("Fresher CV"),
    PROFESSIONAL_RESUME("Professional Resume"),
    EXPERIENCED_RESUME("Experienced Resume"),
    ACADEMIC_CV("Academic CV"),
    MINIMAL_RESUME("Minimal Resume"),
    ATS_FRIENDLY_RESUME("ATS-Friendly Resume")
}

enum class IdCardType(val displayName: String, val docCategory: DocumentCategory) {
    SCHOOL("School ID Card", DocumentCategory.SCHOOL_ID),
    COLLEGE("College ID Card", DocumentCategory.COLLEGE_ID),
    EMPLOYEE("Employee ID Card", DocumentCategory.EMPLOYEE_ID),
    TEACHER("Teacher ID Card", DocumentCategory.TEACHER_ID)
}

enum class TemplateCategory(val displayName: String) {
    MODERN("Modern"),
    MINIMAL("Minimal"),
    ELEGANT("Elegant"),
    CORPORATE("Corporate"),
    ACADEMIC("Academic"),
    PREMIUM("Premium"),
    CREATIVE("Creative"),
    CLASSIC("Classic"),
    PROFESSIONAL("Professional"),
    COLORFUL("Colorful")
}

enum class FontStyleOption(val displayName: String) {
    PLUS_JAKARTA("Plus Jakarta Sans"),
    PLAYFAIR("Playfair Display"),
    POPPINS("Poppins"),
    MERRIWEATHER("Merriweather"),
    ROBOTO_MONO("Roboto Mono"),
    DEVANAGARI("Noto Sans Devanagari (हिन्दी)")
}

enum class BorderStyle(val displayName: String) {
    NONE("None"),
    THIN_SOLID("Thin Solid"),
    DOUBLE_CLASSIC("Double Classic"),
    ACCENT_LEFT("Accent Left Bar"),
    ROUNDED_CARD("Rounded Frame")
}

enum class CvLayoutVariant {
    EXECUTIVE_BANNER,
    TWO_COLUMN_SIDEBAR_LEFT,
    TWO_COLUMN_SIDEBAR_RIGHT,
    SWISS_MINIMAL,
    ACADEMIC_RULED,
    ATS_SINGLE_COLUMN,
    MODERN_TIMELINE,
    CREATIVE_ACCENT_STRIP,
    CLASSIC_SERIF_DOUBLE,
    TECH_MONO_GRID,
    EDITORIAL_SPLIT,
    COMPACT_TWO_PANE,
    BOLD_HEADER_BLOCK,
    FRESHER_HIGHLIGHT,
    EXECUTIVE_BORDERED,
    SIDEBAR_DARK_NAVY,
    MINIMAL_LINE_ACCENT,
    COLOR_BLOCK_TOP,
    CARD_SECTIONED,
    PRESTIGE_CREST
}

enum class IdLayoutVariant {
    CLASSIC_NAVY_CREST,
    MODERN_GRADIENT_STRIPE,
    CORPORATE_LEFT_BAR,
    MINIMAL_WHITE_BADGE,
    ACADEMIC_GOLD_SEAL,
    VERTICAL_LANYARD_PRO,
    HORIZONTAL_EXECUTIVE,
    TECH_DARK_SLATE,
    COLORFUL_CAMPUS,
    TEACHER_PRESTIGE,
    CLEAN_GRID_CR80,
    DUO_TONE_SHIELD,
    BORDERED_OFFICIAL,
    ROUNDED_PILL_HEADER,
    COMPACT_QR_FIRST
}

enum class BiodataLayoutVariant {
    BIODATA_CLASSIC,
    BIODATA_ROYAL,
    BIODATA_MINIMAL,
    BIODATA_SIDEBAR,
    BIODATA_HINDI_HERITAGE,
    BIODATA_MODERN_CARD
}

@Serializable
data class DocumentDesignConfig(
    val templateId: String = "cv_executive_navy",
    val accentColorHex: String = "#121A3F",
    val backgroundColorHex: String = "#FFFFFF",
    val textColorHex: String = "#1A1F36",
    val fontStyle: FontStyleOption = FontStyleOption.PLUS_JAKARTA,
    val fontSizeScale: Float = 1.0f,
    val sectionSpacingScale: Float = 1.0f,
    val borderStyle: BorderStyle = BorderStyle.NONE,
    val sectionOrder: List<String> = listOf(
        "OBJECTIVE",
        "EXPERIENCE",
        "EDUCATION",
        "SKILLS",
        "PROJECTS",
        "CERTIFICATIONS",
        "ACHIEVEMENTS",
        "LANGUAGES",
        "HOBBIES",
        "REFERENCES"
    ),
    val showProfilePhoto: Boolean = true,
    val showSignature: Boolean = false,
    val showQrCode: Boolean = true,
    val institutionAuthorized: Boolean = false,
    val headerAlignCenter: Boolean = false,
    val objectiveTitle: String = "Career Objective",
    val experienceTitle: String = "Work Experience",
    val educationTitle: String = "Education",
    val skillsTitle: String = "Key Skills",
    val projectsTitle: String = "Projects",
    val certificationsTitle: String = "Certifications",
    val achievementsTitle: String = "Achievements",
    val languagesTitle: String = "Languages",
    val hobbiesTitle: String = "Hobbies & Interests",
    val referencesTitle: String = "References"
)

@Serializable
data class EducationEntry(
    val id: String = "",
    val institution: String = "",
    val qualification: String = "",
    val boardOrUniversity: String = "",
    val passingYear: String = "",
    val scoreOrCgpa: String = ""
)

@Serializable
data class WorkExperienceEntry(
    val id: String = "",
    val jobTitle: String = "",
    val companyName: String = "",
    val duration: String = "",
    val responsibilities: String = ""
)

@Serializable
data class ProjectEntry(
    val id: String = "",
    val title: String = "",
    val roleOrTech: String = "",
    val description: String = ""
)

@Serializable
data class CertificationEntry(
    val id: String = "",
    val name: String = "",
    val issuer: String = "",
    val year: String = ""
)

@Serializable
data class ReferenceEntry(
    val id: String = "",
    val name: String = "",
    val designationAndOrg: String = "",
    val contact: String = ""
)

@Serializable
data class CvDocumentData(
    val cvSubCategory: CvSubCategory = CvSubCategory.PROFESSIONAL_RESUME,
    val fullName: String = "",
    val professionalHeadline: String = "",
    val photoUri: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val linkedInUrl: String = "",
    val careerObjective: String = "",
    val education: List<EducationEntry> = emptyList(),
    val experience: List<WorkExperienceEntry> = emptyList(),
    val skills: List<String> = emptyList(),
    val projects: List<ProjectEntry> = emptyList(),
    val certifications: List<CertificationEntry> = emptyList(),
    val languages: List<String> = emptyList(),
    val achievements: List<String> = emptyList(),
    val hobbies: List<String> = emptyList(),
    val references: List<ReferenceEntry> = emptyList(),
    val signatureText: String = ""
)

@Serializable
data class IdCardData(
    val cardType: IdCardType = IdCardType.SCHOOL,
    val institutionName: String = "",
    val institutionTagline: String = "",
    val logoUri: String = "",
    val useBundledCrest: Boolean = true,
    val personName: String = "",
    val photoUri: String = "",
    val fatherName: String = "",
    val motherName: String = "",
    val className: String = "",
    val section: String = "",
    val rollNumber: String = "",
    val admissionNumber: String = "",
    val registrationNumber: String = "",
    val employeeNumber: String = "",
    val course: String = "",
    val department: String = "",
    val designation: String = "",
    val qualification: String = "",
    val dateOfBirth: String = "",
    val joiningDate: String = "",
    val address: String = "",
    val contactNumber: String = "",
    val emergencyContact: String = "",
    val bloodGroup: String = "",
    val academicSession: String = "",
    val validityDate: String = "",
    val backInstructions: String = "If found, please return to the issuing institution office. Carrying this ID card on campus/premises is mandatory.",
    val authorityTitle: String = "Principal / Authorized Signatory"
)

@Serializable
data class BiodataData(
    val name: String = "",
    val photoUri: String = "",
    val dateOfBirth: String = "",
    val gender: String = "",
    val maritalStatus: String = "",
    val nationality: String = "Indian",
    val address: String = "",
    val phone: String = "",
    val email: String = "",
    val introduction: String = "",
    val educationSummary: String = "",
    val occupation: String = "",
    val fatherName: String = "",
    val fatherOccupation: String = "",
    val motherName: String = "",
    val motherOccupation: String = "",
    val siblingsInfo: String = "",
    val familyAddress: String = "",
    val languages: List<String> = emptyList(),
    val skills: List<String> = emptyList(),
    val hobbies: List<String> = emptyList(),
    val declarationText: String = "I hereby declare that the above information is true and correct to the best of my knowledge."
)

data class DocumentTemplate(
    val id: String,
    val name: String,
    val subtitle: String,
    val targetCategory: DocumentCategory,
    val styleCategory: TemplateCategory,
    val accentHex: String,
    val backgroundHex: String,
    val textHex: String,
    val fontOption: FontStyleOption,
    val cvLayout: CvLayoutVariant? = null,
    val idLayout: IdLayoutVariant? = null,
    val biodataLayout: BiodataLayoutVariant? = null,
    val isPortraitCard: Boolean = false
)
