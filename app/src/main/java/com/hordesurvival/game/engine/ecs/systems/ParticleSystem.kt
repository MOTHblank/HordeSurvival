package com.hordesurvival.game.engine.ecs.systems

import com.hordesurvival.game.component.*
import com.hordesurvival.game.engine.ecs.Entity
import com.hordesurvival.game.engine.ecs.System

/**
 * Particle system with linear shrink, velocity, and fade.
 * Fixed: linear shrink (not exponential), removed dead damage_number tag.
 */
class ParticleSystem : System() {

    companion object {
        const val MAX_PARTICLES = 150  // Cap particles for low-end device performance
    }

    override fun update(dt: Float, entities: List<Entity>) {
        var particleCount = 0

        for (entity in entities) {
            if (entity.tag != "particle") continue
            particleCount++

            // If too many particles, kill oldest ones
            if (particleCount > MAX_PARTICLES) {
                entity.active = false
                continue
            }

            val particle = entity.get<ParticleComponent>() ?: continue
            val sprite = entity.get<SpriteComponent>() ?: continue
            val transform = entity.get<TransformComponent>() ?: continue

            particle.timer += dt
            val progress = (particle.timer / particle.lifetime).coerceIn(0f, 1f)

            // Store initial size on first frame
            if (particle.startWidth == 0f) {
                particle.startWidth = sprite.width
                particle.startHeight = sprite.height
            }

            // Update position from velocity (with deceleration)
            entity.get<VelocityComponent>()?.let { vel ->
                val decel = 1f - progress * 0.7f
                transform.x += vel.vx * vel.speed * decel * dt
                transform.y += vel.vy * vel.speed * decel * dt
            }

            // Fade out (linear)
            if (particle.fadeOut) {
                sprite.alpha = particle.startAlpha * (1f - progress)
            }

            // Shrink (linear, not exponential)
            if (particle.shrink) {
                val scale = 1f - progress
                sprite.width = particle.startWidth * scale
                sprite.height = particle.startHeight * scale
            }

            // Remove expired
            if (particle.timer >= particle.lifetime) {
                entity.active = false
            }
        }
    }
}
