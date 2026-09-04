package com.hordesurvival.ui.screens.game

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.hordesurvival.ui.theme.HordeColors
import com.hordesurvival.ui.theme.HordeTypography
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hordesurvival.game.mode.GameModeType
import com.hordesurvival.game.weapon.WeaponType
import com.hordesurvival.ui.components.CornerCutShape
import com.hordesurvival.ui.viewmodel.GameViewModel
import com.hordesurvival.ui.viewmodel.RunSummary

/**
 * Main game screen.
 * Fixed: proper frame-synced game loop, lifecycle handling.
 */
@Composable
fun GameScreen(
    mode: GameModeType = GameModeType.SURVIVAL,
    startingWeapon: WeaponType = WeaponType.MAGIC_MISSILE,
    characterHp: Float = 100f,
    characterSpeed: Float = 200f,
    characterMight: Float = 1f,
    metaHpLevel: Int = 0,
    metaGoldLevel: Int = 0,
    metaMightLevel: Int = 0,
    metaCooldownLevel: Int = 0,
    metaSpeedLevel: Int = 0,
    metaLuckLevel: Int = 0,
    backgroundStyle: Int = 0,
    languageCode: String = "en",
    bgMusicEnabled: Boolean = true,
    graphicsQuality: Int = 1,
    showDamageNumbers: Boolean = true,
    showParticles: Boolean = true,
    showComboCounter: Boolean = true,
    screenShakeEnabled: Boolean = true,
    isContinuing: Boolean = false,
    gameViewModel: GameViewModel = viewModel(),
    onGameOver: (RunSummary) -> Unit,
    onQuit: (RunSummary) -> Unit,
    onSaveRun: ((RunSummary) -> Unit)? = null,
) {
    val isPaused by gameViewModel.isPaused.collectAsState()
    val isGameOver by gameViewModel.isGameOver.collectAsState()
    val showLevelUp by gameViewModel.showLevelUp.collectAsState()
    val bossWarning by gameViewModel.bossWarning.collectAsState()

    val playerHp by gameViewModel.playerHp.collectAsState()
    val playerMaxHp by gameViewModel.playerMaxHp.collectAsState()
    val playerLevel by gameViewModel.playerLevel.collectAsState()
    val currentXp by gameViewModel.currentXp.collectAsState()
    val xpToNext by gameViewModel.xpToNext.collectAsState()
    val gold by gameViewModel.gold.collectAsState()
    val kills by gameViewModel.killCount.collectAsState()
    val gameTime by gameViewModel.gameTime.collectAsState()
    val upgradeOptions by gameViewModel.upgradeOptions.collectAsState()

    val playerWeapons by gameViewModel.playerWeapons.collectAsState()
    val comboCount by gameViewModel.comboCount.collectAsState()
    val comboMultiplier by gameViewModel.comboMultiplier.collectAsState()
    val bossHp by gameViewModel.bossHp.collectAsState()
    val bossMaxHp by gameViewModel.bossMaxHp.collectAsState()
    val bossActive by gameViewModel.bossActive.collectAsState()
    val abilityReady by gameViewModel.abilityReady.collectAsState()
    val abilityCooldown by gameViewModel.abilityCooldown.collectAsState()
    val abilityType = gameViewModel.abilityType
    val gameSpeed by gameViewModel.gameSpeed.collectAsState()
    val achievementPopup by gameViewModel.achievementPopup.collectAsState()
    val tdStageComplete by gameViewModel.tdStageComplete.collectAsState()
    val tdStageNumber by gameViewModel.tdStageNumber.collectAsState()
    val tdStageName by gameViewModel.tdStageName.collectAsState()
    val tdStageKills by gameViewModel.tdStageKills.collectAsState()
    val tdStageGold by gameViewModel.tdStageGold.collectAsState()
    val tdLivesRemaining by gameViewModel.tdLivesRemaining.collectAsState()
    val tdIsVictory by gameViewModel.tdIsVictory.collectAsState()

    // Auto-clear achievement popup after 3 seconds
    LaunchedEffect(achievementPopup) {
        if (achievementPopup != null) {
            kotlinx.coroutines.delay(3000L)
            gameViewModel.clearAchievementPopup()
        }
    }
    // Safety: clear on recomposition if popup is stale
    DisposableEffect(Unit) {
        onDispose { gameViewModel.clearAchievementPopup() }
    }

    // Save callback for lifecycle events (background/kill) — save only, no navigate
    LaunchedEffect(Unit) {
        gameViewModel.onSaveRun = { summary ->
            if (onSaveRun != null) onSaveRun(summary)
            else onQuit(summary) // fallback
        }
    }

    // Handle back press — save and go to menu
    BackHandler {
        if (!gameViewModel.runSaved) {
            gameViewModel.runSaved = true
            com.hordesurvival.game.audio.SoundManager.stopBgMusic()
            onQuit(gameViewModel.getRunSummary())
        }
    }

    // Handle lifecycle — save when app goes to background or is destroyed
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    // App going to background — save run data without navigating
                    if (!gameViewModel.runSaved && !gameViewModel.isGameOver.value) {
                        gameViewModel.runSaved = true
                        com.hordesurvival.game.audio.SoundManager.stopBgMusic()
                        val summary = gameViewModel.getRunSummary()
                        if (onSaveRun != null) onSaveRun(summary)
                        else onQuit(summary)
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Sound settings state
    var musicVol by remember { mutableFloatStateOf(0.5f) }
    var sfxVol by remember { mutableFloatStateOf(0.8f) }
    var bgMusicOn by remember { mutableStateOf(bgMusicEnabled) }
    val context = LocalContext.current

    // Initialize game — fresh start or continue from death
    LaunchedEffect(Unit) {
        if (isContinuing && !gameViewModel.engine.getActiveEntities().isEmpty) {
            // Continue was already handled in MainActivity — just resume the existing game
            gameViewModel.runSaved = false
        } else if (!isContinuing) {
            // Fresh start
            gameViewModel.runSaved = false
            gameViewModel.startGame(mode, startingWeapon, characterHp, characterSpeed, characterMight, metaHpLevel, metaGoldLevel, metaMightLevel, metaCooldownLevel, metaSpeedLevel, metaLuckLevel)
        } else {
            // isContinuing but engine was reset — treat as fresh start
            gameViewModel.runSaved = false
            gameViewModel.startGame(mode, startingWeapon, characterHp, characterSpeed, characterMight, metaHpLevel, metaGoldLevel, metaMightLevel, metaCooldownLevel, metaSpeedLevel, metaLuckLevel)
        }
        // Sync sound volumes from settings
        com.hordesurvival.game.audio.SoundManager.syncVolumes(musicVol, sfxVol)
        // Start background music if enabled
        if (bgMusicEnabled) {
            com.hordesurvival.game.audio.SoundManager.startBgMusic(context)
        }
    }

    // Reset continuing flag after first frame
    LaunchedEffect(Unit) { gameViewModel.runSaved = false }

    // Frame-synced game loop with crash protection
    LaunchedEffect(Unit) {
        while (true) {
            withFrameMillis { _ ->
                try {
                    gameViewModel.update(0f)
                } catch (e: Exception) {
                    android.util.Log.e("GameScreen", "Game loop error", e)
                }
            }
        }
    }

    // Handle game over
    LaunchedEffect(isGameOver) {
        if (isGameOver && !gameViewModel.runSaved) {
            gameViewModel.runSaved = true
            onGameOver(gameViewModel.getRunSummary())
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Game renderer with touch input
        val inputSystem = gameViewModel.inputSystem
        if (inputSystem != null) {
            GameRenderer(
                engine = gameViewModel.engine,
                inputSystem = inputSystem,
                backgroundStyle = backgroundStyle,
                graphicsQuality = graphicsQuality,
                showParticles = showParticles,
                showDamageNumbers = showDamageNumbers,
                gameMode = mode
            )
        }

        // Minimap (top-right, below stats)
        if (!showLevelUp && !isPaused) {
            Minimap(
                engine = gameViewModel.engine,
                modifier = Modifier.align(androidx.compose.ui.Alignment.TopEnd).padding(top = 70.dp, end = 8.dp)
            )
        }

        // HUD (hidden during level-up or pause)
        if (!showLevelUp && !isPaused) {
            GameHud(
                playerHp = playerHp,
                playerMaxHp = playerMaxHp,
                playerLevel = playerLevel,
                currentXp = currentXp,
                xpToNext = xpToNext,
                gold = gold,
                kills = kills,
                gameTime = gameTime,
                currentWeapons = playerWeapons,
                comboCount = comboCount,
                comboMultiplier = comboMultiplier,
                bossHp = bossHp,
                bossMaxHp = bossMaxHp,
                bossActive = bossActive,
                abilityReady = abilityReady,
                abilityCooldown = abilityCooldown,
                abilityType = abilityType,
                showComboCounter = showComboCounter,
                gameSpeed = gameSpeed,
                onPauseClick = { gameViewModel.pauseGame() },
                onAbilityClick = { gameViewModel.useAbility() },
                onSpeedChange = { gameViewModel.setGameSpeed(it) }
            )
        }

        // Achievement popup
        if (achievementPopup != null) {
            Box(
                modifier = Modifier.align(androidx.compose.ui.Alignment.TopCenter).padding(top = 100.dp)
                    .clip(CornerCutShape)
                    .background(HordeColors.GoldColor.copy(alpha = 0.85f))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "🏆 ${achievementPopup}",
                    style = HordeTypography.Value,
                    color = Color.White
                )
            }
        }

        // Boss warning
        BossWarningBanner(
            visible = bossWarning,
            onDismiss = { gameViewModel.dismissBossWarning() }
        )

        // Disable screen shake if setting is off
        if (!screenShakeEnabled) {
            gameViewModel.engine.shakeOffsetX = 0f
            gameViewModel.engine.shakeOffsetY = 0f
        }

        // Level-up screen flash effect
        if (showLevelUp) {
            Box(
                modifier = Modifier.fillMaxSize()
                    .background(HordeColors.SkyBlue.copy(alpha = 0.15f))
            )
        }

        // Level-up overlay
        AnimatedVisibility(
            visible = showLevelUp,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LevelUpScreen(
                level = playerLevel,
                options = upgradeOptions,
                onSelect = { gameViewModel.selectUpgrade(it) },
                languageCode = languageCode
            )
        }

        // Tower Defense stage complete overlay
        if (tdStageComplete) {
            TowerDefenseStageComplete(
                stageNumber = tdStageNumber,
                stageName = tdStageName,
                kills = tdStageKills,
                goldEarned = tdStageGold,
                livesRemaining = tdLivesRemaining,
                isVictory = tdIsVictory,
                languageCode = languageCode,
                onNextStage = {
                    gameViewModel.tdNextStage()
                    gameViewModel.resumeGame()
                },
                onReplayStage = {
                    gameViewModel.tdReplayStage()
                    gameViewModel.resumeGame()
                },
                onMainMenu = {
                    com.hordesurvival.game.audio.SoundManager.stopBgMusic()
                    onQuit(gameViewModel.getRunSummary())
                }
            )
        }

        // Pause overlay
        AnimatedVisibility(
            visible = isPaused && !showLevelUp && !tdStageComplete,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            PauseScreen(
                onResume = { gameViewModel.resumeGame() },
                onQuit = {
                    com.hordesurvival.game.audio.SoundManager.stopBgMusic()
                    gameViewModel.pauseGame()
                    onQuit(gameViewModel.getRunSummary())
                },
                musicVolume = musicVol,
                sfxVolume = sfxVol,
                bgMusicEnabled = bgMusicOn,
                languageCode = languageCode,
                onMusicVolumeChange = { musicVol = it; com.hordesurvival.game.audio.SoundManager.setMusicVolume(it); com.hordesurvival.game.audio.SoundManager.setSfxVolume(sfxVol) },
                onSfxVolumeChange = { sfxVol = it; com.hordesurvival.game.audio.SoundManager.setSfxVolume(it) },
                onBgMusicToggle = { bgMusicOn = !bgMusicOn; com.hordesurvival.game.audio.SoundManager.toggleBgMusic() },
                gameSpeed = gameSpeed,
                onSpeedChange = { gameViewModel.setGameSpeed(it) }
            )
        }
    }
}
