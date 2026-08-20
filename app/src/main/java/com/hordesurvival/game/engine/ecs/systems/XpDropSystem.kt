package com.hordesurvival.game.engine.ecs.systems

import com.hordesurvival.game.component.*
import com.hordesurvival.game.engine.GameEngine
import com.hordesurvival.game.engine.ecs.Entity
import com.hordesurvival.game.engine.ecs.System
import com.hordesurvival.game.audio.SoundManager
import com.hordesurvival.game.enemy.EnemyType
import com.hordesurvival.utils.GameMath

/**
 * Handles enemy death: XP drops, gold, split-on-death, death effects.
 * Fixed: split enemies have CollisionComponent, proper cleanup.
 */
class XpDropSystem(private val engine: GameEngine) : System() {

    // Direct reference to TD mode for gold/score tracking
    var towerDefense: com.hordesurvival.game.mode.TowerDefenseMode? = null

    override fun update(dt: Float, entities: List<Entity>) {
        val player = entities.find { it.tag == "player" }
        val playerComp = player?.get<PlayerComponent>()

        for (entity in entities) {
            if (entity.tag != "enemy") continue
            val health = entity.get<HealthComponent>() ?: continue
            if (!health.isDead || !entity.active) continue

            val transform = entity.get<TransformComponent>() ?: continue
            val enemyComp = entity.get<EnemyComponent>() ?: continue

            // TD mode: directly award gold + score + XP to TowerDefenseMode
            val td = towerDefense
            if (td != null) {
                td.totalKills++
                td.stageKills++
                td.combo++
                td.comboTimer = com.hordesurvival.game.mode.TowerDefenseMode.COMBO_TIMEOUT
                if (td.combo > td.maxCombo) td.maxCombo = td.combo
                val goldReward = (enemyComp.goldValue * td.comboMultiplier).toInt().coerceAtLeast(1)
                val scoreReward = (enemyComp.xpValue * 10 * td.comboMultiplier).toInt()
                td.gold += goldReward
                td.score += scoreReward
                // Award XP directly (gems get cleaned up in TD mode)
                playerComp?.addXp(enemyComp.xpValue)
            } else {
                // Survival mode: drop XP gems + add gold to player
                spawnXpGems(transform.x, transform.y, enemyComp.xpValue)
                playerComp?.let { it.gold += enemyComp.goldValue * (1f + it.goldGainBonus) }
            }
            playerComp?.let { it.totalKills++ }

            // Chance to drop health gem (2% base, higher for bosses)
            val healthDropChance = if (enemyComp.isBoss) 0.5f else 0.02f + (playerComp?.luck ?: 0f) * 0.01f
            if (Math.random() < healthDropChance) {
                spawnHealthGem(transform.x, transform.y, if (enemyComp.isBoss) 30f else 10f)
            }

            // Split on death
            if (enemyComp.splitOnDeath && enemyComp.splitCount > 0) {
                spawnSplits(transform.x, transform.y, enemyComp.splitCount, enemyComp)
            }

            // Boss drops special loot chest
            if (enemyComp.isBoss) {
                spawnBossLootChest(transform.x, transform.y)
            }

            // Death particles — scale with enemy size
            val spriteColor = entity.get<SpriteComponent>()?.color ?: 0xFFB0BEC5.toInt()
            val enemySprite = entity.get<SpriteComponent>()
            val enemySize = enemySprite?.width ?: 12f
            spawnDeathEffect(transform.x, transform.y, spriteColor, enemyComp.isBoss, enemySize)
            SoundManager.playDeath()

            // Mark inactive
            entity.active = false
        }
    }

    private fun spawnXpGems(x: Float, y: Float, totalXp: Float) {
        val gemCount = if (totalXp >= 10f) 3 else if (totalXp >= 3f) 2 else 1
        val xpPerGem = totalXp / gemCount

        repeat(gemCount) {
            val offset = GameMath.randomPointInCircle(20f)
            val gem = engine.createEntity("xp_gem")
            gem.add(TransformComponent(x + offset.x, y + offset.y))
            gem.add(XpGemComponent(value = xpPerGem))
            gem.add(SpriteComponent(
                width = 8f + xpPerGem.coerceAtMost(5f) * 2f,
                height = 8f + xpPerGem.coerceAtMost(5f) * 2f,
                color = getGemColor(xpPerGem),
                shape = SpriteShape.DIAMOND
            ))
        }
    }

    private fun getGemColor(xp: Float): Int {
        return when {
            xp >= 10f -> 0xFF66BB6A.toInt()  // bright green for high XP
            xp >= 3f -> 0xFF81C784.toInt()   // medium green
            else -> 0xFFAAE6BA.toInt()        // mint green (theme color)
        }
    }

    private fun spawnSplits(x: Float, y: Float, count: Int, parent: EnemyComponent) {
        repeat(count) {
            val offset = GameMath.randomPointOnCircle(30f)
            val entity = engine.createEntity("enemy")
            entity.add(TransformComponent(x + offset.x, y + offset.y))
            entity.add(VelocityComponent(speed = 80f))
            entity.add(HealthComponent(
                currentHp = parent.type.baseHp * 0.4f,
                maxHp = parent.type.baseHp * 0.4f
            ))
            entity.add(EnemyComponent(
                type = EnemyType.SWARM_BAT,
                damage = parent.damage * 0.5f,
                xpValue = parent.xpValue * 0.3f,
                goldValue = parent.goldValue * 0.3f,
                contactCooldown = 0.5f
            ))
            entity.add(SpriteComponent(
                width = 12f, height = 12f,
                color = 0xFFCE93D8.toInt(),  // purple circle like swarm
                shape = SpriteShape.CIRCLE
            ))
            entity.add(CollisionComponent(radius = 8f))  // FIXED: was missing
        }
    }

    private fun spawnBossLootChest(x: Float, y: Float) {
        // Boss drops fewer loot boxes
        val types = listOf(LootType.HEALTH, LootType.GOLD)
        for ((i, type) in types.withIndex()) {
            val angle = i * Math.PI.toFloat()
            val dist = 40f
            val lx = x + kotlin.math.cos(angle) * dist
            val ly = y + kotlin.math.sin(angle) * dist
            val entity = engine.createEntity("loot_box")
            entity.add(TransformComponent(lx, ly))
            entity.add(LootBoxComponent(
                lootType = type,
                value = when (type) {
                    LootType.HEALTH -> 30f
                    LootType.GOLD -> 10f
                    LootType.DAMAGE_BOOST -> 0f
                    LootType.MAGNET -> 0f
                },
                bobPhase = i * 1.5f
            ))
            entity.add(SpriteComponent(
                width = 22f, height = 22f,
                color = when (type) {
                    LootType.HEALTH -> 0xFFEF5350.toInt()  // red heart
                    LootType.GOLD -> 0xFFFFD700.toInt()
                    LootType.DAMAGE_BOOST -> 0xFFFF7043.toInt()
                    LootType.MAGNET -> 0xFF42A5F5.toInt()
                },
                shape = SpriteShape.RECT, layer = 2
            ))
            entity.add(CollisionComponent(radius = 15f))
        }
    }

    private fun spawnHealthGem(x: Float, y: Float, healAmount: Float) {
        val offset = GameMath.randomPointInCircle(15f)
        val gem = engine.createEntity("health_gem")
        gem.add(TransformComponent(x + offset.x, y + offset.y))
        gem.add(XpGemComponent(value = healAmount, lifetime = 20f))
        gem.add(SpriteComponent(
            width = 12f, height = 12f,
            color = 0xFFEF5350.toInt(),  // red for heart
            shape = SpriteShape.CIRCLE    // shape overridden by drawHealthGem
        ))
        gem.add(CollisionComponent(radius = 10f))
    }

    private fun spawnDeathEffect(x: Float, y: Float, color: Int, isBoss: Boolean = false, enemySize: Float = 12f) {
        // Scale particle count and spread with enemy size
        val particleCount = if (isBoss) 20 else if (enemySize > 20f) 10 else 5
        val spreadRadius = if (isBoss) 30f else enemySize * 1.5f
        repeat(particleCount) {
            val offset = GameMath.randomPointInCircle(spreadRadius)
            val p = engine.createEntity("particle")
            p.add(TransformComponent(x + offset.x, y + offset.y))
            val speed = if (isBoss) 6f else 4f
            p.add(VelocityComponent(
                vx = offset.x * speed,
                vy = offset.y * speed,
                speed = 1f
            ))
            val size = if (isBoss) 8f + Math.random().toFloat() * 8f else 5f + Math.random().toFloat() * 5f
            p.add(SpriteComponent(
                width = size,
                height = size,
                color = color,
                alpha = 0.9f
            ))
            p.add(ParticleComponent(lifetime = if (isBoss) 1f else 0.5f, fadeOut = true, shrink = true))
        }
        // Boss: extra ring explosion
        if (isBoss) {
            val ring = engine.createEntity("particle")
            ring.add(TransformComponent(x, y))
            ring.add(SpriteComponent(
                width = enemySize * 3f, height = enemySize * 3f,
                color = 0xFFFFAB91.toInt(), alpha = 0.5f,
                shape = SpriteShape.CIRCLE
            ))
            ring.add(ParticleComponent(lifetime = 0.6f, fadeOut = true))
        }
    }
}
