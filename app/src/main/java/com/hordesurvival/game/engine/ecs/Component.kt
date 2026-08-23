package com.hordesurvival.game.engine.ecs

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Marker interface for all ECS components.
 * Components are pure data containers — no logic.
 */
interface Component

/**
 * High-performance thread-safe registry mapping component classes to contiguous integer indices (0..63).
 * Enables O(1) bitwise component queries and array indexing without HashMap lookups.
 */
object ComponentRegistry {
    const val MAX_COMPONENTS = 64
    private val classToIndex = ConcurrentHashMap<Class<out Component>, Int>()
    private val nextIndex = AtomicInteger(0)

    fun getIndex(clazz: Class<out Component>): Int {
        var index = classToIndex[clazz]
        if (index == null) {
            val newIdx = nextIndex.getAndIncrement()
            check(newIdx < MAX_COMPONENTS) { "Exceeded MAX_COMPONENTS limit of $MAX_COMPONENTS" }
            val existing = classToIndex.putIfAbsent(clazz, newIdx)
            index = existing ?: newIdx
        }
        return index
    }

    inline fun <reified T : Component> getIndex(): Int {
        return getIndex(T::class.java)
    }
}
