## 2024-05-24 - [Unified Settings UI Architecture]
**Learning:** The `SettingsScreen` and `PauseScreen` historically used completely divergent, custom-built grids and rows to display toggle and slider settings, leading to visual inconsistencies (missing descriptions, mismatched padding, raw Compose logic mixed with UI).
**Action:** Introduced `HordeToggleSetting`, `HordeSliderSetting`, and `HordeSelectorSetting` to `HordeUI.kt` to act as the single source of truth for configuration UI, and migrated both screens to use them.
