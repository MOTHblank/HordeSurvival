package com.hordesurvival.game.engine.ecs

/**
 * Base entity in the Entity-Component-System architecture.
 * Each entity has an ID and a set of components.
 */
class Entity(var id: Int = -1) {
    val components = mutableMapOf<Class<out Component>, Component>()
    var active = true
    var tag: String = ""
    var age: Float = 0f  // seconds since creation — for stale entity cleanup

    fun <T : Component> add(component: T): Entity {
        components[component::class.java] = component
        return this
    }

    inline fun <reified T : Component> get(): T? {
        @Suppress("UNCHECKED_CAST")
        return components[T::class.java] as? T
    }

    inline fun <reified T : Component> has(): Boolean {
        return components.containsKey(T::class.java)
    }

    inline fun <reified T : Component> remove() {
        components.remove(T::class.java)
    }

    fun clearComponents() {
        components.clear()
    }
}
