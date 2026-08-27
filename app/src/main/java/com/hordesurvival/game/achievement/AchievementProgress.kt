package com.hordesurvival.game.achievement

/**
 * Achievement Progress — tracks partial progress toward achievements.
 * Shows progress bars in the UI.
 */
object AchievementProgress {

    data class AchievementTracker(
        val id: String,
        val name: String,
        val description: String,
        val icon: String,
        val targetValue: Int,
        var currentValue: Int = 0,
        var unlocked: Boolean = false
    ) {
        val progress: Float get() = (currentValue.toFloat() / targetValue).coerceIn(0f, 1f)
        val progressPercent: Int get() = (progress * 100).toInt()
        val isComplete: Boolean get() = currentValue >= targetValue
    }

    val allAchievements = listOf(
        AchievementTracker("first_kill", "First Blood", "Kill your first enemy", "⚔️", 1),
        AchievementTracker("kill_100", "Centurion", "Kill 100 enemies", "👾", 100),
        AchievementTracker("kill_1000", "Mega Boper", "Boop 1,000 monsters", "🗡️", 1000),
        AchievementTracker("kill_5000", "Monster Sweeper", "Boop 5,000 monsters", "✨", 5000),
        AchievementTracker("survive_5min", "Survivor", "Survive for 5 minutes", "⏱️", 300),
        AchievementTracker("survive_10min", "Veteran", "Survive for 10 minutes", "🏅", 600),
        AchievementTracker("survive_30min", "Legend", "Survive for 30 minutes", "👑", 1800),
        AchievementTracker("survive_60min", "Immortal", "Survive for 60 minutes", "🌟", 3600),
        AchievementTracker("reach_level_10", "Apprentice", "Reach level 10", "📚", 10),
        AchievementTracker("reach_level_25", "Master", "Reach level 25", "📖", 25),
        AchievementTracker("reach_level_50", "Grandmaster", "Reach level 50", "🎓", 50),
        AchievementTracker("reach_level_100", "Transcendent", "Reach level 100", "✨", 100),
        AchievementTracker("kill_first_boss", "Boss Slayer", "Kill your first boss", "👹", 1),
        AchievementTracker("kill_10_bosses", "Boss Hunter", "Kill 10 bosses", "🐉", 10),
        AchievementTracker("combo_25", "Combo Master", "Reach 25 combo", "🔥", 25),
        AchievementTracker("combo_50", "Combo Legend", "Reach 50 combo", "💥", 50),
        AchievementTracker("combo_100", "Combo God", "Reach 100 combo", "⚡", 100),
        AchievementTracker("collect_all_weapons", "Arsenal", "Collect all 8 weapons", "✨", 8),
        AchievementTracker("use_evolution", "Evolved", "Evolve a weapon", "🌟", 1),
        AchievementTracker("collect_5_relics", "Relic Hunter", "Collect 5 relics in one run", "💎", 5),
        AchievementTracker("earn_1000_gold", "Rich", "Earn 1,000 gold in one run", "💰", 1000),
        AchievementTracker("no_damage_60s", "Untouchable", "Survive 60s without damage", "🛡️", 60),
        AchievementTracker("td_level_5", "Tower Defender", "Reach TD level 5", "🏗️", 5),
        AchievementTracker("td_level_10", "Tower Master", "Clear all 10 TD levels", "🏰", 10),
        AchievementTracker("daily_complete", "Daily Hero", "Complete a daily challenge", "📅", 1)
    )

    /** Update progress for a specific achievement type with exact prefix matching */
    fun updateProgress(type: String, value: Int) {
        for (achievement in allAchievements) {
            if (!achievement.unlocked) {
                val matches = when (type) {
                    "kill" -> achievement.id == "first_kill" || achievement.id == "kill_100" || achievement.id == "kill_1000" || achievement.id == "kill_5000"
                    "kill_boss" -> achievement.id == "kill_first_boss" || achievement.id == "kill_10_bosses"
                    "reach_level" -> achievement.id.startsWith("reach_level_")
                    "survive" -> achievement.id.startsWith("survive_")
                    "combo" -> achievement.id.startsWith("combo_")
                    else -> achievement.id == type
                }
                if (matches) {
                    achievement.currentValue = value
                    if (achievement.isComplete) {
                        achievement.unlocked = true
                    }
                }
            }
        }
    }

    /** Increment a counter-based achievement */
    fun increment(type: String, amount: Int = 1) {
        for (achievement in allAchievements) {
            if (achievement.id.startsWith(type) && !achievement.unlocked) {
                achievement.currentValue += amount
                if (achievement.isComplete) {
                    achievement.unlocked = true
                }
            }
        }
    }

    /** Get all achievements sorted by progress */
    fun getSortedByProgress(): List<AchievementTracker> {
        return allAchievements.sortedWith(
            compareByDescending<AchievementTracker> { it.unlocked }
                .thenByDescending { it.progress }
        )
    }

    /** Get unlocked count */
    fun getUnlockedCount(): Int = allAchievements.count { it.unlocked }

    /** Get total count */
    fun getTotalCount(): Int = allAchievements.size

    fun reset() {
        for (a in allAchievements) {
            a.currentValue = 0
            a.unlocked = false
        }
    }
}
