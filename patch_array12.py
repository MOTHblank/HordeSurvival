import os

files_to_patch = {
    "app/src/main/java/com/hordesurvival/game/engine/SpatialGrid.kt": [
        ("if (listPool.size > 0) listPool.pop() else com.badlogic.gdx.utils.Array(false, 16)", "if (listPool.size > 0) listPool.pop() else com.badlogic.gdx.utils.Array<Entity>(false, 16)"),
        ("listPool.removeAt(listPool.size - 1)", "listPool.pop()"),
        ("cells.get(key)?.clear()", "cells.get(key)?.clear()"),
        ("val list = cells.get(key)", "val list = cells.get(key) as com.badlogic.gdx.utils.Array<Entity>?")
    ],
    "app/src/main/java/com/hordesurvival/game/engine/ecs/systems/WeaponSystem.kt": [
        ("_enemyQueryResult.removeAt(minIdx)", "_enemyQueryResult.removeIndex(minIdx)"),
        ("nearestEnemies.removeAt(nearestEnemies.size - 1)", "nearestEnemies.removeIndex(nearestEnemies.size - 1)")
    ]
}

for filepath, replacements in files_to_patch.items():
    with open(filepath, 'r') as f:
        content = f.read()

    for old, new in replacements:
        content = content.replace(old, new)

    with open(filepath, 'w') as f:
        f.write(content)
