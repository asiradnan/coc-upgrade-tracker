package com.asiradnan.cocupgradetracker.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.asiradnan.cocupgradetracker.data.TimeFormatters
import com.asiradnan.cocupgradetracker.data.UpgradeCategory
import com.asiradnan.cocupgradetracker.data.UpgradeItem
import com.asiradnan.cocupgradetracker.data.VillageData
import com.asiradnan.cocupgradetracker.ui.components.ClashBevelButton
import com.asiradnan.cocupgradetracker.ui.components.ClashBevelIconButton
import com.asiradnan.cocupgradetracker.ui.components.ClashButtonStyle
import com.asiradnan.cocupgradetracker.ui.components.ClashCrestBadge
import com.asiradnan.cocupgradetracker.ui.components.ClashPanelCard
import com.asiradnan.cocupgradetracker.ui.components.ClashProgressBar
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
            onPasteClick = onPasteClick,
            onDeleteClick = onDeleteClick
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
    }
}

@Composable
private fun LastUpdatedAndPasteRow(
    lastUpdatedText: String,
    onPasteClick: () -> Unit,
    onDeleteClick: () -> Unit
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

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ClashBevelIconButton(
                icon = Icons.Default.Delete,
                contentDescription = "Delete village",
                onClick = onDeleteClick,
                style = ClashButtonStyle.RED,
                buttonSize = 38.dp
            )

            ClashBevelButton(
                text = "Paste update",
                onClick = onPasteClick,
                style = ClashButtonStyle.GREEN
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EarliestFreeOrAlreadyFreeBox(
    village: VillageData,
    baseEpochSeconds: Long,
    nowEpochSeconds: Long
) {
    val colorScheme = MaterialTheme.colorScheme
    val freeSlotDescriptions = village.getFreeSlotDescriptions(nowEpochSeconds)
    val freeSlotCount = village.getFreeSlotCount(nowEpochSeconds)
    val totalSlotCount = village.totalSlotCount

    val allActiveUpgrades = (
        village.builderUpgrades +
            village.labUpgrades +
            village.builderBaseUpgrades +
            village.starLabUpgrades
        ).filter { it.remainingSeconds(baseEpochSeconds, nowEpochSeconds) > 0L }

    val earliest = allActiveUpgrades.minByOrNull { it.finishEpochSeconds(baseEpochSeconds) }
    val maxActiveDuration = allActiveUpgrades.maxOfOrNull { it.durationSecondsAtExport } ?: 0L
    val hasAnyFree = freeSlotDescriptions.isNotEmpty()

    ClashPanelCard(
        headerTitle = if (hasAnyFree) "Available Now" else "Earliest Free",
        headerColor = if (hasAnyFree) Color(0xFF387A18) else Color(0xFF6B532A),
        headerTrailingContent = {
            if (hasAnyFree) {
                ClashCrestBadge(
                    text = "$freeSlotCount/$totalSlotCount Free",
                    containerColor = colorScheme.tertiary,
                    contentColor = Color.White,
                    borderColor = Color(0xFF1E4D08),
                    shadowText = true
                )
            } else if (earliest != null) {
                val remaining = earliest.remainingSeconds(baseEpochSeconds, nowEpochSeconds)
                ClashCrestBadge(
                    text = "in ${TimeFormatters.formatCompactDuration(remaining)}",
                    containerColor = colorScheme.primary,
                    contentColor = Color.White,
                    borderColor = Color(0xFF4A2B00),
                    shadowText = true
                )
            }
        }
    ) {
        if (hasAnyFree) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                freeSlotDescriptions.forEach { tagText ->
                    ClashCrestBadge(
                        text = tagText,
                        containerColor = colorScheme.tertiaryContainer,
                        contentColor = colorScheme.onTertiaryContainer,
                        borderColor = colorScheme.tertiary.copy(alpha = 0.55f)
                    )
                }
            }

            if (earliest != null) {
                val remaining = earliest.remainingSeconds(baseEpochSeconds, nowEpochSeconds)
                val earliestProgress = earliest.progressFraction(
                    baseEpochSeconds = baseEpochSeconds,
                    nowEpochSeconds = nowEpochSeconds,
                    referenceMaxSeconds = maxActiveDuration
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Next finishing: ${earliest.name} (${earliest.levelTransitionText})",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    )
                    Text(
                        text = TimeFormatters.formatCompactDuration(remaining),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                ClashProgressBar(
                    progress = earliestProgress,
                    modifier = Modifier.fillMaxWidth(),
                    fillColor = colorScheme.primary,
                    height = 8.dp
                )
            }
        } else if (earliest != null) {
            val finishEpoch = earliest.finishEpochSeconds(baseEpochSeconds)
            val formattedClock = TimeFormatters.formatShortClockTime(finishEpoch, nowEpochSeconds)
            val earliestProgress = earliest.progressFraction(
                baseEpochSeconds = baseEpochSeconds,
                nowEpochSeconds = nowEpochSeconds,
                referenceMaxSeconds = maxActiveDuration
            )
            val baseLabel = when (earliest.category) {
                UpgradeCategory.BUILDER_BASE_BUILDING,
                UpgradeCategory.BUILDER_BASE_RESEARCH -> "Builder Base"
                else -> "Townhall"
            }

            Text(
                text = formattedClock,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                color = colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${earliest.name} (${earliest.levelTransitionText}) • $baseLabel",
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            ClashProgressBar(
                progress = earliestProgress,
                modifier = Modifier.fillMaxWidth(),
                fillColor = colorScheme.primary,
                height = 10.dp
            )
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
        for (slotIdx in 0 until builderSlotCount) {
            val builderNumber = slotIdx + 1
            val upgrade = regularBuilders.getOrNull(slotIdx)

            UpgradeSlotRow(
                badgeText = "B$builderNumber",
                upgrade = upgrade,
                baseEpochSeconds = baseEpochSeconds,
                pastedAtEpochSeconds = pastedAtEpochSeconds,
                nowEpochSeconds = nowEpochSeconds,
                referenceMaxSeconds = sectionMaxDuration,
                accentColor = colorScheme.primary
            )

            HorizontalDivider(
                modifier = Modifier.padding(start = 56.dp),
                color = colorScheme.outlineVariant
            )
        }

        goblinBuilders.forEach { goblinUpgrade ->
            UpgradeSlotRow(
                badgeText = "GOB",
                upgrade = goblinUpgrade,
                baseEpochSeconds = baseEpochSeconds,
                pastedAtEpochSeconds = pastedAtEpochSeconds,
                nowEpochSeconds = nowEpochSeconds,
                referenceMaxSeconds = sectionMaxDuration,
                accentColor = colorScheme.tertiary
            )
            HorizontalDivider(
                modifier = Modifier.padding(start = 56.dp),
                color = colorScheme.outlineVariant
            )
        }

        UpgradeSlotRow(
            badgeText = "LAB",
            upgrade = regularLab,
            baseEpochSeconds = baseEpochSeconds,
            pastedAtEpochSeconds = pastedAtEpochSeconds,
            nowEpochSeconds = nowEpochSeconds,
            referenceMaxSeconds = sectionMaxDuration,
            accentColor = colorScheme.secondary
        )

        goblinLabs.forEach { goblinLab ->
            HorizontalDivider(
                modifier = Modifier.padding(start = 56.dp),
                color = colorScheme.outlineVariant
            )
            UpgradeSlotRow(
                badgeText = "GOB",
                upgrade = goblinLab,
                baseEpochSeconds = baseEpochSeconds,
                pastedAtEpochSeconds = pastedAtEpochSeconds,
                nowEpochSeconds = nowEpochSeconds,
                referenceMaxSeconds = sectionMaxDuration,
                accentColor = colorScheme.tertiary
            )
        }
    }
}

@Composable
private fun UpgradeSlotRow(
    badgeText: String,
    upgrade: UpgradeItem?,
    baseEpochSeconds: Long,
    pastedAtEpochSeconds: Long,
    nowEpochSeconds: Long,
    referenceMaxSeconds: Long,
    accentColor: Color
) {
    val colorScheme = MaterialTheme.colorScheme
    val remaining = upgrade?.remainingSeconds(baseEpochSeconds, nowEpochSeconds) ?: 0L
    val isBusy = upgrade != null && remaining > 0L

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ClashCrestBadge(
                    text = badgeText,
                    modifier = Modifier.width(44.dp),
                    containerColor = if (isBusy) accentColor else colorScheme.surfaceVariant,
                    contentColor = if (isBusy) Color.White else colorScheme.onSurfaceVariant,
                    borderColor = if (isBusy) {
                        Color.Black.copy(alpha = 0.45f)
                    } else {
                        colorScheme.outline.copy(alpha = 0.4f)
                    },
                    shadowText = isBusy
                )

                Column {
                    if (upgrade != null && remaining > 0L) {
                        Text(
                            text = upgrade.name,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = upgrade.levelTransitionText,
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    } else if (upgrade != null) {
                        Text(
                            text = "Available",
                            style = MaterialTheme.typography.bodyLarge,
                            color = colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Done: ${upgrade.name} (${upgrade.levelTransitionText})",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Text(
                            text = "Available",
                            style = MaterialTheme.typography.bodyLarge,
                            color = colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                        if (pastedAtEpochSeconds > 0L) {
                            val freeSinceSec = if (baseEpochSeconds > 0L) baseEpochSeconds else pastedAtEpochSeconds
                            val elapsedFree = max(0L, nowEpochSeconds - freeSinceSec)
                            Text(
                                text = "Idle at export (${TimeFormatters.formatRelativeAgo(elapsedFree)})",
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            if (upgrade != null && remaining > 0L) {
                val finishEpoch = upgrade.finishEpochSeconds(baseEpochSeconds)
                val exactClock = TimeFormatters.formatShortClockTime(finishEpoch, nowEpochSeconds)
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = TimeFormatters.formatCompactDuration(remaining),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = accentColor
                    )
                    Text(
                        text = exactClock,
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val freedEpoch = when {
                    upgrade != null -> upgrade.finishEpochSeconds(baseEpochSeconds)
                    baseEpochSeconds > 0L -> baseEpochSeconds
                    pastedAtEpochSeconds > 0L -> pastedAtEpochSeconds
                    else -> 0L
                }
                Column(horizontalAlignment = Alignment.End) {
                    ClashCrestBadge(
                        text = "Free",
                        containerColor = colorScheme.tertiary,
                        contentColor = Color.White,
                        borderColor = Color(0xFF1E4D08),
                        shadowText = true
                    )
                    if (freedEpoch > 0L) {
                        Spacer(modifier = Modifier.height(2.dp))
                        val freedClock = TimeFormatters.formatShortClockTime(freedEpoch, nowEpochSeconds)
                        Text(
                            text = if (upgrade != null) freedClock else "since $freedClock",
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (upgrade != null && remaining > 0L) {
            val progress = upgrade.progressFraction(
                baseEpochSeconds = baseEpochSeconds,
                nowEpochSeconds = nowEpochSeconds,
                referenceMaxSeconds = referenceMaxSeconds
            )
            Spacer(modifier = Modifier.height(8.dp))
            ClashProgressBar(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 56.dp),
                fillColor = accentColor,
                height = 8.dp
            )
        }
    }
}
