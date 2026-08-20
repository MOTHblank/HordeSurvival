package com.hordesurvival.game.audio

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri

/**
 * Dynamic music system — intensity changes based on gameplay.
 * Also supports personal music gallery (user picks from device).
 */
object DynamicMusic {

    enum class Intensity(val level: Int) {
        CALM(0),        // exploring, few enemies
        TENSE(1),       // enemies nearby
        INTENSE(2),     // many enemies, taking damage
        EPIC(3),        // boss fight
        CRITICAL(4)     // near death
    }

    var currentIntensity = Intensity.CALM
    var isEnabled = true
    var volume = 0.5f

    // Personal music
    private var personalMusicUri: Uri? = null
    private var personalPlayer: MediaPlayer? = null
    private var usePersonalMusic = false

    // Intensity calculation
    private var enemyCount = 0
    private var playerHpRatio = 1f
    private var bossActive = false
    private var comboCount = 0

    fun updateIntensity(
        enemies: Int,
        hpRatio: Float,
        isBoss: Boolean,
        combo: Int
    ) {
        enemyCount = enemies
        playerHpRatio = hpRatio
        bossActive = isBoss
        comboCount = combo

        currentIntensity = when {
            hpRatio < 0.2f -> Intensity.CRITICAL
            isBoss -> Intensity.EPIC
            enemies > 100 || combo > 30 -> Intensity.INTENSE
            enemies > 40 -> Intensity.TENSE
            else -> Intensity.CALM
        }
    }

    fun setPersonalMusic(context: Context, uri: Uri) {
        personalMusicUri?.let { releasePersonal() }
        personalMusicUri = uri
        usePersonalMusic = true
    }

    fun clearPersonalMusic() {
        releasePersonal()
        personalMusicUri = null
        usePersonalMusic = false
    }

    fun hasPersonalMusic(): Boolean = personalMusicUri != null

    fun startPersonalMusic(context: Context) {
        if (!usePersonalMusic || personalMusicUri == null) return
        try {
            personalPlayer?.release()
            personalPlayer = MediaPlayer.create(context, personalMusicUri!!)
            personalPlayer?.isLooping = true
            personalPlayer?.setVolume(volume, volume)
            personalPlayer?.start()
        } catch (e: Exception) {
            android.util.Log.e("DynamicMusic", "Failed to play personal music", e)
        }
    }

    fun stopPersonalMusic() {
        personalPlayer?.pause()
    }

    fun releasePersonal() {
        personalPlayer?.release()
        personalPlayer = null
    }

    fun updateVolume(v: Float) {
        volume = v
        personalPlayer?.setVolume(v, v)
    }

    /** Get a descriptive label for current intensity */
    fun getIntensityLabel(): String = when (currentIntensity) {
        Intensity.CALM -> "🎵 Calm"
        Intensity.TENSE -> "🎵 Tense"
        Intensity.INTENSE -> "🎵 Intense!"
        Intensity.EPIC -> "🎵 EPIC!"
        Intensity.CRITICAL -> "🎵 CRITICAL!"
    }
}
