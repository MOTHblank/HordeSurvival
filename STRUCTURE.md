# Project Structure & Architecture

## Overview
**Horde Survival** is an Android 2D action-survival horde game built with Kotlin. The game combines Jetpack Compose for UI management and navigation with a custom high-performance Entity Component System (ECS) game engine for real-time game simulation and libGDX / custom graphics rendering.

---

## Directory & File Organization

```
.
├── app/                        # Main Android application module
│   ├── build.gradle.kts        # Module-level Gradle configuration & dependencies
│   ├── proguard-rules.pro      # ProGuard / R8 obfuscation and optimization rules
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml
│           ├── java/com/hordesurvival/   # Kotlin source files
│           └── res/                      # Android resources (strings, drawables, themes)
├── gradle/                     # Gradle wrapper directory
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── build.gradle.kts            # Top-level Gradle build file
├── gradle.properties           # Project-wide Gradle settings
├── settings.gradle.kts         # Module settings & dependency repositories
├── gradlew                     # Gradle wrapper script (Unix)
└── gradlew.bat                 # Gradle wrapper script (Windows)
```

---

## Package Breakdown (`app/src/main/java/com/hordesurvival`)

### Top-Level Application & Entry Points
* `HordeSurvivalApp.kt` - Main Application subclass initializing global state and dependencies.
* `MainActivity.kt` - Main entry point Activity hosting Jetpack Compose navigation and game containers.
* `SplashActivity.kt` - Initial splash screen Activity.

### Core Modules

#### 1. `game/` — Game Engine & Simulation
Contains the custom ECS framework, game rules, systems, and game world state.
* `engine/`:
  * `GameEngine.kt` - Main game loop driver managing entity creation, updates, and system execution.
  * `ecs/Component.kt` - Base interface and contracts for entity data components.
  * `ecs/Entity.kt` - Entity identifier and component bag representation.
  * `ecs/System.kt` - Base class for processing system logic over filtered entities.
  * `ecs/systems/` - Individual processing systems:
    * `MovementSystem.kt` - Handles position updates and velocity logic.
    * `CollisionSystem.kt` - Detects collisions between entities (player, enemies, projectiles, XP drops).
    * `WeaponSystem.kt` - Weapon firing rates, targeting, and projectile spawning.
    * `EnemyAISystem.kt` - Enemy movement, pathfinding, and target tracking.
    * `WaveManagerSystem.kt` - Enemy wave generation, scaling, and boss encounters.
    * `ProjectileSystem.kt`, `ParticleSystem.kt`, `DamageNumberSystem.kt` - Visual & combat effects.
    * `XpDropSystem.kt`, `LootBoxSystem.kt`, `ComboSystem.kt`, `OrbitSystem.kt`, `EliteAbilitySystem.kt`, `RelicSystem.kt`, `AchievementSystem.kt`, `PlayerInputSystem.kt`.
* `achievement/` - Achievement tracking, progression (`AchievementProgress.kt`), and rewards (`AchievementRewards.kt`, `AchievementSystem.kt`).
* `audio/` - Audio controllers for dynamic music (`DynamicMusic.kt`) and sound effects (`SoundManager.kt`).
* `blessing/` - Temporary buffs and shrine blessing mechanics (`BlessingSystem.kt`).
* `character/` - Playable character definitions (`CharacterClass.kt`) and unique character abilities (`CharacterAbilities.kt`).
* `combo/` - Kill combo multipliers and visual effects (`ComboVisual.kt`).
* `component/` - Specific component data structures (e.g., Position, Velocity, Health, Weapon, Target, Renderable) (`Components.kt`).
* `enemy/` - Enemy entity definitions (`EnemyType.kt`) and elite/boss special abilities (`EliteAbilities.kt`).
* `haptic/` - Vibration and tactile feedback manager (`HapticManager.kt`).
* `hazard/` - Environmental stage hazards and traps (`StageHazardSystem.kt`, `MapHazard.kt`).
* `map/` - Map grid, boundaries, and terrain features (`GameMap.kt`).
* `mode/` - Special game modes (`GameModes.kt`), daily challenges (`DailyChallenge.kt`), and quest progressions (`QuestMode.kt`).
* `offline/` - Idle & offline rewards calculation (`OfflineProgress.kt`).
* `pet/` - Companion pets and autonomous helpers (`CompanionPet.kt`).
* `prestige/` - Meta-progression and prestige system (`PrestigeSystem.kt`).
* `relic/` - Passive relic items and artifact modifiers (`RelicSystem.kt`).
* `shop/` - In-run temporary item shop logic (`InRunShop.kt`).
* `skin/` - Visual cosmetic skins (`SkinSystem.kt`).
* `synergy/` - Weapon and item combinations/synergies (`WeaponSynergy.kt`).
* `upgrade/` - In-run upgrade selections and passive bonus types (`UpgradeSystem.kt`, `PassiveType.kt`).
* `weapon/` - Weapon types (`WeaponType.kt`) and weapon evolution formulas (`WeaponEvolution.kt`).

#### 2. `data/` — Persistence & Repository Layer
Manages local state persistence, player saves, stats, and database interactions.
* `database/AppDatabase.kt` - Room Database instance setup.
* `database/Daos.kt` - Data Access Objects for game stats, unlocks, and save data.
* `model/Models.kt` - Data entities and domain models.
* `repository/GameRepository.kt` - Repository layer abstracting database operations.
* `GameSaveManager.kt` - Serialization and DataStore/Preferences save state manager.

#### 3. `ui/` — User Interface & Render Overlays
Built using Jetpack Compose for UI screens and LibGDX / Android Canvas view rendering.
* `screens/` - Screen composables:
  * `menu/` - `MainMenuScreen.kt`, `ModeSelectScreen.kt`
  * `game/` - `GameScreen.kt`, `GameHud.kt`, `GameRenderer.kt`, `PauseScreen.kt`, `LevelUpScreen.kt`, `MinimapComposable.kt`, `TowerDefenseStageComplete.kt`
  * `characterselect/` - `CharacterSelectScreen.kt`
  * `mapselect/` - `MapSelectScreen.kt`
  * `shop/` - `ItemShopScreen.kt`
  * `upgrades/` - `UpgradesScreen.kt`
  * `stats/` - `StatsScreen.kt`
  * `settings/` - `SettingsScreen.kt`
  * `tutorial/` - `TutorialScreen.kt`
  * `gameover/` - `GameOverScreen.kt`
* `viewmodel/` - Jetpack ViewModels (`GameViewModel.kt`, `MainViewModel.kt`) bridging UI state and game models.
* `theme/` - Color schemes, typography, and styling (`Theme.kt`).
* `Locales.kt` - Localization and language helpers.

#### 4. `utils/` — Core Utilities
* `Constants.kt` - Global application constants, balance values, and configurations.
* `MathUtils.kt` - Vector math, angle calculations, and distance functions.
* `ObjectPool.kt` - Memory management tool to pool reusable objects (e.g., particles, projectiles) and reduce GC overhead.

---

## Architectural Patterns

1. **Entity Component System (ECS)**
   * Used for the core gameplay loop.
   * Entities are lightweight containers holding components.
   * Components are pure data holders (e.g., position, health, velocity).
   * Systems contain logic operating on entities possessing specific component compositions.

2. **Model-View-ViewModel (MVVM)**
   * Powers the Compose UI screens and state management.
   * ViewModels expose StateFlow / MutableState to Jetpack Compose views while communicating with `GameRepository` and `GameEngine`.

3. **Repository Pattern & Persistence**
   * Uses Android Room Database and DataStore to store player progress, high scores, upgrades, and preferences.
