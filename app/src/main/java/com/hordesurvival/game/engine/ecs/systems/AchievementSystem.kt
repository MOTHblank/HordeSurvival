package com.hordesurvival.game.engine.ecs.systems

import com.hordesurvival.game.component.*
import com.hordesurvival.game.engine.GameEngine
import com.hordesurvival.game.engine.ecs.Entity
import com.hordesurvival.game.engine.ecs.System
import com.hordesurvival.game.weapon.WeaponEvolution

/**
 * Tracks achievements during gameplay.
 * Checks conditions each frame and marks achievements as unlocked.
 */
class AchievementSystem(private val engine: GameEngine) : System() {

    val achievements = mutableListOf(
        AchievementState(AchievementType.FIRST_KILL, "First Blood", "Kill your first enemy"),
        AchievementState(AchievementType.KILL_100, "Centurion", "Kill 100 enemies"),
        AchievementState(AchievementType.KILL_1000, "Exterminator", "Kill 1000 enemies"),
        AchievementState(AchievementType.SURVIVE_5MIN, "Survivor", "Survive for 5 minutes"),
        AchievementState(AchievementType.SURVIVE_10MIN, "Veteran", "Survive for 10 minutes"),
        AchievementState(AchievementType.SURVIVE_30MIN, "Legend", "Survive for 30 minutes"),
        AchievementState(AchievementType.REACH_LEVEL_10, "Getting Stronger", "Reach level 10"),
        AchievementState(AchievementType.REACH_LEVEL_25, "Powerhouse", "Reach level 25"),
        AchievementState(AchievementType.REACH_LEVEL_50, "Ascended", "Reach level 50"),
        AchievementState(AchievementType.KILL_FIRST_BOSS, "Boss Slayer", "Kill your first boss"),
        AchievementState(AchievementType.COMBO_25, "Combo Master", "Reach 25 combo"),
        AchievementState(AchievementType.COMBO_50, "Combo Legend", "Reach 50 combo"),
        AchievementState(AchievementType.COLLECT_ALL_WEAPONS, "Arsenal", "Collect all 8 weapons in one run"),
        AchievementState(AchievementType.USE_EVOLUTION, "Evolved", "Use a weapon evolution")
    )

    private var lastBossKills = 0

    var onAchievementUnlocked: ((AchievementState) -> Unit)? = null

    override fun update(dt: Float, entities: List<Entity>) {
        val player = engine.playerEntity ?: return
        val comp = player.get<PlayerComponent>() ?: return

        val kills = comp.totalKills
        val level = comp.level
        val time = engine.gameTime
        val combo = player.get<ComboComponent>()?.maxCombo ?: 0
        val weaponCount = comp.weapons.size
        val hasEvolution = comp.weapons.any { weapon ->
            val tier = comp.passiveLevels["WPN_${weapon.name}"] ?: 1
            WeaponEvolution.findEvolution(weapon, comp.passiveLevels) != null && tier >= 5
        }

        // Boss kills — track via entity count changes
        val currentBossKills = entities.count { it.tag == "enemy" && it.get<EnemyComponent>()?.isBoss == true && !it.active && it.get<HealthComponent>()?.isDead == true }

        check(AchievementType.FIRST_KILL, kills >= 1)
        check(AchievementType.KILL_100, kills >= 100)
        check(AchievementType.KILL_1000, kills >= 1000)
        check(AchievementType.SURVIVE_5MIN, time >= 300f)
        check(AchievementType.SURVIVE_10MIN, time >= 600f)
        check(AchievementType.SURVIVE_30MIN, time >= 1800f)
        check(AchievementType.REACH_LEVEL_10, level >= 10)
        check(AchievementType.REACH_LEVEL_25, level >= 25)
        check(AchievementType.REACH_LEVEL_50, level >= 50)
        check(AchievementType.KILL_FIRST_BOSS, currentBossKills > lastBossKills)
        check(AchievementType.COMBO_25, combo >= 25)
        check(AchievementType.COMBO_50, combo >= 50)
        check(AchievementType.COLLECT_ALL_WEAPONS, weaponCount >= 8)
        check(AchievementType.USE_EVOLUTION, hasEvolution)

        lastBossKills = currentBossKills
    }

    private fun check(type: AchievementType, condition: Boolean) {
        val achievement = achievements.find { it.type == type } ?: return
        if (!achievement.unlocked && condition) {
            achievement.unlocked = true
            onAchievementUnlocked?.invoke(achievement)
        }
    }

    fun getUnlockedCount(): Int = achievements.count { it.unlocked }
    fun getTotalCount(): Int = achievements.size

    override fun dispose() { lastBossKills = 0 }
}
