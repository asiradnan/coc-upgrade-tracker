package com.asiradnan.cocupgradetracker

import com.asiradnan.cocupgradetracker.data.CocVillageParser
import com.asiradnan.cocupgradetracker.data.ParseResult
import com.asiradnan.cocupgradetracker.data.TimeFormatters
import org.junit.Assert.assertEquals
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

        // Sorted by earliest finish first: Tornado Trap (7920s) is earliest!
        val earliestBuilder = village.builderUpgrades.first()
        assertEquals("Tornado Trap", earliestBuilder.name)
        assertEquals(4, earliestBuilder.currentLevel)
        assertEquals(5, earliestBuilder.targetLevel)
        assertEquals(exportTime + 7920L, earliestBuilder.finishEpochSeconds(exportTime))

        // Lab upgrades: Root Rider (61500s) and Overgrowth Spell (184200s, Goblin Researcher)
        assertEquals(2, village.labUpgrades.size)
        val earliestLab = village.labUpgrades.first()
        assertEquals("Root Rider", earliestLab.name)
        assertEquals(2, earliestLab.currentLevel)
        assertEquals(3, earliestLab.targetLevel)
        assertEquals(exportTime + 61500L, earliestLab.finishEpochSeconds(exportTime))

        // Duration formatting check
        assertEquals("2h 12m 0s", TimeFormatters.formatDuration(7920L))
        assertEquals("17h 5m 0s", TimeFormatters.formatDuration(61500L))
        assertEquals("3m ago", TimeFormatters.formatRelativeAgo(180L))
        assertEquals("just now", TimeFormatters.formatRelativeAgo(3L))
        assertEquals(2, village.totalBuilderBaseBuilders)
    }
}
