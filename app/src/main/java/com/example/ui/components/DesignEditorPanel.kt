package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.domain.model.BorderStyle
import com.example.domain.model.DocumentDesignConfig
import com.example.domain.model.DocumentTemplate
import com.example.domain.model.FontStyleOption

private val AccentSwatches = listOf(
    "#121A3F", "#6D4AFF", "#0F4C81", "#0D7377",
    "#4A154B", "#7B1E3C", "#1B4332", "#27272A"
)

private val BackgroundSwatches = listOf(
    "#FFFFFF", "#FAF9FE", "#FFFDF9", "#F8FAFC", "#FFFBF5", "#F5F7FB"
)

private val TextSwatches = listOf(
    "#1A1F36", "#111827", "#121A3F", "#241922", "#0F172A"
)

fun parseHexColor(hex: String, fallback: Color = Color(0xFF121A3F)): Color {
    return runCatching {
        Color(android.graphics.Color.parseColor(hex))
    }.getOrDefault(fallback)
}

@Composable
fun DesignEditorPanel(
    design: DocumentDesignConfig,
    templates: List<DocumentTemplate>,
    showSectionReorder: Boolean = true,
    showIdCardControls: Boolean = false,
    onDesignChanged: (DocumentDesignConfig) -> Unit,
    onTemplateSelected: (DocumentTemplate) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Template Style (${templates.size} layouts)",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            templates.forEach { tpl ->
                val selected = tpl.id == design.templateId
                val tplAccent = parseHexColor(tpl.accentHex)
                Surface(
                    onClick = { onTemplateSelected(tpl) },
                    modifier = Modifier
                        .width(156.dp)
                        .minimumInteractiveComponentSize()
                        .testTag("template_pick_${tpl.id}"),
                    shape = RoundedCornerShape(14.dp),
                    color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        width = if (selected) 2.dp else 1.dp,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(tplAccent)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = tpl.styleCategory.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = tpl.name,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1
                        )
                        Text(
                            text = tpl.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                }
            }
        }

        ColorSwatchPicker(
            title = "Accent Color",
            swatches = AccentSwatches,
            selectedHex = design.accentColorHex,
            onSelect = { onDesignChanged(design.copy(accentColorHex = it)) }
        )

        ColorSwatchPicker(
            title = "Page Background",
            swatches = BackgroundSwatches,
            selectedHex = design.backgroundColorHex,
            onSelect = { onDesignChanged(design.copy(backgroundColorHex = it)) }
        )

        ColorSwatchPicker(
            title = "Body Text Color",
            swatches = TextSwatches,
            selectedHex = design.textColorHex,
            onSelect = { onDesignChanged(design.copy(textColorHex = it)) }
        )

        Text(
            text = "Typography & Font Family (Unicode / हिन्दी)",
            style = MaterialTheme.typography.titleMedium
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FontStyleOption.entries.forEach { font ->
                FilterChip(
                    selected = design.fontStyle == font,
                    onClick = { onDesignChanged(design.copy(fontStyle = font)) },
                    label = { Text(font.displayName) },
                    modifier = Modifier.minimumInteractiveComponentSize()
                )
            }
        }

        Column {
            Text(
                text = "Font Size Scale: ${(design.fontSizeScale * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium
            )
            Slider(
                value = design.fontSizeScale,
                onValueChange = { onDesignChanged(design.copy(fontSizeScale = it)) },
                valueRange = 0.85f..1.25f
            )
            Text(
                text = "Section Spacing: ${(design.sectionSpacingScale * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium
            )
            Slider(
                value = design.sectionSpacingScale,
                onValueChange = { onDesignChanged(design.copy(sectionSpacingScale = it)) },
                valueRange = 0.75f..1.4f
            )
        }

        Text(
            text = "Border & Framing",
            style = MaterialTheme.typography.titleMedium
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BorderStyle.entries.forEach { style ->
                FilterChip(
                    selected = design.borderStyle == style,
                    onClick = { onDesignChanged(design.copy(borderStyle = style)) },
                    label = { Text(style.displayName) },
                    modifier = Modifier.minimumInteractiveComponentSize()
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Show Profile Photo", style = MaterialTheme.typography.bodyLarge)
            Switch(
                checked = design.showProfilePhoto,
                onCheckedChange = { onDesignChanged(design.copy(showProfilePhoto = it)) }
            )
        }

        if (showIdCardControls) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Include QR Verification Code", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = design.showQrCode,
                    onCheckedChange = { onDesignChanged(design.copy(showQrCode = it)) }
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Institution-Authorized Official Card", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = if (design.institutionAuthorized) {
                            "Official mode: SAMPLE watermark removed"
                        } else {
                            "Demo mode: Visible SAMPLE watermark applied"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = design.institutionAuthorized,
                    onCheckedChange = { onDesignChanged(design.copy(institutionAuthorized = it)) }
                )
            }
        }

        if (showSectionReorder) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Show Signature Line", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = design.showSignature,
                    onCheckedChange = { onDesignChanged(design.copy(showSignature = it)) }
                )
            }

            Text(
                text = "Editable Section Titles",
                style = MaterialTheme.typography.titleMedium
            )
            OutlinedTextField(
                value = design.objectiveTitle,
                onValueChange = { onDesignChanged(design.copy(objectiveTitle = it)) },
                label = { Text("Summary / Objective Label") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = design.experienceTitle,
                onValueChange = { onDesignChanged(design.copy(experienceTitle = it)) },
                label = { Text("Experience Label") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = design.educationTitle,
                onValueChange = { onDesignChanged(design.copy(educationTitle = it)) },
                label = { Text("Education Label") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = design.skillsTitle,
                onValueChange = { onDesignChanged(design.copy(skillsTitle = it)) },
                label = { Text("Skills Label") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Reorder CV Sections",
                style = MaterialTheme.typography.titleMedium
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                design.sectionOrder.forEachIndexed { index, secKey ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${index + 1}. $secKey",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    if (index > 0) {
                                        val mutable = design.sectionOrder.toMutableList()
                                        val item = mutable.removeAt(index)
                                        mutable.add(index - 1, item)
                                        onDesignChanged(design.copy(sectionOrder = mutable))
                                    }
                                },
                                enabled = index > 0,
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(Icons.Filled.ArrowUpward, contentDescription = "Move $secKey up")
                            }
                            IconButton(
                                onClick = {
                                    if (index < design.sectionOrder.lastIndex) {
                                        val mutable = design.sectionOrder.toMutableList()
                                        val item = mutable.removeAt(index)
                                        mutable.add(index + 1, item)
                                        onDesignChanged(design.copy(sectionOrder = mutable))
                                    }
                                },
                                enabled = index < design.sectionOrder.lastIndex,
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(Icons.Filled.ArrowDownward, contentDescription = "Move $secKey down")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorSwatchPicker(
    title: String,
    swatches: List<String>,
    selectedHex: String,
    onSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            swatches.forEach { hex ->
                val color = parseHexColor(hex)
                val isSelected = selectedHex.equals(hex, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            shape = CircleShape
                        )
                        .clickable { onSelect(hex) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        val checkTint = if (hex.equals("#FFFFFF", true) || hex.equals("#FAF9FE", true) || hex.equals("#FFFDF9", true) || hex.equals("#F8FAFC", true) || hex.equals("#FFFBF5", true) || hex.equals("#F5F7FB", true)) {
                            Color.Black
                        } else {
                            Color.White
                        }
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Selected $hex",
                            tint = checkTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
