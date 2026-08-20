package com.hordesurvival.game.enemy

/**
 * Enemy archetypes with base stats.
 * Stats scale with game time via wave manager.
 */
enum class EnemyType(
    val displayName: String,
    val baseHp: Float,
    val baseDamage: Float,
    val baseSpeed: Float,
    val xpValue: Float,
    val goldValue: Float,
    val contactCooldown: Float,
    val colorHex: Long,          // ARGB
    val shape: String,           // circle, rect, triangle, diamond
    val size: Float,
    val isRanged: Boolean = false,
    val canPhase: Boolean = false,
    val canSplit: Boolean = false,
    val canHeal: Boolean = false,
    val isBoss: Boolean = false,
    val spawnWeight: Float = 1f  // higher = more common
) {
    BASIC_DRONE(
        displayName = "Basic Drone",
        baseHp = 15f, baseDamage = 8f, baseSpeed = 60f,
        xpValue = 1f, goldValue = 0.5f, contactCooldown = 1f,
        colorHex = 0xFFB0BEC5, shape = "circle", size = 24f,
        spawnWeight = 10f
    ),
    FLYING_WISP(
        displayName = "Flying Wisp",
        baseHp = 8f, baseDamage = 5f, baseSpeed = 120f,
        xpValue = 2f, goldValue = 0.5f, contactCooldown = 0.8f,
        colorHex = 0xFFCE93D8, shape = "diamond", size = 18f,
        spawnWeight = 6f
    ),
    TANK_GOLEM(
        displayName = "Tank Golem",
        baseHp = 80f, baseDamage = 20f, baseSpeed = 30f,
        xpValue = 5f, goldValue = 1.5f, contactCooldown = 1.5f,
        colorHex = 0xFF8D6E63, shape = "rect", size = 40f,
        spawnWeight = 3f
    ),
    SHOOTER_TURRET(
        displayName = "Shooter Turret",
        baseHp = 25f, baseDamage = 12f, baseSpeed = 0f,
        xpValue = 3f, goldValue = 1f, contactCooldown = 999f,
        colorHex = 0xFFFFCC80, shape = "rect", size = 28f,
        isRanged = true, spawnWeight = 4f
    ),
    SWARM_BAT(
        displayName = "Swarm Bat",
        baseHp = 5f, baseDamage = 3f, baseSpeed = 150f,
        xpValue = 0.5f, goldValue = 0.2f, contactCooldown = 0.5f,
        colorHex = 0xFFCE93D8, shape = "circle", size = 12f,
        spawnWeight = 8f
    ),
    ELITE_KNIGHT(
        displayName = "Elite Knight",
        baseHp = 50f, baseDamage = 15f, baseSpeed = 70f,
        xpValue = 4f, goldValue = 1.5f, contactCooldown = 1f,
        colorHex = 0xFF90A4AE, shape = "rect", size = 32f,
        spawnWeight = 3f
    ),
    GHOST(
        displayName = "Ghost",
        baseHp = 20f, baseDamage = 10f, baseSpeed = 80f,
        xpValue = 3f, goldValue = 1f, contactCooldown = 0.8f,
        colorHex = 0xFFB39DDB, shape = "circle", size = 26f,
        canPhase = true, spawnWeight = 4f
    ),
    BOSS(
        displayName = "Boss",
        baseHp = 500f, baseDamage = 25f, baseSpeed = 40f,
        xpValue = 50f, goldValue = 10f, contactCooldown = 1f,
        colorHex = 0xFFFFAB91, shape = "star", size = 64f,
        isBoss = true, spawnWeight = 0f  // spawned by timer, not random
    ),
    SPLITTER(
        displayName = "Splitter",
        baseHp = 30f, baseDamage = 8f, baseSpeed = 50f,
        xpValue = 2f, goldValue = 1f, contactCooldown = 1f,
        colorHex = 0xFFA5D6A7, shape = "diamond", size = 30f,
        canSplit = true, spawnWeight = 4f
    ),
    HEALER(
        displayName = "Healer",
        baseHp = 20f, baseDamage = 5f, baseSpeed = 55f,
        xpValue = 3f, goldValue = 1f, contactCooldown = 1f,
        colorHex = 0xFFF48FB1, shape = "circle", size = 24f,
        canHeal = true, spawnWeight = 3f
    ),
    MAGE(
        displayName = "Mage",
        baseHp = 18f, baseDamage = 15f, baseSpeed = 40f,
        xpValue = 4f, goldValue = 1.5f, contactCooldown = 999f,
        colorHex = 0xFF7E57C2, shape = "diamond", size = 22f,
        isRanged = true, spawnWeight = 3f
    );

    companion object {
        /** Total weight for weighted random selection (excludes BOSS) */
        val totalSpawnWeight: Float by lazy {
            entries.filter { !it.isBoss }.sumOf { it.spawnWeight.toDouble() }.toFloat()
        }

        /** Pick a random enemy type weighted by spawnWeight */
        fun random(): EnemyType {
            var roll = (Math.random() * totalSpawnWeight).toFloat()
            for (type in entries) {
                if (type.isBoss) continue
                roll -= type.spawnWeight
                if (roll <= 0f) return type
            }
            return BASIC_DRONE
        }
    }
}
