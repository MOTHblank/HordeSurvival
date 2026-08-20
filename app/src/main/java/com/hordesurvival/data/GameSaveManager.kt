package com.hordesurvival.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.hordesurvival.game.weapon.WeaponType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Save/Load system for mid-run game state persistence.
 * Uses DataStore for fast, async storage.
 */
object GameSaveManager {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "game_save")

    // Save keys
    private val SAVE_EXISTS = booleanPreferencesKey("save_exists")
    private val SAVE_PLAYER_HP = floatPreferencesKey("player_hp")
    private val SAVE_PLAYER_MAX_HP = floatPreferencesKey("player_max_hp")
    private val SAVE_PLAYER_LEVEL = intPreferencesKey("player_level")
    private val SAVE_CURRENT_XP = floatPreferencesKey("current_xp")
    private val SAVE_XP_TO_NEXT = floatPreferencesKey("xp_to_next")
    private val SAVE_GOLD = floatPreferencesKey("gold")
    private val SAVE_KILLS = intPreferencesKey("kills")
    private val SAVE_GAME_TIME = floatPreferencesKey("game_time")
    private val SAVE_PLAYER_X = floatPreferencesKey("player_x")
    private val SAVE_PLAYER_Y = floatPreferencesKey("player_y")
    private val SAVE_WEAPONS = stringPreferencesKey("weapons")
    private val SAVE_PASSIVES = stringPreferencesKey("passives")
    private val SAVE_MAX_COMBO = intPreferencesKey("max_combo")

    // Meta progression keys
    private val META_GOLD = floatPreferencesKey("meta_gold")
    private val META_HP_LEVEL = intPreferencesKey("meta_hp_level")
    private val META_GOLD_LEVEL = intPreferencesKey("meta_gold_level")
    private val META_MIGHT_LEVEL = intPreferencesKey("meta_might_level")
    private val META_COOLDOWN_LEVEL = intPreferencesKey("meta_cooldown_level")
    private val META_SPEED_LEVEL = intPreferencesKey("meta_speed_level")
    private val META_LUCK_LEVEL = intPreferencesKey("meta_luck_level")
    private val META_BEST_TIME = floatPreferencesKey("meta_best_time")
    private val META_BEST_KILLS = intPreferencesKey("meta_best_kills")
    private val META_BEST_LEVEL = intPreferencesKey("meta_best_level")
    private val META_TOTAL_RUNS = intPreferencesKey("meta_total_runs")
    private val META_BEST_COMBO = intPreferencesKey("meta_best_combo")
    private val META_TUTORIAL_DONE = booleanPreferencesKey("tutorial_done")
    private val META_LANGUAGE = stringPreferencesKey("language")

    data class SaveState(
        val playerHp: Float, val playerMaxHp: Float,
        val playerLevel: Int, val currentXp: Float, val xpToNext: Float,
        val gold: Float, val kills: Int, val gameTime: Float,
        val playerX: Float, val playerY: Float,
        val weapons: List<WeaponType>, val passives: Map<String, Int>,
        val maxCombo: Int
    )

    data class MetaState(
        val gold: Float, val hpLevel: Int, val goldLevel: Int,
        val mightLevel: Int, val cooldownLevel: Int,
        val speedLevel: Int, val luckLevel: Int,
        val bestTime: Float, val bestKills: Int, val bestLevel: Int,
        val totalRuns: Int, val bestCombo: Int,
        val tutorialDone: Boolean, val language: String
    )

    suspend fun saveGame(context: Context, state: SaveState) {
        context.dataStore.edit { prefs ->
            prefs[SAVE_EXISTS] = true
            prefs[SAVE_PLAYER_HP] = state.playerHp
            prefs[SAVE_PLAYER_MAX_HP] = state.playerMaxHp
            prefs[SAVE_PLAYER_LEVEL] = state.playerLevel
            prefs[SAVE_CURRENT_XP] = state.currentXp
            prefs[SAVE_XP_TO_NEXT] = state.xpToNext
            prefs[SAVE_GOLD] = state.gold
            prefs[SAVE_KILLS] = state.kills
            prefs[SAVE_GAME_TIME] = state.gameTime
            prefs[SAVE_PLAYER_X] = state.playerX
            prefs[SAVE_PLAYER_Y] = state.playerY
            prefs[SAVE_WEAPONS] = state.weapons.joinToString(",") { it.name }
            prefs[SAVE_PASSIVES] = state.passives.entries.joinToString(",") { "${it.key}:${it.value}" }
            prefs[SAVE_MAX_COMBO] = state.maxCombo
        }
    }

    suspend fun loadGame(context: Context): SaveState? {
        val prefs = context.dataStore.data.first()
        if (prefs[SAVE_EXISTS] != true) return null

        val weapons = (prefs[SAVE_WEAPONS] ?: "").split(",").mapNotNull {
            try { WeaponType.valueOf(it.trim()) } catch (_: Exception) { null }
        }
        val passives = (prefs[SAVE_PASSIVES] ?: "").split(",").associate {
            val parts = it.split(":")
            if (parts.size == 2) parts[0].trim() to (parts[1].trim().toIntOrNull() ?: 0)
            else "" to 0
        }.filter { it.key.isNotEmpty() }

        return SaveState(
            playerHp = prefs[SAVE_PLAYER_HP] ?: 100f,
            playerMaxHp = prefs[SAVE_PLAYER_MAX_HP] ?: 100f,
            playerLevel = prefs[SAVE_PLAYER_LEVEL] ?: 1,
            currentXp = prefs[SAVE_CURRENT_XP] ?: 0f,
            xpToNext = prefs[SAVE_XP_TO_NEXT] ?: 10f,
            gold = prefs[SAVE_GOLD] ?: 0f,
            kills = prefs[SAVE_KILLS] ?: 0,
            gameTime = prefs[SAVE_GAME_TIME] ?: 0f,
            playerX = prefs[SAVE_PLAYER_X] ?: 0f,
            playerY = prefs[SAVE_PLAYER_Y] ?: 0f,
            weapons = weapons,
            passives = passives,
            maxCombo = prefs[SAVE_MAX_COMBO] ?: 0
        )
    }

    suspend fun clearSave(context: Context) {
        context.dataStore.edit { prefs ->
            prefs[SAVE_EXISTS] = false
        }
    }

    suspend fun hasSave(context: Context): Boolean {
        val prefs = context.dataStore.data.first()
        return prefs[SAVE_EXISTS] == true
    }

    // ── Meta Progression ────────────────────────────────────────

    suspend fun saveMeta(context: Context, state: MetaState) {
        context.dataStore.edit { prefs ->
            prefs[META_GOLD] = state.gold
            prefs[META_HP_LEVEL] = state.hpLevel
            prefs[META_GOLD_LEVEL] = state.goldLevel
            prefs[META_MIGHT_LEVEL] = state.mightLevel
            prefs[META_COOLDOWN_LEVEL] = state.cooldownLevel
            prefs[META_SPEED_LEVEL] = state.speedLevel
            prefs[META_LUCK_LEVEL] = state.luckLevel
            prefs[META_BEST_TIME] = state.bestTime
            prefs[META_BEST_KILLS] = state.bestKills
            prefs[META_BEST_LEVEL] = state.bestLevel
            prefs[META_TOTAL_RUNS] = state.totalRuns
            prefs[META_BEST_COMBO] = state.bestCombo
            prefs[META_TUTORIAL_DONE] = state.tutorialDone
            prefs[META_LANGUAGE] = state.language
        }
    }

    suspend fun loadMeta(context: Context): MetaState {
        val prefs = context.dataStore.data.first()
        return MetaState(
            gold = prefs[META_GOLD] ?: 0f,
            hpLevel = prefs[META_HP_LEVEL] ?: 0,
            goldLevel = prefs[META_GOLD_LEVEL] ?: 0,
            mightLevel = prefs[META_MIGHT_LEVEL] ?: 0,
            cooldownLevel = prefs[META_COOLDOWN_LEVEL] ?: 0,
            speedLevel = prefs[META_SPEED_LEVEL] ?: 0,
            luckLevel = prefs[META_LUCK_LEVEL] ?: 0,
            bestTime = prefs[META_BEST_TIME] ?: 0f,
            bestKills = prefs[META_BEST_KILLS] ?: 0,
            bestLevel = prefs[META_BEST_LEVEL] ?: 0,
            totalRuns = prefs[META_TOTAL_RUNS] ?: 0,
            bestCombo = prefs[META_BEST_COMBO] ?: 0,
            tutorialDone = prefs[META_TUTORIAL_DONE] ?: false,
            language = prefs[META_LANGUAGE] ?: "en"
        )
    }
}
