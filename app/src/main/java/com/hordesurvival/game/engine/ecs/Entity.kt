package com.hordesurvival.game.engine.ecs

/**
 * Base entity in the Entity-Component-System architecture.
 * Overhauled: replaced HashMap<Class, Component> with a direct-indexed sparse array Array<Component?>(64)
 * and a 64-bit primitive Long mask (`componentMask`).
 *
 * Old approach:
 * `components[Class]` required full HashMap lookup (hash computation, bucket traversal, equality check)
 * on every component access across every entity in every system every frame (~75,000 Map lookups/frame).
 * Clearing components dropped Node objects into memory, creating GC pressure.
 *
 * New approach:
 * 1. `has<T>()` executes in 1 CPU cycle via bitwise AND `(componentMask and (1L shl index)) != 0L`.
 * 2. `get<T>()` executes in 1 CPU cycle via direct array index access `components[index] as T?`.
 * 3. `clearComponents()` clears active components directly without allocating Map entries or GC churn.
 */
class Entity(var id: Int = -1) {
    @PublishedApi
    internal val components = arrayOfNulls<Component>(ComponentRegistry.MAX_COMPONENTS)

    @PublishedApi
    internal var componentMask: Long = 0L

    var active = true
    var tag: String = ""
    var age: Float = 0f  // seconds since creation — for stale entity cleanup

    fun <T : Component> add(component: T): Entity {
        val index = ComponentRegistry.getIndex(component::class.java)
        components[index] = component
        componentMask = componentMask or (1L shl index)
        return this
    }

    inline fun <reified T : Component> get(): T? {
        val index = ComponentRegistry.getIndex<T>()
        return if ((componentMask and (1L shl index)) != 0L) {
            @Suppress("UNCHECKED_CAST")
            components[index] as T?
        } else {
            null
        }
    }

    inline fun <reified T : Component> has(): Boolean {
        val index = ComponentRegistry.getIndex<T>()
        return (componentMask and (1L shl index)) != 0L
    }

    fun has(index: Int): Boolean {
        return (componentMask and (1L shl index)) != 0L
    }

    inline fun <reified T : Component> remove() {
        val index = ComponentRegistry.getIndex<T>()
        if ((componentMask and (1L shl index)) != 0L) {
            components[index] = null
            componentMask = componentMask and (1L shl index).inv()
        }
    }

    fun clearComponents() {
        if (componentMask != 0L) {
            var mask = componentMask
            while (mask != 0L) {
                val index = java.lang.Long.numberOfTrailingZeros(mask)
                components[index] = null
                mask = mask and (mask - 1)
            }
            componentMask = 0L
        }
    }
}
