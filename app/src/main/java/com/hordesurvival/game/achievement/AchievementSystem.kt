package com.hordesurvival.game.achievement

/**
 * Achievement definitions — track player milestones and grant rewards.
 */
enum class Achievement(
    val displayName: String,
    val description: String,
    val icon: String,
    val goldReward: Int,
    val condition: (AchievementTracker) -> Boolean
) {
    FIRST_BLOOD("First Blood", "Kill your first enemy", "⚔️", 10,
        { it.totalKills >= 1 }),
    KILLER_100("Century", "Kill 100 enemies in one run", "💯", 50,
        { it.killsThisRun >= 100 }),
    KILLER_500("War Machine", "Kill 500 enemies in one run", "🔥", 100,
        { it.killsThisRun >= 500 }),
    KILLER_1000("Genocide", "Kill 1000 enemies in one run", "💀", 200,
        { it.killsThisRun >= 1000 }),
    SURVIVOR_5("Survivor", "Survive for 5 minutes", "⏱️", 30,
        { it.gameTime >= 300f }),
    SURVIVOR_10("Veteran", "Survive for 10 minutes", "🛡️", 60,
        { it.gameTime >= 600f }),
    SURVIVOR_15("Legend", "Survive for 15 minutes", "👑", 150,
        { it.gameTime >= 900f }),
    LEVEL_10("Apprentice", "Reach level 10", "📖", 30,
        { it.playerLevel >= 10 }),
    LEVEL_25("Master", "Reach level 25", "📚", 80,
        { it.playerLevel >= 25 }),
    LEVEL_50("Grandmaster", "Reach level 50", "🎓", 200,
        { it.playerLevel >= 50 }),
    BOSS_KILLER("Boss Slayer", "Defeat your first boss", "👹", 100,
        { it.bossesKilled >= 1 }),
    BOSS_HUNTER("Boss Hunter", "Defeat 5 bosses", "🎯", 250,
        { it.bossesKilled >= 5 }),
    EVOLUTION_FIRST("Evolution!", "Evolve a weapon for the first time", "✨", 150,
        { it.evolutionsUsed >= 1 }),
    EVOLUTION_ALL("Completionist", "Evolve all 8 weapons", "🏆", 500,
        { it.evolutionsUsed >= 8 }),
    COMBO_25("Combo Master", "Reach a 25-hit combo", "⚡", 80,
        { it.maxCombo >= 25 }),
    COMBO_50("Combo King", "Reach a 50-hit combo", "👑", 200,
        { it.maxCombo >= 50 }),
    WEAPONS_3("Arsenal", "Have 3 weapons at once", "🗡️", 40,
        { it.weaponsOwned >= 3 }),
    WEAPONS_6("Fully Loaded", "Have 6 weapons at once", "⚔️", 100,
        { it.weaponsOwned >= 6 }),
    GOLD_1000("Rich", "Earn 1000 gold total", "💰", 50,
        { it.totalGold >= 1000f }),
    GOLD_10000("Millionaire", "Earn 10000 gold total", "💎", 200,
        { it.totalGold >= 10000f }),
    NO_DAMAGE("Untouchable", "Survive 2 minutes without taking damage", "👻", 300,
        { it.noDamageTime >= 120f });

    companion object {
        val total = entries.size
    }
}

/**
 * Tracks player statistics for achievement checking.
 */
data class AchievementTracker(
    var totalKills: Int = 0,
    var killsThisRun: Int = 0,
    var gameTime: Float = 0f,
    var playerLevel: Int = 1,
    var bossesKilled: Int = 0,
    var evolutionsUsed: Int = 0,
    var maxCombo: Int = 0,
    var weaponsOwned: Int = 0,
    var totalGold: Float = 0f,
    var noDamageTime: Float = 0f,
    val unlockedAchievements: MutableSet<String> = mutableSetOf()
) {
    fun checkNewAchievements(): List<Achievement> {
        val newAchievements = mutableListOf<Achievement>()
        for (achievement in Achievement.entries) {
            if (achievement.name !in unlockedAchievements && achievement.condition(this)) {
                unlockedAchievements.add(achievement.name)
                newAchievements.add(achievement)
            }
        }
        return newAchievements
    }
}
