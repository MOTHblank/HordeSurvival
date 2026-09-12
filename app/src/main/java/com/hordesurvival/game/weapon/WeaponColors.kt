package com.hordesurvival.game.weapon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.hordesurvival.ui.theme.HordeColors

/**
 * Single source of truth for per-weapon colors — sprite bodies, hit particles,
 * and trails. Previously duplicated across WeaponSystem.getWeaponColor,
 * CollisionSystem.getWeaponHitColor, and GameRenderer's trail colors.
 */
object WeaponColors {
    val MagicMissile: Color = HordeColors.SkyBlue
    val Lightning: Color = Color(0xFF80DEEA)
    val Fireball: Color = Color(0xFFFFCC80)
    val IceShard: Color = Color(0xFF80CBC4)
    val Poison: Color = HordeColors.MintGreen
    val Boomerang: Color = HordeColors.WarmPeach
    val OrbitShield: Color = HordeColors.Lavender
    val DivineSpear: Color = HordeColors.Cream

    fun color(type: WeaponType): Color = when (type) {
        WeaponType.MAGIC_MISSILE -> MagicMissile
        WeaponType.LIGHTNING_RING -> Lightning
        WeaponType.FIREBALL -> Fireball
        WeaponType.ICE_SHARD -> IceShard
        WeaponType.POISON_CLOUD -> Poison
        WeaponType.BOOMERANG_DAGGER -> Boomerang
        WeaponType.ORBITING_SHIELD -> OrbitShield
        WeaponType.DIVINE_SPEAR -> DivineSpear
    }

    fun argb(type: WeaponType): Int = color(type).toArgb()
}