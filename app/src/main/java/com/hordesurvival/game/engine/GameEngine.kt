package com.hordesurvival.game.engine

import com.hordesurvival.game.component.PlayerComponent
import com.hordesurvival.game.component.TransformComponent
import com.hordesurvival.game.component.WeaponStateComponent
import com.hordesurvival.game.engine.ecs.Entity
import com.hordesurvival.game.engine.ecs.System
import com.hordesurvival.utils.ObjectPool
import java.util.concurrent.atomic.AtomicInteger

/**
 * Core game engine managing the ECS world.
 * Overhauled: integrated 2D SpatialGrid for spatial queries (findNearest, findInRange, collisions),
 * and O(1) entityByIdMap lookup for targeted projectile tracking.
 */
class GameEngine {

    val entities = mutableListOf<Entity>()
    private val systems = mutableListOf<System>()
    private val entitiesToAdd = mutableListOf<Entity>()
    private var nextEntityId = AtomicInteger(0)

    // Spatial partitioning grid for zero-allocation range/collision queries
    val spatialGrid = SpatialGrid(cellSize = 128f)

    // O(1) ID lookup map to eliminate O(N) linear scans during homing/targeting
    private val entityByIdMap = com.badlogic.gdx.utils.IntMap<Entity>(512)

    // Entity pool for reuse
    private val entityPool = ObjectPool(
        factory = { Entity(-1) },
        reset = {
            it.active = false
            it.tag = ""
            it.age = 0f
            it.clearComponents()  // Components become GC-eligible
        },
        initialSize = 128
    )

    // Cached active entities list — reused every frame to avoid allocation
    private val _activeEntitiesCache = mutableListOf<Entity>()
    val cachedActiveEntities: List<Entity> get() = _activeEntitiesCache

    // Public accessor for inline functions
    @PublishedApi
    internal val activeEntitiesCache: MutableList<Entity> get() = _activeEntitiesCache

    var playerEntity: Entity? = null
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
        entity.clearComponents()
        entity.id = nextEntityId.getAndIncrement()
        entityByIdMap.put(entity.id, entity)
        if (tag == "player" && playerEntity == null) {
            playerEntity = entity
        }
        entitiesToAdd.add(entity)
        return entity
    }

    fun removeEntity(entity: Entity) {
        entity.active = false
        if (entity === playerEntity) {
            playerEntity = null
        }
    }

    /** Fast O(1) entity lookup by ID */
    fun getEntityById(id: Int): Entity? {
        val e = entityByIdMap.get(id)
        return if (e != null && e.active) e else null
    }

    /** Return inactive entities to pool + force-kill stale entities */
    private fun recycleEntities() {
        var writeIndex = 0
        for (i in 0 until entities.size) {
            val e = entities[i]

            if (!e.active && !e.has<PlayerComponent>()) {
                if (e === playerEntity) {
                    playerEntity = null
                }
                entityByIdMap.remove(e.id)
                entityPool.free(e)
            } else {
                if (e.active && !e.has<PlayerComponent>()) {
                    // Never recycle weapon state entities — they must persist for weapons to keep firing
                    if (!e.has<WeaponStateComponent>()) {
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
                entities[writeIndex++] = e
            }
        }

        // Remove trailing elements to avoid O(N^2) shifting from iterator removal
        while (entities.size > writeIndex) {
            entities.removeAt(entities.size - 1)
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

    /**
     * Finds nearest entity using SpatialGrid query.
     */
    fun findNearest(x: Float, y: Float, tag: String, maxDist: Float = Float.MAX_VALUE): Entity? {
        return spatialGrid.findNearest(x, y, tag, maxDist)
    }

    // Reusable list for findInRange to avoid allocation
    private val _findRangeResult = com.badlogic.gdx.utils.Array<Entity>(false, 128)

    /**
     * Finds all entities within [radius] matching [tag] using SpatialGrid query.
     */
    fun findInRange(x: Float, y: Float, radius: Float, tag: String): com.badlogic.gdx.utils.Array<Entity> {
        _findRangeResult.clear()
        spatialGrid.queryRange(x, y, radius, tag, _findRangeResult)
        return _findRangeResult
    }

    fun getActiveEntities(): List<Entity> = _activeEntitiesCache

    fun getEntityCount(): Int {
        return _activeEntitiesCache.size
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

        // Update cached active entities list and rebuild SpatialGrid
        _activeEntitiesCache.clear()
        spatialGrid.clear()

        for (i in 0 until entities.size) {
            val e = entities[i]
            if (e.active) {
                e.age += scaledDt  // track entity age
                _activeEntitiesCache.add(e)
                if (e.has<TransformComponent>()) {
                    spatialGrid.insert(e)
                }
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
        entityByIdMap.clear()
        spatialGrid.clear()
        entityPool.freeAll()
        systems.forEach { it.dispose() }
        systems.clear()
        nextEntityId.set(0)
        gameTime = 0f
        isPaused = false
        isGameOver = false
        playerEntity = null
        shakeIntensity = 0f; shakeDuration = 0f; shakeTimer = 0f
        shakeOffsetX = 0f; shakeOffsetY = 0f
        bossIntroTimer = 0f
    }
}
