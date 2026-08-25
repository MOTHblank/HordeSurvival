package com.hordesurvival.game.engine.ecs.systems

import com.hordesurvival.game.audio.SoundManager
import com.hordesurvival.game.component.*
import com.hordesurvival.game.engine.GameEngine
import com.hordesurvival.game.engine.ecs.Entity
import com.hordesurvival.game.engine.ecs.System
import com.hordesurvival.utils.GameMath
import kotlin.math.sin

/**
 * Spawns loot boxes (health, gold, magnet, damage boost) periodically.
 * Boxes appear near the player and can be picked up by collision.
 * Spawn rate increases with player level and luck.
 */
class LootBoxSystem(private val engine: GameEngine) : System() {

    private var spawnTimer = 0f
    private var baseInterval = 15f  // seconds between spawns

    override fun update(dt: Float, entities: List<Entity>) {
        val player = engine.playerEntity?.takeIf { it.active && it.has<PlayerComponent>() }
        val playerPos = player?.get<TransformComponent>() ?: return
        // FIX: player is non-null here (after ?: return), remove unnecessary safe calls
        val playerComp = player.get<PlayerComponent>()

        // Spawn timer
        spawnTimer += dt
        val luckBonus = (playerComp?.luck ?: 0f) * 5f
        val levelBonus = (playerComp?.level ?: 1) * 0.02f
        val interval = (baseInterval / (1f + luckBonus + levelBonus)).coerceAtLeast(5f)

        if (spawnTimer >= interval) {
            spawnTimer = 0f
            spawnLootBox(playerPos, playerComp)
        }

        // Update damage boost timers
        for (e in entities) {
            if (e.tag != "damage_boost_timer" || !e.active) continue
            val boost = e.get<DamageBoostComponent>() ?: continue
            boost.timer += dt
            if (boost.timer >= boost.duration) {
                // Remove the boost — restore to base might (not subtract)
                playerComp?.let { it.might = boost.baseMight }
                e.active = false
            }
        }

        // Update existing loot boxes
        for (e in entities) {
            if (e.tag != "loot_box" || !e.active) continue
            val loot = e.get<LootBoxComponent>() ?: continue
            loot.timer += dt
            loot.bobPhase += dt * 3f

            // Despawn expired boxes
            if (loot.timer >= loot.lifetime) {
                e.active = false
                continue
            }

            // Check collision with player
            val boxPos = e.get<TransformComponent>() ?: continue
            val playerCollision = player.get<CollisionComponent>()
            val boxCollision = e.get<CollisionComponent>()
            if (playerCollision != null && boxCollision != null) {
                val dx = boxPos.x - playerPos.x
                val dy = boxPos.y - playerPos.y
                val dist = dx * dx + dy * dy
                val pickupRange = playerCollision.radius + boxCollision.radius + (playerComp?.pickupRange ?: 50f) * 0.5f
                if (dist <= pickupRange * pickupRange) {
                    applyLoot(player, playerComp, loot)
                    e.active = false
                    SoundManager.playPickupBig()
                }
            }
        }
    }

    private fun spawnLootBox(playerPos: TransformComponent, playerComp: PlayerComponent?) {
        // Decide loot type with weighted random
        val roll = Math.random().toFloat()
        val lootType = when {
            roll < 0.25f -> LootType.HEALTH
            roll < 0.65f -> LootType.GOLD
            roll < 0.85f -> LootType.MAGNET
            else -> LootType.DAMAGE_BOOST
        }

        val value = when (lootType) {
            LootType.HEALTH -> 15f + (playerComp?.level ?: 1) * 1f
            LootType.GOLD -> 3f + (playerComp?.level ?: 1) * 0.5f
            LootType.MAGNET -> 0f  // effect-based
            LootType.DAMAGE_BOOST -> 0f  // effect-based
        }

        // Spawn near player but not too close
        val angle = GameMath.randomRange(0f, Math.PI.toFloat() * 2f)
        val dist = GameMath.randomRange(100f, 250f)
        val x = playerPos.x + kotlin.math.cos(angle) * dist
        val y = playerPos.y + kotlin.math.sin(angle) * dist

        val color = when (lootType) {
            LootType.HEALTH -> 0xFFEF5350.toInt()       // red (heart)
            LootType.GOLD -> 0xFFFFD700.toInt()         // gold
            LootType.MAGNET -> 0xFF42A5F5.toInt()       // blue
            LootType.DAMAGE_BOOST -> 0xFFFF7043.toInt()  // orange
        }

        val entity = engine.createEntity("loot_box")
        entity.add(TransformComponent(x, y))
        entity.add(LootBoxComponent(lootType = lootType, value = value, bobPhase = Math.random().toFloat() * 6f))
        entity.add(SpriteComponent(
            width = 20f, height = 20f,
            color = color,
            shape = SpriteShape.RECT,
            layer = 2
        ))
        entity.add(CollisionComponent(radius = 14f))
    }

    private fun applyLoot(player: Entity, playerComp: PlayerComponent?, loot: LootBoxComponent) {
        when (loot.lootType) {
            LootType.HEALTH -> {
                player.get<HealthComponent>()?.heal(loot.value)
            }
            LootType.GOLD -> {
                playerComp?.let { it.gold += loot.value * (1f + it.goldGainBonus) }
            }
            LootType.MAGNET -> {
                // Pull all nearby XP gems to player
                val playerPos = player.get<TransformComponent>() ?: return
                for (e in engine.getActiveEntities()) {
                    if (e.tag != "xp_gem" || !e.active) continue
                    val gem = e.get<XpGemComponent>() ?: continue
                    gem.magnetized = true
                }
            }
            LootType.DAMAGE_BOOST -> {
                // Temporary damage boost — track base might for proper restore
                val boostAmount = 0.5f  // +50% base might
                val currentMight = playerComp?.might ?: 1f
                val boostEntity = engine.createEntity("damage_boost_timer")
                boostEntity.add(DamageBoostComponent(duration = 10f, mightMultiplier = boostAmount, baseMight = currentMight))
                playerComp?.let { it.might += boostAmount }
            }
        }
    }

    override fun dispose() {
        spawnTimer = 0f
    }
}
