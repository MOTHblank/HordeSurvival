package com.hordesurvival.game.weapon

import com.hordesurvival.game.upgrade.PassiveType

/**
 * Weapon evolutions — combine a weapon at max tier with a specific passive
 * to create a dramatically upgraded version.
 *
 * Inspired by Vampire Survivors weapon evolution system.
 */
enum class WeaponEvolution(
    val baseWeapon: WeaponType,
    val requiredPassive: PassiveType,
    val evolvedName: String,
    val evolvedDescription: String,
    val damageMultiplier: Float = 2.5f,
    val cooldownMultiplier: Float = 0.7f,
    val specialEffect: String = ""
) {
    HOLY_BIBLE(
        baseWeapon = WeaponType.MAGIC_MISSILE,
        requiredPassive = PassiveType.EMPTY_TOME,
        evolvedName = "Holy Bible",
        evolvedDescription = "Orbiting holy projectiles that seek enemies",
        damageMultiplier = 3f,
        specialEffect = "orbit_homing"
    ),
    HELLFIRE(
        baseWeapon = WeaponType.FIREBALL,
        requiredPassive = PassiveType.SPINACH,
        evolvedName = "Hellfire",
        evolvedDescription = "Massive explosions that leave burning ground",
        damageMultiplier = 3f,
        specialEffect = "burn_ground"
    ),
    BLIZZARD(
        baseWeapon = WeaponType.ICE_SHARD,
        requiredPassive = PassiveType.CROWN,
        evolvedName = "Blizzard",
        evolvedDescription = "Freezing storm that slows everything nearby",
        damageMultiplier = 2.5f,
        specialEffect = "freeze_aoe"
    ),
    THUNDERSTORM(
        baseWeapon = WeaponType.LIGHTNING_RING,
        requiredPassive = PassiveType.CLOVER,
        evolvedName = "Thunderstorm",
        evolvedDescription = "Chain lightning that jumps between enemies",
        damageMultiplier = 2.5f,
        specialEffect = "chain_lightning"
    ),
    PLAGUE(
        baseWeapon = WeaponType.POISON_CLOUD,
        requiredPassive = PassiveType.VAMPIRE,
        evolvedName = "Plague",
        evolvedDescription = "Poison clouds that heal you for damage dealt",
        damageMultiplier = 2f,
        specialEffect = "lifesteal_cloud"
    ),
    MEGABOOM(
        baseWeapon = WeaponType.BOOMERANG_DAGGER,
        requiredPassive = PassiveType.WINGS,
        evolvedName = "Megaboom",
        evolvedDescription = "Explosive boomerangs that hit everything",
        damageMultiplier = 3f,
        specialEffect = "explosive_return"
    ),
    AURORA(
        baseWeapon = WeaponType.ORBITING_SHIELD,
        requiredPassive = PassiveType.SHIELD,
        evolvedName = "Aurora",
        evolvedDescription = "Invincible orbiting shields that reflect all damage",
        damageMultiplier = 2f,
        specialEffect = "reflect_shield"
    ),
    JUDGMENT(
        baseWeapon = WeaponType.DIVINE_SPEAR,
        requiredPassive = PassiveType.HEART,
        evolvedName = "Judgment",
        evolvedDescription = "Spears that explode on crit, healing you",
        damageMultiplier = 3f,
        specialEffect = "crit_heal"
    );

    companion object {
        /**
         * Find an evolution for the given weapon if the required passive is at max tier.
         */
        fun findEvolution(weapon: WeaponType, passiveLevels: Map<String, Int>): WeaponEvolution? {
            return entries.find { evo ->
                evo.baseWeapon == weapon &&
                (passiveLevels[evo.requiredPassive.name] ?: 0) >= evo.requiredPassive.maxLevel
            }
        }

        /**
         * Check if a weapon can evolve (is at tier 5 and has matching passive at max).
         */
        fun canEvolve(weapon: WeaponType, weaponTier: Int, passiveLevels: Map<String, Int>): Boolean {
            if (weaponTier < 5) return false
            return findEvolution(weapon, passiveLevels) != null
        }
    }
}
