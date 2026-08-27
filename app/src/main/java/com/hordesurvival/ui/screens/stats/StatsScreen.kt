package com.hordesurvival.ui.screens.stats

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hordesurvival.data.model.PlayerSave
import com.hordesurvival.ui.Locales
import com.hordesurvival.ui.components.HordeBackButton
import com.hordesurvival.ui.components.HordeCard
import com.hordesurvival.ui.components.HordeScreen
import com.hordesurvival.ui.theme.HordeColors
import com.hordesurvival.ui.theme.HordeTypography

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

    HordeScreen {
        Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📊 ${L("your_stats")}", style = HordeTypography.Header.copy(color = HordeColors.WarmPeach))
            Spacer(Modifier.height(24.dp))

            // Stats card
            HordeCard {
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

            Spacer(Modifier.weight(1f))
            HordeBackButton(onClick = onBack)
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = HordeTypography.Body.copy(color = HordeColors.TextSecondary))
        Text(value, style = HordeTypography.Value.copy(color = HordeColors.SkyBlue))
    }
}

private fun formatTime(s: Float): String {
    val m = (s / 60).toInt()
    val sec = (s % 60).toInt()
    return "%d:%02d".format(m, sec)
}
