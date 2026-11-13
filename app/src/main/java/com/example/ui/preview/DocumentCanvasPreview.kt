package com.example.ui.preview

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.domain.model.BiodataData
import com.example.domain.model.BorderStyle
import com.example.domain.model.CvDocumentData
import com.example.domain.model.CvLayoutVariant
import com.example.domain.model.DocumentDesignConfig
import com.example.domain.model.FontStyleOption
import com.example.domain.model.IdCardData
import com.example.domain.model.IdCardType
import com.example.domain.model.IdLayoutVariant
import com.example.domain.model.TemplateCatalog
import com.example.pdf.IdCardPdfRenderer
import com.example.pdf.QrCodeGenerator
import com.example.ui.components.parseHexColor
import com.example.ui.theme.DevanagariFontFamily
import com.example.ui.theme.MerriweatherFamily
import com.example.ui.theme.PlayfairDisplayFamily
import com.example.ui.theme.PlusJakartaSansFamily
import com.example.ui.theme.PoppinsFamily
import com.example.ui.theme.RobotoMonoFamily
import java.io.File

fun resolveComposeFontFamily(option: FontStyleOption, sample: String = ""): FontFamily {
    if (sample.any { it in '\u0900'..'\u097F' }) return DevanagariFontFamily
    return when (option) {
        FontStyleOption.PLUS_JAKARTA -> PlusJakartaSansFamily
        FontStyleOption.PLAYFAIR -> PlayfairDisplayFamily
        FontStyleOption.POPPINS -> PoppinsFamily
        FontStyleOption.MERRIWEATHER -> MerriweatherFamily
        FontStyleOption.ROBOTO_MONO -> RobotoMonoFamily
        FontStyleOption.DEVANAGARI -> DevanagariFontFamily
    }
}

@Composable
fun CvLivePreviewCard(
    data: CvDocumentData,
    design: DocumentDesignConfig,
    modifier: Modifier = Modifier
) {
    val template = TemplateCatalog.findById(design.templateId)
    val variant = template.cvLayout ?: CvLayoutVariant.EXECUTIVE_BANNER
    val accent = parseHexColor(design.accentColorHex, Color(0xFF121A3F))
    val bg = parseHexColor(design.backgroundColorHex, Color.White)
    val textCol = parseHexColor(design.textColorHex, Color(0xFF1A1F36))
    val fontFamily = resolveComposeFontFamily(design.fontStyle, data.fullName + data.careerObjective)
    val scale = design.fontSizeScale.coerceIn(0.85f, 1.25f)

    val borderStroke = when (design.borderStyle) {
        BorderStyle.NONE -> BorderStroke(1.dp, Color(0xFFE2E8F0))
        BorderStyle.THIN_SOLID -> BorderStroke(1.5.dp, accent)
        BorderStyle.DOUBLE_CLASSIC -> BorderStroke(3.dp, accent)
        BorderStyle.ACCENT_LEFT -> BorderStroke(1.dp, accent.copy(alpha = 0.4f))
        BorderStyle.ROUNDED_CARD -> BorderStroke(2.dp, accent)
    }

    val isTwoColLeft = variant in listOf(
        CvLayoutVariant.TWO_COLUMN_SIDEBAR_LEFT,
        CvLayoutVariant.SIDEBAR_DARK_NAVY,
        CvLayoutVariant.COMPACT_TWO_PANE
    )
    val isTwoColRight = variant == CvLayoutVariant.TWO_COLUMN_SIDEBAR_RIGHT
    val hasBanner = variant in listOf(
        CvLayoutVariant.EXECUTIVE_BANNER,
        CvLayoutVariant.BOLD_HEADER_BLOCK,
        CvLayoutVariant.COLOR_BLOCK_TOP,
        CvLayoutVariant.PRESTIGE_CREST
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(595f / 842f),
        color = bg,
        shape = RoundedCornerShape(if (design.borderStyle == BorderStyle.ROUNDED_CARD) 14.dp else 6.dp),
        border = borderStroke,
        shadowElevation = 4.dp
    ) {
        if (isTwoColLeft || isTwoColRight) {
            val isDarkSidebar = variant == CvLayoutVariant.SIDEBAR_DARK_NAVY
            val sideBg = if (isDarkSidebar) accent else accent.copy(alpha = 0.09f)
            val sideText = if (isDarkSidebar) Color.White else textCol
            val sideTitle = if (isDarkSidebar) Color(0xFFD6CCFF) else accent

            Row(modifier = Modifier.fillMaxSize()) {
                val sidebar: @Composable () -> Unit = {
                    Column(
                        modifier = Modifier
                            .weight(0.34f)
                            .fillMaxHeight()
                            .background(sideBg)
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (design.showProfilePhoto && data.photoUri.isNotBlank()) {
                            val model: Any = if (data.photoUri.startsWith("/")) File(data.photoUri) else data.photoUri
                            AsyncImage(
                                model = model,
                                contentDescription = "Profile photograph",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .align(Alignment.CenterHorizontally)
                            )
                        }
                        Text(
                            text = "CONTACT",
                            fontFamily = fontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = (8.5f * scale).sp,
                            color = sideTitle
                        )
                        listOf(data.phone, data.email, data.address, data.linkedInUrl)
                            .filter { it.isNotBlank() }
                            .forEach {
                                Text(
                                    text = it,
                                    fontFamily = fontFamily,
                                    fontSize = (7.5f * scale).sp,
                                    color = sideText
                                )
                            }
                        if (data.skills.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = design.skillsTitle.uppercase(),
                                fontFamily = fontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = (8.5f * scale).sp,
                                color = sideTitle
                            )
                            Text(
                                text = data.skills.joinToString("\n• ", prefix = "• "),
                                fontFamily = fontFamily,
                                fontSize = (7.5f * scale).sp,
                                color = sideText
                            )
                        }
                        if (data.languages.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = design.languagesTitle.uppercase(),
                                fontFamily = fontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = (8.5f * scale).sp,
                                color = sideTitle
                            )
                            Text(
                                text = data.languages.joinToString(", "),
                                fontFamily = fontFamily,
                                fontSize = (7.5f * scale).sp,
                                color = sideText
                            )
                        }
                    }
                }
                val mainPane: @Composable () -> Unit = {
                    Column(
                        modifier = Modifier
                            .weight(0.66f)
                            .fillMaxHeight()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = data.fullName.ifBlank { "Your Full Name" },
                            fontFamily = fontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = (15f * scale).sp,
                            color = accent
                        )
                        if (data.professionalHeadline.isNotBlank()) {
                            Text(
                                text = data.professionalHeadline,
                                fontFamily = fontFamily,
                                fontSize = (9f * scale).sp,
                                color = textCol.copy(alpha = 0.72f)
                            )
                        }
                        HorizontalDivider(color = accent, thickness = 1.dp)
                        CvSectionsPreviewList(data, design, accent, textCol, fontFamily, scale, excludeSide = true)
                    }
                }
                if (isTwoColLeft) {
                    sidebar()
                    mainPane()
                } else {
                    mainPane()
                    sidebar()
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                if (hasBanner) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(accent)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = data.fullName.ifBlank { "Your Full Name" },
                                fontFamily = fontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = (15.5f * scale).sp,
                                color = Color.White
                            )
                            if (data.professionalHeadline.isNotBlank()) {
                                Text(
                                    text = data.professionalHeadline,
                                    fontFamily = fontFamily,
                                    fontSize = (9f * scale).sp,
                                    color = Color(0xFFE6E2FF)
                                )
                            }
                            val contact = listOf(data.phone, data.email, data.address).filter { it.isNotBlank() }.joinToString(" • ")
                            if (contact.isNotBlank()) {
                                Text(
                                    text = contact,
                                    fontFamily = fontFamily,
                                    fontSize = (7.5f * scale).sp,
                                    color = Color(0xFFD6CCFF)
                                )
                            }
                        }
                        if (design.showProfilePhoto && data.photoUri.isNotBlank()) {
                            val model: Any = if (data.photoUri.startsWith("/")) File(data.photoUri) else data.photoUri
                            AsyncImage(
                                model = model,
                                contentDescription = "Profile photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalAlignment = if (variant == CvLayoutVariant.CLASSIC_SERIF_DOUBLE) Alignment.CenterHorizontally else Alignment.Start
                    ) {
                        Text(
                            text = data.fullName.ifBlank { "Your Full Name" },
                            fontFamily = fontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = (15.5f * scale).sp,
                            color = accent
                        )
                        if (data.professionalHeadline.isNotBlank()) {
                            Text(
                                text = data.professionalHeadline,
                                fontFamily = fontFamily,
                                fontSize = (9f * scale).sp,
                                color = textCol.copy(alpha = 0.72f)
                            )
                        }
                        val contact = listOf(data.phone, data.email, data.address).filter { it.isNotBlank() }.joinToString(" | ")
                        if (contact.isNotBlank()) {
                            Text(
                                text = contact,
                                fontFamily = fontFamily,
                                fontSize = (7.5f * scale).sp,
                                color = textCol.copy(alpha = 0.85f)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        HorizontalDivider(color = accent, thickness = 1.2.dp)
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    CvSectionsPreviewList(data, design, accent, textCol, fontFamily, scale, excludeSide = false)
                }
            }
        }
    }
}

@Composable
private fun CvSectionsPreviewList(
    data: CvDocumentData,
    design: DocumentDesignConfig,
    accent: Color,
    textCol: Color,
    fontFamily: FontFamily,
    scale: Float,
    excludeSide: Boolean
) {
    val sections = if (excludeSide) {
        design.sectionOrder.filterNot { it in listOf("SKILLS", "LANGUAGES", "HOBBIES") }
    } else {
        design.sectionOrder
    }
    sections.forEach { key ->
        val content: Pair<String, String>? = when (key) {
            "OBJECTIVE" -> data.careerObjective.takeIf { it.isNotBlank() }?.let { design.objectiveTitle to it }
            "EXPERIENCE" -> data.experience.takeIf { it.isNotEmpty() }?.let { list ->
                design.experienceTitle to list.joinToString("\n") { "${it.jobTitle} — ${it.companyName} (${it.duration})" }
            }
            "EDUCATION" -> data.education.takeIf { it.isNotEmpty() }?.let { list ->
                design.educationTitle to list.joinToString("\n") { "${it.qualification} • ${it.institution} (${it.passingYear})" }
            }
            "SKILLS" -> data.skills.takeIf { it.isNotEmpty() }?.let { design.skillsTitle to it.joinToString(" • ") }
            "PROJECTS" -> data.projects.takeIf { it.isNotEmpty() }?.let { list ->
                design.projectsTitle to list.joinToString("\n") { "${it.title}: ${it.description}" }
            }
            "CERTIFICATIONS" -> data.certifications.takeIf { it.isNotEmpty() }?.let { list ->
                design.certificationsTitle to list.joinToString("\n") { "${it.name} — ${it.issuer}" }
            }
            "ACHIEVEMENTS" -> data.achievements.takeIf { it.isNotEmpty() }?.let { design.achievementsTitle to it.joinToString("\n• ", prefix = "• ") }
            "LANGUAGES" -> data.languages.takeIf { it.isNotEmpty() }?.let { design.languagesTitle to it.joinToString(" • ") }
            "HOBBIES" -> data.hobbies.takeIf { it.isNotEmpty() }?.let { design.hobbiesTitle to it.joinToString(" • ") }
            "REFERENCES" -> data.references.takeIf { it.isNotEmpty() }?.let { list ->
                design.referencesTitle to list.joinToString("\n") { "${it.name} (${it.designationAndOrg})" }
            }
            else -> null
        }
        if (content != null) {
            Text(
                text = content.first.uppercase(),
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = (8.5f * scale).sp,
                color = accent
            )
            Text(
                text = content.second,
                fontFamily = fontFamily,
                fontSize = (7.8f * scale).sp,
                lineHeight = (10.5f * scale).sp,
                color = textCol,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun IdCardLivePreview(
    data: IdCardData,
    design: DocumentDesignConfig,
    showBackSide: Boolean,
    modifier: Modifier = Modifier
) {
    val template = TemplateCatalog.findById(design.templateId)
    val accent = parseHexColor(design.accentColorHex, Color(0xFF121A3F))
    val bg = parseHexColor(design.backgroundColorHex, Color.White)
    val textCol = parseHexColor(design.textColorHex, Color(0xFF121A3F))
    val fontFamily = resolveComposeFontFamily(design.fontStyle, data.personName + data.institutionName)
    val isPortrait = template.isPortraitCard
    val ratio = if (isPortrait) 53.98f / 85.6f else 85.6f / 53.98f

    val qrBitmap: Bitmap? = remember(data, design.showQrCode) {
        if (!design.showQrCode) null else runCatching {
            val payload = listOfNotNull(
                "IDVANTA",
                data.cardType.name,
                data.institutionName.takeIf { it.isNotBlank() },
                data.personName.takeIf { it.isNotBlank() },
                data.admissionNumber.takeIf { it.isNotBlank() },
                data.registrationNumber.takeIf { it.isNotBlank() },
                data.employeeNumber.takeIf { it.isNotBlank() }
            ).joinToString("|")
            QrCodeGenerator().generateQrBitmap(payload, 160)
        }.getOrNull()
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(ratio),
        color = bg,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.5.dp, accent),
        shadowElevation = 6.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (!showBackSide) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(accent)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (data.logoUri.isNotBlank()) {
                            val model: Any = if (data.logoUri.startsWith("/")) File(data.logoUri) else data.logoUri
                            AsyncImage(
                                model = model,
                                contentDescription = "Institution logo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        } else if (data.useBundledCrest) {
                            Image(
                                painter = painterResource(id = R.drawable.img_institution_crest),
                                contentDescription = "Institution crest",
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = data.institutionName.ifBlank { "INSTITUTION NAME" }.uppercase(),
                                fontFamily = fontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = when (data.cardType) {
                                    IdCardType.SCHOOL -> "STUDENT IDENTITY CARD"
                                    IdCardType.COLLEGE -> "COLLEGE STUDENT ID"
                                    IdCardType.EMPLOYEE -> "EMPLOYEE IDENTITY PASS"
                                    IdCardType.TEACHER -> "FACULTY / TEACHER ID"
                                },
                                fontFamily = fontFamily,
                                fontSize = 8.5.sp,
                                color = Color(0xFFD6CCFF)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 62.dp, height = 76.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFEDEFF7))
                                .border(1.dp, accent, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (design.showProfilePhoto && data.photoUri.isNotBlank()) {
                                val model: Any = if (data.photoUri.startsWith("/")) File(data.photoUri) else data.photoUri
                                AsyncImage(
                                    model = model,
                                    contentDescription = "Card holder photograph",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = "PHOTO",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = data.personName.ifBlank { "Card Holder Name" },
                                fontFamily = fontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = accent,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val rows = when (data.cardType) {
                                IdCardType.SCHOOL -> listOfNotNull(
                                    data.className.takeIf { it.isNotBlank() }?.let { "Class: $it ${data.section}".trim() },
                                    data.rollNumber.takeIf { it.isNotBlank() }?.let { "Roll No: $it" },
                                    data.admissionNumber.takeIf { it.isNotBlank() }?.let { "Adm No: $it" },
                                    data.fatherName.takeIf { it.isNotBlank() }?.let { "Father: $it" },
                                    data.emergencyContact.takeIf { it.isNotBlank() }?.let { "Emergency: $it" }
                                )
                                IdCardType.COLLEGE -> listOfNotNull(
                                    data.course.takeIf { it.isNotBlank() }?.let { "Course: $it" },
                                    data.department.takeIf { it.isNotBlank() }?.let { "Dept: $it" },
                                    data.registrationNumber.takeIf { it.isNotBlank() }?.let { "Reg No: $it" },
                                    data.contactNumber.takeIf { it.isNotBlank() }?.let { "Phone: $it" }
                                )
                                IdCardType.EMPLOYEE -> listOfNotNull(
                                    data.designation.takeIf { it.isNotBlank() }?.let { "Role: $it" },
                                    data.department.takeIf { it.isNotBlank() }?.let { "Dept: $it" },
                                    data.employeeNumber.takeIf { it.isNotBlank() }?.let { "ID: $it" },
                                    data.contactNumber.takeIf { it.isNotBlank() }?.let { "Phone: $it" }
                                )
                                IdCardType.TEACHER -> listOfNotNull(
                                    data.designation.takeIf { it.isNotBlank() }?.let { "Designation: $it" },
                                    data.department.takeIf { it.isNotBlank() }?.let { "Dept: $it" },
                                    data.qualification.takeIf { it.isNotBlank() }?.let { "Qual: $it" },
                                    data.employeeNumber.takeIf { it.isNotBlank() }?.let { "Emp No: $it" }
                                )
                            }
                            rows.take(5).forEach { line ->
                                Text(
                                    text = line,
                                    fontFamily = fontFamily,
                                    fontSize = 9.5.sp,
                                    color = textCol,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        if (qrBitmap != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Image(
                                bitmap = qrBitmap.asImageBitmap(),
                                contentDescription = "ID card QR code",
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(accent.copy(alpha = 0.1f))
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        val footer = listOfNotNull(
                            data.academicSession.takeIf { it.isNotBlank() }?.let { "Session: $it" },
                            data.validityDate.takeIf { it.isNotBlank() }?.let { "Valid: $it" },
                            data.bloodGroup.takeIf { it.isNotBlank() }?.let { "Blood: $it" }
                        ).joinToString("  •  ").ifBlank { data.authorityTitle }
                        Text(
                            text = footer,
                            fontFamily = fontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 8.5.sp,
                            color = accent,
                            maxLines = 1
                        )
                    }
                }
            } else {
                // Back side preview
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(accent)
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = "IDENTITY & EMERGENCY DETAILS",
                            fontFamily = fontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = Color.White
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        listOfNotNull(
                            data.fatherName.takeIf { it.isNotBlank() }?.let { "Father's Name: $it" },
                            data.motherName.takeIf { it.isNotBlank() }?.let { "Mother's Name: $it" },
                            data.dateOfBirth.takeIf { it.isNotBlank() }?.let { "Date of Birth: $it" },
                            data.joiningDate.takeIf { it.isNotBlank() }?.let { "Joining Date: $it" },
                            data.emergencyContact.takeIf { it.isNotBlank() }?.let { "Emergency: $it" },
                            data.address.takeIf { it.isNotBlank() }?.let { "Address: $it" }
                        ).forEach {
                            Text(text = it, fontFamily = fontFamily, fontSize = 9.5.sp, color = textCol)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = data.backInstructions,
                            fontFamily = fontFamily,
                            fontSize = 8.sp,
                            color = textCol.copy(alpha = 0.7f),
                            maxLines = 2
                        )
                        Text(
                            text = "Authorized Sign: ${data.authorityTitle}",
                            fontFamily = fontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 8.5.sp,
                            color = accent,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }

            if (!design.institutionAuthorized) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SAMPLE",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0x55B91C1C),
                        modifier = Modifier.rotate(-22f)
                    )
                }
            }
        }
    }
}

@Composable
fun BiodataLivePreviewCard(
    data: BiodataData,
    design: DocumentDesignConfig,
    modifier: Modifier = Modifier
) {
    val accent = parseHexColor(design.accentColorHex, Color(0xFF121A3F))
    val bg = parseHexColor(design.backgroundColorHex, Color.White)
    val textCol = parseHexColor(design.textColorHex, Color(0xFF1A1F36))
    val fontFamily = resolveComposeFontFamily(design.fontStyle, data.name + data.introduction)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(595f / 842f),
        color = bg,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(2.dp, accent),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "PERSONAL BIODATA",
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = accent,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = data.name.ifBlank { "Full Name" },
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = textCol,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            HorizontalDivider(color = accent, thickness = 1.2.dp)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    listOfNotNull(
                        data.dateOfBirth.takeIf { it.isNotBlank() }?.let { "DOB: $it" },
                        data.phone.takeIf { it.isNotBlank() }?.let { "Phone: $it" },
                        data.email.takeIf { it.isNotBlank() }?.let { "Email: $it" },
                        data.address.takeIf { it.isNotBlank() }?.let { "Address: $it" }
                    ).forEach {
                        Text(text = it, fontFamily = fontFamily, fontSize = 8.5.sp, color = textCol)
                    }
                }
                if (design.showProfilePhoto && data.photoUri.isNotBlank()) {
                    val model: Any = if (data.photoUri.startsWith("/")) File(data.photoUri) else data.photoUri
                    AsyncImage(
                        model = model,
                        contentDescription = "Biodata portrait",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            }

            if (data.introduction.isNotBlank()) {
                Text(text = "ABOUT", fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 9.sp, color = accent)
                Text(text = data.introduction, fontFamily = fontFamily, fontSize = 8.sp, color = textCol, maxLines = 3)
            }
            if (data.educationSummary.isNotBlank()) {
                Text(text = "EDUCATION", fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 9.sp, color = accent)
                Text(text = data.educationSummary, fontFamily = fontFamily, fontSize = 8.sp, color = textCol, maxLines = 3)
            }
            val family = listOfNotNull(
                data.fatherName.takeIf { it.isNotBlank() }?.let { "Father: $it" },
                data.motherName.takeIf { it.isNotBlank() }?.let { "Mother: $it" },
                data.siblingsInfo.takeIf { it.isNotBlank() }?.let { "Siblings: $it" }
            ).joinToString("  •  ")
            if (family.isNotBlank()) {
                Text(text = "FAMILY DETAILS", fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 9.sp, color = accent)
                Text(text = family, fontFamily = fontFamily, fontSize = 8.sp, color = textCol)
            }
            if (data.languages.isNotEmpty() || data.skills.isNotEmpty()) {
                Text(text = "SKILLS & LANGUAGES", fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 9.sp, color = accent)
                Text(
                    text = (data.skills + data.languages).joinToString(" • "),
                    fontFamily = fontFamily,
                    fontSize = 8.sp,
                    color = textCol
                )
            }
        }
    }
}
