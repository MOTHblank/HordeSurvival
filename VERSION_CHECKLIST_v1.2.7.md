# VERSION CHECKLIST — v1.2.7

## 📋 Version Bump Locations
- [ ] `app/build.gradle.kts` → `versionName = "1.2.7"`, `versionCode = 8`
- [ ] `README.md` → version badge / changelog
- [ ] GitHub Release → tag `v1.2.7`, attach APK
- [ ] CHANGELOG section in README or separate file

---

## 🐛 Bug Fixes (v1.2.7)

### 1. Continue After Death — باید از ادامه بیاد نه از اول
- [ ] بررسی Continue logic در GameViewModel / GameOverScreen
- [ ] ذخیره state قبل از مرگ (position, weapons, level, HP)
- [ ] Continue = بازگشت به همون state، فقط یکبار قابل استفاده
- [ ] Play Again = شروع کاملاً از نو
- [ ] تست: بمیر → Continue → باید ادامه باشه نه restart

### 2. Tower Defense Mode — بازطراحی کامل
- [ ] نمایش مرزهای چپ و راست (wall/boundary graphic)
- [ ] loot/xp خودکار به پایین بیوفته (auto-collect)
- [ ] سیستم مراحل (Stage 1 → Boss → Stage 2 → ...)
- [ ] فقط Stage 1 اول باز قفل، بقیه unlock بعد از رد کردن
- [ ] پایان مرحله: صفحه نتیجه با امتیازات
- [ ] انتخاب: مرحله بعد / تکرار / برگشت به منو
- [ ] تست: بازی TD → بکش → loot خودکار جمع بشه → boss بمیره → stage complete

### 3. Tutorial — بازطراحی
- [ ] بررسی TutorialScreen فعلی
- [ ] بروزرسانی متن‌ها و تصاویر
- [ ] پوشش سیستم‌های جدید (synergy, blessing, pet, prestige)
- [ ] راهنمای controls و هدف بازی

### 4. Performance Optimization — جلوگیری از crash
- [ ] Entity pool / object recycling برای جلوگیری از GC pressure
- [ ] محدود کردن تعداد entity همزمان روی صفحه
- [ ] کاهش particle effects روی low-end
- [ ] بررسی memory leaks در game loop
- [ ] تست با 1000+ entity بدون crash

---

## 🚀 Release Steps
- [ ] همه باگ‌ها فیکس شدن
- [ ] بیلد موفق (`./gradlew assembleDebug`)
- [ ] تست دستی روی دستگاه/emulator
- [ ] `versionCode` و `versionName` آپدیت شدن
- [ ] Git commit + push
- [ ] GitHub Release با tag `v1.2.7` + APK attachment
- [ ] README آپدیت شده
