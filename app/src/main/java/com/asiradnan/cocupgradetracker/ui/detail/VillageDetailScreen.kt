package com.asiradnan.cocupgradetracker.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.asiradnan.cocupgradetracker.data.TimeFormatters
import com.asiradnan.cocupgradetracker.data.UpgradeCategory
import com.asiradnan.cocupgradetracker.data.UpgradeItem
import com.asiradnan.cocupgradetracker.data.VillageData
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
            color = colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete village",
                    tint = colorScheme.onSurfaceVariant
                )
            }

            FilledTonalButton(
                onClick = onPasteClick,
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = colorScheme.primaryContainer,
                    contentColor = colorScheme.onPrimaryContainer
                )
            ) {
                Text(
                    text = "Paste update",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            }
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            if (hasAnyFree) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(colorScheme.tertiary)
                        )
                        Text(
                            text = "Free now ($freeSlotCount/$totalSlotCount)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colorScheme.tertiary
                        )
                    }

                    if (earliest != null) {
                        val remaining = earliest.remainingSeconds(baseEpochSeconds, nowEpochSeconds)
                        Text(
                            text = "Next in ${TimeFormatters.formatCompactDuration(remaining)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    freeSlotDescriptions.forEach { tagText ->
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = colorScheme.tertiaryContainer
                        ) {
                            Text(
                                text = tagText,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            } else if (earliest != null) {
                val finishEpoch = earliest.finishEpochSeconds(baseEpochSeconds)
                val remaining = earliest.remainingSeconds(baseEpochSeconds, nowEpochSeconds)
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
                    text = "EARLIEST FREE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 0.8.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formattedClock,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = colorScheme.onSurface
                    )

                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "in ${TimeFormatters.formatCompactDuration(remaining)}",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${earliest.name} (${earliest.levelTransitionText}) • $baseLabel",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { earliestProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(MaterialTheme.shapes.extraSmall),
                    color = colorScheme.primary,
                    trackColor = colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round,
                    gapSize = 0.dp,
                    drawStopIndicator = {}
                )
            }
        }
    }
}

@Composable
private fun BaseSectionCard(
    title: String,
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = colorScheme.onSurface
                )

                if (levelBadge != null) {
                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color = colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = levelBadge,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

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
                    modifier = Modifier.padding(start = 54.dp),
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
                    modifier = Modifier.padding(start = 54.dp),
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
                    modifier = Modifier.padding(start = 54.dp),
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
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = colorScheme.surfaceVariant,
                    modifier = Modifier.width(42.dp)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = if (isBusy) accentColor else colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }

                Column {
                    if (upgrade != null && remaining > 0L) {
                        Text(
                            text = upgrade.name,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
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
                            color = colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Text(
                            text = "Available",
                            style = MaterialTheme.typography.bodyLarge,
                            color = colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                        )
                        if (pastedAtEpochSeconds > 0L) {
                            val freeSinceSec = if (baseEpochSeconds > 0L) baseEpochSeconds else pastedAtEpochSeconds
                            val elapsedFree = max(0L, nowEpochSeconds - freeSinceSec)
                            Text(
                                text = "Idle at export (${TimeFormatters.formatRelativeAgo(elapsedFree)})",
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
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
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
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
                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color = colorScheme.tertiaryContainer
                    ) {
                        Text(
                            text = "Free",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                        )
                    }
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
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 54.dp)
                    .height(3.dp)
                    .clip(MaterialTheme.shapes.extraSmall),
                color = accentColor,
                trackColor = colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round,
                gapSize = 0.dp,
                drawStopIndicator = {}
            )
        }
    }
}
