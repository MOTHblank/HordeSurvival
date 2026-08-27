package com.hordesurvival.game.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin

/**
 * High-quality procedural sound system using SoundPool with synthesized PCM sound effects
 * and ambient multi-chord background music.
 * Features ADSR envelopes, pitch-sweeps, harmonic blending, and click-free audio rendering.
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

    private val sounds = mutableMapOf<SoundType, Int>()
    private val lastPlayTime = mutableMapOf<SoundType, Long>()
    private const val MIN_INTERVAL_MS = 35L  // Prevent audio cluttering/flooding

    enum class SoundType {
        SHOOT_MISSILE, SHOOT_FIREBALL, SHOOT_ICE, SHOOT_LIGHTNING,
        SHOOT_POISON, SHOOT_BOOMERANG, SHOOT_SHIELD, SHOOT_SPEAR,
        HIT, HIT_CRIT, DEATH, PICKUP, PICKUP_BIG,
        DAMAGE, LEVEL_UP, BOSS_WARNING, GAME_OVER,
        COMBO_5, COMBO_10, COMBO_25, COMBO_50,
        EVOLUTION, ACHIEVEMENT, CLICK, HOVER, PAUSE
    }

    fun initialize(context: Context? = null) {
        if (initialized) return
        try {
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(16)
                .setAudioAttributes(attrs)
                .build()

            context?.let { ctx ->
                loadSounds(ctx)
            }

            initialized = true
        } catch (e: Exception) {
            Log.w("SoundManager", "init failed", e)
        }
    }

    private fun loadSounds(ctx: Context) {
        for (type in SoundType.values()) {
            try {
                val wavData = generateSfxWav(type)
                val file = File(ctx.cacheDir, "sound_${type.name}.wav")
                FileOutputStream(file).use { it.write(wavData) }
                val soundId = soundPool?.load(file.absolutePath, 1) ?: 0
                if (soundId > 0) sounds[type] = soundId
            } catch (e: Exception) {
                Log.w("SoundManager", "Failed to load $type", e)
            }
        }
    }

    fun release() {
        stopBgMusic()
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

    fun syncVolumes(savedMusic: Float, savedSfx: Float) {
        setMusicVolume(savedMusic)
        setSfxVolume(savedSfx)
    }
    fun getMusicVolume(): Float = musicVolume
    fun getSfxVolume(): Float = sfxVolume
    fun isBgMusicEnabled(): Boolean = bgMusicEnabled

    fun startBgMusic(ctx: Context) {
        if (!bgMusicEnabled) return
        try {
            if (bgMediaPlayer != null) return
            val wavData = generateBgMusicWav()
            val file = File(ctx.cacheDir, "bg_music_v2.wav")
            FileOutputStream(file).use { it.write(wavData) }
            bgMediaPlayer = android.media.MediaPlayer().apply {
                setDataSource(file.absolutePath)
                setVolume(musicVolume, musicVolume)
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.w("SoundManager", "bgMusic start failed", e)
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

    fun play(type: SoundType) {
        if (!enabled || !initialized) return
        val pool = soundPool ?: return
        val soundId = sounds[type] ?: return

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
    fun playHover() = play(SoundType.HOVER)
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

    // ── High Quality Audio Synthesis ──────────────────────────────

    private const val SAMPLE_RATE = 44100

    /** Fast pseudo-random white noise generator */
    private class SimpleNoise(seed: Long) {
        private var state = seed
        fun nextFloat(): Float {
            state = (state * 0x5DEECE66DL + 0xBL) and 0xFFFFFFFFFFFFL
            return ((state ushr 16).toInt() and 0xFFFF) / 32768f - 1f
        }
    }

    private fun generateSfxWav(type: SoundType): ByteArray {
        val samples: ShortArray = when (type) {
            SoundType.SHOOT_MISSILE -> synthSweep(startFreq = 700f, endFreq = 350f, durationSec = 0.06f, amplitude = 0.25f)
            SoundType.SHOOT_FIREBALL -> synthWhoosh(startFreq = 300f, endFreq = 100f, durationSec = 0.09f, noiseMix = 0.35f, amplitude = 0.28f)
            SoundType.SHOOT_ICE -> synthChime(frequencies = floatArrayOf(1046.5f, 1567.98f), durationSec = 0.06f, amplitude = 0.22f)
            SoundType.SHOOT_LIGHTNING -> synthZap(freq = 450f, durationSec = 0.07f, noiseMix = 0.4f, amplitude = 0.25f)
            SoundType.SHOOT_POISON -> synthBubble(baseFreq = 220f, durationSec = 0.08f, amplitude = 0.22f)
            SoundType.SHOOT_BOOMERANG -> synthSwoosh(startFreq = 400f, midFreq = 650f, endFreq = 400f, durationSec = 0.08f, amplitude = 0.22f)
            SoundType.SHOOT_SHIELD -> synthToneHarmonics(baseFreq = 523.25f, durationSec = 0.08f, amplitude = 0.2f)
            SoundType.SHOOT_SPEAR -> synthSweep(startFreq = 800f, endFreq = 1200f, durationSec = 0.06f, amplitude = 0.24f)

            SoundType.HIT -> synthImpact(freq = 150f, noiseMix = 0.25f, durationSec = 0.04f, amplitude = 0.28f)
            SoundType.HIT_CRIT -> synthImpact(freq = 280f, noiseMix = 0.15f, durationSec = 0.06f, amplitude = 0.35f, highBell = 1046.5f)
            SoundType.DEATH -> synthImpact(freq = 110f, noiseMix = 0.3f, durationSec = 0.11f, amplitude = 0.25f)
            SoundType.DAMAGE -> synthImpact(freq = 90f, noiseMix = 0.4f, durationSec = 0.12f, amplitude = 0.32f)

            SoundType.PICKUP -> synthArpeggio(notes = floatArrayOf(880f, 1108.73f), noteDuration = 0.035f, amplitude = 0.22f)
            SoundType.PICKUP_BIG -> synthArpeggio(notes = floatArrayOf(880f, 1108.73f, 1318.51f), noteDuration = 0.045f, amplitude = 0.25f)

            SoundType.LEVEL_UP -> synthArpeggio(notes = floatArrayOf(523.25f, 659.25f, 783.99f, 1046.50f), noteDuration = 0.07f, amplitude = 0.28f)
            SoundType.BOSS_WARNING -> synthWarningHorn(freq1 = 130.81f, freq2 = 196f, durationSec = 0.32f, amplitude = 0.3f)
            SoundType.GAME_OVER -> synthArpeggio(notes = floatArrayOf(329.63f, 261.63f, 220f), noteDuration = 0.15f, amplitude = 0.28f)

            SoundType.COMBO_5 -> synthArpeggio(notes = floatArrayOf(659.25f, 880f), noteDuration = 0.04f, amplitude = 0.22f)
            SoundType.COMBO_10 -> synthArpeggio(notes = floatArrayOf(659.25f, 880f, 1108.73f), noteDuration = 0.04f, amplitude = 0.24f)
            SoundType.COMBO_25 -> synthArpeggio(notes = floatArrayOf(659.25f, 880f, 1108.73f, 1318.51f), noteDuration = 0.04f, amplitude = 0.26f)
            SoundType.COMBO_50 -> synthArpeggio(notes = floatArrayOf(659.25f, 880f, 1108.73f, 1318.51f, 1760f), noteDuration = 0.045f, amplitude = 0.28f)

            SoundType.EVOLUTION -> synthArpeggio(notes = floatArrayOf(261.63f, 392f, 523.25f, 659.25f, 783.99f, 1046.5f), noteDuration = 0.06f, amplitude = 0.3f)
            SoundType.ACHIEVEMENT -> synthArpeggio(notes = floatArrayOf(783.99f, 1046.5f, 1318.51f), noteDuration = 0.08f, amplitude = 0.28f)

            SoundType.CLICK -> synthPop(freq = 880f, durationSec = 0.025f, amplitude = 0.25f)
            SoundType.HOVER -> synthChime(frequencies = floatArrayOf(1318.51f, 1760f), durationSec = 0.025f, amplitude = 0.12f)
            SoundType.PAUSE -> synthPop(freq = 440f, durationSec = 0.03f, amplitude = 0.2f)
        }

        return createWavHeader(samples, SAMPLE_RATE)
    }

    /** Smooth Attack-Release Envelope to avoid clicks */
    private fun applyEnvelope(t: Float, durationSec: Float, attackSec: Float = 0.005f): Float {
        if (t < 0f || t > durationSec) return 0f
        val attack = if (attackSec > 0f && t < attackSec) (t / attackSec) else 1f
        val releaseRatio = ((durationSec - t) / 0.015f).coerceIn(0f, 1f)
        val decay = exp(-t * (4f / durationSec))
        return attack * releaseRatio * decay
    }

    private fun synthSweep(startFreq: Float, endFreq: Float, durationSec: Float, amplitude: Float): ShortArray {
        val numSamples = (durationSec * SAMPLE_RATE).toInt()
        val samples = ShortArray(numSamples)
        var phase = 0.0

        for (i in 0 until numSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            val progress = (t / durationSec).coerceIn(0f, 1f)
            val currentFreq = startFreq + (endFreq - startFreq) * progress

            phase += 2.0 * Math.PI * currentFreq / SAMPLE_RATE
            val sampleVal = (sin(phase) + 0.25 * sin(phase * 2.0)).toFloat()
            val env = applyEnvelope(t, durationSec, attackSec = 0.003f)

            samples[i] = (sampleVal * amplitude * env * Short.MAX_VALUE).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return samples
    }

    private fun synthWhoosh(startFreq: Float, endFreq: Float, durationSec: Float, noiseMix: Float, amplitude: Float): ShortArray {
        val numSamples = (durationSec * SAMPLE_RATE).toInt()
        val samples = ShortArray(numSamples)
        val noise = SimpleNoise(12345L)
        var phase = 0.0

        for (i in 0 until numSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            val progress = (t / durationSec).coerceIn(0f, 1f)
            val currentFreq = startFreq + (endFreq - startFreq) * (progress * progress)

            phase += 2.0 * Math.PI * currentFreq / SAMPLE_RATE
            val tone = sin(phase).toFloat()
            val n = noise.nextFloat()
            val mixed = tone * (1f - noiseMix) + n * noiseMix

            val env = applyEnvelope(t, durationSec, attackSec = 0.008f)
            samples[i] = (mixed * amplitude * env * Short.MAX_VALUE).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return samples
    }

    private fun synthChime(frequencies: FloatArray, durationSec: Float, amplitude: Float): ShortArray {
        val numSamples = (durationSec * SAMPLE_RATE).toInt()
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            var sampleVal = 0f
            for (freq in frequencies) {
                sampleVal += sin(2.0 * Math.PI * freq * t).toFloat()
            }
            sampleVal /= frequencies.size
            val env = applyEnvelope(t, durationSec, attackSec = 0.002f)

            samples[i] = (sampleVal * amplitude * env * Short.MAX_VALUE).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return samples
    }

    private fun synthZap(freq: Float, durationSec: Float, noiseMix: Float, amplitude: Float): ShortArray {
        val numSamples = (durationSec * SAMPLE_RATE).toInt()
        val samples = ShortArray(numSamples)
        val noise = SimpleNoise(67890L)

        for (i in 0 until numSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            val mod = sin(2.0 * Math.PI * 60.0 * t).toFloat()
            val tone = sin(2.0 * Math.PI * (freq + mod * 120f) * t).toFloat()
            val n = noise.nextFloat()
            val mixed = tone * (1f - noiseMix) + n * noiseMix

            val env = applyEnvelope(t, durationSec, attackSec = 0.002f)
            samples[i] = (mixed * amplitude * env * Short.MAX_VALUE).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return samples
    }

    private fun synthBubble(baseFreq: Float, durationSec: Float, amplitude: Float): ShortArray {
        val numSamples = (durationSec * SAMPLE_RATE).toInt()
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            val pitchMod = sin(2.0 * Math.PI * 25.0 * t).toFloat() * 60f
            val freq = baseFreq + pitchMod + (t / durationSec) * 100f
            val sampleVal = sin(2.0 * Math.PI * freq * t).toFloat()

            val env = applyEnvelope(t, durationSec, attackSec = 0.005f)
            samples[i] = (sampleVal * amplitude * env * Short.MAX_VALUE).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return samples
    }

    private fun synthSwoosh(startFreq: Float, midFreq: Float, endFreq: Float, durationSec: Float, amplitude: Float): ShortArray {
        val numSamples = (durationSec * SAMPLE_RATE).toInt()
        val samples = ShortArray(numSamples)
        var phase = 0.0

        for (i in 0 until numSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            val p = t / durationSec
            val freq = if (p < 0.5f) {
                startFreq + (midFreq - startFreq) * (p * 2f)
            } else {
                midFreq + (endFreq - midFreq) * ((p - 0.5f) * 2f)
            }

            phase += 2.0 * Math.PI * freq / SAMPLE_RATE
            // Triangle wave
            val tri = (2.0 * abs(2.0 * (phase / (2.0 * Math.PI) % 1.0) - 1.0) - 1.0).toFloat()

            val env = applyEnvelope(t, durationSec, attackSec = 0.004f)
            samples[i] = (tri * amplitude * env * Short.MAX_VALUE).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return samples
    }

    private fun synthToneHarmonics(baseFreq: Float, durationSec: Float, amplitude: Float): ShortArray {
        val numSamples = (durationSec * SAMPLE_RATE).toInt()
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            val s1 = sin(2.0 * Math.PI * baseFreq * t).toFloat()
            val s2 = sin(2.0 * Math.PI * baseFreq * 2.0 * t).toFloat() * 0.35f
            val s3 = sin(2.0 * Math.PI * baseFreq * 3.0 * t).toFloat() * 0.15f
            val sampleVal = s1 + s2 + s3

            val env = applyEnvelope(t, durationSec, attackSec = 0.002f)
            samples[i] = (sampleVal * (amplitude * 0.7f) * env * Short.MAX_VALUE).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return samples
    }

    private fun synthImpact(freq: Float, noiseMix: Float, durationSec: Float, amplitude: Float, highBell: Float = 0f): ShortArray {
        val numSamples = (durationSec * SAMPLE_RATE).toInt()
        val samples = ShortArray(numSamples)
        val noise = SimpleNoise(54321L)
        var phase = 0.0

        for (i in 0 until numSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            val dropFreq = freq * exp(-t * 25f)

            phase += 2.0 * Math.PI * dropFreq / SAMPLE_RATE
            val tone = sin(phase).toFloat()
            val n = noise.nextFloat()
            var mixed = tone * (1f - noiseMix) + n * noiseMix

            if (highBell > 0f) {
                mixed += sin(2.0 * Math.PI * highBell * t).toFloat() * 0.3f
            }

            val env = applyEnvelope(t, durationSec, attackSec = 0.001f)
            samples[i] = (mixed * amplitude * env * Short.MAX_VALUE).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return samples
    }

    private fun synthArpeggio(notes: FloatArray, noteDuration: Float, amplitude: Float): ShortArray {
        val totalSec = noteDuration * notes.size + 0.05f
        val numSamples = (totalSec * SAMPLE_RATE).toInt()
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            var sampleVal = 0f

            for (idx in notes.indices) {
                val noteStart = idx * noteDuration
                val noteT = t - noteStart
                if (noteT >= 0f) {
                    val freq = notes[idx]
                    val tone = sin(2.0 * Math.PI * freq * noteT).toFloat()
                    val harm = sin(2.0 * Math.PI * freq * 2.0 * noteT).toFloat() * 0.25f
                    val env = applyEnvelope(noteT, totalSec - noteStart, attackSec = 0.003f)
                    sampleVal += (tone + harm) * env
                }
            }

            samples[i] = (sampleVal * amplitude * 0.6f * Short.MAX_VALUE).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return samples
    }

    private fun synthWarningHorn(freq1: Float, freq2: Float, durationSec: Float, amplitude: Float): ShortArray {
        val numSamples = (durationSec * SAMPLE_RATE).toInt()
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            val lfo = 1f + 0.08f * sin(2.0 * Math.PI * 8.0 * t).toFloat()
            val s1 = sin(2.0 * Math.PI * freq1 * lfo * t).toFloat()
            val s2 = sin(2.0 * Math.PI * freq2 * lfo * t).toFloat() * 0.6f
            val sampleVal = (s1 + s2) * 0.65f

            val env = applyEnvelope(t, durationSec, attackSec = 0.02f)
            samples[i] = (sampleVal * amplitude * env * Short.MAX_VALUE).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return samples
    }

    private fun synthPop(freq: Float, durationSec: Float, amplitude: Float): ShortArray {
        val numSamples = (durationSec * SAMPLE_RATE).toInt()
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            val dropFreq = freq * exp(-t * 80f)
            val sampleVal = sin(2.0 * Math.PI * dropFreq * t).toFloat()

            val env = applyEnvelope(t, durationSec, attackSec = 0.001f)
            samples[i] = (sampleVal * amplitude * env * Short.MAX_VALUE).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return samples
    }

    /** Multi-bar ambient chord soundtrack (Am -> F -> C -> G) */
    private fun generateBgMusicWav(): ByteArray {
        val durationSec = 16  // 16 second ambient loop
        val numSamples = durationSec * SAMPLE_RATE
        val samples = ShortArray(numSamples)

        // Chord definitions (Bass, Mid, High, Arp)
        data class ChordDef(val rootBass: Float, val pad1: Float, val pad2: Float, val pad3: Float, val arpNotes: FloatArray)

        val chordAm = ChordDef(55.0f, 110.0f, 130.81f, 164.81f, floatArrayOf(220f, 261.63f, 329.63f, 440f))
        val chordF  = ChordDef(43.65f, 87.31f, 110.0f, 130.81f, floatArrayOf(174.61f, 220f, 261.63f, 349.23f))
        val chordC  = ChordDef(65.41f, 130.81f, 164.81f, 196.0f, floatArrayOf(261.63f, 329.63f, 392f, 523.25f))
        val chordG  = ChordDef(49.0f, 98.0f, 123.47f, 146.83f, floatArrayOf(196f, 246.94f, 293.66f, 392f))

        val barSec = 4.0f

        for (i in 0 until numSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            val barIndex = (t / barSec).toInt().coerceIn(0, 3)
            val barT = t % barSec

            val chord = when (barIndex) {
                0 -> chordAm
                1 -> chordF
                2 -> chordC
                else -> chordG
            }

            // Smooth crossfade between bars
            val padEnvelope = sin(Math.PI * (barT / barSec)).toFloat()

            // Sub bass
            val bass = sin(2.0 * Math.PI * chord.rootBass * t).toFloat() * 0.15f

            // Soft pad chord
            val p1 = sin(2.0 * Math.PI * chord.pad1 * t).toFloat() * 0.08f
            val p2 = sin(2.0 * Math.PI * chord.pad2 * t).toFloat() * 0.07f
            val p3 = sin(2.0 * Math.PI * chord.pad3 * t).toFloat() * 0.06f

            // Slow ambient LFO filter/vibrato effect
            val lfo = 1.0f + 0.003f * sin(2.0 * Math.PI * 0.25 * t).toFloat()

            // Soft high arpeggio
            val arpIdx = ((barT / 0.5f).toInt()) % chord.arpNotes.size
            val arpFreq = chord.arpNotes[arpIdx]
            val arpSubT = barT % 0.5f
            val arpEnv = exp(-arpSubT * 6f)
            val arp = sin(2.0 * Math.PI * arpFreq * lfo * t).toFloat() * 0.04f * arpEnv

            val mix = (bass + (p1 + p2 + p3) * padEnvelope + arp) * 0.55f

            samples[i] = (mix * Short.MAX_VALUE).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        return createWavHeader(samples, SAMPLE_RATE)
    }

    private fun createWavHeader(samples: ShortArray, sampleRate: Int): ByteArray {
        val dataSize = samples.size * 2
        val buf = ByteBuffer.allocate(44 + dataSize).order(ByteOrder.LITTLE_ENDIAN)

        buf.put("RIFF".toByteArray())
        buf.putInt(36 + dataSize)
        buf.put("WAVE".toByteArray())

        buf.put("fmt ".toByteArray())
        buf.putInt(16)
        buf.putShort(1) // PCM
        buf.putShort(1) // Mono
        buf.putInt(sampleRate)
        buf.putInt(sampleRate * 2)
        buf.putShort(2) // Block align
        buf.putShort(16) // 16-bit

        buf.put("data".toByteArray())
        buf.putInt(dataSize)

        for (s in samples) {
            buf.putShort(s)
        }

        return buf.array()
    }
}
