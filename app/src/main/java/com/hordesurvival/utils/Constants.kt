package com.hordesurvival.utils

import androidx.compose.ui.graphics.Color

/**
 * Game constants and configuration.
 * Central location for all tunable game parameters.
 */
object Constants {

    // ── Display ──────────────────────────────────────────────────────────
    const val GAME_WIDTH = 1280f
    const val GAME_HEIGHT = 720f
    const val TARGET_FPS = 60

    // ── Player ───────────────────────────────────────────────────────────
    const val PLAYER_BASE_SPEED = 200f
    const val PLAYER_BASE_HP = 150f
    const val PLAYER_BASE_ARMOR = 0f
    const val PLAYER_BASE_PICKUP_RANGE = 50f
    const val PLAYER_SIZE = 32f
    const val PLAYER_INVINCIBILITY_TIME = 0.5f  // seconds after being hit

    // ── Enemies ──────────────────────────────────────────────────────────
    const val ENEMY_SPAWN_DISTANCE = 400f       // spawn outside camera view
    const val MAX_ENEMIES_ON_SCREEN = 300  // reduced for performance on low-end devices
    const val ENEMY_POOL_INITIAL_SIZE = 200
    const val BOSS_INTERVAL_SECONDS = 300f      // every 5 minutes

    // ── Waves ────────────────────────────────────────────────────────────
    const val WAVE_BASE_INTERVAL = 2.0f         // seconds between spawn waves
    const val WAVE_MIN_INTERVAL = 0.2f          // fastest spawn rate
    const val WAVE_QUADRATIC_FACTOR = 0.001f    // unused (kept for compat), actual scaling uses sqrt

    // ── XP & Leveling ───────────────────────────────────────────────────
    const val XP_BASE_REQUIREMENT = 10f
    const val XP_GROWTH_RATE = 1.15f            // exponential growth per level
    const val MAX_LEVEL = 100
    const val UPGRADE_CHOICES_COUNT = 3

    // ── Weapons ──────────────────────────────────────────────────────────
    const val WEAPON_MAX_TIER = 5

    // ── XP Gems ──────────────────────────────────────────────────────────
    const val GXP_MAGNET_SPEED = 500f           // how fast gems fly to player
    const val GXP_LIFETIME = 30f                // seconds before despawn

    // ── Colors (Pastel Relaxing Palette) ────────────────────────────────
    val COLOR_SKY_BLUE = Color(0xFF6BB6FF)
    val COLOR_LIGHT_BLUE = Color(0xFFA8D8EA)
    val COLOR_LAVENDER = Color(0xFFB19CD9)
    val COLOR_MINT_GREEN = Color(0xFFAAE6BA)
    val COLOR_WARM_PEACH = Color(0xFFFFDAC1)
    val COLOR_SOFT_PINK = Color(0xFFFFB7B2)
    val COLOR_CREAM = Color(0xFFFFF5E1)
    val COLOR_PLAYER = Color(0xFF6BB6FF)

    // Enemy colors by type
    val COLOR_ENEMY_BASIC = Color(0xFFB0BEC5)
    val COLOR_ENEMY_WISP = Color(0xFFCE93D8)
    val COLOR_ENEMY_TANK = Color(0xFF8D6E63)
    val COLOR_ENEMY_TURRET = Color(0xFFFFCC80)
    val COLOR_ENEMY_SWARM = Color(0xFFEF5350)
    val COLOR_ENEMY_KNIGHT = Color(0xFF90A4AE)
    val COLOR_ENEMY_GHOST = Color(0xFFB39DDB)
    val COLOR_ENEMY_BOSS = Color(0xFFFFAB91)
    val COLOR_ENEMY_SPLITTER = Color(0xFFA5D6A7)
    val COLOR_ENEMY_HEALER = Color(0xFFF48FB1)

    // ── Meta Progression ────────────────────────────────────────────────
    // ── Graphics Quality ────────────────────────────────────────
    const val QUALITY_LOW_MAX_ENEMIES = 80
    const val QUALITY_MED_MAX_ENEMIES = 150
    const val QUALITY_HIGH_MAX_ENEMIES = 300

    const val GOLD_PER_KILL_BASE = 1f
    const val META_HP_UPGRADE_AMOUNT = 0.05f    // +5% per purchase
    const val META_GOLD_UPGRADE_AMOUNT = 0.05f
    const val META_MAX_UPGRADE_LEVEL = 20

    // ── Timing ───────────────────────────────────────────────────────────
    const val LEVEL_UP_PAUSE_DURATION = 0f      // 0 = manual dismiss
}
