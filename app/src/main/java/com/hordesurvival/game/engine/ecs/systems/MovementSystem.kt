package com.hordesurvival.game.engine.ecs.systems

import com.hordesurvival.game.component.*
import com.hordesurvival.game.engine.ecs.Entity
import com.hordesurvival.game.engine.ecs.System

/**
 * Movement system — no world bounds clamping.
 * Player moves freely, camera follows.
 */
class MovementSystem : System() {

    override fun update(dt: Float, entities: List<Entity>) {
        for (entity in entities) {
            if (!entity.active) continue
            val transform = entity.get<TransformComponent>() ?: continue
            val velocity = entity.get<VelocityComponent>() ?: continue

            // Apply slow effect
            var speedMultiplier = 1f
            entity.get<EnemyComponent>()?.let { enemy ->
                if (enemy.slowTimer > 0f) {
                    speedMultiplier = enemy.slowFactor
                    enemy.slowTimer -= dt
                }
            }

            // Apply player speed multiplier
            entity.get<PlayerComponent>()?.let { comp ->
                speedMultiplier *= comp.moveSpeed
            }

            val effectiveSpeed = velocity.speed * speedMultiplier

            // Update position — NO bounds clamping, infinite world
            transform.x += velocity.vx * effectiveSpeed * dt
            transform.y += velocity.vy * effectiveSpeed * dt
        }
    }
}
