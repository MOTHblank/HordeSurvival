package com.hordesurvival.utils

/**
 * Generic object pool for performance-critical entities.
 * Reduces GC pressure by reusing enemy/projectile objects.
 */
class ObjectPool<T>(
    private val factory: () -> T,
    val reset: (T) -> Unit = {},
    initialSize: Int = 64
) {
    val available = ArrayDeque<T>(initialSize)
    val active = mutableSetOf<T>()

    init {
        repeat(initialSize) { available.addLast(factory()) }
    }

    /** Obtain an object from the pool */
    fun obtain(): T {
        val obj = if (available.isNotEmpty()) available.removeFirst() else factory()
        active.add(obj)
        return obj
    }

    /** Return an object to the pool */
    fun free(obj: T) {
        if (active.remove(obj)) {
            reset(obj)
            available.addLast(obj)
        }
    }

    /** Return all active objects to the pool */
    fun freeAll() {
        active.forEach { reset(it); available.addLast(it) }
        active.clear()
    }

    /** Number of active objects */
    val activeCount: Int get() = active.size

    /** Iterate over active objects */
    inline fun forEach(action: (T) -> Unit) {
        active.forEach(action)
    }

    /** Remove items matching predicate (returns them to pool) */
    inline fun freeIf(predicate: (T) -> Boolean) {
        val iterator = active.iterator()
        val toFree = mutableListOf<T>()
        while (iterator.hasNext()) {
            val obj = iterator.next()
            if (predicate(obj)) {
                toFree.add(obj)
                iterator.remove()
            }
        }
        toFree.forEach { reset(it); available.addLast(it) }
    }
}
