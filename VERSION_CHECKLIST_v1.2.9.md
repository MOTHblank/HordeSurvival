# Version Checklist — v1.2.9

## Bug Fixes (v1.2.9)

- [x] **BUG-001**: Weapons stop firing after ~30 seconds — weapon entities recycled by stale cleanup
- [x] **BUG-002**: Tower Defense enemy count too high — need spawn rate cap
- [x] **BUG-003**: Score not auto-collecting in TD mode — onEnemyKilled not called for all kills

## Pre-Release

- [x] Update `versionCode` from 10 to 11 in `app/build.gradle.kts`
- [x] Update `versionName` from "1.2.8.1" to "1.2.9" in `app/build.gradle.kts`
- [x] Update README.md with v1.2.9 changelog
- [x] Update CHANGELOG.md
- [x] Build debug APK: `./gradlew assembleDebug`
- [x] Copy APK to `releases/` folder
- [x] Git commit + tag v1.2.9
- [x] Push to GitHub main branch
- [x] Create GitHub Release with APK attached

## Post-Release

- [ ] Verify APK installs on device
- [ ] Test weapons fire continuously for 5+ minutes
- [ ] Test Tower Defense enemy count is manageable
- [ ] Test TD score accumulates correctly on enemy kills
