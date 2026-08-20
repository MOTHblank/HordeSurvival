package com.hordesurvival.game.upgrade

import com.hordesurvival.game.weapon.WeaponType

/**
 * Defines all available upgrades: weapons and passives.
 * Handles random upgrade selection for level-up screens.
 */
object UpgradeManager {

    /**
     * Get 3 random upgrade options for the player's current state.
     * Mixes weapon upgrades, new weapons, and passive upgrades.
     */
    fun getUpgradeOptions(
        currentWeapons: List<WeaponType>,
        currentPassives: Map<String, Int>,
        playerLevel: Int
    ): List<UpgradeOption> {
        val options = mutableListOf<UpgradeOption>()
        val pool = buildUpgradePool(currentWeapons, currentPassives, playerLevel)

        // Shuffle and pick 3 unique options
        pool.shuffle()
        val seen = mutableSetOf<String>()

        for (option in pool) {
            if (options.size >= 3) break
            if (seen.add(option.id)) {
                options.add(option)
            }
        }

        // Fallback: if pool is small, fill with generic options
        while (options.size < 3) {
            options.add(UpgradeOption(
                id = "heal_${options.size}",
                name = "Healing",
                description = "Restore 20% max HP",
                icon = "heart",
                type = UpgradeType.HEAL,
                rarity = Rarity.COMMON
            ))
        }

        return options
    }

    private fun buildUpgradePool(
        currentWeapons: List<WeaponType>,
        currentPassives: Map<String, Int>,
        playerLevel: Int
    ): MutableList<UpgradeOption> {
        val pool = mutableListOf<UpgradeOption>()

        // ── Weapon tier upgrades ────────────────────────────────────
        for (weapon in currentWeapons) {
            val tier = getWeaponTier(weapon, currentPassives)
            if (tier < 5) {
                pool.add(UpgradeOption(
                    id = "${weapon.name}_tier${tier + 1}",
                    name = "${weapon.displayName} Lv${tier + 1}",
                    description = weapon.getUpgradeDescription(tier + 1),
                    icon = weapon.name.lowercase(),
                    type = UpgradeType.WEAPON_UPGRADE,
                    weaponType = weapon,
                    targetTier = tier + 1,
                    currentTier = tier,
                    rarity = if (tier >= 3) Rarity.EPIC else if (tier >= 2) Rarity.RARE else Rarity.COMMON
                ))
            }
        }

        // ── Weapon evolutions (weapon at tier 5 + matching passive at max) ──
        for (weapon in currentWeapons) {
            val tier = getWeaponTier(weapon, currentPassives)
            val evolution = com.hordesurvival.game.weapon.WeaponEvolution.findEvolution(weapon, currentPassives)
            if (evolution != null && tier >= 5) {
                pool.add(UpgradeOption(
                    id = "evolve_${weapon.name}",
                    name = "✦ ${evolution.evolvedName}",
                    description = evolution.evolvedDescription,
                    icon = weapon.name.lowercase(),
                    type = UpgradeType.WEAPON_UPGRADE,
                    weaponType = weapon,
                    targetTier = 6,  // tier 6 = evolved
                    currentTier = tier,
                    rarity = Rarity.LEGENDARY
                ))
            }
        }

        // ── New weapons (if player has room) ────────────────────────
        if (currentWeapons.size < 6) {
            for (weapon in WeaponType.entries) {
                if (weapon !in currentWeapons) {
                    pool.add(UpgradeOption(
                        id = "new_${weapon.name}",
                        name = weapon.displayName,
                        description = weapon.description,
                        icon = weapon.name.lowercase(),
                        type = UpgradeType.NEW_WEAPON,
                        weaponType = weapon,
                        rarity = Rarity.RARE
                    ))
                }
            }
        }

        // ── Passive upgrades ────────────────────────────────────────
        for (passive in PassiveType.entries) {
            val currentLevel = currentPassives[passive.name] ?: 0
            if (currentLevel < passive.maxLevel) {
                pool.add(UpgradeOption(
                    id = "${passive.name}_lv${currentLevel + 1}",
                    name = "${passive.displayName} Lv${currentLevel + 1}",
                    description = passive.getDescription(currentLevel + 1),
                    icon = passive.name.lowercase(),
                    type = UpgradeType.PASSIVE,
                    passiveType = passive,
                    targetTier = currentLevel + 1,
                    currentTier = currentLevel,
                    rarity = if (currentLevel >= 3) Rarity.RARE else Rarity.COMMON
                ))
            }
        }

        return pool
    }

    private fun getWeaponTier(weapon: WeaponType, passives: Map<String, Int>): Int {
        return passives["WPN_${weapon.name}"] ?: 1
    }
}

// ── Data Classes ────────────────────────────────────────────────────────

data class UpgradeOption(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val type: UpgradeType,
    val weaponType: WeaponType? = null,
    val passiveType: PassiveType? = null,
    val targetTier: Int = 1,
    val currentTier: Int = 0,
    val rarity: Rarity = Rarity.COMMON
)

enum class UpgradeType {
    WEAPON_UPGRADE,
    NEW_WEAPON,
    PASSIVE,
    HEAL
}

enum class Rarity(val colorHex: Long) {
    COMMON(0xFFB0BEC5),
    RARE(0xFF6BB6FF),
    EPIC(0xFFB19CD9),
    LEGENDARY(0xFFFFD700)
}
