# 🔍 Horde Survival — تحلیل جامع کد

**تاریخ**: ۱۶ آگوست ۲۰۲۶  
**نسخه فعلی**: v1.1.0  
**وضعیت**: ۴۵ فایل Kotlin، ~۵,۴۵۱ خط کد

---

## 🐛 باگ‌های شناسایی شده

### 🔴 باگ‌های بحرانی (باید فوری رفع شوند)

#### 1. Poison Cloud — رنگ اشتباه (تایپی)
**فایل**: `WeaponSystem.kt` → `spawnPoisonCloud()`
```kotlin
// ❌ فعلی — یک رقم کمه
color = 0xAAAE6BA.toInt()
// ✅ باید این باشه
color = 0xFFAAE6BA.toInt()
```
**اثر**: Poison Cloud با رنگ نادرست رندر میشه (خاکستری به‌جای سبز)

#### 2. Boss spawn callback — دو بار صدا زده میشه
**فایل**: `WaveManagerSystem.kt` → `spawnBoss()`
```kotlin
private fun spawnBoss(playerPos: TransformComponent) {
    spawnEnemy(EnemyType.BOSS, playerPos)
    onBossSpawned?.invoke()  // ❌ اینجا صدا زده میشه
}
```
و در `update()`:
```kotlin
if (playerLevel >= lastBossLevel + 50 && playerLevel > 0) {
    lastBossLevel = (playerLevel / 50) * 50
    spawnBoss(playerPos)
    onBossSpawned?.invoke()  // ❌ و اینجا هم! دوبار صدا زده میشه
}
```
**اثر**: Boss Warning دو بار نشون داده میشه

#### 3. Entity cleanup — Weapon entities هرگز پاک نمیشن
**فایل**: `GameEngine.kt` → `update()`
```kotlin
entities.removeAll { !it.active && it.tag != "player" }
```
Weapon entities با tag = "player" ساخته میشن، پس هیچوقت از لیست حذف نمیشن.
**اثر**: Memory leak — تعداد entities به‌مرور زمان زیاد میشه

#### 4. Boomerang return — منطق ناقص
**فایل**: `ProjectileSystem.kt`
```kotlin
if (proj.returnsToPlayer && proj.maxDistance > 0f) {
    proj.distanceTraveled += proj.speed * dt
    if (proj.distanceTraveled >= proj.maxDistance) {
        proj.returnsToPlayer = false  // ❌ غیرفعال میشه
        proj.isHoming = true
        proj.targetId = player?.id ?: -1
    }
}
```
وقتی `returnsToPlayer` غیرفعال میشه، `isHoming` فعال میشه اما `targetId` باید `player.id` باشه نه `-1`. اگه player null باشه، boomerang گم میشه.

### 🟡 باگ‌های متوسط

#### 5. Lightning Ring — بدون cooldown بین ضربات
**فایل**: `WeaponSystem.kt` → `fireLightningRing()`
هر بار که weapon fire میشه، **تمام** دشمنان در شعاع آسیب می‌بینن. هیچ per-enemy cooldown‌ای وجود نداره. با cooldown پایین weapon، این خیلی OP هست.

#### 6. Orbit Shield damage — ثابت و بدون scale
**فایل**: `CollisionSystem.kt`
```kotlin
eHealth.takeDamage(10f)  // ❌ همیشه ۱۰ — باید از weapon damage استفاده بشه
sHealth.takeDamage(3f)   // ❌ ثابت
```
Shield damage باید با `player.might` و weapon tier scale بشه.

#### 7. XP Gem color — ناسازگار با تم
**فایل**: `XpDropSystem.kt` → `getGemColor()`
رنگ‌های قرمز برای XP gems برگردونده، در حالی که تم بازی mint green (`#AAE6BA`) برای XP هست.

#### 8. Difficulty scaling — ناسازگاری بین README و کد
- README: "Boss every 5 minutes"
- کد: Boss every 50 player levels
- README: "Quadratic difficulty"
- کد: `sqrt()` scaling (نه quadratic)

#### 9. Player invincibility — هم‌زمانی دو مقدار
**فایل**: `CollisionSystem.kt`
```kotlin
playerHealth.invincibleTimer = 0.3f  // ثابت
```
ولی در Constants:
```kotlin
const val PLAYER_INVINCIBILITY_TIME = 0.5f  // تعریف شده ولی استفاده نمیشه
```

### 🟢 باگ‌های جزئی

#### 10. `GXP_MAGNET_SPEED` — تعریف شده ولی استفاده نمیشه
در `Constants.kt` مقدار `500f` تعریف شده ولی در `CollisionSystem.kt` مقدار ثابت `500f` استفاده میشه.

#### 11. Enemy slow — timer reset بدون check
در `CollisionSystem.kt`، slow effect هر بار reset میشه حتی اگه slow جدید ضعیف‌تر از فعلی باشه.

#### 12. Healer AI — heal amount ثابت
مقدار heal (`5f`) ثابت هست و با difficulty scale نمیشه.

---

## ✅ نقاط قوت

### 🏗️ معماری
- **ECS pattern تمیز**: جداسازی خوب Component، Entity، System
- **MVVM**: ViewModel کاملاً از UI جدا شده
- **Crash handling**: try-catch دور کل game loop و sound system
- **Object pooling**: برای particles تعریف شده

### 🎮 گیم‌پلی
- **۸ اسلحه منحصربفرد**: هر کدام مکانیک متفاوت دارن
- **۵ سطح ارتقا per weapon**: با افکت‌های خاص
- **۱۰ نوع دشمن**: با رفتارهای متنوع (healer, ghost, splitter)
- **سیستم ارتقا عمیق**: weapons + passives + meta progression

### 🎨 بصری
- **رندرر Canvas-based**: بدون نیاز به asset خارجی
- **شکل‌های متمایز**: هر دشمن شکل خاص خودش رو داره
- **افکت‌های نوری**: glow, pulse, particle burst
- **پالت رنگ آرام‌بخش**: pastel theme عالی

### 🔧 فنی
- **Localization**: ۶ زبان پشتیبانی میشه
- **Room Database**: برای ذخیره‌سازی
- **DataStore**: برای تنظیمات
- **ToneGenerator**: صدا بدون asset خارجی

---

## ⚠️ نقاط ضعف

### عملکرد
- **Entity cleanup ناکارآمد**: `removeAll` هر فریم روی کل لیست
- **`getActiveEntities()` هر فریم**: لیست جدید ساخته میشه
- **بدون spatial partitioning**: collision detection O(n²)
- **Canvas rendering**: برای تعداد بالا کند میشه

### صدا
- **بدون موسیقی پس‌زمینه**: فقط sound effects
- **ToneGenerator محدود**: صداهای ساده و تکراری
- **صدای یکسان**: بیشتر اسلحه‌ها صدای مشابه دارن

### گیم‌پلی
- **Quest Mode خالی**: فقط enum تعریف شده
- **Daily Challenge خالی**: فقط enum
- **بدون Tutorial**: بازیکن جدید گم میشه
- **بدون save/load**: هر run باید کامل بشه
- **Weapon evolution نداره**: در tier 5 تغییر شکل نمیده

### بصری
- **Damage numbers = particles**: عدد واقعی نشون داده نمیشه
- **بدون screen shake**: موقع ضربه لرزش نداره
- **بدون projectile trail**: اثر دنباله‌دار نداره
- **بدون انیمیشن کاراکتر**: فقط دایره ثابت

---

## 🚀 قابلیت‌های پیشنهادی برای آپدیت

### اولویت بالا (فوری)
1. **رفع باگ‌های بحرانی** (بالا)
2. **Damage numbers واقعی** — Canvas text rendering
3. **Screen shake** — لرزش صفحه موقع ضربه
4. **Projectile trail** — اثر دنباله‌دار برای پرتابه‌ها
5. **موسیقی پس‌زمینه** — حداقل یه loop ساده

### اولویت متوسط
6. **Weapon evolution** — تغییر شکل در tier 5
7. **ترکیب اسلحه + passive** — مثل Vampire Survivors
8. **Quest Mode** — ۱۰ مرحله با objective
9. **Spatial partitioning** — Grid-based collision برای performance
10. **Save/Load** — ذخیره mid-run

### اولویت پایین
11. **Daily Challenge** — modifier روزانه
12. **Leaderboard** — رتبه‌بندی
13. **Tutorial** — راهنمای اول بازی
14. **انیمیشن کاراکتر** — sprite sheet
15. **صداهای متنوع** — هر اسلحه صدای منحصربفرد
16. **Target SDK 35** — آپدیت
17. **Settings ذخیره‌سازی زبان** — باید زبان ذخیره بشه

---

## 📋 خلاصه آماری

| موضوع | تعداد |
|--------|-------|
| باگ بحرانی | ۴ |
| باگ متوسط | ۵ |
| باگ جزئی | ۳ |
| نقطه قوت | ۱۲+ |
| نقطه ضعف | ۱۰ |
| قابلیت پیشنهادی | ۱۷ |

---

*تحلیل شده در ۱۶ آگوست ۲۰۲۶*
