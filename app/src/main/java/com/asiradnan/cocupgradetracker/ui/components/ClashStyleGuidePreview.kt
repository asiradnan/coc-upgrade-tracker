package com.asiradnan.cocupgradetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.asiradnan.cocupgradetracker.ui.theme.COCUpgradeTrackerTheme

/** Style-guide preview of the Clash components, used to compare against the in-game look. */
@Preview(widthDp = 380, heightDp = 560)
@Composable
private fun ClashStyleGuidePreview() {
    COCUpgradeTrackerTheme {
        val colorScheme = MaterialTheme.colorScheme
        Column(
            modifier = Modifier
                .background(colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ClashOutlinedText(
                text = "My Villages",
                style = MaterialTheme.typography.headlineSmall
            )

            ClashPanelCard(headerTitle = "Townhall") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ClashOutlinedText(text = "Music", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Earliest free",
                        style = MaterialTheme.typography.titleMedium,
                        color = colorScheme.onSurface
                    )
                    Text(
                        text = "Consent Choices (body caption)",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                    ClashUpgradeCapsuleBar(
                        titleText = "Archer Tower 21",
                        rightText = "2d 4h",
                        progress = 0.6f
                    )
                    ClashUpgradeCapsuleBar(
                        titleText = "Laboratory",
                        rightText = "Free",
                        progress = 0f,
                        isActive = false,
                        fillStyle = ClashCapsuleFillStyle.ELIXIR
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ClashBevelButton(text = "Privacy policy", onClick = {})
                ClashBevelButton(text = "Cancel", onClick = {}, style = ClashButtonStyle.NEUTRAL)
                ClashBevelButton(text = "Delete", onClick = {}, style = ClashButtonStyle.RED)
            }
        }
    }
}
