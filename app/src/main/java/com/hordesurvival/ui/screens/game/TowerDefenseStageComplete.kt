package com.hordesurvival.ui.screens.game

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hordesurvival.ui.Locales
import com.hordesurvival.ui.components.HordeButton
import com.hordesurvival.ui.components.HordeCard
import com.hordesurvival.ui.components.HordeItemCard
import com.hordesurvival.ui.components.HordeSecondaryButton
import com.hordesurvival.ui.theme.HordeColors

/**
 * Tower Defense stage completion overlay.
 * Uses HordeUI shared components and localization system.
 */
@Composable
fun TowerDefenseStageComplete(
    stageNumber: Int,
    stageName: String,
    kills: Int,
    goldEarned: Int,
    livesRemaining: Int,
    isVictory: Boolean = false,
    languageCode: String = "en",
    onNextStage: () -> Unit,
    onReplayStage: () -> Unit,
    onMainMenu: () -> Unit
) {
    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(300)
        showContent = true
    }
    val scale by animateFloatAsState(
        if (showContent) 1f else 0.7f,
        spring(0.6f), label = "td_stage_complete_scale"
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f)),
        contentAlignment = Alignment.Center
    ) {
        HordeCard(
            modifier = Modifier
                .scale(scale)
                .widthIn(max = 400.dp)
                .fillMaxWidth(0.9f)
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header
                Text(
                    if (isVictory) "🏆 VICTORY!" else "⚔️ STAGE CLEAR!",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isVictory) Color(0xFFFFD700) else Color(0xFF66BB6A)
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "Stage $stageNumber: $stageName",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(Modifier.height(24.dp))

                // Stats cards
                Column(
                    Modifier.widthIn(max = 400.dp).fillMaxWidth(0.9f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatRow("🎯 ${Locales.getString("enemies_killed", languageCode)}", "$kills")
                    StatRow("💰 ${Locales.getString("gold_earned", languageCode)}", "$goldEarned")
                    StatRow("❤️ Lives Remaining", "$livesRemaining")
                }

                Spacer(Modifier.height(32.dp))

                // Buttons
                if (!isVictory) {
                    // Next Stage
                    HordeButton(
                        modifier = Modifier.widthIn(max = 350.dp).fillMaxWidth(0.9f),
                        text = "Next Stage",
                        icon = "▶",
                        color = Color(0xFF66BB6A),
                        onClick = onNextStage
                    )
                }

                Spacer(Modifier.height(10.dp))

                // Replay
                HordeSecondaryButton(
                    modifier = Modifier.widthIn(max = 350.dp).fillMaxWidth(0.9f),
                    text = "Replay Stage",
                    icon = "🔄",
                    onClick = onReplayStage
                )

                Spacer(Modifier.height(10.dp))

                // Main Menu
                HordeSecondaryButton(
                    modifier = Modifier.widthIn(max = 350.dp).fillMaxWidth(0.9f),
                    text = Locales.getString("main_menu", languageCode),
                    icon = "🏠",
                    onClick = onMainMenu
                )
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    HordeItemCard(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(label, fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Medium)
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = HordeColors.GoldColor)
        }
    }
}
