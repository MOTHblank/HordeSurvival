package com.hordesurvival.game.engine.ecs.systems

import com.hordesurvival.game.component.*
import com.hordesurvival.game.engine.GameEngine
import com.hordesurvival.game.engine.ecs.Entity
import com.hordesurvival.game.engine.ecs.System
import com.hordesurvival.utils.GameMath
import kotlin.math.sin

/**
 * Relic system — spawns passive items on the map periodically.
 * Player walks over them to collect permanent (per-run) bonuses.
 */
class RelicSystem(private val engine: GameEngine) : System() {

    private var spawnTimer = 0f
    private val spawnInterval = 45f  // every 45 seconds
    private val maxRelics = 5  // max on map at once

    override fun update(dt: Float, entities: List<Entity>) {
        val player = engine.playerEntity ?: return
        val playerPos = player.get<TransformComponent>() ?: return
        val playerComp = player.get<PlayerComponent>() ?: return

        // Spawn timer
        spawnTimer += dt
        if (spawnTimer >= spawnInterval) {
            spawnTimer = 0f
            val relicCount = entities.count { it.tag == "relic" && it.active }
            if (relicCount < maxRelics) {
                spawnRelic(playerPos)
            }
        }

        // Update relics
        for (e in entities) {
            if (e.tag != "relic" || !e.active) continue
            val relic = e.get<RelicComponent>() ?: continue
            val transform = e.get<TransformComponent>() ?: continue

            relic.timer += dt
            if (relic.timer >= relic.lifetime) {
                e.active = false
                continue
            }

            // Check pickup (same range as loot boxes)
            val dx = playerPos.x - transform.x
            val dy = playerPos.y - transform.y
            val pickupRange = playerComp.pickupRange * 0.5f + 16f
            if (dx * dx + dy * dy < pickupRange * pickupRange) {
                applyRelic(player, playerComp, relic.relicType)
                e.active = false
                spawnPickupEffect(transform.x, transform.y)
            }
        }
    }

    private fun spawnRelic(playerPos: TransformComponent) {
        val types = RelicType.entries.toTypedArray()
        val type = types.random()
        val angle = Math.random().toFloat() * Math.PI.toFloat() * 2f
        val dist = 150f + Math.random().toFloat() * 200f
        val x = playerPos.x + kotlin.math.cos(angle) * dist
        val y = playerPos.y + kotlin.math.sin(angle) * dist

        val color = when (type) {
            RelicType.CROWN -> 0xFFFFD700.toInt()      // gold
            RelicType.WINGS -> 0xFF81D4FA.toInt()      // light blue
            RelicType.ARMOR -> 0xFF90A4AE.toInt()      // grey
            RelicType.CLOVER -> 0xFF66BB6A.toInt()     // green
            RelicType.RING -> 0xFFEF5350.toInt()       // red
            RelicType.AMULET -> 0xFFCE93D8.toInt()     // purple
        }

        val entity = engine.createEntity("relic")
        entity.add(TransformComponent(x, y))
        entity.add(RelicComponent(relicType = type))
        entity.add(SpriteComponent(width = 18f, height = 18f, color = color, shape = SpriteShape.DIAMOND, layer = 2))
    }

    private fun applyRelic(player: Entity, comp: PlayerComponent, type: RelicType) {
        when (type) {
            RelicType.CROWN -> comp.xpGain *= 1.2f
            RelicType.WINGS -> comp.moveSpeed *= 1.15f
            RelicType.ARMOR -> player.get<HealthComponent>()?.let { it.armor += 5f }
            RelicType.CLOVER -> comp.luck += 0.10f
            RelicType.RING -> comp.might *= 1.10f
            RelicType.AMULET -> comp.regenRate += 0.5f
        }
    }

    private fun spawnPickupEffect(x: Float, y: Float) {
        repeat(8) {
            val angle = it * Math.PI.toFloat() * 2f / 8f
            val p = engine.createEntity("particle")
            p.add(TransformComponent(x, y))
            p.add(VelocityComponent(vx = kotlin.math.cos(angle) * 120f, vy = kotlin.math.sin(angle) * 120f, speed = 1f))
            p.add(SpriteComponent(width = 5f, height = 5f, color = 0xFFFFD700.toInt(), alpha = 0.9f))
            p.add(ParticleComponent(lifetime = 0.4f, fadeOut = true, shrink = true))
        }
    }

    override fun dispose() { spawnTimer = 0f }
}
