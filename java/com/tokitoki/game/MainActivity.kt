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
        modalText.text = description
        modalContainer.visibility = View.VISIBLE

        rootLayout.animate()
            .alpha(0.3f)
            .setDuration(200)
            .start()
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
        val tabs = listOf("Main", "Stats", "Home", "Skills")
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

        resourcePanel.addView(makeHeader("Materials:"))

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

        resourcePanel.addView(makeHeader("Knowledge:"))

        val scrolls = "%.1f".format(GameState.scrolls)
        val maxScrolls = "%.1f".format(GameState.maxScrolls)
        if (GameState.maxScrolls > 0) {
            resourcePanel.addView(makeStatLine("Scrolls: $scrolls / $maxScrolls"))
        }

        resourcePanel.addView(makeHeader("Food:"))

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

        layout.addView(addButton("[ACTION] Beg for money", "Costs 1 Stamina, gives 1 Eletro") {
            if (GameState.playerStamina >= 1 && GameState.eletro < GameState.maxEletro) {
                GameState.playerStamina -= 1
                GameState.eletro += 1
                GameState.alltimeEletro += 1
                updateResourcePanel()
                tabContentFrame.post {
                    switchTab("Main")
                }
            }
        })

        layout.addView(addButton("[TASK] Clean stables", "10s task: 1 Stamina/s, gives 12 Eletro", isTask = true) {
            if (GameState.canStartTask() && GameState.playerStamina >= 10) {
                startTask(10_000, "Cleaning stables...", perSecond = {
                    GameState.playerStamina -= 1
                    updateResourcePanel()
                    tabContentFrame.post {
                        switchTab("Main")
                    }
                }) {
                    GameState.eletro += 12
                    updateResourcePanel()
                    tabContentFrame.post {
                        switchTab("Main")
                    }
                }
            }
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

        if (GameState.maxMilk > 0) {
            layout.addView(addButton("[ACTION] Buy Milk", "Costs 2 Eletro, gives 1 Milk") {
                if (GameState.eletro >= 2 && GameState.milk < GameState.maxMilk) {
                    GameState.eletro -= 2
                    GameState.milk += 1
                    GameState.alltimeMilk += 1
                    updateResourcePanel()
                    tabContentFrame.post {
                        switchTab("Main")
                    }
                }
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

        val craftingTitle = TextView(this).apply {
            text = "\nCrafting"
            textSize = 18f
            setPadding(8, 16, 8, 8)
        }
        layout.addView(craftingTitle)

        if (!GameState.hasWindchime) {
            layout.addView(addButton("[TASK] Windchime", "5s task: 0.5 Stamina/s & 1 Eletro/s", isTask = true) {
                if (GameState.canStartTask() && GameState.playerStamina >= 2.5 && GameState.eletro >= 5) {
                    startTask(5000, "Building windchime...", perSecond = {
                        GameState.playerStamina -= 0.5
                        GameState.eletro -= 1
                        updateResourcePanel()
                    }) {
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

        val tokiTitle = TextView(this).apply {
            text = "\nToki Handling"
            textSize = 18f
            setPadding(8, 16, 8, 8)
        }

        if (GameState.alltimeEletro >= 5) {layout.addView(tokiTitle)}

        if (GameState.alltimeEletro >= 5) {
            layout.addView(addButton("[ACTION] Feed Eletro", "Costs 5 Eletro, Helps the toki grow big") {
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
            layout.addView(addButton("[ACTION] Feed Milk", "Costs 2 Milk, Helps the toki grow strong") {
                if (GameState.milk >= 2) {
                    GameState.milk -= 2
                    GameState.milkFed += 2
                    tabContentFrame.post {
                        switchTab("Main")
                    }
                }
            })
        }

        if (GameState.alltimeHerbs >= 5) {
            layout.addView(addButton("[ACTION] Feed Herbs", "Costs 2 Herbs, Helps the toki grow wise") {
                if (GameState.herbs >= 2) {
                    GameState.herbs -= 2
                    GameState.herbsFed += 2
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
        if (GameState.alltimeFed >= 5) {layout.addView(evolveTitle)}

        if (GameState.alltimeFed >= 50 && GameState.milkFed+GameState.meatFed >= 30) {
            layout.addView(addButton("Warhorse", "Allows your toki to evolve into a Warhorse, a strong form focused in battle.\n\n•Learns martial skills quicker.\n•Deals more damage to enemies.\n•Has more HP.") {
                    {
                    GameState.tokiLvl = 1.0
                    GameState.tokiHP = 20.0
                    GameState.tokiHPRate = 0.75
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
        tabContentFrame.addView(layout)
    }

    private fun showHomeTab() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val homeStatus = Button(this).apply {
            text = "Current Home: ${GameState.currentHome} (Space: ${GameState.space}/${GameState.maxSpace})"

            setOnClickListener {
                showHomeDropdown(it)
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

            val actions = LinearLayout(this)
            val buyBtn = Button(this).apply {
                text = "Buy"
                isEnabled = (f.maxAmount == null || owned < f.maxAmount) && (GameState.eletro >= f.cost.eletroCost)
                setOnClickListener {
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
                        "Scroll rack" -> {
                            GameState.maxScrolls += 15
                            GameState.maxMilk +=15
                        }
                        "Windowbox" -> {
                            GameState.maxHerbs += 15
                            GameState.maxMushrooms += 5
                        }
                        "Cot" -> {
                            GameState.HPRestRate += 0.5
                            GameState.staminaRestRate += 0.5
                        }
                        "Food cabinet" -> {
                            GameState.HPRestRate += 0.5
                            GameState.staminaRestRate += 0.5
                        }
                        "Food pot" -> {
                            GameState.HPRestRate += 0.5
                            GameState.staminaRestRate += 0.5
                        }
                    }

                    tabContentFrame.post {
                        switchTab("Home")
                    }
                }
            }
            val sellBtn = Button(this).apply {
                text = "Sell"
                isEnabled = owned > 0
                setOnClickListener {
                    GameState.furnitureOwned[f.name] = owned - 1
                    GameState.space -= f.space
                    GameState.eletro += (f.cost.eletroCost / 2).toInt()
                    tabContentFrame.post {
                        switchTab("Home")
                    }
                }
            }

            buyBtn.setOnLongClickListener {
                Toast.makeText(this, f.effect, Toast.LENGTH_SHORT).show()
                true
            }

            sellBtn.setOnLongClickListener {
                Toast.makeText(this, f.effect, Toast.LENGTH_SHORT).show()
                true
            }

            actions.addView(buyBtn)
            actions.addView(sellBtn)

            row.addView(name)
            row.addView(space)
            row.addView(count)
            row.addView(actions)
            table.addView(row)
        }

        layout.addView(table)
        tabContentFrame.addView(layout)
    }

    private fun showHomeDropdown(view: View) {
        val popupMenu = PopupMenu(this, view)

        val availableHomes = listOf("Home A", "Home B", "Home C")

        availableHomes.forEachIndexed { index, home ->
            popupMenu.menu.add(0, index, index, home)
        }

        popupMenu.show()
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

        val bottomRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        val expView = TextView(this).apply {
            text = "${skill.exp} / ${skill.expToNext}"
            setPadding(0, 0, 8, 0)
        }
        bottomRow.addView(expView)

        val spacer = Space(this)
        bottomRow.addView(spacer, LinearLayout.LayoutParams(0, 0, 1f))

        val trainButton = addButton("[TASK] Train skill", "Train this skill for 5 EXP/sec", isTask = true) {
            if (GameState.canStartTask() && !GameState.isTraining) {
                startTask(
                    infinite = true, label = "Training ${skill.name}...", perSecond = {
                        GameSkills.trainSkill(skill, 5)
                        expView.text = "${skill.exp} / ${skill.expToNext}"
                        levelView.text = "${skill.level} / ${skill.maxLevel}"
                    }
                )
            } else if (GameState.isTraining) {
                stopTask()
            } else {
                Toast.makeText(this, "You cannot start another task right now.", Toast.LENGTH_SHORT).show()
            }
        }
        bottomRow.addView(trainButton)

        box.addView(bottomRow)
        return box
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