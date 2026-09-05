import os
import re

files_to_patch = [
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

for file in files_to_patch:
    with open(file, 'r') as f:
        content = f.read()

    # The memory mentions: "To maintain consistent layout widths across primary game menus, standard action buttons (`HordeButton`, `HordeSecondaryButton`) have a default `Modifier.widthIn(max = 350.dp).fillMaxWidth(0.85f)`. Do not hardcode these width modifiers at the call sites to prevent redundant layout constraints."

    # In fact, we can see in HordeUI.kt that HordeButton and HordeSecondaryButton already have:
    # modifier: Modifier = Modifier.widthIn(max = 350.dp).fillMaxWidth(0.85f)

    # Let's clean up any call sites where HordeButton and HordeSecondaryButton pass:
    # modifier = Modifier.widthIn(max = 350.dp).fillMaxWidth(0.85f)
    # OR modifier = Modifier.widthIn(max = 400.dp).fillMaxWidth(...)
    # wait, they pass it explicitly sometimes, or the memory says "Do not hardcode these width modifiers at the call sites"

    # I will just write a script that regex removes any hardcoded width modifiers on these buttons if it exists.
    pass

print("Done")
