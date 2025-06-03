package com.tokitoki.game

data class Skill(
    val name: String,
    val maxLevel: Int,
    var level: Int = 0,
    var exp: Int = 0,
    var expToNext: Int = 50,
    val type: SkillType,
    val unlockTag: String? = null
) {
    fun addExp(amount: Int) {
        exp += amount
        while (exp >= expToNext && level < maxLevel) {
            exp -= expToNext
            level++
            expToNext = getExpForLevel(level)
        }
        if (level >= maxLevel) {
            exp = expToNext
        }
    }

    private fun getExpForLevel(lvl: Int): Int {
        return 50 + lvl * 20
    }

    fun canTrain(): Boolean {
        unlockTag ?: return true
        return GameState.hasFurnitureWithTag(unlockTag) || GameState.hasUpgradeWithTag(unlockTag)
    }
}

enum class SkillType {
    PLAYER, TOKI
}

object GameSkills {
    val playerSkills = mutableListOf(
        Skill("Martial", maxLevel = 5, type = SkillType.PLAYER, unlockTag = "Martialsource"),
        Skill("Herbalism", maxLevel = 5, type = SkillType.PLAYER, unlockTag = "Plantssource"),
        Skill("Crafting", maxLevel = 5, type = SkillType.PLAYER, unlockTag = "Craftsource")
    )

    val tokiSkills = mutableListOf<Skill>(
        Skill("Tracking", maxLevel = 5, type = SkillType.TOKI, unlockTag = "Toki handling")
    )

    fun getAllSkills(): List<Skill> = playerSkills + tokiSkills

    fun getSkillsByType(type: SkillType) =
        getAllSkills().filter { it.type == type }

    fun trainSkill(skill: Skill, expPerTick: Int = 5) {
        skill.addExp(expPerTick)
    }
}