import os

files_to_patch = {
    "app/src/main/java/com/hordesurvival/game/engine/SpatialGrid.kt": [
        ("var list = cells.get(key) as com.badlogic.gdx.utils.Array<Entity>?", "var list = cells.get(key)"),
        ("private val cells = com.badlogic.gdx.utils.LongMap<com.badlogic.gdx.utils.Array<Entity>>(512)", "private val cells = com.badlogic.gdx.utils.LongMap<com.badlogic.gdx.utils.Array<Entity>>(512)\n    "),
    ],
    "app/src/main/java/com/hordesurvival/game/engine/ecs/systems/WeaponSystem.kt": [
        ("_enemyQueryResult.removeIndex(minIdx)\n            ", "_enemyQueryResult.removeIndex(minIdx)")
    ]
}

for filepath, replacements in files_to_patch.items():
    with open(filepath, 'r') as f:
        content = f.read()

    for old, new in replacements:
        content = content.replace(old, new)

    with open(filepath, 'w') as f:
        f.write(content)
