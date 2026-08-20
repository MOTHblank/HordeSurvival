package com.hordesurvival.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hordesurvival.data.model.PlayerSave
import com.hordesurvival.data.model.RunRecord
import com.hordesurvival.data.model.UnlockedCharacter
import com.hordesurvival.data.repository.GameRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GameRepository(application)

    val playerSave: StateFlow<PlayerSave> = repository.playerSave
        .map { it ?: PlayerSave() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlayerSave())

    val recentRuns = repository.recentRuns
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val characters = repository.allCharacters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _dailyReward = MutableStateFlow<String?>(null); val dailyReward: StateFlow<String?> = _dailyReward

    init {
        viewModelScope.launch {
            repository.initializeSave()
            checkDailyLogin()
        }
    }

    private suspend fun checkDailyLogin() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val current = playerSave.value
        if (current.lastLoginDate != today) {
            val newStreak = if (isYesterday(current.lastLoginDate)) current.loginStreak + 1 else 1
            val reward = when {
                newStreak >= 7 -> 200
                newStreak >= 3 -> 100
                else -> 50
            }
            repository.updateSave(current.copy(
                lastLoginDate = today,
                loginStreak = newStreak,
                totalGold = current.totalGold + reward
            ))
            _dailyReward.value = "Day $newStreak: +$reward gold!"
        }
    }

    private fun isYesterday(dateStr: String): Boolean {
        if (dateStr.isEmpty()) return false
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = sdf.parse(dateStr) ?: return false
            val yesterday = System.currentTimeMillis() - 86400000L
            sdf.format(Date(yesterday)) == dateStr
        } catch (_: Exception) { false }
    }

    fun dismissDailyReward() { _dailyReward.value = null }

    fun addGold(amount: Int) = viewModelScope.launch { repository.addGold(amount) }

    /** Returns true if upgrade succeeded (gold was enough) */
    fun upgradeMeta(stat: String, cost: Int, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val ok = repository.upgradeMeta(stat, cost)
            onResult(ok)
        }
    }

    fun recordRun(run: RunRecord) = viewModelScope.launch { repository.recordRun(run) }

    fun unlockCharacter(id: Int, cost: Int, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val ok = repository.unlockCharacter(id, cost)
            onResult(ok)
        }
    }

    fun updateVolume(music: Float, sfx: Float) = viewModelScope.launch {
        repository.updateSave(playerSave.value.copy(musicVolume = music, sfxVolume = sfx))
    }

    fun toggleVibration() = viewModelScope.launch {
        repository.updateSave(playerSave.value.copy(vibrationEnabled = !playerSave.value.vibrationEnabled))
    }

    fun setLanguage(code: String) = viewModelScope.launch {
        repository.updateSave(playerSave.value.copy(languageCode = code))
    }

    fun setBackgroundStyle(style: Int) = viewModelScope.launch {
        repository.setBackgroundStyle(style)
    }

    fun toggleBgMusic() = viewModelScope.launch {
        repository.updateSave(playerSave.value.copy(bgMusicEnabled = !playerSave.value.bgMusicEnabled))
    }

    fun setGraphicsQuality(quality: Int) = viewModelScope.launch {
        repository.setGraphicsQuality(quality)
    }

    fun toggleDamageNumbers() = viewModelScope.launch {
        repository.setShowDamageNumbers(!playerSave.value.showDamageNumbers)
    }

    fun toggleParticles() = viewModelScope.launch {
        repository.setShowParticles(!playerSave.value.showParticles)
    }

    fun toggleComboCounter() = viewModelScope.launch {
        repository.setShowComboCounter(!playerSave.value.showComboCounter)
    }

    fun toggleScreenShake() = viewModelScope.launch {
        repository.setScreenShakeEnabled(!playerSave.value.screenShakeEnabled)
    }
}
