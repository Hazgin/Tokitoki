package com.tokitoki.game

import android.graphics.Color
import android.os.*
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import android.app.Dialog

class MainActivity : AppCompatActivity() {

    private lateinit var rootLayout: LinearLayout
    private lateinit var tabContentFrame: FrameLayout
    private lateinit var tabBar: LinearLayout
    private lateinit var resourcePanel: LinearLayout
    private lateinit var taskTimerText: TextView
    private lateinit var modalContainer: FrameLayout
    private lateinit var modalText: TextView
    private var lastInterruptedTask: (() -> Unit)? = null


    private var currentTab = "Main"
    private var taskHandler: Handler? = null
    private var taskRunnable: Runnable? = null
    private var gameUpdateHandler: Handler? = null
    private var gameUpdateRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        if (GameState.isFirstLaunch) {
            showIntroModal()
        }

        modalContainer = findViewById(R.id.modalContainer)
        modalText = findViewById(R.id.modalText)


        rootLayout = findViewById(R.id.rootLayout)
        tabContentFrame = findViewById(R.id.tabContentFrame)
        tabBar = findViewById(R.id.tabBar)
        resourcePanel = findViewById(R.id.resourcePanel)

        taskTimerText = TextView(this).apply {
            textSize = 16f
            setPadding(20, 10, 20, 20)
        }

        setupTabs()
        updateResourcePanel()
        supportActionBar?.hide()
        switchTab("Main")

        startGameUpdate()
    }

    private fun showIntroModal() {
        val dialog = Dialog(this)
        dialog.setCancelable(false)
        dialog.setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)

            val story = TextView(context).apply {
                text = "Life has not been kind for you recently. You have just been fired from your clerk job, and are now living at a cousin's farm, on his hayloft, on the condition that you help around with chores.\n\nOn your way 'home' during a rainy night, you hear a soft chirping comming from a dirty alley. There, you find a soft, amorphous, vaguely quadruped creature, small enough to fit your palm. You recognize the creature as a toki foal, tokis being rare fantastical metamorphic creatures of immense potential. They are also very rare. This must be a sign!\n\nDetermined to turn your life around, you decide to take the toki to the hayloft and handle it so it may grow into a companion."
                textSize = 16f
            }
            addView(story)

            val playerInput = EditText(context).apply {
                hint = "Enter your character's name"
            }
            addView(playerInput)

            val tokiInput = EditText(context).apply {
                hint = "Name your toki"
            }
            addView(tokiInput)

            val confirm = Button(context).apply {
                text = "Continue"
                setOnClickListener {
                    val playerName = playerInput.text.toString().trim()
                    val tokiName = tokiInput.text.toString().trim()
                    if (playerName.isEmpty() || tokiName.isEmpty()) {
                        Toast.makeText(context, "Please enter both names.", Toast.LENGTH_SHORT).show()
                    } else {
                        GameState.playerName = playerName
                        GameState.tokiName = tokiName
                        GameState.isFirstLaunch = false
                        dialog.dismiss()
                    }
                }
            }
            addView(confirm)
        })
        dialog.show()
    }

    private fun startGameUpdate() {
        if (gameUpdateHandler == null) {
            gameUpdateHandler = Handler(Looper.getMainLooper())
            gameUpdateRunnable = object : Runnable {
                override fun run() {
                    updateGameState()

                    gameUpdateHandler?.postDelayed(this, 1000)
                }
            }
            gameUpdateHandler?.post(gameUpdateRunnable!!)
        }
    }

    private fun showDescriptionModal(description: String) {
        val dialog = Dialog(this)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val overlay = FrameLayout(this).apply {
            setBackgroundColor(Color.parseColor("#80000000"))
        }

        val modalContent = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
            setBackgroundColor(Color.WHITE)
            val descView = TextView(this@MainActivity).apply {
                text = description
                textSize = 16f
            }
            addView(descView)
        }

        val root = FrameLayout(this)
        root.addView(overlay)
        root.addView(modalContent)

        dialog.setContentView(root)
        dialog.setCancelable(true)
        dialog.show()
    }

    private fun hideDescriptionModal() {
        rootLayout.animate()
            .alpha(1.0f)
            .setDuration(200)
            .withEndAction {
                modalContainer.visibility = View.GONE
            }
            .start()
    }


    private fun updateGameState() {
        if (GameState.playerStamina < GameState.maxPlayerStamina) {
            GameState.playerStamina += GameState.playerStaminaRate
        }

        if (GameState.playerHP < GameState.maxPlayerHP) {
            GameState.playerHP += GameState.playerHPRate
        }

        if (GameState.tokiStamina < GameState.maxTokiStamina) {
            GameState.tokiStamina += GameState.tokiStaminaRate
        }

        if (GameState.tokiHP < GameState.maxTokiHP) {
            GameState.tokiHP += GameState.tokiHPRate
        }

        updateResourcePanel()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopGameUpdate()
    }

    private fun stopGameUpdate() {
        gameUpdateHandler?.removeCallbacks(gameUpdateRunnable!!)
        gameUpdateHandler = null
        gameUpdateRunnable = null
    }

    private fun setupTabs() {
        tabBar.removeAllViews()
        if (GameState.eletro >= 40.0) {
            GameState.unlockTab("Home")
        }
        if (GameState.eletro >= 40.0) {
            GameState.unlockTab("Skills")
        }
        if (GameSkills.tokiSkills.find { it.name == "Running" }?.level ?: 0 >= 1) {
            GameState.unlockTab("Quest")
        }
        val tabs = listOf("Main", "Stats", "Home", "Skills", "Quest")
        for (tab in tabs) {
            if (tab in GameState.unlockedTabs) {
                val button = Button(this).apply {
                    text = tab
                    setOnClickListener { switchTab(tab) }
                }
                tabBar.addView(button)
            }
        }
    }

    private fun switchTab(tab: String) {
        currentTab = tab
        tabContentFrame.removeAllViews()
        when (tab) {
            "Main" -> showMainTab()
            "Stats" -> showStatsTab()
            "Home" -> showHomeTab()
            "Skills" -> showSkillsTab()
            "Quest" -> showQuestTab()
        }
    }

    private fun updateResourcePanel() {
        resourcePanel.removeAllViews()

        fun makeHeader(title: String): TextView {
            return TextView(this).apply {
                text = title
                textSize = 18f
                setPadding(8, 16, 8, 8)
                setTextColor(Color.BLACK)
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
        }

        fun makeStatLine(line: String): TextView {
            return TextView(this).apply {
                text = line
                textSize = 16f
                setPadding(8, 4, 8, 4)
            }
        }

        // Player
        resourcePanel.addView(makeHeader(GameState.playerName.toString() + ":"))

        if (GameState.maxPlayerHP > 0) {
            val hp = "%.1f".format(GameState.playerHP)
            val maxHp = "%.1f".format(GameState.maxPlayerHP)
            resourcePanel.addView(makeStatLine("HP: $hp / $maxHp"))
        }

        if (GameState.maxPlayerStamina > 0) {
            val stam = "%.1f".format(GameState.playerStamina)
            val maxStam = "%.1f".format(GameState.maxPlayerStamina)
            resourcePanel.addView(makeStatLine("Stamina: $stam / $maxStam"))
        }

        // Toki
        resourcePanel.addView(makeHeader(GameState.tokiName.toString() + ":"))

        val tokiHP = "%.1f".format(GameState.tokiHP)
        val tokiMaxHP = "%.1f".format(GameState.maxTokiHP)
        resourcePanel.addView(makeStatLine("HP: $tokiHP / $tokiMaxHP"))

        val tokiStamina = "%.1f".format(GameState.tokiStamina)
        val tokiMaxStamina = "%.1f".format(GameState.maxTokiStamina)
        resourcePanel.addView(makeStatLine("Stamina: $tokiStamina / $tokiMaxStamina"))

        // Resources
        resourcePanel.addView(makeHeader("Valuables:"))

        val ele = "%.1f".format(GameState.eletro)
        val maxEle = "%.1f".format(GameState.maxEletro)
        resourcePanel.addView(makeStatLine("Eletro: $ele / $maxEle"))

        if (GameState.maxHerbs > 0 ) {resourcePanel.addView(makeHeader("Materials:"))}

        val herbs = "%.1f".format(GameState.herbs)
        val maxHerbs = "%.1f".format(GameState.maxHerbs)
        if (GameState.maxHerbs > 0) {
            resourcePanel.addView(makeStatLine("Herbs: $herbs / $maxHerbs"))
        }

        val bones = "%.1f".format(GameState.bones)
        val maxBones = "%.1f".format(GameState.maxBones)
        if (GameState.maxMeat > 0) {
            resourcePanel.addView(makeStatLine("Bones: $bones / $maxBones"))
        }

        if (GameState.maxScrolls > 0 ) {resourcePanel.addView(makeHeader("Research:"))}

        val scrolls = "%.1f".format(GameState.scrolls)
        val maxScrolls = "%.1f".format(GameState.maxScrolls)
        if (GameState.maxScrolls > 0) {
            resourcePanel.addView(makeStatLine("Scrolls: $scrolls / $maxScrolls"))
        }

        val knowledge = "%.1f".format(GameState.knowledge)
        val maxKnowledge = "%.1f".format(GameState.maxKnowledge)
        if (GameState.knowledge > 0) {
            resourcePanel.addView(makeStatLine("Knowledge: $knowledge / $maxKnowledge"))
        }

        if (GameState.maxMilk > 0 || GameState.maxGrain > 0) {resourcePanel.addView(makeHeader("Food:"))}

        val meat = "%.1f".format(GameState.meat)
        val maxMeat = "%.1f".format(GameState.maxMeat)
        if (GameState.maxMeat > 0) {
            resourcePanel.addView(makeStatLine("Meat: $meat / $maxMeat"))
        }

        val milk = "%.1f".format(GameState.milk)
        val maxMilk = "%.1f".format(GameState.maxMilk)
        if (GameState.maxMilk > 0) {
            resourcePanel.addView(makeStatLine("Milk: $milk / $maxMilk"))
        }

        val berries = "%.1f".format(GameState.berries)
        val maxBerries = "%.1f".format(GameState.maxBerries)
        if (GameState.maxBerries > 0) {
            resourcePanel.addView(makeStatLine("Berries: $berries / $maxBerries"))
        }

        val roots = "%.1f".format(GameState.roots)
        val maxRoots = "%.1f".format(GameState.maxRoots)
        if (GameState.maxRoots > 0) {
            resourcePanel.addView(makeStatLine("Roots: $roots / $maxRoots"))
        }

        val grain = "%.1f".format(GameState.grain)
        val maxGrain = "%.1f".format(GameState.maxGrain)
        if (GameState.maxGrain > 0) {
            resourcePanel.addView(makeStatLine("Grain: $grain / $maxGrain"))
        }

        val fruit = "%.1f".format(GameState.fruit)
        val maxFruit = "%.1f".format(GameState.maxFruit)
        if (GameState.maxFruit > 0) {
            resourcePanel.addView(makeStatLine("Fruit: $fruit / $maxFruit"))
        }

        val mushrooms = "%.1f".format(GameState.mushrooms)
        val maxMushrooms = "%.1f".format(GameState.maxMushrooms)
        if (GameState.maxMushrooms > 0) {
            resourcePanel.addView(makeStatLine("Mushrooms: $mushrooms / $maxMushrooms"))
        }

        val spice = "%.1f".format(GameState.spice)
        val maxSpice = "%.1f".format(GameState.maxSpice)
        if (GameState.maxSpice > 0) {
            resourcePanel.addView(makeStatLine("Spice: $spice / $maxSpice"))
        }

        if (GameState.eletro > GameState.maxEletro) {
            GameState.eletro = GameState.maxEletro
        }

        if (GameState.playerStamina < 0) {

        }

        setupTabs()
    }


    private fun showMainTab() {
        updateResourcePanel()
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        (taskTimerText.parent as? ViewGroup)?.removeView(taskTimerText)
        layout.addView(taskTimerText)

        val affluenceTitle = TextView(this).apply {
            text = "\nAffluence"
            textSize = 18f
            setPadding(8, 4, 8, 8)
        }
        layout.addView(affluenceTitle)

        layout.addView(addButton("[ACTION] Do Chores", "Costs 1 Stamina, gives 1 Eletro") {
            if (GameState.playerStamina >= 1 && GameState.eletro < GameState.maxEletro) {
                GameState.playerStamina -= 1
                GameState.eletro += 10
                GameState.alltimeEletro += 1
                updateResourcePanel()
                tabContentFrame.post {
                    switchTab("Main")
                }
            }
        })

        if (GameState.herbs > 2) {
            layout.addView(addButton("[ACTION] Sell Herbs", "Costs 2 Herb, gives 5 Eletro") {
                if (GameState.herbs >= 2 && GameState.eletro < GameState.maxEletro) {
                    GameState.herbs -= 2
                    GameState.eletro += 5
                    GameState.alltimeEletro += 5
                    updateResourcePanel()
                    tabContentFrame.post {
                        switchTab("Main")
                    }
                }
            })
        }

        layout.addView(addButton("[TASK] Clean stables", "10s task: 1 Stamina/s, gives 12 Eletro", isTask = true) {
            val cleanStablesTask = {
                startTask(
                    duration = 10_000,
                    label = "Cleaning stables...",
                    perSecond = {
                        GameState.playerStamina -= 1
                        updateResourcePanel()
                    },
                    onComplete = {
                        GameState.eletro += 12
                        updateResourcePanel()
                    }
                )
            }
            cleanStablesTask()
            lastInterruptedTask = cleanStablesTask
        })

        layout.addView(addButton("[TASK] Rest", "Infinite task: +1 HP & +1 Stamina per second", isTask = true) {
            if (GameState.isResting) {
                stopTask()
            } else if (GameState.canStartTask()) {
                startTask(infinite = true, label = "Resting...", perSecond = {
                    GameState.playerHP = (GameState.playerHP + GameState.HPRestRate).coerceAtMost(GameState.maxPlayerHP)
                    GameState.playerStamina = (GameState.playerStamina + GameState.staminaRestRate).coerceAtMost(GameState.maxPlayerStamina)
                    updateResourcePanel()
                })
            }
        })

        val materialsTitle = TextView(this).apply {
            text = "\nMaterials"
            textSize = 18f
            setPadding(8, 16, 8, 8)
        }
        if (GameState.maxScrolls > 0 || GameState.maxHerbs > 0 || GameState.maxMilk > 0) {layout.addView(materialsTitle)}

        if (GameState.maxScrolls > 0) {
            layout.addView(addButton("[ACTION] Buy Scroll", "Costs 5 Eletro, gives 1 Scroll") {
                if (GameState.eletro >= 5 && GameState.scrolls < GameState.maxScrolls) {
                    GameState.eletro -= 5
                    GameState.scrolls += 1
                    GameState.alltimeScrolls += 1
                    updateResourcePanel()
                    tabContentFrame.post {
                        switchTab("Main")
                    }
                }
            })
        }

        if (GameState.hasFurnitureWithTag("Plantsource") && GameState.maxHerbs > 0) {
            layout.addView(addButton("[ACTION] Collect Herbs", "Costs 1.5 Stamina, gives 1 herb.") {
                if (GameState.playerStamina >= 1.5 && GameState.maxHerbs > GameState.herbs) {
                    GameState.playerStamina -= 1.5
                    GameState.herbs += 1
                    GameState.alltimeHerbs += 1
                    updateResourcePanel()
                    tabContentFrame.post {
                        switchTab("Main")
                    }
                }
            })
        }

        if (GameState.hasFurnitureWithTag("Plantsource") && GameSkills.playerSkills.find { it.name == "Herbalism" }?.level ?: 0 >= 2 && GameState.maxMushrooms > 0) {
            layout.addView(addButton("[ACTION] Collect Mushrooms", "Costs 2 Stamina, gives 1 mushroom.") {
                if (GameState.playerStamina >= 2 && GameState.maxMushrooms > GameState.mushrooms) {
                    GameState.playerStamina -= 2
                    GameState.mushrooms += 1
                    GameState.alltimeMushrooms += 1
                    updateResourcePanel()
                    tabContentFrame.post {
                        switchTab("Main")
                    }
                }
            })
        }

        if (GameState.maxMeat > 0) {
            layout.addView(addButton("[ACTION] Butcher meat", "Offer to butcher some meat for your cousin in exchange of a part of the product.\n\nCosts 1 Stamina, Gives 1 Meat") {
                if (GameState.playerStamina >= 1 && GameState.meat < GameState.maxMeat) {
                    GameState.playerStamina -= 1
                    GameState.meat += 1
                    GameState.alltimeMeat += 1
                    updateResourcePanel()
                    tabContentFrame.post {
                        switchTab("Main")
                    }
                }
            })
        }

        if (GameState.maxMilk > 0) {
            layout.addView(addButton("[ACTION] Milk cow", "Get some milk from one of you kind cousin's cows.\n\nCosts 1 Eletro, gives 1 Milk") {
                if (GameState.eletro >= 1 && GameState.milk < GameState.maxMilk) {
                    GameState.eletro -= 1
                    GameState.milk += 1
                    GameState.alltimeMilk += 1
                    updateResourcePanel()
                    tabContentFrame.post {
                        switchTab("Main")
                    }
                }
            })
        }

        if (GameState.maxBerries > 0 && GameState.maxGrain > 0) {
            layout.addView(addButton("[TASK] Forrage", "Infinite task: -1 stamina/second.\n\nRandom chance to find Berries, Fruit, Roots, Mushrooms, or Grain each second.", isTask = true) {
                if (GameState.isTaskRunning) {
                    stopTask()
                } else if (GameState.canStartTask()) {
                    val forrageTask = {
                        startTask(
                            infinite = true,
                            label = "Forraging...",
                            perSecond = {
                                GameState.playerStamina -= 1.0
                                if (GameState.playerStamina < 0) {
                                    stopTask()
                                    return@startTask
                                }
                                val rand = Math.random()
                                when {
                                    rand < 0.25 -> {
                                        GameState.berries += 1
                                        GameState.alltimeBerries += 1
                                    }
                                    rand < 0.35 -> {
                                        GameState.fruit += 1
                                        GameState.alltimeFruit += 1
                                    }
                                    rand < 0.40 -> {
                                        GameState.roots += 1
                                        GameState.alltimeRoots += 1
                                    }
                                    rand < 0.45 -> {
                                        GameState.mushrooms += 1
                                        GameState.alltimeMushrooms += 1
                                    }
                                    rand < 0.50 -> {
                                        GameState.grain += 1
                                        GameState.alltimeGrain += 1
                                    }
                                    // else, nothing found this second
                                }
                                updateResourcePanel()
                            }
                        )
                    }
                    forrageTask()
                    lastInterruptedTask = forrageTask
                }
            })
        }

        if (GameState.maxSpice > 0) {
            layout.addView(addButton("[ACTION] Buy Spice", "Get some spice from nearby merchants.\n\nCosts 10 Eletro, gives 0.2 Spice") {
                if (GameState.eletro >= 10 && GameState.spice < GameState.maxSpice) {
                    GameState.eletro -= 10
                    GameState.spice += 0.2
                    GameState.alltimeSpice += 0.2
                    updateResourcePanel()
                    tabContentFrame.post {
                        switchTab("Main")
                    }
                }
            })
        }

        val studyTitle = TextView(this).apply {
            text = "\nStudying"
            textSize = 18f
            setPadding(8, 4, 8, 8)
        }
        if (GameState.scrolls > 0) {layout.addView(studyTitle)}

        if (GameState.scrolls > 0) {
            layout.addView(
                addButton(
                    "[TASK] Study",
                    "Infinite task: -1 stamina per second. Knowledge gained dependent on available study materials.",
                    isTask = true
                ) {
                    val taskStudy = {
                        startTask(
                            duration = 10_000,
                            label = "Cleaning stables...",
                            perSecond = {
                                GameState.playerStamina -= 1
                                GameState.knowledge += (GameState.scrolls/10)
                                updateResourcePanel()
                            },
                        )
                    }
                    taskStudy()
                    lastInterruptedTask = taskStudy
                })
        }

        val upgradesTitle = TextView(this).apply {
            text = "\nUpgrades"
            textSize = 18f
            setPadding(8, 16, 8, 8)
        }
        layout.addView(upgradesTitle)

        if (!GameState.hasWallet) {
            layout.addView(addButton("[ACTION] Buy wallet", "Costs 10 Eletro, +10 Max Eletro") {
                if (GameState.eletro >= 10) {
                    GameState.eletro -= 10
                    GameState.maxEletro += 10
                    GameState.hasWallet = true
                    GameState.upgradesAcquired++
                    updateResourcePanel()
                    tabContentFrame.post {
                        switchTab("Main")
                    }
                }
            })
        }

        if (!GameState.hasPurse && GameState.eletro >= 20) {
            layout.addView(addButton("[ACTION] Buy Purse", "Costs 20 Eletro, +25 Max Eletro") {
                if (GameState.eletro >= 20) {
                    GameState.eletro -= 20
                    GameState.maxEletro += 25
                    GameState.hasPurse = true
                    GameState.upgradesAcquired++
                    tabContentFrame.post {
                        switchTab("Main")
                    }
                }
            })
        }

        val craftingTitle = TextView(this).apply {
            text = "\nCrafting"
            textSize = 18f
            setPadding(8, 16, 8, 8)
        }
        layout.addView(craftingTitle)

        if (!GameState.hasWindchime) {
            layout.addView(addButton("[TASK] Windchime", "Build a soothing wincchime.\n5s task: 0.5 Stamina/s & 1 Eletro/s.\n\nRaises Max HP and Stamina, as well as recovery during rest.", isTask = true) {
                val craftWindchimeTask = {
                    startTask(
                        duration = 5000,
                        label = "Building windchime...",
                        perSecond = {
                            GameState.playerStamina -= 0.5
                            GameState.eletro -= 1
                            updateResourcePanel()
                        },
                        onComplete = {
                            GameState.maxPlayerHP += 2
                            GameState.maxPlayerStamina += 2
                            GameState.HPRestRate += 0.5
                            GameState.staminaRestRate += 0.5
                            GameState.hasWindchime = true
                            GameState.upgradesAcquired++
                            updateResourcePanel()
                            tabContentFrame.post {
                                switchTab("Main")
                            }
                        }
                    )
                }
                craftWindchimeTask()
                lastInterruptedTask = craftWindchimeTask
            })
        }

        val tokiTitle = TextView(this).apply {
            text = "\nToki Handling"
            textSize = 18f
            setPadding(8, 16, 8, 8)
        }

        if (GameState.alltimeEletro >= 5) {layout.addView(tokiTitle)}

        if (GameState.alltimeEletro >= 5) {
            layout.addView(addButton("[ACTION] Feed Eletro", "Costs 5 Eletro, Helps the toki grow big.\n\nIts eyes shine when you offer it the coins...") {
                if (GameState.eletro >= 5) {
                    GameState.eletro -= 5
                    GameState.eletroFed += 5
                    tabContentFrame.post {
                        switchTab("Main")
                    }
                }
            })
        }

        if (GameState.alltimeMilk >= 5) {
            layout.addView(addButton("[ACTION] Feed Milk", "Costs 2 Milk, Helps the toki grow strong.\n\nIt hits the ground with its hooves and huffs as you offer the bottle.") {
                if (GameState.milk >= 2) {
                    GameState.milk -= 2
                    GameState.milkFed += 2
                    GameState.maxTokiHP += 1
                    tabContentFrame.post {
                        switchTab("Main")
                    }
                }
            })
        }

        if (GameState.alltimeHerbs >= 5) {
            layout.addView(addButton("[ACTION] Feed Herbs", "Costs 2 Herbs, Helps the toki grow wise.\n\nIt waits patiently as you offer the grasses.") {
                if (GameState.herbs >= 2) {
                    GameState.herbs -= 2
                    GameState.herbsFed += 2
                    GameState.maxTokiStamina += 1
                    tabContentFrame.post {
                        switchTab("Main")
                    }
                }
            })
        }

        if (GameState.alltimeMeat >= 5) {
            layout.addView(addButton("[ACTION] Feed Meat", "Costs 2 Meat, Helps the toki grow strong.\n\nIt shakes and its breath gets funny when it smells the meat.") {
                if (GameState.meat >= 2) {
                    GameState.meat -= 2
                    GameState.meatFed += 2
                    GameState.maxTokiHP += 1
                    tabContentFrame.post {
                        switchTab("Main")
                    }
                }
            })
        }

        if (GameState.alltimeBones >= 5) {
            layout.addView(addButton("[ACTION] Feed Bones", "Costs 2 Bones, Helps the toki grow strong.\n\nIt looks at you firmly and huffs when you offer the bones.") {
                if (GameState.bones >= 2) {
                    GameState.bones -= 2
                    GameState.bonesFed += 2
                    GameState.maxTokiHP += 1
                    tabContentFrame.post {
                        switchTab("Main")
                    }
                }
            })
        }

        if (GameState.alltimeBerries >= 5) {
            layout.addView(addButton("[ACTION] Feed Berries", "Costs 2 Berries, Helps the toki grow wise.\n\nIt sniffs the handful of berries cautiously when you offer them. ") {
                if (GameState.milk >= 2) {
                    GameState.milk -= 2
                    GameState.milkFed += 2
                    GameState.maxTokiStamina += 1
                    tabContentFrame.post {
                        switchTab("Main")
                    }
                }
            })
        }

        if (GameState.alltimeRoots >= 5) {
            layout.addView(addButton("[ACTION] Feed Roots", "Costs 2 Roots, Helps the toki grow sharp.\n\nIt bows dramatically when you offer it the roots.") {
                if (GameState.roots >= 2) {
                    GameState.roots -= 2
                    GameState.rootsFed += 2
                    GameState.maxTokiStamina += 1
                    tabContentFrame.post {
                        switchTab("Main")
                    }
                }
            })
        }

        if (GameState.alltimeGrain >= 5) {
            layout.addView(addButton("[ACTION] Feed Grain", "Costs 2 Grain, Helps the toki grow vigorous.\n\nIt impatiently skips around when you offer it the grain.") {
                if (GameState.grain >= 2) {
                    GameState.grain -= 2
                    GameState.grainFed += 2
                    GameState.maxTokiStamina += 1
                    tabContentFrame.post {
                        switchTab("Main")
                    }
                }
            })
        }

        if (GameState.alltimeFruit >= 5) {
            layout.addView(addButton("[ACTION] Feed Fruit", "Costs 2 Fruit, Helps the toki grow vigorous.\n\nIt spins in place and wags its tail when you offer it the fruits.") {
                if (GameState.fruit >= 2) {
                    GameState.fruit -= 2
                    GameState.fruitFed += 2
                    GameState.maxTokiHP += 1
                    tabContentFrame.post {
                        switchTab("Main")
                    }
                }
            })
        }

        if (GameState.alltimeSpice >= 5) {
            layout.addView(addButton("[ACTION] Feed Spice", "Costs 0.5 Spice, Helps the toki grow sharp.\n\nIt tilts it head and bows when you offer the handful of spice, curiosity in its eyes.") {
                if (GameState.spice >= 1) {
                    GameState.spice -= 0.5
                    GameState.spiceFed += 0.5
                    GameState.maxTokiStamina += 1
                    tabContentFrame.post {
                        switchTab("Main")
                    }
                }
            })
        }

        val evolveTitle = TextView(this).apply {
            text = "\nToki Evolution"
            textSize = 18f
            setPadding(8, 16, 8, 8)
        }
        if (GameState.alltimeFed >= 50) {layout.addView(evolveTitle)}

        if (GameState.alltimeFed >= 50 && GameState.milkFed+GameState.meatFed >= 30) {
            layout.addView(addButton("Warhorse", "Allows your toki to evolve into a Warhorse, a strong form focused in crushing foes.\n\n•Learns martial skills quicker.\n•Deals more damage to enemies.\n•Has more HP.") {
                    {
                    GameState.tokiLvl = 1.0
                    GameState.tokiHP = GameState.tokiHP * 0.75
                    GameState.tokiHPRate =+ 0.75
                    tabContentFrame.post {
                        switchTab("Main")
                    }
                }
            })
        }

        if (GameState.alltimeFed >= 50 && GameState.fruitFed+GameState.grainFed >= 30) {
            layout.addView(addButton("Racehorse", "Allows your toki to evolve into a Racehorse, a nimble form focused in mobility.\n\n•May travel additional distance.\n•Dodges enemies easily.\n•Has more Stamina.") {
                {
                    GameState.tokiLvl = 1.0
                    GameState.tokiStamina = GameState.tokiStamina * 0.75
                    GameState.tokiStaminaRate =+ 0.75
                    tabContentFrame.post {
                        switchTab("Main")
                    }
                }
            })
        }

        if (GameState.alltimeFed >= 50 && GameState.herbsFed+GameState.berriesFed >= 30) {
            layout.addView(addButton("Wildhorse", "Allows your toki to evolve into a Wildhorse, a wise form focused in travelling and forraging.\n\n•May travel additional distance.\n•Can scavenge and forrage better.\n•Carries more items.") {
                {
                    GameState.tokiLvl = 1.0
                    GameState.maxHerbs = GameState.maxHerbs * 0.75
                    GameState.maxRoots = GameState.maxRoots * 0.75
                    GameState.maxBerries = GameState.maxBerries * 0.75
                    GameState.maxMushrooms = GameState.maxMushrooms * 0.75
                    tabContentFrame.post {
                        switchTab("Main")
                    }
                }
            })
        }

        if (GameState.alltimeFed >= 50 && GameState.spiceFed+GameState.rootsFed >= 30) {
            layout.addView(addButton("Trickhorse", "Allows your toki to evolve into a Trickhorse, an intelligent form which may learn much more than mundane horses.\n\n•Learns all skills quicker.\n•Can perform moves and abilities better.\n•Helps you acquire knowledge.") {
                {
                    GameState.tokiLvl = 1.0
                    GameState.maxKnowledge = GameState.maxKnowledge * 0.75
                    GameState.tokiHPRate =+ 0.75
                    tabContentFrame.post {
                        switchTab("Main")
                    }
                }
            })
        }

        tabContentFrame.addView(layout)
    }

    private fun showStatsTab() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }

        val upgradesText = TextView(this).apply {
            text = "Upgrades Acquired: ${GameState.upgradesAcquired}"
            textSize = 18f
        }

        layout.addView(upgradesText)

        val moneyText = TextView(this).apply {
            text = "Lifetime money acquired: ${GameState.alltimeEletro}"
            textSize = 18f
        }

        layout.addView(moneyText)

        tabContentFrame.addView(layout)
    }



    private fun showHomeTab() {
        val parentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val currentHome = GameState.getHomeByName(GameState.currentHome)
        val homeStatus = Button(this).apply {
            text = "Current Home: ${currentHome.name} (Space: ${GameState.space}/${GameState.maxSpace})"
            setOnClickListener { showHomeModal() }
            setOnLongClickListener {
                showDescriptionModal(currentHome.description)
                true
            }
        }
        layout.addView(homeStatus)

        val table = TableLayout(this)
        val header = TableRow(this)
        listOf("Furniture", "Space", "Owned", "Actions").forEach {
            header.addView(TextView(this).apply { text = it })
        }
        table.addView(header)

        val sortedFurniture = GameState.furnitureList.sortedBy { it.name }
        for (f in sortedFurniture) {
            val row = TableRow(this)

            val owned = GameState.furnitureOwned.getOrDefault(f.name, 0)
            val name = TextView(this).apply { text = f.name }
            val space = TextView(this).apply { text = f.space.toString() }
            val count = TextView(this).apply {
                text = if (f.maxAmount != null) "$owned/${f.maxAmount}" else "$owned"
            }

            val actions = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }

            val actionBtn = Button(this).apply {
                var modalShown = false
                var modalHandler: Handler? = null
                text = if (GameState.isSellMode) "Sell" else "Buy"
                isEnabled = if (GameState.isSellMode) owned > 0 else (f.maxAmount == null || owned < f.maxAmount) && (GameState.eletro >= f.cost.eletroCost)

                setOnClickListener {
                    if (GameState.isSellMode) {
                        if (owned > 0) {
                            GameState.furnitureOwned[f.name] = owned - 1
                            GameState.space += f.space
                            GameState.eletro += (f.cost.eletroCost / 2).toInt()
                            when (f.name) {
                                "Box" -> GameState.maxEletro -= 25
                                "Scroll rack" -> GameState.maxScrolls -= 15
                                "Windowbox" -> {
                                    GameState.maxHerbs -= 15
                                    GameState.maxMushrooms -= 5
                                }
                                "Cot" -> {
                                    GameState.HPRestRate -= 0.5
                                    GameState.staminaRestRate -= 0.5
                                }
                                "Food cabinet" -> {
                                    GameState.maxRoots -= 10
                                    GameState.maxGrain -= 10
                                    GameState.maxBones -= 10
                                    GameState.maxSpice -= 1
                                }
                                "Food pot" -> {
                                    GameState.maxMilk -= 10
                                    GameState.maxMeat -= 10
                                    GameState.maxBerries -= 10
                                    GameState.maxFruit -= 10
                                }
                            }
                            updateResourcePanel()
                            showHomeTab()
                        }
                    } else {
                        val current = GameState.furnitureOwned.getOrDefault(f.name, 0)
                        if (f.tags.contains("Resting place")) {
                            for ((key, amount) in GameState.furnitureOwned) {
                                val f2 = GameState.furnitureList.find { it.name == key } ?: continue
                                if (f2.tags.contains("Resting place") && key != f.name && amount > 0) {
                                    GameState.furnitureOwned[key] = 0
                                    GameState.space -= f2.space * amount
                                }
                            }
                        }
                        GameState.eletro -= f.cost.eletroCost
                        GameState.furnitureOwned[f.name] = current + 1
                        GameState.space -= f.space
                        when (f.name) {
                            "Box" -> GameState.maxEletro += 25
                            "Scroll rack" -> GameState.maxScrolls += 15
                            "Windowbox" -> {
                                GameState.maxHerbs += 15
                                GameState.maxMushrooms += 5
                            }
                            "Cot" -> {
                                GameState.HPRestRate += 0.5
                                GameState.staminaRestRate += 0.5
                            }
                            "Food cabinet" -> {
                                GameState.maxRoots += 10
                                GameState.maxGrain += 10
                                GameState.maxBones += 10
                                GameState.maxSpice += 1
                            }
                            "Food pot" -> {
                                GameState.maxMilk += 10
                                GameState.maxMeat += 10
                                GameState.maxBerries += 10
                                GameState.maxFruit += 10
                            }
                        }
                        updateResourcePanel()
                        showHomeTab()
                    }
                }
                setOnTouchListener { v, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            modalShown = false
                            modalHandler = Handler(Looper.getMainLooper()).apply {
                                postDelayed({
                                    modalShown = true
                                    showDescriptionModal(f.effect)
                                }, 500)
                            }
                            true
                        }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            modalHandler?.removeCallbacksAndMessages(null)
                            modalHandler = null
                            if (modalShown) {
                                hideDescriptionModal()
                            } else if (event.action == MotionEvent.ACTION_UP) {
                                v.performClick()
                            }
                            true
                        }
                        else -> false
                    }
                }
            }

            actions.addView(actionBtn)
            row.addView(name)
            row.addView(space)
            row.addView(count)
            row.addView(actions)
            table.addView(row)
        }

        layout.addView(table)
        parentLayout.addView(layout, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1f
        ))

        val sellModeCheckbox = CheckBox(this).apply {
            text = "Sell mode"
            isChecked = GameState.isSellMode
            setOnCheckedChangeListener { _, checked ->
                GameState.isSellMode = checked
                showHomeTab()
            }
        }
        val bottomBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL or Gravity.BOTTOM
            setPadding(24, 12, 24, 12)
            addView(sellModeCheckbox)
        }
        parentLayout.addView(bottomBar)

        tabContentFrame.removeAllViews()
        tabContentFrame.addView(parentLayout)
    }


    private fun showHomeModal() {
        val dialog = Dialog(this)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val overlay = FrameLayout(this).apply {
            setBackgroundColor(Color.parseColor("#80000000")) // Semi-transparent black for 'blur'
        }

        val modalContent = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
            setBackgroundColor(Color.WHITE)
            elevation = 12f
        }

        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val listLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        for (home in GameState.homes) {
            val box = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(24, 24, 24, 24)
                setBackgroundColor(Color.TRANSPARENT) // borderless
            }
            box.addView(TextView(this).apply {
                text = "${home.name} (Space: ${home.maxSpace})"
                textSize = 18f
            })
            box.addView(TextView(this).apply {
                text = home.description
                textSize = 14f
            })
            val moveBtn = Button(this).apply {
                text = if (GameState.currentHome == home.name) "Current" else "Move"
                isEnabled = GameState.currentHome != home.name &&
                        GameState.eletro >= home.costEletro &&
                        GameState.herbs >= home.costHerbs
                setOnClickListener {
                    GameState.eletro -= home.costEletro
                    GameState.herbs -= home.costHerbs
                    GameState.currentHome = home.name
                    GameState.maxSpace = home.maxSpace
                    GameState.space = (home.maxSpace - GameState.space)
                    GameState.homeHPrest = home.hpBonus
                    GameState.homeStaminarest = home.staminaBonus
                    updateResourcePanel()
                    showHomeTab()
                    dialog.dismiss()
                    Toast.makeText(context, "Moved to ${home.name}!", Toast.LENGTH_SHORT).show()
                }
            }
            box.addView(moveBtn)
            listLayout.addView(box)
        }

        scrollView.addView(listLayout)
        modalContent.addView(scrollView)

        modalContent.addView(Button(this).apply {
            text = "Close"
            setOnClickListener { dialog.dismiss() }
        })

        val root = FrameLayout(this)
        root.addView(overlay)
        root.addView(modalContent)

        dialog.setContentView(root)
        dialog.setCancelable(true)
        dialog.show()
    }

    private fun showSkillsTab() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }

        fun addSkillSection(title: String, skills: List<Skill>) {
            val sectionTitle = TextView(this).apply {
                text = title
                textSize = 18f
                setPadding(0, 16, 0, 8)
            }
            layout.addView(sectionTitle)
            for (skill in skills) {
                layout.addView(makeSkillBox(skill))
            }
        }

        addSkillSection("Player Skills", GameSkills.getSkillsByType(SkillType.PLAYER))
        addSkillSection("Toki Skills", GameSkills.getSkillsByType(SkillType.TOKI))

        tabContentFrame.removeAllViews()
        tabContentFrame.addView(layout)
    }

    private fun makeSkillBox(skill: Skill): LinearLayout {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, 16)
        }
        val topRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        val nameView = TextView(this).apply {
            text = skill.name
            textSize = 16f
            setPadding(0, 0, 8, 0)
        }
        val levelView = TextView(this).apply {
            text = "${skill.level} / ${skill.maxLevel}"
            textSize = 16f
            setPadding(8, 0, 0, 0)
            layoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            ).apply { setMargins(0,0,0,0) }
            gravity = Gravity.END
        }
        topRow.addView(nameView)
        topRow.addView(levelView)
        box.addView(topRow)

        val lowestRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        val expView = TextView(this).apply {
            text = "${skill.exp} / ${skill.expToNext}"
            setPadding(0, 0, 8, 0)
        }
        lowestRow.addView(expView)

        val spacer = Space(this)
        lowestRow.addView(spacer, LinearLayout.LayoutParams(0, 0, 1f))

        val trainButton = addButton("[TASK] Train skill", "Train this skill for 5 EXP/sec", isTask = true) {
            if (GameState.canStartTask() && !GameState.isTraining && skill.level < skill.maxLevel) {
                val trainSkillTask = {
                    startTask(
                        infinite = true,
                        label = "Training ${skill.name}...",
                        perSecond = {
                            if (skill.level >= skill.maxLevel) {
                                stopTask()
                                Toast.makeText(this, "Skill is maxed!", Toast.LENGTH_SHORT).show()
                            } else {
                                GameSkills.trainSkill(skill, 5)
                                expView.text = "${skill.exp} / ${skill.expToNext}"
                                levelView.text = "${skill.level} / ${skill.maxLevel}"
                            }
                        }
                    )
                }
                trainSkillTask()
                lastInterruptedTask = trainSkillTask
            }
        }
            trainButton.isEnabled = skill.canTrain()
            lowestRow.addView(trainButton)

            box.addView(lowestRow)
            return box
    }

    private fun showQuestTab() {
        tabContentFrame.removeAllViews()
        if (GameState.currentQuest != null && GameState.currentEncounterIndex != null) {
            val encounter = GameState.currentQuest!!.encounters[GameState.currentEncounterIndex!!]
            when (encounter) {
                is Encounter.Combat -> showCombatEncounter(encounter)
                is Encounter.Exploration -> showExplorationEncounter(encounter)
            }
            return
        }

        val scrollView = ScrollView(this)
        val gridLayout = GridLayout(this).apply {
            columnCount = 2
            orientation = GridLayout.HORIZONTAL
            setPadding(16, 16, 16, 16)
        }

        for (loc in GameState.questLocations) {
            val unlocked = GameState.unlockedQuestLocations.contains(loc.id) ||
                    (loc.requiredSkill == "Running" && (GameSkills.tokiSkills.find { it.name == "Running" }?.level ?: 0) >= loc.requiredSkillLevel)
            if (!unlocked) continue

            val box = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                setPadding(18, 18, 18, 18)
            }

            val nameView = TextView(this).apply {
                text = loc.name
                textSize = 18f
                gravity = Gravity.CENTER_HORIZONTAL
            }
            box.addView(nameView)

            val descView = TextView(this).apply {
                text = loc.description
                textSize = 12f
            }
            box.addView(descView)

            val counter = TextView(this).apply {
                val done = loc.encountersCompleted(GameState.questProgress)
                val total = loc.totalEncounters()
                text = "$done/$total"
                textSize = 14f
            }
            box.addView(counter)

            val embarkBtn = Button(this).apply {
                text = "Embark"
                isEnabled = loc.encountersCompleted(GameState.questProgress) < loc.totalEncounters()
                setOnClickListener {
                    GameState.currentQuest = loc
                    GameState.currentEncounterIndex = loc.encountersCompleted(GameState.questProgress)
                    GameState.currentEncounter = loc.encounters[GameState.currentEncounterIndex!!]
                    when (val e = GameState.currentEncounter) {
                        is Encounter.Combat -> {
                            GameState.currentCombatAllies = mutableListOf(
                                CombatParticipant(GameState.playerName ?: "Player",
                                    GameState.playerHP, GameState.maxPlayerHP, 2.0, 1.0, true),
                                CombatParticipant(GameState.tokiName ?: "Toki",
                                    GameState.tokiHP, GameState.maxTokiHP, 1.2, 1.2, true)
                            )
                            GameState.currentCombatEnemies = e.enemies.map { it.copy() }.toMutableList()
                            GameState.combatLog = mutableListOf("${e.name}: ${e.description}")
                        }
                        is Encounter.Exploration -> {
                            GameState.explorationStress = Triple(0, 0, 0)
                            GameState.explorationTimerMillis = System.currentTimeMillis() + e.baseDurationSec * 1000
                        }

                        null -> TODO()
                    }
                    showQuestTab()
                }
            }
            val bottomRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                addView(counter, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
                addView(embarkBtn)
            }
            box.addView(bottomRow)
            gridLayout.addView(box)
        }

        scrollView.addView(gridLayout)
        tabContentFrame.addView(scrollView)
    }

    private fun showCombatEncounter(encounter: Encounter.Combat) {
        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        val topRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val alliesView = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        for (ally in GameState.currentCombatAllies) {
            val t = TextView(this)
            t.text = "${ally.participantName}: ${ally.hp}/${ally.maxHp} HP"
            alliesView.addView(t)
        }
        topRow.addView(alliesView, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))

        val enemiesView = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        for (enemy in GameState.currentCombatEnemies) {
            val t = TextView(this)
            t.text = "${enemy.participantName}: ${enemy.hp}/${enemy.maxHp} HP"
            enemiesView.addView(t)
        }
        topRow.addView(enemiesView, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        layout.addView(topRow)

        val logView = TextView(this)
        logView.text = GameState.combatLog.takeLast(10).joinToString("\n")
        layout.addView(logView)

        tabContentFrame.removeAllViews()
        tabContentFrame.addView(layout)

        if (!GameState.isTaskRunning) {
            GameState.isTaskRunning = true
            Handler(Looper.getMainLooper()).postDelayed({ runCombatRound(encounter) }, 1000)
        }
    }

    private fun runCombatRound(encounter: Encounter.Combat) {
        if (!GameState.isTaskRunning) return

        for (ally in GameState.currentCombatAllies.filter { it.hp > 0 }) {
            val target = GameState.currentCombatEnemies.firstOrNull { it.hp > 0 }
            if (target != null) {
                val dmg = ally.attackPower
                target.hp -= dmg
                GameState.combatLog.add("${ally.participantName} attacks ${target.participantName} for $dmg!")
                if (target.hp <= 0) {
                    GameState.combatLog.add("${target.participantName} is defeated!")
                    for (drop in encounter.possibleDrops) {
                        if (Math.random() < drop.chance) {
                            GameState.combatLog.add("${target.participantName} dropped ${drop.amount} ${drop.item}!")
                            // TODO: Actually grant drops to player inventory ):
                        }
                    }
                }
            }
        }
        for (enemy in GameState.currentCombatEnemies.filter { it.hp > 0 }) {
            val target = GameState.currentCombatAllies.firstOrNull { it.hp > 0 }
            if (target != null) {
                val dmg = enemy.attackPower
                target.hp -= dmg
                GameState.combatLog.add("${enemy.participantName} attacks ${target.participantName} for $dmg!")
                if (target.hp <= 0) {
                    GameState.combatLog.add("${target.participantName} is defeated!")
                }
            }
        }
        GameState.currentCombatAllies.removeAll { it.hp <= 0 }
        GameState.currentCombatEnemies.removeAll { it.hp <= 0 }

        if (GameState.currentCombatAllies.none { it.hp > 0 }) {
            GameState.combatLog.add("All allies defeated! Quest failed.")
            endQuestEncounter()
        } else if (GameState.currentCombatEnemies.none { it.hp > 0 }) {
            GameState.combatLog.add("Victory! Proceeding to next encounter.")
            nextQuestEncounter()
        } else {
            Handler(Looper.getMainLooper()).postDelayed({ runCombatRound(encounter) }, 1000)
            showCombatEncounter(encounter)
        }
    }

    private fun showExplorationEncounter(encounter: Encounter.Exploration) {
        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        val title = TextView(this).apply {
            text = "${encounter.name}\n${encounter.description}"
            textSize = 16f
        }
        layout.addView(title)

        val timeRemaining = ((GameState.explorationTimerMillis - System.currentTimeMillis()) / 1000).coerceAtLeast(0)
        val timeView = TextView(this).apply {
            text = "Time remaining: ${timeRemaining}s"
        }
        layout.addView(timeView)

        val rewardsView = TextView(this).apply {
            text = "Rewards: " + encounter.rewards.joinToString { "${it.amount} ${it.item}" }
        }
        layout.addView(rewardsView)

        val (wear, frus, unease) = GameState.explorationStress
        val stressView = TextView(this).apply {
            text = "Weariness: $wear/${encounter.stressThresholds.maxWeariness}  " +
                    "Frustration: $frus/${encounter.stressThresholds.maxFrustration}  " +
                    "Unease: $unease/${encounter.stressThresholds.maxUnease}"
        }
        layout.addView(stressView)

        tabContentFrame.removeAllViews()
        tabContentFrame.addView(layout)

        if (!GameState.isTaskRunning) {
            GameState.isTaskRunning = true
            Handler(Looper.getMainLooper()).postDelayed({ runExplorationTick(encounter) }, 1000)
        }
    }

    private fun runExplorationTick(encounter: Encounter.Exploration) {
        if (!GameState.isTaskRunning) return

        val now = System.currentTimeMillis()
        if (now >= GameState.explorationTimerMillis) {
            GameState.combatLog.add("Exploration complete! Rewards earned.")
            nextQuestEncounter()
            return
        }

        var (wear, frus, unease) = GameState.explorationStress
        wear += encounter.stressIncrease.weariness
        frus += encounter.stressIncrease.frustration
        unease += encounter.stressIncrease.unease
        GameState.explorationStress = Triple(wear, frus, unease)

        if (wear >= encounter.stressThresholds.maxWeariness ||
            frus >= encounter.stressThresholds.maxFrustration ||
            unease >= encounter.stressThresholds.maxUnease) {
            GameState.combatLog.add("Stress too high! The party is forced to leave and rest.")
            endQuestEncounter()
            return
        }

        Handler(Looper.getMainLooper()).postDelayed({ runExplorationTick(encounter) }, 1000)
        showExplorationEncounter(encounter)
    }

    private fun nextQuestEncounter() {
        val loc = GameState.currentQuest ?: return
        val idx = GameState.currentEncounterIndex ?: return
        val nextIdx = idx + 1
        GameState.questProgress[loc.id] = nextIdx
        if (nextIdx >= loc.encounters.size) {
            // Quest complete
            GameState.currentQuest = null
            GameState.currentEncounterIndex = null
            GameState.currentEncounter = null
            GameState.isTaskRunning = false
            Toast.makeText(this, "Location complete!", Toast.LENGTH_SHORT).show()
            showQuestTab()
        } else {
            GameState.currentEncounterIndex = nextIdx
            GameState.currentEncounter = loc.encounters[nextIdx]
            GameState.isTaskRunning = false
            showQuestTab()
        }
    }

    private fun endQuestEncounter() {
        GameState.currentQuest = null
        GameState.currentEncounterIndex = null
        GameState.currentEncounter = null
        GameState.isTaskRunning = false
        showQuestTab()
    }

    private fun addButton(
        label: String,
        description: String,
        isTask: Boolean = false,
        onClick: () -> Unit
    ): Button {
        var modalShown = false
        var modalHandler: Handler? = null

        return Button(this).apply {
            text = label
            setBackgroundColor(if (isTask) "#AEDFF7".toColorInt() else Color.LTGRAY)

            setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        modalShown = false
                        modalHandler = Handler(Looper.getMainLooper()).apply {
                            postDelayed({
                                modalShown = true
                                showDescriptionModal(description)
                            }, 500)
                        }
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        modalHandler?.removeCallbacksAndMessages(null)
                        modalHandler = null

                        if (modalShown) {
                            hideDescriptionModal()
                        } else if (event.action == MotionEvent.ACTION_UP) {
                            v.performClick()
                        }
                        true
                    }
                    else -> false
                }
            }

            setOnClickListener { onClick() }
        }
    }

    private fun startTask(
        duration: Long = 0,
        label: String,
        infinite: Boolean = false,
        perSecond: () -> Unit,
        onComplete: (() -> Unit)? = null
    ) {
        stopTask()
        GameState.isTaskRunning = true
        GameState.isResting = label.contains("Rest", ignoreCase = true)

        var elapsed = 0L
        val interval = 1000L
        val handler = Handler(Looper.getMainLooper())

        val task = object : Runnable {
            override fun run() {
                if (!GameState.isTaskRunning) return

                if ((GameState.playerHP <= 0 || GameState.playerStamina <= 0) && !GameState.isResting) {
                    stopTask()
                    startRestTask()
                    return
                }

                perSecond()

                if (!infinite) {
                    elapsed += interval
                    val secondsLeft = ((duration - elapsed) / 1000).coerceAtLeast(0)
                    taskTimerText.text = "$label\nTime remaining: $secondsLeft sec"
                    if (elapsed >= duration) {
                        GameState.resetTasks()
                        taskTimerText.text = ""
                        onComplete?.invoke()
                        return
                    }
                } else {
                    taskTimerText.text = label
                }

                handler.postDelayed(this, interval)
            }
        }
        handler.post(task)
        taskHandler = handler
        taskRunnable = task
    }

    private fun startRestTask() {
        GameState.isResting = true
        startTask(
            infinite = true,
            label = "Resting...",
            perSecond = {
                GameState.playerHP = (GameState.playerHP + GameState.HPRestRate).coerceAtMost(GameState.maxPlayerHP)
                GameState.playerStamina = (GameState.playerStamina + GameState.staminaRestRate).coerceAtMost(GameState.maxPlayerStamina)
                updateResourcePanel()
                if (GameState.playerHP >= GameState.maxPlayerHP && GameState.playerStamina >= GameState.maxPlayerStamina) {
                    stopTask()
                    GameState.isResting = false
                    lastInterruptedTask?.let {
                        val toResume = it
                        lastInterruptedTask = null
                        toResume()
                    }
                }
            }
        )
    }

    private fun stopTask() {
        GameState.resetTasks()
        taskRunnable?.let {
            taskHandler?.removeCallbacks(it)
        }
        taskRunnable = null
        taskHandler = null
        taskTimerText.text = ""
    }

}