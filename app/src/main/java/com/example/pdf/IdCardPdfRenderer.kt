package com.example.pdf

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.text.Layout
import com.example.data.local.StudentRecordEntity
import com.example.domain.model.DocumentDesignConfig
import com.example.domain.model.IdCardData
import com.example.domain.model.IdCardType
import com.example.domain.model.IdLayoutVariant
import com.example.domain.model.TemplateCatalog
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IdCardPdfRenderer @Inject constructor(
    private val fontHelper: PdfFontHelper,
    private val qrCodeGenerator: QrCodeGenerator
) {
    companion object {
        // Standard CR80 (85.6mm x 53.98mm) in 72dpi PostScript points
        const val CR80_W = 243
        const val CR80_H = 153
        const val A4_W = 595
        const val A4_H = 842
    }

    fun renderStandaloneCr80(pdf: PdfDocument, data: IdCardData, design: DocumentDesignConfig): Int {
        val template = TemplateCatalog.findById(design.templateId)
        val portrait = template.isPortraitCard
        val w = if (portrait) CR80_H else CR80_W
        val h = if (portrait) CR80_W else CR80_H

        val p1 = pdf.startPage(PdfDocument.PageInfo.Builder(w, h, 1).create())
        drawCardFront(p1.canvas, RectF(0f, 0f, w.toFloat(), h.toFloat()), data, design)
        pdf.finishPage(p1)

        val p2 = pdf.startPage(PdfDocument.PageInfo.Builder(w, h, 2).create())
        drawCardBack(p2.canvas, RectF(0f, 0f, w.toFloat(), h.toFloat()), data, design)
        pdf.finishPage(p2)
        return 2
    }

    fun renderSingleA4Sheet(pdf: PdfDocument, data: IdCardData, design: DocumentDesignConfig): Int {
        val template = TemplateCatalog.findById(design.templateId)
        val portrait = template.isPortraitCard
        val cardW = if (portrait) CR80_H.toFloat() else CR80_W.toFloat()
        val cardH = if (portrait) CR80_W.toFloat() else CR80_H.toFloat()

        val page = pdf.startPage(PdfDocument.PageInfo.Builder(A4_W, A4_H, 1).create())
        val c = page.canvas
        c.drawColor(Color.WHITE)

        val headerP = fontHelper.createTextPaint(11f, Color.parseColor("#121A3F"), design.fontStyle, "CR80", true)
        fontHelper.drawTextBlock(c, "IDVANTA Print-Ready Sheet (Standard CR80: 85.6 × 53.98 mm)", 40f, 34f, A4_W - 80f, headerP)

        val frontX = 48f
        val frontY = 78f
        val frontRect = RectF(frontX, frontY, frontX + cardW, frontY + cardH)
        drawCardFront(c, frontRect, data, design)
        drawCutMarks(c, frontRect)

        val backX = if (portrait) frontX + cardW + 34f else frontX
        val backY = if (portrait) frontY else frontY + cardH + 38f
        val backRect = RectF(backX, backY, backX + cardW, backY + cardH)
        drawCardBack(c, backRect, data, design)
        drawCutMarks(c, backRect)

        val labelP = fontHelper.createTextPaint(8f, Color.GRAY, design.fontStyle, "Front", false)
        fontHelper.drawTextBlock(c, "FRONT SIDE", frontX, frontY - 13f, cardW, labelP)
        fontHelper.drawTextBlock(c, "BACK SIDE", backX, backY - 13f, cardW, labelP)

        pdf.finishPage(page)
        return 1
    }

    fun renderBulkStudentsA4(
        pdf: PdfDocument,
        students: List<StudentRecordEntity>,
        baseData: IdCardData,
        design: DocumentDesignConfig,
        includeBackSheet: Boolean
    ): Int {
        val cardW = CR80_W.toFloat()
        val cardH = CR80_H.toFloat()
        val marginX = 40f
        val marginY = 48f
        val gapX = 28f
        val gapY = 26f
        val perPage = 8 // 2 cols x 4 rows

        var pageNum = 0
        val chunks = students.chunked(perPage)
        chunks.forEach { batch ->
            pageNum++
            val page = pdf.startPage(PdfDocument.PageInfo.Builder(A4_W, A4_H, pageNum).create())
            val c = page.canvas
            c.drawColor(Color.WHITE)

            val titleP = fontHelper.createTextPaint(9.5f, Color.parseColor("#121A3F"), design.fontStyle, "Bulk", true)
            fontHelper.drawTextBlock(
                c,
                "${baseData.institutionName.ifBlank { "Student ID Batch" }} — Front Sides (Page $pageNum)",
                marginX, 22f, A4_W - marginX * 2, titleP
            )

            batch.forEachIndexed { idx, student ->
                val col = idx % 2
                val row = idx / 2
                val left = marginX + col * (cardW + gapX)
                val top = marginY + row * (cardH + gapY)
                val rect = RectF(left, top, left + cardW, top + cardH)

                val cardData = baseData.copy(
                    cardType = IdCardType.SCHOOL,
                    institutionName = student.schoolName.ifBlank { baseData.institutionName },
                    personName = student.studentName,
                    fatherName = student.fatherName,
                    motherName = student.motherName,
                    className = student.className,
                    section = student.section,
                    rollNumber = student.rollNumber,
                    admissionNumber = student.admissionNumber,
                    dateOfBirth = student.dateOfBirth,
                    address = student.address,
                    emergencyContact = student.emergencyContact,
                    bloodGroup = student.bloodGroup,
                    academicSession = student.academicSession,
                    validityDate = student.validityDate,
                    photoUri = student.photoUri.ifBlank { baseData.photoUri },
                    logoUri = student.logoUri.ifBlank { baseData.logoUri }
                )
                drawCardFront(c, rect, cardData, design)
                drawCutMarks(c, rect)
            }
            pdf.finishPage(page)

            if (includeBackSheet) {
                pageNum++
                val backPage = pdf.startPage(PdfDocument.PageInfo.Builder(A4_W, A4_H, pageNum).create())
                val bc = backPage.canvas
                bc.drawColor(Color.WHITE)
                fontHelper.drawTextBlock(
                    bc,
                    "${baseData.institutionName.ifBlank { "Student ID Batch" }} — Back Sides (Page $pageNum)",
                    marginX, 22f, A4_W - marginX * 2, titleP
                )
                batch.forEachIndexed { idx, student ->
                    val col = idx % 2
                    val row = idx / 2
                    val left = marginX + col * (cardW + gapX)
                    val top = marginY + row * (cardH + gapY)
                    val rect = RectF(left, top, left + cardW, top + cardH)
                    val cardData = baseData.copy(
                        cardType = IdCardType.SCHOOL,
                        institutionName = student.schoolName.ifBlank { baseData.institutionName },
                        personName = student.studentName,
                        fatherName = student.fatherName,
                        motherName = student.motherName,
                        rollNumber = student.rollNumber,
                        admissionNumber = student.admissionNumber,
                        address = student.address,
                        emergencyContact = student.emergencyContact,
                        bloodGroup = student.bloodGroup,
                        validityDate = student.validityDate
                    )
                    drawCardBack(bc, rect, cardData, design)
                    drawCutMarks(bc, rect)
                }
                pdf.finishPage(backPage)
            }
        }
        return pageNum
    }

    fun drawCardFront(canvas: Canvas, bounds: RectF, data: IdCardData, design: DocumentDesignConfig) {
        val template = TemplateCatalog.findById(design.templateId)
        val variant = template.idLayout ?: IdLayoutVariant.CLASSIC_NAVY_CREST
        val accent = fontHelper.parseColorSafe(design.accentColorHex, Color.parseColor("#121A3F"))
        val bg = fontHelper.parseColorSafe(design.backgroundColorHex, Color.WHITE)
        val textCol = fontHelper.parseColorSafe(design.textColorHex, Color.parseColor("#121A3F"))
        val scale = design.fontSizeScale.coerceIn(0.82f, 1.2f)

        canvas.save()
        val clipPath = android.graphics.Path().apply {
            addRoundRect(bounds, 7f, 7f, android.graphics.Path.Direction.CW)
        }
        canvas.clipPath(clipPath)
        canvas.drawRect(bounds, Paint().apply { color = bg })

        val isPortrait = bounds.height() > bounds.width()
        val headerH = if (isPortrait) 44f else 34f
        val leftSpine = variant == IdLayoutVariant.CORPORATE_LEFT_BAR

        if (leftSpine) {
            canvas.drawRect(bounds.left, bounds.top, bounds.left + 16f, bounds.bottom, Paint().apply { color = accent })
            canvas.drawRect(bounds.left + 16f, bounds.top, bounds.right, bounds.top + 26f, Paint().apply { color = fontHelper.lightenColor(accent, 0.88f) })
        } else {
            canvas.drawRect(bounds.left, bounds.top, bounds.right, bounds.top + headerH, Paint().apply { color = accent })
            if (variant == IdLayoutVariant.ACADEMIC_GOLD_SEAL || variant == IdLayoutVariant.TEACHER_PRESTIGE) {
                canvas.drawRect(bounds.left, bounds.top + headerH, bounds.right, bounds.top + headerH + 2.5f, Paint().apply { color = Color.parseColor("#C8A24A") })
            }
        }

        // Logo
        val logoBmp = if (data.logoUri.isNotBlank()) {
            fontHelper.loadBitmapFromUriOrPath(data.logoUri, 200)
        } else if (data.useBundledCrest) {
            fontHelper.loadBundledCrestBitmap()
        } else null

        val headerTextColor = if (leftSpine) accent else Color.WHITE
        val instName = data.institutionName.ifBlank {
            when (data.cardType) {
                IdCardType.SCHOOL -> "UNITY PUBLIC SCHOOL"
                IdCardType.COLLEGE -> "UNITY INSTITUTE OF TECHNOLOGY"
                IdCardType.EMPLOYEE -> "UNITY ENTERPRISES"
                IdCardType.TEACHER -> "UNITY SENIOR ACADEMY"
            }
        }
        val instX = bounds.left + (if (leftSpine) 22f else 8f) + (if (logoBmp != null) 26f else 0f)
        val instW = bounds.right - instX - 8f

        if (logoBmp != null) {
            val lSize = if (leftSpine) 20f else 24f
            val lLeft = bounds.left + (if (leftSpine) 20f else 7f)
            val lTop = bounds.top + 4f
            fontHelper.drawCenterCroppedBitmap(canvas, logoBmp, RectF(lLeft, lTop, lLeft + lSize, lTop + lSize), 4f)
        }

        val instPaint = fontHelper.createTextPaint(7.6f * scale, headerTextColor, design.fontStyle, instName, true)
        fontHelper.drawTextBlock(canvas, instName.uppercase(), instX, bounds.top + 6f, instW, instPaint)

        val roleTag = when (data.cardType) {
            IdCardType.SCHOOL -> "STUDENT IDENTITY CARD"
            IdCardType.COLLEGE -> "COLLEGE STUDENT ID"
            IdCardType.EMPLOYEE -> "EMPLOYEE IDENTITY PASS"
            IdCardType.TEACHER -> "FACULTY / TEACHER ID"
        }
        val tagColor = if (leftSpine) textCol else Color.parseColor("#D6CCFF")
        val tagPaint = fontHelper.createTextPaint(5.8f * scale, tagColor, design.fontStyle, roleTag, true)
        fontHelper.drawTextBlock(canvas, roleTag, instX, bounds.top + 18f, instW, tagPaint)

        // Body layout
        val bodyTop = bounds.top + (if (leftSpine) 32f else headerH + 8f)
        val bodyLeft = bounds.left + (if (leftSpine) 22f else 9f)
        val photoBmp = if (data.photoUri.isNotBlank()) fontHelper.loadBitmapFromUriOrPath(data.photoUri, 260) else null

        if (isPortrait) {
            val photoSize = 54f
            val photoRect = RectF(
                bounds.centerX() - photoSize / 2f,
                bodyTop,
                bounds.centerX() + photoSize / 2f,
                bodyTop + photoSize
            )
            drawPhotoFrame(canvas, photoRect, photoBmp, accent)
            var cy = photoRect.bottom + 6f
            val nameP = fontHelper.createTextPaint(8.8f * scale, accent, design.fontStyle, data.personName, true)
            cy = fontHelper.drawTextBlock(canvas, data.personName.ifBlank { "Card Holder Name" }, bounds.left + 8f, cy, bounds.width() - 16f, nameP, Layout.Alignment.ALIGN_CENTER) + 4f
            val detailP = fontHelper.createTextPaint(6.3f * scale, textCol, design.fontStyle, data.personName + data.address, false)
            frontDetails(data).take(5).forEach { line ->
                cy = fontHelper.drawTextBlock(canvas, line, bounds.left + 10f, cy, bounds.width() - 20f, detailP, Layout.Alignment.ALIGN_CENTER) + 2f
            }
            if (design.showQrCode) {
                val qrBmp = qrCodeGenerator.generateQrBitmap(buildQrPayload(data), 140)
                val qrSize = 34f
                canvas.drawBitmap(qrBmp, null, RectF(bounds.centerX() - qrSize / 2f, bounds.bottom - qrSize - 14f, bounds.centerX() + qrSize / 2f, bounds.bottom - 14f), null)
            }
        } else {
            val photoW = 46f
            val photoH = 56f
            val photoRect = RectF(bodyLeft, bodyTop, bodyLeft + photoW, bodyTop + photoH)
            drawPhotoFrame(canvas, photoRect, photoBmp, accent)

            val infoX = photoRect.right + 8f
            val qrSpace = if (design.showQrCode) 40f else 6f
            val infoW = bounds.right - infoX - qrSpace

            var iy = bodyTop - 1f
            val nameP = fontHelper.createTextPaint(8.6f * scale, accent, design.fontStyle, data.personName, true)
            iy = fontHelper.drawTextBlock(canvas, data.personName.ifBlank { "Full Name" }, infoX, iy, infoW, nameP) + 3f

            val detailP = fontHelper.createTextPaint(6.2f * scale, textCol, design.fontStyle, data.personName + data.address, false)
            frontDetails(data).take(6).forEach { line ->
                if (iy < bounds.bottom - 20f) {
                    iy = fontHelper.drawTextBlock(canvas, line, infoX, iy, infoW, detailP) + 1.8f
                }
            }

            if (design.showQrCode) {
                val qrBmp = qrCodeGenerator.generateQrBitmap(buildQrPayload(data), 160)
                val qrSize = 33f
                val qrRect = RectF(bounds.right - qrSize - 7f, bounds.bottom - qrSize - 17f, bounds.right - 7f, bounds.bottom - 17f)
                canvas.drawBitmap(qrBmp, null, qrRect, null)
            }
        }

        // Bottom strip
        val footerH = 13f
        canvas.drawRect(bounds.left, bounds.bottom - footerH, bounds.right, bounds.bottom, Paint().apply { color = fontHelper.lightenColor(accent, 0.88f) })
        val footText = listOfNotNull(
            data.academicSession.takeIf { it.isNotBlank() }?.let { "Session: $it" },
            data.validityDate.takeIf { it.isNotBlank() }?.let { "Valid: $it" },
            data.bloodGroup.takeIf { it.isNotBlank() }?.let { "Blood: $it" }
        ).joinToString("  •  ").ifBlank { data.authorityTitle }
        val footP = fontHelper.createTextPaint(5.4f * scale, accent, design.fontStyle, footText, true)
        fontHelper.drawTextBlock(canvas, footText, bounds.left + 8f, bounds.bottom - 10f, bounds.width() - 16f, footP)

        if (!design.institutionAuthorized) {
            drawSampleWatermark(canvas, bounds)
        }
        canvas.restore()

        // Outer border
        canvas.drawRoundRect(bounds, 7f, 7f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accent
            style = Paint.Style.STROKE
            strokeWidth = 0.9f
        })
    }

    fun drawCardBack(canvas: Canvas, bounds: RectF, data: IdCardData, design: DocumentDesignConfig) {
        val accent = fontHelper.parseColorSafe(design.accentColorHex, Color.parseColor("#121A3F"))
        val bg = fontHelper.parseColorSafe(design.backgroundColorHex, Color.WHITE)
        val textCol = fontHelper.parseColorSafe(design.textColorHex, Color.parseColor("#121A3F"))
        val scale = design.fontSizeScale.coerceIn(0.82f, 1.2f)

        canvas.save()
        val clipPath = android.graphics.Path().apply {
            addRoundRect(bounds, 7f, 7f, android.graphics.Path.Direction.CW)
        }
        canvas.clipPath(clipPath)
        canvas.drawRect(bounds, Paint().apply { color = bg })

        canvas.drawRect(bounds.left, bounds.top, bounds.right, bounds.top + 22f, Paint().apply { color = accent })
        val hP = fontHelper.createTextPaint(6.8f * scale, Color.WHITE, design.fontStyle, "INSTRUCTIONS", true)
        fontHelper.drawTextBlock(canvas, "IDENTITY & EMERGENCY DETAILS", bounds.left + 8f, bounds.top + 7f, bounds.width() - 16f, hP)

        var y = bounds.top + 28f
        val bodyP = fontHelper.createTextPaint(6.2f * scale, textCol, design.fontStyle, data.address + data.backInstructions, false)
        val backLines = buildList {
            if (data.fatherName.isNotBlank()) add("Father's Name: ${data.fatherName}")
            if (data.motherName.isNotBlank()) add("Mother's Name: ${data.motherName}")
            if (data.dateOfBirth.isNotBlank()) add("Date of Birth: ${data.dateOfBirth}")
            if (data.joiningDate.isNotBlank()) add("Joining Date: ${data.joiningDate}")
            if (data.emergencyContact.isNotBlank()) add("Emergency Contact: ${data.emergencyContact}")
            if (data.address.isNotBlank()) add("Address: ${data.address}")
        }
        backLines.forEach { line ->
            if (y < bounds.bottom - 38f) {
                y = fontHelper.drawTextBlock(canvas, line, bounds.left + 9f, y, bounds.width() - 18f, bodyP) + 2f
            }
        }

        val instrP = fontHelper.createTextPaint(5.5f * scale, Color.DKGRAY, design.fontStyle, data.backInstructions, false)
        fontHelper.drawTextBlock(canvas, data.backInstructions, bounds.left + 9f, bounds.bottom - 36f, bounds.width() - 70f, instrP)

        val sigP = fontHelper.createTextPaint(5.6f * scale, accent, design.fontStyle, data.authorityTitle, true)
        fontHelper.drawTextBlock(canvas, "Authorized Sign\n${data.authorityTitle}", bounds.right - 66f, bounds.bottom - 24f, 58f, sigP, Layout.Alignment.ALIGN_OPPOSITE)

        if (!design.institutionAuthorized) {
            drawSampleWatermark(canvas, bounds)
        }
        canvas.restore()

        canvas.drawRoundRect(bounds, 7f, 7f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accent
            style = Paint.Style.STROKE
            strokeWidth = 0.9f
        })
    }

    private fun drawPhotoFrame(canvas: Canvas, rect: RectF, bitmap: android.graphics.Bitmap?, accent: Int) {
        canvas.drawRoundRect(rect, 4f, 4f, Paint().apply { color = Color.parseColor("#EEF0F8") })
        if (bitmap != null) {
            fontHelper.drawCenterCroppedBitmap(canvas, bitmap, rect, 4f)
        } else {
            val p = fontHelper.createTextPaint(5.5f, Color.GRAY, com.example.domain.model.FontStyleOption.PLUS_JAKARTA, "PHOTO", true)
            fontHelper.drawTextBlock(canvas, "PHOTO", rect.left + 4f, rect.centerY() - 4f, rect.width() - 8f, p, Layout.Alignment.ALIGN_CENTER)
        }
        canvas.drawRoundRect(rect, 4f, 4f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accent
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
        })
    }

    private fun drawSampleWatermark(canvas: Canvas, bounds: RectF) {
        canvas.save()
        canvas.rotate(-24f, bounds.centerX(), bounds.centerY())
        val wmPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(65, 190, 30, 50)
            textSize = 20f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("SAMPLE", bounds.centerX(), bounds.centerY() + 6f, wmPaint)
        canvas.restore()
    }

    private fun drawCutMarks(canvas: Canvas, rect: RectF) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#94A3B8")
            strokeWidth = 0.5f
            style = Paint.Style.STROKE
            pathEffect = DashPathEffect(floatArrayOf(3f, 3f), 0f)
        }
        canvas.drawRect(rect.left - 3f, rect.top - 3f, rect.right + 3f, rect.bottom + 3f, p)
    }

    private fun frontDetails(data: IdCardData): List<String> {
        return when (data.cardType) {
            IdCardType.SCHOOL -> listOfNotNull(
                data.className.takeIf { it.isNotBlank() }?.let {
                    "Class: $it" + (if (data.section.isNotBlank()) " (${data.section})" else "")
                },
                data.rollNumber.takeIf { it.isNotBlank() }?.let { "Roll No: $it" },
                data.admissionNumber.takeIf { it.isNotBlank() }?.let { "Adm No: $it" },
                data.fatherName.takeIf { it.isNotBlank() }?.let { "Father: $it" },
                data.dateOfBirth.takeIf { it.isNotBlank() }?.let { "DOB: $it" },
                data.emergencyContact.takeIf { it.isNotBlank() }?.let { "Emergency: $it" }
            )
            IdCardType.COLLEGE -> listOfNotNull(
                data.course.takeIf { it.isNotBlank() }?.let { "Course: $it" },
                data.department.takeIf { it.isNotBlank() }?.let { "Dept: $it" },
                data.registrationNumber.takeIf { it.isNotBlank() }?.let { "Reg No: $it" },
                data.academicSession.takeIf { it.isNotBlank() }?.let { "Year: $it" },
                data.contactNumber.takeIf { it.isNotBlank() }?.let { "Phone: $it" }
            )
            IdCardType.EMPLOYEE -> listOfNotNull(
                data.designation.takeIf { it.isNotBlank() }?.let { "Role: $it" },
                data.department.takeIf { it.isNotBlank() }?.let { "Dept: $it" },
                data.employeeNumber.takeIf { it.isNotBlank() }?.let { "Emp ID: $it" },
                data.joiningDate.takeIf { it.isNotBlank() }?.let { "Joined: $it" },
                data.contactNumber.takeIf { it.isNotBlank() }?.let { "Phone: $it" },
                data.emergencyContact.takeIf { it.isNotBlank() }?.let { "Emergency: $it" }
            )
            IdCardType.TEACHER -> listOfNotNull(
                data.designation.takeIf { it.isNotBlank() }?.let { "Designation: $it" },
                data.department.takeIf { it.isNotBlank() }?.let { "Dept: $it" },
                data.qualification.takeIf { it.isNotBlank() }?.let { "Qual: $it" },
                data.employeeNumber.takeIf { it.isNotBlank() }?.let { "Emp No: $it" },
                data.contactNumber.takeIf { it.isNotBlank() }?.let { "Contact: $it" }
            )
        }
    }

    fun buildQrPayload(data: IdCardData): String {
        return listOfNotNull(
            "IDVANTA",
            data.cardType.name,
            data.institutionName.takeIf { it.isNotBlank() },
            data.personName.takeIf { it.isNotBlank() },
            data.admissionNumber.takeIf { it.isNotBlank() },
            data.registrationNumber.takeIf { it.isNotBlank() },
            data.employeeNumber.takeIf { it.isNotBlank() },
            data.rollNumber.takeIf { it.isNotBlank() }?.let { "R:$it" }
        ).joinToString("|")
    }
}
