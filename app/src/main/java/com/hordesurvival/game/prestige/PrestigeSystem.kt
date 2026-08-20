package com.hordesurvival.game.prestige

/**
 * Prestige System — reset progress for permanent multipliers.
 * After reaching certain milestones, players can "prestige" to gain
 * permanent bonuses that apply to all future runs.
 */
object PrestigeSystem {

    data class PrestigeLevel(
        val level: Int,
        val name: String,
        val requirement: Int,        // total gold needed to prestige
        val mightMultiplier: Float,  // permanent × bonus
        val goldMultiplier: Float,
        val xpMultiplier: Float,
        val speedMultiplier: Float,
        val luckMultiplier: Float,
        val icon: String
    )

    val prestigeLevels = listOf(
        PrestigeLevel(1, "Bronze Star", 10000, 1.02f, 1.05f, 1.02f, 1.01f, 1.01f, "⭐"),
        PrestigeLevel(2, "Silver Star", 50000, 1.04f, 1.10f, 1.04f, 1.02f, 1.02f, "🌟"),
        PrestigeLevel(3, "Gold Star", 200000, 1.06f, 1.15f, 1.06f, 1.03f, 1.03f, "💫"),
        PrestigeLevel(4, "Diamond Star", 1000000, 1.10f, 1.25f, 1.10f, 1.05f, 1.05f, "💎"),
        PrestigeLevel(5, "Cosmic Star", 5000000, 1.15f, 1.40f, 1.15f, 1.08f, 1.08f, "🔮")
    )

    fun canPrestige(totalGoldEarned: Int, currentPrestigeLevel: Int): Boolean {
        if (currentPrestigeLevel >= prestigeLevels.size) return false
        return totalGoldEarned >= prestigeLevels[currentPrestigeLevel].requirement
    }

    fun getNextPrestige(currentLevel: Int): PrestigeLevel? {
        return prestigeLevels.getOrNull(currentLevel)
    }

    fun getCurrentBonuses(prestigeLevel: Int): PrestigeBonuses {
        if (prestigeLevel <= 0) return PrestigeBonuses()
        val level = prestigeLevels.getOrNull(prestigeLevel - 1) ?: return PrestigeBonuses()
        return PrestigeBonuses(
            mightMultiplier = level.mightMultiplier,
            goldMultiplier = level.goldMultiplier,
            xpMultiplier = level.xpMultiplier,
            speedMultiplier = level.speedMultiplier,
            luckMultiplier = level.luckMultiplier,
            hpMultiplier = level.xpMultiplier  // HP scales same as XP
        )
    }

    data class PrestigeBonuses(
        val mightMultiplier: Float = 1f,
        val goldMultiplier: Float = 1f,
        val xpMultiplier: Float = 1f,
        val speedMultiplier: Float = 1f,
        val luckMultiplier: Float = 1f,
        val hpMultiplier: Float = 1f
    )
}
