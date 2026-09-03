import os

files_to_patch = {
    "app/src/main/java/com/hordesurvival/game/engine/SpatialGrid.kt": [
        ("val list = cells.get(key) as com.badlogic.gdx.utils.Array<Entity>? ?: continue", "val list = cells.get(key) ?: continue"),
        ("var list = cells.get(key)", "var list = cells.get(key)"),
        ("val list = cells.get(key)", "val list = cells.get(key)"),
    ]
}

for filepath, replacements in files_to_patch.items():
    with open(filepath, 'r') as f:
        content = f.read()

    for old, new in replacements:
        content = content.replace(old, new)

    with open(filepath, 'w') as f:
        f.write(content)
