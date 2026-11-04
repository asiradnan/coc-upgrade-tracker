package com.asiradnan.cocupgradetracker.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.asiradnan.cocupgradetracker.data.TimeFormatters
import com.asiradnan.cocupgradetracker.data.VillageData
import com.asiradnan.cocupgradetracker.ui.components.ClashCrestBadge
import com.asiradnan.cocupgradetracker.ui.components.ClashPanelCard
import com.asiradnan.cocupgradetracker.ui.components.ClashProgressBar
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
            ClashPanelCard(
                headerTitle = "My Villages",
                contentPadding = PaddingValues(vertical = 32.dp, horizontal = 20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No villages yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap the + Add village button below to paste your village JSON.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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

    ClashPanelCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ClashCrestBadge(
                    text = if (isUninitialized) "NEW" else "TH${village.townHallLevel}",
                    containerColor = if (isUninitialized) {
                        colorScheme.surfaceVariant
                    } else {
                        colorScheme.primary
                    },
                    contentColor = Color.White,
                    borderColor = Color(0xFF4A2B00),
                    shadowText = !isUninitialized
                )

                Column {
                    Text(
                        text = village.username,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold
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
            }

            if (isUninitialized) {
                ClashCrestBadge(
                    text = "Tap to paste",
                    containerColor = colorScheme.surfaceVariant,
                    contentColor = colorScheme.onSurfaceVariant
                )
            } else if (hasAnyFree) {
                Column(horizontalAlignment = Alignment.End) {
                    ClashCrestBadge(
                        text = "$freeSlotCount/$totalSlotCount Free",
                        containerColor = colorScheme.tertiary,
                        contentColor = Color.White,
                        borderColor = Color(0xFF1E4D08),
                        shadowText = true
                    )
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
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "in ${TimeFormatters.formatCompactDuration(remaining)} • 0/$totalSlotCount free",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
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
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = if (hasAnyFree) colorScheme.tertiary else colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            ClashProgressBar(
                progress = earliestProgress,
                modifier = Modifier.fillMaxWidth(),
                fillColor = if (hasAnyFree) colorScheme.tertiary else colorScheme.primary,
                height = 10.dp
            )
        }
    }
}
