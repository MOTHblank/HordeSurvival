package com.hordesurvival.ui.screens.mapselect

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hordesurvival.game.map.GameMap
import com.hordesurvival.ui.components.HordeButton
import com.hordesurvival.ui.components.HordeHeader
import com.hordesurvival.ui.components.HordeItemCard
import com.hordesurvival.ui.components.HordeScreen
import com.hordesurvival.ui.components.HordeSecondaryButton
import com.hordesurvival.ui.components.SmallCutShape
import com.hordesurvival.ui.theme.HordeColors

/**
 * Map selection screen — shown when player picks Survival mode.
 */
@Composable
fun MapSelectScreen(
    unlockedMapIds: Set<String>,
    playerGold: Int,
    onSelectMap: (GameMap) -> Unit,
    onUnlockMap: (GameMap) -> Unit,
    onBack: () -> Unit
) {
    var selectedMap by remember { mutableStateOf<GameMap?>(null) }

    HordeScreen(contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier.fillMaxSize().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))

            HordeHeader(
                title = "SELECT MAP",
                subtitle = "Choose arena for your survival battle",
                icon = "🗺️",
                accentColor = HordeColors.WarmPeach
            )

            Spacer(Modifier.height(8.dp))
            Box(
                Modifier
                    .clip(SmallCutShape)
                    .background(HordeColors.GoldColor.copy(alpha = 0.12f))
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Text("💰 Gold: $playerGold", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = HordeColors.GoldColor)
            }

            Spacer(Modifier.height(20.dp))

            // Map grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(GameMap.allMaps) { map ->
                    val isUnlocked = map.id in unlockedMapIds || map.unlockCost == 0
                    val isSelected = selectedMap?.id == map.id
                    val alpha = if (isUnlocked) 1f else 0.5f

                    HordeItemCard(
                        modifier = Modifier.height(160.dp).alpha(alpha),
                        selected = isSelected,
                        onClick = if (isUnlocked) { { selectedMap = map } } else null
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(map.icon, fontSize = 36.sp)
                            Spacer(Modifier.height(6.dp))
                            Text(map.name, fontSize = 14.sp, fontWeight = FontWeight.Black,
                                color = if (isUnlocked) Color.White else Color.White.copy(alpha = 0.4f),
                                textAlign = TextAlign.Center)
                            Spacer(Modifier.height(4.dp))
                            if (!isUnlocked) {
                                Text("🔒 ${map.unlockCost} gold", fontSize = 11.sp, color = HordeColors.GoldColor.copy(alpha = 0.7f))
                                Text("Level ${map.minLevel}+", fontSize = 10.sp, color = Color.White.copy(alpha = 0.3f))
                            } else {
                                Text(map.description, fontSize = 10.sp, color = Color.White.copy(alpha = 0.5f),
                                    textAlign = TextAlign.Center, maxLines = 3)
                            }
                            // Modifiers
                            if (isUnlocked) {
                                Spacer(Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    if (map.enemyHpMult > 1f) StatChip("HP×${map.enemyHpMult}", HordeColors.Danger)
                                    if (map.goldMult > 1f) StatChip("Gold×${map.goldMult}", HordeColors.GoldColor)
                                    if (map.xpMult > 1f) StatChip("XP×${map.xpMult}", HordeColors.MintGreen)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Action buttons
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back
                HordeSecondaryButton(
                    text = "Back",
                    icon = "←",
                    onClick = onBack,
                    modifier = Modifier.weight(1f)
                )

                // Play
                val canPlay = selectedMap != null && (selectedMap!!.id in unlockedMapIds || selectedMap!!.unlockCost == 0)
                HordeButton(
                    text = "Play",
                    icon = "▶",
                    color = HordeColors.Success,
                    enabled = canPlay,
                    onClick = { if (canPlay) selectedMap?.let { onSelectMap(it) } },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatChip(text: String, color: Color) {
    Text(
        text, fontSize = 9.sp, fontWeight = FontWeight.Bold,
        color = color,
        modifier = Modifier
            .clip(SmallCutShape)
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 4.dp, vertical = 1.dp)
    )
}
