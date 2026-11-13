package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

private val IdvantaLightColorScheme = lightColorScheme(
    primary = RoyalPurple,
    onPrimary = Color.White,
    primaryContainer = SoftLavender,
    onPrimaryContainer = DeepNavy,
    secondary = DeepNavy,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCE1F5),
    onSecondaryContainer = DeepNavyDark,
    tertiary = AccentGold,
    onTertiary = DeepNavyDark,
    tertiaryContainer = Color(0xFFFBF1DA),
    onTertiaryContainer = Color(0xFF473307),
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    error = CrimsonError,
    onError = Color.White
)

private val IdvantaDarkColorScheme = darkColorScheme(
    primary = Color(0xFFA28BFF),
    onPrimary = DeepNavyDark,
    primaryContainer = Color(0xFF362778),
    onPrimaryContainer = SoftLavender,
    secondary = Color(0xFFB5C1F0),
    onSecondary = DeepNavyDark,
    secondaryContainer = Color(0xFF232C54),
    onSecondaryContainer = Color(0xFFDCE1F5),
    tertiary = Color(0xFFE6C36E),
    onTertiary = Color(0xFF2B1E02),
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    error = Color(0xFFFF7A85),
    onError = Color(0xFF3A040B)
)

val IdvantaShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun IdvantaTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> IdvantaDarkColorScheme
        else -> IdvantaLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = IdvantaTypography,
        shapes = IdvantaShapes,
        content = content
    )
}
