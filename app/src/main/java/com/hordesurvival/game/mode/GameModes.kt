package com.hordesurvival.game.mode

import com.hordesurvival.game.component.*
import com.hordesurvival.game.engine.GameEngine
import com.hordesurvival.game.enemy.EnemyType
import com.hordesurvival.game.weapon.WeaponType
import com.hordesurvival.utils.GameMath
import kotlin.math.cos
import kotlin.math.sin

/**
 * Tower Defense Mode — Vertical Shooter (1942/Raiden style)
 *
 * FIXED screen. Player ship at 80% height.
 * Enemies spawn from TOP, move DOWN in formations.
 * Kill enemies → auto-reward (gold + score).
 * No manual pickup — everything auto-collects.
 * Weapons auto-fire upward. Upgrade between stages.
 * 10 stages with boss at end of each.
 */
class TowerDefenseMode(private val engine: GameEngine) {

    // ── TD Weapon ─────────────────────────────────────────────────
    data class TDWeapon(
        val id: String,
        val name: String,
        val icon: String,
        val baseDamage: Float,
        val fireRate: Float,
        val projectileSpeed: Float,
        val projectileCount: Int = 1,
        val spreadAngle: Float = 0f,
        val piercing: Boolean = false,
        val aoeRadius: Float = 0f,
        val color: Long,
        var level: Int = 1,
        val maxLevel: Int = 10,
        val description: String = ""
    ) {
        val damage: Float get() = baseDamage * (1f + level * 0.3f)
        val currentFireRate: Float get() = fireRate * (1f + level * 0.06f)
        val currentProjectileCount: Int get() = projectileCount + (level / 3)
    }

    // TD-specific weapons (vertical shooter style)
    val tdWeapons = mutableListOf(
        TDWeapon("vulcan", "Vulcan Cannon", "🔫", baseDamage = 8f, fireRate = 6f, projectileSpeed = 900f,
            projectileCount = 1, color = 0xFF00E5FF, maxLevel = 10,
            description = "Fast single-shot. Upgrades add bullets + damage."),
        TDWeapon("spread", "Spread Shot", "💥", baseDamage = 6f, fireRate = 4f, projectileSpeed = 800f,
            projectileCount = 3, spreadAngle = 15f, color = 0xFFFF7043, maxLevel = 10,
            description = "3-way spread. Upgrades widen angle + add bullets."),
        TDWeapon("laser", "Beam Laser", "⚡", baseDamage = 15f, fireRate = 2f, projectileSpeed = 1500f,
            piercing = true, color = 0xFFFFEB3B, maxLevel = 10,
            description = "Piercing beam. Upgrades increase width + damage."),
        TDWeapon("missile", "Homing Missiles", "🚀", baseDamage = 20f, fireRate = 1.5f, projectileSpeed = 600f,
            projectileCount = 2, aoeRadius = 50f, color = 0xFFE040FB, maxLevel = 10,
            description = "Homing + AoE. Upgrades add missiles + blast radius."),
        TDWeapon("mega", "Mega Bomb", "💣", baseDamage = 80f, fireRate = 0.5f, projectileSpeed = 400f,
            aoeRadius = 120f, color = 0xFFFF1744, maxLevel = 10,
            description = "Slow but massive AoE. Upgrades increase blast + damage."),
        TDWeapon("shield", "Energy Shield", "🛡️", baseDamage = 5f, fireRate = 10f, projectileSpeed = 0f,
            projectileCount = 0, color = 0xFF80CBC4, maxLevel = 10,
            description = "Shield orbiting ship. Blocks projectiles + damages enemies.")
    )

    var activeWeaponIndex = 0
    val activeWeapon: TDWeapon get() = tdWeapons[activeWeaponIndex]
    val unlockedWeapons = mutableSetOf(0)  // indices of unlocked weapons

    // ── Score System ──────────────────────────────────────────────
    var score = 0L
    var combo = 0
    var comboTimer = 0f
    val comboMultiplier: Float get() = 1f + (combo / 10) * 0.5f
    var maxCombo = 0

    // ── Stage ─────────────────────────────────────────────────────
    data class Stage(
        val number: Int,
        val name: String,
        val waves: Int,
        val enemiesPerWave: Int,
        val spawnInterval: Float,
        val enemyTypes: List<EnemyType>,
        val hpMult: Float,
        val spdMult: Float,
        val bossHpMult: Float,
        val rewardGold: Int,
        val rewardScore: Int,
        val weaponUnlockIndex: Int = -1
    )

    // ── State ─────────────────────────────────────────────────────
    var currentStage = 1
    var currentWave = 0
    var spawnTimer = 0f
    var continuousSpawnTimer = 0f
    var bossActive = false
    var bossDefeated = false
    var levelComplete = false
    var isTransitioning = false
    var transitionTimer = 0f
    var gameOver = false
    var victory = false
    var totalKills = 0
    var stageKills = 0
    var gold = 0
    var enemyFireTimer = 0f  // enemies shoot at player

    // Ship — FIXED at bottom of screen, moves LEFT/RIGHT only
    var shipX = 0f
    var shipY = 0f
    var shipWidth = 40f
    var shipHeight = 40f
    var shipSpeed = 400f
    var shipHp = 100f
    var shipMaxHp = 100f
    var shipInvincible = 0f
    var fireTimer = 0f
    var shieldTimer = 0f
    var moveDir = 0f
    var verticalDir = 0f  // slight up/down in bottom zone
    var lives = 3
    var maxLives = 3
    var screenW = 1080f
    var screenH = 1920f

    companion object {
        const val WALL_THICKNESS = 24f
        const val PLAYER_Y_MIN = 0.65f  // ship can move between 65%-90% of screen height
        const val PLAYER_Y_MAX = 0.90f
        const val PLAYER_Y_DEFAULT = 0.82f
        const val COMBO_TIMEOUT = 2.5f
        const val ENEMY_FIRE_INTERVAL = 2.5f  // seconds between enemy volleys
    }

    // ── Stages ────────────────────────────────────────────────────
    fun getStage(n: Int): Stage {
        return when (n.coerceIn(1, 10)) {
            1 -> Stage(1, "Training Ground", waves = 5, enemiesPerWave = 6, spawnInterval = 1.2f,
                enemyTypes = listOf(EnemyType.BASIC_DRONE), hpMult = 1f, spdMult = 1f,
                bossHpMult = 3f, rewardGold = 60, rewardScore = 1000)
            2 -> Stage(2, "First Assault", waves = 6, enemiesPerWave = 8, spawnInterval = 1.0f,
                enemyTypes = listOf(EnemyType.BASIC_DRONE, EnemyType.SWARM_BAT), hpMult = 1.3f, spdMult = 1.1f,
                bossHpMult = 4f, rewardGold = 100, rewardScore = 2000, weaponUnlockIndex = 1)
            3 -> Stage(3, "Air Superiority", waves = 7, enemiesPerWave = 9, spawnInterval = 0.9f,
                enemyTypes = listOf(EnemyType.FLYING_WISP, EnemyType.SWARM_BAT), hpMult = 1.6f, spdMult = 1.2f,
                bossHpMult = 5f, rewardGold = 140, rewardScore = 3500)
            4 -> Stage(4, "Turret Hell", waves = 8, enemiesPerWave = 10, spawnInterval = 0.8f,
                enemyTypes = listOf(EnemyType.BASIC_DRONE, EnemyType.SHOOTER_TURRET), hpMult = 2f, spdMult = 1.2f,
                bossHpMult = 6f, rewardGold = 180, rewardScore = 5000, weaponUnlockIndex = 2)
            5 -> Stage(5, "Ghost Squadron", waves = 8, enemiesPerWave = 11, spawnInterval = 0.75f,
                enemyTypes = listOf(EnemyType.GHOST, EnemyType.FLYING_WISP, EnemyType.SWARM_BAT), hpMult = 2.5f, spdMult = 1.3f,
                bossHpMult = 8f, rewardGold = 220, rewardScore = 7000)
            6 -> Stage(6, "Iron Armada", waves = 9, enemiesPerWave = 12, spawnInterval = 0.7f,
                enemyTypes = listOf(EnemyType.TANK_GOLEM, EnemyType.ELITE_KNIGHT, EnemyType.BASIC_DRONE), hpMult = 3f, spdMult = 1.3f,
                bossHpMult = 10f, rewardGold = 260, rewardScore = 9000, weaponUnlockIndex = 3)
            7 -> Stage(7, "Mage Strike", waves = 10, enemiesPerWave = 13, spawnInterval = 0.65f,
                enemyTypes = listOf(EnemyType.MAGE, EnemyType.HEALER, EnemyType.SHOOTER_TURRET), hpMult = 3.5f, spdMult = 1.4f,
                bossHpMult = 12f, rewardGold = 300, rewardScore = 12000, weaponUnlockIndex = 4)
            8 -> Stage(8, "Split Decision", waves = 10, enemiesPerWave = 14, spawnInterval = 0.6f,
                enemyTypes = listOf(EnemyType.SPLITTER, EnemyType.GHOST, EnemyType.MAGE, EnemyType.HEALER), hpMult = 4f, spdMult = 1.5f,
                bossHpMult = 15f, rewardGold = 350, rewardScore = 15000)
            9 -> Stage(9, "Apocalypse Now", waves = 12, enemiesPerWave = 15, spawnInterval = 0.5f,
                enemyTypes = listOf(EnemyType.BASIC_DRONE, EnemyType.SWARM_BAT, EnemyType.TANK_GOLEM, EnemyType.ELITE_KNIGHT, EnemyType.MAGE), hpMult = 5f, spdMult = 1.6f,
                bossHpMult = 20f, rewardGold = 400, rewardScore = 20000, weaponUnlockIndex = 5)
            10 -> Stage(10, "The Overlord", waves = 15, enemiesPerWave = 18, spawnInterval = 0.4f,
                enemyTypes = listOf(EnemyType.SWARM_BAT, EnemyType.GHOST, EnemyType.MAGE, EnemyType.SPLITTER, EnemyType.ELITE_KNIGHT, EnemyType.TANK_GOLEM), hpMult = 6f, spdMult = 1.8f,
                bossHpMult = 30f, rewardGold = 500, rewardScore = 30000)
            else -> getStage(10)
        }
    }

    // ── Update ────────────────────────────────────────────────────
    fun update(dt: Float) {
        if (gameOver || victory) return

        // Invincibility
        if (shipInvincible > 0f) shipInvincible -= dt

        // Combo decay
        if (combo > 0) {
            comboTimer -= dt
            if (comboTimer <= 0f) { combo = 0; comboTimer = 0f }
        }

        // Move ship LEFT/RIGHT — bounded by walls
        shipX += moveDir * shipSpeed * dt
        shipX = shipX.coerceIn(shipWidth / 2f + WALL_THICKNESS, screenW - shipWidth / 2f - WALL_THICKNESS)

        // Slight up/down movement in bottom zone only
        shipY += verticalDir * shipSpeed * 0.5f * dt
        val minY = screenH * PLAYER_Y_MIN
        val maxY = screenH * PLAYER_Y_MAX
        shipY = shipY.coerceIn(minY, maxY)

        // Remove drops not needed in TD
        cleanupDrops()

        // Auto-fire upward
        fireTimer -= dt
        if (fireTimer <= 0f && !isTransitioning) {
            fireTimer = 1f / activeWeapon.currentFireRate
            fireWeapon()
        }

        // Shield weapon special
        if (activeWeapon.id == "shield") {
            shieldTimer += dt
            updateShieldOrbit(dt)
        }

        // Transition between stages
        if (isTransitioning) {
            transitionTimer -= dt
            if (transitionTimer <= 0f) {
                isTransitioning = false
                startStage(currentStage)
            }
            return
        }

        // Enemy shooting — enemies fire bullets at player
        enemyFireTimer -= dt
        if (enemyFireTimer <= 0f) {
            enemyFireTimer = ENEMY_FIRE_INTERVAL / (1f + currentStage * 0.1f)
            enemyShootAtPlayer()
        }

        // Check enemies that passed the bottom line
        checkEnemiesPassedBottom()

        // Check if player is dead
        if (shipHp <= 0f || lives <= 0f) {
            gameOver = true
            return
        }

        val stage = getStage(currentStage)

        // Check boss
        if (bossActive) {
            val bossAlive = engine.getActiveEntities().any {
                it.tag == "enemy" && it.active && it.get<EnemyComponent>()?.isBoss == true && !(it.get<HealthComponent>()?.isDead ?: true)
            }
            if (!bossAlive && !bossDefeated) {
                bossDefeated = true
                bossActive = false
                onStageComplete()
            }
            return
        }

        // Count active enemies to cap spawning
        val activeEnemies = engine.getActiveEntities().count {
            it.tag == "enemy" && it.active && !(it.get<HealthComponent>()?.isDead ?: true)
        }
        val maxEnemiesPerStage = (stage.enemiesPerWave * 2 + currentStage * 3).coerceAtMost(40)

        // Continuous enemy spawning (vertical shooter style — always pressure)
        // Only spawn if under enemy cap
        continuousSpawnTimer -= dt
        if (continuousSpawnTimer <= 0f && activeEnemies < maxEnemiesPerStage) {
            continuousSpawnTimer = stage.spawnInterval * 1.2f  // slightly slower continuous spawns
            spawnFormation(stage)
        }

        // Wave-based spawning for variety
        if (currentWave < stage.waves) {
            spawnTimer -= dt
            if (spawnTimer <= 0f && activeEnemies < maxEnemiesPerStage) {
                spawnTimer = stage.spawnInterval * 4f  // bigger gap between waves
                currentWave++
                spawnWaveFormation(stage)
            }
        } else {
            // All waves spawned, check if all enemies dead → boss
            val enemiesAlive = engine.getActiveEntities().count {
                it.tag == "enemy" && it.active && !(it.get<HealthComponent>()?.isDead ?: true)
            }
            if (enemiesAlive == 0 && !levelComplete) {
                spawnBoss(stage)
            }
        }
    }

    // ── Cleanup: remove drops that aren't needed in TD ────────────
    private fun cleanupDrops() {
        for (e in engine.getActiveEntities()) {
            if (!e.active) continue
            if (e.tag == "xp_gem" || e.tag == "health_gem" || e.tag == "loot_box" || e.tag == "relic") {
                e.active = false
            }
        }
    }

    // ── Enemy Passed Bottom ───────────────────────────────────────
    private fun checkEnemiesPassedBottom() {
        for (e in engine.getActiveEntities()) {
            if (e.tag != "enemy" || !e.active) continue
            val t = e.get<TransformComponent>() ?: continue
            val hp = e.get<HealthComponent>() ?: continue
            if (hp.isDead) continue

            if (t.y > screenH + 30f) {
                e.active = false
                lives--
                spawnLifeLostEffect(screenW / 2f, screenH - 50f)
            }
        }
    }

    // ── Stage Management ──────────────────────────────────────────
    fun startStage(n: Int) {
        currentStage = n
        currentWave = 0
        spawnTimer = 0.5f  // fast initial spawn
        continuousSpawnTimer = 0.3f
        bossActive = false
        bossDefeated = false
        levelComplete = false
        stageKills = 0
    }

    private fun onStageComplete() {
        levelComplete = true
        val stage = getStage(currentStage)
        gold += stage.rewardGold
        score += stage.rewardScore

        // Unlock weapon
        if (stage.weaponUnlockIndex in tdWeapons.indices) {
            unlockedWeapons.add(stage.weaponUnlockIndex)
        }

        if (currentStage >= 10) {
            victory = true
        } else {
            isTransitioning = true
            transitionTimer = 3f
            currentStage++
        }
    }

    // ── Enemy Kill Reward (auto-collect) ──────────────────────────
    fun onEnemyKilled(enemyX: Float, enemyY: Float, baseGold: Float, baseXp: Float) {
        totalKills++
        stageKills++
        combo++
        comboTimer = COMBO_TIMEOUT
        if (combo > maxCombo) maxCombo = combo

        // Auto-reward: gold + score (no pickup needed)
        val goldReward = (baseGold * comboMultiplier).toInt().coerceAtLeast(1)
        val scoreReward = (baseXp * 10 * comboMultiplier).toInt()
        gold += goldReward
        score += scoreReward

        // Spawn vertical drop effect (visual feedback — falls in enemy's lane)
        spawnRewardDrop(enemyX, enemyY, goldReward)
    }

    // ── Spawning Formations ───────────────────────────────────────
    private fun spawnFormation(stage: Stage) {
        val type = stage.enemyTypes.random()
        val pattern = (0..4).random()
        when (pattern) {
            0 -> { // Single line from top
                val count = (3 + currentStage).coerceAtMost(8)
                for (i in 0 until count) {
                    val x = screenW * 0.2f + (screenW * 0.6f) * (i.toFloat() / (count - 1).coerceAtLeast(1))
                    spawnEnemy(type, x, -30f - i * 20f, stage.hpMult, stage.spdMult)
                }
            }
            1 -> { // V-formation
                for (i in -2..2) {
                    val x = screenW / 2f + i * 60f
                    val y = -30f - kotlin.math.abs(i) * 40f
                    spawnEnemy(type, x, y, stage.hpMult, stage.spdMult)
                }
            }
            2 -> { // Random scatter
                repeat(4 + currentStage / 2) {
                    val x = GameMath.randomRange(60f, screenW - 60f)
                    val y = GameMath.randomRange(-200f, -30f)
                    spawnEnemy(type, x, y, stage.hpMult, stage.spdMult)
                }
            }
            3 -> { // Line from left
                for (i in 0 until 5) {
                    spawnEnemy(type, -30f, 100f + i * 80f, stage.hpMult, stage.spdMult)
                }
            }
            4 -> { // Line from right
                for (i in 0 until 5) {
                    spawnEnemy(type, screenW + 30f, 100f + i * 80f, stage.hpMult, stage.spdMult)
                }
            }
        }
    }

    private fun spawnWaveFormation(stage: Stage) {
        val count = (stage.enemiesPerWave * (1f + currentWave * 0.15f)).toInt()
        for (i in 0 until count) {
            val type = stage.enemyTypes.random()
            val x = 40f + (screenW - 80f) * (i.toFloat() / count)
            val y = -30f - i * 25f
            spawnEnemy(type, x, y, stage.hpMult, stage.spdMult)
        }
    }

    private fun spawnBoss(stage: Stage) {
        bossActive = true
        val bossType = EnemyType.BOSS
        val e = engine.createEntity("enemy")
        e.add(TransformComponent(screenW / 2f, -100f))
        e.add(VelocityComponent(vx = 0f, vy = 1f, speed = 30f))
        e.add(HealthComponent(
            currentHp = bossType.baseHp * stage.bossHpMult,
            maxHp = bossType.baseHp * stage.bossHpMult,
            armor = 5f * stage.hpMult
        ))
        e.add(EnemyComponent(
            type = bossType,
            damage = bossType.baseDamage * stage.hpMult * 1.5f,
            xpValue = 200f, goldValue = 100f,
            contactCooldown = 2f, isBoss = true
        ))
        e.add(SpriteComponent(width = 80f, height = 80f, color = 0xFFFF1744.toInt(), shape = SpriteShape.STAR))
        e.add(CollisionComponent(radius = 40f))
    }

    private fun spawnEnemy(type: EnemyType, x: Float, y: Float, hpMult: Float, spdMult: Float) {
        val e = engine.createEntity("enemy")
        e.add(TransformComponent(x, y))
        val vxWobble = if (type == EnemyType.SWARM_BAT || type == EnemyType.GHOST)
            GameMath.randomRange(-0.4f, 0.4f) else 0f
        e.add(VelocityComponent(vx = vxWobble, vy = 1f, speed = type.baseSpeed * spdMult * 1.2f))
        e.add(HealthComponent(currentHp = type.baseHp * hpMult, maxHp = type.baseHp * hpMult))
        e.add(EnemyComponent(
            type = type,
            damage = type.baseDamage * hpMult * 0.3f,
            xpValue = type.xpValue * 0.3f,
            goldValue = type.goldValue * 0.2f,
            contactCooldown = type.contactCooldown
        ))
        e.add(SpriteComponent(
            width = type.size, height = type.size,
            color = type.colorHex.toInt(),
            shape = when (type.shape) {
                "circle" -> SpriteShape.CIRCLE; "rect" -> SpriteShape.RECT
                "triangle" -> SpriteShape.TRIANGLE; "diamond" -> SpriteShape.DIAMOND
                "star" -> SpriteShape.STAR; else -> SpriteShape.CIRCLE
            }
        ))
        e.add(CollisionComponent(radius = type.size / 2f))
    }

    // ── Weapon Firing ─────────────────────────────────────────────
    private fun fireWeapon() {
        val w = activeWeapon
        if (w.id == "shield") return  // shield handled separately

        val count = w.currentProjectileCount
        for (i in 0 until count) {
            val angle = if (count == 1) -90f
            else -90f + (i - (count - 1) / 2f) * w.spreadAngle
            val rad = Math.toRadians(angle.toDouble()).toFloat()

            val proj = engine.createEntity("projectile")
            proj.add(TransformComponent(shipX, shipY - shipHeight / 2f))
            proj.add(VelocityComponent(vx = cos(rad), vy = sin(rad), speed = w.projectileSpeed))
            proj.add(ProjectileComponent(
                damage = w.damage,
                speed = w.projectileSpeed,
                lifetime = 2.5f,
                pierceCount = if (w.piercing) 999 else 0,
                pierceRemaining = if (w.piercing) 999 else 0,
                aoeRadius = w.aoeRadius,
                weaponType = WeaponType.DIVINE_SPEAR
            ))
            val projSize = if (w.id == "mega") 12f else if (w.id == "laser") 3f else 6f
            proj.add(SpriteComponent(
                width = projSize, height = projSize * 2f,
                color = w.color.toInt(),
                shape = if (w.id == "missile") SpriteShape.TRIANGLE else SpriteShape.CIRCLE
            ))
            proj.add(CollisionComponent(radius = projSize * 0.8f, isTrigger = true))
        }
    }

    // ── Shield Orbit ──────────────────────────────────────────────
    private fun updateShieldOrbit(dt: Float) {
        val shieldCount = activeWeapon.currentProjectileCount.coerceAtMost(6)
        val orbitRadius = 60f + activeWeapon.level * 5f
        val existing = engine.getActiveEntities().filter { it.tag == "orbit_shield" && it.active }

        // Create/update shields
        for (i in 0 until shieldCount) {
            val angle = (i.toFloat() / shieldCount) * Math.PI.toFloat() * 2f + shieldTimer * 2f
            val sx = shipX + cos(angle) * orbitRadius
            val sy = shipY + sin(angle) * orbitRadius

            if (i < existing.size) {
                val t = existing[i].get<TransformComponent>()
                if (t != null) { t.x = sx; t.y = sy }
            } else {
                val shield = engine.createEntity("orbit_shield")
                shield.add(TransformComponent(sx, sy))
                shield.add(HealthComponent(currentHp = 30f + activeWeapon.level * 10f, maxHp = 30f + activeWeapon.level * 10f))
                shield.add(SpriteComponent(width = 14f, height = 14f, color = activeWeapon.color.toInt(), shape = SpriteShape.CIRCLE))
                shield.add(CollisionComponent(radius = 10f))
            }
        }
    }

    // ── Ship Damage ───────────────────────────────────────────────
    fun takeDamage(amount: Float) {
        if (shipInvincible > 0f) return
        shipHp = (shipHp - amount).coerceAtLeast(0f)
        shipInvincible = 1.5f
    }

    // ── Weapon Upgrade (between stages) ───────────────────────────
    fun upgradeWeapon(index: Int): Boolean {
        val cost = getUpgradeCost(index)
        if (gold < cost) return false
        val w = tdWeapons.getOrNull(index) ?: return false
        if (w.level >= w.maxLevel) return false
        gold -= cost
        w.level++
        return true
    }

    fun switchWeapon(index: Int) {
        if (index in unlockedWeapons) {
            activeWeaponIndex = index
        }
    }

    fun getUpgradeCost(weaponIndex: Int): Int {
        val w = tdWeapons.getOrNull(weaponIndex) ?: return 9999
        return 25 + w.level * 18
    }

    fun getWeaponDps(weaponIndex: Int): Float {
        val w = tdWeapons.getOrNull(weaponIndex) ?: return 0f
        return w.damage * w.currentFireRate * w.currentProjectileCount
    }

    // ── Enemy Shooting — enemies fire bullets at player ───────────
    private fun enemyShootAtPlayer() {
        val enemies = engine.getActiveEntities().filter {
            it.tag == "enemy" && it.active && !(it.get<HealthComponent>()?.isDead ?: true)
        }
        // Only some enemies shoot (turrets, mages, bosses)
        for (enemy in enemies) {
            val type = enemy.get<EnemyComponent>()?.type ?: continue
            if (type != EnemyType.SHOOTER_TURRET && type != EnemyType.MAGE && type != EnemyType.BOSS) continue
            val t = enemy.get<TransformComponent>() ?: continue
            // Aim at player
            val dx = shipX - t.x
            val dy = shipY - t.y
            val dist = kotlin.math.sqrt(dx * dx + dy * dy)
            if (dist < 10f) continue
            val nx = dx / dist
            val ny = dy / dist
            val bulletSpeed = 300f + currentStage * 20f
            val bullet = engine.createEntity("enemy_projectile")
            bullet.add(TransformComponent(t.x, t.y))
            bullet.add(VelocityComponent(vx = nx, vy = ny, speed = bulletSpeed))
            val damage = type.baseDamage * getStage(currentStage).hpMult * 0.4f
            bullet.add(TurretProjectileComponent(damage = damage, lifetime = 4f))
            bullet.add(SpriteComponent(
                width = 8f, height = 8f,
                color = 0xFFFF1744.toInt(),
                shape = SpriteShape.CIRCLE
            ))
            bullet.add(CollisionComponent(radius = 5f, isTrigger = true))
        }
    }

    // ── Visual Effects ────────────────────────────────────────────
    private fun spawnRewardDrop(x: Float, y: Float, amount: Int) {
        // Vertical drop effect — falls in the enemy's lane
        val drop = engine.createEntity("particle")
        drop.add(TransformComponent(x, y))
        drop.add(VelocityComponent(vx = 0f, vy = 1f, speed = 200f))
        drop.add(SpriteComponent(width = 8f, height = 8f, color = 0xFFFFD700.toInt(), alpha = 0.9f))
        drop.add(ParticleComponent(lifetime = 1.5f, fadeOut = true, shrink = false))
    }

    private fun spawnLifeLostEffect(x: Float, y: Float) {
        repeat(12) {
            val angle = it * Math.PI.toFloat() * 2f / 12f
            val p = engine.createEntity("particle")
            p.add(TransformComponent(x, y))
            p.add(VelocityComponent(vx = cos(angle) * 150f, vy = sin(angle) * 150f, speed = 1f))
            p.add(SpriteComponent(width = 10f, height = 10f, color = 0xFFFF1744.toInt(), alpha = 0.9f))
            p.add(ParticleComponent(lifetime = 0.6f, fadeOut = true, shrink = true))
        }
    }

    // ── Info ──────────────────────────────────────────────────────
    fun getStageName(): String = getStage(currentStage).name
    fun getStageProgress(): Float {
        val stage = getStage(currentStage)
        return if (bossActive) 1f
        else (currentWave.toFloat() / stage.waves).coerceIn(0f, 0.95f)
    }

    // ── Reset ─────────────────────────────────────────────────────
    fun reset() {
        currentStage = 1; currentWave = 0; spawnTimer = 0f; continuousSpawnTimer = 0f
        bossActive = false; bossDefeated = false; levelComplete = false
        isTransitioning = false; transitionTimer = 0f
        gameOver = false; victory = false
        totalKills = 0; stageKills = 0; gold = 0
        score = 0; combo = 0; comboTimer = 0f; maxCombo = 0
        shipX = screenW / 2f; shipY = screenH * PLAYER_Y_DEFAULT
        shipHp = 100f; shipMaxHp = 100f; shipInvincible = 0f; fireTimer = 0f; shieldTimer = 0f
        moveDir = 0f; verticalDir = 0f; lives = 3; enemyFireTimer = 0f
        activeWeaponIndex = 0
        unlockedWeapons.clear(); unlockedWeapons.add(0)
        tdWeapons.forEach { it.level = 1 }
    }
}

// ── Boss Rush (keep) ──────────────────────────────────────────────
data class BossRushState(
    var bossNumber: Int = 0,
    var bossTimer: Float = 0f,
    val bossInterval: Float = 15f,
    var difficultyScale: Float = 1f
) {
    fun update(dt: Float): Boolean {
        bossTimer += dt
        if (bossTimer >= bossInterval) {
            bossTimer = 0f
            bossNumber++
            difficultyScale = 1f + bossNumber * 0.2f
            return true
        }
        return false
    }
}
