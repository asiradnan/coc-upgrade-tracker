package com.asiradnan.cocupgradetracker.data

enum class UpgradeCategory(val displayName: String) {
    BUILDING("Building"),
    TRAP("Trap"),
    HERO("Hero"),
    TROOP("Troop"),
    SPELL("Spell"),
    SIEGE("Siege Machine"),
    PET("Hero Pet"),
    BUILDER_BASE_BUILDING("Builder Base"),
    BUILDER_BASE_RESEARCH("Star Lab")
}

object CocDataCatalog {

    private val buildingNames = mapOf(
        1000000 to "Army Camp",
        1000001 to "Town Hall",
        1000002 to "Elixir Collector",
        1000003 to "Elixir Storage",
        1000004 to "Gold Mine",
        1000005 to "Gold Storage",
        1000006 to "Barracks",
        1000007 to "Laboratory",
        1000008 to "Cannon",
        1000009 to "Archer Tower",
        1000010 to "Wall",
        1000011 to "Wizard Tower",
        1000012 to "Air Defense",
        1000013 to "Mortar",
        1000014 to "Clan Castle",
        1000015 to "Builder's Hut",
        1000016 to "Communications Mast",
        1000019 to "Hidden Tesla",
        1000020 to "Spell Factory",
        1000021 to "X-Bow",
        1000023 to "Dark Elixir Drill",
        1000024 to "Dark Elixir Storage",
        1000026 to "Dark Barracks",
        1000027 to "Inferno Tower",
        1000028 to "Air Sweeper",
        1000029 to "Dark Spell Factory",
        1000031 to "Eagle Artillery",
        1000032 to "Bomb Tower",
        1000059 to "Workshop",
        1000064 to "B.O.B's Hut",
        1000067 to "Scattershot",
        1000068 to "Pet House",
        1000070 to "Blacksmith",
        1000071 to "Hero Hall",
        1000072 to "Spell Tower",
        1000077 to "Monolith",
        1000079 to "Multi-Gear Tower",
        1000084 to "Multi-Archer Tower",
        1000085 to "Ricochet Cannon",
        1000086 to "Revenge Tower",
        1000089 to "Firespitter",
        1000093 to "Helper Hut",
        // Builder Base Buildings
        1000033 to "Wall (BB)",
        1000034 to "Builder Hall",
        1000035 to "Elixir Collector (BB)",
        1000036 to "Elixir Storage (BB)",
        1000037 to "Gold Mine (BB)",
        1000038 to "Gold Storage (BB)",
        1000039 to "Clock Tower",
        1000040 to "Builder Barracks",
        1000041 to "Double Cannon",
        1000042 to "Army Camp (BB)",
        1000043 to "Hidden Tesla (BB)",
        1000044 to "Cannon (BB)",
        1000045 to "Multi Mortar",
        1000046 to "Star Laboratory",
        1000047 to "B.O.T.O's Shack",
        1000048 to "Archer Tower (BB)",
        1000049 to "Reinforcement Camp",
        1000050 to "Firecrackers",
        1000051 to "Guard Post",
        1000052 to "Mega Tesla",
        1000053 to "Battle Machine Altar",
        1000054 to "Air Bombs",
        1000055 to "Crusher",
        1000056 to "Roaster",
        1000057 to "Giant Cannon",
        1000058 to "Gem Mine",
        1000063 to "Lava Launcher",
        1000065 to "O.T.T.O's Hut",
        1000078 to "O.T.T.O's Outpost",
        1000080 to "Battle Copter Altar",
        1000081 to "X-Bow (BB)",
        1000082 to "Healing Hut"
    )

    private val trapNames = mapOf(
        12000000 to "Bomb",
        12000001 to "Spring Trap",
        12000002 to "Giant Bomb",
        12000005 to "Air Bomb",
        12000006 to "Seeking Air Mine",
        12000008 to "Skeleton Trap",
        12000010 to "Spring Trap (BB)",
        12000011 to "Push Trap (BB)",
        12000013 to "Mine (BB)",
        12000014 to "Mega Mine (BB)",
        12000016 to "Tornado Trap",
        12000020 to "Giga Bomb"
    )

    private val heroNames = mapOf(
        28000000 to "Barbarian King",
        28000001 to "Archer Queen",
        28000002 to "Grand Warden",
        28000003 to "Battle Machine",
        28000004 to "Royal Champion",
        28000005 to "Battle Copter",
        28000006 to "Minion Prince"
    )

    private val unitNames = mapOf(
        4000000 to "Barbarian",
        4000001 to "Archer",
        4000002 to "Goblin",
        4000003 to "Giant",
        4000004 to "Wall Breaker",
        4000005 to "Balloon",
        4000006 to "Wizard",
        4000007 to "Healer",
        4000008 to "Dragon",
        4000009 to "P.E.K.K.A",
        4000010 to "Minion",
        4000011 to "Hog Rider",
        4000012 to "Valkyrie",
        4000013 to "Golem",
        4000015 to "Witch",
        4000017 to "Lava Hound",
        4000022 to "Bowler",
        4000023 to "Baby Dragon",
        4000024 to "Miner",
        // Builder Base Troops
        4000031 to "Raged Barbarian",
        4000032 to "Sneaky Archer",
        4000033 to "Beta Minion",
        4000034 to "Boxer Giant",
        4000035 to "Bomber",
        4000036 to "Power P.E.K.K.A",
        4000037 to "Cannon Cart",
        4000038 to "Drop Ship",
        4000041 to "Baby Dragon (BB)",
        4000042 to "Night Witch",
        // Siege & Later Troops
        4000051 to "Wall Wrecker",
        4000052 to "Battle Blimp",
        4000053 to "Yeti",
        4000058 to "Ice Golem",
        4000059 to "Electro Dragon",
        4000062 to "Stone Slammer",
        4000065 to "Dragon Rider",
        4000070 to "Hog Glider",
        4000075 to "Siege Barracks",
        4000082 to "Headhunter",
        4000087 to "Log Launcher",
        4000091 to "Flame Flinger",
        4000092 to "Battle Drill",
        4000095 to "Electro Titan",
        4000097 to "Apprentice Warden",
        4000106 to "Electrofire Wizard",
        4000110 to "Root Rider",
        4000123 to "Druid",
        4000132 to "Thrower",
        4000135 to "Troop Launcher",
        4000150 to "Furnace"
    )

    private val siegeMachineIds = setOf(
        4000051, 4000052, 4000062, 4000075, 4000087, 4000091, 4000092, 4000135
    )

    private val spellNames = mapOf(
        26000000 to "Lightning Spell",
        26000001 to "Healing Spell",
        26000002 to "Rage Spell",
        26000003 to "Jump Spell",
        26000005 to "Freeze Spell",
        26000009 to "Poison Spell",
        26000010 to "Earthquake Spell",
        26000011 to "Haste Spell",
        26000016 to "Clone Spell",
        26000017 to "Skeleton Spell",
        26000028 to "Bat Spell",
        26000035 to "Invisibility Spell",
        26000053 to "Recall Spell",
        26000070 to "Overgrowth Spell",
        26000098 to "Revive Spell"
    )

    private val petNames = mapOf(
        73000000 to "L.A.S.S.I",
        73000001 to "Electro Owl",
        73000002 to "Mighty Yak",
        73000003 to "Unicorn",
        73000004 to "Phoenix",
        73000007 to "Poison Lizard",
        73000008 to "Diggy",
        73000009 to "Frosty",
        73000010 to "Spirit Fox",
        73000011 to "Angry Jelly",
        73000012 to "Sneezy"
    )

    private val helperNames = mapOf(
        93000000 to "Builder's Apprentice",
        93000001 to "Lab Assistant",
        93000002 to "Alchemist"
    )

    fun getBuildingName(dataId: Int): String =
        buildingNames[dataId] ?: "Building (#$dataId)"

    fun getTrapName(dataId: Int): String =
        trapNames[dataId] ?: "Trap (#$dataId)"

    fun getHeroName(dataId: Int): String =
        heroNames[dataId] ?: "Hero (#$dataId)"

    fun getUnitName(dataId: Int): String =
        unitNames[dataId] ?: "Troop (#$dataId)"

    fun isSiegeMachine(dataId: Int): Boolean =
        dataId in siegeMachineIds

    fun getSpellName(dataId: Int): String =
        spellNames[dataId] ?: "Spell (#$dataId)"

    fun getPetName(dataId: Int): String =
        petNames[dataId] ?: "Pet (#$dataId)"

    fun getHelperName(dataId: Int): String =
        helperNames[dataId] ?: "Helper (#$dataId)"
}

