package com.hordesurvival.ui.screens.game

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Tower Defense stage completion overlay.
 * Shows stage results and options: Next Stage / Replay / Menu
 */
@Composable
fun TowerDefenseStageComplete(
    stageNumber: Int,
    stageName: String,
    kills: Int,
    goldEarned: Int,
    livesRemaining: Int,
    isVictory: Boolean = false,
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
        spring(0.6f), label = "s"
    )

    Box(
        Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.75f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(scale)
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF1E1E3F), Color(0xFF151530))))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                .padding(28.dp)
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

            // Stats
            Column(
                Modifier.fillMaxWidth(0.8f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatRow("🎯 Enemies Killed", "$kills")
                StatRow("💰 Gold Earned", "$goldEarned")
                StatRow("❤️ Lives Remaining", "$livesRemaining")
            }

            Spacer(Modifier.height(32.dp))

            // Buttons
            if (!isVictory) {
                // Next Stage
                Box(
                    Modifier.fillMaxWidth(0.7f).height(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFF66BB6A), Color(0xFF4CAF50))))
                        .clickable { onNextStage() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("▶ Next Stage", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(Modifier.height(10.dp))

            // Replay
            Box(
                Modifier.fillMaxWidth(0.7f).height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF2A2A5F))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                    .clickable { onReplayStage() },
                contentAlignment = Alignment.Center
            ) {
                Text("🔄 Replay Stage", fontSize = 16.sp, color = Color.White)
            }

            Spacer(Modifier.height(10.dp))

            // Main Menu
            Box(
                Modifier.fillMaxWidth(0.7f).height(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF1A1A3F))
                    .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(14.dp))
                    .clickable { onMainMenu() },
                contentAlignment = Alignment.Center
            ) {
                Text("🏠 Main Menu", fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 15.sp, color = Color.White.copy(alpha = 0.7f))
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
    }
}
