package com.hordesurvival.ui.screens.game

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import com.hordesurvival.ui.components.hordeInteractive
import androidx.compose.foundation.layout.*

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hordesurvival.game.weapon.WeaponType
import com.hordesurvival.ui.theme.HordeColors
import com.hordesurvival.ui.theme.HordeTypography
import com.hordesurvival.ui.components.CornerCutShape
import com.hordesurvival.ui.components.HordeProgressBar
import com.hordesurvival.ui.components.HordeSmallButton
import com.hordesurvival.ui.components.SmallCutShape
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.sin

/**
 * In-game HUD: HP bar, XP bar, stats, weapon loadout with tiers,
 * combo counter, boss HP bar, passive indicators, pause button.
 */
@Composable
fun GameHud(
    playerHp: Float,
    playerMaxHp: Float,
    playerLevel: Int,
    currentXp: Float,
    xpToNext: Float,
    gold: Float,
    kills: Int,
    gameTime: Float,
    currentWeapons: List<WeaponType> = emptyList(),
    comboCount: Int = 0,
    comboMultiplier: Float = 1f,
    bossHp: Float = 0f,
    bossMaxHp: Float = 0f,
    bossActive: Boolean = false,
    abilityReady: Boolean = true,
    abilityCooldown: Float = 0f,
    abilityType: String = "",
    showComboCounter: Boolean = true,
    gameSpeed: Float = 1f,
    onPauseClick: () -> Unit = {},
    onAbilityClick: () -> Unit = {},
    onSpeedChange: (Float) -> Unit = {}
) {
    val hpRatio = (playerHp / playerMaxHp).coerceIn(0f, 1f)
    val xpRatio = (currentXp / xpToNext).coerceIn(0f, 1f)
    val hpColor = when {
        hpRatio > 0.6f -> HordeColors.MintGreen
        hpRatio > 0.3f -> HordeColors.WarmPeach
        else -> HordeColors.SoftPink
    }

    // Force LTR layout so button positions stay fixed regardless of system language
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Low HP warning - soft warm coral pulse
        if (hpRatio < 0.25f) {
            val pulseAlpha by animateFloatAsState(
                targetValue = 0.15f + 0.1f * sin(System.currentTimeMillis() / 200f).toFloat(),
                label = "hp_pulse"
            )
            Box(Modifier.fillMaxSize().background(HordeColors.WarmPeach.copy(alpha = pulseAlpha)))
        }

        // ── Top — full-width XP bar ─────────────────────────────
        HordeProgressBar(
            progress = xpRatio,
            modifier = Modifier.widthIn(max = 800.dp).fillMaxWidth().height(12.dp).align(Alignment.TopCenter),
            fillBrush = Brush.horizontalGradient(listOf(HordeColors.XpBarFill, HordeColors.SkyBlue.copy(alpha = 0.6f))),
            backgroundColor = HordeColors.DarkCard,
            shape = CornerCutShape
        ) {
            Text("Lv.$playerLevel", style = HordeTypography.Label, color = Color.White, modifier = Modifier.align(Alignment.Center))
        }

        // ── Top row container (Pause, HP, Stats) ─────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, top = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // ── Top left — pause + speed control ──
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Pause button
                HordeSmallButton(
                    text = "⏸",
                    onClick = onPauseClick,
                    modifier = Modifier.size(44.dp),
                    color = HordeColors.SkyBlue.copy(alpha = 0.5f)
                )

                Spacer(Modifier.height(6.dp))

                // Speed control buttons
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    listOf(0.5f to "½", 1f to "1", 2f to "2", 3f to "3").forEach { (speed, label) ->
                        val sel = gameSpeed == speed
                        Box(
                            modifier = Modifier.size(28.dp)
                                .clip(CornerCutShape)
                                .background(if (sel) HordeColors.SkyBlue.copy(alpha = 0.5f) else HordeColors.OverlayLight)
                                .hordeInteractive(onClick = { onSpeedChange(speed) }),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(label, style = HordeTypography.Label, color = if (sel) Color.White else Color.White.copy(alpha = 0.5f))
                        }
                    }
                }
            }

            // ── Top center — HP bar and Boss bar ────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, end = 12.dp)
            ) {
                // HP
                Row(Modifier.widthIn(max = 600.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("❤️", style = HordeTypography.Label)
                    Spacer(Modifier.width(4.dp))
                    HordeProgressBar(
                        progress = hpRatio,
                        modifier = Modifier.weight(1f).height(14.dp),
                        fillBrush = Brush.horizontalGradient(listOf(hpColor, hpColor.copy(alpha = 0.7f))),
                        backgroundColor = HordeColors.DarkCard,
                        borderColor = Color.White.copy(alpha = 0.1f),
                        shape = CornerCutShape
                    ) {
                        Text("${playerHp.toInt()} / ${playerMaxHp.toInt()}", style = HordeTypography.Label, color = Color.White.copy(alpha = 0.8f), modifier = Modifier.align(Alignment.Center))
                    }
                }

                // Boss HP bar (only when boss is active)
                if (bossActive && bossMaxHp > 0f) {
                    Spacer(Modifier.height(6.dp))
                    val bossRatio = (bossHp / bossMaxHp).coerceIn(0f, 1f)
                    Row(Modifier.widthIn(max = 600.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("👹", style = HordeTypography.Body)
                        Spacer(Modifier.width(4.dp))
                        HordeProgressBar(
                            progress = bossRatio,
                            modifier = Modifier.weight(1f).height(16.dp),
                            fillBrush = Brush.horizontalGradient(listOf(HordeColors.Warning, HordeColors.WarmPeach)),
                            backgroundColor = HordeColors.DarkCard,
                            borderColor = HordeColors.WarmPeach.copy(alpha = 0.4f),
                            shape = CornerCutShape
                        ) {
                            Text("BOSS ${bossHp.toInt()}", style = HordeTypography.Label, color = Color.White, modifier = Modifier.align(Alignment.Center))
                        }
                    }
                }
            }

            // ── Top right — stats ────────────────────────────────────
            Column(
                modifier = Modifier
                    .clip(CornerCutShape).background(HordeColors.OverlayLight)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.End
            ) {
                StatText("⏱", formatTime(gameTime), HordeColors.TextSecondary)
                StatText("💰", "${gold.toInt()}", HordeColors.GoldColor)
                StatText("👾", "$kills", HordeColors.MintGreen)
            }
        }

        // ── Combo counter (right side) ───────────────────────────
        if (comboCount >= 3 && showComboCounter) {
            val comboScale by animateFloatAsState(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = 0.4f, stiffness = 300f),
                label = "combo_scale"
            )
            Column(
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp)
                    .graphicsLayer(scaleX = comboScale, scaleY = comboScale),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val comboColor = when {
                    comboCount >= 50 -> HordeColors.GoldColor  // gold
                    comboCount >= 25 -> HordeColors.Warning  // orange
                    comboCount >= 10 -> HordeColors.WarmPeach  // peach
                    else -> HordeColors.WarmPeach              // light peach
                }
                Text(
                    "×$comboCount",
                    fontSize = (24 + comboCount.coerceAtMost(50) / 5).sp,
                    fontWeight = FontWeight.Black,
                    color = comboColor
                )
                Text(
                    "COMBO",
                    style = HordeTypography.Label,
                    color = comboColor.copy(alpha = 0.7f)
                )
                if (comboMultiplier > 1f) {
                    Text(
                        "+${((comboMultiplier - 1f) * 100).toInt()}% XP",
                        style = HordeTypography.Label,
                        color = HordeColors.MintGreen
                    )
                }
            }
        }

        // ── Bottom right — ability button ────────────────────────
        Box(
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 80.dp)
                .size(56.dp)
                .clip(CornerCutShape)
                .background(if (abilityReady) HordeColors.Success.copy(alpha = 0.6f) else Color.Gray.copy(alpha = 0.3f))
                .border(1.5.dp, if (abilityReady) HordeColors.Success else Color.Gray.copy(alpha = 0.5f), CornerCutShape)
                .hordeInteractive(enabled = abilityReady, onClick = onAbilityClick),
            contentAlignment = Alignment.Center
        ) {
            val emoji = when (abilityType) {
                "dash" -> "💨"
                "shield" -> "🛡️"
                "aoe_blast" -> "💥"
                "heal" -> "💚"
                "lightning_storm" -> "⚡"
                else -> "✨"
            }
            Text(emoji, style = HordeTypography.SubHeader)
            if (!abilityReady) {
                // Cooldown overlay
                Box(
                    Modifier.fillMaxSize().background(HordeColors.OverlayMedium.copy(alpha = 0.5f * abilityCooldown))
                )
                Text("${(abilityCooldown * 15).toInt()}s", style = HordeTypography.Label, color = Color.White.copy(alpha = 0.7f))
            }
        }

        // ── Bottom left — weapon loadout row ─────────────────────
        if (currentWeapons.isNotEmpty()) {
            Row(
                modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)
                    .clip(CornerCutShape).background(HordeColors.OverlayLight)
                    .padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                currentWeapons.forEach { weapon ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier.size(32.dp).clip(SmallCutShape).background(Color.White.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(weaponEmoji(weapon), style = HordeTypography.Value)
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(weapon.displayName, style = HordeTypography.Label, color = Color.White.copy(alpha = 0.8f))
                    }
                }
            }
        }
    }
    } // end CompositionLocalProvider LTR
}

@Composable
private fun StatText(icon: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(icon, style = HordeTypography.Label)
        Spacer(Modifier.width(4.dp))
        Text(value, style = HordeTypography.Label, color = color)
    }
}

private fun weaponEmoji(w: WeaponType): String = when (w) {
    WeaponType.MAGIC_MISSILE -> "🔮"
    WeaponType.LIGHTNING_RING -> "⚡"
    WeaponType.FIREBALL -> "🔥"
    WeaponType.ICE_SHARD -> "❄️"
    WeaponType.POISON_CLOUD -> "☠️"
    WeaponType.BOOMERANG_DAGGER -> "🗡️"
    WeaponType.ORBITING_SHIELD -> "🛡️"
    WeaponType.DIVINE_SPEAR -> "🔱"
}

/**
 * Boss warning banner.
 */
@Composable
fun BossWarningBanner(visible: Boolean, onDismiss: () -> Unit) {
    if (!visible) return
    val inf = rememberInfiniteTransition(label = "boss")
    val alpha by inf.animateFloat(0.6f, 1f, infiniteRepeatable(tween(500), RepeatMode.Reverse), label = "a")
    Box(
        Modifier.widthIn(max = 600.dp).fillMaxWidth()
            .background(Brush.horizontalGradient(listOf(HordeColors.Lavender.copy(alpha = alpha * 0.7f), HordeColors.SkyBlue.copy(alpha = alpha * 0.4f), HordeColors.Lavender.copy(alpha = alpha * 0.7f))))
            .hordeInteractive(onClick = onDismiss).padding(vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) { Text("⚠️ BOSS INCOMING ⚠️", style = HordeTypography.Header, color = Color.White) }
}

private fun formatTime(s: Float): String {
    val m = (s / 60).toInt(); val sec = (s % 60).toInt()
    return "%d:%02d".format(m, sec)
}
