package com.example.pdf

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.text.Layout
import com.example.domain.model.BorderStyle
import com.example.domain.model.CvDocumentData
import com.example.domain.model.CvLayoutVariant
import com.example.domain.model.DocumentDesignConfig
import com.example.domain.model.TemplateCatalog
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CvPdfRenderer @Inject constructor(
    private val fontHelper: PdfFontHelper
) {
    companion object {
        const val A4_WIDTH = 595
        const val A4_HEIGHT = 842
    }

    fun render(pdf: PdfDocument, data: CvDocumentData, design: DocumentDesignConfig): Int {
        val template = TemplateCatalog.findById(design.templateId)
        val variant = template.cvLayout ?: CvLayoutVariant.EXECUTIVE_BANNER

        val accent = fontHelper.parseColorSafe(design.accentColorHex, Color.parseColor("#121A3F"))
        val bg = fontHelper.parseColorSafe(design.backgroundColorHex, Color.WHITE)
        val textCol = fontHelper.parseColorSafe(design.textColorHex, Color.parseColor("#1A1F36"))
        val mutedCol = Color.rgb(
            ((Color.red(textCol) + 128) / 2).coerceAtMost(140),
            ((Color.green(textCol) + 128) / 2).coerceAtMost(140),
            ((Color.blue(textCol) + 128) / 2).coerceAtMost(155)
        )
        val softAccent = fontHelper.lightenColor(accent, 0.90f)
        val photoBmp = if (design.showProfilePhoto && data.photoUri.isNotBlank()) {
            fontHelper.loadBitmapFromUriOrPath(data.photoUri, 400)
        } else null

        var pageNum = 1
        var page = startNewPage(pdf, pageNum, bg, accent, design.borderStyle, variant)
        var canvas = page.canvas

        val margin = 38f
        val contentW = A4_WIDTH - margin * 2
        val scale = design.fontSizeScale.coerceIn(0.8f, 1.3f)
        val spacing = 12f * design.sectionSpacingScale.coerceIn(0.7f, 1.5f)

        val isTwoColLeft = variant in listOf(
            CvLayoutVariant.TWO_COLUMN_SIDEBAR_LEFT,
            CvLayoutVariant.SIDEBAR_DARK_NAVY,
            CvLayoutVariant.COMPACT_TWO_PANE
        )
        val isTwoColRight = variant == CvLayoutVariant.TWO_COLUMN_SIDEBAR_RIGHT

        if (isTwoColLeft || isTwoColRight) {
            val sideW = 176f
            val gap = 20f
            val mainW = contentW - sideW - gap
            val sideX = if (isTwoColLeft) margin else margin + mainW + gap
            val mainX = if (isTwoColLeft) margin + sideW + gap else margin

            // Sidebar backdrop on Page 1
            val sideBgColor = when (variant) {
                CvLayoutVariant.SIDEBAR_DARK_NAVY -> accent
                else -> softAccent
            }
            val sideTextColor = if (variant == CvLayoutVariant.SIDEBAR_DARK_NAVY) Color.WHITE else textCol
            val sideAccentColor = if (variant == CvLayoutVariant.SIDEBAR_DARK_NAVY) Color.parseColor("#D6CCFF") else accent

            val sideRect = if (isTwoColLeft) {
                RectF(0f, 0f, sideX + sideW + 10f, A4_HEIGHT.toFloat())
            } else {
                RectF(sideX - 10f, 0f, A4_WIDTH.toFloat(), A4_HEIGHT.toFloat())
            }
            canvas.drawRect(sideRect, Paint().apply { color = sideBgColor })

            var sideY = margin
            if (photoBmp != null) {
                val pSize = 84f
                val px = sideX + (sideW - pSize) / 2f
                fontHelper.drawCenterCroppedBitmap(canvas, photoBmp, RectF(px, sideY, px + pSize, sideY + pSize), 12f)
                sideY += pSize + 14f
            }

            // Contact block in sidebar
            val contactTitlePaint = fontHelper.createTextPaint(10f * scale, sideAccentColor, design.fontStyle, "CONTACT", true)
            val sideBodyPaint = fontHelper.createTextPaint(8.8f * scale, sideTextColor, design.fontStyle, data.fullName + data.address, false)
            sideY = fontHelper.drawTextBlock(canvas, "CONTACT", sideX, sideY, sideW, contactTitlePaint) + 5f
            listOfNotNull(
                data.phone.takeIf { it.isNotBlank() }?.let { "Tel: $it" },
                data.email.takeIf { it.isNotBlank() },
                data.address.takeIf { it.isNotBlank() },
                data.linkedInUrl.takeIf { it.isNotBlank() }
            ).forEach { item ->
                sideY = fontHelper.drawTextBlock(canvas, item, sideX, sideY, sideW, sideBodyPaint) + 4f
            }

            // Skills / Languages / Hobbies in sidebar
            fun drawSideChips(title: String, items: List<String>) {
                if (items.isEmpty()) return
                sideY += 10f
                sideY = fontHelper.drawTextBlock(canvas, title.uppercase(), sideX, sideY, sideW, contactTitlePaint) + 5f
                items.forEach { item ->
                    val p = fontHelper.createTextPaint(8.8f * scale, sideTextColor, design.fontStyle, item, false)
                    sideY = fontHelper.drawTextBlock(canvas, "• $item", sideX, sideY, sideW, p) + 3f
                }
            }
            drawSideChips(design.skillsTitle, data.skills)
            drawSideChips(design.languagesTitle, data.languages)
            drawSideChips(design.hobbiesTitle, data.hobbies)

            // Main column header
            var mainY = margin
            val namePaint = fontHelper.createTextPaint(21f * scale, accent, design.fontStyle, data.fullName, true)
            mainY = fontHelper.drawTextBlock(canvas, data.fullName.ifBlank { "Your Full Name" }, mainX, mainY, mainW, namePaint) + 3f
            if (data.professionalHeadline.isNotBlank()) {
                val headPaint = fontHelper.createTextPaint(11f * scale, mutedCol, design.fontStyle, data.professionalHeadline, false)
                mainY = fontHelper.drawTextBlock(canvas, data.professionalHeadline, mainX, mainY, mainW, headPaint) + 6f
            }
            canvas.drawLine(mainX, mainY, mainX + mainW, mainY, Paint().apply { color = accent; strokeWidth = 1.6f })
            mainY += 12f

            val mainSections = design.sectionOrder.filterNot { it in listOf("SKILLS", "LANGUAGES", "HOBBIES") }
            for (secKey in mainSections) {
                val secContent = buildSectionLines(secKey, data, design)
                if (secContent == null) continue
                val estH = estimateSectionHeight(secContent.first, secContent.second, mainW, scale, design)
                if (mainY + estH > A4_HEIGHT - 44f) {
                    drawPageFooter(canvas, pageNum, mutedCol)
                    pdf.finishPage(page)
                    pageNum++
                    page = startNewPage(pdf, pageNum, bg, accent, design.borderStyle, variant)
                    canvas = page.canvas
                    mainY = margin
                }
                mainY = drawSectionBlock(
                    canvas, secContent.first, secContent.second, mainX, mainY, mainW,
                    accent, textCol, mutedCol, softAccent, design, variant, scale
                ) + spacing
            }

            if (design.showSignature && data.signatureText.isNotBlank()) {
                val sigY = maxOf(mainY + 14f, A4_HEIGHT - 78f).coerceAtMost(A4_HEIGHT - 56f)
                val sigPaint = fontHelper.createTextPaint(11f * scale, textCol, design.fontStyle, data.signatureText, true)
                fontHelper.drawTextBlock(canvas, "Signature: ${data.signatureText}", mainX, sigY, mainW, sigPaint, Layout.Alignment.ALIGN_OPPOSITE)
            }

            drawPageFooter(canvas, pageNum, mutedCol)
            pdf.finishPage(page)
            return pageNum
        }

        // Full-width single-column & header-banner variants
        var y = margin

        val hasTopBanner = variant in listOf(
            CvLayoutVariant.EXECUTIVE_BANNER,
            CvLayoutVariant.BOLD_HEADER_BLOCK,
            CvLayoutVariant.COLOR_BLOCK_TOP,
            CvLayoutVariant.PRESTIGE_CREST
        )

        if (variant == CvLayoutVariant.CREATIVE_ACCENT_STRIP) {
            canvas.drawRect(0f, 0f, 16f, A4_HEIGHT.toFloat(), Paint().apply { color = accent })
        }

        if (hasTopBanner) {
            val bannerH = 104f
            canvas.drawRect(0f, 0f, A4_WIDTH.toFloat(), bannerH, Paint().apply { color = accent })
            val namePaint = fontHelper.createTextPaint(21f * scale, Color.WHITE, design.fontStyle, data.fullName, true)
            val subPaint = fontHelper.createTextPaint(10.5f * scale, Color.parseColor("#E6E2FF"), design.fontStyle, data.professionalHeadline, false)
            val metaPaint = fontHelper.createTextPaint(8.8f * scale, Color.parseColor("#D6CCFF"), design.fontStyle, data.address, false)

            val textMaxW = if (photoBmp != null) contentW - 86f else contentW
            var ty = 22f
            ty = fontHelper.drawTextBlock(canvas, data.fullName.ifBlank { "Your Full Name" }, margin, ty, textMaxW, namePaint) + 3f
            if (data.professionalHeadline.isNotBlank()) {
                ty = fontHelper.drawTextBlock(canvas, data.professionalHeadline, margin, ty, textMaxW, subPaint) + 5f
            }
            val contactLine = listOf(data.phone, data.email, data.address, data.linkedInUrl)
                .filter { it.isNotBlank() }.joinToString("  •  ")
            if (contactLine.isNotBlank()) {
                fontHelper.drawTextBlock(canvas, contactLine, margin, ty, textMaxW, metaPaint)
            }
            if (photoBmp != null) {
                val pRect = RectF(A4_WIDTH - margin - 68f, 18f, A4_WIDTH - margin, 86f)
                fontHelper.drawCenterCroppedBitmap(canvas, photoBmp, pRect, 10f)
            }
            y = bannerH + 18f
        } else {
            val align = if (design.headerAlignCenter || variant == CvLayoutVariant.CLASSIC_SERIF_DOUBLE) {
                Layout.Alignment.ALIGN_CENTER
            } else {
                Layout.Alignment.ALIGN_NORMAL
            }
            val textW = if (photoBmp != null && align == Layout.Alignment.ALIGN_NORMAL) contentW - 76f else contentW
            val namePaint = fontHelper.createTextPaint(22f * scale, accent, design.fontStyle, data.fullName, true)
            val subPaint = fontHelper.createTextPaint(11f * scale, mutedCol, design.fontStyle, data.professionalHeadline, false)
            val metaPaint = fontHelper.createTextPaint(9f * scale, textCol, design.fontStyle, data.address, false)

            if (photoBmp != null && align == Layout.Alignment.ALIGN_NORMAL) {
                fontHelper.drawCenterCroppedBitmap(
                    canvas, photoBmp,
                    RectF(A4_WIDTH - margin - 64f, y, A4_WIDTH - margin, y + 64f), 8f
                )
            }
            y = fontHelper.drawTextBlock(canvas, data.fullName.ifBlank { "Your Full Name" }, margin, y, textW, namePaint, align) + 3f
            if (data.professionalHeadline.isNotBlank()) {
                y = fontHelper.drawTextBlock(canvas, data.professionalHeadline, margin, y, textW, subPaint, align) + 4f
            }
            val contactLine = listOf(data.phone, data.email, data.address, data.linkedInUrl)
                .filter { it.isNotBlank() }.joinToString("  |  ")
            if (contactLine.isNotBlank()) {
                y = fontHelper.drawTextBlock(canvas, contactLine, margin, y, textW, metaPaint, align) + 8f
            }

            if (variant == CvLayoutVariant.CLASSIC_SERIF_DOUBLE) {
                val lineP = Paint().apply { color = accent; strokeWidth = 1.6f }
                canvas.drawLine(margin, y, margin + contentW, y, lineP)
                canvas.drawLine(margin, y + 3f, margin + contentW, y + 3f, Paint().apply { color = accent; strokeWidth = 0.7f })
                y += 14f
            } else {
                canvas.drawLine(margin, y, margin + contentW, y, Paint().apply { color = accent; strokeWidth = 1.4f })
                y += 12f
            }
        }

        for (secKey in design.sectionOrder) {
            val secContent = buildSectionLines(secKey, data, design) ?: continue
            val estH = estimateSectionHeight(secContent.first, secContent.second, contentW, scale, design)
            if (y + minOf(estH, 120f) > A4_HEIGHT - 48f) {
                drawPageFooter(canvas, pageNum, mutedCol)
                pdf.finishPage(page)
                pageNum++
                page = startNewPage(pdf, pageNum, bg, accent, design.borderStyle, variant)
                canvas = page.canvas
                y = margin
            }
            y = drawSectionBlock(
                canvas, secContent.first, secContent.second, margin, y, contentW,
                accent, textCol, mutedCol, softAccent, design, variant, scale
            ) + spacing
        }

        if (design.showSignature && data.signatureText.isNotBlank()) {
            if (y > A4_HEIGHT - 70f) {
                drawPageFooter(canvas, pageNum, mutedCol)
                pdf.finishPage(page)
                pageNum++
                page = startNewPage(pdf, pageNum, bg, accent, design.borderStyle, variant)
                canvas = page.canvas
                y = margin
            }
            val sigPaint = fontHelper.createTextPaint(10.5f * scale, textCol, design.fontStyle, data.signatureText, true)
            fontHelper.drawTextBlock(canvas, "Signature: ${data.signatureText}", margin, y + 12f, contentW, sigPaint, Layout.Alignment.ALIGN_OPPOSITE)
        }

        drawPageFooter(canvas, pageNum, mutedCol)
        pdf.finishPage(page)
        return pageNum
    }

    private fun startNewPage(
        pdf: PdfDocument,
        pageNum: Int,
        bg: Int,
        accent: Int,
        borderStyle: BorderStyle,
        variant: CvLayoutVariant
    ): PdfDocument.Page {
        val pageInfo = PdfDocument.PageInfo.Builder(A4_WIDTH, A4_HEIGHT, pageNum).create()
        val page = pdf.startPage(pageInfo)
        val c = page.canvas
        c.drawColor(bg)
        val effectiveBorder = if (variant == CvLayoutVariant.EXECUTIVE_BORDERED && borderStyle == BorderStyle.NONE) {
            BorderStyle.DOUBLE_CLASSIC
        } else {
            borderStyle
        }
        drawPageBorder(c, accent, effectiveBorder)
        return page
    }

    private fun drawPageBorder(canvas: Canvas, accent: Int, style: BorderStyle) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accent
            this.style = Paint.Style.STROKE
        }
        when (style) {
            BorderStyle.NONE -> Unit
            BorderStyle.THIN_SOLID -> {
                p.strokeWidth = 1.2f
                canvas.drawRect(18f, 18f, A4_WIDTH - 18f, A4_HEIGHT - 18f, p)
            }
            BorderStyle.DOUBLE_CLASSIC -> {
                p.strokeWidth = 1.6f
                canvas.drawRect(16f, 16f, A4_WIDTH - 16f, A4_HEIGHT - 16f, p)
                p.strokeWidth = 0.7f
                canvas.drawRect(21f, 21f, A4_WIDTH - 21f, A4_HEIGHT - 21f, p)
            }
            BorderStyle.ACCENT_LEFT -> {
                canvas.drawRect(0f, 0f, 10f, A4_HEIGHT.toFloat(), Paint().apply { color = accent })
            }
            BorderStyle.ROUNDED_CARD -> {
                p.strokeWidth = 1.3f
                canvas.drawRoundRect(RectF(18f, 18f, A4_WIDTH - 18f, A4_HEIGHT - 18f), 14f, 14f, p)
            }
        }
    }

    private fun buildSectionLines(
        key: String,
        data: CvDocumentData,
        design: DocumentDesignConfig
    ): Pair<String, List<String>>? {
        return when (key) {
            "OBJECTIVE" -> data.careerObjective.takeIf { it.isNotBlank() }?.let { design.objectiveTitle to listOf(it) }
            "EXPERIENCE" -> data.experience.takeIf { it.isNotEmpty() }?.let { list ->
                design.experienceTitle to list.map { exp ->
                    buildString {
                        append(exp.jobTitle.ifBlank { "Role" })
                        if (exp.companyName.isNotBlank()) append(" — ${exp.companyName}")
                        if (exp.duration.isNotBlank()) append(" (${exp.duration})")
                        if (exp.responsibilities.isNotBlank()) append("\n${exp.responsibilities}")
                    }
                }
            }
            "EDUCATION" -> data.education.takeIf { it.isNotEmpty() }?.let { list ->
                design.educationTitle to list.map { edu ->
                    buildString {
                        append(edu.qualification.ifBlank { "Qualification" })
                        if (edu.institution.isNotBlank()) append(" • ${edu.institution}")
                        val extra = listOf(edu.boardOrUniversity, edu.passingYear, edu.scoreOrCgpa)
                            .filter { it.isNotBlank() }.joinToString(" | ")
                        if (extra.isNotBlank()) append("\n$extra")
                    }
                }
            }
            "SKILLS" -> data.skills.takeIf { it.isNotEmpty() }?.let { design.skillsTitle to listOf(it.joinToString("  •  ")) }
            "PROJECTS" -> data.projects.takeIf { it.isNotEmpty() }?.let { list ->
                design.projectsTitle to list.map { prj ->
                    buildString {
                        append(prj.title.ifBlank { "Project" })
                        if (prj.roleOrTech.isNotBlank()) append(" [${prj.roleOrTech}]")
                        if (prj.description.isNotBlank()) append("\n${prj.description}")
                    }
                }
            }
            "CERTIFICATIONS" -> data.certifications.takeIf { it.isNotEmpty() }?.let { list ->
                design.certificationsTitle to list.map { cert ->
                    listOf(cert.name, cert.issuer, cert.year).filter { it.isNotBlank() }.joinToString(" — ")
                }
            }
            "ACHIEVEMENTS" -> data.achievements.takeIf { it.isNotEmpty() }?.let { list ->
                design.achievementsTitle to list.map { "• $it" }
            }
            "LANGUAGES" -> data.languages.takeIf { it.isNotEmpty() }?.let { design.languagesTitle to listOf(it.joinToString("  •  ")) }
            "HOBBIES" -> data.hobbies.takeIf { it.isNotEmpty() }?.let { design.hobbiesTitle to listOf(it.joinToString("  •  ")) }
            "REFERENCES" -> data.references.takeIf { it.isNotEmpty() }?.let { list ->
                design.referencesTitle to list.map { ref ->
                    listOf(ref.name, ref.designationAndOrg, ref.contact).filter { it.isNotBlank() }.joinToString(" • ")
                }
            }
            else -> null
        }
    }

    private fun estimateSectionHeight(
        title: String,
        items: List<String>,
        width: Float,
        scale: Float,
        design: DocumentDesignConfig
    ): Float {
        val hPaint = fontHelper.createTextPaint(11f * scale, Color.BLACK, design.fontStyle, title, true)
        val bPaint = fontHelper.createTextPaint(9.5f * scale, Color.BLACK, design.fontStyle, items.joinToString(" "), false)
        var total = fontHelper.measureTextHeight(title, hPaint, width) + 12f
        items.forEach { total += fontHelper.measureTextHeight(it, bPaint, width) + 6f }
        return total
    }

    private fun drawSectionBlock(
        canvas: Canvas,
        title: String,
        items: List<String>,
        x: Float,
        startY: Float,
        width: Float,
        accent: Int,
        textCol: Int,
        mutedCol: Int,
        softAccent: Int,
        design: DocumentDesignConfig,
        variant: CvLayoutVariant,
        scale: Float
    ): Float {
        var y = startY
        val isCardStyle = variant == CvLayoutVariant.CARD_SECTIONED
        val isTimeline = variant == CvLayoutVariant.MODERN_TIMELINE

        val titlePaint = fontHelper.createTextPaint(11f * scale, accent, design.fontStyle, title, true)
        if (variant == CvLayoutVariant.MINIMAL_LINE_ACCENT || variant == CvLayoutVariant.SWISS_MINIMAL) {
            y = fontHelper.drawTextBlock(canvas, title.uppercase(), x, y, width, titlePaint) + 3f
            canvas.drawLine(x, y, x + 42f, y, Paint().apply { color = accent; strokeWidth = 2f })
            y += 7f
        } else if (variant == CvLayoutVariant.ACADEMIC_RULED || variant == CvLayoutVariant.ATS_SINGLE_COLUMN) {
            y = fontHelper.drawTextBlock(canvas, title.uppercase(), x, y, width, titlePaint) + 2f
            canvas.drawLine(x, y, x + width, y, Paint().apply { color = accent; strokeWidth = 0.9f })
            y += 6f
        } else {
            val pillRect = RectF(x, y, x + width, y + 18f)
            if (!isCardStyle) {
                canvas.drawRect(RectF(x, y + 1f, x + 3.5f, y + 14f), Paint().apply { color = accent })
                y = fontHelper.drawTextBlock(canvas, title.uppercase(), x + 9f, y, width - 9f, titlePaint) + 5f
            } else {
                canvas.drawRoundRect(pillRect, 4f, 4f, Paint().apply { color = softAccent })
                y = fontHelper.drawTextBlock(canvas, title.uppercase(), x + 8f, y + 2f, width - 16f, titlePaint) + 7f
            }
        }

        val bodyPaint = fontHelper.createTextPaint(9.4f * scale, textCol, design.fontStyle, items.joinToString(" "), false)
        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accent }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = mutedCol; strokeWidth = 0.8f }

        items.forEachIndexed { idx, item ->
            val itemX = if (isTimeline) x + 14f else x
            val itemW = if (isTimeline) width - 14f else width
            val itemStart = y
            y = fontHelper.drawTextBlock(canvas, item, itemX, y, itemW, bodyPaint)
            if (isTimeline) {
                canvas.drawCircle(x + 4f, itemStart + 5f, 2.8f, dotPaint)
                if (idx < items.lastIndex) {
                    canvas.drawLine(x + 4f, itemStart + 8f, x + 4f, y + 5f, linePaint)
                }
            }
            y += 5f
        }
        return y
    }

    private fun drawPageFooter(canvas: Canvas, pageNum: Int, mutedCol: Int) {
        val p = fontHelper.createTextPaint(7.8f, mutedCol, com.example.domain.model.FontStyleOption.PLUS_JAKARTA, "Page", false)
        fontHelper.drawTextBlock(
            canvas,
            "IDVANTA Studio • Page $pageNum",
            38f,
            A4_HEIGHT - 24f,
            A4_WIDTH - 76f,
            p,
            Layout.Alignment.ALIGN_OPPOSITE
        )
    }
}
