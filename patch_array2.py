import os

files_to_patch = {
    "app/src/main/java/com/hordesurvival/game/engine/ecs/systems/WeaponSystem.kt": [
        ("nearestEnemies.removeAt(nearestEnemies.size - 1)", "nearestEnemies.removeIndex(nearestEnemies.size - 1)"),
        ("_enemyQueryResult.removeAt(minIdx)", "_enemyQueryResult.removeIndex(minIdx)")
    ],
    "app/src/main/java/com/hordesurvival/ui/components/HordeUI.kt": [
        ("import androidx.compose.foundation.interaction.MutableInteractionSource\n", "")
    ]
}

for filepath, replacements in files_to_patch.items():
    with open(filepath, 'r') as f:
        content = f.read()

    for old, new in replacements:
        content = content.replace(old, new)

    with open(filepath, 'w') as f:
        f.write(content)
