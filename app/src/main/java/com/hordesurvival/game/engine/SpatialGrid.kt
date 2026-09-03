package com.hordesurvival.game.engine

import com.hordesurvival.game.component.TransformComponent
import com.hordesurvival.game.engine.ecs.Entity
import kotlin.math.floor

/**
 * High-performance 2D Spatial Partitioning Grid designed for fast range queries
 * and zero-allocation frame updates.
 *
 * Why: Replacing O(P * E) linear collision checks with spatial queries eliminates
 * the primary performance bottleneck during large enemy hordes (100+ projectiles vs 300+ enemies).
 */
class SpatialGrid(val cellSize: Float = 128f) {

    // Pool of lists mapped by cell hash key to avoid per-frame allocations
    private val cells = com.badlogic.gdx.utils.LongMap<MutableList<Entity>>(512)
    private val activeKeys = com.badlogic.gdx.utils.LongArray(512)
    private val listPool = ArrayList<MutableList<Entity>>(256)

    // Reusable list for queries when caller doesn't provide one
    private val queryResultScratch = com.badlogic.gdx.utils.Array<Entity>(false, 256)

    /**
     * Clears all entities from all cells without releasing bucket list memory.
     */
    fun clear() {
        for (i in 0 until activeKeys.size) {
            val key = activeKeys.get(i)
            cells.get(key)?.clear()
        }
        activeKeys.clear()
    }

    private fun getCellKey(cx: Int, cy: Int): Long {
        return (cx.toLong() shl 32) or (cy.toLong() and 0xFFFFFFFFL)
    }

    fun getCellX(x: Float): Int = floor(x / cellSize).toInt()
    fun getCellY(y: Float): Int = floor(y / cellSize).toInt()

    /**
     * Inserts an entity into the grid based on its world position.
     */
    fun insert(entity: Entity, x: Float, y: Float) {
        val cx = getCellX(x)
        val cy = getCellY(y)
        val key = getCellKey(cx, cy)

        var list = cells.get(key)
        if (list == null) {
            list = if (listPool.isNotEmpty()) listPool.removeAt(listPool.size - 1) else ArrayList(16)
            cells.put(key, list)
        }
        if (list.isEmpty()) {
            activeKeys.add(key)
        }
        list.add(entity)
    }

    /**
     * Inserts an entity into the grid using its TransformComponent.
     */
    fun insert(entity: Entity) {
        val t = entity.get<TransformComponent>() ?: return
        insert(entity, t.x, t.y)
    }

    /**
     * Queries entities within a radius around (x, y).
     * Populates [out] list to avoid garbage allocations. Always clears [out] before populating.
     * If [tagFilter] is specified, only entities matching that tag are returned.
     */
    fun queryRange(
        x: Float,
        y: Float,
        radius: Float,
        tagFilter: String? = null,
        out: com.badlogic.gdx.utils.Array<Entity> = queryResultScratch
    ): com.badlogic.gdx.utils.Array<Entity> {
        out.clear()

        val minCx = getCellX(x - radius)
        val maxCx = getCellX(x + radius)
        val minCy = getCellY(y - radius)
        val maxCy = getCellY(y + radius)

        val r2 = radius * radius

        for (cx in minCx..maxCx) {
            for (cy in minCy..maxCy) {
                val key = getCellKey(cx, cy)
                val list = cells.get(key) ?: continue

                for (i in 0 until list.size) {
                    val e = list[i]
                    if (!e.active) continue
                    if (tagFilter != null && e.tag != tagFilter) continue

                    val pos = e.get<TransformComponent>() ?: continue
                    val dx = pos.x - x
                    val dy = pos.y - y
                    if (dx * dx + dy * dy <= r2) {
                        out.add(e)
                    }
                }
            }
        }

        return out
    }

    /**
     * Finds the nearest entity matching [tagFilter] within [maxDist].
     */
    fun findNearest(
        x: Float,
        y: Float,
        tagFilter: String,
        maxDist: Float = Float.MAX_VALUE
    ): Entity? {
        var best: Entity? = null
        var bestDist2 = if (maxDist == Float.MAX_VALUE) Float.MAX_VALUE else maxDist * maxDist

        // Query candidates in spatial range if maxDist is constrained
        if (maxDist < 2000f) {
            val candidates = queryRange(x, y, maxDist, tagFilter, queryResultScratch)
            for (i in 0 until candidates.size) {
                val e = candidates[i]
                val pos = e.get<TransformComponent>() ?: continue
                val dx = pos.x - x
                val dy = pos.y - y
                val d2 = dx * dx + dy * dy
                if (d2 < bestDist2) {
                    bestDist2 = d2
                    best = e
                }
            }
            return best
        }

        // Global search across active cells if maxDist is infinite
        for (k in 0 until activeKeys.size) {
            val key = activeKeys.get(k)
            val list = cells.get(key) ?: continue
            for (i in 0 until list.size) {
                val e = list[i]
                if (!e.active || e.tag != tagFilter) continue
                val pos = e.get<TransformComponent>() ?: continue
                val dx = pos.x - x
                val dy = pos.y - y
                val d2 = dx * dx + dy * dy
                if (d2 < bestDist2) {
                    bestDist2 = d2
                    best = e
                }
            }
        }

        return best
    }
}
