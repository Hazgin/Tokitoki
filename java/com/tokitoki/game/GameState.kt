package com.tokitoki.game

object GameState {
    var playerName: String? = null
    var tokiName: String? = null
    var isFirstLaunch: Boolean = true
    var homeHPrest = 0.0
    var homeStaminarest = 0.0

    //Vitals
    var playerHP = 10.0
    var maxPlayerHP = 10.0
    var playerHPRate = 0.05 + homeHPrest

    var playerStamina = 10.0
    var maxPlayerStamina = 10.0
    var playerStaminaRate = 0.05 + homeStaminarest


    //T. Vitals
    var tokiHP = 1.0
    var maxTokiHP = 1.0
    var tokiHPRate = 0.05 + homeHPrest

    var tokiStamina = 1.0
    var maxTokiStamina = 1.0
    var tokiStaminaRate = 0.05 + homeStaminarest

    // Resources
    var eletro = 0.0
    var maxEletro = 15.0
    var alltimeEletro = 0.0

    var scrolls = 0.0
    var maxScrolls = 0.0
    var alltimeScrolls = 0.0

    var knowledge = 0.0
    var maxKnowledge = 0.0
    var alltimeKnowledge = 0.0

    var herbs = 0.0
    var maxHerbs = 0.0
    var alltimeHerbs = 0.0

    var milk = 0.0
    var maxMilk = 0.0
    var alltimeMilk = 0.0

    var meat = 0.0
    var maxMeat = 0.0
    var alltimeMeat = 0.0

    var bones = 0.0
    var maxBones = 0.0
    var alltimeBones = 0.0

    var berries = 0.0
    var maxBerries = 0.0
    var alltimeBerries = 0.0

    var roots = 0.0
    var maxRoots = 0.0
    var alltimeRoots = 0.0

    var grain = 0.0
    var maxGrain = 0.0
    var alltimeGrain = 0.0

    var fruit = 0.0
    var maxFruit = 0.0
    var alltimeFruit = 0.0

    var mushrooms = 0.0
    var maxMushrooms = 0.0
    var alltimeMushrooms = 0.0

    var spice = 0.0
    var maxSpice = 0.0
    var alltimeSpice = 0.0

    var isTaskRunning = false
    var isResting = false
    var isTraining = false

    //furniture
    var isSellMode: Boolean = false

    val furnitureList = listOf(
        Furniture(
            name = "Box",
            cost = FurnitureCost(eletroCost = 10f, scrollsCost = 0f, herbsCost = 0f),
            space = 1,
            maxAmount = null,
            effect = "A compact wooden coffer for storing money.\n\nCosts 10 Eletro;\n+25 max Eletro.",
            tags = listOf()
        ),
        Furniture(
            name = "Cot",
            cost = FurnitureCost(eletroCost = 10f, scrollsCost = 0f, herbsCost = 0f),
            space = 1,
            maxAmount = 1,
            effect = "Slightly better than sleeping on the floor.\n\nCosts 10 Eletro;\n+0.5 HP/Stamina recovery while resting.",
            tags = listOf("Resting place")
        ),
        Furniture(
            name = "Food cabinet",
            cost = FurnitureCost(eletroCost = 15f, scrollsCost = 0f, herbsCost = 0f),
            space = 1,
            maxAmount = null,
            effect = "Simple cabinet for storing dry food.\n\nCosts 15 Eletro;\n+10 max Roots;\n+10 max Grain;\n+10 max Bones;\n+1 max Spice.",
            tags = listOf()
        ),
        Furniture(
            name = "Food pot",
            cost = FurnitureCost(eletroCost = 15f, scrollsCost = 0f, herbsCost = 0f),
            space = 1,
            maxAmount = null,
            effect = "Clay pots for preserving food.\n\nCosts 15 Eletro;\n+10 max Milk;\n+10 max Meat;\n+10 max Berries\n+10 max Fruit.",
            tags = listOf()
        ),
        Furniture(
            name = "Frugal pen",
            cost = FurnitureCost(eletroCost = 10f, scrollsCost = 0f, herbsCost = 0f),
            space = 1,
            maxAmount = 1,
            effect = "Costs 10 Eletro; Lets you take care of a toki.",
            tags = listOf("Toki handling")
        ),
        Furniture(
            name = "Punching Pole",
            cost = FurnitureCost(eletroCost = 10f, scrollsCost = 0f, herbsCost = 0f),
            space = 1,
            maxAmount = 1,
            effect = "A thick wooden pole with padding at the top.\n\nCosts 10 Eletro;\nUsed for martial training.",
            tags = listOf("Martialsource")
        ),
        Furniture(
            name = "Scroll rack",
            cost = FurnitureCost(eletroCost = 15f, scrollsCost = 0f, herbsCost = 0f),
            space = 1,
            maxAmount = null,
            effect = "Small shelves for storing scrolls.\n\nCosts 15 Eletro;\n+15 max Scrolls",
            tags = listOf()
        ),
        Furniture(
            name = "Table",
            cost = FurnitureCost(eletroCost = 20f, scrollsCost = 0f, herbsCost = 0f),
            space = 2,
            maxAmount = 1,
            effect = "A long table with chairs. Useful for making stuff.\n\nCosts 20 Eletro;\nCrafting surface",
            tags = listOf("Craftsource")
        ),
        Furniture(
            name = "Windowbox",
            cost = FurnitureCost(eletroCost = 10f, scrollsCost = 0f, herbsCost = 0f),
            space = 1,
            maxAmount = null,
            effect = "A planting vase for growing small plants even inside the house.\n\nCosts 10 Eletro;\n+15 max Herbs",
            tags = listOf("Plantsource")
        )
    )


    data class Furniture(
        val name: String,
        val cost: FurnitureCost,
        val space: Int,
        val maxAmount: Int?,
        val effect: String,
        val tags: List<String>
    )

    data class FurnitureCost(
        val eletroCost: Float,
        val scrollsCost: Float,
        val herbsCost: Float,
    )

    var furnitureOwned = mutableMapOf<String, Int>()


    //Homes

    data class Home(
        val name: String,
        val costEletro: Int = 0,
        val costHerbs: Int = 0,
        val maxSpace: Int,
        val hpBonus: Double = 0.0,
        val staminaBonus: Double = 0.0,
        val description: String
    )

    val homes = listOf(
        Home(
            name = "Hayloft",
            maxSpace = 5,
            description = "A simple hayloft. No cost to move here. No bonuses."
        ),
        Home(
            name = "Hut",
            costEletro = 50,
            maxSpace = 15,
            description = "A basic hut. Costs 50 Eletro to move here. Space: 15."
        ),
        Home(
            name = "Cottage",
            costEletro = 100,
            maxSpace = 25,
            description = "A cozy cottage. Costs 100 Eletro to move here. Space: 25."
        ),
        Home(
            name = "Camp",
            costEletro = 50,
            costHerbs = 15,
            maxSpace = 20,
            hpBonus = 0.5,
            staminaBonus = 0.5,
            description = "A camp in nature. Costs 50 Eletro and 15 Herbs to move here. Space: 20. +0.5 HP and Stamina recovery rate."
        )
    )

    //Toki Evolution
    var eletroFed = 0.0
    var herbsFed = 0.0
    var milkFed = 0.0
    var berriesFed = 0.0
    var bonesFed = 0.0
    var meatFed = 0.0
    var rootsFed = 0.0
    var grainFed = 0.0
    var mushroomFed = 0.0
    var fruitFed = 0.0
    var spiceFed = 0.0
    var alltimeFed = (eletroFed + herbsFed + milkFed + berriesFed + bonesFed + rootsFed + grainFed + mushroomFed + fruitFed + spiceFed)
    var tokiLvl = 0.0

    // Progression
    var upgradesAcquired = 0
    var hasWallet = false
    var hasWindchime = false
    var hasPurse = false

    var currentHome = "Hayloft"
    var space = 5
    var maxSpace = 5

    var HPRestRate = 1.0
    var staminaRestRate = 1.0

    val unlockedTabs = mutableSetOf("Main", "Stats")

    //Skills
    var playerSkills = mutableListOf<Skill>()
    var tokiSkills = mutableListOf<Skill>()

    // QUESTS
    var unlockedQuestLocations = mutableSetOf<String>()
    var questProgress = mutableMapOf<String, Int>() // locationId
    var currentQuest: QuestLocation? = null
    var currentEncounterIndex: Int? = null
    var currentEncounter: Encounter? = null
    var currentCombatAllies = mutableListOf<CombatParticipant>()
    var currentCombatEnemies = mutableListOf<CombatParticipant>()
    var combatLog = mutableListOf<String>()
    var explorationStress = Triple(0, 0, 0) // (weariness + frustration + unease)
    var explorationTimerMillis: Long = 0L

    val questLocations = listOf(
        QuestLocation(
            id = "forest_1",
            name = "Whispering Woods",
            description = "A mysterious woodland, full of hidden dangers and treasures.",
            encounters = listOf(
                Encounter.Exploration(
                    name = "Forest Path",
                    description = "A winding path through the undergrowth.",
                    baseDurationSec = 10,
                    rewards = listOf(ItemDrop("herbs", 2, 1.0)),
                    stressIncrease = StressEffect(2, 1, 1)
                ),
                Encounter.Combat(
                    name = "Wild Boar Ambush",
                    description = "A wild boar charges from the bushes!",
                    enemies = listOf(
                        CombatParticipant("Wild Boar", 8.0, 8.0, 2.0, 0.5)
                    ),
                    possibleDrops = listOf(ItemDrop("meat", 1, 0.7))
                ),
                Encounter.Exploration(
                    name = "Old Tree",
                    description = "An ancient tree towers above the canopy.",
                    baseDurationSec = 8,
                    rewards = listOf(ItemDrop("berries", 3, 1.0)),
                    stressIncrease = StressEffect(1, 2, 1)
                )
            )
        ),

    )


    // Functions
    fun unlockTab(name: String) {
        unlockedTabs.add(name)
    }

    fun canStartTask(): Boolean {
        return !isTaskRunning && !isResting
    }

    fun resetTasks() {
        isTaskRunning = false
        isResting = false
    }

    fun hasFurnitureWithTag(tag: String): Boolean {
        return furnitureList.any { furniture ->
            tag in furniture.tags && (furnitureOwned[furniture.name] ?: 0) > 0
        }
    }

    fun hasUpgradeWithTag(tag: String): Boolean {

        return false
    }

    fun getHomeByName(name: String) = homes.find { it.name == name } ?: homes[0]
}
