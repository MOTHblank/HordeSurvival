import os

files_to_patch = {
    "app/src/main/java/com/hordesurvival/game/engine/SpatialGrid.kt": [
        ("private val cells = com.badlogic.gdx.utils.LongMap<com.badlogic.gdx.utils.Array<Entity>>(512)\n    ", "private val cells = com.badlogic.gdx.utils.LongMap<com.badlogic.gdx.utils.Array<Entity>>(512)"),
        ("val list = cells.get(key) ?: continue", "val list = cells.get(key) as com.badlogic.gdx.utils.Array<Entity>? ?: continue")
    ]
}

for filepath, replacements in files_to_patch.items():
    with open(filepath, 'r') as f:
        content = f.read()

    for old, new in replacements:
        content = content.replace(old, new)

    with open(filepath, 'w') as f:
        f.write(content)
