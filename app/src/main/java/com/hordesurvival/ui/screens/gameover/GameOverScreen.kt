package com.hordesurvival.ui.screens.gameover

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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hordesurvival.ui.Locales
import com.hordesurvival.ui.theme.HordeColors
import com.hordesurvival.ui.viewmodel.RunSummary
import com.hordesurvival.game.ads.AdManager

@Composable
fun GameOverScreen(
    summary: RunSummary,
    onPlayAgain: () -> Unit,
    onMainMenu: () -> Unit,
    onContinue: () -> Unit = {},
    canContinue: Boolean = true,
    activity: android.app.Activity? = null,
    languageCode: String = "en"
) {
    val L = { k: String -> Locales.getString(k, languageCode) }
    var showStats by remember { mutableStateOf(false) }
    var showBtns by remember { mutableStateOf(false) }
    var continueUsed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { kotlinx.coroutines.delay(300); showStats = true; kotlinx.coroutines.delay(500); showBtns = true }
    val ss by animateFloatAsState(if (showStats) 1f else 0.7f, spring(0.6f), label = "s")
    val bs by animateFloatAsState(if (showBtns) 1f else 0.8f, spring(0.7f), label = "b")

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF0D0D2B), Color(0xFF0A0A1F), Color(0xFF050510)))), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(28.dp)) {
            Text("💀", fontSize = 48.sp)
            Spacer(Modifier.height(8.dp))
            Text(L("game_over"), fontSize = 36.sp, fontWeight = FontWeight.Black, color = HordeColors.SoftPink, letterSpacing = 2.sp)
            Spacer(Modifier.height(32.dp))

            // Stats card
            Box(Modifier.scale(ss).fillMaxWidth(0.88f).clip(RoundedCornerShape(18.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF1E1E3F), Color(0xFF151530))))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(18.dp)).padding(24.dp)) {
                Column(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(L("time_survived"), fontSize = 15.sp, color = HordeColors.TextSecondary)
                        Text(formatTime(summary.timeSurvived), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = HordeColors.SkyBlue)
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(L("enemies_killed"), fontSize = 15.sp, color = HordeColors.TextSecondary)
                        Text("${summary.kills}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = HordeColors.SoftPink)
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(L("level_reached"), fontSize = 15.sp, color = HordeColors.TextSecondary)
                        Text("${summary.level}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = HordeColors.Lavender)
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(L("gold_earned"), fontSize = 15.sp, color = HordeColors.TextSecondary)
                        Text("${summary.goldEarned}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = HordeColors.GoldColor)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Continue button (free for now, ad placeholder for future)
            if (canContinue && !continueUsed) {
                Column(Modifier.scale(bs), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier.fillMaxWidth(0.65f).height(54.dp).clip(RoundedCornerShape(16.dp))
                            .background(Brush.horizontalGradient(listOf(Color(0xFF66BB6A).copy(alpha = 0.85f), Color(0xFF4CAF50).copy(alpha = 0.6f))))
                            .border(1.dp, Color(0xFF66BB6A).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .clickable {
                                continueUsed = true
                                // Try to show rewarded ad first
                                val adShown = if (activity != null) {
                                    AdManager.showRewardedAd(activity) { onContinue() }
                                } else false
                                // If no ad available, continue for free
                                if (!adShown) onContinue()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("💚 Continue", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Free (ad coming soon)", fontSize = 9.sp, color = Color.White.copy(alpha = 0.5f))
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }

            // Main buttons
            Column(Modifier.scale(bs), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.fillMaxWidth(0.65f).height(54.dp).clip(RoundedCornerShape(16.dp))
                    .background(Brush.horizontalGradient(listOf(HordeColors.SkyBlue.copy(alpha = 0.85f), HordeColors.Lavender.copy(alpha = 0.6f))))
                    .border(1.dp, HordeColors.SkyBlue.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .clickable { onPlayAgain() }, contentAlignment = Alignment.Center) {
                    Text("🔄  ${L("play_again")}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(Modifier.height(12.dp))
                Box(Modifier.fillMaxWidth(0.65f).height(50.dp).clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1A1A3F)).border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                    .clickable { onMainMenu() }, contentAlignment = Alignment.Center) {
                    Text("🏠  ${L("main_menu")}", fontSize = 16.sp, color = HordeColors.TextSecondary)
                }
            }
        }
    }
}

private fun formatTime(s: Float): String { val m = (s/60).toInt(); return "%d:%02d".format(m, (s%60).toInt()) }
