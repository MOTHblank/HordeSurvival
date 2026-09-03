import os

files_to_patch = {
    "app/src/main/java/com/hordesurvival/game/engine/SpatialGrid.kt": [
        ("cells.get(key)?.clear()", "(cells.get(key) as com.badlogic.gdx.utils.Array<Entity>?)?.clear()")
    ],
    "app/src/main/java/com/hordesurvival/game/engine/ecs/systems/WeaponSystem.kt": [
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
