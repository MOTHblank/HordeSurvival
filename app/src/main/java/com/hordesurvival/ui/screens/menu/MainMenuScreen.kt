package com.hordesurvival.ui.screens.menu

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hordesurvival.ui.Locales
import com.hordesurvival.ui.theme.HordeColors
import com.hordesurvival.ui.components.CornerCutShape
import com.hordesurvival.ui.components.SmallCutShape
import com.hordesurvival.ui.components.HordeButton
import com.hordesurvival.ui.components.HordeScreen
import com.hordesurvival.ui.components.HordeSecondaryButton

@Composable
fun MainMenuScreen(
    onPlayClick: () -> Unit,
    onCharactersClick: () -> Unit,
    onUpgradesClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onStatsClick: () -> Unit = {},
    onShopClick: () -> Unit = {},
    onTutorialClick: () -> Unit = {},
    gold: Int = 0,
    languageCode: String = "en"
) {
    val L = { key: String -> Locales.getString(key, languageCode) }

    // Smooth animations
    val inf = rememberInfiniteTransition(label = "menu")
    val glow by inf.animateFloat(0.5f, 1f, infiniteRepeatable(tween(2500, easing = EaseInOut), RepeatMode.Reverse), label = "glow")
    val breathe by inf.animateFloat(0.97f, 1.03f, infiniteRepeatable(tween(3000, easing = EaseInOut), RepeatMode.Reverse), label = "breathe")

    val config = androidx.compose.ui.platform.LocalConfiguration.current
    val screenW = config.screenWidthDp.toFloat()
    val screenH = config.screenHeightDp.toFloat()

    Box(
        Modifier.fillMaxSize()
            .background(Color(0xFF06060F))
            .drawBehind {
                drawRect(Brush.verticalGradient(
                    listOf(Color(0xFF08081A), Color(0xFF0A0A20), Color(0xFF060612))
                ))
            }
    ) {
        // ── Layer 1: Large ambient orbs ──
        val orb1X = screenW * 0.25f + sin(f1 * 6.28f) * screenW * 0.15f
        val orb1Y = screenH * 0.3f + cos(f2 * 6.28f) * screenH * 0.1f
        Box(Modifier.offset(x = orb1X.dp, y = orb1Y.dp).size(350.dp)
            .background(Brush.radialGradient(listOf(HordeColors.Lavender.copy(alpha = 0.04f), Color.Transparent))))

        val orb2X = screenW * 0.65f + cos(f2 * 6.28f) * screenW * 0.12f
        val orb2Y = screenH * 0.55f + sin(f1 * 6.28f) * screenH * 0.08f
        Box(Modifier.offset(x = orb2X.dp, y = orb2Y.dp).size(280.dp)
            .background(Brush.radialGradient(listOf(HordeColors.SkyBlue.copy(alpha = 0.035f), Color.Transparent))))

        val orb3X = screenW * 0.5f + sin(f3 * 6.28f) * screenW * 0.2f
        val orb3Y = screenH * 0.15f + cos(f3 * 6.28f) * screenH * 0.05f
        Box(Modifier.offset(x = orb3X.dp, y = orb3Y.dp).size(200.dp)
            .background(Brush.radialGradient(listOf(Color(0xFFFF6E40).copy(alpha = 0.025f), Color.Transparent))))

        // ── Layer 2: Floating particles ──
        for (i in 0 until 12) {
            val hash = (i * 7919 + 42) % 10000
            val px = (hash % 1000) / 1000f * screenW
            val py = ((hash / 1000) * 3571) % 10000 / 10000f * screenH
            val speed = 0.3f + (hash % 50) / 100f
            val phase = hash.toFloat()
            val animX = px + sin(f1 * 6.28f * speed + phase) * 30f
            val animY = py + cos(f2 * 6.28f * speed + phase) * 20f
            val alpha = 0.15f + 0.1f * sin(f3 * 6.28f + phase)
            val dotSize = 2f + (hash % 30) / 15f
            Box(Modifier.offset(x = animX.dp, y = animY.dp).size(dotSize.dp)
                .clip(SmallCutShape).background(Color.White.copy(alpha = alpha)))
        }

        // ── Layer 3: Content ──
        Column(
            Modifier.fillMaxSize().padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(60.dp))

            // ── Title area — compact and elegant ──
            Box(contentAlignment = Alignment.Center) {
                // Glow behind title
                Box(Modifier.size(120.dp).background(Brush.radialGradient(
                    listOf(HordeColors.SkyBlue.copy(alpha = 0.08f * glow), Color.Transparent)
                )))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⚔️", fontSize = 36.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "HORDE",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = HordeColors.SkyBlue.copy(alpha = glow),
                        letterSpacing = 8.sp,
                        style = TextStyle(
                            shadow = Shadow(color = HordeColors.SkyBlue.copy(alpha = 0.3f), offset = Offset(0f, 0f), blurRadius = 20f)
                        )
                    )
                    Text(
                        "SURVIVAL",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = HordeColors.TextSecondary.copy(alpha = 0.5f),
                        letterSpacing = 6.sp
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Gold badge — compact
            if (gold > 0) {
                Box(Modifier.clip(CornerCutShape).background(Color(0xFFFFD700).copy(alpha = 0.08f))
                    .border(0.5.dp, Color(0xFFFFD700).copy(alpha = 0.15f), CornerCutShape)
                    .padding(horizontal = 14.dp, vertical = 4.dp)) {
                    Text("💰 $gold", fontSize = 13.sp, color = HordeColors.GoldColor.copy(alpha = 0.9f), fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.weight(1f))

            // ── Buttons — bottom half ──
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Play button — prominent
                HordeButton(
                    text = L("play"),
                    icon = "▶",
                    onClick = onPlayClick,
                    color = HordeColors.SkyBlue,
                    modifier = Modifier.fillMaxWidth(0.7f),
                    breathe = breathe
                )

                // Secondary buttons — compact grid
                Row(Modifier.fillMaxWidth(0.85f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HordeSecondaryButton(text = L("characters"), onClick = onCharactersClick, modifier = Modifier.weight(1f), color = HordeColors.Lavender, icon = "👤")
                    HordeSecondaryButton(text = L("upgrades"), onClick = onUpgradesClick, modifier = Modifier.weight(1f), color = HordeColors.MintGreen, icon = "⬆")
                }
                Row(Modifier.fillMaxWidth(0.85f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HordeSecondaryButton(text = "Shop", onClick = onShopClick, modifier = Modifier.weight(1f), color = HordeColors.GoldColor, icon = "🛒")
                    HordeSecondaryButton(text = "Stats", onClick = onStatsClick, modifier = Modifier.weight(1f), color = HordeColors.SoftPink, icon = "📊")
                }
                Row(Modifier.fillMaxWidth(0.85f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HordeSecondaryButton(text = L("settings"), onClick = onSettingsClick, modifier = Modifier.weight(1f), color = HordeColors.WarmPeach, icon = "⚙")
                    HordeSecondaryButton(text = "Tutorial", onClick = onTutorialClick, modifier = Modifier.weight(1f), color = HordeColors.Cream, icon = "📖")
                }
            }

            Spacer(Modifier.height(28.dp))

            // Footer — subtle
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("v1.2.9", fontSize = 10.sp, color = Color.White.copy(alpha = 0.15f))
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
