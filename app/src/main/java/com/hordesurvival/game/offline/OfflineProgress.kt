package com.hordesurvival.game.offline

import android.content.Context
import android.content.SharedPreferences
import kotlin.math.min

/**
 * Offline progress system — earns gold while player is away.
 * Based on last run performance and time elapsed.
 */
object OfflineProgress {

    private const val PREFS_NAME = "offline_progress"
    private const val KEY_LAST_EXIT = "last_exit_time"
    private const val KEY_LAST_GOLD_PER_MIN = "last_gold_per_min"
    private const val KEY_LAST_LEVEL = "last_level"
    private const val KEY_ACCUMULATED = "accumulated_gold"

    private const val MAX_OFFLINE_HOURS = 12f
    private const val OFFLINE_EFFICIENCY = 0.3f  // 30% of online rate

    private var prefs: SharedPreferences? = null

    fun initialize(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Call when player finishes a run (death/quit).
     * Records performance metrics for offline calculation.
     */
    fun recordRun(goldEarned: Int, timeSurvivedSeconds: Float, level: Int) {
        val goldPerMin = if (timeSurvivedSeconds > 30f) {
            goldEarned / (timeSurvivedSeconds / 60f)
        } else 0f

        prefs?.edit()?.apply {
            putFloat(KEY_LAST_GOLD_PER_MIN, goldPerMin)
            putInt(KEY_LAST_LEVEL, level)
            putLong(KEY_LAST_EXIT, System.currentTimeMillis())
            apply()
        }
    }

    /**
     * Call when player returns to game.
     * Returns gold accumulated while away, or 0 if nothing to collect.
     */
    fun checkAndCollect(): OfflineReward {
        val p = prefs ?: return OfflineReward.EMPTY
        val lastExit = p.getLong(KEY_LAST_EXIT, 0L)
        if (lastExit == 0L) return OfflineReward.EMPTY

        val elapsed = (System.currentTimeMillis() - lastExit) / 1000f  // seconds
        val minElapsed = 60f  // minimum 1 minute away
        if (elapsed < minElapsed) return OfflineReward.EMPTY

        val goldPerMin = p.getFloat(KEY_LAST_GOLD_PER_MIN, 0f)
        val lastLevel = p.getInt(KEY_LAST_LEVEL, 1)
        if (goldPerMin <= 0f) return OfflineReward.EMPTY

        val hoursElapsed = (elapsed / 3600f).coerceAtMost(MAX_OFFLINE_HOURS)
        val minutesElapsed = hoursElapsed * 60f

        // Scale with level (higher level = better offline rate)
        val levelBonus = 1f + (lastLevel - 1) * 0.02f
        val rawGold = goldPerMin * minutesElapsed * OFFLINE_EFFICIENCY * levelBonus
        val accumulated = rawGold.toInt().coerceAtLeast(0)

        // Clear after collection
        p.edit().remove(KEY_LAST_EXIT).apply()

        return OfflineReward(
            gold = accumulated,
            hoursAway = hoursElapsed,
            ratePerHour = (goldPerMin * 60 * OFFLINE_EFFICIENCY * levelBonus).toInt()
        )
    }

    /**
     * Check if there's pending offline gold without collecting.
     */
    fun hasPendingReward(): Boolean {
        val p = prefs ?: return false
        val lastExit = p.getLong(KEY_LAST_EXIT, 0L)
        if (lastExit == 0L) return false
        val elapsed = (System.currentTimeMillis() - lastExit) / 1000f
        return elapsed >= 60f && p.getFloat(KEY_LAST_GOLD_PER_MIN, 0f) > 0f
    }

    fun reset() {
        prefs?.edit()?.clear()?.apply()
    }

    data class OfflineReward(
        val gold: Int,
        val hoursAway: Float,
        val ratePerHour: Int
    ) {
        companion object {
            val EMPTY = OfflineReward(0, 0f, 0)
        }

        fun isEmpty(): Boolean = gold <= 0
        fun formatTime(): String {
            val h = hoursAway.toInt()
            val m = ((hoursAway - h) * 60).toInt()
            return if (h > 0) "${h}h ${m}m" else "${m}m"
        }
    }
}
