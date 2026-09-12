package com.hordesurvival.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.hordesurvival.data.model.PlayerSave
import com.hordesurvival.data.model.RunRecord
import com.hordesurvival.data.model.UnlockedCharacter

/**
 * Room database for game persistence.
 * Stores meta-progression, run history, and unlocks.
 */
@Database(
    entities = [PlayerSave::class, RunRecord::class, UnlockedCharacter::class],
    version = 5,  // CHANGED: 4 -> 5 (new player_save column: unlockedMaps)
    exportSchema = false
)
@TypeConverters(Converters::class)  // CHANGED (NEW): Set<String> <-> CSV TEXT for unlockedMaps
abstract class AppDatabase : RoomDatabase() {

    abstract fun playerDao(): PlayerDao
    abstract fun runDao(): RunDao
    abstract fun characterDao(): CharacterDao

    companion object {
        // CHANGED (NEW): real migration instead of relying on
        // fallbackToDestructiveMigration, which would have WIPED all player data
        // (gold, meta levels, characters, run history) on the version bump that
        // adding unlockedMaps forces. Default '' parses to an empty set — free
        // maps are playable via unlockCost == 0 regardless.
        // (Level gating needs NO column — PlayerSave.bestLevel already exists
        // and is maintained by recordRun.)
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE player_save ADD COLUMN unlockedMaps TEXT NOT NULL DEFAULT ''")
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "horde_survival_db"
                )
                .addMigrations(MIGRATION_4_5)
                // CHANGED: kept as an emergency net, but consider removing it once
                // migrations are routine — a future forgotten migration would silently
                // wipe saves instead of failing loudly during development.
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

/** CHANGED (NEW): Room converter for the unlockedMaps set. Map ids contain no commas. */
class Converters {
    @TypeConverter
    fun fromStringSet(set: Set<String>): String =
        if (set.isEmpty()) "" else set.joinToString(",")

    @TypeConverter
    fun toStringSet(value: String): Set<String> =
        if (value.isEmpty()) emptySet() else value.split(",").toSet()
}