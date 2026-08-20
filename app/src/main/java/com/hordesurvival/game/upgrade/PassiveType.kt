package com.hordesurvival.game.upgrade

/**
 * Passive upgrades available during gameplay.
 * Each affects player stats multiplicatively or additively.
 */
enum class PassiveType(
    val displayName: String,
    val description: String,
    val maxLevel: Int,
    val icon: String
) {
    SPINACH(
        displayName = "Spinach",
        description = "+10% might per level",
        maxLevel = 5,
        icon = "spinach"
    ),
    EMPTY_TOME(
        displayName = "Empty Tome",
        description = "-8% cooldown per level",
        maxLevel = 5,
        icon = "tome"
    ),
    CROWN(
        displayName = "Crown",
        description = "+8% area per level",
        maxLevel = 5,
        icon = "crown"
    ),
    WINGS(
        displayName = "Wings",
        description = "+10% move speed per level",
        maxLevel = 5,
        icon = "wings"
    ),
    DUPLICATOR(
        displayName = "Duplicator",
        description = "+1 projectile count",
        maxLevel = 3,
        icon = "duplicator"
    ),
    SHIELD(
        displayName = "Shield",
        description = "+10% armor per level",
        maxLevel = 5,
        icon = "shield"
    ),
    HEART(
        displayName = "Heart",
        description = "+20 max HP per level",
        maxLevel = 5,
        icon = "heart"
    ),
    CLOVER(
        displayName = "Clover",
        description = "+5% luck per level",
        maxLevel = 5,
        icon = "clover"
    ),
    MAGNET(
        displayName = "Magnet",
        description = "+20% pickup range per level",
        maxLevel = 5,
        icon = "magnet"
    ),
    GROWTH(
        displayName = "Growth",
        description = "+10% XP gain per level",
        maxLevel = 5,
        icon = "growth"
    ),
    SPEEDSTER(
        displayName = "Speedster",
        description = "+5% attack speed per level",
        maxLevel = 5,
        icon = "speedster"
    ),
    VAMPIRE(
        displayName = "Vampire",
        description = "+0.5 HP/sec regeneration",
        maxLevel = 5,
        icon = "vampire"
    );

    /** Apply this passive's effect at the given level to the stat value */
    fun applyEffect(baseValue: Float, level: Int): Float {
        return when (this) {
            SPINACH -> baseValue * (1f + 0.10f * level)       // +10% might
            EMPTY_TOME -> baseValue * (1f - 0.08f * level)    // -8% cooldown
            CROWN -> baseValue * (1f + 0.08f * level)         // +8% area
            WINGS -> baseValue * (1f + 0.10f * level)         // +10% speed
            DUPLICATOR -> baseValue + level                    // +1 projectile
            SHIELD -> baseValue * (1f + 0.10f * level)        // +10% armor
            HEART -> baseValue + 20f * level                   // +20 HP
            CLOVER -> baseValue * (1f + 0.05f * level)        // +5% luck
            MAGNET -> baseValue * (1f + 0.20f * level)        // +20% range
            GROWTH -> baseValue * (1f + 0.10f * level)        // +10% XP
            SPEEDSTER -> baseValue * (1f + 0.05f * level)     // +5% attack speed
            VAMPIRE -> 0.5f * level                            // +0.5 HP/sec
        }
    }

    fun getDescription(level: Int): String {
        return when (this) {
            SPINACH -> "+${level * 10}% might"
            EMPTY_TOME -> "-${level * 8}% cooldown"
            CROWN -> "+${level * 8}% area"
            WINGS -> "+${level * 10}% move speed"
            DUPLICATOR -> "+$level projectile(s)"
            SHIELD -> "+${level * 10}% armor"
            HEART -> "+${level * 20} max HP"
            CLOVER -> "+${level * 5}% luck"
            MAGNET -> "+${level * 20}% pickup range"
            GROWTH -> "+${level * 10}% XP gain"
            SPEEDSTER -> "+${level * 5}% attack speed"
            VAMPIRE -> "+${level * 0.5f} HP/sec regen"
        }
    }
}
