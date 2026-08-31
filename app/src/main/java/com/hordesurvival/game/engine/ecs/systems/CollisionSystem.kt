package com.hordesurvival.game.engine.ecs.systems

import com.hordesurvival.game.component.*
import com.hordesurvival.game.engine.GameEngine
import com.hordesurvival.game.engine.ecs.Entity
import com.hordesurvival.game.engine.ecs.System
import com.hordesurvival.game.audio.SoundManager
import com.badlogic.gdx.math.Vector2
import com.hordesurvival.game.weapon.WeaponType
import com.hordesurvival.utils.Constants
import com.hordesurvival.utils.GameMath
import com.badlogic.gdx.utils.IntFloatMap

/**
 * Handles all collision detection with proper cooldowns and damage batching.
 * Overhauled: rebuilds engine.spatialGrid after movements to ensure fresh positions,
 * and accounts for target entity radius when querying SpatialGrid so large entities/bosses are never missed.
 */
class CollisionSystem(private val engine: GameEngine) : System() {

    // Shield contact cooldown per enemy (prevents per-frame damage)
    // Bolt: Swapped boxed standard Map for libGDX IntFloatMap to eliminate autoboxing of IDs and floats
    private val shieldHitCooldowns = IntFloatMap()

    // Reusable Vector2 for offset calculation — prevents GC allocations in particle loops
    private val tempVec2 = Vector2()

    // Reusable collections — no allocation per frame
    private val _enemies = mutableListOf<Entity>()
    private val _projectiles = mutableListOf<Entity>()
    private val _enemyProjectiles = mutableListOf<Entity>()
    private val _xpGems = mutableListOf<Entity>()
    private val _healthGems = mutableListOf<Entity>()
    private val _poisonClouds = mutableListOf<Entity>()
    private val _orbitShields = mutableListOf<Entity>()

    // Scratch buffer for spatial queries
    private val _nearbyEnemiesBuffer = mutableListOf<Entity>()

    // Maximum possible enemy collision radius (bosses can be up to 40f)
    private val MAX_ENEMY_RADIUS = 40f

    override fun update(dt: Float, entities: List<Entity>) {
        // Rebuild SpatialGrid with updated entity positions after MovementSystem/EnemyAISystem ran
        engine.spatialGrid.clear()
        for (i in 0 until entities.size) {
            val e = entities[i]
            if (e.active && e.has<TransformComponent>()) {
                engine.spatialGrid.insert(e)
            }
        }

        val player = engine.playerEntity ?: return
        val playerTransform = player.get<TransformComponent>() ?: return
        val playerHealth = player.get<HealthComponent>() ?: return
        val playerCollision = player.get<CollisionComponent>() ?: return
        val playerComp = player.get<PlayerComponent>() ?: return

        // Update invincibility timer
        if (playerHealth.invincibleTimer > 0f) {
            playerHealth.invincibleTimer -= dt
        }

        // Categorize non-enemy entities in a single pass — enemies are queried via SpatialGrid
        _enemies.clear(); _projectiles.clear(); _xpGems.clear(); _healthGems.clear()
        _poisonClouds.clear(); _orbitShields.clear(); _enemyProjectiles.clear()
        for (i in 0 until entities.size) {
            val e = entities[i]
            if (!e.active) continue
            when (e.tag) {
                "enemy" -> _enemies.add(e)
                "projectile" -> _projectiles.add(e)
                "enemy_projectile" -> _enemyProjectiles.add(e)
                "xp_gem" -> _xpGems.add(e)
                "health_gem" -> _healthGems.add(e)
                "poison_cloud" -> _poisonClouds.add(e)
                "orbit_shield" -> _orbitShields.add(e)
            }
        }

        // Decay shield cooldowns — iterate map directly
        val cooldownIter = shieldHitCooldowns.iterator()
        while (cooldownIter.hasNext()) {
            val entry = cooldownIter.next()
            val newTime = entry.value - dt
            if (newTime <= 0f) cooldownIter.remove() else shieldHitCooldowns.put(entry.key, newTime)
        }

        // ── Player vs Enemy contact damage ──────────────────────────────
        _nearbyEnemiesBuffer.clear()
        engine.spatialGrid.queryRange(playerTransform.x, playerTransform.y, playerCollision.radius + MAX_ENEMY_RADIUS, "enemy", _nearbyEnemiesBuffer)
        for (i in 0 until _nearbyEnemiesBuffer.size) {
            val enemy = _nearbyEnemiesBuffer[i]
            if (!enemy.active) continue
            val eTransform = enemy.get<TransformComponent>() ?: continue
            val eEnemy = enemy.get<EnemyComponent>() ?: continue
            val eCollision = enemy.get<CollisionComponent>() ?: continue

            val dx = playerTransform.x - eTransform.x
            val dy = playerTransform.y - eTransform.y
            val distSq = dx * dx + dy * dy
            val minDist = playerCollision.radius + eCollision.radius

            if (distSq < minDist * minDist && eEnemy.contactTimer <= 0f && playerHealth.invincibleTimer <= 0f) {
                val dmg = eEnemy.damage
                playerHealth.takeDamage(dmg)
                eEnemy.contactTimer = eEnemy.contactCooldown
                playerHealth.invincibleTimer = Constants.PLAYER_INVINCIBILITY_TIME
                spawnHitParticles(eTransform.x, eTransform.y, 0xFFFFB7B2.toInt())
                SoundManager.playDamage()
                spawnDamageNumber(playerTransform.x, playerTransform.y - 20f, dmg, false)
                engine.shake(intensity = 6f, duration = 0.12f)
            }
        }

        // ── Enemy Projectile vs Player ────────────────────────────
        for (i in 0 until _enemyProjectiles.size) {
            val eproj = _enemyProjectiles[i]
            if (!eproj.active) continue
            val epTransform = eproj.get<TransformComponent>() ?: continue
            val epComp = eproj.get<TurretProjectileComponent>() ?: continue
            val epCollision = eproj.get<CollisionComponent>()?.radius ?: 6f

            epComp.timer += dt
            if (epComp.timer >= epComp.lifetime) {
                eproj.active = false
                continue
            }

            val dx = playerTransform.x - epTransform.x
            val dy = playerTransform.y - epTransform.y
            val distSq = dx * dx + dy * dy
            val minDist = playerCollision.radius + epCollision

            if (distSq < minDist * minDist && playerHealth.invincibleTimer <= 0f) {
                playerHealth.takeDamage(epComp.damage)
                playerHealth.invincibleTimer = Constants.PLAYER_INVINCIBILITY_TIME
                SoundManager.playDamage()
                engine.shake(intensity = 5f, duration = 0.1f)
                eproj.active = false
            }
        }

        // ── Projectile vs Enemy (Spatial Grid query including MAX_ENEMY_RADIUS) ──
        for (i in 0 until _projectiles.size) {
            val proj = _projectiles[i]
            if (!proj.active) continue
            val pTransform = proj.get<TransformComponent>() ?: continue
            val pComp = proj.get<ProjectileComponent>() ?: continue
            val projCollision = proj.get<CollisionComponent>()?.radius ?: 8f

            val queryRadius = projCollision + MAX_ENEMY_RADIUS
            _nearbyEnemiesBuffer.clear()
            engine.spatialGrid.queryRange(pTransform.x, pTransform.y, queryRadius, "enemy", _nearbyEnemiesBuffer)

            for (j in 0 until _nearbyEnemiesBuffer.size) {
                val enemy = _nearbyEnemiesBuffer[j]
                if (!enemy.active) continue
                val eTransform = enemy.get<TransformComponent>() ?: continue
                val eHealth = enemy.get<HealthComponent>() ?: continue
                val eEnemy = enemy.get<EnemyComponent>() ?: continue
                val eCollision = enemy.get<CollisionComponent>() ?: continue

                val dx = pTransform.x - eTransform.x
                val dy = pTransform.y - eTransform.y
                val distSq = dx * dx + dy * dy
                val minDist = projCollision + eCollision.radius

                if (distSq < minDist * minDist) {
                    var actualDamage = pComp.damage
                    val elite = enemy.get<EliteComponent>()
                    if (elite != null && elite.shieldActive && elite.shieldHp > 0f) {
                        val absorbed = minOf(actualDamage, elite.shieldHp)
                        elite.shieldHp -= absorbed
                        actualDamage -= absorbed
                        if (elite.shieldHp <= 0f) {
                            elite.shieldActive = false
                            enemy.get<SpriteComponent>()?.alpha = 1f
                        }
                    }
                    val hpBefore = eHealth.currentHp
                    eHealth.takeDamage(actualDamage)
                    val actualDmg = hpBefore - eHealth.currentHp

                    if (pComp.burnDamage > 0f) {
                        eEnemy.burnTimer = pComp.burnDuration
                        eEnemy.burnDamage = pComp.burnDamage
                    }
                    if (pComp.slowFactor < 1f) {
                        if (pComp.slowFactor < eEnemy.slowFactor || pComp.slowDuration > eEnemy.slowTimer) {
                            eEnemy.slowTimer = pComp.slowDuration
                            eEnemy.slowFactor = pComp.slowFactor
                        }
                    }

                    if (pComp.aoeRadius > 0f) {
                        applyAoeDamage(pTransform.x, pTransform.y, pComp.aoeRadius, pComp.damage * 0.5f)
                        spawnExplosion(pTransform.x, pTransform.y, pComp.aoeRadius)
                    }

                    if (pComp.pierceRemaining > 0) {
                        pComp.pierceRemaining--
                    } else {
                        proj.active = false
                    }

                    spawnHitParticles(eTransform.x, eTransform.y, getWeaponHitColor(pComp.weaponType))
                    SoundManager.playHit()
                    spawnDamageNumber(eTransform.x, eTransform.y - 15f, actualDmg, actualDmg > pComp.damage * 1.5f)
                    if (actualDmg > pComp.damage * 1.5f) SoundManager.playHitCrit()
                    break
                }
            }
        }

        // ── Poison Cloud vs Enemy ───────────────────────────────
        for (i in 0 until _poisonClouds.size) {
            val cloud = _poisonClouds[i]
            if (!cloud.active) continue
            val cTransform = cloud.get<TransformComponent>() ?: continue
            val cComp = cloud.get<PoisonCloudComponent>() ?: continue

            cComp.timer += dt
            if (cComp.timer >= cComp.lifetime) {
                cloud.active = false
                continue
            }

            val remainingRatio = 1f - (cComp.timer / cComp.lifetime)
            cloud.get<SpriteComponent>()?.let { sprite ->
                sprite.alpha = 0.3f * remainingRatio
            }

            cComp.tickTimer += dt
            if (cComp.tickTimer >= cComp.tickInterval) {
                cComp.tickTimer = 0f
                _nearbyEnemiesBuffer.clear()
                engine.spatialGrid.queryRange(cTransform.x, cTransform.y, cComp.radius + MAX_ENEMY_RADIUS, "enemy", _nearbyEnemiesBuffer)
                for (j in 0 until _nearbyEnemiesBuffer.size) {
                    val enemy = _nearbyEnemiesBuffer[j]
                    if (!enemy.active) continue
                    val eHealth = enemy.get<HealthComponent>() ?: continue
                    val hpBefore = eHealth.currentHp
                    eHealth.takeDamage(cComp.damagePerTick)
                    val actualDmg = hpBefore - eHealth.currentHp
                    if (actualDmg > 0f) {
                        val ePos = enemy.get<TransformComponent>() ?: continue
                        spawnDamageNumber(ePos.x, ePos.y - 10f, actualDmg, false)
                    }
                }
            }
        }

        // ── Orbit Shield vs Enemy ───────────────────────────────
        val shieldDamage = 10f * playerComp.might
        for (i in 0 until _orbitShields.size) {
            val shield = _orbitShields[i]
            if (!shield.active) continue
            val sTransform = shield.get<TransformComponent>() ?: continue
            val sHealth = shield.get<HealthComponent>() ?: continue

            _nearbyEnemiesBuffer.clear()
            engine.spatialGrid.queryRange(sTransform.x, sTransform.y, 12f + MAX_ENEMY_RADIUS, "enemy", _nearbyEnemiesBuffer)

            for (j in 0 until _nearbyEnemiesBuffer.size) {
                val enemy = _nearbyEnemiesBuffer[j]
                if (!enemy.active) continue
                val eTransform = enemy.get<TransformComponent>() ?: continue
                val eHealth = enemy.get<HealthComponent>() ?: continue
                val eCollision = enemy.get<CollisionComponent>() ?: continue

                val dx = sTransform.x - eTransform.x
                val dy = sTransform.y - eTransform.y
                val distSq = dx * dx + dy * dy
                val minDist = 12f + eCollision.radius

                if (distSq < minDist * minDist) {
                    val cooldownKey = enemy.id
                    if (shieldHitCooldowns.get(cooldownKey, 0f) <= 0f) {
                        eHealth.takeDamage(shieldDamage)
                        sHealth.takeDamage(3f)
                        shieldHitCooldowns.put(cooldownKey, 0.3f)
                        spawnHitParticles(eTransform.x, eTransform.y, 0xFFB19CD9.toInt())
                    }
                }
            }

            if (sHealth.isDead) {
                shield.active = false
                spawnExplosion(sTransform.x, sTransform.y, 40f)
            }
        }

        // ── Player vs XP Gem ────────────────────────────────────
        if (engine.isPaused) return
        for (i in 0 until _xpGems.size) {
            val gem = _xpGems[i]
            if (!gem.active) continue
            val gTransform = gem.get<TransformComponent>() ?: continue
            val gComp = gem.get<XpGemComponent>() ?: continue
            val gSprite = gem.get<SpriteComponent>()

            val dx = playerTransform.x - gTransform.x
            val dy = playerTransform.y - gTransform.y
            val distSq = dx * dx + dy * dy

            if (distSq < playerComp.pickupRange * playerComp.pickupRange) {
                gComp.magnetized = true
            }

            if (gComp.magnetized) {
                val dist = kotlin.math.sqrt(distSq)
                val speed = com.hordesurvival.utils.Constants.GXP_MAGNET_SPEED + (1f - dist / playerComp.pickupRange).coerceIn(0f, 1f) * 300f
                val invLen = if (dist > 0f) 1f / dist else 1f
                gTransform.x += (dx * invLen) * speed * dt
                gTransform.y += (dy * invLen) * speed * dt

                gSprite?.let {
                    val pulse = 1f + 0.15f * kotlin.math.sin(engine.gameTime * 10f)
                    it.scaleX = pulse
                    it.scaleY = pulse
                }
            }

            if (distSq < 24f * 24f) {
                val comboMult = player.get<ComboComponent>()?.comboMultiplier ?: 1f
                playerComp.addXp(gComp.value * comboMult)
                gem.active = false
                spawnPickupEffect(gTransform.x, gTransform.y)
                SoundManager.playPickup()
            }
        }

        // ── Player vs Health Gem ────────────────────────────────
        for (i in 0 until _healthGems.size) {
            val gem = _healthGems[i]
            if (!gem.active) continue
            val gTransform = gem.get<TransformComponent>() ?: continue
            val gComp = gem.get<XpGemComponent>() ?: continue

            val dx = playerTransform.x - gTransform.x
            val dy = playerTransform.y - gTransform.y
            val distSq = dx * dx + dy * dy

            if (distSq < playerComp.pickupRange * playerComp.pickupRange) {
                gComp.magnetized = true
            }

            if (gComp.magnetized) {
                val dist = kotlin.math.sqrt(distSq)
                val speed = com.hordesurvival.utils.Constants.GXP_MAGNET_SPEED
                val invLen = if (dist > 0f) 1f / dist else 1f
                gTransform.x += (dx * invLen) * speed * dt
                gTransform.y += (dy * invLen) * speed * dt
            }

            if (distSq < 24f * 24f) {
                playerHealth.heal(gComp.value)
                gem.active = false
                spawnPickupEffect(gTransform.x, gTransform.y)
                SoundManager.playPickupBig()
            }
        }

        // ── Player regen ──────────────────────────────────────────
        if (playerComp.regenRate > 0f) {
            playerHealth.heal(playerComp.regenRate * dt)
        }
    }

    private fun applyAoeDamage(x: Float, y: Float, radius: Float, damage: Float) {
        val nearby = engine.findInRange(x, y, radius, "enemy")
        for (i in 0 until nearby.size) {
            nearby[i].get<HealthComponent>()?.takeDamage(damage)
        }
    }

    private fun spawnHitParticles(x: Float, y: Float, color: Int) {
        repeat(4) {
            val offset = GameMath.randomPointInCircle(8f, tempVec2)
            val p = engine.createEntity("particle")
            p.add(TransformComponent(x + offset.x, y + offset.y))
            p.add(VelocityComponent(vx = offset.x * 3f, vy = offset.y * 3f, speed = 1f))
            p.add(SpriteComponent(
                width = 6f + Math.random().toFloat() * 4f,
                height = 6f + Math.random().toFloat() * 4f,
                color = color, alpha = 0.9f
            ))
            p.add(ParticleComponent(lifetime = 0.35f, fadeOut = true, shrink = true))
        }
    }

    private fun spawnExplosion(x: Float, y: Float, radius: Float) {
        val ring = engine.createEntity("particle")
        ring.add(TransformComponent(x, y))
        ring.add(SpriteComponent(width = radius * 2, height = radius * 2, color = 0xFFFFCC80.toInt(), alpha = 0.6f, shape = SpriteShape.CIRCLE))
        ring.add(ParticleComponent(lifetime = 0.4f, fadeOut = true))

        val flash = engine.createEntity("particle")
        flash.add(TransformComponent(x, y))
        flash.add(SpriteComponent(width = radius, height = radius, color = 0xFFFFF5E1.toInt(), alpha = 0.8f, shape = SpriteShape.CIRCLE))
        flash.add(ParticleComponent(lifetime = 0.15f, fadeOut = true, shrink = true))

        repeat(6) {
            val offset = GameMath.randomPointOnCircle(radius * 0.6f, tempVec2)
            val spark = engine.createEntity("particle")
            spark.add(TransformComponent(x + offset.x, y + offset.y))
            spark.add(VelocityComponent(vx = offset.x * 2f, vy = offset.y * 2f, speed = 1f))
            spark.add(SpriteComponent(width = 5f, height = 5f, color = 0xFFFFCC80.toInt(), alpha = 0.8f))
            spark.add(ParticleComponent(lifetime = 0.5f, fadeOut = true, shrink = true))
        }
    }

    private fun spawnPickupEffect(x: Float, y: Float) {
        repeat(5) {
            val angle = it * Math.PI.toFloat() * 2f / 5f
            val p = engine.createEntity("particle")
            p.add(TransformComponent(x, y))
            p.add(VelocityComponent(vx = kotlin.math.cos(angle) * 80f, vy = kotlin.math.sin(angle) * 80f, speed = 1f))
            p.add(SpriteComponent(width = 6f, height = 6f, color = 0xFFAAE6BA.toInt(), alpha = 0.9f))
            p.add(ParticleComponent(lifetime = 0.3f, fadeOut = true, shrink = true))
        }
    }

    private fun spawnDamageNumber(x: Float, y: Float, amount: Float, isCrit: Boolean) {
        val p = engine.createEntity("damage_number")
        p.add(TransformComponent(x, y))
        p.add(DamageNumberComponent(
            amount = amount,
            lifetime = 0.8f,
            isCrit = isCrit,
            vy = -120f - Math.random().toFloat() * 40f
        ))
    }

    private fun getWeaponHitColor(type: WeaponType): Int = when (type) {
        WeaponType.MAGIC_MISSILE -> 0xFF6BB6FF.toInt()
        WeaponType.LIGHTNING_RING -> 0xFF80DEEA.toInt()
        WeaponType.FIREBALL -> 0xFFFFCC80.toInt()
        WeaponType.ICE_SHARD -> 0xFF80CBC4.toInt()
        WeaponType.POISON_CLOUD -> 0xFFAAE6BA.toInt()
        WeaponType.BOOMERANG_DAGGER -> 0xFFFFDAC1.toInt()
        WeaponType.ORBITING_SHIELD -> 0xFFB19CD9.toInt()
        WeaponType.DIVINE_SPEAR -> 0xFFFFF5E1.toInt()
    }
}
