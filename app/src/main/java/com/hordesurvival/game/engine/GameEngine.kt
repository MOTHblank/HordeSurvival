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
 * Visual pass: screen-shake feel fixes (weaker shakes can no longer stomp stronger ones,
 * amplitude is frame-rate normalized, pausing no longer freezes the camera at a random offset),
 * boss intro flash respects game speed, gameSpeed resets with the engine, and the entity-cap
 * recycler sacrifices particles/damage numbers first instead of silently deleting
 * enemies or weapon states.
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
    private val _activeEntitiesCache = com.badlogic.gdx.utils.Array<Entity>(false, 512)
    val cachedActiveEntities: com.badlogic.gdx.utils.Array<Entity> get() = _activeEntitiesCache

    // Public accessor for inline functions
    @PublishedApi
    internal val activeEntitiesCache: com.badlogic.gdx.utils.Array<Entity> get() = _activeEntitiesCache

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

    /**
     * Trigger screen shake.
     * CHANGED: a weaker shake no longer stomps a stronger one in progress —
     * a 3f crit shake landing during an 8f boss-hit shake used to erase the
     * boss impact entirely. Stronger (or equal) shakes restart; a weaker-but-
     * longer one only extends the tail of the current shake.
     */
    fun shake(intensity: Float = 8f, duration: Float = 0.15f) {
        if (intensity >= shakeIntensity || shakeTimer >= shakeDuration) {
            shakeIntensity = intensity
            shakeDuration = duration
            shakeTimer = 0f
        } else if (duration > shakeDuration - shakeTimer) {
            // weaker, but outlasts what's left of the current shake — keep the
            // stronger amplitude and let it ride for the longer duration
            shakeDuration = shakeTimer + duration
        }
    }

    fun createEntity(tag: String = ""): Entity {
        // Safety: prevent entity explosion on low-end devices.
        // CHANGED: victim selection. Previously firstOrNull over ANY active
        // non-player entity — under particle load that could silently deactivate
        // an enemy (vanishes with no death effect, drops no XP gem) or a
        // WeaponStateComponent entity (recycleEntities protects those from
        // recycling, but this path didn't — that weapon would stop firing for
        // the rest of the run). Now particles/damage numbers are sacrificed
        // first — they're numerous, short-lived, and their loss is invisible —
        // and weapon state is never eligible.
        if (tag != "player" && entities.size + entitiesToAdd.size > 500) {
            val victim = entities.firstOrNull {
                it.active && (it.tag == "particle" || it.tag == "damage_number")
            } ?: entities.firstOrNull {
                it.active && !it.has<PlayerComponent>() && !it.has<WeaponStateComponent>()
            }
            victim?.active = false
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
                        // NOTE (unchanged): the 120s enemy force-kill removes the
                        // enemy with NO death effect and NO XP/gold drop — an enemy
                        // that chases you for 2 minutes just vanishes and the reward
                        // is lost. Ideally this would route through the same death
                        // pipeline the combat systems use; left as-is because the
                        // engine can't reach those systems. Consider moving
                        // stuck-enemy cleanup into the enemy/death system instead.
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
    private val _findRangeResult = com.badlogic.gdx.utils.Array<Entity>(false, 64)

    /**
     * Finds all entities within [radius] matching [tag] using SpatialGrid query.
     */
    fun findInRange(x: Float, y: Float, radius: Float, tag: String): com.badlogic.gdx.utils.Array<Entity> {
        _findRangeResult.clear()
        spatialGrid.queryRange(x, y, radius, tag, _findRangeResult)
        return _findRangeResult
    }

    fun getActiveEntities(): com.badlogic.gdx.utils.Array<Entity> = _activeEntitiesCache

    fun getEntityCount(): Int {
        return _activeEntitiesCache.size
    }

    fun update(dt: Float) {
        // CHANGED: zero shake offsets when paused/over — a shake interrupted by
        // pause used to freeze the camera at its last random offset, leaving the
        // entire world shifted a few px under the pause overlay indefinitely.
        if (isPaused || isGameOver) {
            shakeOffsetX = 0f
            shakeOffsetY = 0f
            return
        }

        val scaledDt = dt * gameSpeed
        gameTime += scaledDt

        // Update screen shake
        if (shakeTimer < shakeDuration) {
            shakeTimer += dt
            val progress = (shakeTimer / shakeDuration).coerceIn(0f, 1f)
            val decay = 1f - progress
            // CHANGED: frame-rate normalization — the offset magnitude used to be
            // constant per FRAME, so a 120Hz screen showed literally twice the
            // jitter frequency of a 60Hz screen for the same shake. Scaling by
            // dt*60 (clamped, so lag spikes don't amplify) keeps perceived shake
            // energy consistent across refresh rates. Timer still runs on
            // unscaled dt: shake is a camera effect and should last the same
            // wall-clock time at any game speed.
            val amp = shakeIntensity * decay * (dt * 60f).coerceIn(0f, 1f)
            val angle = (Math.random() * Math.PI * 2).toFloat()
            shakeOffsetX = kotlin.math.cos(angle) * amp
            shakeOffsetY = kotlin.math.sin(angle) * amp
        } else {
            shakeOffsetX = 0f
            shakeOffsetY = 0f
        }

        // Update boss intro flash
        // CHANGED: scaled by game speed — it's a gameplay-paced visual event, and
        // at 3x speed the flash used to linger 3x longer relative to everything
        // else happening on screen.
        if (bossIntroTimer > 0f) bossIntroTimer -= scaledDt

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
        // CHANGED: was not reset — finishing a run at 3x speed leaked into the
        // next run, which opened in fast-forward until something else reset it
        gameSpeed = 1f
        playerEntity = null
        shakeIntensity = 0f; shakeDuration = 0f; shakeTimer = 0f
        shakeOffsetX = 0f; shakeOffsetY = 0f
        bossIntroTimer = 0f
    }
}