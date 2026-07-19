package com.nutritrack.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

enum class ColorTheme(val displayLabel: String) {
    DYNAMIC("Dynamic (Material You)"),
    GREEN("Fresh Greens"),
    BLUE("Ocean Blue"),
    ORANGE("Sunset Orange"),
    PURPLE("Berry Purple"),
    YELLOW("Citrus Yellow"),
    GRAY("Slate Gray"),
    PINK("Rose Pink"),
    TEAL("Teal Mint"),
    RED("Coral Red"),
}

private data class ThemeTriad(val primary: Color, val secondary: Color, val tertiary: Color)

// Only primary/secondary/tertiary are overridden per theme - the same depth of customization the
// app's original placeholder Purple theme used - and every other M3 role (background, surface,
// onPrimary, containers, etc.) falls back to Material3's own baseline defaults for that triad.
private val LIGHT_TRIADS = mapOf(
    ColorTheme.GREEN to ThemeTriad(Color(0xFF2E7D32), Color(0xFF558B2F), Color(0xFF00796B)),
    ColorTheme.BLUE to ThemeTriad(Color(0xFF1565C0), Color(0xFF0277BD), Color(0xFF00838F)),
    ColorTheme.ORANGE to ThemeTriad(Color(0xFFEF6C00), Color(0xFFE65100), Color(0xFFF9A825)),
    ColorTheme.PURPLE to ThemeTriad(Color(0xFF6A1B9A), Color(0xFF8E24AA), Color(0xFFAD1457)),
    ColorTheme.YELLOW to ThemeTriad(Color(0xFFF9A825), Color(0xFFFB8C00), Color(0xFF9E9D24)),
    ColorTheme.GRAY to ThemeTriad(Color(0xFF455A64), Color(0xFF546E7A), Color(0xFF37474F)),
    ColorTheme.PINK to ThemeTriad(Color(0xFFD81B60), Color(0xFFE91E63), Color(0xFFC2185B)),
    ColorTheme.TEAL to ThemeTriad(Color(0xFF00897B), Color(0xFF26A69A), Color(0xFF00ACC1)),
    ColorTheme.RED to ThemeTriad(Color(0xFFD84315), Color(0xFFE64A19), Color(0xFFBF360C)),
)

private val DARK_TRIADS = mapOf(
    ColorTheme.GREEN to ThemeTriad(Color(0xFF81C995), Color(0xFFA5D6A7), Color(0xFF4DB6AC)),
    ColorTheme.BLUE to ThemeTriad(Color(0xFF90CAF9), Color(0xFF81D4FA), Color(0xFF80DEEA)),
    ColorTheme.ORANGE to ThemeTriad(Color(0xFFFFB74D), Color(0xFFFFAB91), Color(0xFFFFD54F)),
    ColorTheme.PURPLE to ThemeTriad(Color(0xFFCE93D8), Color(0xFFE1BEE7), Color(0xFFF48FB1)),
    ColorTheme.YELLOW to ThemeTriad(Color(0xFFFFD95A), Color(0xFFFFCC80), Color(0xFFDCE775)),
    ColorTheme.GRAY to ThemeTriad(Color(0xFFB0BEC5), Color(0xFFCFD8DC), Color(0xFF90A4AE)),
    ColorTheme.PINK to ThemeTriad(Color(0xFFF48FB1), Color(0xFFF8BBD0), Color(0xFFFF8A80)),
    ColorTheme.TEAL to ThemeTriad(Color(0xFF80CBC4), Color(0xFFB2DFDB), Color(0xFF84FFFF)),
    ColorTheme.RED to ThemeTriad(Color(0xFFFFAB91), Color(0xFFFFCCBC), Color(0xFFFF8A65)),
)

// Also doubles as each theme's swatch color in the Settings picker.
val ColorTheme.swatchColor: Color?
    get() = LIGHT_TRIADS[this]?.primary

fun ColorTheme.lightColorSchemeOrNull(): ColorScheme? = LIGHT_TRIADS[this]?.let {
    lightColorScheme(primary = it.primary, secondary = it.secondary, tertiary = it.tertiary)
}

fun ColorTheme.darkColorSchemeOrNull(): ColorScheme? = DARK_TRIADS[this]?.let {
    darkColorScheme(primary = it.primary, secondary = it.secondary, tertiary = it.tertiary)
}
