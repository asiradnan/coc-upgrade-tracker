package com.asiradnan.cocupgradetracker.data

import kotlin.math.max

object CocVillageParser {

    fun looksLikeCocExport(rawInput: String?): Boolean {
        if (rawInput.isNullOrBlank()) return false
        val trimmed = rawInput.trim()
        val jsonStart = trimmed.indexOf('{')
        val jsonEnd = trimmed.lastIndexOf('}')
        if (jsonStart == -1 || jsonEnd == -1 || jsonEnd <= jsonStart) return false

        val clean = trimmed.substring(jsonStart, jsonEnd + 1)
        val hasTag = clean.contains("\"tag\"")
        val hasCocKey = clean.contains("\"buildings\"") ||
                clean.contains("\"heroes\"") ||
                clean.contains("\"buildings2\"") ||
                clean.contains("\"spells\"") ||
                clean.contains("\"units\"")

        return hasTag && hasCocKey
    }

    fun parse(
        rawInput: String,
        pastedAtEpochSeconds: Long,
        customUsername: String? = null
    ): ParseResult {
        val trimmed = rawInput.trim()
        if (trimmed.isEmpty()) {
            return ParseResult.Error("Please paste your Clash of Clans village JSON data first.")
        }

        val jsonStart = trimmed.indexOf('{')
        val jsonEnd = trimmed.lastIndexOf('}')
        if (jsonStart == -1 || jsonEnd == -1 || jsonEnd <= jsonStart) {
            return ParseResult.Error("Invalid format: Could not find a JSON object ({ ... }) in the pasted text.")
        }

        val cleanJson = trimmed.substring(jsonStart, jsonEnd + 1)
        val root = try {
            MinimalJsonParser(cleanJson).parseRootObject()
        } catch (e: Exception) {
            return ParseResult.Error("Invalid JSON syntax: ${e.message ?: "Unable to parse JSON"}")
        }

        val knownKeys = listOf(
            "buildings", "traps", "heroes", "units", "spells",
            "siege_machines", "pets", "buildings2", "traps2", "heroes2", "units2"
        )
        if (knownKeys.none { root.has(it) }) {
            return ParseResult.Error(
                "JSON does not contain Clash of Clans village keys (such as 'buildings', 'units', 'heroes', or 'spells')."
            )
        }

        val tag = root.optString("tag", "").ifBlank { "#VILLAGE" }
        val resolvedUsername = customUsername?.trim()?.takeIf { it.isNotEmpty() }
            ?: root.optString("name", "").trim().takeIf { it.isNotEmpty() }
            ?: tag

        val hasExplicitTimestamp = root.has("timestamp") && root.optLong("timestamp", 0L) > 0L
        val exportTimestamp = if (hasExplicitTimestamp) {
            root.optLong("timestamp", pastedAtEpochSeconds)
        } else {
            pastedAtEpochSeconds
        }

        val boostsObj = root.optObject("boosts")
        val activeBoosts = if (boostsObj != null) {
            ActiveBoosts(
                builderPotionSeconds = max(0L, boostsObj.optLong("builder_boost", 0L)),
                labPotionSeconds = max(0L, boostsObj.optLong("lab_boost", 0L)),
                builderSnackSeconds = max(0L, boostsObj.optLong("builder_consumable", 0L)),
                labSnackSeconds = max(0L, boostsObj.optLong("lab_consumable", 0L)),
                clockTowerBoostSeconds = max(0L, boostsObj.optLong("clocktower_boost", 0L)),
                clockTowerCooldownSeconds = max(0L, boostsObj.optLong("clocktower_cooldown", 0L)),
                helperCooldownSeconds = max(0L, boostsObj.optLong("helper_cooldown", 0L))
            )
        } else {
            ActiveBoosts()
        }

        val buildingsArray = root.optArray("buildings")
        val trapsArray = root.optArray("traps")
        val heroesArray = root.optArray("heroes")
        val unitsArray = root.optArray("units")
        val spellsArray = root.optArray("spells")
        val siegeArray = root.optArray("siege_machines")
        val petsArray = root.optArray("pets")
        val helpersArray = root.optArray("helpers")

        val buildings2Array = root.optArray("buildings2")
        val traps2Array = root.optArray("traps2")
        val heroes2Array = root.optArray("heroes2")
        val units2Array = root.optArray("units2")

        var townHallLevel = 1
        var builderHutCount = 0
        var hasBob = false

        if (buildingsArray != null) {
            for (obj in buildingsArray) {
                val dataId = obj.optInt("data", -1)
                val lvl = obj.optInt("lvl", 1)
                val cnt = max(1, obj.optInt("cnt", 1))
                when (dataId) {
                    1000001 -> townHallLevel = max(townHallLevel, lvl)
                    1000015 -> builderHutCount += cnt
                    1000064 -> if (cnt > 0) hasBob = true
                }
            }
        }

        var builderHallLevel: Int? = null
        var hasOttoBuilder = false
        if (buildings2Array != null) {
            for (obj in buildings2Array) {
                val dataId = obj.optInt("data", -1)
                if (dataId == 1000034) {
                    builderHallLevel = max(builderHallLevel ?: 1, obj.optInt("lvl", 1))
                } else if (dataId == 1000078 || dataId == 1000065) {
                    if (obj.optInt("cnt", 1) > 0) hasOttoBuilder = true
                }
            }
        }

        val builderUpgrades = mutableListOf<UpgradeItem>()
        extractUpgrades(
            array = buildingsArray,
            categoryProvider = { UpgradeCategory.BUILDING },
            nameProvider = { CocDataCatalog.getBuildingName(it) },
            targetList = builderUpgrades,
            prefix = "bld",
            boosts = activeBoosts
        )
        extractUpgrades(
            array = trapsArray,
            categoryProvider = { UpgradeCategory.TRAP },
            nameProvider = { CocDataCatalog.getTrapName(it) },
            targetList = builderUpgrades,
            prefix = "trp",
            boosts = activeBoosts
        )
        extractUpgrades(
            array = heroesArray,
            categoryProvider = { UpgradeCategory.HERO },
            nameProvider = { CocDataCatalog.getHeroName(it) },
            targetList = builderUpgrades,
            prefix = "hero",
            boosts = activeBoosts
        )

        val labUpgrades = mutableListOf<UpgradeItem>()
        extractUpgrades(
            array = unitsArray,
            categoryProvider = { dataId ->
                if (CocDataCatalog.isSiegeMachine(dataId)) UpgradeCategory.SIEGE else UpgradeCategory.TROOP
            },
            nameProvider = { CocDataCatalog.getUnitName(it) },
            targetList = labUpgrades,
            prefix = "unit",
            boosts = activeBoosts
        )
        extractUpgrades(
            array = spellsArray,
            categoryProvider = { UpgradeCategory.SPELL },
            nameProvider = { CocDataCatalog.getSpellName(it) },
            targetList = labUpgrades,
            prefix = "spell",
            boosts = activeBoosts
        )
        extractUpgrades(
            array = siegeArray,
            categoryProvider = { UpgradeCategory.SIEGE },
            nameProvider = { CocDataCatalog.getUnitName(it) },
            targetList = labUpgrades,
            prefix = "siege",
            boosts = activeBoosts
        )

        val petUpgrades = mutableListOf<UpgradeItem>()
        extractUpgrades(
            array = petsArray,
            categoryProvider = { UpgradeCategory.PET },
            nameProvider = { CocDataCatalog.getPetName(it) },
            targetList = petUpgrades,
            prefix = "pet",
            boosts = activeBoosts
        )

        val builderBaseUpgrades = mutableListOf<UpgradeItem>()
        extractUpgrades(
            array = buildings2Array,
            categoryProvider = { UpgradeCategory.BUILDER_BASE_BUILDING },
            nameProvider = { CocDataCatalog.getBuildingName(it) },
            targetList = builderBaseUpgrades,
            prefix = "bb_bld",
            boosts = activeBoosts
        )
        extractUpgrades(
            array = traps2Array,
            categoryProvider = { UpgradeCategory.BUILDER_BASE_BUILDING },
            nameProvider = { CocDataCatalog.getTrapName(it) },
            targetList = builderBaseUpgrades,
            prefix = "bb_trp",
            boosts = activeBoosts
        )
        extractUpgrades(
            array = heroes2Array,
            categoryProvider = { UpgradeCategory.BUILDER_BASE_BUILDING },
            nameProvider = { CocDataCatalog.getHeroName(it) },
            targetList = builderBaseUpgrades,
            prefix = "bb_hero",
            boosts = activeBoosts
        )

        val starLabUpgrades = mutableListOf<UpgradeItem>()
        extractUpgrades(
            array = units2Array,
            categoryProvider = { UpgradeCategory.BUILDER_BASE_RESEARCH },
            nameProvider = { CocDataCatalog.getUnitName(it) },
            targetList = starLabUpgrades,
            prefix = "bb_unit",
            boosts = activeBoosts
        )

        val helpers = mutableListOf<HelperStatus>()
        if (helpersArray != null) {
            for (obj in helpersArray) {
                val dataId = obj.optInt("data", -1)
                if (dataId == -1) continue
                val lvl = obj.optInt("lvl", 1)
                val cooldown = obj.optLong("helper_cooldown", activeBoosts.helperCooldownSeconds)
                helpers.add(
                    HelperStatus(
                        dataId = dataId,
                        name = CocDataCatalog.getHelperName(dataId),
                        level = lvl,
                        cooldownSecondsAtExport = max(0L, cooldown)
                    )
                )
            }
        }

        builderUpgrades.sortBy { it.effectiveDurationSeconds }
        labUpgrades.sortBy { it.effectiveDurationSeconds }
        petUpgrades.sortBy { it.effectiveDurationSeconds }
        builderBaseUpgrades.sortBy { it.effectiveDurationSeconds }
        starLabUpgrades.sortBy { it.effectiveDurationSeconds }

        val nonGoblinBuilderUpgrades = builderUpgrades.count { !it.isGoblinHelper }
        val resolvedRegularBuilders = when {
            builderHutCount > 0 -> builderHutCount.coerceIn(1, 5)
            else -> max(5, nonGoblinBuilderUpgrades.coerceAtMost(5))
        }

        val totalPermanent = resolvedRegularBuilders + if (hasBob) 1 else 0
        val adjustedBuilderUpgrades = if (
            builderUpgrades.size > totalPermanent &&
            builderUpgrades.none { it.isGoblinHelper }
        ) {
            builderUpgrades.mapIndexed { index, item ->
                if (index >= totalPermanent) item.copy(isGoblinHelper = true) else item
            }
        } else {
            builderUpgrades
        }

        val adjustedLabUpgrades = if (
            labUpgrades.size > 1 &&
            labUpgrades.none { it.isGoblinHelper }
        ) {
            labUpgrades.mapIndexed { index, item ->
                if (index >= 1) item.copy(isGoblinHelper = true) else item
            }
        } else {
            labUpgrades
        }

        val resolvedBuilderBaseBuilders = max(
            if (hasOttoBuilder) 2 else 1,
            builderBaseUpgrades.count { !it.isGoblinHelper }.coerceAtMost(2)
        )

        return ParseResult.Success(
            village = VillageData(
                username = resolvedUsername,
                tag = tag,
                exportTimestampSeconds = exportTimestamp,
                hasExplicitTimestamp = hasExplicitTimestamp,
                pastedAtEpochSeconds = pastedAtEpochSeconds,
                townHallLevel = townHallLevel,
                builderHallLevel = builderHallLevel,
                totalRegularBuilders = resolvedRegularBuilders,
                hasBobBuilder = hasBob,
                totalBuilderBaseBuilders = resolvedBuilderBaseBuilders,
                builderUpgrades = adjustedBuilderUpgrades,
                labUpgrades = adjustedLabUpgrades,
                petUpgrades = petUpgrades,
                builderBaseUpgrades = builderBaseUpgrades,
                starLabUpgrades = starLabUpgrades,
                helpers = helpers,
                boosts = activeBoosts,
                rawJson = cleanJson
            )
        )
    }

    private fun extractUpgrades(
        array: List<JsonMap>?,
        categoryProvider: (Int) -> UpgradeCategory,
        nameProvider: (Int) -> String,
        targetList: MutableList<UpgradeItem>,
        prefix: String,
        boosts: ActiveBoosts
    ) {
        if (array == null) return
        array.forEachIndexed { i, obj ->
            val timer = obj.optLong("timer", 0L)
            if (timer <= 0L) return@forEachIndexed

            val dataId = obj.optInt("data", -1)
            val currentLvl = obj.optInt("lvl", 0)
            val cnt = max(1, obj.optInt("cnt", 1))
            val isExtra = obj.optBoolean("extra", false)
            val helperRecurrent = obj.optBoolean("helper_recurrent", false)
            val supercharge = if (obj.has("supercharge")) obj.optInt("supercharge", 0) else null
            val gearUp = obj.optInt("gear_up", 0) > 0 && currentLvl > 0 && obj.optBoolean(
                "gearing_up",
                false
            )

            val category = categoryProvider(dataId)
            val itemName = nameProvider(dataId)
            val effectiveDuration = boosts.computeRealDurationSeconds(timer, category)

            for (instanceIdx in 0 until cnt) {
                targetList.add(
                    UpgradeItem(
                        id = "${prefix}_${dataId}_${i}_$instanceIdx",
                        dataId = dataId,
                        name = itemName,
                        category = category,
                        currentLevel = currentLvl,
                        targetLevel = currentLvl + 1,
                        durationSecondsAtExport = timer,
                        effectiveDurationSeconds = effectiveDuration,
                        isGoblinHelper = isExtra,
                        isApprenticeRecurring = helperRecurrent,
                        superchargeLevel = supercharge,
                        isGearUp = gearUp
                    )
                )
            }
        }
    }

    fun generateSampleVillageJson(nowEpochSeconds: Long): String {
        return """
{
  "tag": "#2PP9LU80Q",
  "timestamp": $nowEpochSeconds,
  "buildings": [
    { "data": 1000001, "lvl": 16 },
    { "data": 1000015, "lvl": 5, "cnt": 5 },
    { "data": 1000064, "lvl": 1 },
    { "data": 1000007, "lvl": 14 },
    { "data": 1000077, "lvl": 2, "timer": 13800, "helper_recurrent": true },
    { "data": 1000085, "lvl": 1, "timer": 46200 },
    { "data": 1000084, "lvl": 1, "timer": 95400 },
    { "data": 1000031, "lvl": 6, "timer": 212400 },
    { "data": 1000067, "lvl": 4, "timer": 345600 }
  ],
  "traps": [
    { "data": 12000016, "lvl": 4, "timer": 7920 }
  ],
  "heroes": [
    { "data": 28000001, "lvl": 94, "timer": 158400, "extra": true },
    { "data": 28000000, "lvl": 95 },
    { "data": 28000002, "lvl": 70 },
    { "data": 28000004, "lvl": 45 }
  ],
  "units": [
    { "data": 4000110, "lvl": 2, "timer": 61500 },
    { "data": 4000000, "lvl": 12 },
    { "data": 4000001, "lvl": 12 }
  ],
  "spells": [
    { "data": 26000070, "lvl": 3, "timer": 184200, "extra": true }
  ],
  "pets": [
    { "data": 73000010, "lvl": 8, "timer": 88200 }
  ],
  "helpers": [
    { "data": 93000000, "lvl": 8, "helper_cooldown": 32400 },
    { "data": 93000001, "lvl": 6, "helper_cooldown": 19800 }
  ],
  "buildings2": [
    { "data": 1000034, "lvl": 10 },
    { "data": 1000052, "lvl": 9, "timer": 54000 },
    { "data": 1000081, "lvl": 4, "timer": 93600 }
  ],
  "units2": [
    { "data": 4000036, "lvl": 19, "timer": 41400 }
  ],
  "boosts": {
    "builder_boost": 1800,
    "lab_consumable": 2400,
    "clocktower_cooldown": 54200
  }
}
        """.trimIndent()
    }

    fun generateSecondSampleVillageJson(nowEpochSeconds: Long): String {
        return """
{
  "tag": "#9QV88GY2L",
  "timestamp": $nowEpochSeconds,
  "buildings": [
    { "data": 1000001, "lvl": 13 },
    { "data": 1000015, "lvl": 1, "cnt": 5 },
    { "data": 1000021, "lvl": 6, "timer": 19200 },
    { "data": 1000027, "lvl": 6, "timer": 64800 },
    { "data": 1000012, "lvl": 10, "timer": 115200 }
  ],
  "heroes": [
    { "data": 28000002, "lvl": 44, "timer": 4800 }
  ],
  "units": [
    { "data": 4000053, "lvl": 2, "timer": 38400 }
  ],
  "buildings2": [
    { "data": 1000034, "lvl": 9 },
    { "data": 1000056, "lvl": 8, "timer": 28800 }
  ],
  "units2": [
    { "data": 4000037, "lvl": 16, "timer": 18000 }
  ]
}
        """.trimIndent()
    }
}