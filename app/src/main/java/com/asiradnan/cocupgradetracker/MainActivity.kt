package com.asiradnan.cocupgradetracker

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import com.asiradnan.cocupgradetracker.ui.TrackerScreen
import com.asiradnan.cocupgradetracker.ui.theme.COCUpgradeTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            COCUpgradeTrackerTheme {
                TrackerScreen()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TrackerScreenPreview() {
    COCUpgradeTrackerTheme {
        TrackerScreen()
    }
}
