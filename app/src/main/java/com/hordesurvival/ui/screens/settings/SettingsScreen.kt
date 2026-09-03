package com.hordesurvival.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hordesurvival.ui.components.HordeBackButton
import com.hordesurvival.ui.components.HordeDialog
import com.hordesurvival.ui.components.HordeHeader
import com.hordesurvival.ui.components.HordeScreen
import com.hordesurvival.ui.components.HordeSliderSetting
import com.hordesurvival.ui.components.HordeToggleSetting
import com.hordesurvival.ui.components.HordeSelectorSetting
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
            HordeSliderSetting(
                title = "Music Volume",
                icon = "🎵",
                value = musicVolume,
                onValueChange = onMusicVolumeChange,
                valueText = "${(musicVolume * 100).toInt()}%"
            )
            Spacer(Modifier.height(8.dp))
            HordeSliderSetting(
                title = "SFX Volume",
                icon = "🔊",
                value = sfxVolume,
                onValueChange = onSfxVolumeChange,
                valueText = "${(sfxVolume * 100).toInt()}%"
            )
            Spacer(Modifier.height(8.dp))
            HordeToggleSetting(
                title = "Vibration",
                icon = "📳",
                checked = vibrationEnabled,
                onToggle = onVibrationToggle
            )
            Spacer(Modifier.height(8.dp))
            HordeToggleSetting(
                title = "Background Music",
                icon = "🎵",
                checked = bgMusicEnabled,
                onToggle = onBgMusicToggle
            )

            Spacer(Modifier.height(20.dp))

            // Background Style
            SectionHeader("🌌 Atmosphere")
            Spacer(Modifier.height(8.dp))

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
            HordeSelectorSetting(
                title = "Background Style",
                options = backgrounds,
                selectedOption = backgroundStyle,
                onOptionSelected = onBackgroundChange,
                columns = 2
            )

            Spacer(Modifier.height(20.dp))

            // Language
            SectionHeader("🌐 Localization")
            Spacer(Modifier.height(8.dp))
            val langs = listOf("en" to "English", "fa" to "فارسی", "zh" to "中文", "ja" to "日本語", "ko" to "한국어", "es" to "Español")
            HordeSelectorSetting(
                title = "Select Language",
                options = langs,
                selectedOption = languageCode,
                onOptionSelected = onLanguageChange,
                columns = 2
            )

            Spacer(Modifier.height(20.dp))

            // ── Graphics & Performance ──
            SectionHeader("🎨 Graphics & Performance")
            Spacer(Modifier.height(8.dp))

            val qualities = listOf(0 to "⚡ Low", 1 to "⚖️ Medium", 2 to "✨ High")
            val qualityDesc = when (graphicsQuality) {
                0 -> "Fewer enemies, no particles, simplified effects"
                1 -> "Balanced performance and visuals"
                else -> "Full effects, all particles, max enemies"
            }
            HordeSelectorSetting(
                title = "Quality Level",
                description = qualityDesc,
                options = qualities,
                selectedOption = graphicsQuality,
                onOptionSelected = onGraphicsQualityChange,
                columns = 3
            )

            Spacer(Modifier.height(20.dp))

            // ── Accessibility ──
            SectionHeader("♿ Accessibility & Gameplay")
            Spacer(Modifier.height(8.dp))

            HordeToggleSetting(
                title = "Damage Numbers",
                icon = "💥",
                description = "Show damage popups on hits",
                checked = showDamageNumbers,
                onToggle = onDamageNumbersToggle
            )
            Spacer(Modifier.height(8.dp))
            HordeToggleSetting(
                title = "Particles",
                icon = "✨",
                description = "Hit effects, explosions, gems",
                checked = showParticles,
                onToggle = onParticlesToggle
            )
            Spacer(Modifier.height(8.dp))
            HordeToggleSetting(
                title = "Combo Counter",
                icon = "🔥",
                description = "Show combo multiplier on screen",
                checked = showComboCounter,
                onToggle = onComboCounterToggle
            )
            Spacer(Modifier.height(8.dp))
            HordeToggleSetting(
                title = "Screen Shake",
                icon = "📳",
                description = "Camera shake on hits and bosses",
                checked = screenShakeEnabled,
                onToggle = onScreenShakeToggle
            )

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
    Text(title, style = HordeTypography.SubHeader, color = HordeColors.Lavender,
        modifier = Modifier.fillMaxWidth().padding(start = 4.dp))
}
