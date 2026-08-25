package com.hordesurvival.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.hordesurvival.game.audio.SoundManager
import com.hordesurvival.game.engine.GameEngine
import com.hordesurvival.game.engine.ecs.systems.*
import com.hordesurvival.game.component.*
import com.hordesurvival.game.mode.GameModeType
import com.hordesurvival.game.mode.TowerDefenseMode
import com.hordesurvival.game.mode.DailyChallenge
import com.hordesurvival.game.relic.RelicSystem
import com.hordesurvival.game.enemy.EliteAbilities
import com.hordesurvival.game.combo.ComboVisual
import com.hordesurvival.game.achievement.AchievementProgress
import com.hordesurvival.game.character.CharacterAbilities
import com.hordesurvival.game.hazard.StageHazardSystem
import com.hordesurvival.game.upgrade.UpgradeManager
import com.hordesurvival.game.upgrade.UpgradeOption
import com.hordesurvival.game.upgrade.PassiveType
import com.hordesurvival.game.weapon.WeaponType
import com.hordesurvival.game.synergy.WeaponSynergy
import com.hordesurvival.game.blessing.BlessingSystem
import com.hordesurvival.game.pet.CompanionPet
import com.hordesurvival.game.prestige.PrestigeSystem
import com.hordesurvival.utils.Constants
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class GameViewModel(application: Application) : AndroidViewModel(application) {

    val engine = GameEngine()

    private val _playerHp = MutableStateFlow(100f);     val playerHp: StateFlow<Float> = _playerHp
    private val _playerMaxHp = MutableStateFlow(100f);  val playerMaxHp: StateFlow<Float> = _playerMaxHp
    private val _playerLevel = MutableStateFlow(1);     val playerLevel: StateFlow<Int> = _playerLevel
    private val _currentXp = MutableStateFlow(0f);      val currentXp: StateFlow<Float> = _currentXp
    private val _xpToNext = MutableStateFlow(10f);      val xpToNext: StateFlow<Float> = _xpToNext
    private val _gold = MutableStateFlow(0f);           val gold: StateFlow<Float> = _gold
    private val _killCount = MutableStateFlow(0);       val killCount: StateFlow<Int> = _killCount
    private val _gameTime = MutableStateFlow(0f);       val gameTime: StateFlow<Float> = _gameTime
    private val _enemyCount = MutableStateFlow(0);      val enemyCount: StateFlow<Int> = _enemyCount
    private val _isPaused = MutableStateFlow(false);    val isPaused: StateFlow<Boolean> = _isPaused
    private val _isGameOver = MutableStateFlow(false);  val isGameOver: StateFlow<Boolean> = _isGameOver
    private val _showLevelUp = MutableStateFlow(false); val showLevelUp: StateFlow<Boolean> = _showLevelUp
    private val _upgradeOptions = MutableStateFlow<List<UpgradeOption>>(emptyList()); val upgradeOptions: StateFlow<List<UpgradeOption>> = _upgradeOptions
    private val _playerWeapons = MutableStateFlow<List<WeaponType>>(listOf(WeaponType.MAGIC_MISSILE)); val playerWeapons: StateFlow<List<WeaponType>> = _playerWeapons
    private val _bossWarning = MutableStateFlow(false); val bossWarning: StateFlow<Boolean> = _bossWarning
    private val _bossHp = MutableStateFlow(0f); val bossHp: StateFlow<Float> = _bossHp
    private val _bossMaxHp = MutableStateFlow(0f); val bossMaxHp: StateFlow<Float> = _bossMaxHp
    private val _bossActive = MutableStateFlow(false); val bossActive: StateFlow<Boolean> = _bossActive
    private val _comboCount = MutableStateFlow(0); val comboCount: StateFlow<Int> = _comboCount
    private val _comboMultiplier = MutableStateFlow(1f); val comboMultiplier: StateFlow<Float> = _comboMultiplier
    private val _maxCombo = MutableStateFlow(0); val maxCombo: StateFlow<Int> = _maxCombo
    private val _abilityCooldown = MutableStateFlow(0f); val abilityCooldown: StateFlow<Float> = _abilityCooldown
    private val _abilityReady = MutableStateFlow(true); val abilityReady: StateFlow<Boolean> = _abilityReady
    private val _achievementPopup = MutableStateFlow<String?>(null); val achievementPopup: StateFlow<String?> = _achievementPopup
    private val _comboDisplay = MutableStateFlow(ComboVisual.getComboDisplay(0, 1f)); val comboDisplay: StateFlow<ComboVisual.ComboDisplay> = _comboDisplay
    private val _achievementProgress = MutableStateFlow(AchievementProgress.getSortedByProgress()); val achievementProgress: StateFlow<List<AchievementProgress.AchievementTracker>> = _achievementProgress
    private val _relicsCollected = MutableStateFlow<List<RelicSystem.ActiveRelic>>(emptyList()); val relicsCollected: StateFlow<List<RelicSystem.ActiveRelic>> = _relicsCollected
    private val _charAbilityName = MutableStateFlow(""); val charAbilityName: StateFlow<String> = _charAbilityName
    private val _charAbilityIcon = MutableStateFlow(""); val charAbilityIcon: StateFlow<String> = _charAbilityIcon
    private val _charAbilityReady = MutableStateFlow(false); val charAbilityReady: StateFlow<Boolean> = _charAbilityReady
    private val _charAbilityCooldown = MutableStateFlow(0f); val charAbilityCooldown: StateFlow<Float> = _charAbilityCooldown

    private var pendingLevelUp = false
    private var abilityCooldownTimer = 0f
    private var abilityCooldownMax = 15f
    var abilityType = ""
        private set
    private var _inputSystem: PlayerInputSystem? = null; val inputSystem: PlayerInputSystem? get() = _inputSystem
    private var _waveManager: WaveManagerSystem? = null
    private var lastUpdateTimeNs = 0L
    private var bossWarningTimer = 0f
    private var companionPet: CompanionPet? = null
    private var relicSystem: RelicSystem? = null
    private var eliteAbilities: EliteAbilities? = null
    private var characterAbilities: CharacterAbilities? = null
    private var stageHazardSystem: StageHazardSystem? = null
    // Store meta-progression state for applying blessings/prestige
    private var currentMetaGoldLevel = 0
    private var currentPrestigeLevel = 0
    private var blessingLevels: Map<String, Int> = emptyMap()
    // Track synergy state to avoid re-applying every frame
    private var lastSynergyWeapons: Set<WeaponType> = emptySet()
    private var lastSynergyBonuses: WeaponSynergy.SynergyBonuses? = null
    // Daily Challenge
    var dailyChallengeModifiers: DailyChallenge.ChallengeModifiers? = null
        private set

    // Tower Defense stage complete state
    private val _tdStageComplete = MutableStateFlow(false); val tdStageComplete: StateFlow<Boolean> = _tdStageComplete
    private val _tdStageNumber = MutableStateFlow(1); val tdStageNumber: StateFlow<Int> = _tdStageNumber
    private val _tdStageName = MutableStateFlow(""); val tdStageName: StateFlow<String> = _tdStageName
    private val _tdStageKills = MutableStateFlow(0); val tdStageKills: StateFlow<Int> = _tdStageKills
    private val _tdStageGold = MutableStateFlow(0); val tdStageGold: StateFlow<Int> = _tdStageGold
    private val _tdLivesRemaining = MutableStateFlow(3); val tdLivesRemaining: StateFlow<Int> = _tdLivesRemaining
    private val _tdIsVictory = MutableStateFlow(false); val tdIsVictory: StateFlow<Boolean> = _tdIsVictory
    private val _tdMaxUnlockedStage = MutableStateFlow(1); val tdMaxUnlockedStage: StateFlow<Int> = _tdMaxUnlockedStage
    private val _tdScore = MutableStateFlow(0L); val tdScore: StateFlow<Long> = _tdScore
    private val _tdCombo = MutableStateFlow(0); val tdCombo: StateFlow<Int> = _tdCombo
    private val _tdLives = MutableStateFlow(3); val tdLives: StateFlow<Int> = _tdLives

    fun startGame(
        mode: GameModeType = GameModeType.SURVIVAL,
        startingWeapon: WeaponType = WeaponType.MAGIC_MISSILE,
        characterHp: Float = 0f,
        characterSpeed: Float = 0f,
        characterMight: Float = 1f,
        metaHpLevel: Int = 0, metaGoldLevel: Int = 0,
        metaMightLevel: Int = 0, metaCooldownLevel: Int = 0,
        metaSpeedLevel: Int = 0, metaLuckLevel: Int = 0,
        prestigeLevel: Int = 0,
        blessingLevelsMap: Map<String, Int> = emptyMap()
    ) {
        currentMetaGoldLevel = metaGoldLevel
        currentPrestigeLevel = prestigeLevel
        blessingLevels = blessingLevelsMap
        try {
            engine.reset()
            pendingLevelUp = false; lastUpdateTimeNs = 0L; bossWarningTimer = 0f
            SoundManager.initialize(getApplication())

            val baseHp = if (characterHp > 0f) characterHp else Constants.PLAYER_BASE_HP
            val baseSpeed = if (characterSpeed > 0f) characterSpeed else Constants.PLAYER_BASE_SPEED
            // Apply prestige multipliers
            val prestigeBonuses = PrestigeSystem.getCurrentBonuses(prestigeLevel)

            // Apply blessing bonuses
            val blessingMight = BlessingSystem.getEffect(BlessingSystem.allBlessings.find { it.type == BlessingSystem.BlessingType.MIGHT }!!, blessingLevels["MIGHT"] ?: 0)
            val blessingVitality = BlessingSystem.getEffect(BlessingSystem.allBlessings.find { it.type == BlessingSystem.BlessingType.VITALITY }!!, blessingLevels["VITALITY"] ?: 0)
            val blessingSwiftness = BlessingSystem.getEffect(BlessingSystem.allBlessings.find { it.type == BlessingSystem.BlessingType.SWIFTNESS }!!, blessingLevels["SWIFTNESS"] ?: 0)
            val blessingWisdom = BlessingSystem.getEffect(BlessingSystem.allBlessings.find { it.type == BlessingSystem.BlessingType.WISDOM }!!, blessingLevels["WISDOM"] ?: 0)
            val blessingResilience = BlessingSystem.getEffect(BlessingSystem.allBlessings.find { it.type == BlessingSystem.BlessingType.RESILIENCE }!!, blessingLevels["RESILIENCE"] ?: 0)
            val blessingRegen = BlessingSystem.getEffect(BlessingSystem.allBlessings.find { it.type == BlessingSystem.BlessingType.REGEN }!!, blessingLevels["REGEN"] ?: 0)
            val blessingLuck = BlessingSystem.getEffect(BlessingSystem.allBlessings.find { it.type == BlessingSystem.BlessingType.LUCK }!!, blessingLevels["LUCK"] ?: 0)
            val blessingFortune = BlessingSystem.getEffect(BlessingSystem.allBlessings.find { it.type == BlessingSystem.BlessingType.FORTUNE }!!, blessingLevels["FORTUNE"] ?: 0)
            val blessingArcane = BlessingSystem.getEffect(BlessingSystem.allBlessings.find { it.type == BlessingSystem.BlessingType.ARCANE }!!, blessingLevels["ARCANE"] ?: 0)

            val finalHp = baseHp * (1f + 0.05f * metaHpLevel) * prestigeBonuses.hpMultiplier * (1f + blessingVitality)
            val finalSpeed = baseSpeed * (1f + 0.05f * metaSpeedLevel) * prestigeBonuses.speedMultiplier * (1f + blessingSwiftness)
            val finalMight = characterMight * (1f + 0.05f * metaMightLevel) * prestigeBonuses.mightMultiplier * (1f + blessingMight)

            val player = engine.createEntity("player")
            player.add(TransformComponent(0f, 0f))
            player.add(VelocityComponent(speed = finalSpeed))
            player.add(HealthComponent(currentHp = finalHp, maxHp = finalHp, armor = blessingResilience))
            player.add(PlayerComponent(
                might = finalMight,
                cooldownReduction = 0.03f * metaCooldownLevel,
                luck = 0.03f * metaLuckLevel + blessingLuck,
                weapons = mutableListOf(startingWeapon),
                regenRate = 0.2f + blessingRegen,  // base regen + blessing
                xpGain = 1f + blessingWisdom,
                pickupRange = 50f,
                projectileBonus = blessingArcane.toInt(),
                goldGainBonus = blessingFortune
            ))
            player.add(SpriteComponent(width = 32f, height = 32f, color = characterColor(startingWeapon), shape = SpriteShape.CIRCLE))
            player.add(CollisionComponent(radius = 16f))
            player.add(ComboComponent())

            val we = engine.createEntity("player")
            we.add(WeaponStateComponent(type = startingWeapon, baseCooldown = startingWeapon.baseCooldown, baseDamage = startingWeapon.baseDamage, projectileCount = startingWeapon.baseProjectiles, area = startingWeapon.baseArea))

            val input = PlayerInputSystem(engine)
            engine.addSystem(input)
            engine.addSystem(EnemyAISystem(engine))
            engine.addSystem(WeaponSystem(engine))
            engine.addSystem(ProjectileSystem(engine))
            engine.addSystem(MovementSystem())
            engine.addSystem(OrbitSystem(engine))
            engine.addSystem(CollisionSystem(engine))
            engine.addSystem(XpDropSystem(engine))
            engine.addSystem(DamageNumberSystem())
            engine.addSystem(ComboSystem(engine))
            engine.addSystem(ParticleSystem())
            engine.addSystem(LootBoxSystem(engine))
            engine.addSystem(EliteAbilitySystem(engine))
            engine.addSystem(AchievementSystem(engine))

            // Initialize new systems
            relicSystem = RelicSystem(engine)
            eliteAbilities = EliteAbilities(engine)
            characterAbilities = CharacterAbilities(engine)
            characterAbilities?.setAbility(startingWeapon)
            _charAbilityName.value = characterAbilities?.getAbilityName() ?: ""
            _charAbilityIcon.value = characterAbilities?.getAbilityIcon() ?: ""
            stageHazardSystem = StageHazardSystem(engine)

            val wm = WaveManagerSystem(engine)
            wm.onBossSpawned = {
                _bossWarning.value = true
                engine.bossIntroTimer = 0.3f  // Boss intro flash
                engine.shake(intensity = 12f, duration = 0.4f)  // Stronger shake for boss
            }
            wm.gameMode = mode

            // Daily Challenge modifiers
            if (mode == GameModeType.DAILY_CHALLENGE) {
                dailyChallengeModifiers = DailyChallenge.getTodayChallenge()
                wm.enemyHpMult = dailyChallengeModifiers?.enemyHpMult ?: 1f
                wm.enemySpdMult = dailyChallengeModifiers?.enemySpdMult ?: 1f
                wm.enemyDmgMult = dailyChallengeModifiers?.enemyDmgMult ?: 1f
                wm.spawnRateMult = dailyChallengeModifiers?.spawnRateMult ?: 1f
            }
            // Initialize Tower Defense mode if selected
            if (mode == GameModeType.TOWER_DEFENSE) {
                val td = TowerDefenseMode(engine)
                td.screenW = 1080f; td.screenH = 1920f
                td.shipX = td.screenW / 2f; td.shipY = td.screenH - 120f
                wm.towerDefense = td
                // Wire TD gold/score tracking to XpDropSystem
                engine.getSystems().filterIsInstance<XpDropSystem>().firstOrNull()?.towerDefense = td
            }
            engine.addSystem(wm)

            _inputSystem = input; _waveManager = wm
            _playerWeapons.value = listOf(startingWeapon)
            _isGameOver.value = false; _isPaused.value = false; _showLevelUp.value = false

            // Ability system — use CharacterAbilities
            abilityType = characterAbilities?.getAbilityName() ?: ""
            abilityCooldownMax = characterAbilities?.getAbilityState()?.type?.cooldown ?: 15f
            abilityCooldownTimer = 0f
            _abilityReady.value = true

            // Achievement system callback — also grant rewards
            engine.getSystems().filterIsInstance<AchievementSystem>().firstOrNull()?.onAchievementUnlocked = { ach ->
                _achievementPopup.value = ach.name
                // Grant achievement rewards
                val reward = com.hordesurvival.game.achievement.AchievementRewards.getReward(ach.type.name.lowercase())
                if (reward != null) {
                    when (reward.rewardType) {
                        com.hordesurvival.game.achievement.AchievementRewards.RewardType.GOLD -> {
                            player.get<PlayerComponent>()?.let { it.gold += reward.rewardValue }
                        }
                        else -> {} // Other rewards handled by meta-progression
                    }
                }
            }

            // Initialize companion pet system
            companionPet = CompanionPet(engine)
            // Check if player has unlocked pets via achievements
            // Owl is unlocked by "reach_level_50", Dragon by "combo_50"
            // For now, pets are available if player has the meta-progress
        } catch (e: Exception) {
            Log.e("GameViewModel", "startGame error", e)
        }
    }

    fun update(@Suppress("UNUSED_PARAMETER") dt: Float) {
        if (_isPaused.value || _isGameOver.value || pendingLevelUp) return

        try {
            val realDt = if (lastUpdateTimeNs == 0L) 1f / 60f
            else ((System.nanoTime() - lastUpdateTimeNs) / 1_000_000_000f).coerceAtMost(0.1f)
            lastUpdateTimeNs = System.nanoTime()

            engine.update(realDt)

            if (_bossWarning.value) {
                bossWarningTimer += realDt
                if (bossWarningTimer >= 3f) { _bossWarning.value = false; bossWarningTimer = 0f }
            }

            val player = engine.getActiveEntities().find { it.tag == "player" && it.has<PlayerComponent>() }
            if (player != null) {
                val health = player.get<HealthComponent>()
                val comp = player.get<PlayerComponent>()

                if (health != null) {
                    _playerHp.value = health.currentHp
                    _playerMaxHp.value = health.maxHp
                    if (health.isDead) {
                        _isGameOver.value = true; engine.isGameOver = true
                        SoundManager.playGameOver()
                        return
                    }
                }

                if (comp != null) {
                    _playerLevel.value = comp.level
                    _currentXp.value = comp.currentXp
                    _xpToNext.value = comp.xpToNext
                    _gold.value = comp.gold
                    _killCount.value = comp.totalKills
                    _playerWeapons.value = comp.weapons.toList()
                    _waveManager?.playerLevel = comp.level

                    // Apply synergy bonuses ONLY when weapon set changes
                    val currentWeaponSet = comp.weapons.toSet()
                    if (currentWeaponSet != lastSynergyWeapons) {
                        // Remove old synergy bonuses first
                        val oldBonuses = lastSynergyBonuses
                        if (oldBonuses != null) {
                            comp.might -= oldBonuses.bonusMight
                            comp.moveSpeed -= oldBonuses.bonusSpeed
                            comp.cooldownReduction -= oldBonuses.bonusCooldownReduction
                            comp.area -= oldBonuses.bonusArea
                            comp.projectileBonus -= oldBonuses.bonusProjectile
                            comp.regenRate -= oldBonuses.bonusRegen
                            comp.luck -= oldBonuses.bonusLuck
                        }
                        // Apply new synergy bonuses
                        val newBonuses = WeaponSynergy.calculateBonuses(comp.weapons)
                        comp.might += newBonuses.bonusMight
                        comp.moveSpeed += newBonuses.bonusSpeed
                        comp.cooldownReduction = (comp.cooldownReduction + newBonuses.bonusCooldownReduction).coerceAtMost(0.5f)
                        comp.area += newBonuses.bonusArea
                        comp.projectileBonus += newBonuses.bonusProjectile
                        comp.regenRate += newBonuses.bonusRegen
                        comp.luck += newBonuses.bonusLuck
                        lastSynergyWeapons = currentWeaponSet
                        lastSynergyBonuses = newBonuses
                    }

                    // Update combo display
                    player.get<ComboComponent>()?.let { cb ->
                        _comboCount.value = cb.count
                        _comboMultiplier.value = cb.comboMultiplier
                        _maxCombo.value = cb.maxCombo
                    }

                    if (comp.shouldLevelUp() && !pendingLevelUp) triggerLevelUp(comp)
                }
            }

            _gameTime.value = engine.gameTime
            // Track enemy count incrementally instead of scanning every frame
            _enemyCount.value = engine.getActiveEntities().count { it.tag == "enemy" }  // TODO: make incremental

            // Tower Defense: sync ship position to player entity
            val td = _waveManager?.towerDefense
            if (td != null && player != null) {
                // Read joystick input directly for TD
                val input = _inputSystem
                if (input != null && input.isTouching) {
                    td.moveDir = input.joyStickX
                    td.verticalDir = input.joyStickY  // vertical movement in bottom zone
                } else {
                    td.moveDir = 0f
                    td.verticalDir = 0f
                }
                // Update TD ship
                td.update(realDt)
                // Sync position to player entity
                val pTransform = player.get<TransformComponent>()
                if (pTransform != null) {
                    pTransform.x = td.shipX
                    pTransform.y = td.shipY
                }
                // Disable physics velocity (TD controls movement directly)
                player.get<VelocityComponent>()?.let { it.vx = 0f; it.vy = 0f }

                // Sync TD state
                _tdScore.value = td.score
                _tdCombo.value = td.combo
                _tdLives.value = td.lives
                _gold.value = td.gold.toFloat()  // Sync TD gold to UI

                // Sync player HealthComponent damage → TD ship HP
                val playerHp = player.get<HealthComponent>()
                if (playerHp != null) {
                    td.shipHp = playerHp.currentHp
                    td.shipMaxHp = playerHp.maxHp
                    if (playerHp.invincibleTimer > 0f) td.shipInvincible = playerHp.invincibleTimer
                    if (playerHp.isDead && td.lives > 0) {
                        td.lives--
                        playerHp.isDead = false
                        playerHp.currentHp = playerHp.maxHp * 0.5f
                        playerHp.invincibleTimer = 2f
                        td.shipHp = playerHp.currentHp
                    }
                }

                // TD gold/score is now tracked directly by XpDropSystem → td.gold/td.score

                // Sync TD state to ViewModel
                if (td.levelComplete && !td.isTransitioning && !_tdStageComplete.value) {
                    // Stage just completed — show stage complete UI
                    _tdStageComplete.value = true
                    _tdStageNumber.value = td.currentStage - 1
                    _tdStageName.value = td.getStage(td.currentStage - 1).name
                    _tdStageKills.value = td.stageKills
                    _tdStageGold.value = td.getStage(td.currentStage - 1).rewardGold
                    _tdLivesRemaining.value = td.lives
                    _tdIsVictory.value = false
                    engine.isPaused = true
                    _isPaused.value = true
                    // Update max unlocked stage
                    if (td.currentStage > _tdMaxUnlockedStage.value) {
                        _tdMaxUnlockedStage.value = td.currentStage
                    }
                } else if (td.victory && !_tdStageComplete.value) {
                    _tdStageComplete.value = true
                    _tdStageNumber.value = 10
                    _tdStageName.value = td.getStageName()
                    _tdStageKills.value = td.totalKills
                    _tdStageGold.value = td.getStage(10).rewardGold
                    _tdLivesRemaining.value = td.lives
                    _tdIsVictory.value = true
                    engine.isPaused = true
                    _isPaused.value = true
                } else if (td.gameOver && !_isGameOver.value) {
                    _isGameOver.value = true
                    engine.isGameOver = true
                    SoundManager.playGameOver()
                }
            }

            // Update companion pets
            if (player != null) {
                val pPos = player.get<TransformComponent>()
                val pComp = player.get<PlayerComponent>()
                if (pPos != null && pComp != null) {
                    companionPet?.update(realDt, pPos, pComp)

                    // Update relic system
                    val pH = player.get<HealthComponent>()
                    if (pH != null) {
                        relicSystem?.update(realDt, pPos, pComp, pH)
                        _relicsCollected.value = relicSystem?.getActiveRelics() ?: emptyList()
                    }
                }
            }

            // Update elite abilities
            if (player != null) {
                val pPos = player.get<TransformComponent>()
                eliteAbilities?.update(realDt, engine.getActiveEntities(), pPos, player.get<HealthComponent>())

                // Update stage hazards
                val pH = player.get<HealthComponent>()
                val pComp = player.get<PlayerComponent>()
                if (pPos != null && pH != null && pComp != null) {
                    stageHazardSystem?.update(realDt, pPos, pH, pComp)
                }
            }

            // Update character abilities
            characterAbilities?.update(realDt)
            _charAbilityReady.value = characterAbilities?.isReady() ?: false
            _charAbilityCooldown.value = characterAbilities?.getCooldownProgress() ?: 0f

            // Update combo visual
            val comboComp = player?.get<ComboComponent>()
            if (comboComp != null) {
                _comboDisplay.value = ComboVisual.getComboDisplay(comboComp.count, comboComp.comboMultiplier)
            }

            // Update achievement progress
            if (player != null) {
                val comp = player.get<PlayerComponent>()
                if (comp != null) {
                    AchievementProgress.updateProgress("kill", comp.totalKills)
                    AchievementProgress.updateProgress("reach_level", comp.level)
                    AchievementProgress.updateProgress("survive", engine.gameTime.toInt())
                    AchievementProgress.updateProgress("combo", comboComp?.maxCombo ?: 0)
                    _achievementProgress.value = AchievementProgress.getSortedByProgress()
                }
            }

            // Update ability cooldown (sync with CharacterAbilities)
            if (!_abilityReady.value) {
                abilityCooldownTimer -= realDt
                _abilityCooldown.value = (abilityCooldownTimer / abilityCooldownMax).coerceIn(0f, 1f)
                if (abilityCooldownTimer <= 0f) {
                    _abilityReady.value = true
                    _abilityCooldown.value = 0f
                }
            }
            // Sync with CharacterAbilities system
            _charAbilityReady.value = characterAbilities?.isReady() ?: false
            _charAbilityCooldown.value = characterAbilities?.getCooldownProgress() ?: 0f

            // Achievement popup auto-clear handled by UI

            // Track boss HP
            val boss = engine.getActiveEntities().find { it.tag == "enemy" && it.get<EnemyComponent>()?.isBoss == true }
            if (boss != null) {
                val bossHpComp = boss.get<HealthComponent>()
                _bossHp.value = bossHpComp?.currentHp ?: 0f
                _bossMaxHp.value = bossHpComp?.maxHp ?: 0f
                _bossActive.value = true
                val bossDead = bossHpComp?.isDead == true
                if (bossDead && !bossWasDeadLastFrame) bossKillCount++
                bossWasDeadLastFrame = bossDead
            } else {
                _bossActive.value = false
                _bossHp.value = 0f
                _bossMaxHp.value = 0f
            }
        } catch (e: Exception) {
            Log.e("GameViewModel", "update error", e)
            // Don't crash — continue next frame
        }
    }

    private fun triggerLevelUp(player: PlayerComponent) {
        pendingLevelUp = true
        _isPaused.value = true; engine.isPaused = true; _showLevelUp.value = true
        SoundManager.playLevelUp()

        player.currentXp -= player.xpToNext
        player.level++
        player.xpToNext = player.calculateXpForLevel(player.level)
        _playerLevel.value = player.level

        _upgradeOptions.value = UpgradeManager.getUpgradeOptions(player.weapons, player.passiveLevels, player.level)
    }

    fun selectUpgrade(option: UpgradeOption) {
        try {
            val player = engine.getActiveEntities().find { it.tag == "player" && it.has<PlayerComponent>() }
            val comp = player?.get<PlayerComponent>() ?: return

            when (option.type) {
                com.hordesurvival.game.upgrade.UpgradeType.WEAPON_UPGRADE -> {
                    option.weaponType?.let { w ->
                        comp.passiveLevels["WPN_${w.name}"] = option.targetTier
                        engine.getActiveEntities().filter { it.has<WeaponStateComponent>() }.forEach { e ->
                            val ws = e.get<WeaponStateComponent>()!!
                            if (ws.type == w) { ws.tier = option.targetTier; applyWeaponTier(ws, w, option.targetTier) }
                        }
                    }
                }
                com.hordesurvival.game.upgrade.UpgradeType.NEW_WEAPON -> {
                    option.weaponType?.let { w ->
                        if (w !in comp.weapons) {
                            comp.weapons.add(w); _playerWeapons.value = comp.weapons.toList()
                            engine.createEntity("player").add(WeaponStateComponent(type = w, baseCooldown = w.baseCooldown, baseDamage = w.baseDamage, projectileCount = w.baseProjectiles, area = w.baseArea))
                        }
                    }
                }
                com.hordesurvival.game.upgrade.UpgradeType.PASSIVE -> {
                    option.passiveType?.let { p ->
                        val lvl = (comp.passiveLevels[p.name] ?: 0) + 1
                        comp.passiveLevels[p.name] = lvl
                        applyPassive(player, comp, p, lvl)
                    }
                }
                com.hordesurvival.game.upgrade.UpgradeType.HEAL -> {
                    player.get<HealthComponent>()?.let { it.heal(it.maxHp * 0.2f) }
                }
            }

            _showLevelUp.value = false; pendingLevelUp = false; _isPaused.value = false; engine.isPaused = false
        } catch (e: Exception) {
            Log.e("GameViewModel", "selectUpgrade error", e)
            _showLevelUp.value = false; pendingLevelUp = false; _isPaused.value = false; engine.isPaused = false
        }
    }

    private fun applyWeaponTier(ws: WeaponStateComponent, w: WeaponType, tier: Int) {
        // Reset to base values before applying cumulative tiers
        ws.baseDamage = w.baseDamage
        ws.baseCooldown = w.baseCooldown
        ws.projectileCount = w.baseProjectiles
        ws.area = w.baseArea
        ws.specialEffect = ""

        // Tier 6 = Weapon Evolution
        if (tier == 6) {
            val evoPassives = mutableMapOf<String, Int>()
            // Gather passive levels from the player component
            val player = engine.getActiveEntities().find { it.has<PlayerComponent>() }
            player?.get<PlayerComponent>()?.passiveLevels?.let { evoPassives.putAll(it) }
            val evolution = com.hordesurvival.game.weapon.WeaponEvolution.findEvolution(w, evoPassives)
            if (evolution != null) {
                ws.baseDamage *= evolution.damageMultiplier
                ws.baseCooldown *= evolution.cooldownMultiplier
                ws.specialEffect = evolution.specialEffect
                // Evolved weapons get bonus projectiles
                ws.projectileCount += 2
            }
            return
        }
        // Apply all tier effects cumulatively up to current tier
        when (w) {
            WeaponType.MAGIC_MISSILE -> {
                if (tier >= 1) ws.projectileCount++
                if (tier >= 2) ws.baseDamage *= 1.2f
                if (tier >= 3) ws.projectileCount++
                if (tier >= 4) {} // homing strength handled in fire
            }
            WeaponType.LIGHTNING_RING -> {
                if (tier >= 1) ws.area *= 1.2f
                if (tier >= 2) ws.baseDamage *= 1.15f
                if (tier >= 4) ws.area *= 1.2f
            }
            WeaponType.FIREBALL -> {
                if (tier >= 1) ws.area *= 1.3f
                if (tier >= 2) ws.baseDamage *= 1.2f
            }
            WeaponType.ICE_SHARD -> {
                if (tier >= 1) ws.projectileCount++
                if (tier >= 2) ws.baseDamage *= 1.2f
                if (tier >= 4) ws.projectileCount++
            }
            WeaponType.POISON_CLOUD -> {
                if (tier >= 1) ws.projectileCount++
                if (tier >= 2) ws.baseDamage *= 1.15f
                if (tier >= 3) ws.baseDamage *= 1.15f
                if (tier >= 4) ws.projectileCount++
            }
            WeaponType.BOOMERANG_DAGGER -> {
                if (tier >= 2) ws.baseDamage *= 1.2f
            }
            WeaponType.ORBITING_SHIELD -> {
                if (tier >= 1) ws.projectileCount++
                if (tier >= 4) ws.projectileCount++
            }
            WeaponType.DIVINE_SPEAR -> {
                if (tier >= 2) ws.baseDamage *= 1.2f
            }
        }
    }

    private fun applyPassive(pe: com.hordesurvival.game.engine.ecs.Entity, comp: PlayerComponent, p: PassiveType, lvl: Int) {
        val hp = pe.get<HealthComponent>()
        when (p) {
            PassiveType.SPINACH -> comp.might = p.applyEffect(1f, lvl)
            PassiveType.EMPTY_TOME -> comp.cooldownReduction = 0.08f * lvl
            PassiveType.CROWN -> comp.area = p.applyEffect(1f, lvl)
            PassiveType.WINGS -> comp.moveSpeed = p.applyEffect(1f, lvl)
            PassiveType.DUPLICATOR -> comp.projectileBonus = lvl
            PassiveType.SHIELD -> hp?.let { it.armor = p.applyEffect(0f, lvl) }
            PassiveType.HEART -> hp?.let { h -> h.maxHp = p.applyEffect(h.maxHp, lvl); h.heal(20f * lvl) }
            PassiveType.CLOVER -> comp.luck = 0.05f * lvl
            PassiveType.MAGNET -> comp.pickupRange = p.applyEffect(50f, lvl)
            PassiveType.GROWTH -> comp.xpGain = p.applyEffect(1f, lvl)
            PassiveType.SPEEDSTER -> comp.attackSpeed = p.applyEffect(1f, lvl)
            PassiveType.VAMPIRE -> {
                // Keep base regen (0.2) and add vampire bonus on top
                val baseRegen = 0.2f
                comp.regenRate = baseRegen + p.applyEffect(0f, lvl)
            }
        }
    }

    /** Use character ability */
    fun useAbility() {
        if (!_abilityReady.value || _isPaused.value || _isGameOver.value) return
        val player = engine.getActiveEntities().find { it.tag == "player" && it.has<PlayerComponent>() } ?: return
        val activated = characterAbilities?.activate(player) ?: false
        if (activated) {
            abilityCooldownTimer = characterAbilities?.getAbilityState()?.type?.cooldown ?: 15f
            _abilityReady.value = false
            SoundManager.playLevelUp()
        }
    }

    fun clearAchievementPopup() { _achievementPopup.value = null }

    fun pauseGame() { _isPaused.value = true; engine.isPaused = true }
    fun resumeGame() { _isPaused.value = false; engine.isPaused = false; lastUpdateTimeNs = System.nanoTime() }

    /** Continue after death — revive player with 50% HP */
    /** Reset all ViewModel state for a fresh game or continue */
    fun resetForNewGame() {
        engine.reset()
        _isGameOver.value = false
        _isPaused.value = false
        _showLevelUp.value = false
        _playerHp.value = 100f
        _playerMaxHp.value = 100f
        _playerLevel.value = 1
        _currentXp.value = 0f
        _xpToNext.value = 10f
        _gold.value = 0f
        _killCount.value = 0
        _gameTime.value = 0f
        _enemyCount.value = 0
        _playerWeapons.value = listOf(WeaponType.MAGIC_MISSILE)
        _bossWarning.value = false
        _bossHp.value = 0f
        _bossMaxHp.value = 0f
        _bossActive.value = false
        _comboCount.value = 0
        _comboMultiplier.value = 1f
        _maxCombo.value = 0
        _abilityReady.value = true
        _abilityCooldown.value = 0f
        _gameSpeed.value = 1f
        _achievementPopup.value = null
        pendingLevelUp = false
        lastUpdateTimeNs = 0L
        bossWarningTimer = 0f
        bossKillCount = 0
        bossWasDeadLastFrame = false
        abilityCooldownTimer = 0f
        companionPet?.reset()
        companionPet = null
        characterAbilities?.reset()
        characterAbilities = null
        stageHazardSystem?.reset()
        stageHazardSystem = null
        relicSystem?.reset()
        relicSystem = null
        eliteAbilities?.reset()
        eliteAbilities = null
        lastSynergyWeapons = emptySet()
        lastSynergyBonuses = null
        dailyChallengeModifiers = null
        runSaved = false
    }

    fun continueGame() {
        val player = engine.getActiveEntities().find { it.tag == "player" && it.has<PlayerComponent>() }
        val health = player?.get<HealthComponent>() ?: return
        health.isDead = false
        health.currentHp = health.maxHp * 0.5f
        health.invincibleTimer = 3f  // 3 seconds of invincibility
        _isGameOver.value = false
        engine.isGameOver = false
        _playerHp.value = health.currentHp
        lastUpdateTimeNs = System.nanoTime()
        runSaved = false
        bossWasDeadLastFrame = false
    }
    fun dismissBossWarning() { _bossWarning.value = false; bossWarningTimer = 0f }

    // ── Tower Defense Stage Management ──
    fun tdNextStage() {
        _tdStageComplete.value = false
        val td = _waveManager?.towerDefense ?: return
        td.startStage(td.currentStage)
        lastUpdateTimeNs = System.nanoTime()
    }

    fun tdReplayStage() {
        _tdStageComplete.value = false
        val td = _waveManager?.towerDefense ?: return
        td.startStage(td.currentStage - 1)
        lastUpdateTimeNs = System.nanoTime()
    }

    fun tdStartFromStage(stage: Int) {
        _tdStageComplete.value = false
        val td = _waveManager?.towerDefense ?: return
        td.startStage(stage)
        lastUpdateTimeNs = System.nanoTime()
    }

    // ── Game Speed Control ──
    private val _gameSpeed = MutableStateFlow(1f); val gameSpeed: StateFlow<Float> = _gameSpeed
    fun setGameSpeed(speed: Float) {
        _gameSpeed.value = speed
        engine.gameSpeed = speed
    }
    fun cycleGameSpeed() {
        val speeds = listOf(0.5f, 1f, 2f, 3f)
        val current = speeds.indexOf(_gameSpeed.value).coerceAtLeast(0)
        val next = speeds[(current + 1) % speeds.size]
        setGameSpeed(next)
    }

    fun getRunSummary() = RunSummary(
        timeSurvived = engine.gameTime,
        kills = _killCount.value, level = _playerLevel.value,
        goldEarned = _gold.value.toInt(),
        weapons = _playerWeapons.value,
        maxCombo = _maxCombo.value,
        bossesKilled = bossKillCount
    )

    private var bossKillCount = 0
    private var bossWasDeadLastFrame = false

    /** Callback to save run data — set by GameScreen */
    var onSaveRun: ((RunSummary) -> Unit)? = null

    /** Whether run has been saved (prevents double-save) */
    var runSaved = false

    override fun onCleared() {
        // Emergency save on destroy (app kill, back press, etc.)
        if (!runSaved) {
            runSaved = true
            try {
                val summary = getRunSummary()
                // Use runBlocking to ensure save completes before process dies
                onSaveRun?.invoke(summary)
            } catch (e: Exception) {
                Log.e("GameViewModel", "Emergency save failed", e)
            }
        }
        super.onCleared()
        engine.reset()
        SoundManager.release()
    }

    private fun characterColor(w: WeaponType): Int = when (w) {
        WeaponType.MAGIC_MISSILE -> 0xFF6BB6FF.toInt()
        WeaponType.LIGHTNING_RING -> 0xFF80DEEA.toInt()
        WeaponType.FIREBALL -> 0xFFFFAB91.toInt()
        WeaponType.ICE_SHARD -> 0xFF80CBC4.toInt()
        WeaponType.POISON_CLOUD -> 0xFFCE93D8.toInt()
        WeaponType.BOOMERANG_DAGGER -> 0xFFFFDAC1.toInt()
        WeaponType.ORBITING_SHIELD -> 0xFFB19CD9.toInt()
        WeaponType.DIVINE_SPEAR -> 0xFFFFF5E1.toInt()
    }
}

data class RunSummary(
    val timeSurvived: Float,
    val kills: Int,
    val level: Int,
    val goldEarned: Int,
    val weapons: List<WeaponType>,
    val maxCombo: Int = 0,
    val bossesKilled: Int = 0
)
