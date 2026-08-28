package com.hordesurvival.ui.screens.menu

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.hordesurvival.game.mode.GameModeType
import com.hordesurvival.game.mode.DailyChallenge
import com.hordesurvival.ui.Locales
import com.hordesurvival.ui.components.HordeBackButton
import com.hordesurvival.ui.components.HordeHeader
import com.hordesurvival.ui.components.HordeItemCard
import com.hordesurvival.ui.components.HordeScreen
import com.hordesurvival.ui.theme.HordeColors
import com.hordesurvival.ui.theme.HordeTypography

@Composable
fun ModeSelectScreen(onModeSelected: (GameModeType) -> Unit, onBack: () -> Unit, languageCode: String = "en") {
    val L = { k: String -> Locales.getString(k, languageCode) }
    val dailyChallenge = remember { DailyChallenge.getTodayChallenge() }

    HordeScreen(contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))

            HordeHeader(
                title = L("select_mode"),
                subtitle = "Choose your battle format",
                icon = "⚔️",
                accentColor = HordeColors.SkyBlue
            )

            Spacer(Modifier.height(28.dp))

            ModeCard("♾️ ${L("survival_mode")}", "Endless waves. How long can you survive?", HordeColors.SkyBlue) { onModeSelected(GameModeType.SURVIVAL) }
            Spacer(Modifier.height(14.dp))
            ModeCard("📜 ${L("quest_mode")}", "10 levels with unique objectives", HordeColors.Lavender) { onModeSelected(GameModeType.QUEST) }
            Spacer(Modifier.height(14.dp))
            ModeCard("${dailyChallenge.icon} ${L("daily_challenge")}", "${dailyChallenge.name}: ${dailyChallenge.description}", HordeColors.WarmPeach) { onModeSelected(GameModeType.DAILY_CHALLENGE) }
            Spacer(Modifier.height(14.dp))
            ModeCard("🏗️ Tower Defense", "Defend your tower from enemy waves!", HordeColors.MintGreen) { onModeSelected(GameModeType.TOWER_DEFENSE) }
            Spacer(Modifier.height(14.dp))
            ModeCard("👹 Boss Rush Extreme", "Bosses every 15s. No breaks. Pure chaos.", HordeColors.SoftPink) { onModeSelected(GameModeType.BOSS_RUSH_EXTREME) }

            Spacer(Modifier.height(32.dp))
            HordeBackButton(text = "← ${L("back")}", onClick = onBack)
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ModeCard(title: String, desc: String, color: Color, onClick: () -> Unit) {
    HordeItemCard(
        modifier = Modifier.widthIn(max = 500.dp)
            .fillMaxWidth(0.92f)
            .height(90.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.widthIn(max = 500.dp).fillMaxWidth(), horizontalAlignment = Alignment.Start) {
            Text(title, style = HordeTypography.SubHeader.copy(fontSize = 18.sp, color = color))
            Spacer(Modifier.height(4.dp))
            Text(desc, style = HordeTypography.Label.copy(fontSize = 13.sp))
        }
    }
}
