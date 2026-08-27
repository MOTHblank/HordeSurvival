package com.hordesurvival.game.engine.ecs.systems

import com.hordesurvival.game.component.*
import com.hordesurvival.game.engine.ecs.Entity
import com.hordesurvival.game.engine.GameEngine
import com.hordesurvival.game.engine.ecs.System
import com.hordesurvival.game.audio.SoundManager
import com.badlogic.gdx.utils.IntSet

/**
 * Tracks kill combos — consecutive kills within a time window.
 * Grants XP bonus multiplier based on combo count.
 * Fixed: tracks already-counted entity IDs to prevent double counting.
 */
class ComboSystem(private val engine: GameEngine) : System() {

    // Track entity IDs that have already been counted as kills this cycle
    // Bolt: Use libGDX IntSet instead of mutableSetOf<Int> to prevent autoboxing
    // Integer objects on every kill, reducing GC pressure during gameplay.
    private val countedKills = IntSet()

    override fun update(dt: Float, entities: List<Entity>) {
        val player = engine.playerEntity ?: return
        val combo = player.get<ComboComponent>() ?: return

        // Update combo timer
        combo.update(dt)

        // If combo reset, clear counted kills
        if (combo.count == 0) {
            countedKills.clear()
        }

        // Check for enemy deaths this frame — only count new deaths
        for (i in 0 until entities.size) {
            val entity = entities[i]
            if (entity.tag != "enemy") continue
            val health = entity.get<HealthComponent>() ?: continue
            if (health.isDead && entity.active && !countedKills.contains(entity.id)) {
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
