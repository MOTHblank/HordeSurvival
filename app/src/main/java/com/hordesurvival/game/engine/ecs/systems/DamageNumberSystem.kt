package com.hordesurvival.game.engine.ecs.systems

import com.hordesurvival.game.component.DamageNumberComponent
import com.hordesurvival.game.component.TransformComponent
import com.hordesurvival.game.engine.ecs.Entity
import com.hordesurvival.game.engine.GameEngine
import com.hordesurvival.game.engine.ecs.System

/**
 * Updates floating damage numbers: moves them upward and removes when expired.
 */
class DamageNumberSystem(private val engine: GameEngine) : System() {

    override fun update(dt: Float, entities: List<Entity>) {
        for (i in 0 until entities.size) {
            val entity = entities[i]
            if (entity.tag != "damage_number") continue
            val dn = entity.get<DamageNumberComponent>() ?: continue
            val transform = entity.get<TransformComponent>() ?: continue

            // Float upward
            transform.y += dn.vy * dt
            dn.vy *= 0.95f  // decelerate

            // Update timer
            dn.timer += dt
            if (dn.timer >= dn.lifetime) {
                entity.active = false
            }
        }
    }
}
