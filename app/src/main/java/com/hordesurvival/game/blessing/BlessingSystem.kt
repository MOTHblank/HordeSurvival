package com.hordesurvival.game.blessing

/**
 * Blessing System — choose blessings between runs for passive bonuses.
 * Blessings cost gold and persist across runs (meta-progression).
 */
object BlessingSystem {

    enum class BlessingType {
        MIGHT,      // +10% damage per level
        VITALITY,   // +10% HP per level
        FORTUNE,    // +10% gold gain per level
        SWIFTNESS,  // +8% speed per level
        WISDOM,     // +15% XP gain per level
        RESILIENCE, // +5 armor per level
        REGEN,      // +0.3 HP/sec per level
        LUCK,       // +5% luck per level
        PERSISTENCE,// +1 extra life per run per level
        ARCANE      // +1 projectile per level
    }

    data class Blessing(
        val type: BlessingType,
        val name: String,
        val description: String,
        val icon: String,
        val maxLevel: Int = 10,
        val baseCost: Int = 100,
        val costMultiplier: Float = 1.5f
    )

    val allBlessings = listOf(
        Blessing(BlessingType.MIGHT, "Blessing of Might", "+10% weapon damage", "⚔️"),
        Blessing(BlessingType.VITALITY, "Blessing of Vitality", "+10% max HP", "❤️"),
        Blessing(BlessingType.FORTUNE, "Blessing of Fortune", "+10% gold from enemies", "💰"),
        Blessing(BlessingType.SWIFTNESS, "Blessing of Swiftness", "+8% move speed", "💨"),
        Blessing(BlessingType.WISDOM, "Blessing of Wisdom", "+15% XP gain", "📖"),
        Blessing(BlessingType.RESILIENCE, "Blessing of Resilience", "+5 armor", "🛡️"),
        Blessing(BlessingType.REGEN, "Blessing of Regeneration", "+0.3 HP/sec regen", "💚"),
        Blessing(BlessingType.LUCK, "Blessing of Luck", "+5% luck for better drops", "🍀"),
        Blessing(BlessingType.PERSISTENCE, "Blessing of Persistence", "+1 extra life per run", "💖"),
        Blessing(BlessingType.ARCANE, "Blessing of the Arcane", "+1 starting projectile", "✨")
    )

    fun getCost(blessing: Blessing, currentLevel: Int): Int {
        return (blessing.baseCost * Math.pow(blessing.costMultiplier.toDouble(), currentLevel.toDouble())).toInt()
    }

    fun getEffect(blessing: Blessing, level: Int): Float {
        return when (blessing.type) {
            BlessingType.MIGHT -> 0.03f * level       // +3% per level (was10%)
            BlessingType.VITALITY -> 0.05f * level     // +5% per level (was10%)
            BlessingType.FORTUNE -> 0.04f * level      // +4% per level (was10%)
            BlessingType.SWIFTNESS -> 0.03f * level    // +3% per level (was8%)
            BlessingType.WISDOM -> 0.05f * level       // +5% per level (was15%)
            BlessingType.RESILIENCE -> 2f * level      // +2 per level (was5)
            BlessingType.REGEN -> 0.1f * level         // +0.1 per level (was0.3)
            BlessingType.LUCK -> 0.02f * level         // +2% per level (was5%)
            BlessingType.PERSISTENCE -> 1f * level
            BlessingType.ARCANE -> 1f * level
        }
    }
}
