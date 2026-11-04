package com.asiradnan.cocupgradetracker.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val ClashDarkColorScheme = darkColorScheme(
    primary = ClashGold,
    onPrimary = OnClashGold,
    primaryContainer = ClashGoldContainer,
    onPrimaryContainer = ClashGold,
    secondary = ElixirPurple,
    onSecondary = OnElixirPurple,
    secondaryContainer = ElixirContainer,
    onSecondaryContainer = ElixirPurple,
    tertiary = BuilderEmerald,
    onTertiary = OnBuilderEmerald,
    tertiaryContainer = BuilderEmeraldContainer,
    onTertiaryContainer = BuilderEmerald,
    error = TrapCoral,
    onError = OnTrapCoral,
    errorContainer = TrapCoralContainer,
    onErrorContainer = OnTrapCoralContainer,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    surfaceContainerLowest = DarkSurfaceLowest,
    surfaceContainerLow = DarkSurfaceLow,
    surfaceContainer = DarkSurface,
    surfaceContainerHigh = DarkSurfaceVariant,
    surfaceContainerHighest = DarkSurfaceHighest,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant
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
            insetsController.isAppearanceLightStatusBars = false
            insetsController.isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = ClashDarkColorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
