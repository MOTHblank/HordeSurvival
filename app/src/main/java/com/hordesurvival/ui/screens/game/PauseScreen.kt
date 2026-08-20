package com.hordesurvival.ui.screens.game

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hordesurvival.ui.theme.HordeColors

/**
 * Pause overlay with Resume, Settings, and Quit buttons.
 * Settings panel allows controlling music and SFX volume.
 */
@Composable
fun PauseScreen(
    onResume: () -> Unit,
    onQuit: () -> Unit,
    musicVolume: Float = 0.5f,
    sfxVolume: Float = 0.8f,
    bgMusicEnabled: Boolean = true,
    gameSpeed: Float = 1f,
    onMusicVolumeChange: (Float) -> Unit = {},
    onSfxVolumeChange: (Float) -> Unit = {},
    onBgMusicToggle: () -> Unit = {},
    onSpeedChange: (Float) -> Unit = {}
) {
    var showSettings by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "⏸ PAUSED",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(32.dp))

            if (showSettings) {
                // Settings panel
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(HordeColors.CardBg.copy(alpha = 0.95f))
                        .padding(20.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "⚙ Settings",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = HordeColors.WarmPeach
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Background Music Toggle
                        Box(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                                .background(HordeColors.DarkCard).padding(12.dp)
                        ) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🎵 Background Music", color = Color.White, fontSize = 14.sp)
                                Switch(
                                    checked = bgMusicEnabled,
                                    onCheckedChange = { onBgMusicToggle() },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = HordeColors.SkyBlue,
                                        checkedTrackColor = HordeColors.SkyBlue.copy(alpha = 0.3f)
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        // Music Volume
                        Box(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                                .background(HordeColors.DarkCard).padding(12.dp)
                        ) {
                            Column {
                                Text("🎶 Music Volume: ${(musicVolume * 100).toInt()}%", color = Color.White, fontSize = 14.sp)
                                Slider(
                                    value = musicVolume,
                                    onValueChange = onMusicVolumeChange,
                                    colors = SliderDefaults.colors(
                                        thumbColor = HordeColors.SkyBlue,
                                        activeTrackColor = HordeColors.SkyBlue
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        // SFX Volume
                        Box(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                                .background(HordeColors.DarkCard).padding(12.dp)
                        ) {
                            Column {
                                Text("🔊 SFX Volume: ${(sfxVolume * 100).toInt()}%", color = Color.White, fontSize = 14.sp)
                                Slider(
                                    value = sfxVolume,
                                    onValueChange = onSfxVolumeChange,
                                    colors = SliderDefaults.colors(
                                        thumbColor = HordeColors.SkyBlue,
                                        activeTrackColor = HordeColors.SkyBlue
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(onClick = { showSettings = false }) {
                            Text("← Back", color = HordeColors.TextSecondary)
                        }
                    }
                }
            } else {
                // Main pause buttons
                Button(onClick = onResume) {
                    Text("▶ Resume", fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = { showSettings = true }) {
                    Text("⚙ Settings", color = HordeColors.SkyBlue, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Spacer(Modifier.height(12.dp))

                // Game speed control
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Text("Speed: ", color = HordeColors.TextSecondary, fontSize = 14.sp)
                    listOf(0.5f to "0.5x", 1f to "1x", 2f to "2x", 3f to "3x").forEach { (speed, label) ->
                        val sel = gameSpeed == speed
                        Box(
                            Modifier.padding(horizontal = 4.dp).clip(RoundedCornerShape(8.dp))
                                .background(if (sel) HordeColors.SkyBlue.copy(alpha = 0.3f) else Color.Transparent)
                                .clickable { onSpeedChange(speed) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) { Text(label, color = if (sel) HordeColors.SkyBlue else HordeColors.TextSecondary, fontSize = 13.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal) }
                    }
                }
                Spacer(Modifier.height(12.dp))

                TextButton(onClick = onQuit) {
                    Text("🏠 Quit to Menu", color = HordeColors.TextSecondary)
                }
            }
        }
    }
}
