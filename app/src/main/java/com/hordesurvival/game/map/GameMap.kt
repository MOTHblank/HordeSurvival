package com.hordesurvival.game.map

/**
 * Map system for Survival (infinite) mode.
 * Each map has unique background, hazards, and enemy modifiers.
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
    val particleColor: Long = 0xFFFFFFFF
) {
    companion object {
        val allMaps = listOf(
            GameMap(
                id = "grasslands", name = "Green Fields", icon = "🌿",
                description = "Peaceful grasslands. Standard difficulty. Great for beginners.",
                backgroundStyle = 0, unlockCost = 0, minLevel = 0
            ),
            GameMap(
                id = "starfield", name = "Star Field", icon = "✨",
                description = "Deep space. Enemies move 15% faster. +20% XP gain.",
                backgroundStyle = 1, unlockCost = 500, minLevel = 5,
                enemySpdMult = 1.15f, xpMult = 1.2f
            ),
            GameMap(
                id = "nebula", name = "Nebula Storm", icon = "🌌",
                description = "Cosmic nebula. Enemies have +30% HP. +50% gold drops.",
                backgroundStyle = 2, unlockCost = 1200, minLevel = 10,
                enemyHpMult = 1.3f, goldMult = 1.5f
            ),
            GameMap(
                id = "darkness", name = "Dark Realm", icon = "🌑",
                description = "Eternal darkness. Reduced visibility. Enemies deal +25% damage. 2x XP.",
                backgroundStyle = 4, unlockCost = 2500, minLevel = 20,
                enemyDmgMult = 1.25f, xpMult = 2f,
                ambientColor = 0xFF050508, particleColor = 0xFF444444
            ),
            GameMap(
                id = "lava", name = "Inferno Pits", icon = "🌋",
                description = "Volcanic wasteland. Lava hazards. Enemies spawn faster. +80% gold.",
                backgroundStyle = 3, unlockCost = 5000, minLevel = 30,
                spawnRateMult = 1.4f, goldMult = 1.8f,
                hazardType = MapHazardType.LAVA_STREAMS
            ),
            GameMap(
                id = "ice", name = "Frozen Tundra", icon = "❄️",
                description = "Icy plains. Player moves 10% slower. Enemies freeze on death (30% chance).",
                backgroundStyle = 4, unlockCost = 5000, minLevel = 30,
                hazardType = MapHazardType.ICE_PATCHES,
                ambientColor = 0xFF0A1525, particleColor = 0xFF88CCFF
            ),
            GameMap(
                id = "graveyard", name = "Cursed Graveyard", icon = "⚰️",
                description = "Undead rise. Enemies split on death. Double boss HP. Triple gold.",
                backgroundStyle = 4, unlockCost = 8000, minLevel = 40,
                enemyHpMult = 1.5f, goldMult = 3f,
                hazardType = MapHazardType.GRAVEYARD_FOG,
                ambientColor = 0xFF0A0A15, particleColor = 0xFF666688
            ),
            GameMap(
                id = "void", name = "The Void", icon = "🕳️",
                description = "Reality breaks. Random enemy modifiers each wave. 5x XP. Extreme danger.",
                backgroundStyle = 4, unlockCost = 15000, minLevel = 50,
                enemyHpMult = 2f, enemySpdMult = 1.3f, enemyDmgMult = 1.5f,
                xpMult = 5f, goldMult = 2f,
                hazardType = MapHazardType.VOID_RIFT,
                ambientColor = 0xFF000000, particleColor = 0xFF9900FF
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
