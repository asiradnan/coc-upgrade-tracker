package com.asiradnan.cocupgradetracker.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.asiradnan.cocupgradetracker.data.TimeFormatters
import com.asiradnan.cocupgradetracker.data.VillageData
import kotlin.math.abs

@Composable
fun HomeScreen(
    villages: List<VillageData>,
    nowEpochSeconds: Long,
    modifier: Modifier = Modifier,
    onSelectVillage: (Int) -> Unit,
    onSwipeToDetail: () -> Unit,
    onDeleteVillageClick: (Int) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    var cumulativeDragX by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = modifier
            .pointerInput(villages.size) {
                detectHorizontalDragGestures(
                    onDragStart = { cumulativeDragX = 0f },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        cumulativeDragX += dragAmount
                    },
                    onDragEnd = {
                        if (abs(cumulativeDragX) > 90f && villages.isNotEmpty()) {
                            onSwipeToDetail()
                        }
                        cumulativeDragX = 0f
                    },
                    onDragCancel = { cumulativeDragX = 0f }
                )
            }
            .verticalScroll(rememberScrollState())
            .padding(
                start = 20.dp,
                end = 20.dp,
                top = 8.dp,
                bottom = 88.dp
            )
    ) {
        if (villages.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 36.dp, horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No villages yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap the + button below to add your first village.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                villages.forEachIndexed { idx, village ->
                    VillageSummaryRowCard(
                        village = village,
                        nowEpochSeconds = nowEpochSeconds,
                        onClick = { onSelectVillage(idx) },
                        onLongClick = { onDeleteVillageClick(idx) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VillageSummaryRowCard(
    village: VillageData,
    nowEpochSeconds: Long,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val baseEpochSeconds = village.effectiveBaseEpochSeconds
    val freeSlots = village.getFreeSlotDescriptions(nowEpochSeconds)
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

    val hasAnyFree = freeSlotCount > 0
    val isUninitialized = village.pastedAtEpochSeconds == 0L && village.rawJson.isBlank()

    val subtitleText = if (isUninitialized) {
        "No data pasted yet"
    } else {
        buildString {
            append("TH ${village.townHallLevel}")
            if (village.builderHallLevel != null) {
                append(" • BH ${village.builderHallLevel}")
            }
            if (village.tag.isNotBlank() && village.tag != "#NEW") {
                append(" • ${village.tag}")
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
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
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                ) {
                    Text(
                        text = village.username,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitleText,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (isUninitialized) {
                    Text(
                        text = "Tap to paste",
                        style = MaterialTheme.typography.labelMedium,
                        color = colorScheme.onSurfaceVariant
                    )
                } else if (hasAnyFree) {
                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            shape = MaterialTheme.shapes.extraSmall,
                            color = colorScheme.tertiaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(colorScheme.tertiary)
                                )
                                Text(
                                    text = "$freeSlotCount/$totalSlotCount free",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = colorScheme.onTertiaryContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = freeSlots.joinToString(" • "),
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else if (earliest != null) {
                    val finishEpoch = earliest.finishEpochSeconds(baseEpochSeconds)
                    val remaining = earliest.remainingSeconds(baseEpochSeconds, nowEpochSeconds)
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = TimeFormatters.formatShortClockTime(finishEpoch, nowEpochSeconds),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "in ${TimeFormatters.formatCompactDuration(remaining)} • 0/$totalSlotCount free",
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.primary
                        )
                    }
                }
            }

            if (!isUninitialized && earliest != null) {
                val earliestProgress = earliest.progressFraction(
                    baseEpochSeconds = baseEpochSeconds,
                    nowEpochSeconds = nowEpochSeconds,
                    referenceMaxSeconds = maxActiveDuration
                )
                val remaining = earliest.remainingSeconds(baseEpochSeconds, nowEpochSeconds)

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Next: ${earliest.name} (${earliest.levelTransitionText})",
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    )
                    Text(
                        text = TimeFormatters.formatCompactDuration(remaining),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = if (hasAnyFree) colorScheme.onSurfaceVariant else colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))

                LinearProgressIndicator(
                    progress = { earliestProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(MaterialTheme.shapes.extraSmall),
                    color = if (hasAnyFree) colorScheme.tertiary else colorScheme.primary,
                    trackColor = colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round,
                    gapSize = 0.dp,
                    drawStopIndicator = {}
                )
            }
        }
    }
}
