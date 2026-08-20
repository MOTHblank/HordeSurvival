package com.hordesurvival.data.repository

import android.content.Context
import com.hordesurvival.data.database.AppDatabase
import com.hordesurvival.data.model.PlayerSave
import com.hordesurvival.data.model.RunRecord
import com.hordesurvival.data.model.UnlockedCharacter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

class GameRepository(context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val playerDao = db.playerDao()
    private val runDao = db.runDao()
    private val characterDao = db.characterDao()

    val playerSave: Flow<PlayerSave?> = playerDao.getPlayerSave()

    suspend fun initializeSave() {
        if (playerDao.getPlayerSaveOnce() == null) {
            playerDao.savePlayerData(PlayerSave())
        }
        // Always ensure characters exist in DB
        initializeDefaultCharacters()
    }

    suspend fun addGold(amount: Int) = playerDao.addGold(amount)

    /** Upgrade meta stat — deducts gold and increments level atomically */
    suspend fun upgradeMeta(stat: String, cost: Int): Boolean {
        val rows = when (stat) {
            "hp" -> playerDao.upgradeMetaHp(cost)
            "gold" -> playerDao.upgradeMetaGold(cost)
            "might" -> playerDao.upgradeMetaMight(cost)
            "cooldown" -> playerDao.upgradeMetaCooldown(cost)
            "speed" -> playerDao.upgradeMetaSpeed(cost)
            "luck" -> playerDao.upgradeMetaLuck(cost)
            else -> 0
        }
        return rows > 0
    }

    suspend fun unlockCharacter(id: Int, cost: Int): Boolean {
        val save = playerDao.getPlayerSaveOnce() ?: return false
        if (save.totalGold < cost) return false
        playerDao.deductGold(cost)
        characterDao.unlockCharacter(id)
        return true
    }

    suspend fun updateSave(save: PlayerSave) = playerDao.savePlayerData(save)
    suspend fun setBackgroundStyle(style: Int) = playerDao.setBackgroundStyle(style)
    suspend fun setGraphicsQuality(quality: Int) = playerDao.setGraphicsQuality(quality)
    suspend fun setShowDamageNumbers(show: Boolean) = playerDao.setShowDamageNumbers(show)
    suspend fun setShowParticles(show: Boolean) = playerDao.setShowParticles(show)
    suspend fun setShowComboCounter(show: Boolean) = playerDao.setShowComboCounter(show)
    suspend fun setScreenShakeEnabled(enabled: Boolean) = playerDao.setScreenShakeEnabled(enabled)

    val recentRuns: Flow<List<RunRecord>> = runDao.getRecentRuns()

    suspend fun recordRun(run: RunRecord) {
        // Use NonCancellable to ensure DB writes complete even if scope is cancelled
        withContext(NonCancellable) {
            runDao.insertRun(run)
            val save = playerDao.getPlayerSaveOnce() ?: return@withContext
            playerDao.savePlayerData(save.copy(
                totalGold = save.totalGold + run.goldEarned,
                totalRuns = save.totalRuns + 1,
                totalKills = save.totalKills + run.kills,
                bestTime = maxOf(save.bestTime, run.timeSurvived),
                bestLevel = maxOf(save.bestLevel, run.level)
            ))
        }
    }

    val allCharacters: Flow<List<UnlockedCharacter>> = characterDao.getAllCharacters()

    private suspend fun initializeDefaultCharacters() {
        // Only insert if characters table is empty
        val existing = characterDao.getUnlockedCharactersOnce()
        if (existing.isNotEmpty()) return
        val characters = listOf(
            UnlockedCharacter(0, "Mage", true, "MAGIC_MISSILE", "Homing missiles", 100f, 200f, 1f, "Balanced mage with homing magic."),
            UnlockedCharacter(1, "Paladin", true, "ORBITING_SHIELD", "+20% armor", 130f, 170f, 0.9f, "Tanky warrior with orbiting shields."),
            UnlockedCharacter(2, "Rogue", true, "BOOMERANG_DAGGER", "+30% speed", 80f, 260f, 1.1f, "Fast and deadly with boomerangs."),
            UnlockedCharacter(3, "Alchemist", false, "POISON_CLOUD", "Poison +50% duration", 90f, 200f, 1f, "Master of poison."),
            UnlockedCharacter(4, "Archmage", false, "DIVINE_SPEAR", "+25% area", 70f, 180f, 1.2f, "Glass cannon with immense power."),
            UnlockedCharacter(5, "Pyromancer", false, "FIREBALL", "Fire +40% damage", 85f, 190f, 1.15f, "Explosive fire specialist."),
            UnlockedCharacter(6, "Frost Mage", false, "ICE_SHARD", "Slow +30% stronger", 95f, 210f, 0.95f, "Freezes everything."),
            UnlockedCharacter(7, "Storm Caller", false, "LIGHTNING_RING", "Lightning +30% radius", 80f, 220f, 1.05f, "Electric AOE master."),
            UnlockedCharacter(8, "Assassin", false, "BOOMERANG_DAGGER", "+20% crit chance", 60f, 300f, 1.3f, "Glass cannon speedster."),
            UnlockedCharacter(9, "Necromancer", false, "POISON_CLOUD", "Summons minions", 75f, 180f, 0.85f, "Dark magic wielder."),
        )
        characters.forEach { characterDao.insertCharacter(it) }
    }
}
