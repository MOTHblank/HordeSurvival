package com.hordesurvival.ui.screens.mapselect

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hordesurvival.game.map.GameMap
import com.hordesurvival.ui.components.HordeButton
import com.hordesurvival.ui.components.HordeSecondaryButton
import com.hordesurvival.ui.components.HordeScreen
import com.hordesurvival.ui.components.HordeCard

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
            Spacer(Modifier.height(40.dp))

            Text("🗺️ Select Map", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
            Spacer(Modifier.height(8.dp))
            Text("Gold: $playerGold", fontSize = 16.sp, color = Color(0xFFFFD700))

            Spacer(Modifier.height(20.dp))

            // Map grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(GameMap.allMaps) { map ->
                    val isUnlocked = map.id in unlockedMapIds || map.unlockCost == 0
                    val isSelected = selectedMap?.id == map.id

                    HordeCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        selected = isSelected,
                        onClick = if (isUnlocked) { { selectedMap = map } } else null
                    ) {
                        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Text(map.icon, fontSize = 36.sp)
                            Spacer(Modifier.height(6.dp))
                            Text(map.name, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                                color = if (isUnlocked) Color.White else Color.White.copy(alpha = 0.4f),
                                textAlign = TextAlign.Center)
                            Spacer(Modifier.height(4.dp))
                            if (!isUnlocked) {
                                Text("🔒 ${map.unlockCost} gold", fontSize = 11.sp, color = Color(0xFFFFD700).copy(alpha = 0.7f))
                                Text("Level ${map.minLevel}+", fontSize = 10.sp, color = Color.White.copy(alpha = 0.3f))
                            } else {
                                Text(map.description, fontSize = 10.sp, color = Color.White.copy(alpha = 0.5f),
                                    textAlign = TextAlign.Center, maxLines = 3)
                            }
                            // Modifiers
                            if (isUnlocked) {
                                Spacer(Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    if (map.enemyHpMult > 1f) StatChip("HP×${map.enemyHpMult}", Color(0xFFFF5252))
                                    if (map.goldMult > 1f) StatChip("Gold×${map.goldMult}", Color(0xFFFFD700))
                                    if (map.xpMult > 1f) StatChip("XP×${map.xpMult}", Color(0xFF69F0AE))
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
                    color = Color(0xFF66BB6A),
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
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 4.dp, vertical = 1.dp)
    )
}
