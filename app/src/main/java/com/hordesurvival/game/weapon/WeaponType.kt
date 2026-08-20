package com.hordesurvival.game.weapon

/**
 * All weapon types with their base stats and upgrade paths.
 * Each weapon has 5 upgrade tiers with distinct effects.
 */
enum class WeaponType(
    val displayName: String,
    val baseDamage: Float,
    val baseCooldown: Float,
    val baseProjectiles: Int,
    val baseArea: Float,
    val description: String
) {
    MAGIC_MISSILE(
        displayName = "Magic Missile",
        baseDamage = 8f,
        baseCooldown = 0.8f,
        baseProjectiles = 1,
        baseArea = 1f,
        description = "Homing projectiles that seek enemies"
    ),
    LIGHTNING_RING(
        displayName = "Lightning Ring",
        baseDamage = 12f,
        baseCooldown = 1.5f,
        baseProjectiles = 1,
        baseArea = 80f,
        description = "Circular AOE around the player"
    ),
    FIREBALL(
        displayName = "Fireball",
        baseDamage = 20f,
        baseCooldown = 2.0f,
        baseProjectiles = 1,
        baseArea = 40f,
        description = "Explodes on impact, burns enemies"
    ),
    ICE_SHARD(
        displayName = "Ice Shard",
        baseDamage = 10f,
        baseCooldown = 1.0f,
        baseProjectiles = 1,
        baseArea = 1f,
        description = "Piercing shot that slows enemies"
    ),
    POISON_CLOUD(
        displayName = "Poison Cloud",
        baseDamage = 5f,
        baseCooldown = 3.0f,
        baseProjectiles = 1,
        baseArea = 60f,
        description = "Spawns stationary damage zones"
    ),
    BOOMERANG_DAGGER(
        displayName = "Boomerang Dagger",
        baseDamage = 15f,
        baseCooldown = 1.2f,
        baseProjectiles = 1,
        baseArea = 1f,
        description = "Flies out and returns to player"
    ),
    ORBITING_SHIELD(
        displayName = "Orbiting Shield",
        baseDamage = 8f,
        baseCooldown = 0.1f,   // continuous orbit
        baseProjectiles = 1,
        baseArea = 60f,
        description = "Rotating protective barrier"
    ),
    DIVINE_SPEAR(
        displayName = "Divine Spear",
        baseDamage = 35f,
        baseCooldown = 2.5f,
        baseProjectiles = 1,
        baseArea = 1f,
        description = "Long-range, high damage, crit chance"
    );

    /** Get upgrade description for a specific tier */
    fun getUpgradeDescription(tier: Int): String {
        return when (this) {
            MAGIC_MISSILE -> when (tier) {
                1 -> "Fires 1 extra missile"
                2 -> "+20% damage"
                3 -> "Fires 2 extra missiles"
                4 -> "+50% homing strength"
                5 -> "Missiles explode on impact"
                else -> ""
            }
            LIGHTNING_RING -> when (tier) {
                1 -> "+20% radius"
                2 -> "+15% damage"
                3 -> "Strikes twice per tick"
                4 -> "+40% radius"
                5 -> "Chain lightning to 3 enemies"
                else -> ""
            }
            FIREBALL -> when (tier) {
                1 -> "+30% explosion radius"
                2 -> "+20% damage"
                3 -> "Burns enemies for 3s"
                4 -> "+50% explosion radius"
                5 -> "Meteor shower: 3 fireballs"
                else -> ""
            }
            ICE_SHARD -> when (tier) {
                1 -> "+1 pierce count"
                2 -> "+20% damage"
                3 -> "Freeze on hit (0.5s)"
                4 -> "+2 pierce, +30% slow"
                5 -> "Blizzard: shards in all directions"
                else -> ""
            }
            POISON_CLOUD -> when (tier) {
                1 -> "+1 cloud"
                2 -> "+2s duration"
                3 -> "+30% damage, larger clouds"
                4 -> "+2 clouds"
                5 -> "Clouds follow player"
                else -> ""
            }
            BOOMERANG_DAGGER -> when (tier) {
                1 -> "+30% flight distance"
                2 -> "+20% damage"
                3 -> "+50% return speed"
                4 -> "Spins: hits multiple enemies"
                5 -> "Triple boomerang"
                else -> ""
            }
            ORBITING_SHIELD -> when (tier) {
                1 -> "+1 orbiting shield"
                2 -> "+20% shield HP"
                3 -> "Shield reflects projectiles"
                4 -> "+1 more shield, +30% damage"
                5 -> "Explosive shield on break"
                else -> ""
            }
            DIVINE_SPEAR -> when (tier) {
                1 -> "+25% range"
                2 -> "+20% damage"
                3 -> "+15% crit chance"
                4 -> "Spear pierces all enemies"
                5 -> "Judgment: massive AOE on crit"
                else -> ""
            }
        }
    }
}
