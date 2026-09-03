import os

files_to_patch = {
    "app/src/main/java/com/hordesurvival/game/engine/SpatialGrid.kt": [
        ("LongMap<MutableList<Entity>>", "LongMap<com.badlogic.gdx.utils.Array<Entity>>"),
        ("ArrayList<MutableList<Entity>>", "com.badlogic.gdx.utils.Array<com.badlogic.gdx.utils.Array<Entity>>"),
        ("ArrayList<Entity>", "com.badlogic.gdx.utils.Array<Entity>"),
        ("MutableList<Entity>", "com.badlogic.gdx.utils.Array<Entity>"),
        ("if (listPool.isNotEmpty()) listPool.removeAt(listPool.size - 1) else com.badlogic.gdx.utils.Array(16)", "if (listPool.size > 0) listPool.pop() else com.badlogic.gdx.utils.Array<Entity>(false, 16)"),
        ("list.isEmpty()", "list.isEmpty"),
        ("listPool.isNotEmpty()", "listPool.size > 0"),
        ("cells.get(key)?.clear()", "cells.get(key)?.clear()"),
        ("var list = cells.get(key)", "var list = cells.get(key)"),
        ("val list = cells.get(key) ?: continue", "val list = cells.get(key) ?: continue"),
        ("listPool.removeAt(listPool.size - 1)", "listPool.pop()")
    ],
    "app/src/main/java/com/hordesurvival/game/engine/GameEngine.kt": [
        ("private val _findRangeResult = mutableListOf<Entity>()", "private val _findRangeResult = com.badlogic.gdx.utils.Array<Entity>(false, 64)"),
        ("fun findInRange(x: Float, y: Float, radius: Float, tag: String): List<Entity>", "fun findInRange(x: Float, y: Float, radius: Float, tag: String): com.badlogic.gdx.utils.Array<Entity>")
    ],
    "app/src/main/java/com/hordesurvival/game/engine/ecs/systems/CollisionSystem.kt": [
        ("private val _enemies = mutableListOf<Entity>()", "private val _enemies = com.badlogic.gdx.utils.Array<Entity>(false, 256)"),
        ("private val _projectiles = mutableListOf<Entity>()", "private val _projectiles = com.badlogic.gdx.utils.Array<Entity>(false, 256)"),
        ("private val _enemyProjectiles = mutableListOf<Entity>()", "private val _enemyProjectiles = com.badlogic.gdx.utils.Array<Entity>(false, 128)"),
        ("private val _xpGems = mutableListOf<Entity>()", "private val _xpGems = com.badlogic.gdx.utils.Array<Entity>(false, 128)"),
        ("private val _healthGems = mutableListOf<Entity>()", "private val _healthGems = com.badlogic.gdx.utils.Array<Entity>(false, 16)"),
        ("private val _poisonClouds = mutableListOf<Entity>()", "private val _poisonClouds = com.badlogic.gdx.utils.Array<Entity>(false, 16)"),
        ("private val _orbitShields = mutableListOf<Entity>()", "private val _orbitShields = com.badlogic.gdx.utils.Array<Entity>(false, 16)"),
        ("private val _nearbyEnemiesBuffer = mutableListOf<Entity>()", "private val _nearbyEnemiesBuffer = com.badlogic.gdx.utils.Array<Entity>(false, 64)")
    ],
    "app/src/main/java/com/hordesurvival/game/engine/ecs/systems/WeaponSystem.kt": [
        ("private val _enemyQueryResult = mutableListOf<Entity>()", "private val _enemyQueryResult = com.badlogic.gdx.utils.Array<Entity>(false, 64)"),
        ("fun findNearestEnemies(x: Float, y: Float, count: Int, maxDist: Float): MutableList<Entity>", "fun findNearestEnemies(x: Float, y: Float, count: Int, maxDist: Float): com.badlogic.gdx.utils.Array<Entity>"),
        ("nearestEnemies.removeAt(nearestEnemies.size - 1)", "nearestEnemies.removeIndex(nearestEnemies.size - 1)"),
        ("_enemyQueryResult.removeAt(minIdx)", "_enemyQueryResult.removeIndex(minIdx)")
    ],
    "app/src/main/java/com/hordesurvival/game/engine/ecs/systems/EnemyAISystem.kt": [
        ("private val _healCandidatesBuffer = mutableListOf<Entity>()", "private val _healCandidatesBuffer = com.badlogic.gdx.utils.Array<Entity>(false, 16)")
    ]
}

for filepath, replacements in files_to_patch.items():
    with open(filepath, 'r') as f:
        content = f.read()

    for old, new in replacements:
        content = content.replace(old, new)

    with open(filepath, 'w') as f:
        f.write(content)
