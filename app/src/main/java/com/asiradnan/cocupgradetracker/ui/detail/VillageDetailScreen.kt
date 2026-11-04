package com.asiradnan.cocupgradetracker.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.asiradnan.cocupgradetracker.data.TimeFormatters
import com.asiradnan.cocupgradetracker.data.UpgradeItem
import com.asiradnan.cocupgradetracker.data.VillageData
import com.asiradnan.cocupgradetracker.ui.components.ClashBevelButton
import com.asiradnan.cocupgradetracker.ui.components.ClashButtonStyle
import com.asiradnan.cocupgradetracker.ui.components.ClashCapsuleFillStyle
import com.asiradnan.cocupgradetracker.ui.components.ClashCrestBadge
import com.asiradnan.cocupgradetracker.ui.components.ClashPanelCard
import com.asiradnan.cocupgradetracker.ui.components.ClashUpgradeCapsuleBar
import kotlin.math.max

@Composable
fun VillageDetailScreen(
    village: VillageData,
    nowEpochSeconds: Long,
    onPasteClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                start = 20.dp,
                end = 20.dp,
                top = 8.dp,
                bottom = 24.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val lastUpdatedText = if (village.pastedAtEpochSeconds > 0L) {
            val elapsed = max(0L, nowEpochSeconds - village.pastedAtEpochSeconds)
            "Updated ${TimeFormatters.formatRelativeAgo(elapsed)}"
        } else {
            "Not updated yet"
        }

        LastUpdatedAndPasteRow(
            lastUpdatedText = lastUpdatedText,
            onPasteClick = onPasteClick
        )

        Spacer(modifier = Modifier.height(12.dp))

        val baseEpoch = village.effectiveBaseEpochSeconds

        EarliestFreeOrAlreadyFreeBox(
            village = village,
            baseEpochSeconds = baseEpoch,
            nowEpochSeconds = nowEpochSeconds
        )

        Spacer(modifier = Modifier.height(16.dp))

        BaseSectionCard(
            title = "Townhall",
            headerColor = Color(0xFF5C5242),
            levelBadge = if (village.pastedAtEpochSeconds > 0L) "TH ${village.townHallLevel}" else null,
            totalBuilders = village.totalHomeBuilders,
            builderUpgrades = village.builderUpgrades,
            labUpgrades = village.labUpgrades,
            baseEpochSeconds = baseEpoch,
            pastedAtEpochSeconds = village.pastedAtEpochSeconds,
            nowEpochSeconds = nowEpochSeconds
        )

        Spacer(modifier = Modifier.height(16.dp))

        BaseSectionCard(
            title = "Builder Base",
            headerColor = Color(0xFF3B4D66),
            levelBadge = if (village.pastedAtEpochSeconds > 0L && village.builderHallLevel != null) {
                "BH ${village.builderHallLevel}"
            } else {
                null
            },
            totalBuilders = village.totalBuilderBaseBuilders,
            builderUpgrades = village.builderBaseUpgrades,
            labUpgrades = village.starLabUpgrades,
            baseEpochSeconds = baseEpoch,
            pastedAtEpochSeconds = village.pastedAtEpochSeconds,
            nowEpochSeconds = nowEpochSeconds
        )

        Spacer(modifier = Modifier.height(20.dp))

        ClashBevelButton(
            text = "Delete village",
            leadingIcon = Icons.Default.Delete,
            onClick = onDeleteClick,
            style = ClashButtonStyle.RED
        )
    }
}

@Composable
private fun LastUpdatedAndPasteRow(
    lastUpdatedText: String,
    onPasteClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = lastUpdatedText,
            style = MaterialTheme.typography.bodySmall,
            color = colorScheme.onBackground.copy(alpha = 0.82f),
            modifier = Modifier.weight(1f)
        )

        ClashBevelButton(
            text = "Paste update",
            onClick = onPasteClick,
            style = ClashButtonStyle.GREEN
        )
    }
}

@Composable
private fun EarliestFreeOrAlreadyFreeBox(
    village: VillageData,
    baseEpochSeconds: Long,
    nowEpochSeconds: Long
) {
    val colorScheme = MaterialTheme.colorScheme
    val freeDurationSeconds = village.getFreeDurationSeconds(nowEpochSeconds)

    val allActiveUpgrades = (
        village.builderUpgrades +
            village.labUpgrades +
            village.builderBaseUpgrades +
            village.starLabUpgrades
        ).filter { it.remainingSeconds(baseEpochSeconds, nowEpochSeconds) > 0L }

    val earliest = allActiveUpgrades.minByOrNull { it.finishEpochSeconds(baseEpochSeconds) }

    ClashPanelCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (freeDurationSeconds != null) {
                val freeTimeLabel = if (freeDurationSeconds <= 0L) {
                    "now"
                } else {
                    TimeFormatters.formatCompactDuration(freeDurationSeconds)
                }
                Text(
                    text = "Free",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = colorScheme.error
                )
                Text(
                    text = freeTimeLabel,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = colorScheme.error
                )
            } else if (earliest != null) {
                val finishEpoch = earliest.finishEpochSeconds(baseEpochSeconds)
                val formattedClock = TimeFormatters.formatShortClockTime(finishEpoch, nowEpochSeconds)
                Text(
                    text = "Earliest free",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = colorScheme.onSurface
                )
                Text(
                    text = formattedClock,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
private fun BaseSectionCard(
    title: String,
    headerColor: Color,
    levelBadge: String?,
    totalBuilders: Int,
    builderUpgrades: List<UpgradeItem>,
    labUpgrades: List<UpgradeItem>,
    baseEpochSeconds: Long,
    pastedAtEpochSeconds: Long,
    nowEpochSeconds: Long
) {
    val colorScheme = MaterialTheme.colorScheme
    val regularBuilders = builderUpgrades.filter { !it.isGoblinHelper }
    val goblinBuilders = builderUpgrades.filter { it.isGoblinHelper }
    val builderSlotCount = max(totalBuilders, regularBuilders.size)

    val regularLab = labUpgrades.firstOrNull { !it.isGoblinHelper }
    val goblinLabs = labUpgrades.filter { it.isGoblinHelper }

    val sectionMaxDuration = (builderUpgrades + labUpgrades)
        .filter { it.remainingSeconds(baseEpochSeconds, nowEpochSeconds) > 0L }
        .maxOfOrNull { it.durationSecondsAtExport } ?: 0L

    ClashPanelCard(
        headerTitle = title,
        headerColor = headerColor,
        containerColor = Color(0xFF634B3A),
        borderColor = Color(0xFF2E2219),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
        headerTrailingContent = {
            if (levelBadge != null) {
                ClashCrestBadge(
                    text = levelBadge,
                    containerColor = colorScheme.primary,
                    contentColor = Color.White,
                    borderColor = Color(0xFF4A2B00),
                    shadowText = true
                )
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (slotIdx in 0 until builderSlotCount) {
                val builderNumber = slotIdx + 1
                val upgrade = regularBuilders.getOrNull(slotIdx)

                UpgradeSlotRow(
                    slotLabel = "Builder $builderNumber",
                    upgrade = upgrade,
                    baseEpochSeconds = baseEpochSeconds,
                    pastedAtEpochSeconds = pastedAtEpochSeconds,
                    nowEpochSeconds = nowEpochSeconds,
                    referenceMaxSeconds = sectionMaxDuration,
                    fillStyle = ClashCapsuleFillStyle.GREEN
                )
            }

            goblinBuilders.forEach { goblinUpgrade ->
                UpgradeSlotRow(
                    slotLabel = "Goblin Builder",
                    upgrade = goblinUpgrade,
                    baseEpochSeconds = baseEpochSeconds,
                    pastedAtEpochSeconds = pastedAtEpochSeconds,
                    nowEpochSeconds = nowEpochSeconds,
                    referenceMaxSeconds = sectionMaxDuration,
                    fillStyle = ClashCapsuleFillStyle.GREEN
                )
            }

            UpgradeSlotRow(
                slotLabel = "Laboratory",
                upgrade = regularLab,
                baseEpochSeconds = baseEpochSeconds,
                pastedAtEpochSeconds = pastedAtEpochSeconds,
                nowEpochSeconds = nowEpochSeconds,
                referenceMaxSeconds = sectionMaxDuration,
                fillStyle = ClashCapsuleFillStyle.ELIXIR
            )

            goblinLabs.forEach { goblinLab ->
                UpgradeSlotRow(
                    slotLabel = "Goblin Researcher",
                    upgrade = goblinLab,
                    baseEpochSeconds = baseEpochSeconds,
                    pastedAtEpochSeconds = pastedAtEpochSeconds,
                    nowEpochSeconds = nowEpochSeconds,
                    referenceMaxSeconds = sectionMaxDuration,
                    fillStyle = ClashCapsuleFillStyle.ELIXIR
                )
            }
        }
    }
}

@Composable
private fun UpgradeSlotRow(
    slotLabel: String,
    upgrade: UpgradeItem?,
    baseEpochSeconds: Long,
    pastedAtEpochSeconds: Long,
    nowEpochSeconds: Long,
    referenceMaxSeconds: Long,
    fillStyle: ClashCapsuleFillStyle = ClashCapsuleFillStyle.GREEN
) {
    val remaining = upgrade?.remainingSeconds(baseEpochSeconds, nowEpochSeconds) ?: 0L

    if (upgrade != null && remaining > 0L) {
        val progress = upgrade.progressFraction(
            baseEpochSeconds = baseEpochSeconds,
            nowEpochSeconds = nowEpochSeconds,
            referenceMaxSeconds = referenceMaxSeconds
        )
        ClashUpgradeCapsuleBar(
            titleText = upgrade.name,
            rightText = TimeFormatters.formatCompactDuration(remaining),
            progress = progress,
            isActive = true,
            showStopwatch = true,
            fillStyle = fillStyle
        )
    } else {
        val titleText = if (upgrade != null) {
            "${upgrade.name} (Done)"
        } else {
            slotLabel
        }
        val freedEpoch = when {
            upgrade != null -> upgrade.finishEpochSeconds(baseEpochSeconds)
            baseEpochSeconds > 0L -> baseEpochSeconds
            pastedAtEpochSeconds > 0L -> pastedAtEpochSeconds
            else -> 0L
        }
        val freeText = if (freedEpoch > 0L) {
            val elapsedFree = max(0L, nowEpochSeconds - freedEpoch)
            if (elapsedFree > 0L) {
                "Free ${TimeFormatters.formatCompactDuration(elapsedFree)}"
            } else {
                "Free"
            }
        } else {
            "Free"
        }

        ClashUpgradeCapsuleBar(
            titleText = titleText,
            rightText = freeText,
            progress = 0f,
            isActive = false,
            showStopwatch = false,
            titleColor = Color(0xFFE0D3C3),
            rightTextColor = Color(0xFFFF6B5E)
        )
    }
}
