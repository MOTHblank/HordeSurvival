## 2026-08-24 - Unified UI Component System
**Learning:** Replaced dozens of hardcoded, duplicate Compose Box-based buttons and cards with a shared `HordeUI.kt` component system. Localized string resources must be explicitly handled when wrapping generic components, as shown in ModeSelectScreen where `"← ${L("back")}"` was needed instead of defaulting to "Back".
**Action:** When replacing custom UI elements with unified components, double-check that no localization mapping (`L(key)`) is silently dropped.
