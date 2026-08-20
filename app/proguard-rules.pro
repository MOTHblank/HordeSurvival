# Horde Survival ProGuard Rules

# Keep Room entities
-keep class com.hordesurvival.data.model.** { *; }
-keep class com.hordesurvival.game.component.** { *; }
-keep class com.hordesurvival.game.enemy.EnemyType { *; }
-keep class com.hordesurvival.game.weapon.** { *; }
-keep class com.hordesurvival.game.upgrade.** { *; }
-keep class com.hordesurvival.game.mode.** { *; }

# Keep Compose
-keep class androidx.compose.** { *; }

# Keep Kotlin coroutines
-keep class kotlinx.coroutines.** { *; }

# Keep Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep LibGDX
-keep class com.badlogic.gdx.** { *; }
-dontwarn com.badlogic.gdx.**

# Keep enum values
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
