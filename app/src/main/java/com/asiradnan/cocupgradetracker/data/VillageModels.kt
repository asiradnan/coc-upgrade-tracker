package com.asiradnan.cocupgradetracker.data

import kotlin.math.max
import kotlin.math.min

data class UpgradeItem(
    val id: String,
    val dataId: Int,
    val name: String,
    val category: UpgradeCategory,
    val currentLevel: Int,
    val targetLevel: Int,
    val durationSecondsAtExport: Long,
    val effectiveDurationSeconds: Long = durationSecondsAtExport, // Real-world duration accounting for boosts
    val isGoblinHelper: Boolean = false,
    val isApprenticeRecurring: Boolean = false,
    val superchargeLevel: Int? = null,
    val isGearUp: Boolean = false
) {
    fun finishEpochSeconds(baseEpochSeconds: Long): Long =
        baseEpochSeconds + effectiveDurationSeconds

    fun remainingSeconds(baseEpochSeconds: Long, nowEpochSeconds: Long): Long =
        max(0L, finishEpochSeconds(baseEpochSeconds) - nowEpochSeconds)

    fun progressFraction(
        baseEpochSeconds: Long,
        nowEpochSeconds: Long,
        referenceMaxSeconds: Long = effectiveDurationSeconds
    ): Float {
        val remaining = remainingSeconds(baseEpochSeconds, nowEpochSeconds)
        if (remaining <= 0L) return 1f
        val baselineWindow = max(
            effectiveDurationSeconds,
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

data class ActiveBoosts(
    val builderPotionSeconds: Long = 0L,     // "builder_boost" (10x speed)
    val labPotionSeconds: Long = 0L,         // "lab_boost" (24x speed)
    val builderSnackSeconds: Long = 0L,      // "builder_consumable" - Builder Bite (2x speed)
    val labSnackSeconds: Long = 0L,          // "lab_consumable" - Study Soup (4x speed)
    val clockTowerBoostSeconds: Long = 0L,   // "clocktower_boost" (10x BB speed)
    val clockTowerCooldownSeconds: Long = 0L,// "clocktower_cooldown"
    val helperCooldownSeconds: Long = 0L     // "helper_cooldown"
) {
    val hasAnyActiveBoost: Boolean
        get() = builderPotionSeconds > 0L ||
                labPotionSeconds > 0L ||
                builderSnackSeconds > 0L ||
                labSnackSeconds > 0L ||
                clockTowerBoostSeconds > 0L

    /**
     * Converts raw unboosted `timer` work at export into real-world seconds until completion,
     * and can also compute remaining unboosted work or remaining real seconds at `elapsedRealSec`.
     */
    fun computeRealDurationSeconds(
        unboostedTimerSeconds: Long,
        category: UpgradeCategory,
        elapsedRealSeconds: Long = 0L
    ): Long {
        if (unboostedTimerSeconds <= 0L) return 0L
        val segments = buildSpeedSegments(category)
        var remainingWork = unboostedTimerSeconds.toDouble()
        var realTimeCursor = 0.0

        for ((segDuration, multiplier) in segments) {
            if (segDuration <= 0L) continue
            val maxWorkInSeg = segDuration * multiplier
            if (remainingWork <= maxWorkInSeg) {
                realTimeCursor += remainingWork / multiplier
                remainingWork = 0.0
                break
            } else {
                remainingWork -= maxWorkInSeg
                realTimeCursor += segDuration.toDouble()
            }
        }

        if (remainingWork > 0.0) {
            realTimeCursor += remainingWork
        }

        val totalRealSeconds = kotlin.math.ceil(realTimeCursor).toLong()
        return max(0L, totalRealSeconds - max(0L, elapsedRealSeconds))
    }

    /**
     * Returns (durationSeconds, speedMultiplier) phases starting from export timestamp.
     * - Home Builder: Builder Potion (10x) and Builder Bite (2x) queue or overlap; in CoC,
     *   same-building boosts stack additively on bonus speed (1 + 9 + 1 = 11x) while both run.
     * - Home Lab: Research Potion (24x = +23x) + Study Soup (4x = +3x) = 27x while both active.
     * - Builder Base (Building & Star Lab): Clock Tower runs at 10x.
     */
    private fun buildSpeedSegments(category: UpgradeCategory): List<Pair<Long, Double>> {
        return when (category) {
            UpgradeCategory.BUILDING,
            UpgradeCategory.TRAP,
            UpgradeCategory.HERO -> combineTwoBoosts(
                durA = builderPotionSeconds, bonusA = 9.0,
                durB = builderSnackSeconds, bonusB = 1.0
            )

            UpgradeCategory.TROOP,
            UpgradeCategory.SPELL,
            UpgradeCategory.SIEGE -> combineTwoBoosts(
                durA = labPotionSeconds, bonusA = 23.0,
                durB = labSnackSeconds, bonusB = 3.0
            )

            UpgradeCategory.BUILDER_BASE_BUILDING,
            UpgradeCategory.BUILDER_BASE_RESEARCH -> {
                if (clockTowerBoostSeconds > 0L) {
                    listOf(clockTowerBoostSeconds to 10.0)
                } else {
                    emptyList()
                }
            }

            UpgradeCategory.PET -> emptyList()
        }
    }

    private fun combineTwoBoosts(
        durA: Long,
        bonusA: Double,
        durB: Long,
        bonusB: Double
    ): List<Pair<Long, Double>> {
        val overlap = min(durA, durB)
        val list = mutableListOf<Pair<Long, Double>>()
        if (overlap > 0L) {
            list.add(overlap to (1.0 + bonusA + bonusB))
        }
        if (durA > overlap) {
            list.add((durA - overlap) to (1.0 + bonusA))
        } else if (durB > overlap) {
            list.add((durB - overlap) to (1.0 + bonusB))
        }
        return list
    }
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
    val boosts: ActiveBoosts = ActiveBoosts(),
    val rawJson: String
) {
    val totalHomeBuilders: Int
        get() = totalRegularBuilders + if (hasBobBuilder) 1 else 0

    val totalSlotCount: Int
        get() = totalHomeBuilders + 1 + totalBuilderBaseBuilders + 1
    val effectiveBaseEpochSeconds: Long
        get() {
            val allTimers =
                (builderUpgrades + labUpgrades + petUpgrades + builderBaseUpgrades + starLabUpgrades)
                    .map { it.effectiveDurationSeconds }
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
