package com.tokitoki.game

object GameState {
    var playerName: String? = null
    var tokiName: String? = null
    var isFirstLaunch: Boolean = true

    //Vitals
    var playerHP = 10.0
    var maxPlayerHP = 10.0
    var playerHPRate = 0.05

    var playerStamina = 10.0
    var maxPlayerStamina = 10.0
    var playerStaminaRate = 0.05


    //T. Vitals
    var tokiHP = 1.0
    var maxTokiHP = 1.0
    var tokiHPRate = 0.05

    var tokiStamina = 1.0
    var maxTokiStamina = 1.0
    var tokiStaminaRate = 0.05

    // Resources
    var eletro = 0.0
    var maxEletro = 15.0
    var alltimeEletro = 0.0

    var scrolls = 0.0
    var maxScrolls = 0.0
    var alltimeScrolls = 0.0

    var herbs = 0.0
    var maxHerbs = 0.0
    var alltimeHerbs = 0.0

    var milk = 0.0
    var maxMilk = 0.0
    var alltimeMilk = 0.0

    var meat = 0.0
    var maxMeat = 0.0
    var alltimeMeat = 0.0

    var berries = 0.0
    var maxBerries = 0.0
    var alltimeBerries = 0.0

    var roots = 0.0
    var maxRoots = 0.0
    var alltimeRoots = 0.0

    var grains = 0.0
    var maxGrains = 0.0
    var alltimeGrains = 0.0

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

    val furnitureList = listOf(
        Furniture(
            name = "Box",
            cost = FurnitureCost(eletroCost = 10f, scrollsCost = 0f, herbsCost = 0f),
            space = 1,
            maxAmount = null,
            effect = "Costs 10 Eletro; +25 max Eletro",
            tags = listOf()
        ),
        Furniture(
            name = "Cot",
            cost = FurnitureCost(eletroCost = 10f, scrollsCost = 0f, herbsCost = 0f),
            space = 1,
            maxAmount = 1,
            effect = "Costs 10 Eletro; +0.5 HP/Stamina recovery while resting",
            tags = listOf("Resting place")
        ),
        Furniture(
            name = "Feeding Bowls",
            cost = FurnitureCost(eletroCost = 10f, scrollsCost = 0f, herbsCost = 0f),
            space = 1,
            maxAmount = 1,
            effect = "Costs 10 Eletro; Lets you take care of a toki",
            tags = listOf("Toki handling")
        ),
        Furniture(
            name = "Punching bag",
            cost = FurnitureCost(eletroCost = 10f, scrollsCost = 0f, herbsCost = 0f),
            space = 1,
            maxAmount = 1,
            effect = "Costs 10 Eletro; Used for martial training",
            tags = listOf("Martialsource")
        ),
        Furniture(
            name = "Small shelf",
            cost = FurnitureCost(eletroCost = 15f, scrollsCost = 0f, herbsCost = 0f),
            space = 1,
            maxAmount = null,
            effect = "Costs 15 Eletro; +15 max Scrolls",
            tags = listOf()
        ),
        Furniture(
            name = "Table",
            cost = FurnitureCost(eletroCost = 20f, scrollsCost = 0f, herbsCost = 0f),
            space = 2,
            maxAmount = 1,
            effect = "Costs 20 Eletro; Crafting surface",
            tags = listOf("Craftsource")
        ),
        Furniture(
            name = "Windowbox",
            cost = FurnitureCost(eletroCost = 10f, scrollsCost = 0f, herbsCost = 0f),
            space = 1,
            maxAmount = null,
            effect = "Costs 10 Eletro; +15 max Herbs",
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

    //Toki Evolution
    var eletroFed = 0.0
    var herbsFed = 0.0
    var milkFed = 0.0
    var berriesFed = 0.0
    var mossFed = 0.0
    var meatFed = 0.0
    var rootFed = 0.0
    var grainFed = 0.0
    var mushroomFed = 0.0
    var fruitFed = 0.0
    var spiceFed = 0.0
    var alltimeFed = eletroFed + herbsFed + milkFed + berriesFed + mossFed + rootFed + grainFed + mushroomFed + fruitFed + spiceFed
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
}