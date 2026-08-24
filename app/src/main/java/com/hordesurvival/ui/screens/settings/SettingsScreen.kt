package com.hordesurvival.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hordesurvival.ui.components.HordeScreen
import com.hordesurvival.ui.components.HordeButton
import androidx.compose.ui.unit.sp
import com.hordesurvival.ui.theme.HordeColors

@Composable
fun SettingsScreen(
    musicVolume: Float, sfxVolume: Float, vibrationEnabled: Boolean,
    languageCode: String, backgroundStyle: Int,
    bgMusicEnabled: Boolean = true,
    graphicsQuality: Int = 1,
    showDamageNumbers: Boolean = true,
    showParticles: Boolean = true,
    showComboCounter: Boolean = true,
    screenShakeEnabled: Boolean = true,
    onMusicVolumeChange: (Float) -> Unit, onSfxVolumeChange: (Float) -> Unit,
    onVibrationToggle: () -> Unit, onLanguageChange: (String) -> Unit,
    onBackgroundChange: (Int) -> Unit, onBgMusicToggle: () -> Unit = {},
    onGraphicsQualityChange: (Int) -> Unit = {},
    onDamageNumbersToggle: () -> Unit = {},
    onParticlesToggle: () -> Unit = {},
    onComboCounterToggle: () -> Unit = {},
    onScreenShakeToggle: () -> Unit = {},
    onBack: () -> Unit
) {
    var showSaveDialog by remember { mutableStateOf(false) }

    HordeScreen {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("⚙ Settings", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = HordeColors.WarmPeach)
            Spacer(Modifier.height(24.dp))

            // Music
            SliderSetting("🎵 Music Volume", musicVolume, onMusicVolumeChange)
            Spacer(Modifier.height(12.dp))

            // SFX
            SliderSetting("🔊 SFX Volume", sfxVolume, onSfxVolumeChange)
            Spacer(Modifier.height(12.dp))

            // Vibration
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(HordeColors.CardBg).padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("📳 Vibration", color = Color.White, fontSize = 16.sp)
                    Switch(checked = vibrationEnabled, onCheckedChange = { onVibrationToggle() },
                        colors = SwitchDefaults.colors(checkedThumbColor = HordeColors.SkyBlue, checkedTrackColor = HordeColors.SkyBlue.copy(alpha = 0.3f)))
                }
            }
            Spacer(Modifier.height(12.dp))

            // Background Music Toggle
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(HordeColors.CardBg).padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("🎵 Background Music", color = Color.White, fontSize = 16.sp)
                    Switch(checked = bgMusicEnabled, onCheckedChange = { onBgMusicToggle() },
                        colors = SwitchDefaults.colors(checkedThumbColor = HordeColors.SkyBlue, checkedTrackColor = HordeColors.SkyBlue.copy(alpha = 0.3f)))
                }
            }
            Spacer(Modifier.height(12.dp))

            // Background Style
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(HordeColors.CardBg).padding(16.dp)) {
                Column {
                    Text("🌌 Background", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))

                    val backgrounds = listOf(
                        0 to "▦ Grid Lines",
                        1 to "✦ Stars Only",
                        2 to "🌌 Nebula",
                        3 to "♟️ Checkerboard",
                        4 to "◼ Solid Dark",
                        5 to "🕌 Persian",
                        6 to "🏛️ Roman",
                        7 to "🔺 Egyptian",
                    )

                    for (row in backgrounds.chunked(2)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            row.forEach { (id, label) ->
                                val sel = backgroundStyle == id
                                Box(
                                    Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                                        .background(if (sel) HordeColors.SkyBlue.copy(alpha = 0.25f) else HordeColors.DarkCard)
                                        .clickable { onBackgroundChange(id) }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(label, color = if (sel) HordeColors.SkyBlue else HordeColors.TextSecondary,
                                        fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp)
                                }
                            }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(6.dp))
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            // Language
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(HordeColors.CardBg).padding(16.dp)) {
                Column {
                    Text("🌐 Language", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    val langs = listOf("en" to "English", "fa" to "فارسی", "zh" to "中文", "ja" to "日本語", "ko" to "한국어", "es" to "Español")
                    for (row in langs.chunked(2)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            row.forEach { (code, label) ->
                                val sel = languageCode == code
                                Box(Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                                    .background(if (sel) HordeColors.SkyBlue.copy(alpha = 0.25f) else HordeColors.DarkCard)
                                    .clickable { onLanguageChange(code) }.padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center) {
                                    Text(label, color = if (sel) HordeColors.SkyBlue else HordeColors.TextSecondary,
                                        fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal, fontSize = 14.sp)
                                }
                            }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(6.dp))
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Graphics & Performance ──
            SectionHeader("🎨 Graphics & Performance")
            Spacer(Modifier.height(10.dp))

            // Graphics Quality
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(HordeColors.CardBg).padding(16.dp)) {
                Column {
                    Text("Quality Level", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    val qualities = listOf(0 to "⚡ Low", 1 to "⚖️ Medium", 2 to "✨ High")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        qualities.forEach { (id, label) ->
                            val sel = graphicsQuality == id
                            Box(
                                Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                                    .background(if (sel) HordeColors.SkyBlue.copy(alpha = 0.25f) else HordeColors.DarkCard)
                                    .clickable { onGraphicsQualityChange(id) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(label, color = if (sel) HordeColors.SkyBlue else HordeColors.TextSecondary,
                                    fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp)
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        when (graphicsQuality) {
                            0 -> "Fewer enemies, no particles, simplified effects"
                            1 -> "Balanced performance and visuals"
                            else -> "Full effects, all particles, max enemies"
                        },
                        fontSize = 11.sp, color = HordeColors.TextSecondary
                    )
                }
            }
            Spacer(Modifier.height(12.dp))

            // ── Accessibility ──
            SectionHeader("♿ Accessibility")
            Spacer(Modifier.height(10.dp))

            ToggleSetting("💥 Damage Numbers", "Show damage popups on hits", showDamageNumbers, onDamageNumbersToggle)
            Spacer(Modifier.height(8.dp))
            ToggleSetting("✨ Particles", "Hit effects, explosions, gems", showParticles, onParticlesToggle)
            Spacer(Modifier.height(8.dp))
            ToggleSetting("🔥 Combo Counter", "Show combo multiplier on screen", showComboCounter, onComboCounterToggle)
            Spacer(Modifier.height(8.dp))
            ToggleSetting("📳 Screen Shake", "Camera shake on hits and bosses", screenShakeEnabled, onScreenShakeToggle)

            Spacer(Modifier.height(24.dp))
            HordeButton(text = "← Back", onClick = { showSaveDialog = true }, color = HordeColors.TextSecondary, isSecondary = true)
        }

        // Save confirmation dialog
        if (showSaveDialog) {
            AlertDialog(
                onDismissRequest = { showSaveDialog = false },
                title = { Text("Save Settings?", fontWeight = FontWeight.Bold) },
                text = { Text("Do you want to save your changes?") },
                confirmButton = {
                    TextButton(onClick = { showSaveDialog = false; onBack() }) {
                        Text("Save & Exit", color = HordeColors.SkyBlue)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSaveDialog = false }) {
                        Text("Cancel", color = HordeColors.TextSecondary)
                    }
                }
            )
        }
    }
}

@Composable
private fun SliderSetting(label: String, value: Float, onChange: (Float) -> Unit) {
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(HordeColors.CardBg).padding(16.dp)) {
        Column {
            Text(label, color = Color.White, fontSize = 16.sp)
            Slider(value = value, onValueChange = onChange,
                colors = SliderDefaults.colors(thumbColor = HordeColors.SkyBlue, activeTrackColor = HordeColors.SkyBlue))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = HordeColors.Lavender,
        modifier = Modifier.fillMaxWidth().padding(start = 4.dp))
}

@Composable
private fun ToggleSetting(title: String, description: String, checked: Boolean, onToggle: () -> Unit) {
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(HordeColors.CardBg).padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, color = Color.White, fontSize = 15.sp)
                Text(description, color = HordeColors.TextSecondary, fontSize = 11.sp)
            }
            Switch(checked = checked, onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(checkedThumbColor = HordeColors.SkyBlue, checkedTrackColor = HordeColors.SkyBlue.copy(alpha = 0.3f)))
        }
    }
}
