package com.hordesurvival.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hordesurvival.ui.components.HordeBackButton
import com.hordesurvival.ui.components.HordeDialog
import com.hordesurvival.ui.components.HordeHeader
import com.hordesurvival.ui.components.HordeItemCard
import com.hordesurvival.ui.components.HordeScreen
import com.hordesurvival.ui.components.HordeSliderSetting
import com.hordesurvival.ui.components.HordeToggleSetting
import com.hordesurvival.ui.theme.HordeColors
import com.hordesurvival.ui.theme.HordeTypography

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
        Column(Modifier.widthIn(max = 600.dp).fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(16.dp))

            HordeHeader(
                title = "SETTINGS",
                subtitle = "Audio, Display & Preferences",
                icon = "⚙️",
                accentColor = HordeColors.WarmPeach
            )

            Spacer(Modifier.height(24.dp))

            // Audio
            SectionHeader("🎵 Audio & Feedback")
            Spacer(Modifier.height(8.dp))
            HordeSliderSetting("🎵 Music Volume", musicVolume, onMusicVolumeChange)
            Spacer(Modifier.height(8.dp))
            HordeSliderSetting("🔊 SFX Volume", sfxVolume, onSfxVolumeChange)
            Spacer(Modifier.height(8.dp))

            // Vibration
            HordeToggleSetting(
                title = "📳 Vibration",
                checked = vibrationEnabled,
                onToggle = onVibrationToggle
            )
            Spacer(Modifier.height(8.dp))

            // Background Music Toggle
            HordeToggleSetting(
                title = "🎵 Background Music",
                checked = bgMusicEnabled,
                onToggle = onBgMusicToggle
            )
            Spacer(Modifier.height(20.dp))

            // Background Style
            SectionHeader("🌌 Atmosphere")
            Spacer(Modifier.height(8.dp))
            HordeItemCard {
                Column {
                    Text("Background Style", style = HordeTypography.Body.copy(fontWeight = FontWeight.Bold))
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
                                HordeItemCard(
                                    modifier = Modifier.weight(1f),
                                    onClick = { onBackgroundChange(id) },
                                    selected = sel
                                ) {
                                    Text(label, style = HordeTypography.Label.copy(
                                        color = if (sel) HordeColors.SkyBlue else HordeColors.TextSecondary,
                                        fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.sp
                                    ))
                                }
                            }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(6.dp))
                    }
                }
            }
            Spacer(Modifier.height(20.dp))

            // Language
            SectionHeader("🌐 Localization")
            Spacer(Modifier.height(8.dp))
            HordeItemCard {
                Column {
                    Text("Select Language", style = HordeTypography.Body.copy(fontWeight = FontWeight.Bold))
                    Spacer(Modifier.height(10.dp))
                    val langs = listOf("en" to "English", "fa" to "فارسی", "zh" to "中文", "ja" to "日本語", "ko" to "한국어", "es" to "Español")
                    for (row in langs.chunked(2)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            row.forEach { (code, label) ->
                                val sel = languageCode == code
                                HordeItemCard(
                                    modifier = Modifier.weight(1f),
                                    onClick = { onLanguageChange(code) },
                                    selected = sel
                                ) {
                                    Text(label, style = HordeTypography.Body.copy(
                                        color = if (sel) HordeColors.SkyBlue else HordeColors.TextSecondary,
                                        fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal
                                    ))
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
            Spacer(Modifier.height(8.dp))

            // Graphics Quality
            HordeItemCard {
                Column {
                    Text("Quality Level", style = HordeTypography.Body.copy(fontWeight = FontWeight.Bold))
                    Spacer(Modifier.height(10.dp))
                    val qualities = listOf(0 to "⚡ Low", 1 to "⚖️ Medium", 2 to "✨ High")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        qualities.forEach { (id, label) ->
                            val sel = graphicsQuality == id
                            HordeItemCard(
                                modifier = Modifier.weight(1f),
                                onClick = { onGraphicsQualityChange(id) },
                                selected = sel
                            ) {
                                Text(label, style = HordeTypography.Label.copy(
                                    color = if (sel) HordeColors.SkyBlue else HordeColors.TextSecondary,
                                    fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                ))
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
                        style = HordeTypography.Label.copy(fontSize = 11.sp)
                    )
                }
            }
            Spacer(Modifier.height(20.dp))

            // ── Accessibility ──
            SectionHeader("♿ Accessibility & Gameplay")
            Spacer(Modifier.height(8.dp))

            HordeToggleSetting("💥 Damage Numbers", "Show damage popups on hits", showDamageNumbers, onDamageNumbersToggle)
            Spacer(Modifier.height(8.dp))
            HordeToggleSetting("✨ Particles", "Hit effects, explosions, gems", showParticles, onParticlesToggle)
            Spacer(Modifier.height(8.dp))
            HordeToggleSetting("🔥 Combo Counter", "Show combo multiplier on screen", showComboCounter, onComboCounterToggle)
            Spacer(Modifier.height(8.dp))
            HordeToggleSetting("📳 Screen Shake", "Camera shake on hits and bosses", screenShakeEnabled, onScreenShakeToggle)

            Spacer(Modifier.height(28.dp))
            HordeBackButton(onClick = { showSaveDialog = true })
        }

        // Save confirmation dialog
        if (showSaveDialog) {
            HordeDialog(
                onDismissRequest = { showSaveDialog = false },
                title = "Save Settings?",
                text = "Do you want to save your changes?",
                confirmButtonText = "Save & Exit",
                onConfirm = { showSaveDialog = false; onBack() },
                dismissButtonText = "Cancel",
                onDismiss = { showSaveDialog = false }
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(title, style = HordeTypography.SubHeader.copy(fontSize = 17.sp, color = HordeColors.Lavender),
        modifier = Modifier.fillMaxWidth().padding(start = 4.dp))
}
