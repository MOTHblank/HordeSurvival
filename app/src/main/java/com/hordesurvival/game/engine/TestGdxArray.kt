package com.hordesurvival.game.engine
import com.badlogic.gdx.utils.Array
import com.hordesurvival.game.engine.ecs.Entity

class TestGdxArray {
    fun test() {
        val arr = Array<Entity>()
        arr.add(Entity(0))
        val size = arr.size
        val e = arr[0]
        arr.clear()
    }
}
