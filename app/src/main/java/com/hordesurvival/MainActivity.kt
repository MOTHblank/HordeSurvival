package com.hordesurvival

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hordesurvival.data.model.RunRecord
import com.hordesurvival.data.model.UnlockedCharacter
import com.hordesurvival.game.mode.GameModeType
import com.hordesurvival.game.weapon.WeaponType
import com.hordesurvival.ui.screens.characterselect.CharacterSelectScreen
import com.hordesurvival.ui.screens.game.GameScreen
import com.hordesurvival.ui.screens.gameover.GameOverScreen
import com.hordesurvival.ui.screens.menu.MainMenuScreen
import com.hordesurvival.ui.screens.menu.ModeSelectScreen
import com.hordesurvival.ui.screens.settings.SettingsScreen
import com.hordesurvival.ui.screens.shop.ItemShopScreen
import com.hordesurvival.ui.screens.stats.StatsScreen
import com.hordesurvival.ui.screens.tutorial.TutorialScreen
import com.hordesurvival.ui.screens.upgrades.UpgradesScreen
import com.hordesurvival.ui.viewmodel.MainViewModel
import com.hordesurvival.ui.viewmodel.GameViewModel
import com.hordesurvival.ui.viewmodel.RunSummary
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { HordeSurvivalGameNav() }
    }
}

@Composable
fun HordeSurvivalGameNav() {
    val nav = rememberNavController()
    val vm: MainViewModel = viewModel()
    val gameVm: GameViewModel = viewModel()
    val save by vm.playerSave.collectAsState()
    val chars by vm.characters.collectAsState()
    val lang = save.languageCode

    var selChar by remember { mutableStateOf<UnlockedCharacter?>(null) }
    var selMode by remember { mutableStateOf(GameModeType.SURVIVAL) }
    var lastSummary by remember { mutableStateOf(RunSummary(0f, 0, 0, 0, emptyList())) }
    var canContinue by remember { mutableStateOf(false) }
    var isContinuing by remember { mutableStateOf(false) }

    NavHost(navController = nav, startDestination = "main_menu", modifier = Modifier.fillMaxSize()) {

        composable("tutorial") {
            TutorialScreen(
                languageCode = lang,
                onFinish = { nav.popBackStack() },
                onSkip = { nav.popBackStack() }
            )
        }

        composable("main_menu") {
            MainMenuScreen(
                onPlayClick = { nav.navigate("mode_select") },
                onCharactersClick = { nav.navigate("character_select") },
                onUpgradesClick = { nav.navigate("upgrades") },
                onSettingsClick = { nav.navigate("settings") },
                onStatsClick = { nav.navigate("stats") },
                onShopClick = { nav.navigate("shop") },
                onTutorialClick = { nav.navigate("tutorial") },
                gold = save.totalGold, languageCode = lang
            )
        }

        composable("mode_select") {
            ModeSelectScreen(
                onModeSelected = { selMode = it; nav.navigate("game") { popUpTo("main_menu") { inclusive = false } } },
                onBack = { nav.popBackStack() }, languageCode = lang
            )
        }

        composable("character_select") {
            CharacterSelectScreen(
                characters = chars, selectedId = selChar?.characterId ?: 0,
                onSelect = { selChar = it },
                onConfirm = { if (selChar != null) nav.navigate("game") { popUpTo("main_menu") { inclusive = false } } },
                onBack = { nav.popBackStack() },
                onUnlock = { id, cost -> vm.unlockCharacter(id, cost) },
                gold = save.totalGold, languageCode = lang
            )
        }

        composable("game") {
            val c = selChar
            canContinue = true
            var gameSaved by remember { mutableStateOf(false) }
            GameScreen(
                mode = selMode,
                isContinuing = isContinuing,
                gameViewModel = gameVm,
                startingWeapon = WeaponType.valueOf(c?.startingWeapon ?: "MAGIC_MISSILE"),
                characterHp = c?.baseHp ?: 100f,
                characterSpeed = c?.baseSpeed ?: 200f,
                characterMight = c?.baseMight ?: 1f,
                metaHpLevel = save.metaHpLevel, metaGoldLevel = save.metaGoldLevel,
                metaMightLevel = save.metaMightLevel, metaCooldownLevel = save.metaCooldownLevel,
                metaSpeedLevel = save.metaSpeedLevel, metaLuckLevel = save.metaLuckLevel,
                backgroundStyle = save.backgroundStyle,
                languageCode = lang,
                bgMusicEnabled = save.bgMusicEnabled,
                graphicsQuality = save.graphicsQuality,
                showDamageNumbers = save.showDamageNumbers,
                showParticles = save.showParticles,
                showComboCounter = save.showComboCounter,
                screenShakeEnabled = save.screenShakeEnabled,
                onGameOver = { s ->
                    lastSummary = s
                    if (!gameSaved) {
                        gameSaved = true
                        vm.recordRun(RunRecord(
                            characterId = c?.characterId ?: 0, gameMode = selMode.name,
                            timeSurvived = s.timeSurvived, level = s.level, kills = s.kills,
                            goldEarned = s.goldEarned, weaponsUsed = s.weapons.joinToString(",") { it.name }
                        ))
                    }
                    nav.navigate("game_over") { popUpTo("game") { inclusive = true } }
                },
                onQuit = { s ->
                    if (!gameSaved) {
                        gameSaved = true
                        vm.recordRun(RunRecord(
                            characterId = c?.characterId ?: 0, gameMode = selMode.name,
                            timeSurvived = s.timeSurvived, level = s.level, kills = s.kills,
                            goldEarned = s.goldEarned, weaponsUsed = s.weapons.joinToString(",") { it.name }
                        ))
                    }
                    nav.navigate("main_menu") { popUpTo("main_menu") { inclusive = true } }
                },
                onSaveRun = { s ->
                    // Save only (for lifecycle/background) — no navigation
                    if (!gameSaved) {
                        gameSaved = true
                        vm.recordRun(RunRecord(
                            characterId = c?.characterId ?: 0, gameMode = selMode.name,
                            timeSurvived = s.timeSurvived, level = s.level, kills = s.kills,
                            goldEarned = s.goldEarned, weaponsUsed = s.weapons.joinToString(",") { it.name }
                        ))
                    }
                }
            )
        }

        composable("game_over") {
            val activity = androidx.compose.ui.platform.LocalContext.current as? android.app.Activity
            GameOverScreen(lastSummary,
                onPlayAgain = {
                    // Full reset — start a completely new game
                    isContinuing = false
                    gameVm.resetForNewGame()
                    nav.navigate("game") {
                        popUpTo("game") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onMainMenu = {
                    // Go to menu — game is over, reset state
                    isContinuing = false
                    gameVm.resetForNewGame()
                    nav.navigate("main_menu") { popUpTo("main_menu") { inclusive = true } }
                },
                onContinue = {
                    // Continue from death — revive BEFORE navigating
                    gameVm.continueGame()
                    isContinuing = true  // Tell GameScreen we're continuing, not restarting
                    canContinue = false  // Only one continue per run
                    nav.navigate("game") {
                        popUpTo("game") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                canContinue = canContinue,
                activity = activity,
                languageCode = lang
            )
        }

        composable("upgrades") {
            UpgradesScreen(save, onUpgrade = { stat, cost -> vm.upgradeMeta(stat, cost) }, onBack = { nav.popBackStack() }, languageCode = lang)
        }

        composable("stats") {
            StatsScreen(save, languageCode = lang, onBack = { nav.popBackStack() })
        }

        composable("shop") {
            ItemShopScreen(
                gold = save.totalGold,
                onPurchase = { id, cost -> vm.addGold(-cost) },
                onBack = { nav.popBackStack() }
            )
        }

        composable("settings") {
            SettingsScreen(save.musicVolume, save.sfxVolume, save.vibrationEnabled, lang, save.backgroundStyle,
                bgMusicEnabled = save.bgMusicEnabled,
                graphicsQuality = save.graphicsQuality,
                showDamageNumbers = save.showDamageNumbers,
                showParticles = save.showParticles,
                showComboCounter = save.showComboCounter,
                screenShakeEnabled = save.screenShakeEnabled,
                onMusicVolumeChange = { vm.updateVolume(it, save.sfxVolume) },
                onSfxVolumeChange = { vm.updateVolume(save.musicVolume, it) },
                onVibrationToggle = { vm.toggleVibration() },
                onLanguageChange = { vm.setLanguage(it) },
                onBackgroundChange = { vm.setBackgroundStyle(it) },
                onBgMusicToggle = { vm.toggleBgMusic() },
                onGraphicsQualityChange = { vm.setGraphicsQuality(it) },
                onDamageNumbersToggle = { vm.toggleDamageNumbers() },
                onParticlesToggle = { vm.toggleParticles() },
                onComboCounterToggle = { vm.toggleComboCounter() },
                onScreenShakeToggle = { vm.toggleScreenShake() },
                onBack = { nav.popBackStack() })
        }
    }
}
