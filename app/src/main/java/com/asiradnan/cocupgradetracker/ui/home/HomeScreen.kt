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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import com.asiradnan.cocupgradetracker.ui.components.ClashBevelButton
import com.asiradnan.cocupgradetracker.ui.components.ClashButtonStyle
import com.asiradnan.cocupgradetracker.ui.components.ClashBevelCard
import com.asiradnan.cocupgradetracker.ui.components.ClashOutlinedText
import com.asiradnan.cocupgradetracker.ui.components.ClashPanelCard
import com.asiradnan.cocupgradetracker.ui.components.ClashTownHallImage
import com.asiradnan.cocupgradetracker.ui.theme.ClashGreenBright
import com.asiradnan.cocupgradetracker.ui.theme.ClashRed
import kotlin.math.abs

@Composable
fun HomeScreen(
    villages: List<VillageData>,
    nowEpochSeconds: Long,
    modifier: Modifier = Modifier,
    onSelectVillage: (Int) -> Unit,
    onSwipeToDetail: () -> Unit,
    onDeleteVillageClick: (Int) -> Unit,
    onPasteFromClipboardClick: () -> Unit
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
                start = 16.dp,
                end = 16.dp,
                top = 10.dp,
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
                    ClashOutlinedText(
                        text = "No villages yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Copy your village export from Clash of Clans, then open this app or tap 'Paste update' to add your village.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    ClashBevelButton(
                        text = "Paste from clipboard",
                        onClick = onPasteFromClipboardClick,
                        style = ClashButtonStyle.GREEN,
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                    )
                }
            }
        } else {
            ClashOutlinedText(
                text = "Villages",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )
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
    val freeDurationSeconds = village.getFreeDurationSeconds(nowEpochSeconds)

    val allActiveUpgrades = (
        village.builderUpgrades +
            village.labUpgrades +
            village.builderBaseUpgrades +
            village.starLabUpgrades
        ).filter { it.remainingSeconds(baseEpochSeconds, nowEpochSeconds) > 0L }

    val earliest = allActiveUpgrades.minByOrNull { it.finishEpochSeconds(baseEpochSeconds) }
    val isUninitialized = village.pastedAtEpochSeconds == 0L && village.rawJson.isBlank()

    ClashBevelCard(
        onClick = onClick,
        onLongClick = onLongClick
    ) {
        // Respective Town Hall image
        ClashTownHallImage(
            townHallLevel = village.townHallLevel,
            modifier = Modifier.size(50.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Village name and Town Hall level
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = village.username,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black
                ),
                color = Color(0xFF261D15),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!isUninitialized) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "TH ${village.townHallLevel}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF756A5B)
                )
            }
        }

        // Right: Status
        if (isUninitialized) {
            Text(
                text = "Tap to paste",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = colorScheme.onSurfaceVariant
            )
        } else if (freeDurationSeconds != null) {
            val isFresh = freeDurationSeconds < 60L
            val (statusTitle, statusSubtitle) = if (isFresh) {
                "Free" to "now"
            } else {
                "Idle for" to TimeFormatters.formatCompactDuration(freeDurationSeconds)
            }
            Column(horizontalAlignment = Alignment.End) {
                ClashOutlinedText(
                    text = statusTitle,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = ClashRed,
                    outlineWidth = 1.0.dp,
                    shadowOffset = 0.5.dp,
                    shadowAlpha = 0.22f,
                    shadowBlurRadius = 0.5.dp
                )
                ClashOutlinedText(
                    text = statusSubtitle,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = ClashRed,
                    outlineWidth = 1.0.dp,
                    shadowOffset = 0.5.dp,
                    shadowAlpha = 0.22f,
                    shadowBlurRadius = 0.5.dp
                )
            }
        } else if (earliest != null) {
            val finishEpoch = earliest.finishEpochSeconds(baseEpochSeconds)
            val formattedClock = TimeFormatters.formatClockTimeOnly(finishEpoch)
            ClashOutlinedText(
                text = formattedClock,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 17.sp
                ),
                color = ClashGreenBright,
                outlineWidth = 1.1.dp,
                shadowOffset = 0.5.dp,
                shadowAlpha = 0.22f,
                shadowBlurRadius = 0.5.dp
            )
        }
    }
}
