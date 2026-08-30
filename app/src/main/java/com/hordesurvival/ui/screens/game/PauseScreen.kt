package com.hordesurvival.ui.screens.game

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hordesurvival.ui.Locales
import com.hordesurvival.ui.components.HordeBackButton
import com.hordesurvival.ui.components.HordeButton
import com.hordesurvival.ui.components.HordeCard
import com.hordesurvival.ui.components.HordeItemCard
import com.hordesurvival.ui.components.HordeSliderSetting
import com.hordesurvival.ui.components.HordeToggleSetting
import com.hordesurvival.ui.components.HordeSelectorSetting
import com.hordesurvival.ui.components.HordeSecondaryButton
import com.hordesurvival.ui.theme.HordeColors

/**
 * Pause overlay with Resume, Settings, and Quit buttons.
 * Uses HordeUI shared components and localization system.
 */
@Composable
fun PauseScreen(
    onResume: () -> Unit,
    onQuit: () -> Unit,
    musicVolume: Float = 0.5f,
    sfxVolume: Float = 0.8f,
    bgMusicEnabled: Boolean = true,
    gameSpeed: Float = 1f,
    languageCode: String = "en",
    onMusicVolumeChange: (Float) -> Unit = {},
    onSfxVolumeChange: (Float) -> Unit = {},
    onBgMusicToggle: () -> Unit = {},
    onSpeedChange: (Float) -> Unit = {}
) {
    var showSettings by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HordeColors.OverlayDark),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .widthIn(max = 400.dp)
                .fillMaxWidth(0.9f)
                .padding(16.dp)
        ) {
            Text(
                text = "⏸ ${Locales.getString("pause", languageCode).uppercase()}",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(28.dp))

            if (showSettings) {
                // Settings panel wrapped in HordeCard
                HordeCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "⚙ ${Locales.getString("settings", languageCode)}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = HordeColors.WarmPeach,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Background Music Toggle
                    HordeToggleSetting(
                        title = "🎵 ${Locales.getString("background_music", languageCode)}",
                        checked = bgMusicEnabled,
                        onToggle = onBgMusicToggle,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    // Music Volume
                    HordeSliderSetting(
                        label = "🎶 ${Locales.getString("music_volume", languageCode)}",
                        valueText = "${(musicVolume * 100).toInt()}%",
                        value = musicVolume,
                        onValueChange = onMusicVolumeChange,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    // SFX Volume
                    HordeSliderSetting(
                        label = "🔊 ${Locales.getString("sfx_volume", languageCode)}",
                        valueText = "${(sfxVolume * 100).toInt()}%",
                        value = sfxVolume,
                        onValueChange = onSfxVolumeChange,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    HordeBackButton(
                        text = "← ${Locales.getString("back", languageCode)}",
                        onClick = { showSettings = false },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            } else {
                // Main pause buttons
                HordeButton(
                    modifier = Modifier.widthIn(max = 350.dp).fillMaxWidth(0.9f),
                    text = Locales.getString("resume", languageCode),
                    icon = "▶",
                    onClick = onResume
                )
                Spacer(modifier = Modifier.height(12.dp))
                HordeSecondaryButton(
                    modifier = Modifier.widthIn(max = 350.dp).fillMaxWidth(0.9f),
                    text = Locales.getString("settings", languageCode),
                    icon = "⚙",
                    onClick = { showSettings = true }
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Game speed control card
                HordeSelectorSetting(
                    label = "${Locales.getString("speed", languageCode)}:",
                    options = listOf(0.5f to "0.5x", 1f to "1x", 2f to "2x", 3f to "3x"),
                    selectedOption = gameSpeed,
                    onOptionSelected = onSpeedChange,
                    modifier = Modifier.widthIn(max = 350.dp).fillMaxWidth(0.9f)
                )
                Spacer(Modifier.height(24.dp))

                HordeSecondaryButton(
                    modifier = Modifier.widthIn(max = 350.dp).fillMaxWidth(0.9f),
                    text = Locales.getString("quit", languageCode),
                    icon = "🏠",
                    onClick = onQuit
                )
            }
        }
    }
}
