package com.asiradnan.cocupgradetracker.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.asiradnan.cocupgradetracker.R

/**
 * Display tier: chunky "Clash" style font used for titles, buttons, labels and numbers.
 * Titan One (SIL Open Font License) is a round, bubbly, licence-safe stand-in for Supercell-Magic.
 *
 * It only ships a Regular weight, so font synthesis is disabled: any
 * `.copy(fontWeight = FontWeight.Black)` keeps the real glyphs instead of a blurry fake bold.
 */
val ClashDisplayFontFamily = FontFamily(
    Font(R.font.titan_one, FontWeight.Normal)
)

/** Body tier: plain readable font for captions, descriptions and long text. */
val ClashBodyFontFamily = FontFamily.SansSerif

private fun displayStyle(
    fontSize: Int,
    lineHeight: Int,
    letterSpacing: Double = 0.3
) = TextStyle(
    fontFamily = ClashDisplayFontFamily,
    fontWeight = FontWeight.Normal,
    fontSynthesis = FontSynthesis.None,
    fontSize = fontSize.sp,
    lineHeight = lineHeight.sp,
    // A little extra tracking leaves room for the black outline between letters
    letterSpacing = letterSpacing.sp
)

val Typography = Typography(
    // ---- Display tier (Titan One: wide glyphs, so sizes run ~1sp smaller) ----
    headlineSmall = displayStyle(fontSize = 22, lineHeight = 29),
    titleLarge = displayStyle(fontSize = 19, lineHeight = 26),
    titleMedium = displayStyle(fontSize = 16, lineHeight = 22),
    titleSmall = displayStyle(fontSize = 14, lineHeight = 19),
    labelLarge = displayStyle(fontSize = 14, lineHeight = 19),
    labelMedium = displayStyle(fontSize = 12, lineHeight = 16, letterSpacing = 0.4),
    labelSmall = displayStyle(fontSize = 11, lineHeight = 15, letterSpacing = 0.4),

    // ---- Body tier (system sans) ----
    bodyLarge = TextStyle(
        fontFamily = ClashBodyFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = ClashBodyFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 19.sp,
        letterSpacing = 0.sp,
        fontFeatureSettings = "tnum"
    ),
    bodySmall = TextStyle(
        fontFamily = ClashBodyFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.1.sp,
        fontFeatureSettings = "tnum"
    )
)
