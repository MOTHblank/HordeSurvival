package com.hordesurvival.game.engine.ecs.systems

import com.hordesurvival.game.component.*
import com.hordesurvival.game.engine.GameEngine
import com.hordesurvival.game.engine.ecs.Entity
import com.hordesurvival.game.engine.ecs.System
import com.hordesurvival.game.weapon.WeaponType
import com.hordesurvival.utils.GameMath
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Projectile movement, homing, boomerang return, and lifetime.
 * Fixed: boomerang return logic, proper distance tracking.
 */
class ProjectileSystem(private val engine: GameEngine) : System() {

    // Trail spawn timer — spawn trail particles every N seconds per projectile
    private var trailTimer = 0f
    private val TRAIL_INTERVAL = 0.03f  // every 30ms

    override fun update(dt: Float, entities: List<Entity>) {
        val player = entities.find { it.tag == "player" }
        val playerPos = player?.get<TransformComponent>()

        trailTimer += dt
        val shouldSpawnTrail = trailTimer >= TRAIL_INTERVAL
        if (shouldSpawnTrail) trailTimer = 0f

        for (entity in entities) {
            if (entity.tag != "projectile") continue
            val proj = entity.get<ProjectileComponent>() ?: continue
            val transform = entity.get<TransformComponent>() ?: continue
            val velocity = entity.get<VelocityComponent>() ?: continue

            // Lifetime countdown
            proj.timer -= dt
            if (proj.timer <= 0f) {
                entity.active = false
                continue
            }

            // ── Boomerang logic ───────────────────────────────────
            if (proj.returnsToPlayer && proj.maxDistance > 0f) {
                proj.distanceTraveled += proj.speed * dt
                if (proj.distanceTraveled >= proj.maxDistance) {
                    if (player != null) {
                        // Start returning to player
                        proj.returnsToPlayer = false
                        proj.targetId = player.id
                        proj.isHoming = true
                        proj.homingStrength = proj.returnSpeed * 0.01f
                        // Reverse velocity direction
                        velocity.vx = -velocity.vx
                        velocity.vy = -velocity.vy
                    } else {
                        // No player found — destroy projectile
                        entity.active = false
                        continue
                    }
                }
            }

            // Boomerang homing back to player
            if (proj.isHoming && proj.targetId == (player?.id ?: -2) && playerPos != null) {
                val desiredAngle = GameMath.angleTo(transform.x, transform.y, playerPos.x, playerPos.y)
                val currentAngle = atan2(velocity.vy, velocity.vx)
                val angleDiff = GameMath.normalizeAngle(desiredAngle - currentAngle)
                val turnRate = proj.homingStrength * 5f * dt
                val newAngle = currentAngle + angleDiff.coerceIn(-turnRate, turnRate)
                velocity.vx = cos(newAngle)
                velocity.vy = sin(newAngle)

                // Check if returned to player
                val dist = GameMath.distance(transform.x, transform.y, playerPos.x, playerPos.y)
                if (dist < 20f) {
                    entity.active = false
                    continue
                }
            }
            // ── Regular homing ────────────────────────────────────
            else if (proj.isHoming && proj.targetId >= 0) {
                val target = engine.getActiveEntities().find { it.id == proj.targetId && it.active }
                if (target != null) {
                    val targetPos = target.get<TransformComponent>()
                    if (targetPos != null) {
                        val desiredAngle = GameMath.angleTo(transform.x, transform.y, targetPos.x, targetPos.y)
                        val currentAngle = atan2(velocity.vy, velocity.vx)
                        val angleDiff = GameMath.normalizeAngle(desiredAngle - currentAngle)
                        val turnRate = proj.homingStrength * dt
                        val newAngle = currentAngle + angleDiff.coerceIn(-turnRate, turnRate)
                        velocity.vx = cos(newAngle)
                        velocity.vy = sin(newAngle)
                    }
                } else {
                    proj.isHoming = false
                }
            }

            // Update rotation to face movement direction
            transform.rotation = atan2(velocity.vy, velocity.vx)

            // Spawn trail particle
            if (shouldSpawnTrail) {
                spawnTrailParticle(transform.x, transform.y, proj.weaponType)
            }
        }
    }

    private fun spawnTrailParticle(x: Float, y: Float, weaponType: WeaponType) {
        val color = when (weaponType) {
            WeaponType.MAGIC_MISSILE -> 0xFF6BB6FF.toInt()
            WeaponType.FIREBALL -> 0xFFFFCC80.toInt()
            WeaponType.ICE_SHARD -> 0xFF80CBC4.toInt()
            WeaponType.BOOMERANG_DAGGER -> 0xFFFFDAC1.toInt()
            WeaponType.DIVINE_SPEAR -> 0xFFFFF5E1.toInt()
            else -> 0xFF808080.toInt()
        }
        val p = engine.createEntity("particle")
        val offset = GameMath.randomPointInCircle(3f)
        p.add(TransformComponent(x + offset.x, y + offset.y))
        p.add(SpriteComponent(
            width = 6f, height = 6f,
            color = color, alpha = 0.6f,
            shape = SpriteShape.CIRCLE
        ))
        p.add(ParticleComponent(lifetime = 0.25f, fadeOut = true, shrink = true))
    }
}
