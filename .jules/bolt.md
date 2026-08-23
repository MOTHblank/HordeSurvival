## 2026-03-31 - Vector2 Allocation Hotspots in Particle/Drop Helpers

**Learning:** `GameMath.randomPointInCircle()` and `GameMath.randomPointOnCircle()` were instantiating a `new Vector2()` on every call. Because these functions were invoked inside `repeat(...)` loops for particles, trail effects, XP gems, and death explosions across multiple ECS systems (`CollisionSystem`, `ProjectileSystem`, `XpDropSystem`, `WeaponSystem`), hundreds of short-lived `Vector2` instances were allocated per second during active gameplay, leading to severe GC pressure on mobile devices.

**Action:** Always provide an optional target parameter (e.g., `out: Vector2 = Vector2()`) for math and utility functions returning vectors or geometry. System classes in hot loops should maintain a class-level `tempVec2` field to pass as the `out` argument.
