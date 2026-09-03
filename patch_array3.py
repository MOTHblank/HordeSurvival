import os

files_to_patch = {
    "app/src/main/java/com/hordesurvival/game/engine/ecs/systems/WeaponSystem.kt": [
        ("_enemyQueryResult.removeAt(_enemyQueryResult.size - 1)", "_enemyQueryResult.removeIndex(_enemyQueryResult.size - 1)")
    ]
}

for filepath, replacements in files_to_patch.items():
    with open(filepath, 'r') as f:
        content = f.read()

    for old, new in replacements:
        content = content.replace(old, new)

    with open(filepath, 'w') as f:
        f.write(content)
