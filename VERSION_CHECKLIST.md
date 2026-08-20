# Version Checklist — v1.2.8.1

## Pre-Release
- [x] Update `versionCode` from 9 to 10 in `app/build.gradle.kts`
- [x] Update `versionName` from "1.2.8" to "1.2.8.1" in `app/build.gradle.kts`
- [x] Update README.md with v1.2.8.1 changelog
- [x] Update CHANGELOG.md
- [x] Build debug APK: `./gradlew assembleDebug`
- [x] Copy APK to `releases/` folder
- [x] Git commit + tag v1.2.8.1
- [x] Push to GitHub main branch
- [x] Create GitHub Release with APK attached

## Performance Improvements (v1.2.8.1)

- [x] **PERF-001**: Entity filter consolidation — replaced 4+ filter calls with single-pass categorization
- [x] **PERF-002**: Layer sort optimization — insertion sort (O(n) for nearly-sorted) instead of sortedBy (O(n log n))
- [x] **PERF-003**: Grid background — removed intersection dots (nested while loop eliminated)
- [x] **PERF-004**: Damage number string cache — cached formatted text, avoids String.format every frame
- [x] **PERF-005**: Enemy count tracking (TODO: incremental counter)

## Graphics Improvements (v1.2.8.1)

- [x] **GFX-001**: Death explosion effects — particles scale with enemy size, boss gets ring explosion
- [x] **GFX-002**: XP Magnet visual — magnetized gems glow blue, sparkle ring, magnet line to player
- [x] **GFX-003**: Low HP warning overlay — red vignette edges when HP < 30%, pulsing
- [x] **GFX-004**: Boss intro animation — orange screen flash + stronger shake on boss spawn
- [x] **GFX-005**: Combo visual enhancement — bounce scale, edge glow for high combos
- [x] **GFX-006**: Weapon trail effects — fireball (orange), ice (cyan), missile (blue), spear (gold) trails

## Post-Release
- [ ] Verify APK installs on device
- [ ] Test low HP warning overlay
- [ ] Test boss intro flash
- [ ] Test XP magnet visual
- [ ] Test weapon trails on all 8 weapons
