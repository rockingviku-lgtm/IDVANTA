package com.example.domain.model

object TemplateCatalog {

    val cvTemplates: List<DocumentTemplate> = listOf(
        DocumentTemplate(
            id = "cv_executive_navy",
            name = "Executive Navy",
            subtitle = "Bold top banner with crisp two-tone structure",
            targetCategory = DocumentCategory.RESUME,
            styleCategory = TemplateCategory.CORPORATE,
            accentHex = "#121A3F",
            backgroundHex = "#FFFFFF",
            textHex = "#1A1F36",
            fontOption = FontStyleOption.PLUS_JAKARTA,
            cvLayout = CvLayoutVariant.EXECUTIVE_BANNER
        ),
        DocumentTemplate(
            id = "cv_sidebar_royal",
            name = "Royal Sidebar",
            subtitle = "Left indigo sidebar for skills & contact details",
            targetCategory = DocumentCategory.CV,
            styleCategory = TemplateCategory.MODERN,
            accentHex = "#6D4AFF",
            backgroundHex = "#FFFFFF",
            textHex = "#1B1E2E",
            fontOption = FontStyleOption.POPPINS,
            cvLayout = CvLayoutVariant.TWO_COLUMN_SIDEBAR_LEFT
        ),
        DocumentTemplate(
            id = "cv_right_rail",
            name = "Editorial Right Rail",
            subtitle = "Narrative main column with right summary rail",
            targetCategory = DocumentCategory.RESUME,
            styleCategory = TemplateCategory.ELEGANT,
            accentHex = "#3E2C7D",
            backgroundHex = "#FAF9FE",
            textHex = "#1F2233",
            fontOption = FontStyleOption.PLUS_JAKARTA,
            cvLayout = CvLayoutVariant.TWO_COLUMN_SIDEBAR_RIGHT
        ),
        DocumentTemplate(
            id = "cv_swiss_minimal",
            name = "Swiss Minimal",
            subtitle = "Grid-aligned typography with ample whitespace",
            targetCategory = DocumentCategory.RESUME,
            styleCategory = TemplateCategory.MINIMAL,
            accentHex = "#18181B",
            backgroundHex = "#FFFFFF",
            textHex = "#18181B",
            fontOption = FontStyleOption.PLUS_JAKARTA,
            cvLayout = CvLayoutVariant.SWISS_MINIMAL
        ),
        DocumentTemplate(
            id = "cv_academic_oxford",
            name = "Academic Monograph",
            subtitle = "Serif ruled sections ideal for educators & scholars",
            targetCategory = DocumentCategory.CV,
            styleCategory = TemplateCategory.ACADEMIC,
            accentHex = "#1E3A5F",
            backgroundHex = "#FFFDF9",
            textHex = "#1F242D",
            fontOption = FontStyleOption.MERRIWEATHER,
            cvLayout = CvLayoutVariant.ACADEMIC_RULED
        ),
        DocumentTemplate(
            id = "cv_ats_clarity",
            name = "ATS Clarity Pro",
            subtitle = "Single-column parser-safe clean layout",
            targetCategory = DocumentCategory.RESUME,
            styleCategory = TemplateCategory.PROFESSIONAL,
            accentHex = "#1F3B73",
            backgroundHex = "#FFFFFF",
            textHex = "#111827",
            fontOption = FontStyleOption.PLUS_JAKARTA,
            cvLayout = CvLayoutVariant.ATS_SINGLE_COLUMN
        ),
        DocumentTemplate(
            id = "cv_modern_timeline",
            name = "Chrono Timeline",
            subtitle = "Vertical node timeline for career progression",
            targetCategory = DocumentCategory.RESUME,
            styleCategory = TemplateCategory.MODERN,
            accentHex = "#5736E6",
            backgroundHex = "#FFFFFF",
            textHex = "#191C2E",
            fontOption = FontStyleOption.POPPINS,
            cvLayout = CvLayoutVariant.MODERN_TIMELINE
        ),
        DocumentTemplate(
            id = "cv_creative_strip",
            name = "Studio Accent Strip",
            subtitle = "Vertical left color ribbon with modern badges",
            targetCategory = DocumentCategory.CV,
            styleCategory = TemplateCategory.CREATIVE,
            accentHex = "#0D7377",
            backgroundHex = "#FFFFFF",
            textHex = "#1D242B",
            fontOption = FontStyleOption.POPPINS,
            cvLayout = CvLayoutVariant.CREATIVE_ACCENT_STRIP
        ),
        DocumentTemplate(
            id = "cv_classic_serif",
            name = "Heritage Serif",
            subtitle = "Centered classical header with double hairline rules",
            targetCategory = DocumentCategory.CV,
            styleCategory = TemplateCategory.CLASSIC,
            accentHex = "#4A2545",
            backgroundHex = "#FFFDFB",
            textHex = "#241922",
            fontOption = FontStyleOption.PLAYFAIR,
            cvLayout = CvLayoutVariant.CLASSIC_SERIF_DOUBLE
        ),
        DocumentTemplate(
            id = "cv_tech_mono",
            name = "Engineering Spec",
            subtitle = "Monospace technical labels with skill matrix",
            targetCategory = DocumentCategory.RESUME,
            styleCategory = TemplateCategory.MODERN,
            accentHex = "#0F4C81",
            backgroundHex = "#F8FAFC",
            textHex = "#0F172A",
            fontOption = FontStyleOption.ROBOTO_MONO,
            cvLayout = CvLayoutVariant.TECH_MONO_GRID
        ),
        DocumentTemplate(
            id = "cv_editorial_split",
            name = "Editorial Split",
            subtitle = "Asymmetric top masthead and balanced columns",
            targetCategory = DocumentCategory.RESUME,
            styleCategory = TemplateCategory.PREMIUM,
            accentHex = "#2A245E",
            backgroundHex = "#FFFFFF",
            textHex = "#17182B",
            fontOption = FontStyleOption.PLAYFAIR,
            cvLayout = CvLayoutVariant.EDITORIAL_SPLIT
        ),
        DocumentTemplate(
            id = "cv_compact_pane",
            name = "Compact Two-Pane",
            subtitle = "Dense one-page executive summary & career list",
            targetCategory = DocumentCategory.RESUME,
            styleCategory = TemplateCategory.PROFESSIONAL,
            accentHex = "#274C77",
            backgroundHex = "#FFFFFF",
            textHex = "#1B263B",
            fontOption = FontStyleOption.PLUS_JAKARTA,
            cvLayout = CvLayoutVariant.COMPACT_TWO_PANE
        ),
        DocumentTemplate(
            id = "cv_bold_header",
            name = "Vanguard Header",
            subtitle = "Full-width contrasting header block & pills",
            targetCategory = DocumentCategory.CV,
            styleCategory = TemplateCategory.COLORFUL,
            accentHex = "#6D4AFF",
            backgroundHex = "#FFFFFF",
            textHex = "#161B33",
            fontOption = FontStyleOption.POPPINS,
            cvLayout = CvLayoutVariant.BOLD_HEADER_BLOCK
        ),
        DocumentTemplate(
            id = "cv_fresher_launch",
            name = "Campus Launch",
            subtitle = "Education & projects spotlight for graduates",
            targetCategory = DocumentCategory.CV,
            styleCategory = TemplateCategory.ACADEMIC,
            accentHex = "#2B6CB0",
            backgroundHex = "#FFFFFF",
            textHex = "#1A202C",
            fontOption = FontStyleOption.PLUS_JAKARTA,
            cvLayout = CvLayoutVariant.FRESHER_HIGHLIGHT
        ),
        DocumentTemplate(
            id = "cv_executive_bordered",
            name = "Chancery Framed",
            subtitle = "Full page perimeter frame with formal seal alignment",
            targetCategory = DocumentCategory.CV,
            styleCategory = TemplateCategory.CLASSIC,
            accentHex = "#121A3F",
            backgroundHex = "#FFFEFA",
            textHex = "#141A33",
            fontOption = FontStyleOption.MERRIWEATHER,
            cvLayout = CvLayoutVariant.EXECUTIVE_BORDERED
        ),
        DocumentTemplate(
            id = "cv_dark_navy_sidebar",
            name = "Midnight Sidebar",
            subtitle = "Deep navy side panel with crisp ivory typography",
            targetCategory = DocumentCategory.RESUME,
            styleCategory = TemplateCategory.PREMIUM,
            accentHex = "#121A3F",
            backgroundHex = "#FFFFFF",
            textHex = "#1A2038",
            fontOption = FontStyleOption.PLUS_JAKARTA,
            cvLayout = CvLayoutVariant.SIDEBAR_DARK_NAVY
        ),
        DocumentTemplate(
            id = "cv_minimal_line",
            name = "Hairline Atelier",
            subtitle = "Subtle horizontal dividers with airy spacing",
            targetCategory = DocumentCategory.RESUME,
            styleCategory = TemplateCategory.MINIMAL,
            accentHex = "#4C35B5",
            backgroundHex = "#FFFFFF",
            textHex = "#222431",
            fontOption = FontStyleOption.PLUS_JAKARTA,
            cvLayout = CvLayoutVariant.MINIMAL_LINE_ACCENT
        ),
        DocumentTemplate(
            id = "cv_color_block",
            name = "Spectrum Banner",
            subtitle = "Vibrant coral-plum top block & clean card sections",
            targetCategory = DocumentCategory.CV,
            styleCategory = TemplateCategory.COLORFUL,
            accentHex = "#7C3AED",
            backgroundHex = "#FCFBFF",
            textHex = "#1E1B4B",
            fontOption = FontStyleOption.POPPINS,
            cvLayout = CvLayoutVariant.COLOR_BLOCK_TOP
        ),
        DocumentTemplate(
            id = "cv_card_sectioned",
            name = "Modular Cards",
            subtitle = "Elevated subtle tinted section blocks",
            targetCategory = DocumentCategory.RESUME,
            styleCategory = TemplateCategory.CREATIVE,
            accentHex = "#1D4ED8",
            backgroundHex = "#F5F7FB",
            textHex = "#111827",
            fontOption = FontStyleOption.PLUS_JAKARTA,
            cvLayout = CvLayoutVariant.CARD_SECTIONED
        ),
        DocumentTemplate(
            id = "cv_prestige_hindi",
            name = "Prestige Bilingual (हिन्दी/EN)",
            subtitle = "Devanagari-optimized formal CV with gold crest accent",
            targetCategory = DocumentCategory.CV,
            styleCategory = TemplateCategory.ELEGANT,
            accentHex = "#121A3F",
            backgroundHex = "#FFFFFF",
            textHex = "#181C30",
            fontOption = FontStyleOption.DEVANAGARI,
            cvLayout = CvLayoutVariant.PRESTIGE_CREST
        )
    )

    val idTemplates: List<DocumentTemplate> = listOf(
        DocumentTemplate(
            id = "id_classic_navy_crest",
            name = "Navy Academy Crest",
            subtitle = "Official school crest header & clean CR80 grid",
            targetCategory = DocumentCategory.SCHOOL_ID,
            styleCategory = TemplateCategory.ACADEMIC,
            accentHex = "#121A3F",
            backgroundHex = "#FFFFFF",
            textHex = "#121A3F",
            fontOption = FontStyleOption.PLUS_JAKARTA,
            idLayout = IdLayoutVariant.CLASSIC_NAVY_CREST
        ),
        DocumentTemplate(
            id = "id_modern_purple_stripe",
            name = "Royal Stripe Campus",
            subtitle = "Diagonal royal purple band & QR verification badge",
            targetCategory = DocumentCategory.SCHOOL_ID,
            styleCategory = TemplateCategory.MODERN,
            accentHex = "#6D4AFF",
            backgroundHex = "#FFFFFF",
            textHex = "#181B2C",
            fontOption = FontStyleOption.POPPINS,
            idLayout = IdLayoutVariant.MODERN_GRADIENT_STRIPE
        ),
        DocumentTemplate(
            id = "id_corporate_left_bar",
            name = "Corporate Left Pillar",
            subtitle = "Sleek vertical accent spine for employee badges",
            targetCategory = DocumentCategory.EMPLOYEE_ID,
            styleCategory = TemplateCategory.CORPORATE,
            accentHex = "#1E293B",
            backgroundHex = "#FFFFFF",
            textHex = "#0F172A",
            fontOption = FontStyleOption.PLUS_JAKARTA,
            idLayout = IdLayoutVariant.CORPORATE_LEFT_BAR
        ),
        DocumentTemplate(
            id = "id_minimal_white",
            name = "Pure Minimal CR80",
            subtitle = "Clean monochrome white badge with micro-typography",
            targetCategory = DocumentCategory.EMPLOYEE_ID,
            styleCategory = TemplateCategory.MINIMAL,
            accentHex = "#27272A",
            backgroundHex = "#FFFFFF",
            textHex = "#18181B",
            fontOption = FontStyleOption.PLUS_JAKARTA,
            idLayout = IdLayoutVariant.MINIMAL_WHITE_BADGE
        ),
        DocumentTemplate(
            id = "id_academic_gold",
            name = "Gold Seal University",
            subtitle = "Prestigious navy & gold double-framed college ID",
            targetCategory = DocumentCategory.COLLEGE_ID,
            styleCategory = TemplateCategory.PREMIUM,
            accentHex = "#162447",
            backgroundHex = "#FFFDF8",
            textHex = "#162447",
            fontOption = FontStyleOption.MERRIWEATHER,
            idLayout = IdLayoutVariant.ACADEMIC_GOLD_SEAL
        ),
        DocumentTemplate(
            id = "id_vertical_lanyard",
            name = "Vertical Lanyard Pro",
            subtitle = "Portrait orientation badge with lanyard slot mark",
            targetCategory = DocumentCategory.COLLEGE_ID,
            styleCategory = TemplateCategory.PROFESSIONAL,
            accentHex = "#3B28CC",
            backgroundHex = "#FFFFFF",
            textHex = "#17192D",
            fontOption = FontStyleOption.POPPINS,
            idLayout = IdLayoutVariant.VERTICAL_LANYARD_PRO,
            isPortraitCard = true
        ),
        DocumentTemplate(
            id = "id_horizontal_exec",
            name = "Executive Landscape",
            subtitle = "Balanced landscape corporate credential",
            targetCategory = DocumentCategory.EMPLOYEE_ID,
            styleCategory = TemplateCategory.ELEGANT,
            accentHex = "#121A3F",
            backgroundHex = "#F9FAFC",
            textHex = "#121A3F",
            fontOption = FontStyleOption.PLUS_JAKARTA,
            idLayout = IdLayoutVariant.HORIZONTAL_EXECUTIVE
        ),
        DocumentTemplate(
            id = "id_tech_dark_slate",
            name = "Obsidian Tech Pass",
            subtitle = "High-contrast dark header with monospace ID tag",
            targetCategory = DocumentCategory.EMPLOYEE_ID,
            styleCategory = TemplateCategory.CREATIVE,
            accentHex = "#0F172A",
            backgroundHex = "#F8FAFC",
            textHex = "#0F172A",
            fontOption = FontStyleOption.ROBOTO_MONO,
            idLayout = IdLayoutVariant.TECH_DARK_SLATE
        ),
        DocumentTemplate(
            id = "id_colorful_campus",
            name = "Vibrant Scholar",
            subtitle = "Teal & lavender energetic school student pass",
            targetCategory = DocumentCategory.SCHOOL_ID,
            styleCategory = TemplateCategory.COLORFUL,
            accentHex = "#0D9488",
            backgroundHex = "#FFFFFF",
            textHex = "#132A2E",
            fontOption = FontStyleOption.POPPINS,
            idLayout = IdLayoutVariant.COLORFUL_CAMPUS
        ),
        DocumentTemplate(
            id = "id_teacher_prestige",
            name = "Faculty Prestige",
            subtitle = "Distinguished burgundy-navy card for teachers",
            targetCategory = DocumentCategory.TEACHER_ID,
            styleCategory = TemplateCategory.CLASSIC,
            accentHex = "#4A154B",
            backgroundHex = "#FFFFFF",
            textHex = "#1F1629",
            fontOption = FontStyleOption.MERRIWEATHER,
            idLayout = IdLayoutVariant.TEACHER_PRESTIGE
        ),
        DocumentTemplate(
            id = "id_clean_grid",
            name = "Structured Table CR80",
            subtitle = "Boxed key-value rows for instant office verification",
            targetCategory = DocumentCategory.SCHOOL_ID,
            styleCategory = TemplateCategory.PROFESSIONAL,
            accentHex = "#1D4ED8",
            backgroundHex = "#FFFFFF",
            textHex = "#1E293B",
            fontOption = FontStyleOption.PLUS_JAKARTA,
            idLayout = IdLayoutVariant.CLEAN_GRID_CR80
        ),
        DocumentTemplate(
            id = "id_duotone_shield",
            name = "Duo-Tone Faculty Shield",
            subtitle = "Vertical split-tone card for academic staff",
            targetCategory = DocumentCategory.TEACHER_ID,
            styleCategory = TemplateCategory.ELEGANT,
            accentHex = "#283593",
            backgroundHex = "#FFFFFF",
            textHex = "#1A237E",
            fontOption = FontStyleOption.PLUS_JAKARTA,
            idLayout = IdLayoutVariant.DUO_TONE_SHIELD,
            isPortraitCard = true
        ),
        DocumentTemplate(
            id = "id_bordered_official",
            name = "Chancery Official",
            subtitle = "Formal bordered institution card with seal mark",
            targetCategory = DocumentCategory.TEACHER_ID,
            styleCategory = TemplateCategory.CLASSIC,
            accentHex = "#121A3F",
            backgroundHex = "#FFFEFB",
            textHex = "#121A3F",
            fontOption = FontStyleOption.PLAYFAIR,
            idLayout = IdLayoutVariant.BORDERED_OFFICIAL
        ),
        DocumentTemplate(
            id = "id_pill_header",
            name = "Campus Pill Header",
            subtitle = "Soft lavender header capsule with rounded portrait",
            targetCategory = DocumentCategory.COLLEGE_ID,
            styleCategory = TemplateCategory.MODERN,
            accentHex = "#6D4AFF",
            backgroundHex = "#FFFFFF",
            textHex = "#1B1E36",
            fontOption = FontStyleOption.POPPINS,
            idLayout = IdLayoutVariant.ROUNDED_PILL_HEADER
        ),
        DocumentTemplate(
            id = "id_qr_first",
            name = "Smart QR Pass",
            subtitle = "Prominent QR scan block for attendance & library",
            targetCategory = DocumentCategory.COLLEGE_ID,
            styleCategory = TemplateCategory.CREATIVE,
            accentHex = "#0369A1",
            backgroundHex = "#FFFFFF",
            textHex = "#0C4A6E",
            fontOption = FontStyleOption.PLUS_JAKARTA,
            idLayout = IdLayoutVariant.COMPACT_QR_FIRST
        )
    )

    val biodataTemplates: List<DocumentTemplate> = listOf(
        DocumentTemplate(
            id = "bio_classic_ivory",
            name = "Classic Framed Biodata",
            subtitle = "Double-border formal personal & family dossier",
            targetCategory = DocumentCategory.BIODATA,
            styleCategory = TemplateCategory.CLASSIC,
            accentHex = "#121A3F",
            backgroundHex = "#FFFDF9",
            textHex = "#1A1F36",
            fontOption = FontStyleOption.PLAYFAIR,
            biodataLayout = BiodataLayoutVariant.BIODATA_CLASSIC
        ),
        DocumentTemplate(
            id = "bio_royal_purple",
            name = "Royal Crest Biodata",
            subtitle = "Elevated header banner with structured sections",
            targetCategory = DocumentCategory.BIODATA,
            styleCategory = TemplateCategory.PREMIUM,
            accentHex = "#6D4AFF",
            backgroundHex = "#FFFFFF",
            textHex = "#191B2E",
            fontOption = FontStyleOption.PLUS_JAKARTA,
            biodataLayout = BiodataLayoutVariant.BIODATA_ROYAL
        ),
        DocumentTemplate(
            id = "bio_minimal_clean",
            name = "Swiss Clean Biodata",
            subtitle = "Minimal monochrome hierarchy for fast reading",
            targetCategory = DocumentCategory.BIODATA,
            styleCategory = TemplateCategory.MINIMAL,
            accentHex = "#27272A",
            backgroundHex = "#FFFFFF",
            textHex = "#18181B",
            fontOption = FontStyleOption.PLUS_JAKARTA,
            biodataLayout = BiodataLayoutVariant.BIODATA_MINIMAL
        ),
        DocumentTemplate(
            id = "bio_sidebar_navy",
            name = "Executive Two-Column",
            subtitle = "Left portrait & contact rail with family timeline",
            targetCategory = DocumentCategory.BIODATA,
            styleCategory = TemplateCategory.PROFESSIONAL,
            accentHex = "#1E3A5F",
            backgroundHex = "#FFFFFF",
            textHex = "#162033",
            fontOption = FontStyleOption.POPPINS,
            biodataLayout = BiodataLayoutVariant.BIODATA_SIDEBAR
        ),
        DocumentTemplate(
            id = "bio_hindi_heritage",
            name = "संस्कृति बायोडाटा (Hindi)",
            subtitle = "Devanagari-tuned ornamental gold & maroon frame",
            targetCategory = DocumentCategory.BIODATA,
            styleCategory = TemplateCategory.ELEGANT,
            accentHex = "#7B1E3C",
            backgroundHex = "#FFFBF5",
            textHex = "#2D1520",
            fontOption = FontStyleOption.DEVANAGARI,
            biodataLayout = BiodataLayoutVariant.BIODATA_HINDI_HERITAGE
        ),
        DocumentTemplate(
            id = "bio_modern_card",
            name = "Modern Card Dossier",
            subtitle = "Soft tinted cards for education, skills & family",
            targetCategory = DocumentCategory.BIODATA,
            styleCategory = TemplateCategory.MODERN,
            accentHex = "#4F46E5",
            backgroundHex = "#F8F9FE",
            textHex = "#1E1B4B",
            fontOption = FontStyleOption.PLUS_JAKARTA,
            biodataLayout = BiodataLayoutVariant.BIODATA_MODERN_CARD
        )
    )

    val allTemplates: List<DocumentTemplate> = cvTemplates + idTemplates + biodataTemplates

    fun findById(id: String): DocumentTemplate {
        return allTemplates.firstOrNull { it.id == id } ?: cvTemplates.first()
    }

    fun defaultTemplateFor(category: DocumentCategory): DocumentTemplate {
        return when (category) {
            DocumentCategory.CV -> cvTemplates[1]
            DocumentCategory.RESUME -> cvTemplates[0]
            DocumentCategory.SCHOOL_ID -> idTemplates[0]
            DocumentCategory.COLLEGE_ID -> idTemplates[4]
            DocumentCategory.EMPLOYEE_ID -> idTemplates[2]
            DocumentCategory.TEACHER_ID -> idTemplates[9]
            DocumentCategory.BIODATA -> biodataTemplates[0]
        }
    }
}
