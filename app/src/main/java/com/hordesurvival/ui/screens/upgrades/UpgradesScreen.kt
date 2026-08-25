package com.hordesurvival.ui.screens.upgrades

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hordesurvival.data.model.PlayerSave
import com.hordesurvival.ui.components.HordeBackButton
import com.hordesurvival.ui.components.HordeItemCard
import com.hordesurvival.ui.components.HordeScreen
import com.hordesurvival.ui.components.HordeSmallButton
import com.hordesurvival.ui.theme.HordeColors
import com.hordesurvival.utils.Constants

/**
 * Meta-progression upgrades screen.
 * Now includes: HP, Gold, Might, Cooldown, Speed, Luck upgrades.
 */
@Composable
fun UpgradesScreen(
    playerSave: PlayerSave,
    onUpgrade: (String, Int) -> Unit,
    onBack: () -> Unit,
    languageCode: String = "en"
) {
    val upgrades = listOf(
        UpgradeDef("❤️", "Max HP", "+5% starting HP", playerSave.metaHpLevel, { onUpgrade("hp", it) }),
        UpgradeDef("💰", "Gold Gain", "+5% gold from enemies", playerSave.metaGoldLevel, { onUpgrade("gold", it) }),
        UpgradeDef("⚔️", "Might", "+5% weapon damage", playerSave.metaMightLevel, { onUpgrade("might", it) }),
        UpgradeDef("⏱", "Cooldown", "-3% weapon cooldown", playerSave.metaCooldownLevel, { onUpgrade("cooldown", it) }),
        UpgradeDef("💨", "Speed", "+5% move speed", playerSave.metaSpeedLevel, { onUpgrade("speed", it) }),
        UpgradeDef("🍀", "Luck", "+3% better upgrades", playerSave.metaLuckLevel, { onUpgrade("luck", it) }),
    )

    HordeScreen(contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("⬆ Upgrades", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = HordeColors.MintGreen)
            Spacer(modifier = Modifier.height(6.dp))
            Text("💰 ${playerSave.totalGold} gold", fontSize = 16.sp, color = HordeColors.GoldColor)

            Spacer(modifier = Modifier.height(20.dp))

            upgrades.forEach { def ->
                val cost = calculateMetaCost(def.level)
                UpgradeRow(
                    icon = def.icon,
                    title = def.title,
                    description = "${def.description} (${def.level}/${Constants.META_MAX_UPGRADE_LEVEL})",
                    cost = cost,
                    canAfford = playerSave.totalGold >= cost,
                    isMaxed = def.level >= Constants.META_MAX_UPGRADE_LEVEL,
                    onBuy = { def.onBuy(cost) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats
            HordeItemCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("📊 Stats", fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    StatLine("Total Runs", "${playerSave.totalRuns}")
                    StatLine("Total Kills", "${playerSave.totalKills}")
                    StatLine("Best Time", formatTime(playerSave.bestTime))
                    StatLine("Best Level", "${playerSave.bestLevel}")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            HordeBackButton(onClick = onBack)
        }
    }
}

private data class UpgradeDef(
    val icon: String, val title: String, val description: String,
    val level: Int, val onBuy: (Int) -> Unit  // takes cost
)

@Composable
private fun UpgradeRow(
    icon: String, title: String, description: String,
    cost: Int, canAfford: Boolean, isMaxed: Boolean, onBuy: () -> Unit
) {
    HordeItemCard(onClick = if (!isMaxed && canAfford) onBuy else null) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                Text(description, fontSize = 11.sp, color = HordeColors.TextSecondary)
            }
            if (isMaxed) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(HordeColors.MintGreen.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) { Text("MAX", color = HordeColors.MintGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            } else {
                HordeSmallButton(
                    text = "${cost}💰",
                    onClick = onBuy,
                    enabled = canAfford
                )
            }
        }
    }
}

@Composable
private fun StatLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = HordeColors.TextSecondary, fontSize = 13.sp)
        Text(value, color = HordeColors.SkyBlue, fontWeight = FontWeight.Medium, fontSize = 13.sp)
    }
    Spacer(modifier = Modifier.height(3.dp))
}

private fun calculateMetaCost(level: Int): Int = 50 + level * 75

private fun formatTime(seconds: Float): String {
    val m = (seconds / 60).toInt(); val s = (seconds % 60).toInt()
    return "%d:%02d".format(m, s)
}
