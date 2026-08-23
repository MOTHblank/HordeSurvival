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
