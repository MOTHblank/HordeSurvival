package com.hordesurvival.game.skin

/**
 * Skin system — character cosmetics with rarity tiers.
 * Unlocked via achievements, gold, or events.
 */
data class CharacterSkin(
    val id: String,
    val name: String,
    val characterId: Int,  // which character this skin is for
    val icon: String,
    val description: String,
    val rarity: Rarity,
    val colorPrimary: Long,
    val colorSecondary: Long,
    val particleColor: Long,
    val unlockCondition: UnlockCondition,
    val price: Int = 0  // gold price if purchasable
) {
    enum class Rarity(val label: String, val color: Long) {
        COMMON("Common", 0xFF9E9E9E),
        RARE("Rare", 0xFF2196F3),
        EPIC("Epic", 0xFF9C27B0),
        LEGENDARY("Legendary", 0xFFFF9800),
        MYTHIC("Mythic", 0xFFFF1744)
    }

    sealed class UnlockCondition {
        data class Achievement(val achievementId: String) : UnlockCondition()
        data class GoldPurchase(val cost: Int) : UnlockCondition()
        data class Level(val minLevel: Int) : UnlockCondition()
        data class Event(val eventName: String) : UnlockCondition()
        object Default : UnlockCondition()  // always available
    }

    companion object {
        val allSkins = listOf(
            // ── Default skins (free) ──
            CharacterSkin("default_warrior", "Standard", 0, "⚔️",
                "Default warrior look", Rarity.COMMON, 0xFF6BB6FF, 0xFF4A90D9, 0xFF6BB6FF,
                UnlockCondition.Default),
            CharacterSkin("default_ranger", "Standard", 1, "🏹",
                "Default ranger look", Rarity.COMMON, 0xFFAAE6BA, 0xFF7CC68D, 0xFFAAE6BA,
                UnlockCondition.Default),
            CharacterSkin("default_mage", "Standard", 2, "🧙",
                "Default mage look", Rarity.COMMON, 0xFFB19CD9, 0xFF8E7CC3, 0xFFB19CD9,
                UnlockCondition.Default),
            CharacterSkin("default_summoner", "Standard", 3, "🐉",
                "Default summoner look", Rarity.COMMON, 0xFFFFDAC1, 0xFFD4A88C, 0xFFFFDAC1,
                UnlockCondition.Default),
            CharacterSkin("default_necro", "Standard", 4, "💀",
                "Default necromancer look", Rarity.COMMON, 0xFFCE93D8, 0xFFAB7BC0, 0xFFCE93D8,
                UnlockCondition.Default),

            // ── Rare skins (gold) ──
            CharacterSkin("golden_warrior", "Golden Knight", 0, "👑",
                "Shining gold armor. +5% gold drops.", Rarity.RARE, 0xFFFFD700, 0xFFDAA520, 0xFFFFF8E1,
                UnlockCondition.GoldPurchase(2000), price = 2000),
            CharacterSkin("shadow_ranger", "Shadow Walker", 1, "🌑",
                "Dark assassin. +5% crit chance.", Rarity.RARE, 0xFF37474F, 0xFF263238, 0xFF78909C,
                UnlockCondition.GoldPurchase(2000), price = 2000),
            CharacterSkin("frost_mage", "Frost Mage", 2, "❄️",
                "Ice wizard. Weapons slow enemies 10%.", Rarity.RARE, 0xFF80DEEA, 0xFF4DD0E1, 0xFFE0F7FA,
                UnlockCondition.GoldPurchase(2000), price = 2000),

            // ── Epic skins (achievement) ──
            CharacterSkin("blood_warrior", "Blood Knight", 0, "🩸",
                "Blood-soaked armor. Lifesteal 3% of damage.", Rarity.EPIC, 0xFFFF1744, 0xFFD50000, 0xFFFF8A80,
                UnlockCondition.Achievement("kill_1000")),
            CharacterSkin("arcane_mage", "Arcane Master", 2, "✨",
                "Pure arcane energy. +10% area.", Rarity.EPIC, 0xFFE040FB, 0xFFAA00FF, 0xFFEA80FC,
                UnlockCondition.Achievement("reach_level_50")),
            CharacterSkin("dragon_summoner", "Dragon Lord", 3, "🐲",
                "Rides a dragon. Pets deal +25% damage.", Rarity.EPIC, 0xFFFF6E40, 0xFFDD2C00, 0xFFFFAB91,
                UnlockCondition.Achievement("combo_50")),

            // ── Legendary skins (high level) ──
            CharacterSkin("void_necro", "Void Walker", 4, "🕳️",
                "Between dimensions. +15% cooldown reduction.", Rarity.LEGENDARY, 0xFF9900FF, 0xFF6A00CC, 0xFFD1C4E9,
                UnlockCondition.Level(50)),
            CharacterSkin("celestial_warrior", "Celestial Guard", 0, "⭐",
                "Divine protector. +20% max HP.", Rarity.LEGENDARY, 0xFFFFF176, 0xFFFFD54F, 0xFFFFF9C4,
                UnlockCondition.Level(40)),

            // ── Mythic skins (special) ──
            CharacterSkin("phoenix_ranger", "Phoenix", 1, "🔥",
                "Reborn from ashes. Revive once per run with 50% HP.", Rarity.MYTHIC, 0xFFFF6D00, 0xFFE65100, 0xFFFFE0B2,
                UnlockCondition.Event("anniversary")),
            CharacterSkin("cosmic_mage", "Cosmic Entity", 2, "🌌",
                "Beyond reality. All stats +10%.", Rarity.MYTHIC, 0xFF00E5FF, 0xFF00B8D4, 0xFFE0F7FA,
                UnlockCondition.Event("cosmic"))
        )

        fun getSkinsForCharacter(characterId: Int): List<CharacterSkin> =
            allSkins.filter { it.characterId == characterId }

        fun getUnlockedSkins(unlockedIds: Set<String>): List<CharacterSkin> =
            allSkins.filter { it.id in unlockedIds || it.unlockCondition is UnlockCondition.Default }
    }
}
