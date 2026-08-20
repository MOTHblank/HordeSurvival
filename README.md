# 🧟 Horde Survival

A **Vampire Survivors-style** Android game built with **Kotlin + Jetpack Compose**.

Survive the endless horde, level up, evolve your weapons, and become unstoppable!

---

## 📥 Download

**Latest Version: v1.2.9**

[📥 Download APK](https://github.com/niksiratforex-ux/HordeSurvival/releases/download/v1.2.9/HordeSurvival-v1.2.9-debug.apk)

Or build from source:
```bash
git clone https://github.com/niksiratforex-ux/HordeSurvival.git
cd HordeSurvival
./gradlew assembleDebug
```

📄 [Full Changelog](CHANGELOG.md)

---

## 🎮 v1.2.9 — What's New

### 🐛 Bug Fixes
- **Weapons stop firing** — fixed weapon entities being recycled after 30 seconds
- **Tower Defense enemy cap** — enemies per stage are now capped to prevent overwhelming floods
- **TD score auto-collect** — score now correctly accumulates on every enemy kill

## 🎮 v1.2.8.1 — What's New

### ⚡ Performance
- **Entity filter consolidation** — replaced 4+ filter calls per frame with single-pass categorization
- **Layer sort** — insertion sort for nearly-sorted entity lists (O(n) vs O(n log n))
- **Grid background** — removed intersection dots (eliminated nested loop)
- **Damage numbers** — cached formatted strings, no more String.format every frame

### 🎨 Graphics
- **Death explosions** — particles scale with enemy size; bosses get ring explosion effect
- **XP Magnet visual** — magnetized gems glow blue with sparkle ring + magnet line to player
- **Low HP warning** — red vignette edges pulse when HP drops below 30%
- **Boss intro** — orange screen flash + stronger shake when boss spawns
- **Combo enhancement** — bounce scale + edge glow for high combo tiers
- **Weapon trails** — fireball (orange), ice (cyan), missile (blue), spear (gold) trailing particles

---

## 🎮 v1.2.8 — What's New

### 🐛 Critical Bug Fixes

**1. Blessing System — actually works now!**
- **Resilience** (🛡️ armor) blessing was calculated but **never applied** to the player — you bought armor and got nothing
- **Fortune** (💰 gold gain) blessing was calculated but **never applied** — gold drops were not multiplied
- Both blessings now properly affect gameplay

**2. Performance Fix — Orbiting Shield**
- Orbiting Shield weapon was doing an O(n) entity search **every frame inside its loop** — moved outside loop
- Should noticeably reduce lag when using Orbiting Shield with many enemies on screen

**3. Code Quality**
- Removed unnecessary null checks and assertions in 4 files
- Cleaned up unused variables in DailyChallenge and QuestMode

---

## 🎮 v1.2.7 — What's New

### 🐛 Bug Fixes

**1. Continue After Death** — fixed!
- Continue now properly resumes from where you died (not a restart)
- Continue button limited to one use per run

**2. Tower Defense Mode** — major improvements
- 🔲 Left/right boundary walls now visible (purple glow)
- 💎 XP and loot auto-collect toward player
- 🏆 Stage complete screen with kills, gold, and lives stats
- ▶ Next Stage / 🔄 Replay / 🏠 Main Menu after each stage
- Stages unlock progressively as you clear them

**3. Tutorial** — updated
- Covers all new systems: Weapon Synergy, Blessings, Pets, Prestige, Tower Defense

**4. Performance** — optimized for low-end devices
- Entity hard cap at 500 (prevents crash when screen is crowded)
- `findInRange` optimized (zero per-call allocation)
- Particle cap at 150, improved object pool recycling
- AGP 8.2.0 → 8.2.2 (fixes KSP build compatibility)

---

## 🎮 v1.2.6 — What's New

### 🆕 9 New Features

**1. Daily Challenge Mode** — unique modifiers every day
- 10 rotating challenges (Swarm Day, Iron Wall, Speed Demon, No Mercy, etc.)
- Same challenge for all players worldwide
- Special rewards for completion

**2. Relic System** — permanent passive items on map
- 12 relics with 4 rarity tiers (Common → Legendary)
- Crown, Wings, Armor, Clover, Ring, Amulet, Chalice, Gauntlet, Oracle, Phoenix Feather, Time Glass, Void Heart
- Spawn every 45 seconds, last 2 minutes

**3. Elite Enemy Abilities** — 8 special abilities
- 🌀 Teleport — teleports near player every 3s
- 🛡️ Shield — regenerating HP shield
- 💥 Explode on Death — AOE damage
- 💚 Healer Aura — heals nearby enemies
- ⚡ Speed Boost — faster when low HP
- 🔪 Split on Death — spawns 3 mini-elites
- 🧛 Vampiric — heals when hitting player
- 🌵 Thorns — reflects 30% damage

**4. Combo Visual** — tiered display with effects
- 6 tiers: Bronze → Silver → Gold → Platinum → Diamond → Godlike
- Screen flash at higher combos
- Color-coded counter

**5. Achievement Progress** — 24 achievements with tracking
- Kill milestones (1, 100, 1000, 5000)
- Survival milestones (5min, 10min, 30min, 60min)
- Level milestones (10, 25, 50, 100)
- Boss kills, combos, weapon collection
- Progress bars show completion

**6. Character Abilities** — 8 unique active skills
- ⏸️ Time Freeze — freeze all enemies 3s (Magic Missile)
- ☄️ Meteor Strike — massive AOE damage (Fireball)
- 👤 Shadow Clone — fighting clone 5s (Boomerang)
- 💚 Healing Aura — 50% heal + 3s invincible (Shield)
- ⚡ Lightning Storm — strike all enemies (Lightning)
- 🔥 Berserker Rage — 2x damage/speed 5s (Spear)
- ❄️ Frost Nova — freeze + damage nearby (Ice)
- 💀 Soul Harvest — kill enemies below 30% HP (Poison)

**7. Stage Hazards** — 8 environmental dangers
- 🔥 Fire Geyser — periodic burst
- 🧊 Ice Patch — slows enemies
- 🗡️ Spike Trap — contact damage
- ☠️ Poison Swamp — DOT damage
- ⚡ Lightning Rod — strikes nearest enemy
- 💚 Healing Spring — heals player
- 🟣 Teleport Pad — random teleport
- 🔴 Fire Wall — burns passers

**8. Minimap** — real-time tracking
- Enemy positions (red dots)
- Loot positions (gold/green dots)
- Player position (center blue dot)
- Boss indicators (orange)

**9. Tower Defense Mode** — complete rewrite
- Classic vertical shooter style (1942-style)
- Fixed screen size (phone dimensions)
- Player at bottom, moves left/right only
- Enemies spawn from top, move down
- 3 lives — enemy passes bottom = lose a life
- 5 unique TD weapons (Laser, Missile, Lightning, Freeze, Plasma)
- Weapon upgrade system (10 levels each)
- 10 stages with progressive difficulty
- Boss at end of each stage
- New weapon unlock per stage

### 🐛 Bug Fixes (v1.2.6)
- Game Over: Play Again works first tap
- Continue: works first tap (no double-press)
- Main Menu: properly resets game state
- Background: covers all zoom levels and directions
- Prestige HP: uses correct multiplier
- Daily Challenge: modifiers actually applied
- Ability system: unified (no duplicate)
- Achievement matching: precise prefix matching
- Synergy: only applies on weapon change (no stacking)
- Consistent signing key (updates install over previous)

### ⚖️ Balance Changes
- Health drop rate: 5% → 2%
- Enemy gold values: halved
- Blessing effects: reduced 60-70%
- Prestige multipliers: reduced 50%
- Difficulty: scales with time (not just level)
- Boss loot: fewer boxes, less gold

---

## 🎮 Full Feature List

### Core Gameplay
- **Auto-attack system** — weapons fire automatically
- **8 weapons** with 5 upgrade tiers + evolution
- **12 passive upgrades** (Spinach, Tome, Crown, Wings, etc.)
- **Character abilities** — unique active skill per weapon type
- **Weapon Synergy** — 8 bonus combos for matching weapons

### Enemies & Bosses
- **11 enemy types** with unique behaviors
- **Boss phase system** — 4 phases, minion spawns
- **Elite enemies** with 8 special abilities
- **Healer enemies** that heal nearby allies

### Game Modes
- **Survival** — endless horde
- **Daily Challenge** — unique daily modifiers
- **Tower Defense** — vertical shooter with 10 stages
- **Boss Rush** — continuous boss fights

### Systems
- **Relic System** — 12 permanent pickups
- **Loot Boxes** — health, gold, magnet, damage boost
- **Combo System** — 6 visual tiers
- **24 Achievements** with progress tracking
- **Prestige System** — 5 levels of permanent bonuses
- **Blessing System** — 10 meta-progression blessings
- **Companion Pets** — 5 pets with unique abilities

### Quality of Life
- **Minimap** — real-time enemy/loot tracking
- **Continue After Death** — revive once per run
- **6 languages** — EN, FA, ZH, JA, KO, ES
- **8 background themes**
- **Music/SFX controls**
- **Graphics quality settings**

---

## 🛠️ Tech Stack

| Component | Version |
|-----------|---------|
| Kotlin | 1.9.20 |
| Jetpack Compose BOM | 2023.10.01 |
| Compose Compiler | 1.5.5 |
| AGP | 8.2.0 |
| KSP | 1.9.20-1.0.14 |
| Gradle | 8.4 |
| JDK | 17 |
| compileSdk | 34 |
| minSdk | 24 |
| targetSdk | 34 |

### Architecture
- **Custom ECS** (Entity-Component-System) game engine
- **Canvas rendering** with Jetpack Compose
- **DataStore** for save data
- **Room Database** for run history

---

## 📁 Project Structure

```
app/src/main/java/com/hordesurvival/
├── game/
│   ├── achievement/     # AchievementRewards, AchievementProgress
│   ├── audio/           # SoundManager
│   ├── billing/         # BillingManager
│   ├── blessing/        # BlessingSystem
│   ├── character/       # CharacterAbilities
│   ├── combo/           # ComboVisual
│   ├── component/       # ECS Components (20+ types)
│   ├── enemy/           # EnemyType, EliteAbilities
│   ├── engine/
│   │   ├── ecs/         # Entity, Component, System base
│   │   └── ecs/systems/ # 15 game systems
│   ├── hazard/          # StageHazardSystem, MapHazard
│   ├── mode/            # GameModes, TowerDefense, DailyChallenge, QuestMode
│   ├── pet/             # CompanionPet
│   ├── prestige/        # PrestigeSystem
│   ├── relic/           # RelicSystem
│   ├── synergy/         # WeaponSynergy
│   ├── upgrade/         # UpgradeSystem, PassiveType
│   └── weapon/          # WeaponType, WeaponEvolution
├── ui/
│   ├── screens/         # Game, Menu, Settings, Shop, etc.
│   └── viewmodel/       # GameViewModel, MainViewModel
├── data/                # GameSaveManager, Database, Repository
└── utils/               # Constants, MathUtils, ObjectPool
```

---

## 📝 License

This project is open source.

---

**Made with ❤️ using Kotlin & Jetpack Compose**
