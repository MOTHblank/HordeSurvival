package com.hordesurvival.game.synergy

import com.hordesurvival.game.weapon.WeaponType

/**
 * Weapon Synergy System — bonus effects when specific weapon combos are equipped.
 * Players with matching weapon pairs get powerful passive bonuses.
 */
object WeaponSynergy {

    data class Synergy(
        val id: String,
        val name: String,
        val description: String,
        val requiredWeapons: Set<WeaponType>,
        val bonusMight: Float = 0f,
        val bonusSpeed: Float = 0f,
        val bonusCooldownReduction: Float = 0f,
        val bonusArea: Float = 0f,
        val bonusProjectile: Int = 0,
        val bonusRegen: Float = 0f,
        val bonusLuck: Float = 0f
    )

    val allSynergies = listOf(
        Synergy(
            id = "elemental_mastery",
            name = "Elemental Mastery",
            description = "Fire + Ice: +25% damage, enemies burn and freeze simultaneously",
            requiredWeapons = setOf(WeaponType.FIREBALL, WeaponType.ICE_SHARD),
            bonusMight = 0.25f
        ),
        Synergy(
            id = "storm_caller",
            name = "Storm Caller",
            description = "Lightning + Ice: +30% area, lightning chains to frozen enemies",
            requiredWeapons = setOf(WeaponType.LIGHTNING_RING, WeaponType.ICE_SHARD),
            bonusArea = 0.3f
        ),
        Synergy(
            id = "poison_plague",
            name = "Poison Plague",
            description = "Poison + Fire: poison clouds ignite, dealing burn damage",
            requiredWeapons = setOf(WeaponType.POISON_CLOUD, WeaponType.FIREBALL),
            bonusMight = 0.2f,
            bonusCooldownReduction = 0.1f
        ),
        Synergy(
            id = "holy_knight",
            name = "Holy Knight",
            description = "Shield + Spear: +2 projectiles, orbiting shields reflect projectiles",
            requiredWeapons = setOf(WeaponType.ORBITING_SHIELD, WeaponType.DIVINE_SPEAR),
            bonusProjectile = 2
        ),
        Synergy(
            id = "shadow_dancer",
            name = "Shadow Dancer",
            description = "Boomerang + Missile: +20% speed, projectiles home stronger",
            requiredWeapons = setOf(WeaponType.BOOMERANG_DAGGER, WeaponType.MAGIC_MISSILE),
            bonusSpeed = 0.2f
        ),
        Synergy(
            id = "nature_wrath",
            name = "Nature's Wrath",
            description = "Poison + Lightning: +0.5 HP regen, poison chains between enemies",
            requiredWeapons = setOf(WeaponType.POISON_CLOUD, WeaponType.LIGHTNING_RING),
            bonusRegen = 0.5f
        ),
        Synergy(
            id = "void_master",
            name = "Void Master",
            description = "Spear + Boomerang: +15% luck, projectiles pierce all enemies",
            requiredWeapons = setOf(WeaponType.DIVINE_SPEAR, WeaponType.BOOMERANG_DAGGER),
            bonusLuck = 0.15f
        ),
        Synergy(
            id = "arcane_trinity",
            name = "Arcane Trinity",
            description = "Missile + Fire + Ice: +50% might, all weapons evolve faster",
            requiredWeapons = setOf(WeaponType.MAGIC_MISSILE, WeaponType.FIREBALL, WeaponType.ICE_SHARD),
            bonusMight = 0.5f
        )
    )

    /** Get all active synergies for the player's current weapon set */
    fun getActiveSynergies(currentWeapons: List<WeaponType>): List<Synergy> {
        val weaponSet = currentWeapons.toSet()
        return allSynergies.filter { synergy ->
            synergy.requiredWeapons.all { it in weaponSet }
        }
    }

    /** Calculate total synergy bonuses */
    fun calculateBonuses(currentWeapons: List<WeaponType>): SynergyBonuses {
        val active = getActiveSynergies(currentWeapons)
        return SynergyBonuses(
            bonusMight = active.sumOf { it.bonusMight.toDouble() }.toFloat(),
            bonusSpeed = active.sumOf { it.bonusSpeed.toDouble() }.toFloat(),
            bonusCooldownReduction = active.sumOf { it.bonusCooldownReduction.toDouble() }.toFloat(),
            bonusArea = active.sumOf { it.bonusArea.toDouble() }.toFloat(),
            bonusProjectile = active.sumOf { it.bonusProjectile },
            bonusRegen = active.sumOf { it.bonusRegen.toDouble() }.toFloat(),
            bonusLuck = active.sumOf { it.bonusLuck.toDouble() }.toFloat(),
            activeSynergyNames = active.map { it.name }
        )
    }

    data class SynergyBonuses(
        val bonusMight: Float = 0f,
        val bonusSpeed: Float = 0f,
        val bonusCooldownReduction: Float = 0f,
        val bonusArea: Float = 0f,
        val bonusProjectile: Int = 0,
        val bonusRegen: Float = 0f,
        val bonusLuck: Float = 0f,
        val activeSynergyNames: List<String> = emptyList()
    )
}
