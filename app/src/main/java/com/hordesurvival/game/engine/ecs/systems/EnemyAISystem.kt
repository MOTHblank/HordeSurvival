package com.hordesurvival.game.engine.ecs.systems

import com.hordesurvival.game.component.*
import com.hordesurvival.game.engine.GameEngine
import com.hordesurvival.game.engine.ecs.Entity
import com.hordesurvival.game.engine.ecs.System
import com.hordesurvival.game.enemy.EnemyType
import kotlin.math.atan2
import kotlin.math.sqrt
import kotlin.math.cos
import kotlin.math.sin
import com.hordesurvival.game.component.SpriteShape

/**
 * Controls enemy behavior: chase player, ranged attacks, healing, phasing.
 * Different enemy types have different AI patterns.
 * Overhauled: uses GameEngine's SpatialGrid for range queries in healer logic.
 */
class EnemyAISystem(private val engine: GameEngine) : System() {

    private val _healCandidatesBuffer = mutableListOf<Entity>()

    override fun update(dt: Float, entities: List<Entity>) {
        val player = engine.playerEntity ?: return
        val playerTransform = player.get<TransformComponent>() ?: return

        for (i in 0 until entities.size) {
            val entity = entities[i]
            if (entity.tag != "enemy" || !entity.active) continue
            val enemy = entity.get<EnemyComponent>() ?: continue
            val transform = entity.get<TransformComponent>() ?: continue
            val velocity = entity.get<VelocityComponent>() ?: continue

            // Update burn effect
            if (enemy.burnTimer > 0f) {
                enemy.burnTimer -= dt
                entity.get<HealthComponent>()?.takeDamage(enemy.burnDamage * dt)
            }

            // Contact cooldown
            if (enemy.contactTimer > 0f) enemy.contactTimer -= dt

            // Healer logic
            if (enemy.type == EnemyType.HEALER) {
                enemy.healTimer -= dt
                if (enemy.healTimer <= 0f) {
                    enemy.healTimer = enemy.healCooldown
                    healNearbyEnemies(transform.x, transform.y)
                }
            }

            // Ghost phasing
            if (enemy.type == EnemyType.GHOST) {
                enemy.phaseTimer -= dt
                // Ghost phases through player periodically
            }

            // Movement AI based on type
            when (enemy.type) {
                EnemyType.SHOOTER_TURRET -> {
                    velocity.vx = 0f
                    velocity.vy = 0f
                    enemy.shootTimer -= dt
                    if (enemy.shootTimer <= 0f) {
                        enemy.shootTimer = 2f
                        val dx = playerTransform.x - transform.x
                        val dy = playerTransform.y - transform.y
                        val dist = sqrt(dx * dx + dy * dy)
                        if (dist > 0f && dist < 500f) {
                            spawnEnemyProjectile(transform.x, transform.y, dx / dist, dy / dist, enemy.damage * 0.5f)
                        }
                    }
                }
                EnemyType.MAGE -> {
                    val dx = playerTransform.x - transform.x
                    val dy = playerTransform.y - transform.y
                    val dist = sqrt(dx * dx + dy * dy)
                    if (dist > 0f) {
                        if (dist < 150f) {
                            velocity.vx = -(dx / dist)
                            velocity.vy = -(dy / dist)
                        } else if (dist > 300f) {
                            velocity.vx = (dx / dist)
                            velocity.vy = (dy / dist)
                        } else {
                            velocity.vx = -(dy / dist) * 0.5f
                            velocity.vy = (dx / dist) * 0.5f
                        }
                    }
                    // Mage uses shootTimer ONLY — never touch contactTimer
                    enemy.shootTimer -= dt
                    if (enemy.shootTimer <= 0f) {
                        enemy.shootTimer = 2.5f
                        val ndx = playerTransform.x - transform.x
                        val ndy = playerTransform.y - transform.y
                        val ndist = sqrt(ndx * ndx + ndy * ndy)
                        if (ndist > 0f && ndist < 400f) {
                            spawnEnemyProjectile(transform.x, transform.y, ndx / ndist, ndy / ndist, enemy.damage * 0.6f)
                        }
                    }
                    // Keep contactTimer at 0 so contact damage works independently
                    enemy.contactTimer = 0f
                }
                EnemyType.GHOST -> {
                    // Moves toward player with sine wave offset
                    val dx = playerTransform.x - transform.x
                    val dy = playerTransform.y - transform.y
                    val dist = sqrt(dx * dx + dy * dy)
                    if (dist > 0f) {
                        val sinOffset = kotlin.math.sin(engine.gameTime * 3f + entity.id) * 0.3f
                        velocity.vx = (dx / dist) + sinOffset
                        velocity.vy = (dy / dist)
                        // Normalize
                        val len = sqrt(velocity.vx * velocity.vx + velocity.vy * velocity.vy)
                        if (len > 0f) { velocity.vx /= len; velocity.vy /= len }
                    }
                }
                EnemyType.SWARM_BAT -> {
                    // Fast, erratic movement toward player
                    val dx = playerTransform.x - transform.x
                    val dy = playerTransform.y - transform.y
                    val dist = sqrt(dx * dx + dy * dy)
                    if (dist > 0f) {
                        val wobble = kotlin.math.sin(engine.gameTime * 8f + entity.id * 0.5f) * 0.4f
                        velocity.vx = (dx / dist) + wobble
                        velocity.vy = (dy / dist)
                        val len = sqrt(velocity.vx * velocity.vx + velocity.vy * velocity.vy)
                        if (len > 0f) { velocity.vx /= len; velocity.vy /= len }
                    }
                }
                EnemyType.BOSS -> {
                    // Boss phase system based on HP
                    val hp = entity.get<HealthComponent>()
                    val hpRatio = if (hp != null) hp.currentHp / hp.maxHp else 1f
                    val newPhase = when {
                        hpRatio < 0.25f -> 3
                        hpRatio < 0.50f -> 2
                        hpRatio < 0.75f -> 1
                        else -> 0
                    }
                    if (newPhase > enemy.bossPhase) {
                        enemy.bossPhase = newPhase
                        // Phase transition: spawn minions
                        spawnBossMinions(transform, newPhase)
                    }
                    // Speed increases with phase
                    val phaseSpeed = 1f + enemy.bossPhase * 0.3f
                    val dx = playerTransform.x - transform.x
                    val dy = playerTransform.y - transform.y
                    val dist = sqrt(dx * dx + dy * dy)
                    if (dist > 0f) {
                        velocity.vx = (dx / dist) * phaseSpeed
                        velocity.vy = (dy / dist) * phaseSpeed
                    }
                }
                else -> {
                    // Standard chase behavior
                    val dx = playerTransform.x - transform.x
                    val dy = playerTransform.y - transform.y
                    val dist = sqrt(dx * dx + dy * dy)
                    if (dist > 0f) {
                        velocity.vx = dx / dist
                        velocity.vy = dy / dist
                    }
                }
            }
        }
    }

    private fun spawnEnemyProjectile(x: Float, y: Float, vx: Float, vy: Float, damage: Float) {
        val proj = engine.createEntity("enemy_projectile")
        proj.add(TransformComponent(x, y))
        proj.add(VelocityComponent(vx = vx, vy = vy, speed = 200f))
        proj.add(TurretProjectileComponent(damage = damage, speed = 200f, lifetime = 3f))
        proj.add(SpriteComponent(width = 8f, height = 8f, color = 0xFFFF8A65.toInt(), shape = SpriteShape.CIRCLE))
        proj.add(CollisionComponent(radius = 6f, isTrigger = true))
    }

    private fun spawnBossMinions(bossPos: TransformComponent, phase: Int) {
        val count = 3 + phase * 2
        repeat(count) {
            val angle = Math.random().toFloat() * Math.PI.toFloat() * 2f
            val dist = 60f + Math.random().toFloat() * 40f
            val x = bossPos.x + kotlin.math.cos(angle) * dist
            val y = bossPos.y + kotlin.math.sin(angle) * dist
            val minion = engine.createEntity("enemy")
            minion.add(TransformComponent(x, y))
            minion.add(VelocityComponent(speed = 100f + phase * 20f))
            minion.add(HealthComponent(currentHp = 15f + phase * 5f, maxHp = 15f + phase * 5f))
            minion.add(EnemyComponent(
                type = EnemyType.SWARM_BAT,
                damage = 5f + phase * 2f,
                xpValue = 1f, goldValue = 1f,
                contactCooldown = 0.5f
            ))
            minion.add(SpriteComponent(width = 12f, height = 12f, color = 0xFFCE93D8.toInt(), shape = SpriteShape.CIRCLE))
            minion.add(CollisionComponent(radius = 7f))
        }
    }

    private fun healNearbyEnemies(x: Float, y: Float) {
        val healRadius = 100f
        // Heal scales with game time — stronger heals as difficulty increases
        val timeMinutes = engine.gameTime / 60f
        val healPercent = 0.05f + timeMinutes * 0.005f  // 5% base + 0.5% per minute
        _healCandidatesBuffer.clear()
        engine.spatialGrid.queryRange(x, y, healRadius, "enemy", _healCandidatesBuffer)
        for (i in 0 until _healCandidatesBuffer.size) {
            val entity = _healCandidatesBuffer[i]
            if (!entity.active) continue
            val health = entity.get<HealthComponent>() ?: continue
            health.heal(health.maxHp * healPercent)
        }
    }
}
