package com.hordesurvival.game.engine.ecs.systems

import com.hordesurvival.game.component.*
import com.hordesurvival.game.engine.GameEngine
import com.hordesurvival.game.engine.ecs.Entity
import com.hordesurvival.game.engine.ecs.System
import com.hordesurvival.game.audio.SoundManager
import com.hordesurvival.game.weapon.WeaponType
import com.hordesurvival.utils.Constants
import com.hordesurvival.utils.GameMath

/**
 * Handles all collision detection with proper cooldowns and damage batching.
 * Optimized: no per-frame list allocation — iterates entities directly.
 */
class CollisionSystem(private val engine: GameEngine) : System() {

    // Shield contact cooldown per enemy (prevents per-frame damage)
    private val shieldHitCooldowns = mutableMapOf<Int, Float>()

    // Reusable collections — no allocation per frame
    private val damageNumbers = mutableListOf<DamageNumberData>()
    private val _enemies = mutableListOf<Entity>()
    private val _projectiles = mutableListOf<Entity>()
    private val _enemyProjectiles = mutableListOf<Entity>()
    private val _xpGems = mutableListOf<Entity>()
    private val _healthGems = mutableListOf<Entity>()
    private val _poisonClouds = mutableListOf<Entity>()
    private val _orbitShields = mutableListOf<Entity>()

    data class DamageNumberData(val x: Float, val y: Float, val amount: Float, val isCrit: Boolean)

    override fun update(dt: Float, entities: List<Entity>) {
        val player = entities.find { it.tag == "player" && it.has<PlayerComponent>() } ?: return
        val playerTransform = player.get<TransformComponent>() ?: return
        val playerHealth = player.get<HealthComponent>() ?: return
        val playerCollision = player.get<CollisionComponent>() ?: return
        val playerComp = player.get<PlayerComponent>() ?: return

        // Update invincibility timer
        if (playerHealth.invincibleTimer > 0f) {
            playerHealth.invincibleTimer -= dt
        }

        // Categorize entities in a single pass — no allocation
        _enemies.clear(); _projectiles.clear(); _xpGems.clear(); _healthGems.clear()
        _poisonClouds.clear(); _orbitShields.clear()
        for (e in entities) {
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

        // Decay shield cooldowns — iterate map directly, use enemy list for validation
        val cooldownIter = shieldHitCooldowns.iterator()
        while (cooldownIter.hasNext()) {
            val entry = cooldownIter.next()
            val newTime = entry.value - dt
            if (newTime <= 0f) cooldownIter.remove() else entry.setValue(newTime)
        }

        damageNumbers.clear()

        // ── Player vs Enemy contact damage ─────────────────────────
        for (enemy in _enemies) {
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
        for (eproj in _enemyProjectiles) {
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

        // ── Projectile vs Enemy ───────────────────────────────────
        for (proj in _projectiles) {
            val pTransform = proj.get<TransformComponent>() ?: continue
            val pComp = proj.get<ProjectileComponent>() ?: continue
            val projCollision = proj.get<CollisionComponent>()?.radius ?: 8f

            for (enemy in _enemies) {
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
                    // Shield absorption: elite shielded enemies take reduced projectile damage
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
                        // Only apply if new slow is stronger or longer than current
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

        // ── Poison Cloud vs Enemy (with proper tick timing) ───────
        for (cloud in _poisonClouds) {
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
                val nearbyEnemies = engine.findInRange(cTransform.x, cTransform.y, cComp.radius, "enemy")
                for (enemy in nearbyEnemies) {
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

        // ── Orbit Shield vs Enemy (with cooldown fix + might scaling) ──
        val shieldDamage = 10f * playerComp.might  // Scale with player might
        for (shield in _orbitShields) {
            val sTransform = shield.get<TransformComponent>() ?: continue
            val sHealth = shield.get<HealthComponent>() ?: continue

            for (enemy in _enemies) {
                val eTransform = enemy.get<TransformComponent>() ?: continue
                val eHealth = enemy.get<HealthComponent>() ?: continue
                val eCollision = enemy.get<CollisionComponent>() ?: continue

                val dx = sTransform.x - eTransform.x
                val dy = sTransform.y - eTransform.y
                val distSq = dx * dx + dy * dy
                val minDist = 12f + eCollision.radius

                if (distSq < minDist * minDist) {
                    val cooldownKey = enemy.id
                    if (shieldHitCooldowns[cooldownKey] == null || shieldHitCooldowns[cooldownKey]!! <= 0f) {
                        eHealth.takeDamage(shieldDamage)
                        sHealth.takeDamage(3f)
                        shieldHitCooldowns[cooldownKey] = 0.3f
                        spawnHitParticles(eTransform.x, eTransform.y, 0xFFB19CD9.toInt())
                    }
                }
            }

            if (sHealth.isDead) {
                shield.active = false
                spawnExplosion(sTransform.x, sTransform.y, 40f)
            }
        }

        // ── Player vs XP Gem (skip when paused) ───────────────────
        if (engine.isPaused) return
        for (gem in _xpGems) {
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
                val dist = GameMath.distance(playerTransform.x, playerTransform.y, gTransform.x, gTransform.y)
                val speed = com.hordesurvival.utils.Constants.GXP_MAGNET_SPEED + (1f - dist / playerComp.pickupRange).coerceIn(0f, 1f) * 300f
                val len = if (dist > 0f) dist else 1f
                gTransform.x += (dx / len) * speed * dt
                gTransform.y += (dy / len) * speed * dt

                gSprite?.let {
                    val pulse = 1f + 0.15f * kotlin.math.sin(engine.gameTime * 10f)
                    it.scaleX = pulse
                    it.scaleY = pulse
                }
            }

            if (distSq < 24f * 24f) {
                // Apply combo multiplier to XP
                val comboMult = player.get<ComboComponent>()?.comboMultiplier ?: 1f
                playerComp.addXp(gComp.value * comboMult)
                gem.active = false
                spawnPickupEffect(gTransform.x, gTransform.y)
                SoundManager.playPickup()
            }
        }

        // ── Player vs Health Gem ────────────────────────────────
        for (gem in _healthGems) {
            val gTransform = gem.get<TransformComponent>() ?: continue
            val gComp = gem.get<XpGemComponent>() ?: continue

            val dx = playerTransform.x - gTransform.x
            val dy = playerTransform.y - gTransform.y
            val distSq = dx * dx + dy * dy

            // Auto-magnet if in range
            if (distSq < playerComp.pickupRange * playerComp.pickupRange) {
                gComp.magnetized = true
            }

            if (gComp.magnetized) {
                val dist = GameMath.distance(playerTransform.x, playerTransform.y, gTransform.x, gTransform.y)
                val speed = com.hordesurvival.utils.Constants.GXP_MAGNET_SPEED
                val len = if (dist > 0f) dist else 1f
                gTransform.x += (dx / len) * speed * dt
                gTransform.y += (dy / len) * speed * dt
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

        // ── Spawn damage numbers ──────────────────────────────────
        for (dn in damageNumbers) {
            spawnDamageNumberEntity(dn.x, dn.y, dn.amount, dn.isCrit)
        }
    }

    private fun applyAoeDamage(x: Float, y: Float, radius: Float, damage: Float) {
        val nearby = engine.findInRange(x, y, radius, "enemy")
        for (enemy in nearby) {
            enemy.get<HealthComponent>()?.takeDamage(damage)
        }
    }

    private fun spawnHitParticles(x: Float, y: Float, color: Int) {
        repeat(4) {
            val offset = GameMath.randomPointInCircle(8f)
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
            val offset = GameMath.randomPointOnCircle(radius * 0.6f)
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
        damageNumbers.add(DamageNumberData(x, y, amount, isCrit))
    }

    private fun spawnDamageNumberEntity(x: Float, y: Float, amount: Float, isCrit: Boolean) {
        // Spawn a damage number entity — rendered as text in GameRenderer
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
