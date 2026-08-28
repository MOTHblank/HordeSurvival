## 2026-03-31 - Vector2 Allocation Hotspots in Particle/Drop Helpers

**Learning:** `GameMath.randomPointInCircle()` and `GameMath.randomPointOnCircle()` were instantiating a `new Vector2()` on every call. Because these functions were invoked inside `repeat(...)` loops for particles, trail effects, XP gems, and death explosions across multiple ECS systems (`CollisionSystem`, `ProjectileSystem`, `XpDropSystem`, `WeaponSystem`), hundreds of short-lived `Vector2` instances were allocated per second during active gameplay, leading to severe GC pressure on mobile devices.

**Action:** Always provide an optional target parameter (e.g., `out: Vector2 = Vector2()`) for math and utility functions returning vectors or geometry. System classes in hot loops should maintain a class-level `tempVec2` field to pass as the `out` argument.

## 2026-03-31 - Spatial Grid Partitioning & Zero-Allocation Render Loop

**Learning:** Unindexed linear collision checks in `CollisionSystem` ($O(P \times E)$) and per-frame list filtering/sorting in `GameRenderer` were causing severe frame-time spikes during high entity counts (100+ projectiles vs 300+ enemies).
When querying spatial structures for collisions:
1. Rebuild or sync spatial grid cells *after* entity movements occur during the frame so spatial queries reflect current entity coordinates.
2. Query radii must account for maximum target entity collision bounds (e.g. `queryRadius = projRadius + MAX_ENEMY_RADIUS`) so large enemies/bosses are not filtered out prematurely.

**Action:** Use `SpatialGrid` for localized spatial range queries and maintain reusable scratch lists for Compose Canvas sorting/culling to ensure zero garbage collection churn during rendering.

## 2026-03-31 - ECS Component Bitmasking & Zero-Allocation Render Loop

**Learning:** Storing ECS components in per-entity `HashMap<Class<out Component>, Component>` maps caused millions of `HashMap` lookups per second across system loops (~75,000/frame with 500 entities x 15 queries x 10 systems). Recycling entities dropped Map `Node` objects into heap memory, triggering GC pauses. Additionally, `List.sortBy` in render loops allocated lambda closures and `Float` wrapper objects every frame, while `Pair` keys in Compose text caches generated hundreds of short-lived key objects per second.

**Action:** Store ECS components in a direct-indexed sparse array `Array<Component?>(64)` paired with a primitive `Long` mask (`componentMask`). Map component classes to indices (0..63) via a thread-safe `ComponentRegistry`. Use in-place primitive partial selection sort for culling visible entities and composite primitive `Long` keys `(hash shl 32) or fontPx` for text layout caching to achieve zero GC allocations per frame during rendering.

## 2026-03-31 - Zero-Allocation Canvas Path Geometry & O(1) Targeted Entity Lookup

**Learning:** Allocating `Path()` inside Compose Canvas draw functions (`drawTriangle`, `drawDiamond`, `drawStar`, `drawPolygon`, `drawHeart`, and tiled background renderers like Persian/Roman/Egyptian) instantiates thousands of short-lived C++/JVM path objects per frame during active combat. Furthermore, searching active entity lists linearly ($O(N)$) for homing target IDs in `ProjectileSystem` caused frame-time spikes when dozens of homing missiles were in flight.

**Action:** Pre-allocate composable scratch `Path` instances (`remember { Path() }`) and reset them in-place (`path.reset()`) for all custom geometry rendering. Maintain an $O(1)$ ID lookup map (`entityByIdMap`) in `GameEngine` for direct entity retrieval by ID, and bound text layout caches to prevent memory accumulation.
## 2024-05-19 - Removed Autoboxing overhead in CollisionSystem
**Learning:** `mutableMapOf<Int, Float>` causes autoboxing and unboxing, generating unnecessary GC pressure in the hot `update` loop.
**Action:** Always prefer LibGDX's primitive collections like `IntFloatMap`, `Array`, or `FloatArray` when working with primitive types (Int, Float, Boolean) in hot paths to avoid autoboxing overhead.

## 2024-05-19 - Removed Iterator Allocations in Hot Paths
**Learning:** Standard `for (entity in entities)` loops implicitly allocate an `Iterator` object per iteration, producing hundreds of GC allocations per frame across ECS systems. Additionally, `entities.find { it.tag == "player" }` executes an O(N) lookup and allocates a lambda closure every frame.
**Action:** Always use indexed loops (`for (i in 0 until entities.size)`) when iterating through `Array` or `List` structures in `update()` loops, and use the cached `engine.playerEntity` to locate the player instead of searching the entity list.
## $(date +%Y-%m-%d) - [Indexed loops over Collection functions in systems]
**Learning:** `RelicSystem` and `AchievementSystem` were using Kotlin collection functions like `count {}` and `any {}` which allocate an `Iterator` and lambda per frame, creating GC pressure that hurts mobile performance. Also, the active `entities` list passed to `System.update()` only contains *active* entities, meaning it missed inactive/dead entities (like bosses that were just killed).
**Action:** Replace `count {}` and `any {}` with indexed `for` loops (e.g. `for (i in 0 until entities.size)`) in hot loop `System.update()` methods. When checking for dead entities, iterate over `engine.entities` instead of the passed `entities` parameter to reliably catch them.
## 2024-11-20 - [ECS Entity Recycling and Entity Count Optimization]
**Learning:** `ArrayList` iterator removal inside a loop (like `GameEngine.recycleEntities()`) creates O(N^2) shifting behavior and generates per-frame `Iterator` allocations leading to GC pressure. Calling `GameEngine.getEntityCount()` iterating the list every time when thousands of projectiles want to check if they can spawn particles causes O(N*M) loop iterations per frame where N is number of total entities and M is the number of projectiles.
**Action:** Use an O(N) two-pointer (write index) approach for in-place array compaction when repeatedly removing from a large ArrayList during hot paths. Replace iterating loops with O(1) checks against existing cached values (like `_activeEntitiesCache.size`) for global states.

## 2024-05-20 - Removed Autoboxing overhead in ComboSystem
**Learning:** `mutableSetOf<Int>()` autoboxes `Int` to `Integer` on every insertion. In a hot path like `ComboSystem` checking deaths per frame, this allocates `Integer` instances repeatedly and creates GC churn.
**Action:** Always prefer LibGDX's primitive collections like `IntSet` over `mutableSetOf<Int>()` when tracking primitive IDs (like entity IDs) to prevent autoboxing overhead.

## 2026-03-31 - Reset Delta-Time Timestamp on Unpause & Zero-Allocation Enemy Counter

**Learning:** When resuming gameplay after an in-game pause or level-up screen selection, `lastUpdateTimeNs` in `GameViewModel` was retaining the timestamp from before the pause. On the first frame after unpausing, the computed delta time `(System.nanoTime() - lastUpdateTimeNs)` spiked to seconds/minutes (capped at `0.1s`), causing a 100ms delta-step frame burst that resulted in freezing and dropped frame rates down to 0 FPS. Additionally, `engine.getActiveEntities().count { it.tag == "enemy" }` was allocating lambda objects and list iterators every frame.

**Action:** Always reset `lastUpdateTimeNs = System.nanoTime()` in all unpause / state resume methods (such as `selectUpgrade()`). Replace `.count {}` with an indexed loop (`for (i in 0 until activeEntities.size)`) over `engine.cachedActiveEntities` to eliminate GC allocation churn in the core update loop.
## 2026-08-28 - Zero-Allocation Partial Selection Sort for Target Queries
**Learning:** Querying the spatial grid and then using Kotlin's `.sortBy { e -> distSq }` and `.take(count)` to find the nearest N enemies (e.g., for Magic Missile) allocates lambda closures, autoboxes primitive `Float` distances, and creates new `List` instances every frame/shot, severely contributing to GC churn in hot combat loops.
**Action:** Always use primitive, in-place algorithms like partial selection sort within a reusable `MutableList` when querying for the 'top K' elements by distance. Limit list consumption directly by index to avoid intermediate collections.
