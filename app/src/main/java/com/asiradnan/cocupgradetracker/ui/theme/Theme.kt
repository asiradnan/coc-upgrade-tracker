package com.asiradnan.cocupgradetracker.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val ClashSettingsColorScheme = lightColorScheme(
    primary = ClashGold,
    onPrimary = OnClashGold,
    primaryContainer = ClashGoldContainer,
    onPrimaryContainer = OnClashGoldContainer,
    secondary = ElixirPurple,
    onSecondary = OnElixirPurple,
    secondaryContainer = ElixirContainer,
    onSecondaryContainer = OnElixirContainer,
    tertiary = ClashGreen,
    onTertiary = OnClashGreen,
    tertiaryContainer = ClashGreenContainer,
    onTertiaryContainer = OnClashGreenContainer,
    error = ClashRed,
    onError = OnClashRed,
    errorContainer = ClashRedContainer,
    onErrorContainer = OnClashRedContainer,
    background = SlateBackground,
    onBackground = OnSlateBackground,
    surface = CreamSurface,
    onSurface = EspressoText,
    surfaceVariant = CreamSurfaceVariant,
    onSurfaceVariant = TaupeText,
    surfaceContainerLowest = SlateSurfaceLowest,
    surfaceContainerLow = CreamSurfaceLow,
    surfaceContainer = CreamSurface,
    surfaceContainerHigh = CreamSurfaceVariant,
    surfaceContainerHighest = CreamSurfaceHighest,
    outline = StoneOutline,
    outlineVariant = SandOutlineVariant
)

@Composable
fun COCUpgradeTrackerTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            @Suppress("DEPRECATION")
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            @Suppress("DEPRECATION")
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            val insetsController = WindowCompat.getInsetsController(window, view)
            // Keep light status/nav bar icons against the dark-slate outer backdrop
            insetsController.isAppearanceLightStatusBars = false
            insetsController.isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = ClashSettingsColorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
