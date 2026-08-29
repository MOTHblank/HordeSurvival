1. **Optimize `CollisionSystem.kt` & `WeaponSystem.kt` Iterator Allocations:**
   The `IntFloatMap.iterator()` call creates a new `IntFloatMap.Entries` or `IntFloatMap.Keys` object every frame.
   We can cache `val shieldHitCooldownsIter = shieldHitCooldowns.iterator()` and call `shieldHitCooldownsIter.reset()` at the start of the decay loop.
2. **Optimize `WaveManagerSystem.kt` & `XpDropSystem.kt` `listOf()` Allocations:**
   The `listOf()` creates a new `ArrayList` every time.
   - In `WaveManagerSystem.kt` -> `spawnMiniBoss()`: Move `listOf(EnemyType.ELITE_KNIGHT, EnemyType.TANK_GOLEM, EnemyType.GHOST)` to a static/companion or pre-allocated class field array: `private val miniBossTypes = arrayOf(EnemyType.ELITE_KNIGHT, EnemyType.TANK_GOLEM, EnemyType.GHOST)` and use `miniBossTypes.random()`.
   - In `XpDropSystem.kt` -> `spawnBossLootChest()`: Move `listOf(LootType.HEALTH, LootType.GOLD)` to a pre-allocated array `private val bossLootTypes = arrayOf(LootType.HEALTH, LootType.GOLD)` and use indexed `for (i in 0 until bossLootTypes.size)` instead of `for ((i, type) in types.withIndex())`.
3. **Execute pre-commit tests and check limits.**
4. **Submit PR.**

This is ONE performance improvement: eliminating per-frame/per-drop short-lived object allocations (`Iterator` and `List`) in systems (`CollisionSystem`, `WeaponSystem`, `WaveManagerSystem`, `XpDropSystem`) by caching iterators and lifting inline lists to static arrays, stopping GC pressure from autoboxing and collection instantiation.
