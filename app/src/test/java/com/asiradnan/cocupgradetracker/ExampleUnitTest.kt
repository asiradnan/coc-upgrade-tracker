package com.asiradnan.cocupgradetracker

import com.asiradnan.cocupgradetracker.data.CocDataCatalog
import com.asiradnan.cocupgradetracker.data.CocVillageParser
import com.asiradnan.cocupgradetracker.data.ParseResult
import com.asiradnan.cocupgradetracker.data.TimeFormatters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun parseSampleVillageJson_extractsBuilderAndLabUpgradesAndEarliestFreeTimes() {
        val exportTime = 1793633644L
        val sampleJson = CocVillageParser.generateSampleVillageJson(exportTime)
        val result = CocVillageParser.parse(sampleJson, exportTime)

        assertTrue("Expected ParseResult.Success", result is ParseResult.Success)
        val village = (result as ParseResult.Success).village

        assertEquals("#2PP9LU80Q", village.tag)
        assertEquals(16, village.townHallLevel)
        assertEquals(6, village.totalHomeBuilders) // 5 Builder's Huts + B.O.B's Hut

        // 5 building upgrades + 1 trap upgrade + 1 hero (Goblin) upgrade = 7 builder upgrades
        assertEquals(7, village.builderUpgrades.size)

        // Sorted by earliest finish first: Tornado Trap (7920s unboosted, 792s with 10x Builder Potion) is earliest!
        val earliestBuilder = village.builderUpgrades.first()
        assertEquals("Tornado Trap", earliestBuilder.name)
        assertEquals(4, earliestBuilder.currentLevel)
        assertEquals(5, earliestBuilder.targetLevel)
        assertEquals(7920L, earliestBuilder.durationSecondsAtExport)
        assertEquals(exportTime + 792L, earliestBuilder.finishEpochSeconds(exportTime))

        // Lab upgrades: Root Rider (61500s unboosted, 54300s with 4x Study Soup) and Overgrowth Spell (184200s, Goblin Researcher)
        assertEquals(2, village.labUpgrades.size)
        val earliestLab = village.labUpgrades.first()
        assertEquals("Root Rider", earliestLab.name)
        assertEquals(2, earliestLab.currentLevel)
        assertEquals(3, earliestLab.targetLevel)
        assertEquals(61500L, earliestLab.durationSecondsAtExport)
        assertEquals(exportTime + 54300L, earliestLab.finishEpochSeconds(exportTime))

        // Duration & relative time formatting check
        assertEquals("2h 12m 0s", TimeFormatters.formatDuration(7920L))
        assertEquals("17h 5m 0s", TimeFormatters.formatDuration(61500L))
        assertEquals("3m ago", TimeFormatters.formatRelativeAgo(180L))
        assertEquals("just now", TimeFormatters.formatRelativeAgo(3L))
        assertEquals("just now", TimeFormatters.formatRelativeAgo(45L))
        assertTrue(TimeFormatters.formatPastedClockTime(exportTime, exportTime).startsWith("Today at"))
        assertEquals(2, village.totalBuilderBaseBuilders)
        assertEquals(10, village.totalSlotCount)
        assertFalse(village.isLabMaxed)
        assertFalse(village.isStarLabMaxed)
        assertEquals(0, village.getFreeSlotCount(exportTime))
        assertEquals(1, village.getFreeSlotCount(exportTime + 1000L))
        assertEquals(2, village.getFreeSlotCount(exportTime + 8000L))
        assertTrue(earliestBuilder.progressFraction(exportTime, exportTime, 345600L) > 0.9f)
        assertEquals(1f, earliestBuilder.progressFraction(exportTime, exportTime + 8000L, 345600L), 0.001f)
    }

    @Test
    fun maxedLabsAndSquadBarracks_notCountedAsFreeAndNamedCorrectly() {
        val exportTime = 1793633644L
        val json = """
        {
          "tag": "#MAXLAB01",
          "timestamp": $exportTime,
          "buildings": [
            { "data": 1000001, "lvl": 10 },
            { "data": 1000015, "lvl": 1, "cnt": 2 },
            { "data": 1000007, "lvl": 8 },
            { "data": 1000106, "lvl": 2, "timer": 36000 },
            { "data": 1000021, "lvl": 3, "timer": 72000 }
          ],
          "units": [
            { "data": 4000000, "lvl": 7 },
            { "data": 4000001, "lvl": 7 },
            { "data": 4000002, "lvl": 7 }
          ],
          "spells": [
            { "data": 26000000, "lvl": 7 }
          ],
          "buildings2": [
            { "data": 1000034, "lvl": 10 },
            { "data": 1000046, "lvl": 10 },
            { "data": 1000052, "lvl": 9, "timer": 54000 }
          ],
          "units2": [
            { "data": 4000031, "lvl": 20 },
            { "data": 4000032, "lvl": 20 },
            { "data": 4000036, "lvl": 20 }
          ]
        }
        """.trimIndent()

        val result = CocVillageParser.parse(json, exportTime)
        assertTrue("Expected ParseResult.Success", result is ParseResult.Success)
        val village = (result as ParseResult.Success).village

        // Squad Barracks (1000106) and Squads catalog check
        assertEquals("Squad Barracks", CocDataCatalog.getBuildingName(1000106))
        assertEquals("Barch Squad", CocDataCatalog.getUnitName(4000195))
        assertEquals("Squad Barracks", village.builderUpgrades.first().name)

        // Both TH Lab (Lab 8) and BB Star Lab (Star Lab 10) have all units/spells at max level
        assertTrue("Expected Home Village Lab to be detected as maxed", village.isLabMaxed)
        assertTrue("Expected Builder Base Star Lab to be detected as maxed", village.isStarLabMaxed)

        // All builders are busy (2 TH builders + 1 BB builder) and both Labs are maxed (idle, not free)
        assertEquals(0, village.getFreeSlotCount(exportTime))
        assertNull(village.getFreeDurationSeconds(exportTime))
        assertTrue(village.getFreeSlotDescriptions(exportTime).isEmpty())
    }
}
