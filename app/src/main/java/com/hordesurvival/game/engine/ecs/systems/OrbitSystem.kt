package com.hordesurvival.game.engine.ecs.systems

import com.hordesurvival.game.component.OrbitComponent
import com.hordesurvival.game.component.TransformComponent
import com.hordesurvival.game.engine.ecs.Entity
import com.hordesurvival.game.engine.ecs.System
import kotlin.math.cos
import kotlin.math.sin

/**
 * Updates orbiting entities (shield weapon) around their center point.
 */
class OrbitSystem : System() {

    override fun update(dt: Float, entities: List<Entity>) {
        // Find player position for orbit center
        val player = entities.find { it.tag == "player" }
        val playerPos = player?.get<TransformComponent>()

        for (entity in entities) {
            if (entity.tag != "orbit_shield") continue
            val orbit = entity.get<OrbitComponent>() ?: continue
            val transform = entity.get<TransformComponent>() ?: continue

            // Update center to player position and sync radius
            if (playerPos != null) {
                orbit.centerX = playerPos.x
                orbit.centerY = playerPos.y
            }
            // Sync radius from weapon state if available
            // FIX: reuse already-found player instead of searching every frame
            if (player != null) {
                player.get<com.hordesurvival.game.component.PlayerComponent>()?.let { comp ->
                    orbit.radius = 60f * comp.area
                }
            }

            // Rotate
            orbit.angle += orbit.angularSpeed * dt
            if (orbit.angle > Math.PI.toFloat() * 2f) {
                orbit.angle -= Math.PI.toFloat() * 2f
            }

            // Update position on orbit
            transform.x = orbit.centerX + cos(orbit.angle) * orbit.radius
            transform.y = orbit.centerY + sin(orbit.angle) * orbit.radius
        }
    }
}
