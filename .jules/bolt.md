## 2024-05-15 - Eliminate ECS Scratch Buffer GC Pressure
**Learning:** In a Kotlin/LibGDX ECS architecture, spatial queries and collision checks executed every frame using `mutableListOf<Entity>` as scratch buffers allocate memory under the hood (e.g. iterators or backing array resizing if not careful), causing GC micro-stutters.
**Action:** Always use libGDX's primitive-friendly, allocation-free `com.badlogic.gdx.utils.Array<T>` instead of Kotlin's standard boxed collections for any list that is cleared and re-populated every frame in an `update()` loop.
