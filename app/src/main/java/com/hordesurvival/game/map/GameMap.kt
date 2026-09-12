package com.hordesurvival.game.map

/**
 * Map system for Survival (infinite) mode.
 * Each map has unique background, hazards, and enemy modifiers.
 *
 * Visual color roles (keep consistent across all rendering code):
 *  - ambientColor  : fullscreen atmospheric tint (drawn at low alpha)
 *  - particleColor : floating ambient particles (fireflies, embers, snow, stardust...)
 *  - accentColor   : UI highlight color for this map (map-select cards, HUD accents)
 */
data class GameMap(
    val id: String,
    val name: String,
    val icon: String,
    val description: String,
    val backgroundStyle: Int,   // 0-4 maps to existing background styles
    val unlockCost: Int,        // gold to unlock (0 = free)
    val minLevel: Int = 0,      // player meta-level required
    val enemyHpMult: Float = 1f,
    val enemySpdMult: Float = 1f,
    val enemyDmgMult: Float = 1f,
    val spawnRateMult: Float = 1f,
    val xpMult: Float = 1f,
    val goldMult: Float = 1f,
    val hazardType: MapHazardType = MapHazardType.NONE,
    val ambientColor: Long = 0xFF0D0D2B,
    val particleColor: Long = 0xFFFFFFFF,
    val accentColor: Long = 0xFF90A4AE
) {
    companion object {
        val allMaps = listOf(
            GameMap(
                id = "grasslands", name = "Green Fields", icon = "🌿",
                description = "Peaceful grasslands. Standard difficulty. Great for beginners.",
                backgroundStyle = 0, unlockCost = 0, minLevel = 0,
                ambientColor = 0xFF0B1E10,   // deep forest green
                particleColor = 0xFFF5E6A3,  // fireflies
                accentColor = 0xFF66BB6A
            ),
            GameMap(
                id = "starfield", name = "Star Field", icon = "✨",
                description = "Deep space. Enemies move 15% faster. +20% XP gain.",
                backgroundStyle = 1, unlockCost = 500, minLevel = 5,
                enemySpdMult = 1.15f, xpMult = 1.2f,
                ambientColor = 0xFF050510,   // near-black navy
                particleColor = 0xFFFFFFFF,  // stars
                accentColor = 0xFF4FC3F7
            ),
            GameMap(
                id = "nebula", name = "Nebula Storm", icon = "🌌",
                description = "Cosmic nebula. Enemies have +30% HP. +50% gold drops.",
                backgroundStyle = 2, unlockCost = 1200, minLevel = 10,
                enemyHpMult = 1.3f, goldMult = 1.5f,
                ambientColor = 0xFF160A24,   // deep violet haze
                particleColor = 0xFFFF8AD4,  // pink stardust
                accentColor = 0xFFBA68C8
            ),
            GameMap(
                id = "darkness", name = "Dark Realm", icon = "🌑",
                description = "Eternal darkness. Reduced visibility. Enemies deal +25% damage. 2x XP.",
                backgroundStyle = 4, unlockCost = 2500, minLevel = 20,
                enemyDmgMult = 1.25f, xpMult = 2f,
                ambientColor = 0xFF050508,
                particleColor = 0xFF555560,  // drifting ash (brighter so it's actually visible)
                accentColor = 0xFF9E9E9E
            ),
            GameMap(
                id = "lava", name = "Inferno Pits", icon = "🌋",
                description = "Volcanic wasteland. Lava hazards. Enemies spawn faster. +80% gold.",
                backgroundStyle = 3, unlockCost = 5000, minLevel = 30,
                spawnRateMult = 1.4f, goldMult = 1.8f,
                hazardType = MapHazardType.LAVA_STREAMS,
                ambientColor = 0xFF1F0805,   // scorched red-brown (was default navy)
                particleColor = 0xFFFF8A3D,  // rising embers
                accentColor = 0xFFFF7043
            ),
            GameMap(
                id = "ice", name = "Frozen Tundra", icon = "❄️",
                description = "Icy plains. Player moves 10% slower. Enemies freeze on death (30% chance).",
                backgroundStyle = 4, unlockCost = 5000, minLevel = 30,
                hazardType = MapHazardType.ICE_PATCHES,
                ambientColor = 0xFF0A1525,
                particleColor = 0xFFB8DFFF,  // snow
                accentColor = 0xFF81D4FA
            ),
            GameMap(
                id = "graveyard", name = "Cursed Graveyard", icon = "⚰️",
                description = "Undead rise. Enemies split on death. Double boss HP. Triple gold.",
                backgroundStyle = 4, unlockCost = 8000, minLevel = 40,
                enemyHpMult = 1.5f, goldMult = 3f,
                hazardType = MapHazardType.GRAVEYARD_FOG,
                ambientColor = 0xFF0A0A15,
                particleColor = 0xFF9FAECB,  // ghost wisps
                accentColor = 0xFF90A4AE
            ),
            GameMap(
                id = "void", name = "The Void", icon = "🕳️",
                description = "Reality breaks. Random enemy modifiers each wave. 5x XP. Extreme danger.",
                backgroundStyle = 4, unlockCost = 15000, minLevel = 50,
                enemyHpMult = 2f, enemySpdMult = 1.3f, enemyDmgMult = 1.5f,
                xpMult = 5f, goldMult = 2f,
                hazardType = MapHazardType.VOID_RIFT,
                ambientColor = 0xFF04000A,   // black with a hint of violet (pure black flattens everything)
                particleColor = 0xFFB388FF,  // violet sparks
                accentColor = 0xFFA855F7
            )
        )

        fun getMap(id: String): GameMap = allMaps.find { it.id == id } ?: allMaps[0]

        fun getUnlockedMaps(unlockedIds: Set<String>): List<GameMap> =
            allMaps.filter { it.id in unlockedIds || it.unlockCost == 0 }
    }

    enum class MapHazardType {
        NONE, LAVA_STREAMS, ICE_PATCHES, GRAVEYARD_FOG, VOID_RIFT
    }
}