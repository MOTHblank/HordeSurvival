package com.hordesurvival.game.engine.ecs.systems

import com.hordesurvival.game.component.HealthComponent
import com.hordesurvival.game.engine.GameEngine
import com.hordesurvival.game.engine.ecs.Entity
import com.hordesurvival.game.engine.ecs.System

/**
 * System to update visual hit flash timers on entities that take damage.
 */
class HitFlashSystem(private val engine: GameEngine) : System() {
    override fun update(dt: Float, entities: List<Entity>) {
        for (i in 0 until entities.size) {
            val entity = entities[i]
            if (!entity.active) continue
            val health = entity.get<HealthComponent>() ?: continue
            if (health.hitFlashTimer > 0f) {
                health.hitFlashTimer -= dt
            }
        }
    }
}
