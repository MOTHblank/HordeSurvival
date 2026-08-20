package com.hordesurvival.game.mode

import com.hordesurvival.game.enemy.EnemyType
import com.hordesurvival.game.weapon.WeaponType
import java.util.Calendar

/**
 * Daily Challenge Mode — unique modifiers every day.
 * Same challenge for all players worldwide.
 */
object DailyChallenge {

    data class ChallengeModifiers(
        val name: String,
        val description: String,
        val icon: String,
        val enemyHpMult: Float = 1f,
        val enemySpdMult: Float = 1f,
        val enemyDmgMult: Float = 1f,
        val spawnRateMult: Float = 1f,
        val healthDropMult: Float = 1f,
        val goldMult: Float = 1f,
        val xpMult: Float = 1f,
        val playerHpMult: Float = 1f,
        val playerDmgMult: Float = 1f,
        val onlyEnemyType: EnemyType? = null,
        val forcedWeapon: WeaponType? = null,
        val noHealthDrops: Boolean = false,
        val noXpDrops: Boolean = false,
        val fogOfWar: Boolean = false,
        val speedMode: Boolean = false,
        val maxEnemies: Int = 300
    )

    /** Get today's challenge — seeded by date */
    fun getTodayChallenge(): ChallengeModifiers {
        val today = getDaySeed()

        val challenges = listOf(
            // Swarm Day — only swarm bats, tons of them
            ChallengeModifiers(
                name = "Swarm Day",
                description = "Only swarm bats. Thousands of them.",
                icon = "🦇",
                onlyEnemyType = EnemyType.SWARM_BAT,
                spawnRateMult = 3f,
                maxEnemies = 500,
                enemyHpMult = 0.5f,
                goldMult = 0.5f
            ),
            // Iron Wall — only tanks and knights
            ChallengeModifiers(
                name = "Iron Wall",
                description = "Heavy enemies only. Bring big guns.",
                icon = "🛡️",
                onlyEnemyType = EnemyType.TANK_GOLEM,
                spawnRateMult = 0.5f,
                enemyHpMult = 3f,
                enemyDmgMult = 2f,
                goldMult = 2f
            ),
            // Speed Run — everything is2x faster
            ChallengeModifiers(
                name = "Speed Demon",
                description = "2x speed. Everything. Including you.",
                icon = "💨",
                enemySpdMult = 2f,
                spawnRateMult = 1.5f,
                speedMode = true,
                xpMult = 1.5f
            ),
            // No Healing — no health drops
            ChallengeModifiers(
                name = "No Mercy",
                description = "No health drops. Don't get hit.",
                icon = "💀",
                noHealthDrops = true,
                enemyDmgMult = 1.5f,
                goldMult = 2f
            ),
            // Ghost Protocol — only ghosts
            ChallengeModifiers(
                name = "Ghost Protocol",
                description = "Invisible enemies everywhere.",
                icon = "👻",
                onlyEnemyType = EnemyType.GHOST,
                spawnRateMult = 2f,
                enemySpdMult = 1.5f,
                fogOfWar = true
            ),
            // Mage Academy — only mages
            ChallengeModifiers(
                name = "Mage Academy",
                description = "Bullet hell. Mages everywhere.",
                icon = "🧙",
                onlyEnemyType = EnemyType.MAGE,
                spawnRateMult = 2f,
                maxEnemies = 200,
                xpMult = 2f
            ),
            // Tank Parade — massive HP enemies
            ChallengeModifiers(
                name = "Iron Parade",
                description = "Enemies have 5x HP. Good luck.",
                icon = "🏰",
                enemyHpMult = 5f,
                spawnRateMult = 0.3f,
                goldMult = 5f,
                xpMult = 3f
            ),
            // Glass Cannon — player has1HP, but 3x damage
            ChallengeModifiers(
                name = "Glass Cannon",
                description = "1 HP. 3x damage. One shot kills you.",
                icon = "⚡",
                playerHpMult = 0.01f,
                playerDmgMult = 3f,
                noHealthDrops = true,
                goldMult = 3f
            ),
            // Boss Rush — bosses every 20 seconds
            ChallengeModifiers(
                name = "Boss Marathon",
                description = "Bosses every 20 seconds. Endless.",
                icon = "👹",
                spawnRateMult = 0.5f,
                enemyHpMult = 2f,
                goldMult = 3f,
                xpMult = 2f
            ),
            // Zen Mode — easy, relaxed, high XP
            ChallengeModifiers(
                name = "Zen Garden",
                description = "Easy enemies, high XP. Relax.",
                icon = "🌸",
                enemyHpMult = 0.3f,
                enemyDmgMult = 0.3f,
                enemySpdMult = 0.5f,
                spawnRateMult = 0.5f,
                xpMult = 3f,
                healthDropMult = 3f,
                goldMult = 2f
            )
        )

        return challenges[today % challenges.size]
    }

    private fun getDaySeed(): Int {
        val cal = Calendar.getInstance()
        return cal.get(Calendar.YEAR) * 10000 + (cal.get(Calendar.MONTH) + 1) * 100 + cal.get(Calendar.DAY_OF_MONTH)
    }

    /** Get the daily leaderboard key (date-based) */
    fun getDailyKey(): String {
        val cal = Calendar.getInstance()
        return "${cal.get(Calendar.YEAR)}-${(cal.get(Calendar.MONTH) + 1).toString().padStart(2, '0')}-${cal.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')}"
    }
}
