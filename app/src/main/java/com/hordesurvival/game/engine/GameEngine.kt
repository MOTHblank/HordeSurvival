package com.hordesurvival.game.engine

import com.hordesurvival.game.component.PlayerComponent
import com.hordesurvival.game.component.WeaponStateComponent
import com.hordesurvival.game.engine.ecs.Entity
import com.hordesurvival.game.engine.ecs.System
import com.hordesurvival.utils.ObjectPool
import java.util.concurrent.atomic.AtomicInteger

/**
 * Core game engine managing the ECS world.
 * Fixed: inactive entity cleanup, proper entity lifecycle.
 * Optimized: reduced GC pressure with cached lists.
 */
class GameEngine {

    val entities = mutableListOf<Entity>()
    private val systems = mutableListOf<System>()
    private val entitiesToAdd = mutableListOf<Entity>()
    private var nextEntityId = AtomicInteger(0)

    // Entity pool for reuse
    private val entityPool = ObjectPool(
        factory = { Entity(-1) },
        reset = {
            it.active = false
            it.tag = ""
            it.age = 0f
            it.components.clear()  // Components become GC-eligible
        },
        initialSize = 128
    )

    // Cached active entities list — reused every frame to avoid allocation
    private val _activeEntitiesCache = mutableListOf<Entity>()
    val cachedActiveEntities: List<Entity> get() = _activeEntitiesCache

    // Public accessor for inline functions
    @PublishedApi
    internal val activeEntitiesCache: MutableList<Entity> get() = _activeEntitiesCache

    var gameTime = 0f
    var isPaused = false
    var isGameOver = false
    var gameSpeed: Float = 1f  // 0.5x, 1x, 2x, 3x

    // Boss intro flash timer
    var bossIntroTimer: Float = 0f

    // Screen shake
    var shakeIntensity = 0f
    var shakeDuration = 0f
    private var shakeTimer = 0f
    var shakeOffsetX = 0f
    var shakeOffsetY = 0f

    /** Trigger screen shake */
    fun shake(intensity: Float = 8f, duration: Float = 0.15f) {
        shakeIntensity = intensity
        shakeDuration = duration
        shakeTimer = 0f
    }

    fun createEntity(tag: String = ""): Entity {
        // Safety: prevent entity explosion on low-end devices
        if (tag != "player" && entities.size + entitiesToAdd.size > 500) {
            // At hard cap, recycle oldest non-player entity
            val oldest = entities.firstOrNull { !it.has<com.hordesurvival.game.component.PlayerComponent>() && it.active }
            if (oldest != null) oldest.active = false
        }
        val entity = entityPool.obtain()
        entity.active = true
        entity.tag = tag
        entity.components.clear()
        entity.id = nextEntityId.getAndIncrement()
        entitiesToAdd.add(entity)
        return entity
    }

    fun removeEntity(entity: Entity) {
        entity.active = false
    }

    /** Return inactive entities to pool + force-kill stale entities */
    private fun recycleEntities() {
        val iter = entities.iterator()
        while (iter.hasNext()) {
            val e = iter.next()
            if (!e.active && !e.has<PlayerComponent>()) {
                entityPool.free(e)
                iter.remove()
            } else if (e.active && !e.has<PlayerComponent>()) {
                // Never recycle weapon state entities — they must persist for weapons to keep firing
                if (e.has<WeaponStateComponent>()) continue

                // Force-kill entities that lived too long (stale cleanup)
                val maxAge = when (e.tag) {
                    "particle" -> 5f
                    "damage_number" -> 3f
                    "projectile" -> 6f
                    "enemy_projectile" -> 6f
                    "xp_gem" -> 30f
                    "health_gem" -> 30f
                    "loot_box" -> 25f
                    "relic" -> 60f
                    "poison_cloud" -> 15f
                    "orbit_shield" -> Float.MAX_VALUE  // never expire
                    "enemy" -> 120f  // enemies: 2 min max
                    else -> 30f
                }
                if (e.age > maxAge) {
                    e.active = false
                }
            }
        }
    }

    fun getSystems(): List<System> = systems.toList()

    fun addSystem(system: System): GameEngine {
        systems.add(system)
        systems.sortBy { it.priority }
        system.initialize()
        return this
    }

    inline fun <reified T : com.hordesurvival.game.engine.ecs.Component> getEntitiesWith(): List<Entity> {
        return activeEntitiesCache.filter { it.has<T>() }
    }

    fun findNearest(x: Float, y: Float, tag: String, maxDist: Float = Float.MAX_VALUE): Entity? {
        var best: Entity? = null
        var bestDist = maxDist * maxDist
        for (e in entities) {
            if (!e.active || e.tag != tag) continue
            val pos = e.get<com.hordesurvival.game.component.TransformComponent>() ?: continue
            val dx = pos.x - x
            val dy = pos.y - y
            val dist = dx * dx + dy * dy
            if (dist < bestDist) {
                bestDist = dist
                best = e
            }
        }
        return best
    }

    // Reusable list for findInRange to avoid allocation
    private val _findRangeResult = mutableListOf<Entity>()

    fun findInRange(x: Float, y: Float, radius: Float, tag: String): List<Entity> {
        _findRangeResult.clear()
        val r2 = radius * radius
        for (e in _activeEntitiesCache) {  // Use cached list instead of full entities
            if (!e.active || e.tag != tag) continue
            val pos = e.get<com.hordesurvival.game.component.TransformComponent>() ?: continue
            val dx = pos.x - x
            val dy = pos.y - y
            if (dx * dx + dy * dy <= r2) {
                _findRangeResult.add(e)
            }
        }
        return _findRangeResult  // Return reference (callers must not modify)
    }

    fun getActiveEntities(): List<Entity> = _activeEntitiesCache

    fun getEntityCount(): Int {
        var count = 0
        for (e in entities) { if (e.active) count++ }
        return count
    }

    fun update(dt: Float) {
        if (isPaused || isGameOver) return

        val scaledDt = dt * gameSpeed
        gameTime += scaledDt

        // Update screen shake
        if (shakeTimer < shakeDuration) {
            shakeTimer += dt
            val progress = (shakeTimer / shakeDuration).coerceIn(0f, 1f)
            val decay = 1f - progress
            val angle = (Math.random() * Math.PI * 2).toFloat()
            shakeOffsetX = kotlin.math.cos(angle) * shakeIntensity * decay
            shakeOffsetY = kotlin.math.sin(angle) * shakeIntensity * decay
        } else {
            shakeOffsetX = 0f
            shakeOffsetY = 0f
        }

        // Update boss intro flash
        if (bossIntroTimer > 0f) bossIntroTimer -= dt

        // Add pending entities BEFORE systems run (so they're visible this frame)
        if (entitiesToAdd.isNotEmpty()) {
            entities.addAll(entitiesToAdd)
            entitiesToAdd.clear()
        }

        // Update cached active entities list (reuse existing list, no allocation)
        _activeEntitiesCache.clear()
        for (e in entities) {
            if (e.active) {
                e.age += scaledDt  // track entity age
                _activeEntitiesCache.add(e)
            }
        }

        // Run all systems with cached list (scaled by game speed)
        for (system in systems) {
            if (system.enabled) {
                system.update(scaledDt, _activeEntitiesCache)
            }
        }

        // Cleanup: recycle inactive entities AND stale entities that lived too long
        recycleEntities()
    }

    fun reset() {
        entities.clear()
        entitiesToAdd.clear()
        _activeEntitiesCache.clear()
        entityPool.freeAll()
        systems.forEach { it.dispose() }
        systems.clear()
        nextEntityId.set(0)
        gameTime = 0f
        isPaused = false
        isGameOver = false
        shakeIntensity = 0f; shakeDuration = 0f; shakeTimer = 0f
        shakeOffsetX = 0f; shakeOffsetY = 0f
        bossIntroTimer = 0f
    }
}
