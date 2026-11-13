package com.example.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.domain.model.FontStyleOption
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfFontHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val typefaceCache = mutableMapOf<FontStyleOption, Typeface>()

    fun containsDevanagari(text: String): Boolean {
        return text.any { ch -> ch in '\u0900'..'\u097F' }
    }

    fun getTypeface(option: FontStyleOption, textSample: String = "", bold: Boolean = false): Typeface {
        val effectiveOption = if (containsDevanagari(textSample)) FontStyleOption.DEVANAGARI else option
        val base = typefaceCache.getOrPut(effectiveOption) {
            val resId = when (effectiveOption) {
                FontStyleOption.PLUS_JAKARTA -> R.font.plus_jakarta_sans
                FontStyleOption.PLAYFAIR -> R.font.playfair_display
                FontStyleOption.POPPINS -> R.font.poppins
                FontStyleOption.MERRIWEATHER -> R.font.merriweather
                FontStyleOption.ROBOTO_MONO -> R.font.roboto_mono
                FontStyleOption.DEVANAGARI -> R.font.noto_sans_devanagari
            }
            runCatching { ResourcesCompat.getFont(context, resId) }.getOrNull() ?: Typeface.SANS_SERIF
        }
        return if (bold) Typeface.create(base, Typeface.BOLD) else base
    }

    fun parseColorSafe(hex: String, fallback: Int = Color.parseColor("#121A3F")): Int {
        return runCatching { Color.parseColor(hex) }.getOrDefault(fallback)
    }

    fun lightenColor(color: Int, factor: Float = 0.88f): Int {
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        val nr = (r + (255 - r) * factor).toInt().coerceIn(0, 255)
        val ng = (g + (255 - g) * factor).toInt().coerceIn(0, 255)
        val nb = (b + (255 - b) * factor).toInt().coerceIn(0, 255)
        return Color.rgb(nr, ng, nb)
    }

    fun createTextPaint(
        sizePt: Float,
        color: Int,
        fontOption: FontStyleOption,
        sampleText: String = "",
        bold: Boolean = false
    ): TextPaint {
        return TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            textSize = sizePt
            this.color = color
            typeface = getTypeface(fontOption, sampleText, bold)
        }
    }

    fun buildStaticLayout(
        text: String,
        paint: TextPaint,
        widthPx: Int,
        align: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL,
        lineSpacingMult: Float = 1.18f
    ): StaticLayout {
        val safeWidth = widthPx.coerceAtLeast(16)
        return StaticLayout.Builder.obtain(text, 0, text.length, paint, safeWidth)
            .setAlignment(align)
            .setLineSpacing(0f, lineSpacingMult)
            .setIncludePad(false)
            .build()
    }

    fun drawTextBlock(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        width: Float,
        paint: TextPaint,
        align: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL,
        lineSpacingMult: Float = 1.18f
    ): Float {
        if (text.isBlank()) return y
        val layout = buildStaticLayout(text, paint, width.toInt(), align, lineSpacingMult)
        canvas.save()
        canvas.translate(x, y)
        layout.draw(canvas)
        canvas.restore()
        return y + layout.height
    }

    fun measureTextHeight(
        text: String,
        paint: TextPaint,
        width: Float,
        lineSpacingMult: Float = 1.18f
    ): Float {
        if (text.isBlank()) return 0f
        return buildStaticLayout(text, paint, width.toInt(), Layout.Alignment.ALIGN_NORMAL, lineSpacingMult)
            .height.toFloat()
    }

    fun loadBitmapFromUriOrPath(uriOrPath: String, maxDim: Int = 600): Bitmap? {
        if (uriOrPath.isBlank()) return null
        return runCatching {
            val raw = if (uriOrPath.startsWith("content://") || uriOrPath.startsWith("file://")) {
                context.contentResolver.openInputStream(Uri.parse(uriOrPath))?.use {
                    BitmapFactory.decodeStream(it)
                }
            } else {
                val f = File(uriOrPath)
                if (f.exists()) BitmapFactory.decodeFile(f.absolutePath) else null
            } ?: return null

            val scale = minOf(maxDim.toFloat() / raw.width, maxDim.toFloat() / raw.height, 1f)
            if (scale < 1f) {
                Bitmap.createScaledBitmap(
                    raw,
                    (raw.width * scale).toInt().coerceAtLeast(1),
                    (raw.height * scale).toInt().coerceAtLeast(1),
                    true
                )
            } else {
                raw
            }
        }.getOrNull()
    }

    fun loadBundledCrestBitmap(): Bitmap? {
        return runCatching {
            BitmapFactory.decodeResource(context.resources, R.drawable.img_institution_crest)
        }.getOrNull()
    }

    fun drawCenterCroppedBitmap(canvas: Canvas, bitmap: Bitmap, dest: RectF, radius: Float = 0f) {
        val srcRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
        val dstRatio = dest.width() / dest.height()
        val srcRect = if (srcRatio > dstRatio) {
            val targetW = (bitmap.height * dstRatio).toInt()
            val left = ((bitmap.width - targetW) / 2).coerceAtLeast(0)
            android.graphics.Rect(left, 0, left + targetW, bitmap.height)
        } else {
            val targetH = (bitmap.width / dstRatio).toInt()
            val top = ((bitmap.height - targetH) / 2).coerceAtLeast(0)
            android.graphics.Rect(0, top, bitmap.width, top + targetH)
        }
        canvas.save()
        if (radius > 0f) {
            val path = android.graphics.Path().apply {
                addRoundRect(dest, radius, radius, android.graphics.Path.Direction.CW)
            }
            canvas.clipPath(path)
        }
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(bitmap, srcRect, dest, paint)
        canvas.restore()
    }
}
