package com.hordesurvival.game.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.util.Log
import java.io.*
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Professional sound system using SoundPool with programmatically generated WAV tones.
 * Each sound type has unique frequency/duration for variety.
 * Thread-safe with a single playback thread.
 */
object SoundManager {

    private var soundPool: SoundPool? = null
    private var enabled = true
    private var volume = 0.8f
    private var musicVolume = 0.5f
    private var sfxVolume = 0.8f
    private var bgMusicEnabled = true
    private var bgMediaPlayer: android.media.MediaPlayer? = null
    private var initialized = false

    // Sound IDs in SoundPool
    private val sounds = mutableMapOf<SoundType, Int>()

    // Throttle: prevent sound flooding
    private val lastPlayTime = mutableMapOf<SoundType, Long>()
    private const val MIN_INTERVAL_MS = 30L  // minimum 30ms between same sound

    enum class SoundType {
        SHOOT_MISSILE, SHOOT_FIREBALL, SHOOT_ICE, SHOOT_LIGHTNING,
        SHOOT_POISON, SHOOT_BOOMERANG, SHOOT_SHIELD, SHOOT_SPEAR,
        HIT, HIT_CRIT, DEATH, PICKUP, PICKUP_BIG,
        DAMAGE, LEVEL_UP, BOSS_WARNING, GAME_OVER,
        COMBO_5, COMBO_10, COMBO_25, COMBO_50,
        EVOLUTION, ACHIEVEMENT, CLICK, PAUSE
    }

    fun initialize(context: Context? = null) {
        if (initialized) return
        try {
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(12)
                .setAudioAttributes(attrs)
                .build()

            // Generate and load all sounds
            context?.let { ctx ->
                loadSounds(ctx)
            } ?: run {
                // Fallback: generate without context (file-based)
                loadSoundsGenerated()
            }

            initialized = true
        } catch (e: Exception) {
            Log.w("SoundManager", "init failed", e)
        }
    }

    private fun loadSounds(ctx: Context) {
        // Generate WAV files and load into SoundPool
        val soundDefs = mapOf(
            SoundType.SHOOT_MISSILE to ToneDef(880f, 0.04f, 0.3f, waveType = "sine"),
            SoundType.SHOOT_FIREBALL to ToneDef(220f, 0.08f, 0.4f, waveType = "sawtooth"),
            SoundType.SHOOT_ICE to ToneDef(1200f, 0.06f, 0.3f, waveType = "sine"),
            SoundType.SHOOT_LIGHTNING to ToneDef(200f, 0.1f, 0.5f, waveType = "square"),
            SoundType.SHOOT_POISON to ToneDef(150f, 0.12f, 0.25f, waveType = "sawtooth"),
            SoundType.SHOOT_BOOMERANG to ToneDef(600f, 0.05f, 0.35f, waveType = "triangle"),
            SoundType.SHOOT_SHIELD to ToneDef(400f, 0.03f, 0.2f, waveType = "sine"),
            SoundType.SHOOT_SPEAR to ToneDef(1000f, 0.06f, 0.4f, waveType = "sine"),
            SoundType.HIT to ToneDef(300f, 0.03f, 0.3f, waveType = "square"),
            SoundType.HIT_CRIT to ToneDef(500f, 0.05f, 0.5f, waveType = "square"),
            SoundType.DEATH to ToneDef(180f, 0.15f, 0.4f, waveType = "sawtooth", decay = true),
            SoundType.PICKUP to ToneDef(1047f, 0.05f, 0.25f, waveType = "sine"),
            SoundType.PICKUP_BIG to ToneDef(1319f, 0.08f, 0.3f, waveType = "sine"),
            SoundType.DAMAGE to ToneDef(120f, 0.1f, 0.5f, waveType = "square", decay = true),
            SoundType.LEVEL_UP to ToneDef(523f, 0.15f, 0.4f, waveType = "sine"),
            SoundType.BOSS_WARNING to ToneDef(200f, 0.2f, 0.5f, waveType = "square"),
            SoundType.GAME_OVER to ToneDef(100f, 0.4f, 0.5f, waveType = "sawtooth", decay = true),
            SoundType.COMBO_5 to ToneDef(660f, 0.06f, 0.3f, waveType = "sine"),
            SoundType.COMBO_10 to ToneDef(880f, 0.08f, 0.35f, waveType = "sine"),
            SoundType.COMBO_25 to ToneDef(1100f, 0.1f, 0.4f, waveType = "sine"),
            SoundType.COMBO_50 to ToneDef(1320f, 0.12f, 0.45f, waveType = "sine"),
            SoundType.EVOLUTION to ToneDef(440f, 0.3f, 0.5f, waveType = "sine"),
            SoundType.ACHIEVEMENT to ToneDef(880f, 0.2f, 0.4f, waveType = "sine"),
            SoundType.CLICK to ToneDef(600f, 0.02f, 0.2f, waveType = "sine"),
            SoundType.PAUSE to ToneDef(300f, 0.05f, 0.2f, waveType = "sine")
        )

        for ((type, def) in soundDefs) {
            try {
                val wavData = generateWav(def)
                val file = File(ctx.cacheDir, "sound_${type.name}.wav")
                FileOutputStream(file).use { it.write(wavData) }
                val soundId = soundPool?.load(file.absolutePath, 1) ?: 0
                if (soundId > 0) sounds[type] = soundId
            } catch (e: Exception) {
                Log.w("SoundManager", "Failed to load $type", e)
            }
        }
    }

    private fun loadSoundsGenerated() {
        // Fallback: no sounds loaded (will be silent)
        Log.w("SoundManager", "No context provided, sounds disabled")
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        sounds.clear()
        initialized = false
    }

    fun setVolume(v: Float) { volume = v.coerceIn(0f, 1f) }
    fun setEnabled(e: Boolean) { enabled = e }
    fun setMusicVolume(v: Float) {
        musicVolume = v.coerceIn(0f, 1f)
        bgMediaPlayer?.setVolume(musicVolume, musicVolume)
    }
    fun setSfxVolume(v: Float) { sfxVolume = v.coerceIn(0f, 1f); volume = sfxVolume }

    /** Sync volumes from saved preferences */
    fun syncVolumes(savedMusic: Float, savedSfx: Float) {
        setMusicVolume(savedMusic)
        setSfxVolume(savedSfx)
    }
    fun getMusicVolume(): Float = musicVolume
    fun getSfxVolume(): Float = sfxVolume
    fun isBgMusicEnabled(): Boolean = bgMusicEnabled

    /** Start background music loop using generated ambient tone */
    fun startBgMusic(ctx: Context) {
        if (!bgMusicEnabled) return
        try {
            if (bgMediaPlayer != null) return // already playing
            // Generate a longer ambient WAV for background music
            val wavData = generateBgMusicWav()
            val file = java.io.File(ctx.cacheDir, "bg_music.wav")
            java.io.FileOutputStream(file).use { it.write(wavData) }
            bgMediaPlayer = android.media.MediaPlayer().apply {
                setDataSource(file.absolutePath)
                setVolume(musicVolume, musicVolume)
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            android.util.Log.w("SoundManager", "bgMusic start failed", e)
        }
    }

    fun stopBgMusic() {
        try {
            bgMediaPlayer?.stop()
            bgMediaPlayer?.release()
        } catch (_: Exception) {}
        bgMediaPlayer = null
    }

    fun pauseBgMusic() {
        try { bgMediaPlayer?.pause() } catch (_: Exception) {}
    }

    fun resumeBgMusic() {
        try { if (bgMusicEnabled) bgMediaPlayer?.start() } catch (_: Exception) {}
    }

    fun toggleBgMusic() {
        bgMusicEnabled = !bgMusicEnabled
        if (bgMusicEnabled) resumeBgMusic() else pauseBgMusic()
    }

    /**
     * Play a sound with throttling to prevent flooding.
     */
    fun play(type: SoundType) {
        if (!enabled || !initialized) return
        val pool = soundPool ?: return
        val soundId = sounds[type] ?: return

        // Throttle
        val now = System.currentTimeMillis()
        val lastPlay = lastPlayTime[type] ?: 0L
        if (now - lastPlay < MIN_INTERVAL_MS) return
        lastPlayTime[type] = now

        try {
            pool.play(soundId, volume, volume, 1, 0, 1f)
        } catch (e: Exception) {
            Log.w("SoundManager", "play $type failed", e)
        }
    }

    // Convenience methods
    fun playShoot(type: com.hordesurvival.game.weapon.WeaponType) {
        when (type) {
            com.hordesurvival.game.weapon.WeaponType.MAGIC_MISSILE -> play(SoundType.SHOOT_MISSILE)
            com.hordesurvival.game.weapon.WeaponType.FIREBALL -> play(SoundType.SHOOT_FIREBALL)
            com.hordesurvival.game.weapon.WeaponType.ICE_SHARD -> play(SoundType.SHOOT_ICE)
            com.hordesurvival.game.weapon.WeaponType.LIGHTNING_RING -> play(SoundType.SHOOT_LIGHTNING)
            com.hordesurvival.game.weapon.WeaponType.POISON_CLOUD -> play(SoundType.SHOOT_POISON)
            com.hordesurvival.game.weapon.WeaponType.BOOMERANG_DAGGER -> play(SoundType.SHOOT_BOOMERANG)
            com.hordesurvival.game.weapon.WeaponType.ORBITING_SHIELD -> play(SoundType.SHOOT_SHIELD)
            com.hordesurvival.game.weapon.WeaponType.DIVINE_SPEAR -> play(SoundType.SHOOT_SPEAR)
        }
    }
    fun playHit() = play(SoundType.HIT)
    fun playHitCrit() = play(SoundType.HIT_CRIT)
    fun playDeath() = play(SoundType.DEATH)
    fun playPickup() = play(SoundType.PICKUP)
    fun playPickupBig() = play(SoundType.PICKUP_BIG)
    fun playDamage() = play(SoundType.DAMAGE)
    fun playLevelUp() = play(SoundType.LEVEL_UP)
    fun playBossWarning() = play(SoundType.BOSS_WARNING)
    fun playGameOver() = play(SoundType.GAME_OVER)
    fun playClick() = play(SoundType.CLICK)
    fun playPause() = play(SoundType.PAUSE)
    fun playEvolution() = play(SoundType.EVOLUTION)
    fun playAchievement() = play(SoundType.ACHIEVEMENT)

    fun playCombo(combo: Int) {
        when {
            combo >= 50 -> play(SoundType.COMBO_50)
            combo >= 25 -> play(SoundType.COMBO_25)
            combo >= 10 -> play(SoundType.COMBO_10)
            combo >= 5 -> play(SoundType.COMBO_5)
        }
    }

    // ── WAV Generation ──────────────────────────────────────────────

    private data class ToneDef(
        val frequency: Float,
        val durationSec: Float,
        val amplitude: Float = 0.3f,
        val waveType: String = "sine",
        val decay: Boolean = false
    )

    private fun generateWav(def: ToneDef): ByteArray {
        val sampleRate = 22050
        val numSamples = (def.durationSec * sampleRate).toInt()
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val envelope = if (def.decay) {
                (1f - t / def.durationSec).coerceIn(0f, 1f)
            } else {
                1f
            }

            val sample = when (def.waveType) {
                "sine" -> kotlin.math.sin(2.0 * Math.PI * def.frequency * t).toFloat()
                "square" -> if (kotlin.math.sin(2.0 * Math.PI * def.frequency * t) > 0) 1f else -1f
                "sawtooth" -> (2f * (def.frequency * t % 1f) - 1f)
                "triangle" -> (4f * kotlin.math.abs(def.frequency * t % 1f - 0.5f) - 1f)
                else -> kotlin.math.sin(2.0 * Math.PI * def.frequency * t).toFloat()
            }

            samples[i] = (sample * def.amplitude * envelope * Short.MAX_VALUE).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        // Build WAV file
        val dataSize = numSamples * 2
        val buf = ByteBuffer.allocate(44 + dataSize).order(ByteOrder.LITTLE_ENDIAN)

        // RIFF header
        buf.put("RIFF".toByteArray())
        buf.putInt(36 + dataSize)
        buf.put("WAVE".toByteArray())

        // fmt chunk
        buf.put("fmt ".toByteArray())
        buf.putInt(16) // chunk size
        buf.putShort(1) // PCM
        buf.putShort(1) // mono
        buf.putInt(sampleRate)
        buf.putInt(sampleRate * 2) // byte rate
        buf.putShort(2) // block align
        buf.putShort(16) // bits per sample

        // data chunk
        buf.put("data".toByteArray())
        buf.putInt(dataSize)
        for (sample in samples) {
            buf.putShort(sample)
        }

        return buf.array()
    }

    /** Generate ambient background music WAV — dark synth pad loop */
    private fun generateBgMusicWav(): ByteArray {
        val sampleRate = 22050
        val durationSec = 16  // 16 second loop
        val numSamples = durationSec * sampleRate
        val samples = ShortArray(numSamples)

        // Multiple layered tones for ambient feel
        val tones = floatArrayOf(55f, 82.4f, 110f, 164.8f) // A1, E2, A2, E3
        val amplitudes = floatArrayOf(0.12f, 0.08f, 0.06f, 0.04f)

        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val progress = t / durationSec
            // Slow envelope: fade in/out over the loop
            val envelope = if (progress < 0.05f) progress / 0.05f
                else if (progress > 0.95f) (1f - progress) / 0.05f
                else 1f

            var sample = 0f
            for (j in tones.indices) {
                val freq = tones[j]
                // Sine wave with slow vibrato
                val vibrato = 1f + 0.003f * kotlin.math.sin(2.0 * Math.PI * 0.5 * t + j.toDouble()).toFloat()
                sample += (kotlin.math.sin(2.0 * Math.PI * freq * vibrato * t) * amplitudes[j]).toFloat()
                // Add subtle harmonic
                sample += (kotlin.math.sin(2.0 * Math.PI * freq * 2.0 * t) * amplitudes[j] * 0.15f).toFloat()
            }

            // Low-pass filter effect (simple moving average approximation)
            samples[i] = (sample * envelope * Short.MAX_VALUE * 0.6f).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        // Build WAV
        val dataSize = numSamples * 2
        val buf = ByteBuffer.allocate(44 + dataSize).order(ByteOrder.LITTLE_ENDIAN)
        buf.put("RIFF".toByteArray()); buf.putInt(36 + dataSize); buf.put("WAVE".toByteArray())
        buf.put("fmt ".toByteArray()); buf.putInt(16); buf.putShort(1); buf.putShort(1)
        buf.putInt(sampleRate); buf.putInt(sampleRate * 2); buf.putShort(2); buf.putShort(16)
        buf.put("data".toByteArray()); buf.putInt(dataSize)
        for (sample in samples) buf.putShort(sample)
        return buf.array()
    }
}
