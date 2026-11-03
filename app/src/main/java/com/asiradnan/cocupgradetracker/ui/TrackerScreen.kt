package com.asiradnan.cocupgradetracker.ui

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.asiradnan.cocupgradetracker.data.CocVillageParser
import com.asiradnan.cocupgradetracker.data.ParseResult
import com.asiradnan.cocupgradetracker.data.TimeFormatters
import com.asiradnan.cocupgradetracker.data.UpgradeCategory
import com.asiradnan.cocupgradetracker.data.UpgradeItem
import com.asiradnan.cocupgradetracker.data.VillageData
import com.asiradnan.cocupgradetracker.ui.theme.BuilderEmerald
import com.asiradnan.cocupgradetracker.ui.theme.ClashGold
import com.asiradnan.cocupgradetracker.ui.theme.DarkBackground
import com.asiradnan.cocupgradetracker.ui.theme.DarkBorder
import com.asiradnan.cocupgradetracker.ui.theme.DarkSurface
import com.asiradnan.cocupgradetracker.ui.theme.ElixirPurple
import com.asiradnan.cocupgradetracker.ui.theme.GoblinGreen
import com.asiradnan.cocupgradetracker.ui.theme.TextPrimary
import com.asiradnan.cocupgradetracker.ui.theme.TextSecondary
import com.asiradnan.cocupgradetracker.ui.theme.TrapCoral
import kotlin.math.abs
import kotlin.math.max
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val PREFS_NAME = "coc_upgrade_tracker_prefs_v2"
private const val KEY_VILLAGE_COUNT = "village_count"
private const val KEY_SELECTED_INDEX = "selected_village_index"
private const val KEY_SAMPLES_INITIALIZED = "samples_initialized"
private const val LOOP_PAGER_COUNT = 10_000

private fun createEmptyVillage(name: String): VillageData = VillageData(
    username = name,
    tag = "#NEW",
    exportTimestampSeconds = 0L,
    hasExplicitTimestamp = false,
    pastedAtEpochSeconds = 0L,
    townHallLevel = 1,
    builderHallLevel = 1,
    totalRegularBuilders = 5,
    hasBobBuilder = false,
    totalBuilderBaseBuilders = 1,
    builderUpgrades = emptyList(),
    labUpgrades = emptyList(),
    petUpgrades = emptyList(),
    builderBaseUpgrades = emptyList(),
    starLabUpgrades = emptyList(),
    helpers = emptyList(),
    rawJson = ""
)

@Composable
fun TrackerScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    val coroutineScope = rememberCoroutineScope()

    var nowEpochSeconds by remember { mutableLongStateOf(System.currentTimeMillis() / 1000L) }
    LaunchedEffect(Unit) {
        while (true) {
            nowEpochSeconds = System.currentTimeMillis() / 1000L
            delay(1000L)
        }
    }

    var villages by remember { mutableStateOf<List<VillageData>>(emptyList()) }
    var currentIndex by rememberSaveable { mutableIntStateOf(0) }
    var isHomePage by rememberSaveable { mutableStateOf(true) }
    var showAddVillageDialog by rememberSaveable { mutableStateOf(false) }
    var pendingDeleteIndex by rememberSaveable { mutableStateOf<Int?>(null) }

    fun saveVillagesToPrefs(list: List<VillageData>, selectedIdx: Int) {
        val editor = prefs.edit().clear()
        editor.putBoolean(KEY_SAMPLES_INITIALIZED, true)
        editor.putInt(KEY_VILLAGE_COUNT, list.size)
        editor.putInt(KEY_SELECTED_INDEX, selectedIdx)
        list.forEachIndexed { idx, v ->
            editor.putString("v_${idx}_name", v.username)
            editor.putString("v_${idx}_json", v.rawJson)
            editor.putLong("v_${idx}_paste", v.pastedAtEpochSeconds)
        }
        editor.apply()
    }

    LaunchedEffect(Unit) {
        val count = prefs.getInt(KEY_VILLAGE_COUNT, 0)
        val alreadyInitialized = prefs.getBoolean(KEY_SAMPLES_INITIALIZED, false)
        val loaded = mutableListOf<VillageData>()
        val nowSec = System.currentTimeMillis() / 1000L
        for (i in 0 until count) {
            val name = prefs.getString("v_${i}_name", null)
            val json = prefs.getString("v_${i}_json", null)
            val pasteSec = prefs.getLong("v_${i}_paste", nowSec)
            if (json.isNullOrBlank()) {
                if (!name.isNullOrBlank()) {
                    loaded.add(createEmptyVillage(name))
                }
            } else {
                val res = CocVillageParser.parse(json, pasteSec, name)
                if (res is ParseResult.Success) {
                    loaded.add(res.village)
                }
            }
        }
        if (loaded.isEmpty() && !alreadyInitialized) {
            val initialPasteSec = nowSec - 180L
            val sample1 = CocVillageParser.parse(
                CocVillageParser.generateSampleVillageJson(nowSec),
                initialPasteSec,
                "Chief Asir"
            )
            val sample2 = CocVillageParser.parse(
                CocVillageParser.generateSecondSampleVillageJson(nowSec),
                initialPasteSec,
                "Mini Village"
            )
            if (sample1 is ParseResult.Success) loaded.add(sample1.village)
            if (sample2 is ParseResult.Success) loaded.add(sample2.village)
            saveVillagesToPrefs(loaded, 0)
        }
        villages = loaded
        currentIndex = prefs.getInt(KEY_SELECTED_INDEX, 0).coerceIn(0, (loaded.size - 1).coerceAtLeast(0))
    }

    val safeIndex = currentIndex.coerceIn(0, (villages.size - 1).coerceAtLeast(0))

    fun handlePasteForVillage(villageIdx: Int) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clipText = clipboard?.primaryClip
            ?.takeIf { it.itemCount > 0 }
            ?.getItemAt(0)
            ?.coerceToText(context)
            ?.toString()

        if (clipText.isNullOrBlank()) {
            Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
            return
        }

        val nowSec = System.currentTimeMillis() / 1000L
        val preservedName = villages.getOrNull(villageIdx)?.username
        when (val result = CocVillageParser.parse(clipText, nowSec, preservedName)) {
            is ParseResult.Success -> {
                val parsedVillage = result.village
                val updatedList = villages.toMutableList()
                val targetIdx = if (villageIdx in updatedList.indices) {
                    updatedList[villageIdx] = parsedVillage
                    villageIdx
                } else {
                    updatedList.add(parsedVillage)
                    updatedList.lastIndex
                }
                villages = updatedList
                currentIndex = targetIdx
                saveVillagesToPrefs(updatedList, targetIdx)
                Toast.makeText(context, "Updated ${parsedVillage.username}", Toast.LENGTH_SHORT).show()
            }
            is ParseResult.Error -> {
                Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun deleteVillageAt(indexToDelete: Int) {
        if (indexToDelete !in villages.indices) return
        val removedName = villages[indexToDelete].username
        val updatedList = villages.toMutableList().also { it.removeAt(indexToDelete) }
        if (updatedList.isEmpty()) {
            villages = emptyList()
            currentIndex = 0
            saveVillagesToPrefs(emptyList(), 0)
            isHomePage = true
        } else {
            val nextIdx = indexToDelete % updatedList.size
            villages = updatedList
            currentIndex = nextIdx
            saveVillagesToPrefs(updatedList, nextIdx)
        }
        Toast.makeText(context, "Deleted $removedName", Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBackground
    ) { innerPadding ->
        val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        val topPad = statusBarTop + 12.dp
        val bottomPad = maxOf(innerPadding.calculateBottomPadding(), navBarBottom) + 20.dp

        if (isHomePage || villages.isEmpty()) {
            HomePageContent(
                villages = villages,
                nowEpochSeconds = nowEpochSeconds,
                topPadding = topPad,
                bottomPadding = bottomPad,
                onSelectVillage = { idx ->
                    currentIndex = idx
                    saveVillagesToPrefs(villages, idx)
                    isHomePage = false
                },
                onSwipeToDetail = {
                    if (villages.isNotEmpty()) {
                        isHomePage = false
                    }
                },
                onAddVillageClick = {
                    showAddVillageDialog = true
                },
                onDeleteVillageClick = { idx ->
                    pendingDeleteIndex = idx
                }
            )
        } else {
            val villageCount = villages.size
            val pagerPageCount = if (villageCount > 1) LOOP_PAGER_COUNT else 1
            val initialPagerPage = remember(villageCount) {
                if (villageCount > 1) {
                    val mid = LOOP_PAGER_COUNT / 2
                    val alignedBase = mid - (mid % villageCount)
                    alignedBase + safeIndex
                } else {
                    0
                }
            }

            val pagerState = rememberPagerState(
                initialPage = initialPagerPage,
                pageCount = { pagerPageCount }
            )

            // Sync currentIndex whenever the user finishes swiping to a new page
            LaunchedEffect(pagerState.settledPage, villageCount) {
                if (villageCount > 0) {
                    val resolvedIdx = Math.floorMod(pagerState.settledPage, villageCount)
                    if (resolvedIdx != currentIndex) {
                        currentIndex = resolvedIdx
                        saveVillagesToPrefs(villages, resolvedIdx)
                    }
                }
            }

            // Only way to go back home is by clicking the Home icon; edge back-swipe rotates villages
            BackHandler(enabled = !isHomePage) {
                if (villageCount > 1) {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val vIdx = Math.floorMod(page, villages.size)
                val pageVillage = villages.getOrNull(vIdx)

                if (pageVillage != null) {
                    VillageDetailPage(
                        village = pageVillage,
                        totalVillages = villages.size,
                        nowEpochSeconds = nowEpochSeconds,
                        topPadding = topPad,
                        bottomPadding = bottomPad,
                        onHomeClick = { isHomePage = true },
                        onNextVillage = {
                            if (villages.size > 1) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                        },
                        onPasteClick = { handlePasteForVillage(vIdx) },
                        onDeleteClick = { pendingDeleteIndex = vIdx }
                    )
                }
            }
        }
    }

    if (showAddVillageDialog) {
        AddVillageDialog(
            defaultName = "Village ${villages.size + 1}",
            onDismiss = { showAddVillageDialog = false },
            onSave = { enteredName, rawJson ->
                val finalName = enteredName.trim().ifEmpty { "Village ${villages.size + 1}" }
                val nowSec = System.currentTimeMillis() / 1000L
                when (val result = CocVillageParser.parse(rawJson, nowSec, finalName)) {
                    is ParseResult.Success -> {
                        val newVillage = result.village.copy(username = finalName)
                        val updatedList = villages.toMutableList().apply { add(newVillage) }
                        val newIdx = updatedList.lastIndex
                        villages = updatedList
                        currentIndex = newIdx
                        saveVillagesToPrefs(updatedList, newIdx)
                        showAddVillageDialog = false
                        isHomePage = false
                        Toast.makeText(context, "Added ${newVillage.username}", Toast.LENGTH_SHORT).show()
                        null
                    }
                    is ParseResult.Error -> result.message
                }
            }
        )
    }

    val deleteIdx = pendingDeleteIndex
    if (deleteIdx != null && deleteIdx in villages.indices) {
        val targetVillage = villages[deleteIdx]
        DeleteVillageConfirmDialog(
            villageName = targetVillage.username,
            onDismiss = { pendingDeleteIndex = null },
            onConfirmDelete = {
                pendingDeleteIndex = null
                deleteVillageAt(deleteIdx)
            }
        )
    }
}

@Composable
private fun VillageDetailPage(
    village: VillageData,
    totalVillages: Int,
    nowEpochSeconds: Long,
    topPadding: Dp,
    bottomPadding: Dp,
    onHomeClick: () -> Unit,
    onNextVillage: () -> Unit,
    onPasteClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                start = 18.dp,
                end = 18.dp,
                top = topPadding,
                bottom = bottomPadding
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Top Row: [Home button]  village name  [->]
        VillageHeaderRow(
            villageName = village.username,
            totalVillages = totalVillages,
            onHomeClick = onHomeClick,
            onNextVillage = onNextVillage
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Second Row: "last updated: 3m ago" + [delete] + [paste update] buttons
        val lastUpdatedText = if (village.pastedAtEpochSeconds > 0L) {
            val elapsed = max(0L, nowEpochSeconds - village.pastedAtEpochSeconds)
            "last updated: ${TimeFormatters.formatRelativeAgo(elapsed)}"
        } else {
            "last updated: never"
        }

        LastUpdatedAndPasteRow(
            lastUpdatedText = lastUpdatedText,
            onPasteClick = onPasteClick,
            onDeleteClick = onDeleteClick
        )

        Spacer(modifier = Modifier.height(18.dp))

        val baseEpoch = village.effectiveBaseEpochSeconds

        // 3. Earliest free / already free box (checks both Townhall and Builder Base)
        EarliestFreeOrAlreadyFreeBox(
            village = village,
            baseEpochSeconds = baseEpoch,
            nowEpochSeconds = nowEpochSeconds
        )

        Spacer(modifier = Modifier.height(18.dp))

        // 4. Townhall section (builders + lab in one card)
        BaseSectionCard(
            title = "Townhall",
            totalBuilders = village.totalHomeBuilders,
            builderUpgrades = village.builderUpgrades,
            labUpgrades = village.labUpgrades,
            baseEpochSeconds = baseEpoch,
            nowEpochSeconds = nowEpochSeconds
        )

        Spacer(modifier = Modifier.height(18.dp))

        // 5. Builder Base section (builders + lab in one card)
        BaseSectionCard(
            title = "Builder Base",
            totalBuilders = village.totalBuilderBaseBuilders,
            builderUpgrades = village.builderBaseUpgrades,
            labUpgrades = village.starLabUpgrades,
            baseEpochSeconds = baseEpoch,
            nowEpochSeconds = nowEpochSeconds
        )
    }
}

@Composable
private fun HomePageContent(
    villages: List<VillageData>,
    nowEpochSeconds: Long,
    topPadding: Dp,
    bottomPadding: Dp,
    onSelectVillage: (Int) -> Unit,
    onSwipeToDetail: () -> Unit,
    onAddVillageClick: () -> Unit,
    onDeleteVillageClick: (Int) -> Unit
) {
    var cumulativeDragX by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
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
                start = 18.dp,
                end = 18.dp,
                top = topPadding,
                bottom = bottomPadding
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top header box: "COC Upgrade Tracker"
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = DarkSurface,
            border = BorderStroke(1.5.dp, DarkBorder)
        ) {
            Text(
                text = "COC Upgrade Tracker",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = ClashGold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp, horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Right-aligned "Add village (+)" button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onAddVillageClick() }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Add village",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = CircleShape,
                    color = DarkSurface,
                    border = BorderStroke(1.5.dp, ClashGold),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "+",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ClashGold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (villages.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Text(
                    text = "No villages added yet. Tap \"Add village +\" to add one.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 28.dp, horizontal = 18.dp)
                )
            }
        } else {
            // List of village cards
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                villages.forEachIndexed { idx, village ->
                    VillageSummaryRowCard(
                        village = village,
                        nowEpochSeconds = nowEpochSeconds,
                        onClick = { onSelectVillage(idx) },
                        onDeleteClick = { onDeleteVillageClick(idx) }
                    )
                }
            }
        }
    }
}

@Composable
private fun VillageSummaryRowCard(
    village: VillageData,
    nowEpochSeconds: Long,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val baseEpochSeconds = village.effectiveBaseEpochSeconds
    val freeSlots = getFreeSlotDescriptions(village, baseEpochSeconds, nowEpochSeconds)
    val allActiveUpgrades = (
        village.builderUpgrades +
            village.labUpgrades +
            village.builderBaseUpgrades +
            village.starLabUpgrades
        ).filter { it.remainingSeconds(baseEpochSeconds, nowEpochSeconds) > 0L }
    val earliest = allActiveUpgrades.minByOrNull { it.finishEpochSeconds(baseEpochSeconds) }

    val hasAnyFree = freeSlots.isNotEmpty()
    val statusText: String
    val statusSubtext: String?
    val statusColor: Color

    if (village.pastedAtEpochSeconds == 0L && village.rawJson.isBlank()) {
        statusText = "Tap to paste"
        statusSubtext = "No data yet"
        statusColor = TextSecondary
    } else if (hasAnyFree) {
        statusText = "Already free"
        statusSubtext = freeSlots.first()
        statusColor = BuilderEmerald
    } else if (earliest != null) {
        val finishEpoch = earliest.finishEpochSeconds(baseEpochSeconds)
        val remaining = earliest.remainingSeconds(baseEpochSeconds, nowEpochSeconds)
        statusText = TimeFormatters.formatShortClockTime(finishEpoch, nowEpochSeconds)
        statusSubtext = TimeFormatters.formatDuration(remaining)
        statusColor = ClashGold
    } else {
        statusText = "Already free"
        statusSubtext = null
        statusColor = BuilderEmerald
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.2.dp, DarkBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = village.username,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = statusColor
                    )
                    if (statusSubtext != null) {
                        Text(
                            text = statusSubtext,
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            color = TextSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DarkBackground,
                    border = BorderStroke(1.dp, TrapCoral.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onDeleteClick() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        TrashCanIcon(
                            color = TrapCoral,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeleteVillageConfirmDialog(
    villageName: String,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        titleContentColor = TextPrimary,
        textContentColor = TextPrimary,
        title = {
            Text(
                text = "Delete Village",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Text(
                text = "Are you sure you want to delete \"$villageName\"?",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmDelete,
                colors = ButtonDefaults.buttonColors(
                    containerColor = TrapCoral,
                    contentColor = Color.White
                )
            ) {
                Text("Delete", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun AddVillageDialog(
    defaultName: String,
    onDismiss: () -> Unit,
    onSave: (name: String, rawJson: String) -> String?
) {
    val context = LocalContext.current
    var nameText by rememberSaveable { mutableStateOf("") }
    var jsonText by rememberSaveable { mutableStateOf("") }
    var errorMsg by rememberSaveable { mutableStateOf<String?>(null) }

    fun readClipboardText(): String? {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        return clipboard?.primaryClip
            ?.takeIf { it.itemCount > 0 }
            ?.getItemAt(0)
            ?.coerceToText(context)
            ?.toString()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        titleContentColor = TextPrimary,
        textContentColor = TextPrimary,
        title = {
            Text(
                text = "Add Village",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("Village Name") },
                    placeholder = { Text(defaultName, color = TextSecondary.copy(alpha = 0.6f)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ClashGold,
                        unfocusedBorderColor = DarkBorder,
                        focusedLabelColor = ClashGold,
                        unfocusedLabelColor = TextSecondary
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Village JSON Data",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = DarkBackground,
                        border = BorderStroke(1.dp, ClashGold),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                val clip = readClipboardText()
                                if (!clip.isNullOrBlank()) {
                                    jsonText = clip
                                    errorMsg = null
                                } else {
                                    errorMsg = "Clipboard is empty"
                                }
                            }
                    ) {
                        Text(
                            text = "Paste Clipboard",
                            color = ClashGold,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = jsonText,
                    onValueChange = {
                        jsonText = it
                        errorMsg = null
                    },
                    placeholder = {
                        Text(
                            text = """{"tag": "#...", "timestamp": 1730000000, "buildings": [...]}""",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = TextSecondary.copy(alpha = 0.6f)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ClashGold,
                        unfocusedBorderColor = DarkBorder
                    )
                )

                if (errorMsg != null) {
                    Text(
                        text = errorMsg!!,
                        color = Color(0xFFFCA5A5),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val effectiveJson = if (jsonText.isBlank()) {
                        readClipboardText().orEmpty().also {
                            if (it.isNotBlank()) jsonText = it
                        }
                    } else {
                        jsonText
                    }
                    val err = onSave(nameText.ifBlank { defaultName }, effectiveJson)
                    if (err != null) {
                        errorMsg = err
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ClashGold,
                    contentColor = Color(0xFF1A1200)
                )
            ) {
                Text("Add", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun VillageHeaderRow(
    villageName: String,
    totalVillages: Int,
    onHomeClick: () -> Unit,
    onNextVillage: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Top-left Home button (only way to go back home)
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = DarkSurface,
            border = BorderStroke(1.dp, DarkBorder),
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable { onHomeClick() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                HomeHouseIcon(
                    color = ClashGold,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Text(
            text = villageName,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        )

        // Top-right Next button to rotate between villages
        Surface(
            shape = CircleShape,
            color = DarkSurface,
            border = BorderStroke(1.dp, DarkBorder),
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .clickable(enabled = totalVillages > 1) { onNextVillage() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "→",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (totalVillages > 1) ClashGold else TextSecondary.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@Composable
private fun HomeHouseIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = w * 0.1f

        val roofPath = Path().apply {
            moveTo(w * 0.5f, h * 0.1f)
            lineTo(w * 0.1f, h * 0.48f)
            lineTo(w * 0.9f, h * 0.48f)
            close()
        }
        drawPath(
            path = roofPath,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        val bodyPath = Path().apply {
            moveTo(w * 0.22f, h * 0.48f)
            lineTo(w * 0.22f, h * 0.88f)
            lineTo(w * 0.78f, h * 0.88f)
            lineTo(w * 0.78f, h * 0.48f)
        }
        drawPath(
            path = bodyPath,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        drawRect(
            color = color,
            topLeft = Offset(w * 0.42f, h * 0.62f),
            size = Size(w * 0.16f, h * 0.26f),
            style = Stroke(width = strokeWidth * 0.85f, join = StrokeJoin.Round)
        )
    }
}

@Composable
private fun TrashCanIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = w * 0.1f

        // Lid line
        drawLine(
            color = color,
            start = Offset(w * 0.15f, h * 0.22f),
            end = Offset(w * 0.85f, h * 0.22f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        // Lid handle
        drawLine(
            color = color,
            start = Offset(w * 0.36f, h * 0.12f),
            end = Offset(w * 0.64f, h * 0.12f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        // Can body
        val bodyPath = Path().apply {
            moveTo(w * 0.24f, h * 0.26f)
            lineTo(w * 0.3f, h * 0.88f)
            lineTo(w * 0.7f, h * 0.88f)
            lineTo(w * 0.76f, h * 0.26f)
        }
        drawPath(
            path = bodyPath,
            color = color,
            style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
private fun LastUpdatedAndPasteRow(
    lastUpdatedText: String,
    onPasteClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = lastUpdatedText,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = TextSecondary,
            modifier = Modifier.weight(1f)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = DarkSurface,
                border = BorderStroke(1.2.dp, TrapCoral.copy(alpha = 0.75f)),
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onDeleteClick() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    TrashCanIcon(
                        color = TrapCoral,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "delete",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = TrapCoral
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = DarkSurface,
                border = BorderStroke(1.2.dp, ClashGold),
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onPasteClick() }
            ) {
                Text(
                    text = "paste update",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = ClashGold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    }
}

private fun getFreeSlotDescriptions(
    village: VillageData,
    baseEpochSeconds: Long,
    nowEpochSeconds: Long
): List<String> {
    val activeThBuilderCount = village.builderUpgrades.count {
        !it.isGoblinHelper && it.remainingSeconds(baseEpochSeconds, nowEpochSeconds) > 0L
    }
    val freeThBuilders = max(0, village.totalHomeBuilders - activeThBuilderCount)

    val activeThLabCount = village.labUpgrades.count {
        !it.isGoblinHelper && it.remainingSeconds(baseEpochSeconds, nowEpochSeconds) > 0L
    }
    val isThLabFree = activeThLabCount == 0

    val activeBbBuilderCount = village.builderBaseUpgrades.count {
        !it.isGoblinHelper && it.remainingSeconds(baseEpochSeconds, nowEpochSeconds) > 0L
    }
    val freeBbBuilders = max(0, village.totalBuilderBaseBuilders - activeBbBuilderCount)

    val activeBbLabCount = village.starLabUpgrades.count {
        !it.isGoblinHelper && it.remainingSeconds(baseEpochSeconds, nowEpochSeconds) > 0L
    }
    val isBbLabFree = activeBbLabCount == 0

    return buildList {
        if (freeThBuilders > 0) {
            add(
                if (freeThBuilders == 1) "Townhall Builder"
                else "$freeThBuilders Townhall Builders"
            )
        }
        if (isThLabFree) {
            add("Townhall Lab")
        }
        if (freeBbBuilders > 0) {
            add(
                if (freeBbBuilders == 1) "Builder Base Builder"
                else "$freeBbBuilders Builder Base Builders"
            )
        }
        if (isBbLabFree) {
            add("Builder Base Lab")
        }
    }
}

@Composable
private fun EarliestFreeOrAlreadyFreeBox(
    village: VillageData,
    baseEpochSeconds: Long,
    nowEpochSeconds: Long
) {
    val freeSlotDescriptions = getFreeSlotDescriptions(village, baseEpochSeconds, nowEpochSeconds)

    val allActiveUpgrades = (
        village.builderUpgrades +
            village.labUpgrades +
            village.builderBaseUpgrades +
            village.starLabUpgrades
        ).filter { it.remainingSeconds(baseEpochSeconds, nowEpochSeconds) > 0L }

    val earliest = allActiveUpgrades.minByOrNull { it.finishEpochSeconds(baseEpochSeconds) }

    val hasAnyFree = freeSlotDescriptions.isNotEmpty()
    val borderColor = when {
        hasAnyFree || earliest == null -> BuilderEmerald
        earliest.isLabCategory -> ElixirPurple
        else -> ClashGold
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.5.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            if (hasAnyFree) {
                Text(
                    text = "Already free: ${freeSlotDescriptions.joinToString(", ")}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = BuilderEmerald
                )
                if (earliest != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    val finishEpoch = earliest.finishEpochSeconds(baseEpochSeconds)
                    val remaining = earliest.remainingSeconds(baseEpochSeconds, nowEpochSeconds)
                    val formattedClock = TimeFormatters.formatShortClockTime(finishEpoch, nowEpochSeconds)
                    Text(
                        text = "Next finishing: ${earliest.name} in ${TimeFormatters.formatDuration(remaining)} ($formattedClock)",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            } else if (earliest != null) {
                val finishEpoch = earliest.finishEpochSeconds(baseEpochSeconds)
                val remaining = earliest.remainingSeconds(baseEpochSeconds, nowEpochSeconds)
                val formattedClock = TimeFormatters.formatShortClockTime(finishEpoch, nowEpochSeconds)
                val sourceLabel = when (earliest.category) {
                    UpgradeCategory.BUILDER_BASE_BUILDING -> "Builder Base • Builder"
                    UpgradeCategory.BUILDER_BASE_RESEARCH -> "Builder Base • Lab"
                    UpgradeCategory.TROOP, UpgradeCategory.SPELL, UpgradeCategory.SIEGE -> "Townhall • Lab"
                    else -> "Townhall • Builder"
                }

                Text(
                    text = "Earliest free: $formattedClock",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$sourceLabel • ${earliest.name} (${earliest.levelTransitionText})",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = borderColor,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = TimeFormatters.formatDuration(remaining),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = TextSecondary
                    )
                }
            } else {
                Text(
                    text = "Already free: All Builders & Labs",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = BuilderEmerald
                )
            }
        }
    }
}

@Composable
private fun BaseSectionCard(
    title: String,
    totalBuilders: Int,
    builderUpgrades: List<UpgradeItem>,
    labUpgrades: List<UpgradeItem>,
    baseEpochSeconds: Long,
    nowEpochSeconds: Long
) {
    val regularBuilders = builderUpgrades.filter { !it.isGoblinHelper }
    val goblinBuilders = builderUpgrades.filter { it.isGoblinHelper }
    val builderSlotCount = max(totalBuilders, regularBuilders.size)

    val regularLab = labUpgrades.firstOrNull { !it.isGoblinHelper }
    val goblinLabs = labUpgrades.filter { it.isGoblinHelper }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.2.dp, DarkBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Builder slots (builder 1 upgrade, builder 2 upgrade, ...)
            for (slotIdx in 0 until builderSlotCount) {
                val builderNumber = slotIdx + 1
                val upgrade = regularBuilders.getOrNull(slotIdx)

                UpgradeSlotRow(
                    slotLabel = "Builder $builderNumber",
                    upgrade = upgrade,
                    baseEpochSeconds = baseEpochSeconds,
                    nowEpochSeconds = nowEpochSeconds,
                    accentColor = ClashGold
                )

                HorizontalDivider(color = DarkBorder.copy(alpha = 0.5f))
            }

            // Extra Goblin builder upgrades (if any active)
            goblinBuilders.forEach { goblinUpgrade ->
                UpgradeSlotRow(
                    slotLabel = "Goblin Builder",
                    upgrade = goblinUpgrade,
                    baseEpochSeconds = baseEpochSeconds,
                    nowEpochSeconds = nowEpochSeconds,
                    accentColor = GoblinGreen
                )
                HorizontalDivider(color = DarkBorder.copy(alpha = 0.5f))
            }

            // Lab upgrade slot
            UpgradeSlotRow(
                slotLabel = "Lab",
                upgrade = regularLab,
                baseEpochSeconds = baseEpochSeconds,
                nowEpochSeconds = nowEpochSeconds,
                accentColor = ElixirPurple
            )

            // Extra Goblin lab research (if any active)
            goblinLabs.forEach { goblinLab ->
                HorizontalDivider(color = DarkBorder.copy(alpha = 0.5f))
                UpgradeSlotRow(
                    slotLabel = "Goblin Lab",
                    upgrade = goblinLab,
                    baseEpochSeconds = baseEpochSeconds,
                    nowEpochSeconds = nowEpochSeconds,
                    accentColor = GoblinGreen
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
    nowEpochSeconds: Long,
    accentColor: Color
) {
    val remaining = upgrade?.remainingSeconds(baseEpochSeconds, nowEpochSeconds) ?: 0L

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 9.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 10.dp)
        ) {
            if (upgrade != null && remaining > 0L) {
                Text(
                    text = "$slotLabel • ${upgrade.name}",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary
                )
                Text(
                    text = upgrade.levelTransitionText,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            } else if (upgrade != null) {
                Text(
                    text = slotLabel,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary
                )
                Text(
                    text = "Finished: ${upgrade.name} (${upgrade.levelTransitionText})",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            } else {
                Text(
                    text = slotLabel,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary
                )
                Text(
                    text = "Idle / Ready to upgrade",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        if (upgrade != null && remaining > 0L) {
            val finishEpoch = upgrade.finishEpochSeconds(baseEpochSeconds)
            val exactClock = TimeFormatters.formatShortClockTime(finishEpoch, nowEpochSeconds)
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = TimeFormatters.formatDuration(remaining),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = accentColor
                )
                Text(
                    text = exactClock,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        } else {
            Text(
                text = "Free",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                ),
                color = BuilderEmerald
            )
        }
    }
}
