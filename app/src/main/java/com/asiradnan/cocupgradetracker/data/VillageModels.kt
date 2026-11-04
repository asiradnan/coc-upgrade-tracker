package com.asiradnan.cocupgradetracker.data

import kotlin.math.max

data class UpgradeItem(
    val id: String,
    val dataId: Int,
    val name: String,
    val category: UpgradeCategory,
    val currentLevel: Int,
    val targetLevel: Int,
    val durationSecondsAtExport: Long,
    val isGoblinHelper: Boolean = false,
    val isApprenticeRecurring: Boolean = false,
    val superchargeLevel: Int? = null,
    val isGearUp: Boolean = false
) {
    fun finishEpochSeconds(baseEpochSeconds: Long): Long =
        baseEpochSeconds + durationSecondsAtExport

    fun remainingSeconds(baseEpochSeconds: Long, nowEpochSeconds: Long): Long =
        max(0L, finishEpochSeconds(baseEpochSeconds) - nowEpochSeconds)

    fun progressFraction(
        baseEpochSeconds: Long,
        nowEpochSeconds: Long,
        referenceMaxSeconds: Long = durationSecondsAtExport
    ): Float {
        val remaining = remainingSeconds(baseEpochSeconds, nowEpochSeconds)
        if (remaining <= 0L) return 1f
        val baselineWindow = max(
            durationSecondsAtExport,
            (max(referenceMaxSeconds, 3600L) * 1.15f).toLong()
        )
        if (baselineWindow <= 0L) return 1f
        val ratio = 1f - (remaining.toFloat() / baselineWindow.toFloat())
        return ratio.coerceIn(0.06f, 1f)
    }

    val isLabCategory: Boolean
        get() = category == UpgradeCategory.TROOP ||
            category == UpgradeCategory.SPELL ||
            category == UpgradeCategory.SIEGE ||
            category == UpgradeCategory.BUILDER_BASE_RESEARCH

    val levelTransitionText: String
        get() = when {
            isGearUp -> "Gear Up (Lv $currentLevel)"
            superchargeLevel != null && currentLevel > 0 ->
                "Supercharge $superchargeLevel → ${superchargeLevel + 1}"
            currentLevel <= 0 -> "New → Lv 1"
            else -> "Lv $currentLevel → $targetLevel"
        }
}

data class HelperStatus(
    val dataId: Int,
    val name: String,
    val level: Int,
    val cooldownSecondsAtExport: Long
) {
    fun readyEpochSeconds(baseEpochSeconds: Long): Long =
        baseEpochSeconds + cooldownSecondsAtExport

    fun remainingCooldown(baseEpochSeconds: Long, nowEpochSeconds: Long): Long =
        max(0L, readyEpochSeconds(baseEpochSeconds) - nowEpochSeconds)
}

data class VillageData(
    val username: String,
    val tag: String,
    val exportTimestampSeconds: Long,
    val hasExplicitTimestamp: Boolean,
    val pastedAtEpochSeconds: Long,
    val townHallLevel: Int,
    val builderHallLevel: Int?,
    val totalRegularBuilders: Int,
    val hasBobBuilder: Boolean,
    val totalBuilderBaseBuilders: Int = 1,
    val builderUpgrades: List<UpgradeItem>,
    val labUpgrades: List<UpgradeItem>,
    val petUpgrades: List<UpgradeItem>,
    val builderBaseUpgrades: List<UpgradeItem>,
    val starLabUpgrades: List<UpgradeItem>,
    val helpers: List<HelperStatus>,
    val rawJson: String
) {
    val totalHomeBuilders: Int
        get() = totalRegularBuilders + if (hasBobBuilder) 1 else 0

    val totalSlotCount: Int
        get() = totalHomeBuilders + 1 + totalBuilderBaseBuilders + 1

    val effectiveBaseEpochSeconds: Long
        get() {
            val allTimers = (builderUpgrades + labUpgrades + petUpgrades + builderBaseUpgrades + starLabUpgrades)
                .map { it.durationSecondsAtExport }
            val maxTimer = allTimers.maxOrNull() ?: 0L
            return if (hasExplicitTimestamp && maxTimer > 0L && (exportTimestampSeconds + maxTimer) < pastedAtEpochSeconds) {
                pastedAtEpochSeconds
            } else {
                exportTimestampSeconds
            }
        }

    fun getFreeSlotCount(nowEpochSeconds: Long): Int {
        val baseEpoch = effectiveBaseEpochSeconds

        val activeThBuilderCount = builderUpgrades.count {
            !it.isGoblinHelper && it.remainingSeconds(baseEpoch, nowEpochSeconds) > 0L
        }
        val freeThBuilders = max(0, totalHomeBuilders - activeThBuilderCount)

        val activeThLabCount = labUpgrades.count {
            !it.isGoblinHelper && it.remainingSeconds(baseEpoch, nowEpochSeconds) > 0L
        }
        val freeThLab = if (activeThLabCount == 0) 1 else 0

        val activeBbBuilderCount = builderBaseUpgrades.count {
            !it.isGoblinHelper && it.remainingSeconds(baseEpoch, nowEpochSeconds) > 0L
        }
        val freeBbBuilders = max(0, totalBuilderBaseBuilders - activeBbBuilderCount)

        val activeBbLabCount = starLabUpgrades.count {
            !it.isGoblinHelper && it.remainingSeconds(baseEpoch, nowEpochSeconds) > 0L
        }
        val freeBbLab = if (activeBbLabCount == 0) 1 else 0

        return freeThBuilders + freeThLab + freeBbBuilders + freeBbLab
    }

    fun getFreeDurationSeconds(nowEpochSeconds: Long): Long? {
        val baseEpoch = effectiveBaseEpochSeconds
        val fallbackIdleEpoch = if (baseEpoch > 0L) baseEpoch else pastedAtEpochSeconds
        val freedEpochs = mutableListOf<Long>()

        fun collectPoolFreeEpochs(slotCount: Int, upgrades: List<UpgradeItem>) {
            val regular = upgrades.filter { !it.isGoblinHelper }
            for (i in 0 until slotCount) {
                val upg = regular.getOrNull(i)
                if (upg == null) {
                    if (fallbackIdleEpoch > 0L) freedEpochs.add(fallbackIdleEpoch)
                } else if (upg.remainingSeconds(baseEpoch, nowEpochSeconds) <= 0L) {
                    freedEpochs.add(upg.finishEpochSeconds(baseEpoch))
                }
            }
        }

        collectPoolFreeEpochs(totalHomeBuilders, builderUpgrades)
        collectPoolFreeEpochs(1, labUpgrades)
        collectPoolFreeEpochs(totalBuilderBaseBuilders, builderBaseUpgrades)
        collectPoolFreeEpochs(1, starLabUpgrades)

        val earliestFreed = freedEpochs.minOrNull() ?: return null
        return max(0L, nowEpochSeconds - earliestFreed)
    }

    fun getFreeSlotDescriptions(nowEpochSeconds: Long): List<String> {
        val baseEpoch = effectiveBaseEpochSeconds

        val activeThBuilderCount = builderUpgrades.count {
            !it.isGoblinHelper && it.remainingSeconds(baseEpoch, nowEpochSeconds) > 0L
        }
        val freeThBuilders = max(0, totalHomeBuilders - activeThBuilderCount)

        val activeThLabCount = labUpgrades.count {
            !it.isGoblinHelper && it.remainingSeconds(baseEpoch, nowEpochSeconds) > 0L
        }
        val isThLabFree = activeThLabCount == 0

        val activeBbBuilderCount = builderBaseUpgrades.count {
            !it.isGoblinHelper && it.remainingSeconds(baseEpoch, nowEpochSeconds) > 0L
        }
        val freeBbBuilders = max(0, totalBuilderBaseBuilders - activeBbBuilderCount)

        val activeBbLabCount = starLabUpgrades.count {
            !it.isGoblinHelper && it.remainingSeconds(baseEpoch, nowEpochSeconds) > 0L
        }
        val isBbLabFree = activeBbLabCount == 0

        return buildList {
            if (freeThBuilders > 0) {
                add(if (freeThBuilders == 1) "TH Builder" else "TH Builder ×$freeThBuilders")
            }
            if (isThLabFree) {
                add("TH Lab")
            }
            if (freeBbBuilders > 0) {
                add(if (freeBbBuilders == 1) "BB Builder" else "BB Builder ×$freeBbBuilders")
            }
            if (isBbLabFree) {
                add("BB Lab")
            }
        }
    }

    companion object {
        fun createEmpty(name: String): VillageData = VillageData(
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
    }
}

sealed class ParseResult {
    data class Success(val village: VillageData) : ParseResult()
    data class Error(val message: String) : ParseResult()
}
