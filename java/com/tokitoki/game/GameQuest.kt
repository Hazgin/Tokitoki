package com.tokitoki.game

data class QuestLocation(
    val id: String,
    val name: String,
    val description: String,
    val encounters: List<Encounter>,
    val requiredSkill: String = "Running",
    val requiredSkillLevel: Int = 1
) {
    fun encountersCompleted(progress: Map<String, Int>) = progress[id] ?: 0
    fun totalEncounters() = encounters.size
}

sealed class Encounter(val name: String, val description: String) {
    class Combat(
        name: String,
        description: String,
        val enemies: List<CombatParticipant>,
        val possibleDrops: List<ItemDrop> = emptyList()
    ) : Encounter(name, description)

    class Exploration(
        name: String,
        description: String,
        val baseDurationSec: Int,
        val rewards: List<ItemDrop>,
        val stressIncrease: StressEffect = StressEffect(),
        val stressThresholds: StressThresholds = StressThresholds()
    ) : Encounter(name, description)
}

data class CombatParticipant(
    val participantName: String,
    var hp: Double,
    val maxHp: Double,
    val attackPower: Double,
    val attackSpeed: Double, // ApS
    val isAlly: Boolean = false
)

data class ItemDrop(
    val item: String,
    val amount: Int = 1,
    val chance: Double = 1.0 // 1.0 = 100% bobao
)

data class StressEffect(
    val weariness: Int = 1,
    val frustration: Int = 1,
    val unease: Int = 1
)

data class StressThresholds(
    val maxWeariness: Int = 10,
    val maxFrustration: Int = 10,
    val maxUnease: Int = 10
)