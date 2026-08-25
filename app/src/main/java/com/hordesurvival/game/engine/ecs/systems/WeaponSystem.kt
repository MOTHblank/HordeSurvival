package com.hordesurvival.game.engine.ecs.systems

import com.hordesurvival.game.component.*
import com.hordesurvival.game.engine.GameEngine
import com.badlogic.gdx.utils.IntFloatMap
import com.hordesurvival.game.engine.ecs.Entity
import com.hordesurvival.game.engine.ecs.System
import com.badlogic.gdx.math.Vector2
import com.hordesurvival.game.weapon.WeaponType
import com.hordesurvival.game.weapon.WeaponEvolution
import com.hordesurvival.game.audio.SoundManager
import com.hordesurvival.utils.Constants
import com.hordesurvival.utils.GameMath
import kotlin.math.cos
import kotlin.math.sin

/**
 * Auto-attack system: fires all player weapons automatically.
 * Each weapon has unique projectile behavior and targeting.
 * Overhauled: uses GameEngine's SpatialGrid for targeting and enemy discovery.
 */
class WeaponSystem(private val engine: GameEngine) : System() {

    // Reusable Vector2 for offset calculation in poison cloud placement
    private val tempVec2 = Vector2()

    // Per-enemy cooldown for Lightning Ring to prevent per-wave damage stacking
    private val lightningHitCooldowns = IntFloatMap()

    // Scratch buffers for spatial queries
    private val _enemyQueryResult = mutableListOf<Entity>()

    override fun update(dt: Float, entities: List<Entity>) {
        val player = engine.playerEntity ?: return
        val playerTransform = player.get<TransformComponent>() ?: return
        val playerComp = player.get<PlayerComponent>() ?: return

        // Iterate all WeaponStateComponent entities owned by player
        for (i in 0 until entities.size) {
            val entity = entities[i]
            val ws = entity.get<WeaponStateComponent>() ?: continue

            ws.cooldownTimer -= dt
            if (ws.cooldownTimer <= 0f) {
                fireWeapon(ws, playerTransform, playerComp, entities, dt)
                val cdReduction = 1f - playerComp.cooldownReduction
                ws.cooldownTimer = ws.baseCooldown * cdReduction / playerComp.attackSpeed
            }
        }
    }

    private fun fireWeapon(
        weapon: WeaponStateComponent,
        playerPos: TransformComponent,
        player: PlayerComponent,
        entities: List<Entity>,
        dt: Float = 1f / 60f
    ) {
        // Play weapon-specific sound
        SoundManager.playShoot(weapon.type)

        when (weapon.type) {
            WeaponType.MAGIC_MISSILE -> fireMagicMissile(weapon, playerPos, player)
            WeaponType.LIGHTNING_RING -> fireLightningRing(weapon, playerPos, player, dt)
            WeaponType.FIREBALL -> fireFireball(weapon, playerPos, player)
            WeaponType.ICE_SHARD -> fireIceShard(weapon, playerPos, player)
            WeaponType.POISON_CLOUD -> firePoisonCloud(weapon, playerPos, player)
            WeaponType.BOOMERANG_DAGGER -> fireBoomerang(weapon, playerPos, player)
            WeaponType.ORBITING_SHIELD -> fireOrbitingShield(weapon, playerPos, player)
            WeaponType.DIVINE_SPEAR -> fireDivineSpear(weapon, playerPos, player)
        }
    }

    private fun fireMagicMissile(ws: WeaponStateComponent, pos: TransformComponent, player: PlayerComponent) {
        val count = ws.projectileCount + player.projectileBonus
        val nearestEnemies = findNearestEnemies(pos.x, pos.y, count, 500f)
        val isEvolved = ws.specialEffect == "orbit_homing"

        for (i in 0 until count) {
            val target = nearestEnemies.getOrNull(i)
            val angle = if (target != null) {
                val t = target.get<TransformComponent>()
                if (t != null) GameMath.angleTo(pos.x, pos.y, t.x, t.y)
                else (i * Math.PI.toFloat() * 2f / count)
            } else {
                (i * Math.PI.toFloat() * 2f / count)
            }

            spawnProjectile(
                x = pos.x, y = pos.y,
                angle = angle,
                damage = ws.baseDamage * player.might,
                speed = if (isEvolved) 500f else 400f,
                lifetime = if (isEvolved) 5f else 3f,
                pierce = if (isEvolved) 999 else 0,
                isHoming = true,
                homingStrength = if (isEvolved) 8f else if (ws.tier >= 4) 4f else 2f,
                targetId = target?.id ?: -1,
                weaponType = WeaponType.MAGIC_MISSILE,
                aoeRadius = if (isEvolved) 50f else if (ws.tier >= 5) 30f else 0f
            )
        }
    }

    private fun fireLightningRing(ws: WeaponStateComponent, pos: TransformComponent, player: PlayerComponent, dt: Float) {
        // Lightning ring is an AOE around the player — damage enemies in radius
        // FIX: per-enemy cooldown (0.3s) to prevent instant-killing large groups
        val radius = ws.area * player.area
        val damage = ws.baseDamage * player.might
        val enemies = engine.findInRange(pos.x, pos.y, radius, "enemy")

        // Decay cooldowns with real dt
        val iter = lightningHitCooldowns.iterator()
        while (iter.hasNext()) {
            val entry = iter.next()
            val newTime = entry.value - dt
            if (newTime <= 0f) iter.remove() else lightningHitCooldowns.put(entry.key, newTime)
        }

        for (i in 0 until enemies.size) {
            val enemy = enemies[i]
            val hp = enemy.get<HealthComponent>() ?: continue
            // Skip if this enemy was recently hit by lightning
            if (lightningHitCooldowns.containsKey(enemy.id) && lightningHitCooldowns.get(enemy.id, 0f) > 0f) continue
            hp.takeDamage(damage)
            lightningHitCooldowns.put(enemy.id, 0.3f)  // 300ms cooldown per enemy
            // Visual flash effect
            spawnHitEffect(enemy.get<TransformComponent>()?.x ?: pos.x,
                enemy.get<TransformComponent>()?.y ?: pos.y, 0xFF6BB6FF.toInt())
        }

        // Visual ring effect
        spawnRingEffect(pos.x, pos.y, radius)
    }

    private fun fireFireball(ws: WeaponStateComponent, pos: TransformComponent, player: PlayerComponent) {
        val count = ws.projectileCount + player.projectileBonus
        val target = findNearestEnemy(pos.x, pos.y, 400f)
        val isEvolved = ws.specialEffect == "burn_ground"

        for (i in 0 until count) {
            val angle = if (target != null) {
                val t = target.get<TransformComponent>()!!
                GameMath.angleTo(pos.x, pos.y, t.x, t.y) + (i - count / 2f) * 0.3f
            } else {
                (i * Math.PI.toFloat() * 2f / count)
            }

            spawnProjectile(
                x = pos.x, y = pos.y,
                angle = angle,
                damage = ws.baseDamage * player.might,
                speed = if (isEvolved) 400f else 300f,
                lifetime = 2.5f,
                pierce = 0,
                weaponType = WeaponType.FIREBALL,
                aoeRadius = ws.area * player.area * (if (isEvolved) 2.5f else if (ws.tier >= 4) 1.5f else 1f),
                burnDamage = ws.baseDamage * player.might * (if (isEvolved) 0.6f else if (ws.tier >= 3) 0.3f else 0f),
                burnDuration = if (isEvolved || ws.tier >= 3) 3f else 0f
            )
        }
    }

    private fun fireIceShard(ws: WeaponStateComponent, pos: TransformComponent, player: PlayerComponent) {
        val count = ws.projectileCount + player.projectileBonus
        val target = findNearestEnemy(pos.x, pos.y, 400f)

        for (i in 0 until count) {
            val angle = if (target != null) {
                val t = target.get<TransformComponent>()!!
                GameMath.angleTo(pos.x, pos.y, t.x, t.y)
            } else {
                (i * Math.PI.toFloat() * 2f / count)
            }

            spawnProjectile(
                x = pos.x, y = pos.y,
                angle = angle,
                damage = ws.baseDamage * player.might,
                speed = 500f,
                lifetime = 2f,
                pierce = 1 + if (ws.tier >= 4) 2 else if (ws.tier >= 1) 1 else 0,
                weaponType = WeaponType.ICE_SHARD,
                slowFactor = if (ws.tier >= 4) 0.3f else 0.5f,
                slowDuration = 2f
            )
        }
    }

    private fun firePoisonCloud(ws: WeaponStateComponent, pos: TransformComponent, player: PlayerComponent) {
        val count = ws.projectileCount + player.projectileBonus
        val target = findNearestEnemy(pos.x, pos.y, 300f)

        for (i in 0 until count) {
            // Spawn cloud at a position near enemies
            val cx: Float
            val cy: Float
            if (target != null) {
                val t = target.get<TransformComponent>()!!
                val offset = GameMath.randomPointInCircle(50f, tempVec2)
                cx = t.x + offset.x
                cy = t.y + offset.y
            } else {
                val offset = GameMath.randomPointOnCircle(150f, tempVec2)
                cx = pos.x + offset.x
                cy = pos.y + offset.y
            }

            spawnPoisonCloud(cx, cy, ws.baseDamage * player.might, ws.area * player.area,
                if (ws.tier >= 2) 7f else 5f)
        }
    }

    private fun fireBoomerang(ws: WeaponStateComponent, pos: TransformComponent, player: PlayerComponent) {
        val count = ws.projectileCount + player.projectileBonus
        val target = findNearestEnemy(pos.x, pos.y, 400f)

        for (i in 0 until count) {
            val angle = if (target != null) {
                val t = target.get<TransformComponent>()!!
                GameMath.angleTo(pos.x, pos.y, t.x, t.y) + i * 0.5f
            } else {
                (i * Math.PI.toFloat() * 2f / count)
            }

            val maxDist = 200f * (if (ws.tier >= 1) 1.3f else 1f)
            spawnProjectile(
                x = pos.x, y = pos.y,
                angle = angle,
                damage = ws.baseDamage * player.might,
                speed = 350f,
                lifetime = 3f,
                pierce = if (ws.tier >= 4) 999 else 0,
                weaponType = WeaponType.BOOMERANG_DAGGER,
                returnsToPlayer = true,
                returnSpeed = 300f * (if (ws.tier >= 3) 1.5f else 1f),
                maxDistance = maxDist
            )
        }
    }

    private fun fireOrbitingShield(ws: WeaponStateComponent, pos: TransformComponent, player: PlayerComponent) {
        val count = ws.projectileCount + player.projectileBonus
        val activeEntities = engine.getActiveEntities()
        var existingCount = 0
        for (i in 0 until activeEntities.size) {
            val e = activeEntities[i]
            if (e.tag == "orbit_shield" && e.has<OrbitComponent>()) {
                e.get<OrbitComponent>()?.let { it.radius = ws.area * player.area }
                existingCount++
            }
        }

        // Ensure correct number of shields
        if (existingCount < count) {
            for (i in existingCount until count) {
                val angle = (i.toFloat() / count) * Math.PI.toFloat() * 2f
                val orbitRadius = ws.area * player.area
                spawnOrbitShield(pos.x, pos.y, orbitRadius,
                    angle, 2f, ws.baseDamage * player.might,
                    50f * (if (ws.tier >= 2) 1.2f else 1f))
            }
        }
    }

    private fun fireDivineSpear(ws: WeaponStateComponent, pos: TransformComponent, player: PlayerComponent) {
        val count = ws.projectileCount + player.projectileBonus
        val target = findNearestEnemy(pos.x, pos.y, 600f)

        for (i in 0 until count) {
            val angle = if (target != null) {
                val t = target.get<TransformComponent>()!!
                GameMath.angleTo(pos.x, pos.y, t.x, t.y)
            } else {
                (Math.PI.toFloat() / 2f)
            }

            val critChance = 0.1f + if (ws.tier >= 3) 0.15f else 0f
            val isCrit = Math.random() < critChance
            val damage = ws.baseDamage * player.might * if (isCrit) 2.5f else 1f

            spawnProjectile(
                x = pos.x, y = pos.y,
                angle = angle,
                damage = damage,
                speed = 600f,
                lifetime = 1.5f,
                pierce = if (ws.tier >= 4) 999 else 0,
                weaponType = WeaponType.DIVINE_SPEAR,
                aoeRadius = if (isCrit && ws.tier >= 5) 80f else 0f
            )
        }
    }

    // ── Helper spawn functions ──────────────────────────────────────

    private fun spawnProjectile(
        x: Float, y: Float, angle: Float, damage: Float, speed: Float,
        lifetime: Float, pierce: Int, weaponType: WeaponType,
        isHoming: Boolean = false, homingStrength: Float = 0f,
        targetId: Int = -1, aoeRadius: Float = 0f,
        burnDamage: Float = 0f, burnDuration: Float = 0f,
        slowFactor: Float = 1f, slowDuration: Float = 0f,
        returnsToPlayer: Boolean = false, returnSpeed: Float = 0f,
        maxDistance: Float = 0f
    ) {
        val entity = engine.createEntity("projectile")
        entity.add(TransformComponent(x, y, angle))
        entity.add(VelocityComponent(cos(angle), sin(angle), speed))
        entity.add(ProjectileComponent(
            damage = damage, speed = speed, lifetime = lifetime, timer = lifetime,
            pierceCount = pierce, pierceRemaining = pierce,
            isHoming = isHoming, homingStrength = homingStrength, targetId = targetId,
            aoeRadius = aoeRadius, burnDamage = burnDamage, burnDuration = burnDuration,
            slowFactor = slowFactor, slowDuration = slowDuration,
            returnsToPlayer = returnsToPlayer, returnSpeed = returnSpeed,
            maxDistance = maxDistance, weaponType = weaponType
        ))
        entity.add(SpriteComponent(
            width = 12f, height = 12f,
            color = getWeaponColor(weaponType),
            shape = getWeaponShape(weaponType)
        ))
        entity.add(CollisionComponent(radius = 8f, isTrigger = true))
    }

    private fun spawnPoisonCloud(x: Float, y: Float, damage: Float, radius: Float, duration: Float) {
        val entity = engine.createEntity("poison_cloud")
        entity.add(TransformComponent(x, y))
        entity.add(PoisonCloudComponent(
            damagePerTick = damage, tickInterval = 0.5f,
            lifetime = duration, radius = radius
        ))
        entity.add(SpriteComponent(
            width = radius * 2, height = radius * 2,
            color = 0xFFAAE6BA.toInt(), alpha = 0.4f,
            shape = SpriteShape.CIRCLE
        ))
    }

    private fun spawnOrbitShield(x: Float, y: Float, radius: Float, angle: Float,
                                  angularSpeed: Float, damage: Float, hp: Float) {
        val entity = engine.createEntity("orbit_shield")
        entity.add(TransformComponent(x, y))
        entity.add(OrbitComponent(x, y, radius, angle, angularSpeed, hp))
        entity.add(SpriteComponent(
            width = 20f, height = 20f,
            color = 0xFFB19CD9.toInt(),
            shape = SpriteShape.DIAMOND
        ))
        entity.add(CollisionComponent(radius = 12f, isTrigger = true))
        entity.add(HealthComponent(currentHp = hp, maxHp = hp))
    }

    private fun spawnHitEffect(x: Float, y: Float, color: Int) {
        val entity = engine.createEntity("particle")
        entity.add(TransformComponent(x, y))
        entity.add(SpriteComponent(width = 16f, height = 16f, color = color, alpha = 0.8f))
        entity.add(ParticleComponent(lifetime = 0.3f, fadeOut = true, shrink = true))
    }

    private fun spawnRingEffect(x: Float, y: Float, radius: Float) {
        val entity = engine.createEntity("particle")
        entity.add(TransformComponent(x, y))
        entity.add(SpriteComponent(
            width = radius * 2, height = radius * 2,
            color = 0xFF6BB6FF.toInt(), alpha = 0.5f,
            shape = SpriteShape.CIRCLE
        ))
        entity.add(ParticleComponent(lifetime = 0.2f, fadeOut = true))
    }

    // ── Targeting helpers ──────────────────────────────────────────

    private fun findNearestEnemy(x: Float, y: Float, maxDist: Float): Entity? {
        return engine.findNearest(x, y, "enemy", maxDist)
    }

    private fun findNearestEnemies(x: Float, y: Float, count: Int, maxDist: Float): List<Entity> {
        _enemyQueryResult.clear()
        engine.spatialGrid.queryRange(x, y, maxDist, "enemy", _enemyQueryResult)
        if (_enemyQueryResult.size <= count) return _enemyQueryResult.toList()

        _enemyQueryResult.sortBy { e ->
            val t = e.get<TransformComponent>()
            if (t != null) {
                val dx = t.x - x
                val dy = t.y - y
                dx * dx + dy * dy
            } else Float.MAX_VALUE
        }
        return _enemyQueryResult.take(count)
    }

    private fun getWeaponColor(type: WeaponType): Int = when (type) {
        WeaponType.MAGIC_MISSILE -> 0xFF6BB6FF.toInt()
        WeaponType.LIGHTNING_RING -> 0xFF80DEEA.toInt()
        WeaponType.FIREBALL -> 0xFFFFCC80.toInt()
        WeaponType.ICE_SHARD -> 0xFF80CBC4.toInt()
        WeaponType.POISON_CLOUD -> 0xFFAAE6BA.toInt()
        WeaponType.BOOMERANG_DAGGER -> 0xFFFFDAC1.toInt()
        WeaponType.ORBITING_SHIELD -> 0xFFB19CD9.toInt()
        WeaponType.DIVINE_SPEAR -> 0xFFFFF5E1.toInt()
    }

    private fun getWeaponShape(type: WeaponType): SpriteShape = when (type) {
        WeaponType.MAGIC_MISSILE -> SpriteShape.CIRCLE
        WeaponType.LIGHTNING_RING -> SpriteShape.STAR
        WeaponType.FIREBALL -> SpriteShape.CIRCLE
        WeaponType.ICE_SHARD -> SpriteShape.TRIANGLE
        WeaponType.POISON_CLOUD -> SpriteShape.CIRCLE
        WeaponType.BOOMERANG_DAGGER -> SpriteShape.DIAMOND
        WeaponType.ORBITING_SHIELD -> SpriteShape.DIAMOND
        WeaponType.DIVINE_SPEAR -> SpriteShape.TRIANGLE
    }
}
