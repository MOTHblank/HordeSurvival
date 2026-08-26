package com.hordesurvival.ui.screens.menu

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import com.hordesurvival.ui.components.CornerCutShape
import com.hordesurvival.ui.components.HordeButton
import com.hordesurvival.ui.components.HordeScreen
import com.hordesurvival.ui.components.HordeSecondaryButton
import com.hordesurvival.ui.theme.HordeColors

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

    // Smooth animations for title glow and play button breathing
    val inf = rememberInfiniteTransition(label = "menu_glow")
    val glow by inf.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2500, easing = EaseInOut), RepeatMode.Reverse),
        label = "glow"
    )
    val breathe by inf.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(tween(3000, easing = EaseInOut), RepeatMode.Reverse),
        label = "breathe"
    )

    HordeScreen {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(60.dp))

            // ── Title area — compact and elegant ──
            Box(contentAlignment = Alignment.Center) {
                // Glow behind title
                Box(
                    Modifier
                        .size(120.dp)
                        .background(
                            Brush.radialGradient(
                                listOf(HordeColors.SkyBlue.copy(alpha = 0.08f * glow), Color.Transparent)
                            )
                        )
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⚔️", fontSize = 36.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "HORDE",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = HordeColors.SkyBlue.copy(alpha = glow),
                        letterSpacing = 8.sp,
                        style = TextStyle(
                            shadow = Shadow(color = HordeColors.SkyBlue.copy(alpha = 0.3f), offset = Offset(0f, 0f), blurRadius = 20f)
                        )
                    )
                    Text(
                        text = "SURVIVAL",
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
                Box(
                    Modifier
                        .clip(CornerCutShape)
                        .background(Color(0xFFFFD700).copy(alpha = 0.08f))
                        .border(0.5.dp, Color(0xFFFFD700).copy(alpha = 0.15f), CornerCutShape)
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "💰 $gold",
                        fontSize = 13.sp,
                        color = HordeColors.GoldColor.copy(alpha = 0.9f),
                        fontWeight = FontWeight.SemiBold
                    )
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
                    HordeSecondaryButton(
                        text = L("characters"),
                        onClick = onCharactersClick,
                        modifier = Modifier.weight(1f),
                        color = HordeColors.Lavender,
                        icon = "👤"
                    )
                    HordeSecondaryButton(
                        text = L("upgrades"),
                        onClick = onUpgradesClick,
                        modifier = Modifier.weight(1f),
                        color = HordeColors.MintGreen,
                        icon = "⬆"
                    )
                }
                Row(Modifier.fillMaxWidth(0.85f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HordeSecondaryButton(
                        text = L("shop"),
                        onClick = onShopClick,
                        modifier = Modifier.weight(1f),
                        color = HordeColors.GoldColor,
                        icon = "🛒"
                    )
                    HordeSecondaryButton(
                        text = L("stats"),
                        onClick = onStatsClick,
                        modifier = Modifier.weight(1f),
                        color = HordeColors.SoftPink,
                        icon = "📊"
                    )
                }
                Row(Modifier.fillMaxWidth(0.85f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HordeSecondaryButton(
                        text = L("settings"),
                        onClick = onSettingsClick,
                        modifier = Modifier.weight(1f),
                        color = HordeColors.WarmPeach,
                        icon = "⚙"
                    )
                    HordeSecondaryButton(
                        text = L("tutorial"),
                        onClick = onTutorialClick,
                        modifier = Modifier.weight(1f),
                        color = HordeColors.Cream,
                        icon = "📖"
                    )
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
