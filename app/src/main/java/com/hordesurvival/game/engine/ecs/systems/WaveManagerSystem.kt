package com.hordesurvival.game.engine.ecs.systems

import com.hordesurvival.game.audio.SoundManager
import com.hordesurvival.game.component.*
import com.hordesurvival.game.engine.GameEngine
import com.hordesurvival.game.engine.ecs.Entity
import com.hordesurvival.game.engine.ecs.System
import com.hordesurvival.game.enemy.EnemyType
import com.hordesurvival.game.mode.GameModeType
import com.hordesurvival.game.mode.TowerDefenseMode
import com.hordesurvival.utils.GameMath
import kotlin.math.sqrt

/**
 * Wave spawning with level-based elite enemies and bosses.
 * Elite every 10 levels, Boss every 50 levels.
 */
class WaveManagerSystem(private val engine: GameEngine) : System() {

    private var spawnTimer = 0f
    private var waveNumber = 0
    private var difficultyMultiplier = 1f
    private var lastEliteLevel = 0
    private var lastBossLevel = 0
    private var lastMiniBossTime = 0f

    var onBossSpawned: (() -> Unit)? = null
    var onEliteSpawned: (() -> Unit)? = null
    var gameMode: GameModeType = GameModeType.SURVIVAL
    var towerDefense: TowerDefenseMode? = null
    // Daily Challenge modifiers
    var enemyHpMult: Float = 1f
    var enemySpdMult: Float = 1f
    var enemyDmgMult: Float = 1f
    var spawnRateMult: Float = 1f

    /** Reference to player level — set from GameViewModel */
    var playerLevel = 1

    override fun update(dt: Float, entities: List<Entity>) {
        val player = engine.playerEntity
        val playerPos = player?.get<TransformComponent>() ?: return

        // Tower Defense mode uses its own wave system
        if (gameMode == GameModeType.TOWER_DEFENSE) {
            towerDefense?.update(dt)
            // In TD mode, enemies move downward — no need for standard wave spawning
            return
        }

        val timeMinutes = engine.gameTime / 60f
        difficultyMultiplier = 1f + 0.15f * sqrt(playerLevel.toFloat()) + timeMinutes * 0.05f

        val spawnInterval = (2.0f / (1f + timeMinutes * 0.06f) / spawnRateMult).coerceAtLeast(0.3f)

        spawnTimer += dt
        if (spawnTimer >= spawnInterval) {
            spawnTimer = 0f
            waveNumber++
            spawnWave(playerPos)
        }

        // Elite enemy every 10 player levels
        if (playerLevel >= lastEliteLevel + 10 && playerLevel > 0) {
            lastEliteLevel = (playerLevel / 10) * 10
            spawnElite(playerPos)
            onEliteSpawned?.invoke()
        }

        // Boss every 50 player levels
        if (playerLevel >= lastBossLevel + 50 && playerLevel > 0) {
            lastBossLevel = (playerLevel / 50) * 50
            spawnBoss(playerPos)
            onBossSpawned?.invoke()
            SoundManager.playBossWarning()
        }

        // Mini-boss every 5 minutes (not a true boss, just a strong enemy)
        val timeMinutesForMini = engine.gameTime / 60f
        if (timeMinutesForMini >= lastMiniBossTime + 5f && timeMinutesForMini > 1f) {
            lastMiniBossTime = (timeMinutes / 5f).toInt() * 5f
            spawnMiniBoss(playerPos)
        }

        // Cap enemy count with fast distance-based culling (no sort)
        val maxEnemies = com.hordesurvival.utils.Constants.MAX_ENEMIES_ON_SCREEN
        val cullDistanceSq = 900f * 900f  // despawn enemies beyond 900 units
        var enemyCount = 0
        var farthestEnemy: Entity? = null
        var farthestDistSq = 0f
        for (e in entities) {
            if (e.tag != "enemy" || !e.active) continue
            enemyCount++
            val t = e.get<TransformComponent>() ?: continue
            val dx = t.x - playerPos.x; val dy = t.y - playerPos.y
            val distSq = dx * dx + dy * dy
            // Immediately cull enemies way too far
            if (distSq > cullDistanceSq) { e.active = false; enemyCount--; continue }
            if (distSq > farthestDistSq) { farthestDistSq = distSq; farthestEnemy = e }
        }
        // If still over cap, despawn the farthest
        if (enemyCount > maxEnemies && farthestEnemy != null) {
            farthestEnemy.active = false
        }
    }

    private fun spawnWave(playerPos: TransformComponent) {
        when (gameMode) {
            GameModeType.DAILY_CHALLENGE -> {
                // Boss Rush: spawn a boss every 30 seconds
                if (waveNumber % 15 == 0 && waveNumber > 0) {
                    spawnBoss(playerPos)
                    onBossSpawned?.invoke()
                    SoundManager.playBossWarning()
                } else {
                    // Small minion waves between bosses
                    val count = (3 + playerLevel / 10).coerceAtMost(8)
                    repeat(count) { spawnEnemy(EnemyType.SWARM_BAT, playerPos) }
                }
            }
            GameModeType.QUEST -> {
                // Wave Mode: fixed waves, increasing difficulty
                val waveSize = (5 + waveNumber * 2).coerceAtMost(30)
                repeat(waveSize) { spawnEnemy(EnemyType.random(), playerPos) }
                // Elite every 5 waves
                if (waveNumber % 5 == 0 && waveNumber > 0) {
                    spawnElite(playerPos)
                    onEliteSpawned?.invoke()
                }
            }
            else -> {
                // Standard Survival
                val baseCount = 2 + (playerLevel / 5)
                val scaledCount = (baseCount * difficultyMultiplier).toInt().coerceIn(1, 25)
                repeat(scaledCount) { spawnEnemy(EnemyType.random(), playerPos) }
                if (waveNumber % 5 == 0 && waveNumber > 0) {
                    repeat((3 + playerLevel / 8).coerceAtMost(12)) { spawnEnemy(EnemyType.SWARM_BAT, playerPos) }
                }
            }
        }
    }

    private fun spawnElite(playerPos: TransformComponent) {
        // Elite is a stronger version of a random enemy
        val baseType = EnemyType.random()
        val angle = GameMath.randomRange(0f, Math.PI.toFloat() * 2f)
        val x = playerPos.x + kotlin.math.cos(angle) * 500f
        val y = playerPos.y + kotlin.math.sin(angle) * 500f

        val hpScale = difficultyMultiplier * 3f
        val dmgScale = difficultyMultiplier * 2f

        val entity = engine.createEntity("enemy")
        entity.add(TransformComponent(x, y))
        entity.add(VelocityComponent(speed = baseType.baseSpeed * 0.8f))
        entity.add(HealthComponent(currentHp = baseType.baseHp * hpScale, maxHp = baseType.baseHp * hpScale, armor = 5f * difficultyMultiplier))
        entity.add(EnemyComponent(
            type = baseType, damage = baseType.baseDamage * dmgScale,
            xpValue = baseType.xpValue * 10f, goldValue = baseType.goldValue * 5f,
            contactCooldown = baseType.contactCooldown, isBoss = false
        ))
        entity.add(SpriteComponent(
            width = baseType.size * 1.5f, height = baseType.size * 1.5f,
            color = 0xFFFFD700.toInt(), shape = SpriteShape.STAR  // gold = elite
        ))
        entity.add(CollisionComponent(radius = baseType.size * 0.75f))
    }

    private fun spawnBoss(playerPos: TransformComponent) {
        spawnEnemy(EnemyType.BOSS, playerPos)
        // Screen shake on boss spawn
        engine.shake(intensity = 12f, duration = 0.4f)
        // Note: onBossSpawned callback is called from update(), not here
    }

    private fun spawnMiniBoss(playerPos: TransformComponent) {
        val types = listOf(EnemyType.ELITE_KNIGHT, EnemyType.TANK_GOLEM, EnemyType.GHOST)
        val baseType = types.random()
        val angle = GameMath.randomRange(0f, Math.PI.toFloat() * 2f)
        val x = playerPos.x + kotlin.math.cos(angle) * 450f
        val y = playerPos.y + kotlin.math.sin(angle) * 450f

        val hpScale = difficultyMultiplier * 5f
        val dmgScale = difficultyMultiplier * 2.5f

        val entity = engine.createEntity("enemy")
        entity.add(TransformComponent(x, y))
        entity.add(VelocityComponent(speed = baseType.baseSpeed * 1.2f))
        entity.add(HealthComponent(
            currentHp = baseType.baseHp * hpScale,
            maxHp = baseType.baseHp * hpScale,
            armor = 3f * difficultyMultiplier
        ))
        entity.add(EnemyComponent(
            type = baseType, damage = baseType.baseDamage * dmgScale,
            xpValue = baseType.xpValue * 8f, goldValue = baseType.goldValue * 4f,
            contactCooldown = baseType.contactCooldown
        ))
        entity.add(SpriteComponent(
            width = baseType.size * 1.8f, height = baseType.size * 1.8f,
            color = 0xFFFF6E40.toInt(),  // orange-red = mini-boss
            shape = SpriteShape.STAR
        ))
        entity.add(CollisionComponent(radius = baseType.size * 0.9f))
    }

    private fun spawnEnemy(type: EnemyType, playerPos: TransformComponent) {
        val angle = GameMath.randomRange(0f, Math.PI.toFloat() * 2f)
        val dist = 500f + GameMath.randomRange(0f, 150f)
        val x = playerPos.x + kotlin.math.cos(angle) * dist
        val y = playerPos.y + kotlin.math.sin(angle) * dist

        val hpScale = (difficultyMultiplier * enemyHpMult).coerceAtMost(10f)
        val dmgScale = (difficultyMultiplier * enemyDmgMult).coerceAtMost(5f)
        val spdScale = ((1f + (difficultyMultiplier - 1f) * 0.25f) * enemySpdMult).coerceAtMost(2.5f)

        val entity = engine.createEntity("enemy")
        entity.add(TransformComponent(x, y))
        entity.add(VelocityComponent(speed = type.baseSpeed * spdScale))
        entity.add(HealthComponent(
            currentHp = type.baseHp * hpScale, maxHp = type.baseHp * hpScale,
            armor = if (type == EnemyType.ELITE_KNIGHT) 5f * hpScale else 0f
        ))
        entity.add(EnemyComponent(
            type = type, damage = type.baseDamage * dmgScale,
            xpValue = type.xpValue, goldValue = type.goldValue,
            contactCooldown = type.contactCooldown,
            contactTimer = if (type.isRanged && type != EnemyType.MAGE) 1.5f else 0f,
            splitOnDeath = type.canSplit, splitCount = if (type.canSplit) 2 else 0,
            isBoss = type.isBoss, healCooldown = if (type.canHeal) 3f else 0f
        ))
        entity.add(SpriteComponent(
            width = type.size, height = type.size,
            color = type.colorHex.toInt(),
            shape = when (type.shape) {
                "circle" -> SpriteShape.CIRCLE; "rect" -> SpriteShape.RECT
                "triangle" -> SpriteShape.TRIANGLE; "diamond" -> SpriteShape.DIAMOND
                "star" -> SpriteShape.STAR; else -> SpriteShape.CIRCLE
            }
        ))
        entity.add(CollisionComponent(radius = type.size / 2f))
    }

    override fun dispose() {
        spawnTimer = 0f; waveNumber = 0; difficultyMultiplier = 1f
        lastEliteLevel = 0; lastBossLevel = 0
    }
}
