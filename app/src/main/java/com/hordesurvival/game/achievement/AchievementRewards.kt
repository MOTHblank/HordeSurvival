package com.hordesurvival.game.achievement

/**
 * Achievement Rewards — unlock permanent bonuses by completing achievements.
 * Each achievement grants a specific reward when unlocked.
 */
object AchievementRewards {

    enum class RewardType {
        GOLD,               // One-time gold bonus
        PASSIVE_BUFF,       // Permanent stat increase
        PET_UNLOCK,         // Unlock a companion pet
        SKIN_UNLOCK,        // Unlock a character skin
        BLESSING_UNLOCK,    // Unlock a new blessing option
        SPECIAL_WEAPON      // Unlock a unique weapon variant
    }

    data class AchievementReward(
        val achievementId: String,
        val name: String,
        val description: String,
        val rewardType: RewardType,
        val rewardValue: Float = 0f,
        val rewardName: String = "",
        val icon: String = "🏆"
    )

    val rewards = listOf(
        AchievementReward("first_kill", "First Blood", "Kill your first enemy", RewardType.GOLD, 50f, icon = "⚔️"),
        AchievementReward("kill_100", "Centurion", "Boop 100 monsters in one run", RewardType.GOLD, 200f, icon = "👾"),
        AchievementReward("kill_1000", "Mega Boper", "Boop 1000 monsters in one run", RewardType.PASSIVE_BUFF, 0.05f, "Might +5%", "🗡️"),
        AchievementReward("survive_5min", "Survivor", "Survive for 5 minutes", RewardType.GOLD, 100f, icon = "⏱️"),
        AchievementReward("survive_10min", "Veteran", "Survive for 10 minutes", RewardType.GOLD, 300f, icon = "🏅"),
        AchievementReward("survive_30min", "Legend", "Survive for 30 minutes", RewardType.PASSIVE_BUFF, 0.10f, "HP +10%", "👑"),
        AchievementReward("reach_level_10", "Apprentice", "Reach level 10", RewardType.GOLD, 150f, icon = "📚"),
        AchievementReward("reach_level_25", "Master", "Reach level 25", RewardType.PASSIVE_BUFF, 0.05f, "XP +5%", "📖"),
        AchievementReward("reach_level_50", "Grandmaster", "Reach level 50", RewardType.PET_UNLOCK, 0f, "Owl Pet", "🦉"),
        AchievementReward("kill_first_boss", "Boss Slayer", "Kill your first boss", RewardType.GOLD, 500f, icon = "👹"),
        AchievementReward("combo_25", "Combo Master", "Reach 25 combo", RewardType.PASSIVE_BUFF, 0.03f, "Luck +3%", "🔥"),
        AchievementReward("combo_50", "Combo Legend", "Reach 50 combo", RewardType.PET_UNLOCK, 0f, "Dragon Pet", "🐉"),
        AchievementReward("collect_all_weapons", "Arsenal", "Collect all 8 weapons in one run", RewardType.SPECIAL_WEAPON, 0f, "Weapon Evolver", "✨"),
        AchievementReward("use_evolution", "Evolved", "Evolve a weapon for the first time", RewardType.BLESSING_UNLOCK, 0f, "Arcane Blessing", "🌟")
    )

    fun getReward(achievementId: String): AchievementReward? {
        return rewards.find { it.achievementId == achievementId }
    }

    fun getUnlockedRewards(unlockedIds: List<String>): List<AchievementReward> {
        return rewards.filter { it.achievementId in unlockedIds }
    }
}
