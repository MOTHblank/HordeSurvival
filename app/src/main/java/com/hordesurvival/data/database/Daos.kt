package com.hordesurvival.data.database

import androidx.room.*
import com.hordesurvival.data.model.PlayerSave
import com.hordesurvival.data.model.RunRecord
import com.hordesurvival.data.model.UnlockedCharacter
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Query("SELECT * FROM player_save WHERE id = 1")
    fun getPlayerSave(): Flow<PlayerSave?>

    @Query("SELECT * FROM player_save WHERE id = 1")
    suspend fun getPlayerSaveOnce(): PlayerSave?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePlayerData(data: PlayerSave)

    @Query("UPDATE player_save SET totalGold = totalGold + :gold WHERE id = 1")
    suspend fun addGold(gold: Int)

    // FIX: deduct gold AND increment level in ONE atomic query
    @Query("UPDATE player_save SET metaHpLevel = metaHpLevel + 1, totalGold = totalGold - :cost WHERE id = 1 AND totalGold >= :cost")
    suspend fun upgradeMetaHp(cost: Int): Int

    @Query("UPDATE player_save SET metaGoldLevel = metaGoldLevel + 1, totalGold = totalGold - :cost WHERE id = 1 AND totalGold >= :cost")
    suspend fun upgradeMetaGold(cost: Int): Int

    @Query("UPDATE player_save SET metaMightLevel = metaMightLevel + 1, totalGold = totalGold - :cost WHERE id = 1 AND totalGold >= :cost")
    suspend fun upgradeMetaMight(cost: Int): Int

    @Query("UPDATE player_save SET metaCooldownLevel = metaCooldownLevel + 1, totalGold = totalGold - :cost WHERE id = 1 AND totalGold >= :cost")
    suspend fun upgradeMetaCooldown(cost: Int): Int

    @Query("UPDATE player_save SET metaSpeedLevel = metaSpeedLevel + 1, totalGold = totalGold - :cost WHERE id = 1 AND totalGold >= :cost")
    suspend fun upgradeMetaSpeed(cost: Int): Int

    @Query("UPDATE player_save SET metaLuckLevel = metaLuckLevel + 1, totalGold = totalGold - :cost WHERE id = 1 AND totalGold >= :cost")
    suspend fun upgradeMetaLuck(cost: Int): Int

    @Query("UPDATE player_save SET totalGold = totalGold - :cost WHERE id = 1 AND totalGold >= :cost")
    suspend fun deductGold(cost: Int): Int

    @Query("UPDATE player_save SET backgroundStyle = :style WHERE id = 1")
    suspend fun setBackgroundStyle(style: Int)

    @Query("UPDATE player_save SET graphicsQuality = :quality WHERE id = 1")
    suspend fun setGraphicsQuality(quality: Int)

    @Query("UPDATE player_save SET showDamageNumbers = :show WHERE id = 1")
    suspend fun setShowDamageNumbers(show: Boolean)

    @Query("UPDATE player_save SET showParticles = :show WHERE id = 1")
    suspend fun setShowParticles(show: Boolean)

    @Query("UPDATE player_save SET showComboCounter = :show WHERE id = 1")
    suspend fun setShowComboCounter(show: Boolean)

    @Query("UPDATE player_save SET screenShakeEnabled = :enabled WHERE id = 1")
    suspend fun setScreenShakeEnabled(enabled: Boolean)
}

@Dao
interface RunDao {
    @Insert
    suspend fun insertRun(run: RunRecord)

    @Query("SELECT * FROM run_records ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentRuns(limit: Int = 20): Flow<List<RunRecord>>

    @Query("SELECT MAX(timeSurvived) FROM run_records")
    suspend fun getBestTime(): Float?

    @Query("SELECT MAX(level) FROM run_records")
    suspend fun getBestLevel(): Int?
}

@Dao
interface CharacterDao {
    @Query("SELECT * FROM characters")
    fun getAllCharacters(): Flow<List<UnlockedCharacter>>

    @Query("SELECT * FROM characters WHERE isUnlocked = 1")
    fun getUnlockedCharacters(): Flow<List<UnlockedCharacter>>

    @Query("SELECT * FROM characters LIMIT 1")
    suspend fun getUnlockedCharactersOnce(): List<UnlockedCharacter>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacter(character: UnlockedCharacter)

    @Query("UPDATE characters SET isUnlocked = 1 WHERE characterId = :id")
    suspend fun unlockCharacter(id: Int)
}
