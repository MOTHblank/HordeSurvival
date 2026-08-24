package com.hordesurvival.ui.screens.menu

import androidx.compose.foundation.background
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
import com.hordesurvival.game.mode.GameModeType
import com.hordesurvival.game.mode.DailyChallenge
import com.hordesurvival.ui.Locales
import com.hordesurvival.ui.theme.HordeColors
import com.hordesurvival.ui.components.HordeScreen
import com.hordesurvival.ui.components.HordeButton

@Composable
fun ModeSelectScreen(onModeSelected: (GameModeType) -> Unit, onBack: () -> Unit, languageCode: String = "en") {
    val L = { k: String -> Locales.getString(k, languageCode) }
    val dailyChallenge = remember { DailyChallenge.getTodayChallenge() }
    HordeScreen {
        Column(Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(L("select_mode"), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = HordeColors.SkyBlue)
            Spacer(Modifier.height(40.dp))
            ModeCard("♾️ ${L("survival_mode")}", "Endless waves. How long can you survive?", HordeColors.SkyBlue) { onModeSelected(GameModeType.SURVIVAL) }
            Spacer(Modifier.height(16.dp))
            ModeCard("📜 ${L("quest_mode")}", "10 levels with unique objectives", HordeColors.Lavender) { onModeSelected(GameModeType.QUEST) }
            Spacer(Modifier.height(16.dp))
            ModeCard("${dailyChallenge.icon} ${L("daily_challenge")}", "${dailyChallenge.name}: ${dailyChallenge.description}", HordeColors.WarmPeach) { onModeSelected(GameModeType.DAILY_CHALLENGE) }
            Spacer(Modifier.height(16.dp))
            ModeCard("🏗️ Tower Defense", "Defend your tower from enemy waves!", HordeColors.MintGreen) { onModeSelected(GameModeType.TOWER_DEFENSE) }
            Spacer(Modifier.height(16.dp))
            ModeCard("👹 Boss Rush Extreme", "Bosses every 15s. No breaks. Pure chaos.", HordeColors.SoftPink) { onModeSelected(GameModeType.BOSS_RUSH_EXTREME) }
            Spacer(Modifier.height(32.dp))
            HordeButton(text = "← ${L("back")}", onClick = onBack, color = HordeColors.TextSecondary, isSecondary = true)
        }
    }
}

@Composable
private fun ModeCard(title: String, desc: String, color: Color, onClick: () -> Unit) {
    Box(Modifier.fillMaxWidth(0.85f).height(100.dp).clip(RoundedCornerShape(16.dp))
        .background(Brush.horizontalGradient(listOf(color.copy(alpha = 0.3f), HordeColors.DarkCard)))
        .clickable { onClick() }.padding(16.dp), contentAlignment = Alignment.CenterStart) {
        Column {
            Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
            Spacer(Modifier.height(4.dp))
            Text(desc, fontSize = 14.sp, color = HordeColors.TextSecondary)
        }
    }
}
