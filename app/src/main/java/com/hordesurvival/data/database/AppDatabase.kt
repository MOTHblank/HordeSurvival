package com.hordesurvival.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.hordesurvival.data.model.PlayerSave
import com.hordesurvival.data.model.RunRecord
import com.hordesurvival.data.model.UnlockedCharacter

/**
 * Room database for game persistence.
 * Stores meta-progression, run history, and unlocks.
 */
@Database(
    entities = [PlayerSave::class, RunRecord::class, UnlockedCharacter::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun playerDao(): PlayerDao
    abstract fun runDao(): RunDao
    abstract fun characterDao(): CharacterDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "horde_survival_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
