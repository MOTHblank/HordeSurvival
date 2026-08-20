# Horde Survival — میراث توسعه برای MimoCode

## وضعیت فعلی: v1.2.0 (آگوست ۲۰۲۶)

## ✅ ویژگی‌های جدید v1.2.0

### ✨ Weapon Evolution (ترکیب اسلحه + passive)
- [x] ۸ evolution: Holy Bible, Hellfire, Blizzard, Thunderstorm, Plague, Megaboom, Aurora, Judgment
- [x] هر evolution = اسلحه tier 5 + passive max level
- [x] UpgradeManager پیشنهاد evolution با rarity LEGENDARY میده
- [x] damage/cooldown multiplier + special effect

### 💥 Floating Damage Numbers
- [x] عدد آسیب واقعی (نه particle burst)
- [x] رنگ طلایی برای crit، سفید برای معمولی
- [x] Shadow + outline برای خوانایی
- [x] فرمت k برای اعداد بزرگ (1k, 2k)
- [x] بالا رفتن + محو شدن

### 📳 Screen Shake
- [x] لرزش صفحه موقع ضربه خوردن بازیکن (intensity=6)
- [x] لرزش شدید موقع spawn شدن Boss (intensity=12)
- [x] Decay نرم (سینوسی)

### 🌟 Projectile Trail
- [x] اثر دنباله‌دار برای تمام پرتابه‌ها
- [x] رنگ متناسب با نوع اسلحه
- [x] هر 30ms یه particle trail
- [x] محو شدن + کوچک شدن

### 🔧 باگ‌های رفع شده
- [x] Crash شلوغی (SoundManager)
- [x] Memory leak (weapon entities)
- [x] Poison Cloud color
- [x] Boss double callback
- [x] Boomerang return bug
- [x] Lightning Ring OP (per-enemy cooldown)
- [x] Shield damage static (scale با might)
- [x] XP Gem color (mint green)
- [x] Invincibility constant
- [x] Enemy slow reset
- [x] Healer static heal

این پروژه یک بازی کامل Android در سبک **Vampire Survivors** است که با **Kotlin + Jetpack Compose** ساخته شده.
برای ادامه توسعه، این فایل زیپ را همراه این README به MimoCode بدهید.

---

## ✅ آنچه انجام شده

### هسته بازی (Game Engine)
- [x] ECS Engine کامل (Entity-Component-System)
- [x] حلقه بازی frame-synced با زمان واقعی (System.nanoTime)
- [x] سیستم ذخیره‌سازی Room Database
- [x] مدیریت موجودیت‌ها با object pooling و cleanup خودکار

### سیستم حرکت و کنترل
- [x] جوی‌استیک مجازی با dead zone
- [x] دنیای نامحدود (بدون boundary)
- [x] دوربین دنبال‌کننده بازیکن

### اسلحه‌ها (۸ عدد، هر کدام ۵ سطح ارتقا)
- [x] Magic Missile (ردیاب)
- [x] Lightning Ring (دایره‌ای)
- [x] Fireball (انفجاری + سوختن)
- [x] Ice Shard (نافذ + کندکننده)
- [x] Poison Cloud (منطقه‌ای)
- [x] Boomerang Dagger (برگشتی)
- [x] Orbiting Shield (چرخشی)
- [x] Divine Spear (برد بلند + crit)

### دشمنان (۱۰ نوع)
- [x] Basic Drone, Flying Wisp, Tank Golem, Shooter Turret
- [x] Swarm Bat, Elite Knight, Ghost, Splitter, Healer
- [x] Boss (هر ۵۰ لول)
- [x] Elite enemy (هر ۱۰ لول، طلایی)

### سیستم ارتقا
- [x] ارتقای درون‌بازی: ۳ انتخاب تصادفی هر لول
- [x] ۱۲ ارتقای passive (Spinach, Tome, Crown, Wings, Duplicator, Shield, Heart, Clover, Magnet, Growth, Speedster, Vampire)
- [x] ارتقای دائمی: HP, Gold, Might, Cooldown, Speed, Luck
- [x] سیستم طلا با کسر واقعی

### کاراکترها (۱۰ عدد)
- [x] Mage, Paladin, Rogue, Alchemist, Archmage
- [x] Pyromancer, Frost Mage, Storm Caller, Assassin, Necromancer
- [x] هر کدام اسلحه و رنگ منحصربفرد
- [x] قفل/بازکردن با طلا

### UI/UX
- [x] منوی اصلی با انیمیشن
- [x] انتخاب حالت بازی
- [x] انتخاب کاراکتر
- [x] HUD درون‌بازی (HP, XP, طلا, زمان, اسلحه‌ها)
- [x] صفحه Level Up با کارت‌های متحرک
- [x] صفحه Game Over با آمار
- [x] صفحه ارتقای دائمی
- [x] صفحه تنظیمات

### چندزبانه (۶ زبان)
- [x] English, فارسی, 中文, 日本語, 한국어, Español
- [x] سیستم Locales.kt

### صدا
- [x] SoundManager با ToneGenerator
- [x] صدای شلیک، ضربه، جمع‌آوری، level up، آسیب، مرگ، boss، game over

### بصری
- [x] پس‌زمینه شطرنجی متحرک با ستاره
- [x] شکل‌های متمایز هر نوع دشمن
- [x] نوار HP دشمنان
- [x] افکت ذرات (particle system)
- [x] جوی‌استیک مجازی

### پایداری
- [x] try-catch دور کل حلقه بازی
- [x] صدا هیچوقت کرش نمیکنه
- [x] global crash handler
- [x] SoundManager thread-safe با queue و throttle (رفع crash شلوغی)
- [x] Entity memory leak رفع شد (weapon entities پاک میشن)
- [x] Particle cap (حداکثر ۳۰۰) برای جلوگیری از سرریز
- [x] GC pressure کاهش یافت (cached lists, no per-frame allocation)

---

## 🐛 باگ‌های رفع شده (v1.1.1 — ۱۶ آگوست ۲۰۲۶)

### بحرانی
- [x] **Crash شلوغی**: SoundManager هر صدا یک coroutine میساخت → صدها coroutine همزمان → ToneGenerator crash. رفع شد: single-thread queue + throttle (حداکثر ۱۵ صدا/ثانیه)
- [x] **Memory leak**: Weapon entities با tag="player" هیچوقت پاک نمیشن. رفع شد: cleanup بر اساس PlayerComponent
- [x] **Poison Cloud رنگ**: `0xAAAE6BA` → `0xFFAAE6BA`
- [x] **Boss double callback**: `onBossSpawned` دوبار صدا زده میشد
- [x] **Boomerang گم میشد**: اگه player null بود، targetId=-1 میشد. رفع شد: destroy projectile

### متوسط
- [x] **Lightning Ring OP**: بدون per-enemy cooldown → instant kill گروهی. رفع شد: 0.3s cooldown per enemy
- [x] **Orbit Shield damage ثابت**: `10f` بدون scale. رفع شد: `10f * player.might`
- [x] **XP Gem رنگ قرمز**: باید سبز باشه. رفع شد: `0xFFAAE6BA` (mint green)
- [x] **Player invincibility**: hardcoded `0.3f`. رفع شد: استفاده از `Constants.PLAYER_INVINCIBILITY_TIME`

### جزئی
- [x] **GXP_MAGNET_SPEED unused**: hardcoded `500f`. رفع شد: استفاده از constant
- [x] **Enemy slow reset**: حتی slow ضعیف‌تر reset میکرد. رفع شد: فقط اگه قوی‌تر باشه
- [x] **Healer heal ثابت**: `5f` بدون scale. رفع شد: `5% maxHp + 0.5% per minute`

### بهینه‌سازی
- [x] GameEngine: cached active entities list (بدون allocation هر فریم)
- [x] CollisionSystem: single-pass entity categorization (۵ لیست جدید → ۰)
- [x] Distance checks: squared distance به‌جای sqrt
- [x] ParticleSystem: MAX_PARTICLES = 300 cap

## 🚀 آنچه باید انجام شود (برای ادامه)

### اولویت بالا
- [ ] **ادامه موسیقی پس‌زمینه** — فعلی فقط صدای افکت داره، موسیقی ambient نداره
- [ ] **بهبود رندرر** — فعلی Canvas-based هست، میشه از LibGDX یا Android Canvas bitmap استفاده کرد
- [ ] **Particle trail برای پرتابه‌ها** — اثر دنباله‌دار
- [ ] **Screen shake** — لرزش صفحه موقع ضربه
- [ ] **Damage numbers واقعی** — عدد آسیب روی دشمن (فعلاً particle burst هست)

### اولویت متوسط
- [ ] **حالت Quest Mode** — ۱۰ مرحله با objective مشخص (فعلاً فقط تعریف شده، منطق بازی نداره)
- [ ] **Daily Challenge** — modifier روزانه (فعلاً فقط enum تعریف شده)
- [ ] **Leaderboard** — رتبه‌بندی جهانی
- [ ] **ترکیب اسلحه‌ها** — مثل Vampire Survivors: اسلحه + passive = اسلحه نهایی
- [ ] **Max weapon evolution** — تغییر شکل و قدرت ویژه در tier 5

### اولویت پایین
- [ ] **انیمیشن کاراکتر** — sprite sheet یا skeletal animation
- [ ] **صداهای متنوع‌تر** — هر اسلحه صدای منحصربفرد
- [ ] **Tutorial** — راهنمای اول بازی
- [ ] **Settings ذخیره‌سازی زبان** — باید زبان ذخیره بشه و موقع شروع اعمال بشه
- [ ] **بازیافت Entity** — object pooling واقعی برای performance
- [ ] **Target SDK 35** — آپدیت به آخرین SDK

---

## 📁 ساختار پروژه

```
HordeSurvival/
├── app/src/main/java/com/hordesurvival/
│   ├── HordeSurvivalApp.kt          # Application class
│   ├── MainActivity.kt               # Navigation + entry point
│   ├── data/
│   │   ├── database/AppDatabase.kt   # Room DB
│   │   ├── database/Daos.kt          # Data Access Objects
│   │   ├── model/Models.kt           # Entity models
│   │   └── repository/GameRepository.kt
│   ├── game/
│   │   ├── audio/SoundManager.kt     # ToneGenerator sound
│   │   ├── component/Components.kt   # ECS components
│   │   ├── enemy/EnemyType.kt        # 10 enemy types
│   │   ├── engine/
│   │   │   ├── GameEngine.kt         # Core ECS world
│   │   │   └── ecs/systems/          # 10 game systems
│   │   ├── mode/GameMode.kt          # Game modes
│   │   ├── upgrade/                  # Passives + upgrade manager
│   │   └── weapon/WeaponType.kt      # 8 weapons
│   ├── ui/
│   │   ├── Locales.kt                # 6-language system
│   │   ├── theme/Theme.kt            # Color palette
│   │   ├── screens/                  # All UI screens
│   │   └── viewmodel/                # ViewModels
│   └── utils/                        # Constants, math, pool
├── app/src/main/res/                 # Resources
├── app/build.gradle.kts              # Dependencies
├── settings.gradle.kts
└── build.gradle.kts
```

## 🔧 نحوه ادامه کار

1. فایل زیپ را extract کنید
2. در Android Studio باز کنید
3. Gradle sync کنید
4. این README را به MimoCode بدهید تا بداند چه چیزی ساخته شده
5. درخواست‌های جدیدتان را بگویید

## 📊 آمار پروژه
- **۴۵ فایل Kotlin**
- **۵,۴۵۱ خط کد**
- **زمان توسعه**: ~۶ ساعت (آگوست ۲۰۲۶)
- **نسخه**: v1.1.0
