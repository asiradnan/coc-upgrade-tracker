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
 * Clash font families loaded from res/font/clash_regular.ttf and res/font/clash_bold.ttf.
 */
val ClashRegular = FontFamily(
    Font(R.font.clash_regular, FontWeight.Normal)
)

val ClashBold = FontFamily(
    Font(R.font.clash_bold, FontWeight.Bold)
)

/**
 * Unified Clash family that maps Normal & Medium to clash_regular,
 * and SemiBold, Bold, ExtraBold, Black to clash_bold.
 */
val ClashFontFamily = FontFamily(
    Font(R.font.clash_regular, FontWeight.Normal),
    Font(R.font.clash_regular, FontWeight.Medium),
    Font(R.font.clash_bold, FontWeight.SemiBold),
    Font(R.font.clash_bold, FontWeight.Bold),
    Font(R.font.clash_bold, FontWeight.ExtraBold),
    Font(R.font.clash_bold, FontWeight.Black)
)

/** Display tier: chunky "Clash" style font used for titles, buttons, labels and numbers. */
val ClashDisplayFontFamily = ClashFontFamily

/** Body tier: plain readable font for captions, descriptions and long text. */
val ClashBodyFontFamily = FontFamily.SansSerif

private fun displayStyle(
    fontSize: Int,
    lineHeight: Int,
    fontWeight: FontWeight = FontWeight.Bold,
    letterSpacing: Double = 0.3
) = TextStyle(
    fontFamily = ClashFontFamily,
    fontWeight = fontWeight,
    fontSynthesis = FontSynthesis.None,
    fontSize = fontSize.sp,
    lineHeight = lineHeight.sp,
    // A little extra tracking leaves room for the black outline between letters
    letterSpacing = letterSpacing.sp
)

val Typography = Typography(
    // ---- Display tier (Clash font) ----
    headlineSmall = displayStyle(fontSize = 22, lineHeight = 29, fontWeight = FontWeight.Bold),
    titleLarge = displayStyle(fontSize = 19, lineHeight = 26, fontWeight = FontWeight.Bold),
    titleMedium = displayStyle(fontSize = 16, lineHeight = 22, fontWeight = FontWeight.Bold),
    titleSmall = displayStyle(fontSize = 14, lineHeight = 19, fontWeight = FontWeight.Bold),
    labelLarge = displayStyle(fontSize = 14, lineHeight = 19, fontWeight = FontWeight.Bold),
    labelMedium = displayStyle(fontSize = 12, lineHeight = 16, fontWeight = FontWeight.Normal, letterSpacing = 0.4),
    labelSmall = displayStyle(fontSize = 11, lineHeight = 15, fontWeight = FontWeight.Normal, letterSpacing = 0.4),

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
