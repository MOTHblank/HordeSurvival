package com.hordesurvival.game.engine.ecs.systems

import com.hordesurvival.game.component.*
import com.hordesurvival.game.engine.GameEngine
import com.hordesurvival.game.engine.ecs.Entity
import com.hordesurvival.game.engine.ecs.System
import com.hordesurvival.utils.GameMath
import kotlin.math.sqrt

/**
 * Handles elite enemy abilities: Teleport, Shielded, Explode-on-death.
 */
class EliteAbilitySystem(private val engine: GameEngine) : System() {

    override fun update(dt: Float, entities: List<Entity>) {
        val player = engine.playerEntity?.takeIf { it.active && it.has<PlayerComponent>() } ?: return
        val playerPos = player.get<TransformComponent>() ?: return

        for (entity in entities) {
            if (entity.tag != "enemy" || !entity.active) continue
            val elite = entity.get<EliteComponent>() ?: continue
            val transform = entity.get<TransformComponent>() ?: continue
            val health = entity.get<HealthComponent>() ?: continue

            when (elite.ability) {
                EliteAbility.TELEPORT -> {
                    elite.teleportTimer += dt
                    if (elite.teleportTimer >= elite.teleportCooldown) {
                        elite.teleportTimer = 0f
                        // Teleport near player
                        val angle = Math.random().toFloat() * Math.PI.toFloat() * 2f
                        val dist = 80f + Math.random().toFloat() * 60f
                        transform.x = playerPos.x + kotlin.math.cos(angle) * dist
                        transform.y = playerPos.y + kotlin.math.sin(angle) * dist
                        // Spawn effect
                        spawnTeleportEffect(transform.x, transform.y)
                    }
                }
                EliteAbility.SHIELDED -> {
                    if (!elite.shieldActive && health.currentHp < health.maxHp * 0.5f) {
                        // Activate shield at 50% HP
                        elite.shieldActive = true
                        elite.shieldHp = health.maxHp * 0.3f
                        elite.shieldMaxHp = elite.shieldHp
                    }
                    // Shield absorbs damage
                    if (elite.shieldActive && elite.shieldHp > 0f) {
                        entity.get<SpriteComponent>()?.let {
                            it.alpha = 0.7f + 0.3f * kotlin.math.sin(engine.gameTime * 8f).toFloat()
                        }
                    } else if (elite.shieldActive) {
                        elite.shieldActive = false
                        entity.get<SpriteComponent>()?.alpha = 1f
                    }
                }
                EliteAbility.EXPLODE_ON_DEATH -> {
                    if (health.isDead && entity.active && !elite.hasExploded) {
                        elite.hasExploded = true
                        val radius = 100f
                        val damage = 30f
                        val dx = playerPos.x - transform.x
                        val dy = playerPos.y - transform.y
                        if (dx * dx + dy * dy < radius * radius) {
                            player.get<HealthComponent>()?.takeDamage(damage)
                        }
                        spawnExplosionEffect(transform.x, transform.y, radius)
                        entity.active = false  // Prevent re-triggering
                    }
                }
                EliteAbility.NONE -> {}
            }
        }
    }

    private fun spawnTeleportEffect(x: Float, y: Float) {
        repeat(8) {
            val angle = it * Math.PI.toFloat() * 2f / 8f
            val p = engine.createEntity("particle")
            p.add(TransformComponent(x, y))
            p.add(VelocityComponent(vx = kotlin.math.cos(angle) * 150f, vy = kotlin.math.sin(angle) * 150f, speed = 1f))
            p.add(SpriteComponent(width = 8f, height = 8f, color = 0xFFCE93D8.toInt(), alpha = 0.8f))
            p.add(ParticleComponent(lifetime = 0.4f, fadeOut = true, shrink = true))
        }
    }

    private fun spawnExplosionEffect(x: Float, y: Float, radius: Float) {
        // Ring
        val ring = engine.createEntity("particle")
        ring.add(TransformComponent(x, y))
        ring.add(SpriteComponent(width = radius * 2, height = radius * 2, color = 0xFFFF6E40.toInt(), alpha = 0.6f, shape = SpriteShape.CIRCLE))
        ring.add(ParticleComponent(lifetime = 0.5f, fadeOut = true))
        // Sparks
        repeat(12) {
            val angle = it * Math.PI.toFloat() * 2f / 12f
            val spark = engine.createEntity("particle")
            spark.add(TransformComponent(x, y))
            spark.add(VelocityComponent(vx = kotlin.math.cos(angle) * 200f, vy = kotlin.math.sin(angle) * 200f, speed = 1f))
            spark.add(SpriteComponent(width = 6f, height = 6f, color = 0xFFFFCC80.toInt(), alpha = 0.9f))
            spark.add(ParticleComponent(lifetime = 0.6f, fadeOut = true, shrink = true))
        }
    }

    override fun dispose() {}
}
