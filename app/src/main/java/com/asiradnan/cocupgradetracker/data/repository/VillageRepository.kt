package com.asiradnan.cocupgradetracker.data.repository

import android.content.Context
import com.asiradnan.cocupgradetracker.data.CocVillageParser
import com.asiradnan.cocupgradetracker.data.ParseResult
import com.asiradnan.cocupgradetracker.data.VillageData

data class StoredVillagesSnapshot(
    val villages: List<VillageData>,
    val selectedIndex: Int
)

class VillageRepository(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadVillages(): StoredVillagesSnapshot {
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
                    loaded.add(VillageData.createEmpty(name))
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
            saveVillages(loaded, 0)
        }

        val selectedIdx = prefs.getInt(KEY_SELECTED_INDEX, 0)
            .coerceIn(0, (loaded.size - 1).coerceAtLeast(0))

        return StoredVillagesSnapshot(
            villages = loaded,
            selectedIndex = selectedIdx
        )
    }

    fun saveVillages(list: List<VillageData>, selectedIndex: Int) {
        val editor = prefs.edit().clear()
        editor.putBoolean(KEY_SAMPLES_INITIALIZED, true)
        editor.putInt(KEY_VILLAGE_COUNT, list.size)
        editor.putInt(KEY_SELECTED_INDEX, selectedIndex)
        list.forEachIndexed { idx, v ->
            editor.putString("v_${idx}_name", v.username)
            editor.putString("v_${idx}_json", v.rawJson)
            editor.putLong("v_${idx}_paste", v.pastedAtEpochSeconds)
        }
        editor.apply()
    }

    companion object {
        private const val PREFS_NAME = "coc_upgrade_tracker_prefs_v2"
        private const val KEY_VILLAGE_COUNT = "village_count"
        private const val KEY_SELECTED_INDEX = "selected_village_index"
        private const val KEY_SAMPLES_INITIALIZED = "samples_initialized"
    }
}

