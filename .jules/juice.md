## 2026-08-24 - Unified UI Component System
**Learning:** Replaced dozens of hardcoded, duplicate Compose Box-based buttons and cards with a shared `HordeUI.kt` component system. Localized string resources must be explicitly handled when wrapping generic components, as shown in ModeSelectScreen where `"← ${L("back")}"` was needed instead of defaulting to "Back".
**Action:** When replacing custom UI elements with unified components, double-check that no localization mapping (`L(key)`) is silently dropped.
## 2026-08-24 - Systemic UI Refactor
**Learning:** Hardcoded constraints (like `Modifier.fillMaxWidth(0.7f)`) in base UI components (`HordeButton`, `HordeSecondaryButton`) completely prevent them from being reused in responsive Flex layouts (like `Row` with `Modifier.weight()`).
**Action:** Always extract layout constraints out of the base component and into the call site so that the underlying widget remains layout-agnostic and reusable.
