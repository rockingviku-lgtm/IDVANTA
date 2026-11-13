package com.example.pdf

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.text.Layout
import com.example.domain.model.BiodataData
import com.example.domain.model.BiodataLayoutVariant
import com.example.domain.model.DocumentDesignConfig
import com.example.domain.model.TemplateCatalog
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiodataPdfRenderer @Inject constructor(
    private val fontHelper: PdfFontHelper
) {
    companion object {
        const val A4_W = 595
        const val A4_H = 842
    }

    fun render(pdf: PdfDocument, data: BiodataData, design: DocumentDesignConfig): Int {
        val template = TemplateCatalog.findById(design.templateId)
        val variant = template.biodataLayout ?: BiodataLayoutVariant.BIODATA_CLASSIC
        val accent = fontHelper.parseColorSafe(design.accentColorHex, Color.parseColor("#121A3F"))
        val bg = fontHelper.parseColorSafe(design.backgroundColorHex, Color.WHITE)
        val textCol = fontHelper.parseColorSafe(design.textColorHex, Color.parseColor("#1A1F36"))
        val softAccent = fontHelper.lightenColor(accent, 0.91f)
        val scale = design.fontSizeScale.coerceIn(0.85f, 1.25f)
        val spacing = 12f * design.sectionSpacingScale.coerceIn(0.75f, 1.45f)

        val page = pdf.startPage(PdfDocument.PageInfo.Builder(A4_W, A4_H, 1).create())
        val canvas = page.canvas
        canvas.drawColor(bg)

        // Frame border for classic/heritage styles
        if (variant == BiodataLayoutVariant.BIODATA_CLASSIC || variant == BiodataLayoutVariant.BIODATA_HINDI_HERITAGE) {
            val frameP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = accent
                style = Paint.Style.STROKE
                strokeWidth = 2f
            }
            canvas.drawRect(20f, 20f, A4_W - 20f, A4_H - 20f, frameP)
            frameP.strokeWidth = 0.8f
            canvas.drawRect(25f, 25f, A4_W - 25f, A4_H - 25f, frameP)
        }

        val margin = 42f
        val contentW = A4_W - margin * 2
        var y = margin

        val headerTitle = if (variant == BiodataLayoutVariant.BIODATA_HINDI_HERITAGE || fontHelper.containsDevanagari(data.name)) {
            "व्यक्तिगत विवरण / PERSONAL BIODATA"
        } else {
            "PERSONAL BIODATA"
        }

        if (variant == BiodataLayoutVariant.BIODATA_ROYAL) {
            canvas.drawRect(0f, 0f, A4_W.toFloat(), 92f, Paint().apply { color = accent })
            val hP = fontHelper.createTextPaint(10f * scale, Color.parseColor("#D6CCFF"), design.fontStyle, headerTitle, true)
            val nP = fontHelper.createTextPaint(20f * scale, Color.WHITE, design.fontStyle, data.name, true)
            fontHelper.drawTextBlock(canvas, headerTitle, margin, 22f, contentW, hP)
            fontHelper.drawTextBlock(canvas, data.name.ifBlank { "Full Name" }, margin, 40f, contentW - 80f, nP)
            y = 108f
        } else {
            val hP = fontHelper.createTextPaint(11f * scale, accent, design.fontStyle, headerTitle, true)
            val nP = fontHelper.createTextPaint(20f * scale, textCol, design.fontStyle, data.name, true)
            y = fontHelper.drawTextBlock(canvas, headerTitle, margin, y, contentW, hP, Layout.Alignment.ALIGN_CENTER) + 4f
            y = fontHelper.drawTextBlock(canvas, data.name.ifBlank { "Full Name" }, margin, y, contentW, nP, Layout.Alignment.ALIGN_CENTER) + 8f
            canvas.drawLine(margin, y, margin + contentW, y, Paint().apply { color = accent; strokeWidth = 1.5f })
            y += 14f
        }

        val photoBmp = if (design.showProfilePhoto && data.photoUri.isNotBlank()) {
            fontHelper.loadBitmapFromUriOrPath(data.photoUri, 400)
        } else null
        if (photoBmp != null) {
            val pTop = if (variant == BiodataLayoutVariant.BIODATA_ROYAL) 16f else y
            val pRect = RectF(A4_W - margin - 76f, pTop, A4_W - margin, pTop + 92f)
            fontHelper.drawCenterCroppedBitmap(canvas, photoBmp, pRect, 8f)
        }

        val personalRows = listOfNotNull(
            "Name" to data.name.ifBlank { "—" },
            data.dateOfBirth.takeIf { it.isNotBlank() }?.let { "Date of Birth" to it },
            data.gender.takeIf { it.isNotBlank() }?.let { "Gender" to it },
            data.maritalStatus.takeIf { it.isNotBlank() }?.let { "Marital Status" to it },
            data.nationality.takeIf { it.isNotBlank() }?.let { "Nationality" to it },
            data.phone.takeIf { it.isNotBlank() }?.let { "Contact Number" to it },
            data.email.takeIf { it.isNotBlank() }?.let { "Email" to it },
            data.address.takeIf { it.isNotBlank() }?.let { "Address" to it }
        )

        val personalW = if (photoBmp != null && variant != BiodataLayoutVariant.BIODATA_ROYAL) contentW - 92f else contentW
        y = drawKeyValueSection(canvas, "PERSONAL DETAILS", personalRows, margin, y, personalW, accent, textCol, softAccent, design, scale) + spacing

        if (data.introduction.isNotBlank()) {
            y = drawParagraphSection(canvas, "ABOUT ME", data.introduction, margin, y, contentW, accent, textCol, design, scale) + spacing
        }
        if (data.educationSummary.isNotBlank() || data.occupation.isNotBlank()) {
            val eduRows = listOfNotNull(
                data.educationSummary.takeIf { it.isNotBlank() }?.let { "Education" to it },
                data.occupation.takeIf { it.isNotBlank() }?.let { "Occupation" to it }
            )
            y = drawKeyValueSection(canvas, "EDUCATION & CAREER", eduRows, margin, y, contentW, accent, textCol, softAccent, design, scale) + spacing
        }

        val familyRows = listOfNotNull(
            data.fatherName.takeIf { it.isNotBlank() }?.let {
                "Father's Name" to (it + if (data.fatherOccupation.isNotBlank()) " (${data.fatherOccupation})" else "")
            },
            data.motherName.takeIf { it.isNotBlank() }?.let {
                "Mother's Name" to (it + if (data.motherOccupation.isNotBlank()) " (${data.motherOccupation})" else "")
            },
            data.siblingsInfo.takeIf { it.isNotBlank() }?.let { "Siblings" to it },
            data.familyAddress.takeIf { it.isNotBlank() }?.let { "Family Residence" to it }
        )
        if (familyRows.isNotEmpty()) {
            y = drawKeyValueSection(canvas, "FAMILY BACKGROUND", familyRows, margin, y, contentW, accent, textCol, softAccent, design, scale) + spacing
        }

        val extraRows = listOfNotNull(
            data.languages.takeIf { it.isNotEmpty() }?.let { "Languages Known" to it.joinToString(", ") },
            data.skills.takeIf { it.isNotEmpty() }?.let { "Core Skills" to it.joinToString(", ") },
            data.hobbies.takeIf { it.isNotEmpty() }?.let { "Hobbies" to it.joinToString(", ") }
        )
        if (extraRows.isNotEmpty()) {
            y = drawKeyValueSection(canvas, "SKILLS & LANGUAGES", extraRows, margin, y, contentW, accent, textCol, softAccent, design, scale) + spacing
        }

        if (data.declarationText.isNotBlank() && y < A4_H - 64f) {
            val declP = fontHelper.createTextPaint(8.8f * scale, Color.DKGRAY, design.fontStyle, data.declarationText, false)
            fontHelper.drawTextBlock(canvas, "Declaration: ${data.declarationText}", margin, y + 6f, contentW, declP)
        }

        pdf.finishPage(page)
        return 1
    }

    private fun drawKeyValueSection(
        canvas: Canvas,
        title: String,
        rows: List<Pair<String, String>>,
        x: Float,
        startY: Float,
        width: Float,
        accent: Int,
        textCol: Int,
        softAccent: Int,
        design: DocumentDesignConfig,
        scale: Float
    ): Float {
        var y = startY
        canvas.drawRoundRect(RectF(x, y, x + width, y + 18f), 4f, 4f, Paint().apply { color = softAccent })
        val tP = fontHelper.createTextPaint(9.8f * scale, accent, design.fontStyle, title, true)
        y = fontHelper.drawTextBlock(canvas, title, x + 8f, y + 3f, width - 16f, tP) + 8f

        val labelW = minOf(128f, width * 0.36f)
        val valX = x + labelW + 8f
        val valW = width - labelW - 8f

        rows.forEach { (k, v) ->
            val kP = fontHelper.createTextPaint(9.2f * scale, accent, design.fontStyle, k, true)
            val vP = fontHelper.createTextPaint(9.2f * scale, textCol, design.fontStyle, v, false)
            val y1 = fontHelper.drawTextBlock(canvas, "$k:", x + 4f, y, labelW - 4f, kP)
            val y2 = fontHelper.drawTextBlock(canvas, v, valX, y, valW, vP)
            y = maxOf(y1, y2) + 5f
        }
        return y
    }

    private fun drawParagraphSection(
        canvas: Canvas,
        title: String,
        body: String,
        x: Float,
        startY: Float,
        width: Float,
        accent: Int,
        textCol: Int,
        design: DocumentDesignConfig,
        scale: Float
    ): Float {
        var y = startY
        val tP = fontHelper.createTextPaint(10f * scale, accent, design.fontStyle, title, true)
        y = fontHelper.drawTextBlock(canvas, title, x, y, width, tP) + 4f
        val bP = fontHelper.createTextPaint(9.3f * scale, textCol, design.fontStyle, body, false)
        y = fontHelper.drawTextBlock(canvas, body, x, y, width, bP)
        return y
    }
}
