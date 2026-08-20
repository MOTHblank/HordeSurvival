# HordeSurvival — Build Environment Setup Guide

## پیش‌نیازها (Exact Versions)

| Component | Version | Download |
|---|---|---|
| JDK | 17.0.2 | `https://download.java.net/java/GA/jdk17.0.2/dfd4a8d0985749f896bed50d7138ee7f/8/GPL/openjdk-17.0.2_linux-x64_bin.tar.gz` |
| Android SDK Platform | 34 (ext7 r02) | `https://dl.google.com/android/repository/platform-34-ext7_r02.zip` |
| Android Build Tools | 34.0.0 | via sdkmanager |
| Android Platform Tools | latest | via sdkmanager |
| Android Cmdline Tools | 11076708 | `https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip` |
| Gradle | 8.4 | via wrapper (自动下载) |
| AGP | **8.2.2** | 项目 build.gradle.kts 中指定 |
| Kotlin | 1.9.20 | 项目 build.gradle.kts 中指定 |
| KSP | 1.9.20-1.0.14 | 项目 build.gradle.kts 中指定 |

## ⚠️ 已知坑点

### 1. android.jar 缺失
`sdkmanager install "platforms;android-34"` 有时候安装不完整，`android.jar` 不在预期位置。
**解法：** 直接下载 platform zip 然后手动 extract：
```bash
cd $ANDROID_HOME/platforms
wget -q "https://dl.google.com/android/repository/platform-34-ext7_r02.zip" -O platform-34.zip
unzip -q platform-34.zip
rm platform-34.zip
# 确认: ls android-34/android.jar
```

### 2. AGP 版本兼容性
AGP 8.2.0 + KSP 1.9.20-1.0.14 有 bug（`MissingValueException: Cannot query the value of this provider`）。
**解法：** 用 AGP **8.2.2**（项目 build.gradle.kts 里 `id("com.android.application") version "8.2.2"`）

### 3. 内存不足导致 OOM Kill
KSP + Kotlin 编译很吃内存。5.9GB 内存的机器上需要调优。
**解法：** gradle.properties 中设置：
```properties
org.gradle.jvmargs=-Xmx2500m -XX:MaxMetaspaceSize=384m -XX:+HeapDumpOnOutOfMemoryError -XX:+UseSerialGC -XX:ReservedCodeCacheSize=128m
org.gradle.daemon=false
org.gradle.parallel=false
kotlin.incremental=false
```

### 4. gradle-wrapper.jar
repo 里的 wrapper jar 是有效的（63KB），**不需要额外下载**。

### 5. android.useAndroidX
repo 的 gradle.properties 里**已经有** `android.useAndroidX=true`，不需要手动加。

## 🚀 一键安装脚本

```bash
#!/bin/bash
set -e

WORKDIR="/home/work"
REPO="https://github.com/niksiratforex-ux/HordeSurvival.git"
BRANCH="main"
JDK_URL="https://download.java.net/java/GA/jdk17.0.2/dfd4a8d0985749f896bed50d7138ee7f/8/GPL/openjdk-17.0.2_linux-x64_bin.tar.gz"
CMDTOOLS_URL="https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip"
PLATFORM_URL="https://dl.google.com/android/repository/platform-34-ext7_r02.zip"

export JAVA_HOME="$WORKDIR/jdk-17.0.2"
export ANDROID_HOME="$WORKDIR/android-sdk"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$PATH"

echo "=== [1/6] Clone repo ==="
cd "$WORKDIR/.openclaw/workspace"
git clone --branch "$BRANCH" "$REPO" 2>/dev/null || echo "Already cloned"
cd HordeSurvival

echo "=== [2/6] Install JDK 17 ==="
if [ ! -d "$JAVA_HOME" ]; then
  wget -q "$JDK_URL" -O /tmp/jdk17.tar.gz
  tar xzf /tmp/jdk17.tar.gz -C "$WORKDIR"
  rm /tmp/jdk17.tar.gz
fi
java -version 2>&1

echo "=== [3/6] Install Android SDK cmdline-tools ==="
if [ ! -d "$ANDROID_HOME/cmdline-tools/latest" ]; then
  mkdir -p "$ANDROID_HOME/cmdline-tools"
  wget -q "$CMDTOOLS_URL" -O /tmp/cmdline-tools.zip
  cd "$ANDROID_HOME/cmdline-tools"
  unzip -q /tmp/cmdline-tools.zip
  mv cmdline-tools latest
  rm /tmp/cmdline-tools.zip
fi

echo "=== [4/6] Install build-tools + platform-tools ==="
yes | sdkmanager --licenses >/dev/null 2>&1 || true
sdkmanager "build-tools;34.0.0" "platform-tools" 2>&1 | tail -3

echo "=== [5/6] Install Android platform 34 (direct download) ==="
if [ ! -f "$ANDROID_HOME/platforms/android-34/android.jar" ]; then
  mkdir -p "$ANDROID_HOME/platforms"
  wget -q "$PLATFORM_URL" -O /tmp/platform-34.zip
  cd "$ANDROID_HOME/platforms"
  unzip -q /tmp/platform-34.zip
  rm /tmp/platform-34.zip
fi
ls -lh "$ANDROID_HOME/platforms/android-34/android.jar"

echo "=== [6/6] Configure & Build ==="
cd "$WORKDIR/.openclaw/workspace/HordeSurvival"
echo "sdk.dir=$ANDROID_HOME" > local.properties

# gradle.properties tweaks for low-memory environments
cat > gradle.properties << 'EOF'
org.gradle.jvmargs=-Xmx2500m -XX:MaxMetaspaceSize=384m -XX:+HeapDumpOnOutOfMemoryError -XX:+UseSerialGC -XX:ReservedCodeCacheSize=128m
org.gradle.daemon=false
org.gradle.parallel=false
org.gradle.caching=true
kotlin.incremental=false
android.useAndroidX=true
android.enableJetifier=true
EOF

# Fix AGP version (8.2.0 has KSP bug)
sed -i 's/version "8.2.0"/version "8.2.2"/' build.gradle.kts

chmod +x gradlew
./gradlew assembleDebug --no-daemon --console=plain

echo "=== DONE ==="
ls -lh app/build/outputs/apk/debug/app-debug.apk
```

## 📁 项目结构概览

```
HordeSurvival/
├── app/src/main/java/com/hordesurvival/
│   ├── MainActivity.kt, SplashActivity.kt, HordeSurvivalApp.kt
│   ├── data/              # Room DB (AppDatabase, Daos, Models, GameSaveManager)
│   ├── game/
│   │   ├── achievement/   # AchievementRewards, AchievementSystem, AchievementProgress
│   │   ├── ads/           # AdManager
│   │   ├── audio/         # SoundManager
│   │   ├── billing/       # BillingManager
│   │   ├── blessing/      # BlessingSystem (10 blessings)
│   │   ├── character/     # CharacterAbilities
│   │   ├── combo/         # ComboVisual
│   │   ├── component/     # ECS Components
│   │   ├── enemy/         # EnemyType, EliteAbilities
│   │   ├── engine/        # GameEngine + ECS (Entity, Component, System)
│   │   │   └── ecs/systems/  # Collision, Combo, DamageNumber, Elite, EnemyAI,
│   │   │                      # LootBox, Orbit, WaveManager, Weapon, Achievement
│   │   ├── hazard/        # MapHazard (5 types)
│   │   ├── mode/          # GameModes (TowerDefense, BossRush, DailyChallenge, Quest)
│   │   ├── pet/           # CompanionPet (5 pets)
│   │   ├── prestige/      # PrestigeSystem (5 levels)
│   │   ├── relic/         # RelicSystem
│   │   ├── synergy/       # WeaponSynergy (8 combos)
│   │   └── upgrade/       # UpgradeSystem
│   └── ui/
│       ├── screens/       # game, menu, characterselect, tutorial, upgrades, settings
│       └── viewmodel/     # GameViewModel, MainViewModel
├── build.gradle.kts       # AGP 8.2.2, Kotlin 1.9.20, KSP 1.9.20-1.0.14
├── gradle.properties      # JVM args, AndroidX
└── debug.keystore         # Debug signing key
```

## 🔧 遇到的问题和解决路径

| # | 问题 | 尝试 | 结果 |
|---|---|---|---|
| 1 | 没有 JDK | 下载 OpenJDK 17.0.2 | ✅ |
| 2 | sdkmanager 安装 platform-34 不完整（缺 android.jar） | 直接下载 platform zip extract | ✅ |
| 3 | AGP 8.2.0 + KSP `MissingValueException` | 升级 AGP → 8.2.2 | ✅ |
| 4 | `android.useAndroidX=true` 缺失 | 检查发现 repo 已有 | ✅ 无需改 |
| 5 | gradle-wrapper.jar 为空 | 检查发现 63KB 有效 | ✅ 无需改 |
| 6 | KSP 编译 OOM (4GB heap) | 降到 2.5GB + SerialGC + 关 parallel | ✅ |
| 7 | AGP 8.1.4 找不到 android-34 | 因为 android.jar 缺失，修了 #2 后回到 8.2.2 | ✅ |
