package com.hordesurvival.game.mode

import java.util.Calendar

/**
 * Quest and Daily Challenge system.
 * Provides structured goals beyond simple survival.
 */
enum class GameModeType {
    SURVIVAL,
    QUEST,
    DAILY_CHALLENGE,
    TOWER_DEFENSE,
    BOSS_RUSH_EXTREME
}

data class Quest(
    val id: String,
    val title: String,
    val description: String,
    val targetValue: Int,
    val rewardGold: Int,
    val questType: QuestType
)

enum class QuestType {
    SURVIVE_TIME,      // Survive X seconds
    KILL_ENEMIES,      // Kill X enemies
    REACH_LEVEL,       // Reach level X
    KILL_BOSS,         // Kill X bosses
    USE_WEAPON,        // Use specific weapon for X kills
    COLLECT_GOLD,      // Collect X gold in one run
    NO_DAMAGE_STREAK,  // Survive X seconds without taking damage
    COMBO_REACH        // Reach X combo
}

object QuestManager {

    /** Get daily quests — seeded by date so same quests all day */
    fun getDailyQuests(): List<Quest> {
        val today = Calendar.getInstance().let {
            it.get(Calendar.YEAR) * 10000 + it.get(Calendar.MONTH) * 100 + it.get(Calendar.DAY_OF_MONTH)
        }

        return listOf(
            Quest(
                id = "daily_survive_${today}",
                title = "Survivor",
                description = "Survive for 5 minutes",
                targetValue = 300,
                rewardGold = 50,
                questType = QuestType.SURVIVE_TIME
            ),
            Quest(
                id = "daily_kill_${today}",
                title = "Exterminator",
                description = "Kill 200 enemies",
                targetValue = 200,
                rewardGold = 75,
                questType = QuestType.KILL_ENEMIES
            ),
            Quest(
                id = "daily_level_${today}",
                title = "Ascension",
                description = "Reach level 15",
                targetValue = 15,
                rewardGold = 100,
                questType = QuestType.REACH_LEVEL
            )
        )
    }

    /** Get story quests (progressive) */
    fun getStoryQuests(): List<Quest> {
        return listOf(
            Quest("story_1", "First Steps", "Survive for 2 minutes", 120, 30, QuestType.SURVIVE_TIME),
            Quest("story_2", "Getting Stronger", "Reach level 5", 5, 40, QuestType.REACH_LEVEL),
            Quest("story_3", "Monster Hunter", "Kill 50 enemies", 50, 50, QuestType.KILL_ENEMIES),
            Quest("story_4", "Boss Slayer", "Defeat your first boss", 1, 100, QuestType.KILL_BOSS),
            Quest("story_5", "Combo Master", "Reach 20 combo", 20, 60, QuestType.COMBO_REACH),
            Quest("story_6", "Gold Rush", "Collect 100 gold in one run", 100, 80, QuestType.COLLECT_GOLD),
            Quest("story_7", "Untouchable", "Survive 60 seconds without damage", 60, 120, QuestType.NO_DAMAGE_STREAK),
            Quest("story_8", "Legendary", "Reach level 30", 30, 200, QuestType.REACH_LEVEL),
        )
    }

    /** Check if a quest is completed based on run stats */
    fun checkQuestCompletion(
        quest: Quest,
        timeSurvived: Float,
        kills: Int,
        level: Int,
        goldEarned: Int,
        maxCombo: Int,
        bossesKilled: Int
    ): Boolean {
        return when (quest.questType) {
            QuestType.SURVIVE_TIME -> timeSurvived >= quest.targetValue
            QuestType.KILL_ENEMIES -> kills >= quest.targetValue
            QuestType.REACH_LEVEL -> level >= quest.targetValue
            QuestType.KILL_BOSS -> bossesKilled >= quest.targetValue
            QuestType.COLLECT_GOLD -> goldEarned >= quest.targetValue
            QuestType.COMBO_REACH -> maxCombo >= quest.targetValue
            QuestType.USE_WEAPON -> false  // needs weapon tracking
            QuestType.NO_DAMAGE_STREAK -> false  // needs damage tracking
        }
    }
}
