# Changelog — Horde Survival

## v1.2.9 — Bug Fixes

### Fixes
- **CRITICAL**: Weapons stop firing after ~30 seconds — weapon entities were recycled by stale entity cleanup. Added WeaponStateComponent check to skip weapon entities in recycleEntities()
- **Tower Defense**: Enemy count capped per stage — prevents overwhelming enemy floods. Continuous + wave spawning now respect max enemy limit
- **Tower Defense**: Score now auto-collects on enemy kills — rewired kill tracking from broken goldValue detection to direct XpDropSystem callback

## v1.2.8.1 — Performance & Graphics Overhaul

### Performance
- Entity filter consolidation: single-pass categorization replaces 4+ filter calls per frame
- Layer sort: insertion sort (O(n) for nearly-sorted) instead of sortedBy (O(n log n))
- Grid background: removed intersection dots (eliminated nested while loop)
- Damage numbers: cached formatted strings, avoids String.format allocation every frame

### Graphics
- Death explosions: particle count/size scales with enemy; boss gets ring explosion
- XP Magnet: magnetized gems glow blue with sparkle ring + magnet line to player
- Low HP warning: red vignette edges pulse when HP < 30%
- Boss intro: orange screen flash + stronger shake on boss spawn
- Combo enhancement: bounce scale + screen edge glow for high combo tiers
- Weapon trails: fireball (orange), ice (cyan), missile (blue), spear (gold) trailing particles

## v1.2.8 — Blessing System Fix
- **CRITICAL**: Resilience blessing (armor) was calculated but never applied to player
- **CRITICAL**: Fortune blessing (gold gain) was calculated but never applied — gold drops were not multiplied
- Added `goldGainBonus` field to PlayerComponent
- Gold drops and loot now properly apply fortune blessing multiplier
- Orbiting Shield: fixed O(n) entity search per frame inside loop (moved outside)
- Removed unnecessary null assertions and safe calls in 4 files
- Cleaned up unused variables in DailyChallenge and QuestMode

## v1.2.7 — Continue After Death + Tower Defense
- Continue after death: properly resumes from death point
- Tower Defense: visible boundary walls, XP/loot auto-collect, stage complete screen
- Tutorial: covers all new systems
- Performance: entity cap at 500, findInRange optimized, particle cap at 150
- AGP 8.2.0 → 8.2.2 (KSP compatibility)

## v1.2.6 — Daily Challenge + Quest Mode
- Daily Challenge Mode: 10 rotating challenges, same for all players
- Quest Mode: 3 daily quests with gold rewards
- In-Run Shop: buy weapons, passives, heals between waves
- Skin System: character skins with unique bonuses
- Offline Progress: earn gold while away
- 5 New Achievements

## v1.2.5 — Polish & Bug Fixes
- Continue after death: works correctly
- Play Again: single tap
- Touch: works after restart
- Background: 2.5x canvas, borders hidden
- Health items: red heart shape
- Swarm: small purple circle
- Lightning: outline instead of flash
- Tutorial: only from menu
- Settings: scroll + confirm dialog
- Score save: all states (death, quit, back, background)

## v1.2.4 — New Systems
- Weapon Synergy (8 combos)
- Blessing System (10 blessings)
- Companion Pet (5 animals)
- Map Hazards (5 types)
- Tower Defense Mode
- Boss Rush Extreme

## v1.2.3 — Prestige & Achievements
- Prestige System (5 levels)
- Achievement Rewards (14 achievements)
- Game Speed Control (0.5x–3x)
- Camera Zoom (close stationary, far moving)
- Graphics Quality (Low/Med/High)
- Accessibility Settings (damage numbers, particles, combo, shake)

## v1.2.0 — Evolution Update
- Weapon Evolution (8 weapons × evolution)
- Passive Items (12 types)
- Elite Enemies (teleport, shield, explode)
- Relic System (map pickups)
- Combo System

## v1.1.0 — Core Expansion
- 8 Weapons (Magic Missile, Lightning, Fireball, Ice, Poison, Boomerang, Shield, Spear)
- 11 Enemy Types
- Boss System
- Level-up Upgrade System
- Meta Progression (HP, Gold, Might, Cooldown, Speed, Luck upgrades)
- Character Select (multiple characters)
- Multiple Background Styles

## v1.0.0 — Initial Release
- Core ECS game engine
- Canvas rendering
- Virtual joystick controls
- Basic survival mode
- XP & leveling system
- Basic enemies & weapons
