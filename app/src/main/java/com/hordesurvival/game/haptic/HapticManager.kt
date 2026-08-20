package com.hordesurvival.game.haptic

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Haptic feedback manager — vibration patterns for game events.
 * Requires VIBRATE permission in AndroidManifest.
 */
object HapticManager {

    private var vibrator: Vibrator? = null
    var isEnabled = true

    fun initialize(context: Context) {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun release() {
        vibrator = null
    }

    // ── Event-based haptics ───────────────────────────────────────

    fun hit() {
        if (!isEnabled) return
        vibrate(15, 40)
    }

    fun kill() {
        if (!isEnabled) return
        vibrate(25, 80)
    }

    fun playerDamage() {
        if (!isEnabled) return
        vibrate(40, 120)
    }

    fun levelUp() {
        if (!isEnabled) return
        // Rising pattern
        pattern(longArrayOf(0, 30, 50, 30, 50, 50, 80), intArrayOf(0, 60, 0, 100, 0, 150, 200))
    }

    fun bossWarning() {
        if (!isEnabled) return
        // Heavy double-pulse
        pattern(longArrayOf(0, 80, 100, 80), intArrayOf(0, 200, 0, 200))
    }

    fun bossKill() {
        if (!isEnabled) return
        // Long satisfying pulse
        vibrate(200, 255)
    }

    fun death() {
        if (!isEnabled) return
        // Long fading vibration
        pattern(longArrayOf(0, 100, 50, 150, 50, 200), intArrayOf(0, 255, 0, 180, 0, 100))
    }

    fun collectGem() {
        if (!isEnabled) return
        vibrate(8, 30)
    }

    fun weaponFire() {
        if (!isEnabled) return
        vibrate(5, 20)
    }

    fun shopPurchase() {
        if (!isEnabled) return
        pattern(longArrayOf(0, 20, 40, 20), intArrayOf(0, 80, 0, 120))
    }

    fun combo(multiplier: Int) {
        if (!isEnabled) return
        val amp = (30 + multiplier * 10).coerceAtMost(200)
        vibrate(15, amp)
    }

    // ── Low-level ─────────────────────────────────────────────────

    private fun vibrate(durationMs: Long, amplitude: Int) {
        val v = vibrator ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val safeAmp = amplitude.coerceIn(1, 255)
            v.vibrate(VibrationEffect.createOneShot(durationMs, safeAmp))
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(durationMs)
        }
    }

    private fun pattern(timings: LongArray, amplitudes: IntArray) {
        val v = vibrator ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val safeAmps = amplitudes.map { it.coerceIn(0, 255) }.toIntArray()
            v.vibrate(VibrationEffect.createWaveform(timings, safeAmps, -1))
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(timings, -1)
        }
    }
}
