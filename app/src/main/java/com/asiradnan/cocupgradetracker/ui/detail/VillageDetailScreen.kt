package com.asiradnan.cocupgradetracker.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.asiradnan.cocupgradetracker.data.TimeFormatters
import com.asiradnan.cocupgradetracker.data.UpgradeItem
import com.asiradnan.cocupgradetracker.data.VillageData
import com.asiradnan.cocupgradetracker.ui.components.ClashBevelButton
import com.asiradnan.cocupgradetracker.ui.components.ClashButtonStyle
import com.asiradnan.cocupgradetracker.ui.components.ClashCapsuleFillStyle
import com.asiradnan.cocupgradetracker.ui.components.ClashCrestBadge
import com.asiradnan.cocupgradetracker.ui.components.ClashEtchedDivider
import com.asiradnan.cocupgradetracker.ui.components.ClashOutlinedText
import com.asiradnan.cocupgradetracker.ui.components.ClashPanelCard
import com.asiradnan.cocupgradetracker.ui.components.ClashUpgradeCapsuleBar
import com.asiradnan.cocupgradetracker.ui.theme.ClashGreenBright
import com.asiradnan.cocupgradetracker.ui.theme.ClashRed
import com.asiradnan.cocupgradetracker.ui.theme.PanelHeaderBuilderBase
import com.asiradnan.cocupgradetracker.ui.theme.PanelHeaderTownHall
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
        LastUpdatedAndPasteRow(
            pastedAtEpochSeconds = village.pastedAtEpochSeconds,
            nowEpochSeconds = nowEpochSeconds,
            onPasteClick = onPasteClick
        )

        Spacer(modifier = Modifier.height(12.dp))

        val baseEpoch = village.effectiveBaseEpochSeconds

        EarliestFreeOrAlreadyFreeBox(
            village = village,
            baseEpochSeconds = baseEpoch,
            nowEpochSeconds = nowEpochSeconds
        )

        Spacer(modifier = Modifier.height(14.dp))
        ClashEtchedDivider()
        Spacer(modifier = Modifier.height(14.dp))

        BaseSectionCard(
            title = "Townhall",
            headerColor = PanelHeaderTownHall,
            levelBadge = if (village.pastedAtEpochSeconds > 0L) "TH ${village.townHallLevel}" else null,
            totalBuilders = village.totalHomeBuilders,
            builderUpgrades = village.builderUpgrades,
            labUpgrades = village.labUpgrades,
            baseEpochSeconds = baseEpoch,
            pastedAtEpochSeconds = village.pastedAtEpochSeconds,
            nowEpochSeconds = nowEpochSeconds,
            showLab = village.townHallLevel >= 3
        )

        Spacer(modifier = Modifier.height(14.dp))
        ClashEtchedDivider()

        if (village.townHallLevel >= 6) {
            Spacer(modifier = Modifier.height(14.dp))

            BaseSectionCard(
                title = "Builder Base",
                headerColor = PanelHeaderBuilderBase,
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
            Spacer(modifier = Modifier.height(14.dp))
            ClashEtchedDivider()
        }

        Spacer(modifier = Modifier.height(18.dp))

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
    pastedAtEpochSeconds: Long,
    nowEpochSeconds: Long,
    onPasteClick: () -> Unit
) {
    val isUpdated = pastedAtEpochSeconds > 0L
    val elapsed = if (isUpdated) max(0L, nowEpochSeconds - pastedAtEpochSeconds) else 0L

    val statusDotColor = when {
        !isUpdated -> Color(0xFF9E9E9E)
        elapsed < 3600L * 4 -> ClashGreenBright
        elapsed < 86400L -> Color(0xFFE59412)
        else -> Color(0xFF88847C)
    }

    val primaryText = if (isUpdated) {
        "Updated ${TimeFormatters.formatRelativeAgo(elapsed)}"
    } else {
        "Not updated yet"
    }

    val secondaryText = if (isUpdated) {
        TimeFormatters.formatPastedClockTime(pastedAtEpochSeconds, nowEpochSeconds)
    } else {
        "Paste village JSON to start"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(statusDotColor)
            )

            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = primaryText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    ),
                    color = Color(0xFF261D15),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = secondaryText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.5.sp
                    ),
                    color = Color(0xFF6B655C),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        ClashBevelButton(
            text = "Paste Update",
            onClick = onPasteClick,
            style = ClashButtonStyle.GREEN,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun EarliestFreeOrAlreadyFreeBox(
    village: VillageData,
    baseEpochSeconds: Long,
    nowEpochSeconds: Long
) {
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
                val isFresh = freeDurationSeconds < 60L
                val (freeLabel, timeLabel) = if (isFresh) {
                    "Free" to "now"
                } else {
                    "Idle for" to TimeFormatters.formatCompactDuration(freeDurationSeconds)
                }
                ClashOutlinedText(
                    text = freeLabel,
                    style = MaterialTheme.typography.titleMedium,
                    color = ClashRed,
                    outlineWidth = 1.0.dp,
                    shadowOffset = 0.5.dp,
                    shadowAlpha = 0.22f,
                    shadowBlurRadius = 0.5.dp
                )
                ClashOutlinedText(
                    text = timeLabel,
                    style = MaterialTheme.typography.titleLarge,
                    color = ClashRed,
                    outlineWidth = 1.1.dp,
                    shadowOffset = 0.5.dp,
                    shadowAlpha = 0.22f,
                    shadowBlurRadius = 0.5.dp
                )
            } else if (earliest != null) {
                val finishEpoch = earliest.finishEpochSeconds(baseEpochSeconds)
                val formattedClock =
                    TimeFormatters.formatShortClockTime(finishEpoch, nowEpochSeconds)
                ClashOutlinedText(
                    text = "Earliest free",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    outlineWidth = 1.0.dp,
                    shadowOffset = 0.5.dp,
                    shadowAlpha = 0.22f,
                    shadowBlurRadius = 0.5.dp
                )
                ClashOutlinedText(
                    text = formattedClock,
                    style = MaterialTheme.typography.titleLarge,
                    color = ClashGreenBright,
                    outlineWidth = 1.1.dp,
                    shadowOffset = 0.5.dp,
                    shadowAlpha = 0.22f,
                    shadowBlurRadius = 0.5.dp
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
    nowEpochSeconds: Long,
    showLab: Boolean = true
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
            if (showLab) {
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
            if (elapsedFree >= 60L) {
                "Idle for ${TimeFormatters.formatCompactDuration(elapsedFree)}"
            } else {
                "Free now"
            }
        } else {
            "Free now"
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
