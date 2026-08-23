package com.hordesurvival.game.hazard

import com.badlogic.gdx.math.Vector2
import com.hordesurvival.game.component.*
import com.hordesurvival.game.engine.GameEngine
import com.hordesurvival.utils.GameMath
import kotlin.math.cos
import kotlin.math.sin

/**
 * Map Hazards & Environment System — obstacles that damage or affect gameplay.
 * Hazards spawn periodically and add environmental challenge.
 */
class MapHazard(private val engine: GameEngine) {

    private val tempVec2 = Vector2()

    enum class HazardType {
        SPIKE_TRAP,      // Stationary, damages on contact
        FIRE_GEYSER,     // Periodic burst of fire damage
        SLOW_ZONE,       // Slows player and enemies
        HEALING_POOL,    // Heals player while standing in it
        TELEPORT_PAD     // Teleports player to random location
    }

    data class HazardState(
        val type: HazardType,
        var x: Float,
        var y: Float,
        var timer: Float = 0f,
        var active: Boolean = true,
        var phase: Float = 0f
    )

    private val hazards = mutableListOf<HazardState>()
    private var spawnTimer = 0f
    private val spawnInterval = 30f  // seconds between hazard spawns
    private val maxHazards = 8
    private var playerInSlowZone = false
    private var baseMoveSpeed = 1f  // tracks player speed before slow

    fun update(dt: Float, playerPos: TransformComponent, playerHealth: HealthComponent, playerComp: PlayerComponent) {
        spawnTimer += dt
        if (spawnTimer >= spawnInterval && hazards.size < maxHazards) {
            spawnTimer = 0f
            spawnHazard(playerPos)
        }

        // Track if player is in any slow zone this frame
        var inSlowZoneThisFrame = false

        for (hazard in hazards) {
            if (!hazard.active) continue
            hazard.timer += dt
            hazard.phase += dt

            val dx = playerPos.x - hazard.x
            val dy = playerPos.y - hazard.y
            val distSq = dx * dx + dy * dy
            val inRange = distSq < 50f * 50f

            when (hazard.type) {
                HazardType.SPIKE_TRAP -> {
                    if (inRange && hazard.timer >= 1f) {
                        hazard.timer = 0f
                        playerHealth.takeDamage(10f)
                        engine.shake(intensity = 4f, duration = 0.1f)
                    }
                }
                HazardType.FIRE_GEYSER -> {
                    if (hazard.timer >= 3f) {
                        hazard.timer = 0f
                        if (inRange) {
                            playerHealth.takeDamage(20f)
                            engine.shake(intensity = 6f, duration = 0.15f)
                        }
                        // Visual burst effect
                        spawnHazardParticle(hazard.x, hazard.y, 0xFFFF7043.toInt(), 8)
                    }
                }
                HazardType.SLOW_ZONE -> {
                    if (inRange) {
                        inSlowZoneThisFrame = true
                        if (!playerInSlowZone) {
                            // Entering slow zone — save base speed
                            baseMoveSpeed = playerComp.moveSpeed
                            playerInSlowZone = true
                        }
                        playerComp.moveSpeed = (baseMoveSpeed * 0.7f).coerceAtLeast(0.3f)
                    }
                }
                HazardType.HEALING_POOL -> {
                    if (inRange && hazard.timer >= 1f) {
                        hazard.timer = 0f
                        playerHealth.heal(5f)
                    }
                }
                HazardType.TELEPORT_PAD -> {
                    if (inRange && hazard.timer >= 2f) {
                        hazard.timer = 0f
                        val angle = GameMath.randomRange(0f, Math.PI.toFloat() * 2f)
                        val dist = GameMath.randomRange(100f, 300f)
                        playerPos.x += cos(angle) * dist
                        playerPos.y += sin(angle) * dist
                        spawnHazardParticle(hazard.x, hazard.y, 0xFFCE93D8.toInt(), 12)
                    }
                }
            }
        }

        // Restore speed when player leaves all slow zones
        if (playerInSlowZone && !inSlowZoneThisFrame) {
            playerComp.moveSpeed = baseMoveSpeed
            playerInSlowZone = false
        }

        // Cleanup expired hazards
        hazards.removeAll { !it.active }
    }

    private fun spawnHazard(playerPos: TransformComponent) {
        val angle = GameMath.randomRange(0f, Math.PI.toFloat() * 2f)
        val dist = GameMath.randomRange(150f, 400f)
        val x = playerPos.x + cos(angle) * dist
        val y = playerPos.y + sin(angle) * dist

        val type = HazardType.entries.toTypedArray().random()
        hazards.add(HazardState(type = type, x = x, y = y))

        // Spawn visual entity
        val entity = engine.createEntity("hazard")
        entity.add(TransformComponent(x, y))
        val color = when (type) {
            HazardType.SPIKE_TRAP -> 0xFF90A4AE.toInt()
            HazardType.FIRE_GEYSER -> 0xFFFF7043.toInt()
            HazardType.SLOW_ZONE -> 0xFF81D4FA.toInt()
            HazardType.HEALING_POOL -> 0xFF66BB6A.toInt()
            HazardType.TELEPORT_PAD -> 0xFFCE93D8.toInt()
        }
        entity.add(SpriteComponent(width = 30f, height = 30f, color = color, alpha = 0.6f, shape = SpriteShape.CIRCLE))
    }

    private fun spawnHazardParticle(x: Float, y: Float, color: Int, count: Int) {
        repeat(count) {
            val offset = GameMath.randomPointInCircle(20f, tempVec2)
            val p = engine.createEntity("particle")
            p.add(TransformComponent(x + offset.x, y + offset.y))
            p.add(VelocityComponent(vx = offset.x * 3f, vy = offset.y * 3f, speed = 1f))
            p.add(SpriteComponent(width = 4f, height = 4f, color = color, alpha = 0.8f))
            p.add(ParticleComponent(lifetime = 0.5f, fadeOut = true, shrink = true))
        }
    }

    fun reset() {
        hazards.clear()
        spawnTimer = 0f
    }
}
