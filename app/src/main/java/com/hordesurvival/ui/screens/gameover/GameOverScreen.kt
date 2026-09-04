package com.hordesurvival.ui.screens.gameover

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
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
import com.hordesurvival.ui.components.HordeScreen
import com.hordesurvival.ui.components.HordeSecondaryButton
import com.hordesurvival.ui.theme.HordeColors
import com.hordesurvival.ui.theme.HordeTypography
import com.hordesurvival.ui.viewmodel.RunSummary

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

    HordeScreen(contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(28.dp)) {
            Text("😵💫", style = HordeTypography.Title)
            Spacer(Modifier.height(8.dp))
            Text(L("game_over"), style = HordeTypography.Title, color = HordeColors.WarmPeach, letterSpacing = 2.sp)
            Spacer(Modifier.height(4.dp))
            Text("Monster Bouncer! 🌟", style = HordeTypography.Label, color = HordeColors.MintGreen)
            Spacer(Modifier.height(28.dp))

            // Stats card
            HordeCard(Modifier.scale(ss).widthIn(max = 400.dp).fillMaxWidth(0.9f)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(L("time_survived"), style = HordeTypography.Body, color = HordeColors.TextSecondary)
                    Text(formatTime(summary.timeSurvived), style = HordeTypography.Value, color = HordeColors.SkyBlue)
                }
                Spacer(Modifier.height(14.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("👾 ${L("enemies_killed")}", style = HordeTypography.Body, color = HordeColors.TextSecondary)
                    Text("${summary.kills}", style = HordeTypography.Value, color = HordeColors.MintGreen)
                }
                Spacer(Modifier.height(14.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(L("level_reached"), style = HordeTypography.Body, color = HordeColors.TextSecondary)
                    Text("${summary.level}", style = HordeTypography.Value, color = HordeColors.Lavender)
                }
                Spacer(Modifier.height(14.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(L("gold_earned"), style = HordeTypography.Body, color = HordeColors.TextSecondary)
                    Text("${summary.goldEarned}", style = HordeTypography.Value, color = HordeColors.GoldColor)
                }
            }

            Spacer(Modifier.height(24.dp))

            // Continue button
            if (canContinue && !continueUsed) {
                Column(Modifier.scale(bs), horizontalAlignment = Alignment.CenterHorizontally) {
                    HordeButton(text = "Continue",
                        icon = "💚",
                        color = HordeColors.Success,
                        onClick = {
                            continueUsed = true
                            onContinue()
                        }
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }

            // Main buttons
            Column(Modifier.scale(bs), horizontalAlignment = Alignment.CenterHorizontally) {
                HordeButton(text = L("play_again"),
                    icon = "🔄",
                    color = HordeColors.SkyBlue,
                    onClick = onPlayAgain
                )
                Spacer(Modifier.height(12.dp))
                HordeSecondaryButton(text = L("main_menu"),
                    icon = "🏠",
                    onClick = onMainMenu
                )
            }
        }
    }
}

private fun formatTime(s: Float): String { val m = (s/60).toInt(); return "%d:%02d".format(m, (s%60).toInt()) }
