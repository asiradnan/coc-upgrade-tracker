package com.asiradnan.cocupgradetracker.ui

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider
import com.asiradnan.cocupgradetracker.data.repository.VillageRepository
import com.asiradnan.cocupgradetracker.ui.components.AddVillageDialog
import com.asiradnan.cocupgradetracker.ui.components.ClashBevelButton
import com.asiradnan.cocupgradetracker.ui.components.ClashButtonStyle
import com.asiradnan.cocupgradetracker.ui.components.DeleteVillageConfirmDialog
import com.asiradnan.cocupgradetracker.ui.components.HomeTopAppBar
import com.asiradnan.cocupgradetracker.ui.components.VillageDetailTopAppBar
import com.asiradnan.cocupgradetracker.ui.detail.VillageDetailScreen
import com.asiradnan.cocupgradetracker.ui.home.HomeScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val LOOP_PAGER_COUNT = 10_000

@Composable
fun TrackerScreen() {
    val context = LocalContext.current
    val viewModel = remember(context) {
        val activity = context as? ComponentActivity
        if (activity != null) {
            ViewModelProvider(activity, TrackerViewModel.Factory(context))[TrackerViewModel::class.java]
        } else {
            TrackerViewModel(VillageRepository(context))
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var nowEpochSeconds by remember { mutableLongStateOf(System.currentTimeMillis() / 1000L) }
    LaunchedEffect(Unit) {
        while (true) {
            nowEpochSeconds = System.currentTimeMillis() / 1000L
            delay(1000L)
        }
    }

    val villages = uiState.villages
    val safeIndex = uiState.safeIndex
    val currentVillage = uiState.currentVillage
    val isHomePage = uiState.isHomePage

    fun readClipboardText(): String? {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        return clipboard?.primaryClip
            ?.takeIf { it.itemCount > 0 }
            ?.getItemAt(0)
            ?.coerceToText(context)
            ?.toString()
    }

    fun showOutcomeToast(outcome: PasteOutcome) {
        val msg = when (outcome) {
            is PasteOutcome.Success -> outcome.message
            is PasteOutcome.Error -> outcome.message
        }
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    val activity = context as? ComponentActivity
    val lifecycle = activity?.lifecycle

    // Auto-detect CoC export JSON in clipboard and auto-paste when app opens or returns from Clash of Clans
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val clip = readClipboardText()
                val nowSec = System.currentTimeMillis() / 1000L
                val outcome = viewModel.autoPasteClipboardIfNew(clip, nowSec)
                if (outcome is PasteOutcome.Success) {
                    Toast.makeText(context, outcome.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
        lifecycle?.addObserver(observer)
        onDispose {
            lifecycle?.removeObserver(observer)
        }
    }

    val villageCount = villages.size
    val pagerPageCount = if (villageCount > 1) LOOP_PAGER_COUNT else 1
    val initialPagerPage = remember(villageCount, isHomePage) {
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

    // 1. Track when we transition from Home -> Detail to snap immediately without animation
    var wasHomePage by remember { mutableStateOf(isHomePage) }

    LaunchedEffect(safeIndex, isHomePage, villageCount) {
        if (!isHomePage && villageCount > 1) {
            val currentMod = Math.floorMod(pagerState.currentPage, villageCount)
            if (currentMod != safeIndex) {
                val forwardDelta = Math.floorMod(safeIndex - currentMod, villageCount)
                val targetPage = pagerState.currentPage + forwardDelta
                if (wasHomePage) {
                    // Coming from Home screen: snap instantly so settledPage updates immediately
                    pagerState.scrollToPage(targetPage)
                } else if (!pagerState.isScrollInProgress) {
                    // Already on Detail screen (e.g. top bar selection or paste update): animate
                    pagerState.animateScrollToPage(targetPage)
                }
            }
        }
        wasHomePage = isHomePage
    }

    // 2. Only sync pager -> ViewModel when the pager settles AND the page matches user swipe
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .collect { settledPage ->
                if (!uiState.isHomePage && uiState.villages.isNotEmpty()) {
                    val resolvedIdx = Math.floorMod(settledPage, uiState.villages.size)
                    viewModel.onPageSettled(resolvedIdx)
                }
            }
    }

    val showingHome = isHomePage || villages.isEmpty()
    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colorScheme.background,
        topBar = {
            if (showingHome) {
                HomeTopAppBar(
                    villageCount = villages.size,
                    onAddVillageClick = { viewModel.setShowAddVillageDialog(true) }
                )
            } else {
                VillageDetailTopAppBar(
                    villageName = currentVillage?.username ?: "Village",
                    selectedIndex = safeIndex,
                    totalVillages = villages.size,
                    onHomeClick = { viewModel.navigateHome() },
                    onNextVillageClick = {
                        if (villages.size > 1) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    onSelectVillageClick = { targetIdx ->
                        if (villages.size > 1 && targetIdx in villages.indices) {
                            viewModel.selectVillage(targetIdx)
                            val currentMod = Math.floorMod(pagerState.currentPage, villages.size)
                            if (currentMod != targetIdx) {
                                val forwardDelta = Math.floorMod(targetIdx - currentMod, villages.size)
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + forwardDelta)
                                }
                            }
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (showingHome) {
                ClashBevelButton(
                    text = "Paste Update",
                    onClick = {
                        val nowSec = System.currentTimeMillis() / 1000L
                        val outcome = viewModel.pasteUpdateIntelligent(
                            clipText = readClipboardText(),
                            nowEpochSeconds = nowSec
                        )
                        showOutcomeToast(outcome)
                    },
                    style = ClashButtonStyle.GREEN,
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                )
            }
        }
    ) { innerPadding ->
        if (showingHome) {
            HomeScreen(
                villages = villages,
                nowEpochSeconds = nowEpochSeconds,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                onSelectVillage = { idx -> viewModel.selectVillage(idx) },
                onSwipeToDetail = { viewModel.openDetailFromSwipe() },
                onDeleteVillageClick = { idx -> viewModel.requestDeleteVillage(idx) },
                onPasteFromClipboardClick = {
                    val nowSec = System.currentTimeMillis() / 1000L
                    val outcome = viewModel.pasteUpdateIntelligent(
                        clipText = readClipboardText(),
                        nowEpochSeconds = nowSec
                    )
                    showOutcomeToast(outcome)
                }
            )
        } else {
            BackHandler(enabled = true) {
                viewModel.navigateHome()
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) { page ->
                val vIdx = Math.floorMod(page, villages.size)
                val pageVillage = villages.getOrNull(vIdx)

                if (pageVillage != null) {
                    VillageDetailScreen(
                        village = pageVillage,
                        nowEpochSeconds = nowEpochSeconds,
                        onPasteClick = {
                            val nowSec = System.currentTimeMillis() / 1000L
                            val outcome = viewModel.pasteUpdateIntelligent(
                                clipText = readClipboardText(),
                                nowEpochSeconds = nowSec,
                                preferredVillageIdx = vIdx
                            )
                            showOutcomeToast(outcome)
                        },
                        onDeleteClick = { viewModel.requestDeleteVillage(vIdx) }
                    )
                }
            }
        }
    }

    if (uiState.showAddVillageDialog) {
        AddVillageDialog(
            defaultName = "Village ${villages.size + 1}",
            onDismiss = { viewModel.setShowAddVillageDialog(false) },
            onSave = { enteredName, rawJson ->
                val nowSec = System.currentTimeMillis() / 1000L
                when (val outcome = viewModel.addVillage(enteredName, rawJson, nowSec)) {
                    is PasteOutcome.Success -> {
                        Toast.makeText(context, outcome.message, Toast.LENGTH_SHORT).show()
                        null
                    }
                    is PasteOutcome.Error -> outcome.message
                }
            }
        )
    }

    val deleteIdx = uiState.pendingDeleteIndex
    if (deleteIdx != null && deleteIdx in villages.indices) {
        val targetVillage = villages[deleteIdx]
        DeleteVillageConfirmDialog(
            villageName = targetVillage.username,
            onDismiss = { viewModel.requestDeleteVillage(null) },
            onConfirmDelete = {
                val removedName = viewModel.confirmDeleteVillage(deleteIdx)
                if (removedName != null) {
                    Toast.makeText(context, "Deleted $removedName", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}
