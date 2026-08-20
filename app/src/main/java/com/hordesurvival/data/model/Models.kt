package com.hordesurvival.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_save")
data class PlayerSave(
    @PrimaryKey val id: Int = 1,
    val totalGold: Int = 0,
    val totalRuns: Int = 0,
    val totalKills: Int = 0,
    val bestTime: Float = 0f,
    val bestLevel: Int = 0,
    val metaHpLevel: Int = 0,
    val metaGoldLevel: Int = 0,
    val metaMightLevel: Int = 0,
    val metaCooldownLevel: Int = 0,
    val metaSpeedLevel: Int = 0,
    val metaLuckLevel: Int = 0,
    val selectedCharacterId: Int = 0,
    val musicVolume: Float = 0.7f,
    val sfxVolume: Float = 0.8f,
    val vibrationEnabled: Boolean = true,
    val languageCode: String = "en",
    val backgroundStyle: Int = 0,  // 0=grid, 1=stars, 2=nebula, 3=checkerboard, 4=solid
    val bgMusicEnabled: Boolean = true,
    val questsCompleted: String = "",
    val dailyChallengeDate: String = "",
    val dailyChallengeScore: Int = 0,
    val lastLoginDate: String = "",
    val loginStreak: Int = 0,
    // Graphics & Accessibility settings
    val graphicsQuality: Int = 1,          // 0=Low, 1=Medium, 2=High
    val showDamageNumbers: Boolean = true,
    val showParticles: Boolean = true,
    val showComboCounter: Boolean = true,
    val screenShakeEnabled: Boolean = true,
    // Prestige
    val prestigeLevel: Int = 0,
    val totalGoldEarned: Int = 0,
    // Blessings (JSON-encoded map: blessingType:level)
    val blessingLevels: String = "",
    // Pets (comma-separated pet type names)
    val unlockedPets: String = ""
)

@Entity(tableName = "run_records")
data class RunRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val characterId: Int = 0,
    val gameMode: String = "SURVIVAL",
    val timeSurvived: Float = 0f,
    val level: Int = 0,
    val kills: Int = 0,
    val goldEarned: Int = 0,
    val weaponsUsed: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "characters")
data class UnlockedCharacter(
    @PrimaryKey val characterId: Int,
    val name: String,
    val isUnlocked: Boolean = false,
    val startingWeapon: String = "MAGIC_MISSILE",
    val specialAbility: String = "",
    val baseHp: Float = 100f,
    val baseSpeed: Float = 200f,
    val baseMight: Float = 1f,
    val description: String = ""
)
