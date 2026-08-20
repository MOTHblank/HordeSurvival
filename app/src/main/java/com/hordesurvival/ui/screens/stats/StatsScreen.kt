package com.hordesurvival.ui.screens.stats

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hordesurvival.data.model.PlayerSave
import com.hordesurvival.ui.Locales
import com.hordesurvival.ui.theme.HordeColors

/**
 * Stats overview screen — shows player lifetime stats.
 */
@Composable
fun StatsScreen(
    save: PlayerSave,
    languageCode: String = "en",
    onBack: () -> Unit
) {
    val L = { k: String -> Locales.getString(k, languageCode) }

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(HordeColors.DarkBg, HordeColors.DarkSurface)))) {
        Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📊 ${L("your_stats")}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = HordeColors.WarmPeach)
            Spacer(Modifier.height(24.dp))

            // Stats card
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFF1E1E3F), Color(0xFF151530))))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column(Modifier.fillMaxWidth()) {
                    StatRow("🏃 ${L("total_runs")}", "${save.totalRuns}")
                    Spacer(Modifier.height(12.dp))
                    StatRow("💀 ${L("total_kills")}", "${save.totalKills}")
                    Spacer(Modifier.height(12.dp))
                    StatRow("⏱ ${L("best_time")}", formatTime(save.bestTime))
                    Spacer(Modifier.height(12.dp))
                    StatRow("⭐ ${L("best_level")}", "${save.bestLevel}")
                    Spacer(Modifier.height(12.dp))
                    StatRow("💰 Gold", "${save.totalGold}")
                    Spacer(Modifier.height(12.dp))
                    StatRow("❤️ Max HP Upgrade", "Lv.${save.metaHpLevel}")
                    Spacer(Modifier.height(12.dp))
                    StatRow("⚔️ Might Upgrade", "Lv.${save.metaMightLevel}")
                    Spacer(Modifier.height(12.dp))
                    StatRow("🕐 Cooldown Upgrade", "Lv.${save.metaCooldownLevel}")
                    Spacer(Modifier.height(12.dp))
                    StatRow("💨 Speed Upgrade", "Lv.${save.metaSpeedLevel}")
                    Spacer(Modifier.height(12.dp))
                    StatRow("🍀 Luck Upgrade", "Lv.${save.metaLuckLevel}")
                }
            }

            Spacer(Modifier.weight(1f))
            TextButton(onClick = onBack) { Text("← Back", color = HordeColors.TextSecondary) }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 15.sp, color = HordeColors.TextSecondary)
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = HordeColors.SkyBlue)
    }
}

private fun formatTime(s: Float): String {
    val m = (s / 60).toInt()
    val sec = (s % 60).toInt()
    return "%d:%02d".format(m, sec)
}
