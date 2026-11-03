package com.asiradnan.cocupgradetracker.data

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.max

enum class BaseTab {
    TOWNHALL,
    BUILDER_BASE
}

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
}

sealed class ParseResult {
    data class Success(val village: VillageData) : ParseResult()
    data class Error(val message: String) : ParseResult()
}

internal class JsonMap(private val map: Map<String, Any?>) {
    fun has(key: String): Boolean = map.containsKey(key) && map[key] != null

    fun optString(key: String, default: String = ""): String =
        (map[key] as? String) ?: map[key]?.toString() ?: default

    fun optInt(key: String, default: Int = 0): Int = when (val v = map[key]) {
        is Number -> v.toInt()
        is String -> v.toIntOrNull() ?: default
        else -> default
    }

    fun optLong(key: String, default: Long = 0L): Long = when (val v = map[key]) {
        is Number -> v.toLong()
        is String -> v.toLongOrNull() ?: default
        else -> default
    }

    fun optBoolean(key: String, default: Boolean = false): Boolean = when (val v = map[key]) {
        is Boolean -> v
        is Number -> v.toInt() != 0
        is String -> v.equals("true", ignoreCase = true)
        else -> default
    }

    @Suppress("UNCHECKED_CAST")
    fun optArray(key: String): List<JsonMap>? {
        val list = map[key] as? List<*> ?: return null
        return list.mapNotNull { item ->
            (item as? Map<String, Any?>)?.let { JsonMap(it) }
        }
    }
}

internal class MinimalJsonParser(private val input: String) {
    private var pos = 0

    fun parseRootObject(): JsonMap {
        skipWhitespace()
        val value = parseValue()
        @Suppress("UNCHECKED_CAST")
        val map = value as? Map<String, Any?>
            ?: throw IllegalArgumentException("Root JSON element must be an object")
        return JsonMap(map)
    }

    private fun parseValue(): Any? {
        skipWhitespace()
        if (pos >= input.length) throw IllegalArgumentException("Unexpected end of JSON")
        return when (val ch = input[pos]) {
            '{' -> parseObject()
            '[' -> parseArray()
            '"' -> parseString()
            't', 'f' -> parseBoolean()
            'n' -> parseNull()
            '-', in '0'..'9' -> parseNumber()
            else -> throw IllegalArgumentException("Unexpected character '$ch' at position $pos")
        }
    }

    private fun parseObject(): Map<String, Any?> {
        expectChar('{')
        skipWhitespace()
        val result = linkedMapOf<String, Any?>()
        if (pos < input.length && input[pos] == '}') {
            pos++
            return result
        }
        while (pos < input.length) {
            skipWhitespace()
            val key = parseString()
            skipWhitespace()
            expectChar(':')
            val value = parseValue()
            result[key] = value
            skipWhitespace()
            if (pos < input.length && input[pos] == ',') {
                pos++
                skipWhitespace()
                if (pos < input.length && input[pos] == '}') {
                    pos++
                    return result
                }
            } else if (pos < input.length && input[pos] == '}') {
                pos++
                return result
            } else {
                throw IllegalArgumentException("Expected ',' or '}' in object at position $pos")
            }
        }
        throw IllegalArgumentException("Unterminated JSON object")
    }

    private fun parseArray(): List<Any?> {
        expectChar('[')
        skipWhitespace()
        val result = mutableListOf<Any?>()
        if (pos < input.length && input[pos] == ']') {
            pos++
            return result
        }
        while (pos < input.length) {
            val value = parseValue()
            result.add(value)
            skipWhitespace()
            if (pos < input.length && input[pos] == ',') {
                pos++
                skipWhitespace()
                if (pos < input.length && input[pos] == ']') {
                    pos++
                    return result
                }
            } else if (pos < input.length && input[pos] == ']') {
                pos++
                return result
            } else {
                throw IllegalArgumentException("Expected ',' or ']' in array at position $pos")
            }
        }
        throw IllegalArgumentException("Unterminated JSON array")
    }

    private fun parseString(): String {
        expectChar('"')
        val sb = StringBuilder()
        while (pos < input.length) {
            val ch = input[pos++]
            when (ch) {
                '"' -> return sb.toString()
                '\\' -> {
                    if (pos >= input.length) throw IllegalArgumentException("Unterminated string escape")
                    when (val esc = input[pos++]) {
                        '"', '\\', '/' -> sb.append(esc)
                        'b' -> sb.append('\b')
                        'f' -> sb.append('\u000C')
                        'n' -> sb.append('\n')
                        'r' -> sb.append('\r')
                        't' -> sb.append('\t')
                        'u' -> {
                            if (pos + 4 > input.length) throw IllegalArgumentException("Invalid unicode escape")
                            val hex = input.substring(pos, pos + 4)
                            sb.append(hex.toInt(16).toChar())
                            pos += 4
                        }
                        else -> sb.append(esc)
                    }
                }
                else -> sb.append(ch)
            }
        }
        throw IllegalArgumentException("Unterminated string literal")
    }

    private fun parseNumber(): Number {
        val start = pos
        if (input[pos] == '-') pos++
        while (pos < input.length && input[pos] in '0'..'9') pos++
        var isDouble = false
        if (pos < input.length && input[pos] == '.') {
            isDouble = true
            pos++
            while (pos < input.length && input[pos] in '0'..'9') pos++
        }
        if (pos < input.length && (input[pos] == 'e' || input[pos] == 'E')) {
            isDouble = true
            pos++
            if (pos < input.length && (input[pos] == '+' || input[pos] == '-')) pos++
            while (pos < input.length && input[pos] in '0'..'9') pos++
        }
        val raw = input.substring(start, pos)
        return if (isDouble) {
            raw.toDoubleOrNull() ?: 0.0
        } else {
            raw.toLongOrNull() ?: 0L
        }
    }

    private fun parseBoolean(): Boolean {
        return when {
            input.startsWith("true", pos) -> {
                pos += 4
                true
            }
            input.startsWith("false", pos) -> {
                pos += 5
                false
            }
            else -> throw IllegalArgumentException("Invalid boolean token at position $pos")
        }
    }

    private fun parseNull(): Any? {
        if (input.startsWith("null", pos)) {
            pos += 4
            return null
        }
        throw IllegalArgumentException("Invalid null token at position $pos")
    }

    private fun skipWhitespace() {
        while (pos < input.length && input[pos].isWhitespace()) {
            pos++
        }
    }

    private fun expectChar(expected: Char) {
        skipWhitespace()
        if (pos >= input.length || input[pos] != expected) {
            throw IllegalArgumentException("Expected '$expected' at position $pos")
        }
        pos++
    }
}

object CocVillageParser {

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
            prefix = "bld"
        )
        extractUpgrades(
            array = trapsArray,
            categoryProvider = { UpgradeCategory.TRAP },
            nameProvider = { CocDataCatalog.getTrapName(it) },
            targetList = builderUpgrades,
            prefix = "trp"
        )
        extractUpgrades(
            array = heroesArray,
            categoryProvider = { UpgradeCategory.HERO },
            nameProvider = { CocDataCatalog.getHeroName(it) },
            targetList = builderUpgrades,
            prefix = "hero"
        )

        val labUpgrades = mutableListOf<UpgradeItem>()
        extractUpgrades(
            array = unitsArray,
            categoryProvider = { dataId ->
                if (CocDataCatalog.isSiegeMachine(dataId)) UpgradeCategory.SIEGE else UpgradeCategory.TROOP
            },
            nameProvider = { CocDataCatalog.getUnitName(it) },
            targetList = labUpgrades,
            prefix = "unit"
        )
        extractUpgrades(
            array = spellsArray,
            categoryProvider = { UpgradeCategory.SPELL },
            nameProvider = { CocDataCatalog.getSpellName(it) },
            targetList = labUpgrades,
            prefix = "spell"
        )
        extractUpgrades(
            array = siegeArray,
            categoryProvider = { UpgradeCategory.SIEGE },
            nameProvider = { CocDataCatalog.getUnitName(it) },
            targetList = labUpgrades,
            prefix = "siege"
        )

        val petUpgrades = mutableListOf<UpgradeItem>()
        extractUpgrades(
            array = petsArray,
            categoryProvider = { UpgradeCategory.PET },
            nameProvider = { CocDataCatalog.getPetName(it) },
            targetList = petUpgrades,
            prefix = "pet"
        )

        val builderBaseUpgrades = mutableListOf<UpgradeItem>()
        extractUpgrades(
            array = buildings2Array,
            categoryProvider = { UpgradeCategory.BUILDER_BASE_BUILDING },
            nameProvider = { CocDataCatalog.getBuildingName(it) },
            targetList = builderBaseUpgrades,
            prefix = "bb_bld"
        )
        extractUpgrades(
            array = traps2Array,
            categoryProvider = { UpgradeCategory.BUILDER_BASE_BUILDING },
            nameProvider = { CocDataCatalog.getTrapName(it) },
            targetList = builderBaseUpgrades,
            prefix = "bb_trp"
        )
        extractUpgrades(
            array = heroes2Array,
            categoryProvider = { UpgradeCategory.BUILDER_BASE_BUILDING },
            nameProvider = { CocDataCatalog.getHeroName(it) },
            targetList = builderBaseUpgrades,
            prefix = "bb_hero"
        )

        val starLabUpgrades = mutableListOf<UpgradeItem>()
        extractUpgrades(
            array = units2Array,
            categoryProvider = { UpgradeCategory.BUILDER_BASE_RESEARCH },
            nameProvider = { CocDataCatalog.getUnitName(it) },
            targetList = starLabUpgrades,
            prefix = "bb_unit"
        )

        val helpers = mutableListOf<HelperStatus>()
        if (helpersArray != null) {
            for (obj in helpersArray) {
                val dataId = obj.optInt("data", -1)
                if (dataId == -1) continue
                val lvl = obj.optInt("lvl", 1)
                val cooldown = obj.optLong("helper_cooldown", 0L)
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

        builderUpgrades.sortBy { it.durationSecondsAtExport }
        labUpgrades.sortBy { it.durationSecondsAtExport }
        petUpgrades.sortBy { it.durationSecondsAtExport }
        builderBaseUpgrades.sortBy { it.durationSecondsAtExport }
        starLabUpgrades.sortBy { it.durationSecondsAtExport }

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
                rawJson = cleanJson
            )
        )
    }

    private fun extractUpgrades(
        array: List<JsonMap>?,
        categoryProvider: (Int) -> UpgradeCategory,
        nameProvider: (Int) -> String,
        targetList: MutableList<UpgradeItem>,
        prefix: String
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
            val gearUp = obj.optInt("gear_up", 0) > 0 && currentLvl > 0 && obj.optBoolean("gearing_up", false)

            val category = categoryProvider(dataId)
            val itemName = nameProvider(dataId)

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
  ]
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

object TimeFormatters {
    private val shortTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
    private val exactTimeFormatter = DateTimeFormatter.ofPattern("h:mm:ss a", Locale.getDefault())
    private val exactDateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault())
    private val fullDateTimeFormatter = DateTimeFormatter.ofPattern("EEE, MMM d • h:mm a", Locale.getDefault())

    fun formatDuration(seconds: Long): String {
        if (seconds <= 0L) return "Done"
        val days = seconds / 86400
        val hours = (seconds % 86400) / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60

        return buildString {
            if (days > 0) append("${days}d ")
            if (hours > 0 || days > 0) append("${hours}h ")
            if (minutes > 0 || hours > 0 || days > 0) append("${minutes}m ")
            append("${secs}s")
        }.trim()
    }

    fun formatShortClockTime(
        finishEpochSeconds: Long,
        nowEpochSeconds: Long,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): String {
        val finishZdt = ZonedDateTime.ofInstant(Instant.ofEpochSecond(finishEpochSeconds), zoneId)
        val nowZdt = ZonedDateTime.ofInstant(Instant.ofEpochSecond(nowEpochSeconds), zoneId)
        val finishDate: LocalDate = finishZdt.toLocalDate()
        val today: LocalDate = nowZdt.toLocalDate()

        val timePart = finishZdt.format(shortTimeFormatter).lowercase(Locale.getDefault())
        return when (finishDate) {
            today -> timePart
            today.plusDays(1) -> "$timePart (Tomorrow)"
            else -> "$timePart (${finishZdt.format(exactDateFormatter)})"
        }
    }

    fun formatExactFinishTime(
        finishEpochSeconds: Long,
        nowEpochSeconds: Long,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): String {
        val finishZdt = ZonedDateTime.ofInstant(Instant.ofEpochSecond(finishEpochSeconds), zoneId)
        val nowZdt = ZonedDateTime.ofInstant(Instant.ofEpochSecond(nowEpochSeconds), zoneId)
        val finishDate: LocalDate = finishZdt.toLocalDate()
        val today: LocalDate = nowZdt.toLocalDate()

        val timePart = finishZdt.format(exactTimeFormatter)
        return when (finishDate) {
            today -> "Today at $timePart"
            today.plusDays(1) -> "Tomorrow at $timePart (${finishZdt.format(DateTimeFormatter.ofPattern("MMM d", Locale.getDefault()))})"
            else -> "${finishZdt.format(exactDateFormatter)} at $timePart"
        }
    }

    fun formatRelativeAgo(elapsedSeconds: Long): String {
        val safeSec = max(0L, elapsedSeconds)
        return when {
            safeSec < 10L -> "just now"
            safeSec < 60L -> "${safeSec}s ago"
            safeSec < 3600L -> "${safeSec / 60L}m ago"
            safeSec < 86400L -> "${safeSec / 3600L}h ago"
            else -> "${safeSec / 86400L}d ago"
        }
    }

    fun formatFullDateTime(
        epochSeconds: Long,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): String {
        val zdt = ZonedDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), zoneId)
        return zdt.format(fullDateTimeFormatter)
    }
}
