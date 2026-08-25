## 2026-08-24 - Unified UI Component System
**Learning:** Replaced dozens of hardcoded, duplicate Compose Box-based buttons and cards with a shared `HordeUI.kt` component system. Localized string resources must be explicitly handled when wrapping generic components, as shown in ModeSelectScreen where `"← ${L("back")}"` was needed instead of defaulting to "Back".
**Action:** When replacing custom UI elements with unified components, double-check that no localization mapping (`L(key)`) is silently dropped.
## 2026-08-24 - Systemic UI Refactor
**Learning:** Hardcoded constraints (like `Modifier.fillMaxWidth(0.7f)`) in base UI components (`HordeButton`, `HordeSecondaryButton`) completely prevent them from being reused in responsive Flex layouts (like `Row` with `Modifier.weight()`).
**Action:** Always extract layout constraints out of the base component and into the call site so that the underlying widget remains layout-agnostic and reusable.
## 2024-05-24 - Layout Constraints in Shared Components
**Learning:** Hardcoding `Modifier.fillMaxWidth()` inside a shared component like `HordeCard` forces it to take up the full width of its parent. When used inside a grid or row (like in MapSelectScreen or Shop items), this breaks the intended dynamic layout and sizing.
**Action:** Always allow callers to pass layout constraints via the `modifier` parameter in shared components instead of hardcoding them inside the base component to preserve flexibility for complex layouts.
