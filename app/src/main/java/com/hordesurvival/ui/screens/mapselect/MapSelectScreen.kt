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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hordesurvival.game.map.GameMap
import com.hordesurvival.ui.components.HordeButton
import com.hordesurvival.ui.components.HordeHeader
import com.hordesurvival.ui.components.HordeItemCard
import com.hordesurvival.ui.components.HordeScreen
import com.hordesurvival.ui.components.HordeSecondaryButton
import com.hordesurvival.ui.components.SmallCutShape
import com.hordesurvival.ui.theme.HordeColors
import com.hordesurvival.ui.theme.HordeTypography

/**
 * Map selection screen — shown when player picks Survival mode.
 *
 * Visual pass: per-map identity via GameMap.accentColor (card tint) and an
 * atmosphere preview strip (ambientColor → particleColor gradient — the exact
 * colors the in-game renderer uses). onUnlockMap is now actually wired:
 * affordable + level-qualified locked maps are tappable to unlock.
 *
 * NOTE: strings are English-only while other screens use Locales — needs
 * map_select keys added to Locales before wiring languageCode here.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MapSelectScreen(
    unlockedMapIds: Set<String>,
    playerGold: Int,
    playerLevel: Int = 0,   // NEW — pass the player's meta level (call site!)
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
                Text("💰 Gold: $playerGold", style = HordeTypography.Body, color = HordeColors.GoldColor)
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
                    val levelOk = playerLevel >= map.minLevel
                    val canUnlock = !isUnlocked && levelOk && playerGold >= map.unlockCost
                    val alpha = if (isUnlocked) 1f else 0.5f

                    HordeItemCard(
                        modifier = Modifier.height(160.dp).alpha(alpha),
                        selected = isSelected,
                        // CHANGED: locked + affordable + level-qualified = tap to unlock
                        // (was: locked cards were permanently non-interactive and
                        // onUnlockMap was never called from anywhere)
                        onClick = when {
                            isUnlocked -> { { selectedMap = map } }
                            canUnlock -> { { onUnlockMap(map) } }
                            else -> null
                        }
                    ) {
                        Box(Modifier.fillMaxSize()) {
                            // CHANGED: per-map accent tint — instant visual identity
                            Box(
                                Modifier.fillMaxSize().background(
                                    Color(map.accentColor).copy(alpha = if (isUnlocked) 0.10f else 0.04f)
                                )
                            )
                            // CHANGED: atmosphere preview strip — the exact ambient/particle
                            // colors the in-game renderer will use for this map
                            Box(
                                Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(5.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(map.ambientColor), Color(map.particleColor))
                                        )
                                    )
                            )

                            Column(
                                Modifier.align(Alignment.Center)
                                    .padding(horizontal = 8.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(map.icon, style = HordeTypography.Title)
                                Spacer(Modifier.height(6.dp))
                                Text(map.name, style = HordeTypography.Body,
                                    color = if (isUnlocked) Color.White else Color.White.copy(alpha = 0.4f),
                                    textAlign = TextAlign.Center)
                                Spacer(Modifier.height(4.dp))
                                if (!isUnlocked) {
                                    if (canUnlock) {
                                        Text("🔓 Unlock for ${map.unlockCost} gold",
                                            style = HordeTypography.Label, color = HordeColors.GoldColor)
                                    } else {
                                        Text("🔒 ${map.unlockCost} gold",
                                            style = HordeTypography.Label,
                                            color = HordeColors.GoldColor.copy(alpha = if (levelOk) 0.7f else 0.4f))
                                    }
                                    Text(
                                        if (levelOk) "Level ${map.minLevel}+" else "Requires level ${map.minLevel}",
                                        style = HordeTypography.Label,
                                        color = Color.White.copy(alpha = 0.3f)
                                    )
                                } else {
                                    Text(map.description, style = HordeTypography.Label,
                                        color = Color.White.copy(alpha = 0.5f),
                                        textAlign = TextAlign.Center, maxLines = 3)
                                }
                                // Modifiers
                                if (isUnlocked) {
                                    Spacer(Modifier.height(4.dp))
                                    // CHANGED: FlowRow (5 chips overflowed a fixed Row) + Spd/Dmg added
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        if (map.enemyHpMult > 1f) StatChip("HP×${map.enemyHpMult}", HordeColors.Danger)
                                        if (map.enemySpdMult > 1f) StatChip("Spd×${map.enemySpdMult}", HordeColors.Info)
                                        if (map.enemyDmgMult > 1f) StatChip("Dmg×${map.enemyDmgMult}", HordeColors.Warning)
                                        if (map.goldMult > 1f) StatChip("Gold×${map.goldMult}", HordeColors.GoldColor)
                                        if (map.xpMult > 1f) StatChip("XP×${map.xpMult}", HordeColors.MintGreen)
                                    }
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
                HordeSecondaryButton(
                    text = "Back",
                    icon = "←",
                    onClick = onBack,
                    modifier = Modifier.weight(1f, fill = false)
                )

                val canPlay = selectedMap != null && (selectedMap!!.id in unlockedMapIds || selectedMap!!.unlockCost == 0)
                HordeButton(
                    text = "Play",
                    icon = "▶",
                    color = HordeColors.Success,
                    enabled = canPlay,
                    onClick = { if (canPlay) selectedMap?.let { onSelectMap(it) } },
                    modifier = Modifier.weight(1f, fill = false)
                )
            }
        }
    }
}

@Composable
private fun StatChip(text: String, color: Color) {
    Text(
        text, style = HordeTypography.Label,
        color = color,
        modifier = Modifier
            .clip(SmallCutShape)
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 4.dp, vertical = 1.dp)
    )
}