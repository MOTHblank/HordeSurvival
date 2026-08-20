# 📋 Horde Survival — نیازمندی‌های محیط توسعه

**آخرین بروزرسانی**: ۱۶ آگوست ۲۰۲۶  
**نسخه پروژه: v1.2.5

---

## 🔧 نرم‌افزارهای مورد نیاز

### 1. JDK 17
```
نسخه: OpenJDK 17.0.2
دانلود: https://download.java.net/java/GA/jdk17.0.2/dfd4a8d0985749f896bed50d7138ee7f/8/GPL/openjdk-17.0.2_linux-x64_bin.tar.gz
نصب: extract به /home/work/dev-tools/jdk-17.0.2
```

### 2. Android SDK
```
نسخه SDK: 34 (Android 14)
ابزارها:
  - platforms;android-34
  - build-tools;34.0.0
  - platform-tools
نصب: sdkmanager --install "platforms;android-34" "build-tools;34.0.0" "platform-tools"
```

### 3. Gradle
```
نسخه: 8.4
دانلود: https://services.gradle.org/distributions/gradle-8.4-bin.zip
```

### 4. Android Studio (اختیاری — برای توسعه GUI)
```
نسخه: Hedgehog (2023.1.1) یا جدیدتر
لینک: https://developer.android.com/studio
```

---

## ⚙️ تنظیمات Gradle

### build.gradle.kts (root)
```kotlin
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
    id("com.google.devtools.ksp") version "1.9.20-1.0.14" apply false
}
```

### app/build.gradle.kts
```kotlin
android {
    namespace = "com.hordesurvival"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.hordesurvival"
        minSdk = 24
        targetSdk = 34
        versionCode = 3
        versionName = "1.2.2"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.5" }
}
```

### gradle.properties
```properties
org.gradle.jvmargs=-Xmx1536m -Xms256m
org.gradle.daemon=false
android.useAndroidX=true
kotlin.code.style=official
```

### local.properties (خودکار ساخته میشه)
```properties
sdk.dir=/path/to/android-sdk
```

---

## 📦 Dependency‌ها

### Core Android
- androidx.core:core-ktx:1.12.0
- androidx.lifecycle:lifecycle-runtime-ktx:2.6.2
- androidx.activity:activity-compose:1.8.1

### Compose
- androidx.compose:compose-bom:2023.10.01
- androidx.compose.ui:ui
- androidx.compose.ui:ui-graphics
- androidx.compose.material3:material3
- androidx.compose.material:material-icons-extended
- androidx.compose.animation:animation

### Navigation
- androidx.navigation:navigation-compose:2.7.5

### Database
- androidx.room:room-runtime:2.6.1
- androidx.room:room-ktx:2.6.1
- androidx.room:room-compiler:2.6.1 (KSP)

### Game Engine
- com.badlogicgames.gdx:gdx:1.12.1
- com.badlogicgames.gdx:gdx-backend-android:1.12.1
- com.badlogicgames.gdx:gdx-platform:1.12.1 (native libs)

### سایر
- org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3
- androidx.datastore:datastore-preferences:1.0.0
- com.google.code.gson:gson:2.10.1

---

## 🚀 دستورات سریع

### نصب محیط (Linux بدون root)
```bash
# 1. JDK 17
mkdir -p ~/dev-tools && cd ~/dev-tools
wget -q "https://download.java.net/java/GA/jdk17.0.2/dfd4a8d0985749f896bed50d7138ee7f/8/GPL/openjdk-17.0.2_linux-x64_bin.tar.gz" -O jdk17.tar.gz
tar xzf jdk17.tar.gz && rm jdk17.tar.gz

# 2. Android SDK
wget -q "https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip" -O cmdline-tools.zip
mkdir -p android-sdk/cmdline-tools
unzip -q cmdline-tools.zip -d android-sdk/cmdline-tools/
mv android-sdk/cmdline-tools/cmdline-tools android-sdk/cmdline-tools/latest
rm cmdline-tools.zip

# 3. SDK packages
export JAVA_HOME=~/dev-tools/jdk-17.0.2
export ANDROID_HOME=~/dev-tools/android-sdk
export PATH=$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$PATH
yes | sdkmanager --licenses > /dev/null 2>&1
sdkmanager "platforms;android-34" "build-tools;34.0.0" "platform-tools"

# 4. Platform fix (اگه android.jar ناقص بود)
cd /tmp
wget -q "https://dl.google.com/android/repository/platform-34-ext7_r02.zip" -O platform-34.zip
unzip -q -o platform-34.zip -d $ANDROID_HOME/platforms/
echo "AndroidVersion.ApiLevel=34" > $ANDROID_HOME/platforms/android-34/build.properties

# 5. Gradle
cd ~/dev-tools
wget -q "https://services.gradle.org/distributions/gradle-8.4-bin.zip" -O gradle-8.4.zip
unzip -q gradle-8.4.zip
```

### بیلد
```bash
export JAVA_HOME=~/dev-tools/jdk-17.0.2
export ANDROID_HOME=~/dev-tools/android-sdk
export PATH=$JAVA_HOME/bin:$PATH

cd HordeSurvival
echo "sdk.dir=$ANDROID_HOME" > local.properties
echo "org.gradle.jvmargs=-Xmx1536m -Xms256m" > gradle.properties
echo "org.gradle.daemon=false" >> gradle.properties
echo "android.useAndroidX=true" >> gradle.properties
echo "kotlin.code.style=official" >> gradle.properties

~/dev-tools/gradle-8.4/bin/gradle assembleDebug --no-daemon

# APK: app/build/outputs/apk/debug/app-debug.apk
```

---

## ⚠️ نکات مهم

1. **android.jar ناقص**: بعضی وقتا SDK platform 34 بدون `android.jar` نصب میشه. باید دانلود جداگانه و extract بشه.
2. **build.properties ناقص**: فایل `build.properties` ممکنه ساخته نشه. باید دستی ایجاد بشه.
3. **gradlew ناقص**: ممکنه `gradlew` و `gradle-wrapper.jar` در repo نباشن. باید ساخته بشن.
4. **حافظه**: بیلد حداقل 1.5GB RAM نیاز داره. `org.gradle.jvmargs=-Xmx1536m` تنظیم بشه.
5. **android.useAndroidX=true**: بدون این، بیلد fail میشه.

---

## 📊 خلاصه نسخه‌ها

| component | version |
|-----------|---------|
| Kotlin | 1.9.20 |
| AGP | 8.2.0 |
| KSP | 1.9.20-1.0.14 |
| Compose BOM | 2023.10.01 |
| Compose Compiler | 1.5.5 |
| Gradle | 8.4 |
| JDK | 17 |
| compileSdk | 34 |
| minSdk | 24 |
| targetSdk | 34 |
