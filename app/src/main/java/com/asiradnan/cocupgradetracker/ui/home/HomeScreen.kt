package com.asiradnan.cocupgradetracker.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.asiradnan.cocupgradetracker.data.TimeFormatters
import com.asiradnan.cocupgradetracker.data.VillageData
import com.asiradnan.cocupgradetracker.ui.components.ClashBevelButton
import com.asiradnan.cocupgradetracker.ui.components.ClashBevelCard
import com.asiradnan.cocupgradetracker.ui.components.ClashButtonStyle
import com.asiradnan.cocupgradetracker.ui.components.ClashOutlinedText
import com.asiradnan.cocupgradetracker.ui.components.ClashPanelCard
import com.asiradnan.cocupgradetracker.ui.components.ClashTownHallImage
import com.asiradnan.cocupgradetracker.ui.theme.ClashGold
import com.asiradnan.cocupgradetracker.ui.theme.ClashGreenBright
import com.asiradnan.cocupgradetracker.ui.theme.ClashInkOutline
import com.asiradnan.cocupgradetracker.ui.theme.ClashRed
import kotlinx.coroutines.launch
import java.util.IdentityHashMap
import kotlin.math.abs
import kotlin.math.roundToInt

private val SlotSpring = spring<Float>(
    dampingRatio = 0.8f,
    stiffness = Spring.StiffnessMedium
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    villages: List<VillageData>,
    nowEpochSeconds: Long,
    modifier: Modifier = Modifier,
    onSelectVillage: (Int) -> Unit,
    onSwipeToDetail: () -> Unit,
    onDeleteVillageClick: (Int) -> Unit,
    onPasteFromClipboardClick: () -> Unit,
    onReorderVillages: (List<VillageData>) -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    var cumulativeDragX by remember { mutableFloatStateOf(0f) }
    var isOrganizing by remember { mutableStateOf(false) }

    // Local display order. Re-synced whenever the source list changes.
    var order by remember(villages) { mutableStateOf(villages) }

    // Stable per-village keys (tag-based, de-duplicated) so per-item state follows the item.
    val keyMap = remember(villages) {
        val map = IdentityHashMap<VillageData, String>()
        val seen = mutableMapOf<String, Int>()
        villages.forEach { v ->
            val n = seen.getOrDefault(v.tag, 0)
            seen[v.tag] = n + 1
            map[v] = if (n == 0 && v.tag.isNotBlank() && v.tag != "#NEW") v.tag else "${v.tag}#$n#${v.username}"
        }
        map
    }
    fun keyOf(v: VillageData): String = keyMap[v] ?: v.tag

    // One animated visual offset per village; survives reordering because it is keyed by village.
    val offsets = remember { mutableMapOf<String, Animatable<Float, AnimationVector1D>>() }

    // Drag state
    var draggingKey by remember { mutableStateOf<String?>(null) }
    var dragFromIndex by remember { mutableIntStateOf(-1) }
    var dragY by remember { mutableFloatStateOf(0f) }
    var elevatedKey by remember { mutableStateOf<String?>(null) }
    var cardHeightPx by remember { mutableFloatStateOf(0f) }
    var lastHapticSlot by remember { mutableIntStateOf(-1) }

    val spacingPx = with(density) { 10.dp.toPx() }
    val stepPx = (if (cardHeightPx > 0f) cardHeightPx else with(density) { 82.dp.toPx() }) + spacingPx

    fun targetIndexNow(): Int {
        val from = dragFromIndex
        if (from < 0 || order.isEmpty()) return -1
        return (from + (dragY / stepPx).roundToInt()).coerceIn(0, order.lastIndex)
    }

    val targetIndex = if (draggingKey != null) targetIndexNow() else -1

    // Light tick whenever the dragged card enters a new slot
    LaunchedEffect(targetIndex) {
        if (targetIndex >= 0 && targetIndex != lastHapticSlot) {
            if (lastHapticSlot != -1) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            lastHapticSlot = targetIndex
        }
    }

    /**
     * Drops the dragged card: commits the new order and animates every card from where it
     * visually is right now into its final slot, so nothing jumps.
     */
    fun finishDrag(commit: Boolean) {
        val key = draggingKey ?: return
        val from = dragFromIndex
        val to = if (commit) targetIndexNow() else from
        val step = stepPx
        val releasedDragY = dragY
        val oldOrder = order
        if (from !in oldOrder.indices || to !in oldOrder.indices) {
            draggingKey = null; dragFromIndex = -1; dragY = 0f; lastHapticSlot = -1
            return
        }
        val newOrder = if (from != to) {
            oldOrder.toMutableList().also { it.add(to, it.removeAt(from)) }
        } else {
            oldOrder
        }

        scope.launch {
            // Re-express every card's current on-screen offset relative to its NEW slot
            oldOrder.forEachIndexed { i, v ->
                val k = keyOf(v)
                val anim = offsets.getOrPut(k) { Animatable(0f) }
                val j = newOrder.indexOfFirst { it === v }
                val shown = if (k == key) releasedDragY + anim.value else anim.value
                anim.snapTo(shown + (i - j) * step)
            }
            elevatedKey = key
            order = newOrder
            draggingKey = null
            dragFromIndex = -1
            dragY = 0f
            lastHapticSlot = -1
            if (from != to) onReorderVillages(newOrder)

            newOrder.forEach { v ->
                val k = keyOf(v)
                val anim = offsets.getOrPut(k) { Animatable(0f) }
                launch {
                    anim.animateTo(0f, SlotSpring)
                    if (k == key && elevatedKey == key) elevatedKey = null
                }
            }
        }
    }
    val finishDragState = rememberUpdatedState<(Boolean) -> Unit> { commit -> finishDrag(commit) }

    // Intercept system back button to exit organize mode if active
    BackHandler(enabled = isOrganizing) {
        if (draggingKey != null) finishDragState.value(false)
        isOrganizing = false
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(villages.size, isOrganizing) {
                if (!isOrganizing) {
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
            }
    ) {
        if (villages.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 10.dp,
                        bottom = 88.dp
                    )
            ) {
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
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState(), enabled = draggingKey == null)
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 10.dp,
                        bottom = 88.dp
                    )
            ) {
                // Header with Villages title and Organize / Done toggle button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        ClashOutlinedText(
                            text = "Villages",
                            style = MaterialTheme.typography.titleLarge
                        )
                        if (isOrganizing) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Drag ≡ to reorder",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                                color = Color(0xFF756A5B)
                            )
                        }
                    }

                    if (villages.size > 1) {
                        if (!isOrganizing) {
                            ClashBevelButton(
                                text = "Organize",
                                leadingIcon = Icons.Default.Edit,
                                onClick = { isOrganizing = true },
                                style = ClashButtonStyle.NEUTRAL,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            )
                        } else {
                            ClashBevelButton(
                                text = "Done",
                                leadingIcon = Icons.Default.Check,
                                onClick = {
                                    if (draggingKey != null) finishDragState.value(false)
                                    isOrganizing = false
                                },
                                style = ClashButtonStyle.GREEN,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    order.forEachIndexed { idx, village ->
                        val itemKey = keyOf(village)
                        key(itemKey) {
                            val anim = offsets.getOrPut(itemKey) { Animatable(0f) }
                            val isDragged = itemKey == draggingKey
                            val from = dragFromIndex
                            val to = targetIndex

                            // Non-dragged cards slide one slot to open a gap for the dragged card
                            val targetShift = when {
                                draggingKey == null || isDragged || from < 0 || to < 0 -> 0f
                                from < to && idx in (from + 1)..to -> -stepPx
                                from > to && idx in to until from -> stepPx
                                else -> 0f
                            }
                            LaunchedEffect(targetShift) {
                                anim.animateTo(targetShift, SlotSpring)
                            }

                            val handleModifier = if (isOrganizing && villages.size > 1) {
                                Modifier.pointerInput(itemKey, idx) {
                                    detectVerticalDragGestures(
                                        onDragStart = {
                                            elevatedKey = null
                                            draggingKey = itemKey
                                            dragFromIndex = idx
                                            dragY = 0f
                                            lastHapticSlot = idx
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        },
                                        onVerticalDrag = { change, dragAmount ->
                                            change.consume()
                                            dragY += dragAmount
                                        },
                                        onDragEnd = { finishDragState.value(true) },
                                        onDragCancel = { finishDragState.value(false) }
                                    )
                                }
                            } else {
                                Modifier
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .zIndex(if (isDragged || itemKey == elevatedKey) 10f else 1f)
                                    .graphicsLayer {
                                        translationY = anim.value + if (isDragged) dragY else 0f
                                        if (isDragged) {
                                            scaleX = 1.03f
                                            scaleY = 1.03f
                                            shadowElevation = 14.dp.toPx()
                                        }
                                    }
                                    .onGloballyPositioned { coordinates ->
                                        if (!isDragged) {
                                            cardHeightPx = coordinates.size.height.toFloat()
                                        }
                                    }
                            ) {
                                val sourceIndex = villages.indexOfFirst { it === village }
                                    .let { if (it >= 0) it else idx }

                                VillageSummaryRowCard(
                                    village = village,
                                    nowEpochSeconds = nowEpochSeconds,
                                    enabled = !isOrganizing,
                                    onClick = { if (!isOrganizing) onSelectVillage(sourceIndex) },
                                    onLongClick = { if (!isOrganizing) onDeleteVillageClick(sourceIndex) },
                                    showDragHandle = isOrganizing && villages.size > 1,
                                    isDragging = isDragged,
                                    dragHandleModifier = handleModifier
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClashDragHandle(
    modifier: Modifier = Modifier,
    isDragging: Boolean = false
) {
    val barColor = if (isDragging) ClashGold else Color(0xFF9E978C)
    val highlightColor = if (isDragging) Color(0xFFFFE599) else Color(0xFFDCD6CC)

    // Generous 44dp x 52dp touch target so the handle is easy to grab
    Box(
        modifier = modifier.size(width = 44.dp, height = 52.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(3.5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height(3.5.dp)
                        .clip(RoundedCornerShape(1.75.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(highlightColor, barColor)
                            )
                        )
                )
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
    onLongClick: () -> Unit,
    enabled: Boolean = true,
    showDragHandle: Boolean = false,
    isDragging: Boolean = false,
    dragHandleModifier: Modifier = Modifier
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

    val cardBorderColor = if (isDragging) ClashGold else ClashInkOutline
    val cardBgColor = if (isDragging) Color(0xFFFFFDF8) else Color(0xFFFAF8F5)
    val cardLipColor = if (isDragging) Color(0xFFE5B560) else Color(0xFFC7C3B7)

    ClashBevelCard(
        onClick = onClick,
        onLongClick = onLongClick,
        enabled = enabled,
        borderColor = cardBorderColor,
        backgroundColor = cardBgColor,
        bottomLipColor = cardLipColor
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
                .padding(end = 8.dp),
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

        // Far right: Drag handle (only visible when in organizing mode)
        if (showDragHandle) {
            Spacer(modifier = Modifier.width(4.dp))
            ClashDragHandle(
                isDragging = isDragging,
                modifier = dragHandleModifier
            )
        }
    }
}
