package com.hordesurvival.game.engine.ecs

/**
 * Base class for ECS systems.
 * Systems process entities that have specific component combinations.
 */
abstract class System {
    var enabled = true
    var priority = 0

    /** Called once when the system is added to the engine */
    open fun initialize() {}

    /** Called every frame with delta time */
    abstract fun update(dt: Float, entities: List<Entity>)

    /** Called when the system is removed */
    open fun dispose() {}
}
