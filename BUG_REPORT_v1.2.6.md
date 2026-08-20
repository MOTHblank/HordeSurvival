# Bug Report — v1.2.6

## Critical Bugs

### BUG-001: WeaponSynergy bonuses never applied
**File**: `game/synergy/WeaponSynergy.kt` + `ui/viewmodel/GameViewModel.kt`
**Severity**: HIGH
**Description**: `WeaponSynergy.calculateBonuses()` exists but is never called. Synergy bonuses (bonusMight, bonusSpeed, etc.) are calculated but never applied to the player. Players with matching weapon combos get no bonus.
**Fix**: Apply synergy bonuses in `GameViewModel.update()` after reading player state.

### BUG-002: BlessingSystem never integrated
**File**: `game/blessing/BlessingSystem.kt` + `ui/viewmodel/GameViewModel.kt`
**Severity**: HIGH
**Description**: BlessingSystem is fully defined (10 blessings) but never used in gameplay. Blessings are never applied to player stats.
**Fix**: Load blessing levels from MetaState and apply effects in `startGame()`.

### BUG-003: CompanionPet never instantiated
**File**: `game/pet/CompanionPet.kt` + `ui/viewmodel/GameViewModel.kt`
**Severity**: HIGH
**Description**: CompanionPet class exists (5 pets) but is never created or updated during gameplay.
**Fix**: Instantiate CompanionPet in `startGame()`, call `update()` in game loop, apply bonuses.

### BUG-005: PrestigeSystem bonuses never applied
**File**: `game/prestige/PrestigeSystem.kt` + `ui/viewmodel/GameViewModel.kt`
**Severity**: HIGH
**Description**: PrestigeSystem is defined (5 levels) but prestige bonuses are never applied to player stats.
**Fix**: Load prestige level from MetaState and apply multipliers in `startGame()`.

### BUG-006: AchievementRewards never granted
**File**: `game/achievement/AchievementRewards.kt` + `ui/viewmodel/GameViewModel.kt`
**Severity**: MEDIUM
**Description**: Achievements are tracked and unlocked, but the rewards (gold, passive buffs, pet unlocks) from AchievementRewards are never granted.
**Fix**: Grant rewards when achievements are unlocked in the AchievementSystem callback.

## Gameplay Bugs

### BUG-004: MapHazard SLOW_ZONE permanently reduces speed
**File**: `game/hazard/MapHazard.kt`
**Severity**: HIGH
**Description**: `SLOW_ZONE` sets `playerComp.moveSpeed = (playerComp.moveSpeed * 0.7f).coerceAtLeast(0.3f)` every frame while player is in the zone. This permanently reduces speed because it's multiplicative — even after leaving the zone, the speed stays reduced.
**Fix**: Track original speed and restore it when player leaves the zone.

### BUG-007: DamageBoostComponent stacking issue
**File**: `game/engine/ecs/systems/LootBoxSystem.kt`
**Severity**: MEDIUM
**Description**: Multiple damage boosts stack additively (might += 0.5f each), but when they expire, they subtract 0.5f each. If the player's base might changed (e.g., from leveling), the subtraction can reduce might below the intended base value.
**Fix**: Track the player's base might at boost time and restore properly.

### BUG-008: Boss kill counter increments every frame
**File**: `ui/viewmodel/GameViewModel.kt` (line ~145)
**Severity**: MEDIUM
**Description**: `if (bossHpComp?.isDead == true) bossKillCount++` runs every frame while the boss is dead, not just once on death. The counter increments rapidly.
**Fix**: Use a flag or check transition from alive to dead, not just the dead state.

### BUG-009: runSaved not reset on continue
**File**: `ui/viewmodel/GameViewModel.kt`
**Severity**: LOW
**Description**: When `continueGame()` is called, `runSaved` is not reset to `false`. This can cause the next death/quit to not save properly.
**Fix**: Reset `runSaved = false` in `continueGame()`.

### BUG-010: Weapon tier upgrades cumulative without reset
**File**: `ui/viewmodel/GameViewModel.kt` → `applyWeaponTier()`
**Severity**: LOW
**Description**: `applyWeaponTier()` applies cumulative multipliers (e.g., `ws.baseDamage *= 1.2f` at tier 2) but doesn't reset to base values first. If called multiple times with the same tier, stats compound incorrectly.
**Fix**: Reset weapon state to base values before applying tier effects.
