package com.hordesurvival.game.combo

/**
 * Combo Visual System — big combo counter display + screen effects.
 * Shows combo count prominently with color-coded tiers.
 */
object ComboVisual {

    enum class ComboTier(
        val minCombo: Int,
        val label: String,
        val color: Long,
        val flashColor: Long,
        val fontSize: Float,
        val screenShake: Float
    ) {
        BRONZE(0, "", 0xFFFFFFFF, 0x00FFFFFF, 24f, 0f),
        SILVER(10, "NICE!", 0xFFC0C0C0, 0x20C0C0C0, 28f, 2f),
        GOLD(25, "GREAT!", 0xFFFFD700, 0x30FFD700, 34f, 4f),
        PLATINUM(50, "AMAZING!", 0xFF00E5FF, 0x4000E5FF, 40f, 6f),
        DIAMOND(100, "LEGENDARY!", 0xFFE040FB, 0x50E040FB, 48f, 8f),
        GODLIKE(200, "GODLIKE!", 0xFFFF1744, 0x60FF1744, 56f, 10f)
    }

    data class ComboDisplay(
        val count: Int,
        val tier: ComboTier,
        val multiplier: Float,
        val label: String,
        val color: Long,
        val fontSize: Float,
        val screenShake: Float,
        val flashAlpha: Float,
        val bounceScale: Float = 1f,     // for bounce animation
        val edgeGlowAlpha: Float = 0f    // for screen edge glow
    )

    fun getComboDisplay(count: Int, multiplier: Float): ComboDisplay {
        val tier = when {
            count >= 200 -> ComboTier.GODLIKE
            count >= 100 -> ComboTier.DIAMOND
            count >= 50 -> ComboTier.PLATINUM
            count >= 25 -> ComboTier.GOLD
            count >= 10 -> ComboTier.SILVER
            else -> ComboTier.BRONZE
        }

        return ComboDisplay(
            count = count,
            tier = tier,
            multiplier = multiplier,
            label = tier.label,
            color = tier.color,
            fontSize = tier.fontSize,
            screenShake = tier.screenShake,
            flashAlpha = if (count >= 10) (0.1f + (count / 200f) * 0.3f).coerceAtMost(0.4f) else 0f,
            bounceScale = if (count >= 10) 1f + (count / 500f).coerceAtMost(0.3f) else 1f,
            edgeGlowAlpha = if (count >= 25) (0.05f + (count / 400f) * 0.15f).coerceAtMost(0.2f) else 0f
        )
    }

    /** Get color for combo count */
    fun getComboColor(count: Int): Long {
        return when {
            count >= 200 -> 0xFFFF1744
            count >= 100 -> 0xFFE040FB
            count >= 50 -> 0xFF00E5FF
            count >= 25 -> 0xFFFFD700
            count >= 10 -> 0xFFC0C0C0
            else -> 0xFFFFFFFF
        }
    }
}
