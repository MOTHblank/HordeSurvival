import os
import re

files_to_check = [
    "app/src/main/java/com/hordesurvival/ui/screens/menu/MainMenuScreen.kt",
    "app/src/main/java/com/hordesurvival/ui/screens/menu/ModeSelectScreen.kt",
    "app/src/main/java/com/hordesurvival/ui/screens/game/PauseScreen.kt",
    "app/src/main/java/com/hordesurvival/ui/screens/game/TowerDefenseStageComplete.kt",
    "app/src/main/java/com/hordesurvival/ui/screens/gameover/GameOverScreen.kt",
    "app/src/main/java/com/hordesurvival/ui/screens/upgrades/UpgradesScreen.kt",
    "app/src/main/java/com/hordesurvival/ui/screens/shop/ItemShopScreen.kt",
    "app/src/main/java/com/hordesurvival/ui/screens/settings/SettingsScreen.kt",
    "app/src/main/java/com/hordesurvival/ui/screens/stats/StatsScreen.kt",
    "app/src/main/java/com/hordesurvival/ui/screens/tutorial/TutorialScreen.kt",
    "app/src/main/java/com/hordesurvival/ui/screens/game/LevelUpScreen.kt",
    "app/src/main/java/com/hordesurvival/ui/screens/game/GameHud.kt",
]

for filepath in files_to_check:
    with open(filepath, 'r') as f:
        content = f.read()

    # Find occurrences of Modifier.widthIn(max = ...).fillMaxWidth(...)
    # and replace with Modifier.widthIn(max = ...) [wait, memory says:
    # "To prevent UI elements from stretching excessively on wider screens, apply `Modifier.widthIn(max = ...)` strictly *before* `Modifier.fillMaxWidth(...)` so the maximum width is clamped prior to filling."]

    # Wait, the instruction is actually:
    # "To prevent UI elements from stretching excessively on wider screens, apply `Modifier.widthIn(max = ...)` strictly *before* `Modifier.fillMaxWidth(...)` so the maximum width is clamped prior to filling."

    # Looking at the memory, it says:
    # "When designing Jetpack Compose UI for responsiveness across different aspect ratios, use `GridCells.Adaptive` instead of `GridCells.Fixed` for grids. To prevent UI elements from stretching excessively on wider screens, apply `Modifier.widthIn(max = ...)` strictly *before* `Modifier.fillMaxWidth(...)` so the maximum width is clamped prior to filling."

    # Let's check `Modifier.fillMaxWidth(...).widthIn(max = ...)`
    pass

print("Done")
