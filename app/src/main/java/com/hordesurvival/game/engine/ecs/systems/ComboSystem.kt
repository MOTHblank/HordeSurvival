package com.hordesurvival.game.engine.ecs.systems

import com.hordesurvival.game.component.*
import com.hordesurvival.game.engine.ecs.Entity
import com.hordesurvival.game.engine.ecs.System
import com.hordesurvival.game.audio.SoundManager

/**
 * Tracks kill combos — consecutive kills within a time window.
 * Grants XP bonus multiplier based on combo count.
 * Fixed: tracks already-counted entity IDs to prevent double counting.
 */
class ComboSystem : System() {

    // Track entity IDs that have already been counted as kills this cycle
    private val countedKills = mutableSetOf<Int>()

    override fun update(dt: Float, entities: List<Entity>) {
        val player = entities.find { it.tag == "player" && it.has<PlayerComponent>() } ?: return
        val combo = player.get<ComboComponent>() ?: return

        // Update combo timer
        combo.update(dt)

        // If combo reset, clear counted kills
        if (combo.count == 0) {
            countedKills.clear()
        }

        // Check for enemy deaths this frame — only count new deaths
        for (entity in entities) {
            if (entity.tag != "enemy") continue
            val health = entity.get<HealthComponent>() ?: continue
            if (health.isDead && entity.active && entity.id !in countedKills) {
                countedKills.add(entity.id)
                combo.addKill()
                SoundManager.playCombo(combo.count)
            }
        }
    }

    override fun dispose() {
        countedKills.clear()
    }
}
