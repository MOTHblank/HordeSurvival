package com.hordesurvival.game.character

/**
 * Character class system — each class has unique stats and abilities.
 * Classes are selected at character creation and affect gameplay style.
 */
enum class CharacterClass(
    val displayName: String,
    val icon: String,
    val description: String,
    val hpMult: Float,
    val speedMult: Float,
    val mightMult: Float,
    val cooldownReduction: Float,
    val luckBonus: Float,
    val specialPassive: String,
    val startingPassives: Map<String, Int> = emptyMap()
) {
    WARRIOR(
        displayName = "Warrior", icon = "⚔️",
        description = "Tanky melee fighter. +40% HP, +20% might, -10% speed. Takes less damage.",
        hpMult = 1.4f, speedMult = 0.9f, mightMult = 1.2f,
        cooldownReduction = 0f, luckBonus = 0f,
        specialPassive = "THORNS: Reflects 15% melee damage back to attackers",
        startingPassives = mapOf("SHIELD" to 1, "HEART" to 1)
    ),
    RANGER(
        displayName = "Ranger", icon = "🏹",
        description = "Fast and deadly. +25% speed, +15% luck, -20% HP. Better drops.",
        hpMult = 0.8f, speedMult = 1.25f, mightMult = 1f,
        cooldownReduction = 0.05f, luckBonus = 0.15f,
        specialPassive = "SCAVENGER: +30% more loot from enemies",
        startingPassives = mapOf("WINGS" to 1, "CLOVER" to 1)
    ),
    MAGE(
        displayName = "Mage", icon = "🧙",
        description = "Arcane power. +30% area, -15% cooldown, -25% HP. Weapons hit harder.",
        hpMult = 0.75f, speedMult = 1f, mightMult = 1.1f,
        cooldownReduction = 0.15f, luckBonus = 0.05f,
        specialPassive = "ARCANE SURGE: Every 10th kill triggers AoE explosion",
        startingPassives = mapOf("EMPTY_TOME" to 1, "CROWN" to 1)
    ),
    SUMMONER(
        displayName = "Summoner", icon = "🐉",
        description = "Pet master. Companions deal +50% damage. Starts with a pet.",
        hpMult = 0.9f, speedMult = 1f, mightMult = 0.9f,
        cooldownReduction = 0.05f, luckBonus = 0.1f,
        specialPassive = "BEAST MASTER: Pets attack 40% faster, +50% damage",
        startingPassives = mapOf("GROWTH" to 1)
    ),
    NECROMANCER(
        displayName = "Necromancer", icon = "💀",
        description = "Death magic. Killed enemies rise as allies for 8s. Low HP.",
        hpMult = 0.65f, speedMult = 1.05f, mightMult = 1.15f,
        cooldownReduction = 0.1f, luckBonus = 0f,
        specialPassive = "UNDEAD ARMY: 25% chance killed enemy fights for you (8s)",
        startingPassives = mapOf("VAMPIRE" to 1)
    );

    fun applyToStats(baseHp: Float, baseSpeed: Float, baseMight: Float): Triple<Float, Float, Float> =
        Triple(baseHp * hpMult, baseSpeed * speedMult, baseMight * mightMult)
}
