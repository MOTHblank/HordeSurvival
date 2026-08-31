package com.hordesurvival.game.hazard

import com.hordesurvival.game.component.*
import com.hordesurvival.game.engine.GameEngine
import com.hordesurvival.game.engine.ecs.Entity
import com.hordesurvival.utils.GameMath
import kotlin.math.cos
import kotlin.math.sin

/**
 * Stage Hazards — environmental dangers that spawn on the map.
 * Different hazard types with unique effects.
 */
class StageHazardSystem(private val engine: GameEngine) {

    enum class HazardType(
        val displayName: String,
        val color: Long,
        val shape: SpriteShape,
        val size: Float,
        val damage: Float,
        val interval: Float,
        val description: String
    ) {
        FIRE_GEYSER("Fire Geyser", 0xFFFF5722, SpriteShape.CIRCLE, 40f, 15f, 2f,
            "Periodic fire burst"),
        ICE_PATCH("Ice Patch", 0xFF80CBC4, SpriteShape.CIRCLE, 60f, 0f, 0f,
            "Slows enemies in area"),
        SPIKE_TRAP("Spike Trap", 0xFF90A4AE, SpriteShape.TRIANGLE, 30f, 20f, 1.5f,
            "Damages on contact"),
        POISON_SWAMP("Poison Swamp", 0xFF66BB6A, SpriteShape.CIRCLE, 80f, 5f, 1f,
            "Poison damage over time"),
        LIGHTNING_ROD("Lightning Rod", 0xFFFFEB3B, SpriteShape.STAR, 35f, 30f, 3f,
            "Strikes nearby enemies"),
        HEALING_SPRING("Healing Spring", 0xFF4CAF50, SpriteShape.CIRCLE, 50f, -10f, 2f,
            "Heals player while standing in it"),
        TELEPORT_PAD("Teleport Pad", 0xFF9C27B0, SpriteShape.DIAMOND, 40f, 0f, 0f,
            "Teleports player to random location"),
        FIRE_WALL("Fire Wall", 0xFFFF7043, SpriteShape.RECT, 120f, 10f, 0.5f,
            "Passes through, burns enemies")
    }

    data class HazardInstance(
        val type: HazardType,
        var x: Float,
        var y: Float,
        var timer: Float = 0f,
        var active: Boolean = true,
        var phase: Float = 0f,
        val entityId: Int = -1
    )

    private val hazards = mutableListOf<HazardInstance>()
    private var spawnTimer = 0f
    private var spawnInterval = 25f
    private val maxHazards = 6
    private var hazardLevel = 0  // increases difficulty

    fun update(dt: Float, playerPos: TransformComponent, playerHealth: HealthComponent, playerComp: PlayerComponent) {
        // Spawn new hazards
        spawnTimer += dt
        if (spawnTimer >= spawnInterval && hazards.size < maxHazards) {
            spawnTimer = 0f
            spawnHazard(playerPos)
        }

        // Update existing hazards
        for (hazard in hazards) {
            if (!hazard.active) continue
            hazard.timer += dt
            hazard.phase += dt

            val dx = playerPos.x - hazard.x
            val dy = playerPos.y - hazard.y
            val distSq = dx * dx + dy * dy
            val inRange = distSq < hazard.type.size * hazard.type.size

            when (hazard.type) {
                HazardType.FIRE_GEYSER -> {
                    if (hazard.timer >= hazard.type.interval) {
                        hazard.timer = 0f
                        if (inRange) {
                            playerHealth.takeDamage(hazard.type.damage)
                            engine.shake(intensity = 5f, duration = 0.1f)
                        }
                        spawnBurstEffect(hazard.x, hazard.y, hazard.type.color.toInt(), 8)
                    }
                }

                HazardType.ICE_PATCH -> {
                    if (inRange) {
                        // Slow player
                        playerComp.moveSpeed = (playerComp.moveSpeed * 0.95f).coerceAtLeast(0.5f)
                    }
                    // Also slow enemies in range
                    val nearby = engine.findInRange(hazard.x, hazard.y, hazard.type.size, "enemy")
                    for (e in nearby) {
                        e.get<EnemyComponent>()?.let {
                            it.slowTimer = 0.5f
                            it.slowFactor = 0.3f
                        }
                    }
                }

                HazardType.SPIKE_TRAP -> {
                    if (inRange && hazard.timer >= hazard.type.interval) {
                        hazard.timer = 0f
                        playerHealth.takeDamage(hazard.type.damage)
                        engine.shake(intensity = 3f, duration = 0.08f)
                    }
                }

                HazardType.POISON_SWAMP -> {
                    if (inRange && hazard.timer >= hazard.type.interval) {
                        hazard.timer = 0f
                        playerHealth.takeDamage(hazard.type.damage)
                    }
                    // Damage enemies too
                    if (hazard.timer >= hazard.type.interval) {
                        val nearby = engine.findInRange(hazard.x, hazard.y, hazard.type.size, "enemy")
                        for (e in nearby) {
                            e.get<HealthComponent>()?.takeDamage(hazard.type.damage * 0.5f)
                        }
                    }
                }

                HazardType.LIGHTNING_ROD -> {
                    if (hazard.timer >= hazard.type.interval) {
                        hazard.timer = 0f
                        // Strike nearest enemy
                        val nearest = engine.findNearest(hazard.x, hazard.y, "enemy", 200f)
                        if (nearest != null) {
                            nearest.get<HealthComponent>()?.takeDamage(hazard.type.damage)
                            val ePos = nearest.get<TransformComponent>()
                            if (ePos != null) spawnLightningEffect(hazard.x, hazard.y, ePos.x, ePos.y)
                        }
                    }
                }

                HazardType.HEALING_SPRING -> {
                    if (inRange && hazard.timer >= hazard.type.interval) {
                        hazard.timer = 0f
                        playerHealth.heal(-hazard.type.damage)  // negative damage = heal
                    }
                }

                HazardType.TELEPORT_PAD -> {
                    if (inRange && hazard.timer >= 2f) {
                        hazard.timer = 0f
                        val angle = GameMath.randomRange(0f, Math.PI.toFloat() * 2f)
                        val dist = GameMath.randomRange(100f, 300f)
                        playerPos.x += cos(angle) * dist
                        playerPos.y += sin(angle) * dist
                        spawnTeleportEffect(hazard.x, hazard.y)
                    }
                }

                HazardType.FIRE_WALL -> {
                    // Burns enemies that pass through
                    val nearby = engine.findInRange(hazard.x, hazard.y, hazard.type.size / 2f, "enemy")
                    for (e in nearby) {
                        e.get<HealthComponent>()?.takeDamage(hazard.type.damage * dt)
                    }
                }
            }

            // Update visual
            val entity = engine.getActiveEntities().find { it.id == hazard.entityId }
            entity?.get<SpriteComponent>()?.let {
                it.alpha = 0.4f + 0.2f * kotlin.math.sin(hazard.phase * 3f)
            }
        }

        // Cleanup
        hazards.removeAll { !it.active }
    }

    private fun spawnHazard(playerPos: TransformComponent) {
        val type = HazardType.entries.toTypedArray().random()
        val angle = GameMath.randomRange(0f, Math.PI.toFloat() * 2f)
        val dist = GameMath.randomRange(150f, 400f)
        val x = playerPos.x + cos(angle) * dist
        val y = playerPos.y + sin(angle) * dist

        val entity = engine.createEntity("hazard")
        entity.add(TransformComponent(x, y))
        entity.add(SpriteComponent(
            width = type.size, height = type.size,
            color = type.color.toInt(),
            alpha = 0.5f,
            shape = type.shape
        ))

        hazards.add(HazardInstance(
            type = type, x = x, y = y,
            entityId = entity.id
        ))
    }

    fun setDifficulty(level: Int) {
        hazardLevel = level
        spawnInterval = (25f - level * 1.5f).coerceAtLeast(10f)
    }

    // ── Visual Effects ────────────────────────────────────────────
    private fun spawnBurstEffect(x: Float, y: Float, color: Int, count: Int) {
        repeat(count) {
            val angle = it * Math.PI.toFloat() * 2f / count
            val p = engine.createEntity("particle")
            p.add(TransformComponent(x, y))
            p.add(VelocityComponent(vx = cos(angle) * 100f, vy = sin(angle) * 100f, speed = 1f))
            p.add(SpriteComponent(width = 6f, height = 6f, color = color, alpha = 0.8f))
            p.add(ParticleComponent(lifetime = 0.4f, fadeOut = true, shrink = true))
        }
    }

    private fun spawnLightningEffect(x1: Float, y1: Float, x2: Float, y2: Float) {
        val p = engine.createEntity("particle")
        p.add(TransformComponent((x1 + x2) / 2f, (y1 + y2) / 2f))
        val dx = x2 - x1; val dy = y2 - y1
        val lenSq = dx * dx + dy * dy
        val len = kotlin.math.sqrt(lenSq)
        p.add(SpriteComponent(width = 4f, height = len, color = 0xFFFFEB3B.toInt(), alpha = 0.8f))
        p.add(ParticleComponent(lifetime = 0.15f, fadeOut = true))
    }

    private fun spawnTeleportEffect(x: Float, y: Float) {
        repeat(6) {
            val angle = it * Math.PI.toFloat() * 2f / 6f
            val p = engine.createEntity("particle")
            p.add(TransformComponent(x, y))
            p.add(VelocityComponent(vx = cos(angle) * 80f, vy = sin(angle) * 80f, speed = 1f))
            p.add(SpriteComponent(width = 5f, height = 5f, color = 0xFF9C27B0.toInt(), alpha = 0.7f))
            p.add(ParticleComponent(lifetime = 0.3f, fadeOut = true))
        }
    }

    fun reset() {
        hazards.clear()
        spawnTimer = 0f
        hazardLevel = 0
    }
}
