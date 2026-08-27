## 2026-08-24 - Unified UI Component System
**Learning:** Replaced dozens of hardcoded, duplicate Compose Box-based buttons and cards with a shared `HordeUI.kt` component system. Localized string resources must be explicitly handled when wrapping generic components, as shown in ModeSelectScreen where `"← ${L("back")}"` was needed instead of defaulting to "Back".
**Action:** When replacing custom UI elements with unified components, double-check that no localization mapping (`L(key)`) is silently dropped.
## 2026-08-24 - Systemic UI Refactor
**Learning:** Hardcoded constraints (like `Modifier.fillMaxWidth(0.7f)`) in base UI components (`HordeButton`, `HordeSecondaryButton`) completely prevent them from being reused in responsive Flex layouts (like `Row` with `Modifier.weight()`).
**Action:** Always extract layout constraints out of the base component and into the call site so that the underlying widget remains layout-agnostic and reusable.
## 2024-05-24 - Unifying the Card System
**Learning:** Migrating hardcoded `Box` card layouts to a unified `HordeItemCard` provides a consistent "juice" across the app by introducing scale-on-press animations, unified rounded corners, and easy visual selection states. `HordeScreen` was updated to support custom `contentAlignment` which made migrating everything much easier.
**Action:** When adding new screens, always use `HordeItemCard` for lists, settings rows, grids, and selections. Do not write custom `Box` components that mimic cards.
## 2026-08-25 - Hardcoded Shapes vs Shared Styles
**Learning:** Hardcoding shape configurations like `CutCornerShape(topStart = 8.dp...)` across multiple screens reduces maintainability, clutters code, and makes systemic style updates difficult.
**Action:** Always extract common visual motifs (like angular cuts or specific radii) into public, shared constants within `HordeUI.kt` (e.g., `CornerCutShape`, `SmallCutShape`) and reuse them across all screens.

## 2026-08-25 - Functional Information in Visual Redesigns
**Learning:** When decluttering UI (e.g., converting a verbose text-based weapon list into a sleek row of icons), it's easy to accidentally drop functional information (like the weapon name), which is a regression for the player.
**Action:** Find creative ways to retain vital text information in redesigned components, such as using a much smaller font size placed directly underneath or adjacent to the new visual elements.
## 2026-08-25 - Extracted Animated Background to HordeScreen
**Learning:** Extracting the animated orbs, particles, and dynamic gradient from `MainMenuScreen` into `HordeScreen` instantly propagates a high-quality, juicy background to all out-of-game UI screens (e.g. Mode Select, Settings, GameOver).
**Action:** When a visually pleasing and complex background pattern is found in a specific screen (like a main menu), consider pushing it down into the core shared UI components to elevate the visual baseline of the entire app.
## 2026-08-25 - Tradeoff: Global Animated Backgrounds
**Learning:** Extracting complex animated backgrounds into a shared root component (`HordeScreen`) applies multiple continuous `infiniteRepeatable` animations to every single screen in the app.
**Action:** While this greatly improves visual consistency and 'juice' across menus, it increases battery usage and baseline rendering cost. If frame drops are observed in heavy menus (like Upgrades or Map Select), the animations should be throttled or simplified based on the `graphicsQuality` setting.
## 2026-08-25 - Systemic Shape Unification
**Learning:** Found scattered usages of `RoundedCornerShape(8.dp)`, `RoundedCornerShape(12.dp)`, `CircleShape`, and hardcoded `CutCornerShape(...)` across the codebase (e.g., in `PauseScreen`, `GameScreen`, `HordeUI`, `MapSelectScreen`, `UpgradesScreen`, etc.). This violated the design system rules which dictated the use of the shared `CornerCutShape` and `SmallCutShape`.
**Action:** Replaced all hardcoded shape usages (`RoundedCornerShape`, `CircleShape` and specific `CutCornerShape(...)`) with `CornerCutShape` or `SmallCutShape` from `HordeUI.kt` to ensure consistent angular arcade aesthetic across the app.
## 2026-08-27 - Typography System & Continuous Background
**Learning:** Hardcoded text styles lead to massive inconsistency.  redrawing an animated background on every navigation caused jitter and restarted animations.
**Action:** Extract backgrounds to  behind the  for seamless looping across screen transitions, and strictly use  for all text to enforce consistency.
## 2026-08-27 - Typography System & Continuous Background
**Learning:** Hardcoded text styles lead to massive inconsistency. HordeScreen redrawing an animated background on every navigation caused jitter and restarted animations.
**Action:** Extract backgrounds to MainActivity behind the NavHost for seamless looping across screen transitions, and strictly use HordeTypography for all text to enforce consistency.
