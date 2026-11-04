package com.asiradnan.cocupgradetracker.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.asiradnan.cocupgradetracker.data.CocVillageParser
import com.asiradnan.cocupgradetracker.data.ParseResult
import com.asiradnan.cocupgradetracker.data.VillageData
import com.asiradnan.cocupgradetracker.data.repository.VillageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class TrackerUiState(
    val villages: List<VillageData> = emptyList(),
    val currentIndex: Int = 0,
    val isHomePage: Boolean = true,
    val showAddVillageDialog: Boolean = false,
    val pendingDeleteIndex: Int? = null
) {
    val safeIndex: Int
        get() = currentIndex.coerceIn(0, (villages.size - 1).coerceAtLeast(0))

    val currentVillage: VillageData?
        get() = villages.getOrNull(safeIndex)
}

sealed class PasteOutcome {
    data class Success(val message: String) : PasteOutcome()
    data class Error(val message: String) : PasteOutcome()
}

class TrackerViewModel(
    private val repository: VillageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrackerUiState())
    val uiState: StateFlow<TrackerUiState> = _uiState.asStateFlow()

    init {
        val snapshot = repository.loadVillages()
        _uiState.value = TrackerUiState(
            villages = snapshot.villages,
            currentIndex = snapshot.selectedIndex
        )
    }

    fun selectVillage(index: Int) {
        val currentList = _uiState.value.villages
        if (index !in currentList.indices) return
        repository.saveVillages(currentList, index)
        _uiState.update {
            it.copy(
                currentIndex = index,
                isHomePage = false
            )
        }
    }

    fun navigateHome() {
        _uiState.update { it.copy(isHomePage = true) }
    }

    fun openDetailFromSwipe() {
        if (_uiState.value.villages.isNotEmpty()) {
            _uiState.update { it.copy(isHomePage = false) }
        }
    }

    fun onPageSettled(resolvedIndex: Int) {
        val state = _uiState.value
        if (resolvedIndex in state.villages.indices && resolvedIndex != state.currentIndex) {
            repository.saveVillages(state.villages, resolvedIndex)
            _uiState.update { it.copy(currentIndex = resolvedIndex) }
        }
    }

    fun setShowAddVillageDialog(show: Boolean) {
        _uiState.update { it.copy(showAddVillageDialog = show) }
    }

    fun requestDeleteVillage(index: Int?) {
        _uiState.update { it.copy(pendingDeleteIndex = index) }
    }

    fun confirmDeleteVillage(indexToDelete: Int): String? {
        val currentList = _uiState.value.villages
        if (indexToDelete !in currentList.indices) {
            _uiState.update { it.copy(pendingDeleteIndex = null) }
            return null
        }

        val removedName = currentList[indexToDelete].username
        val updatedList = currentList.toMutableList().also { it.removeAt(indexToDelete) }

        if (updatedList.isEmpty()) {
            repository.saveVillages(emptyList(), 0)
            _uiState.update {
                it.copy(
                    villages = emptyList(),
                    currentIndex = 0,
                    isHomePage = true,
                    pendingDeleteIndex = null
                )
            }
        } else {
            val nextIdx = indexToDelete % updatedList.size
            repository.saveVillages(updatedList, nextIdx)
            _uiState.update {
                it.copy(
                    villages = updatedList,
                    currentIndex = nextIdx,
                    pendingDeleteIndex = null
                )
            }
        }
        return removedName
    }

    fun addVillage(
        enteredName: String,
        rawJson: String,
        nowEpochSeconds: Long
    ): PasteOutcome {
        val state = _uiState.value
        val trimmedCustomName = enteredName.trim()
        val fallbackName = trimmedCustomName.ifEmpty { "Village ${state.villages.size + 1}" }

        return when (val result = CocVillageParser.parse(rawJson, nowEpochSeconds, fallbackName)) {
            is ParseResult.Success -> {
                val parsed = result.village
                val updatedList = state.villages.toMutableList()
                val existingTagIdx = updatedList.indexOfFirst {
                    hasValidTag(it.tag) && it.tag.equals(parsed.tag, ignoreCase = true)
                }

                if (existingTagIdx >= 0) {
                    val existing = updatedList[existingTagIdx]
                    val resolvedName = trimmedCustomName.ifEmpty { existing.username }
                    val updatedVillage = parsed.copy(username = resolvedName)
                    updatedList[existingTagIdx] = updatedVillage
                    repository.saveVillages(updatedList, existingTagIdx)
                    _uiState.update {
                        it.copy(
                            villages = updatedList,
                            currentIndex = existingTagIdx,
                            showAddVillageDialog = false,
                            isHomePage = false
                        )
                    }
                    PasteOutcome.Success("Updated existing ${updatedVillage.username} (${parsed.tag})")
                } else {
                    val newVillage = parsed.copy(username = fallbackName)
                    updatedList.add(newVillage)
                    val newIdx = updatedList.lastIndex
                    repository.saveVillages(updatedList, newIdx)
                    _uiState.update {
                        it.copy(
                            villages = updatedList,
                            currentIndex = newIdx,
                            showAddVillageDialog = false,
                            isHomePage = false
                        )
                    }
                    PasteOutcome.Success("Added ${newVillage.username}")
                }
            }
            is ParseResult.Error -> PasteOutcome.Error(result.message)
        }
    }

    /**
     * Intelligently updates a village by matching the JSON's player tag against all saved villages,
     * preventing accidental overwrites when pasting on a different village's screen.
     */
    fun pasteUpdateIntelligent(
        clipText: String?,
        nowEpochSeconds: Long,
        preferredVillageIdx: Int? = null
    ): PasteOutcome {
        if (clipText.isNullOrBlank()) {
            return PasteOutcome.Error("Clipboard is empty")
        }

        val currentList = _uiState.value.villages
        return when (val result = CocVillageParser.parse(clipText, nowEpochSeconds)) {
            is ParseResult.Success -> {
                val parsed = result.village
                val updatedList = currentList.toMutableList()

                // 1. Always match by village tag first so we never overwrite the wrong village
                val matchingTagIdx = if (hasValidTag(parsed.tag)) {
                    updatedList.indexOfFirst { it.tag.equals(parsed.tag, ignoreCase = true) }
                } else {
                    -1
                }

                if (matchingTagIdx >= 0) {
                    val existing = updatedList[matchingTagIdx]
                    val preservedName = existing.username.ifBlank { parsed.username }
                    val updatedVillage = parsed.copy(username = preservedName)
                    updatedList[matchingTagIdx] = updatedVillage
                    repository.saveVillages(updatedList, matchingTagIdx)
                    _uiState.update {
                        it.copy(
                            villages = updatedList,
                            currentIndex = matchingTagIdx
                        )
                    }
                    return PasteOutcome.Success("Updated ${updatedVillage.username} (${parsed.tag})")
                }

                // 2. If preferred village is an uninitialized slot (#NEW), initialize it
                if (preferredVillageIdx != null && preferredVillageIdx in updatedList.indices) {
                    val preferred = updatedList[preferredVillageIdx]
                    if (!hasValidTag(preferred.tag) && preferred.rawJson.isBlank()) {
                        val preservedName = preferred.username.ifBlank { parsed.username }
                        val updatedVillage = parsed.copy(username = preservedName)
                        updatedList[preferredVillageIdx] = updatedVillage
                        repository.saveVillages(updatedList, preferredVillageIdx)
                        _uiState.update {
                            it.copy(
                                villages = updatedList,
                                currentIndex = preferredVillageIdx
                            )
                        }
                        return PasteOutcome.Success("Updated ${updatedVillage.username} (${parsed.tag})")
                    }
                }

                // 3. If any uninitialized slot (#NEW) exists, initialize it
                val uninitializedIdx = updatedList.indexOfFirst { !hasValidTag(it.tag) && it.rawJson.isBlank() }
                if (uninitializedIdx >= 0) {
                    val preservedName = updatedList[uninitializedIdx].username.ifBlank { parsed.username }
                    val updatedVillage = parsed.copy(username = preservedName)
                    updatedList[uninitializedIdx] = updatedVillage
                    repository.saveVillages(updatedList, uninitializedIdx)
                    _uiState.update {
                        it.copy(
                            villages = updatedList,
                            currentIndex = uninitializedIdx
                        )
                    }
                    return PasteOutcome.Success("Updated ${updatedVillage.username} (${parsed.tag})")
                }

                // 4. Otherwise, reject overwriting a different village's tag
                PasteOutcome.Error("No village with tag ${parsed.tag}. Use + Add to add it first.")
            }
            is ParseResult.Error -> PasteOutcome.Error(result.message)
        }
    }

    private fun hasValidTag(tag: String): Boolean =
        tag.isNotBlank() && tag != "#VILLAGE" && tag != "#NEW"

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TrackerViewModel(VillageRepository(context.applicationContext)) as T
        }
    }
}
