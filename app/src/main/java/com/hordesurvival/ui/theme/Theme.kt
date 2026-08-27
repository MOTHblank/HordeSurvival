package com.hordesurvival.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Horde Survival color theme — cheerful light fantasy palette for children.
 * Features bright pastel accents and inviting magical tones.
 */
object HordeColors {
    // ── Primary ────────────────────────────────────────────────────
    val SkyBlue = Color(0xFF6BB6FF)
    val LightBlue = Color(0xFFA8D8EA)
    val Lavender = Color(0xFFB19CD9)
    val MintGreen = Color(0xFFAAE6BA)
    val WarmPeach = Color(0xFFFFDAC1)
    val SoftPink = Color(0xFFFFB7B2)
    val Cream = Color(0xFFFFF5E1)

    // ── Background ─────────────────────────────────────────────────
    val DarkBg = Color(0xFF1B173D)
    val DarkSurface = Color(0xFF252054)
    val DarkCard = Color(0xFF283670)

    // ── UI Elements ────────────────────────────────────────────────
    val ButtonPrimary = Color(0xFF6BB6FF)
    val ButtonSecondary = Color(0xFFB19CD9)
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFB0BEC5)
    val XpBarFill = Color(0xFFAAE6BA)
    val XpBarBg = Color(0xFF2D2A5A)
    val HpBarFill = Color(0xFFFFB7B2)
    val GoldColor = Color(0xFFFFD700)

    // ── Rarity Colors ──────────────────────────────────────────────
    val Common = Color(0xFFB0BEC5)
    val Rare = Color(0xFF6BB6FF)
    val Epic = Color(0xFFB19CD9)
    val Legendary = Color(0xFFFFD700)

    // ── Card Colors ────────────────────────────────────────────────
    val CardBg = Color(0xFF22204E)
    val CardBorder = Color(0xFF4A4A82)
}

object HordeTypography {
    val Title = TextStyle(
        fontSize = 32.sp,
        fontWeight = FontWeight.Black,
        color = Color.White
    )
    val Header = TextStyle(
        fontSize = 28.sp,
        fontWeight = FontWeight.Black,
        color = HordeColors.SkyBlue
    )
    val SubHeader = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = HordeColors.WarmPeach
    )
    val Body = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        color = Color.White
    )
    val Label = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = HordeColors.TextSecondary
    )
    val Value = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
    )
    val Button = TextStyle(
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
    )
}
