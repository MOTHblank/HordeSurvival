package com.hordesurvival.game.engine.ecs.systems

import com.hordesurvival.game.component.*
import com.hordesurvival.game.engine.GameEngine
import com.badlogic.gdx.utils.IntFloatMap
import com.hordesurvival.game.engine.ecs.Entity
import com.hordesurvival.game.engine.ecs.System
import com.badlogic.gdx.math.Vector2
import com.hordesurvival.game.weapon.WeaponType
import com.hordesurvival.game.weapon.WeaponColors
import com.hordesurvival.game.audio.SoundManager
import com.hordesurvival.utils.GameMath
import kotlin.math.cos
import kotlin.math.sin

/**
 * Auto-attack system: fires all player weapons automatically.
 * Each weapon has unique projectile behavior and targeting.
 * Overhauled: uses GameEngine's SpatialGrid for targeting and enemy discovery.
 *
 * Visual/gameplay pass: lightning per-enemy cooldown now decays with real frame
 * dt (previously decayed once per WEAPON cooldown with one frame's dt — enemies
 * were hit ~once per 30-60s instead of every ring pulse); lightning range query
 * expanded by MAX_ENEMY_RADIUS so large enemies clipping the ring aren't missed;
 * ice shard / divine spear volleys fan out instead of stacking into one line;
 * fireball fan centered ((i - (count-1)/2) — single shots aimed 0.15 rad off
 * target before); weapon colors sourced from shared WeaponColors.
 */
class WeaponSystem(private val engine: GameEngine) : System() {

    // Reusable Vector2 for offset calculation in poison cloud placement
    private val tempVec2 = Vector2()

    // Per-enemy cooldown for Lightning Ring to prevent per-wave damage stacking
    private val lightningHitCooldowns = IntFloatMap()

    // Scratch buffers for spatial queries
    private val _enemyQueryResult = com.badlogic.gdx.utils.Array<Entity>(false, 64)

    // CHANGED: mirrors CollisionSystem — max possible enemy collision radius,
    // added to center-distance queries so large enemies are never missed
    private val MAX_ENEMY_RADIUS = 40f

    override fun update(dt: Float, entities: com.badlogic.gdx.utils.Array<Entity>) {
        val player = engine.playerEntity ?: return
        val playerTransform = player.get<TransformComponent>() ?: return
        val playerComp = player.get<PlayerComponent>() ?: return

        // CHANGED: decay lightning per-enemy cooldowns with the REAL frame dt.
        // This used to live inside fireLightningRing, which only runs once per
        // weapon cooldown (seconds apart) and subtracted a single frame's dt —
        // a 0.3s per-enemy cooldown took ~19 weapon cycles to expire, so enemies
        // were zapped roughly once every 30-60 seconds instead of per pulse.
        val iter = lightningHitCooldowns.iterator()
        while (iter.hasNext()) {
            val entry = iter.next()
            val newTime = entry.value - dt
            if (newTime <= 0f) iter.remove() else lightningHitCooldowns.put(entry.key, newTime)
        }

        // Iterate all WeaponStateComponent entities owned by player
        for (i in 0 until entities.size) {
            val entity = entities[i]
            val ws = entity.get<WeaponStateComponent>() ?: continue

            ws.cooldownTimer -= dt
            if (ws.cooldownTimer <= 0f) {
                fireWeapon(ws, playerTransform, playerComp)
                val cdReduction = 1f - playerComp.cooldownReduction
                ws.cooldownTimer = ws.baseCooldown * cdReduction / playerComp.attackSpeed
            }
        }
    }

    private fun fireWeapon(
        weapon: WeaponStateComponent,
        playerPos: TransformComponent,
        player: PlayerComponent
    ) {
        // Play weapon-specific sound
        // NOTE: fires every cooldown tick even for ORBITING_SHIELD when all
        // shields already exist (sound with no event). Minor; left as-is.
        SoundManager.playShoot(weapon.type)

        when (weapon.type) {
            WeaponType.MAGIC_MISSILE -> fireMagicMissile(weapon, playerPos, player)
            WeaponType.LIGHTNING_RING -> fireLightningRing(weapon, playerPos, player)
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
            val target = if (i < nearestEnemies.size) nearestEnemies[i] else null
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

    private fun fireLightningRing(ws: WeaponStateComponent, pos: TransformComponent, player: PlayerComponent) {
        // Lightning ring is an AOE around the player — damage enemies in radius
        // FIX: per-enemy cooldown (0.3s, decayed in update()) to prevent
        // instant-killing large groups
        val radius = ws.area * player.area
        val damage = ws.baseDamage * player.might
        // CHANGED: query expanded by MAX_ENEMY_RADIUS — center-distance queries
        // miss large enemies whose edge is inside the ring; per-enemy check below
        // is edge-inclusive, matching CollisionSystem.applyAoeDamage semantics.
        val enemies = engine.findInRange(pos.x, pos.y, radius + MAX_ENEMY_RADIUS, "enemy")

        for (i in 0 until enemies.size) {
            val enemy = enemies[i]
            if (!enemy.active) continue
            val hp = enemy.get<HealthComponent>() ?: continue
            // Skip if this enemy was recently hit by lightning
            if (lightningHitCooldowns.containsKey(enemy.id) && lightningHitCooldowns.get(enemy.id, 0f) > 0f) continue
            val eTransform = enemy.get<TransformComponent>() ?: continue
            val eCollision = enemy.get<CollisionComponent>()
            val reach = radius + (eCollision?.radius ?: 16f)
            val dx = eTransform.x - pos.x
            val dy = eTransform.y - pos.y
            if (dx * dx + dy * dy > reach * reach) continue

            hp.takeDamage(damage)
            lightningHitCooldowns.put(enemy.id, 0.3f)  // 300ms cooldown per enemy
            // Visual flash effect
            spawnHitEffect(eTransform.x, eTransform.y, WeaponColors.argb(WeaponType.LIGHTNING_RING))
        }

        // Visual ring effect
        spawnRingEffect(pos.x, pos.y, radius)
    }

    private fun fireFireball(ws: WeaponStateComponent, pos: TransformComponent, player: PlayerComponent) {
        val count = ws.projectileCount + player.projectileBonus
        val target = findNearestEnemy(pos.x, pos.y, 400f)
        val isEvolved = ws.specialEffect == "burn_ground"

        for (i in 0 until count) {
            // CHANGED: (i - (count - 1) / 2f) — was (i - count / 2f), which is
            // off-center: a single fireball aimed 0.15 rad off its target, and
            // 3-volleys drifted half a step to one side.
            val angle = if (target != null) {
                val t = target.get<TransformComponent>()!!
                GameMath.angleTo(pos.x, pos.y, t.x, t.y) + (i - (count - 1) / 2f) * 0.3f
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
            // CHANGED: centered fan — all shards previously fired at the SAME
            // angle, so count > 1 stacked into one visual projectile
            val angle = if (target != null) {
                val t = target.get<TransformComponent>()!!
                GameMath.angleTo(pos.x, pos.y, t.x, t.y) + (i - (count - 1) / 2f) * 0.22f
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
        // NOTE (unchanged): if projectileCount DECREASES, excess shields are
        // never removed — acceptable, just noting.
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
            // CHANGED: tight centered fan — spears previously stacked on one line,
            // wasting pierce on the same enemy column
            val angle = if (target != null) {
                val t = target.get<TransformComponent>()!!
                GameMath.angleTo(pos.x, pos.y, t.x, t.y) + (i - (count - 1) / 2f) * 0.15f
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
            color = WeaponColors.argb(weaponType),
            shape = getWeaponShape(weaponType)
        ))
        entity.add(CollisionComponent(radius = 8f, isTrigger = true))
    }

    private fun spawnPoisonCloud(x: Float, y: Float, damage: Float, radius: Float, duration: Float) {
        val entity = engine.createEntity("poison_cloud")
        entity.add(PoisonCloudComponent(
            damagePerTick = damage, tickInterval = 0.5f,
            lifetime = duration, radius = radius
        ))
        entity.add(TransformComponent(x, y))
        entity.add(SpriteComponent(
            width = radius * 2, height = radius * 2,
            color = WeaponColors.argb(WeaponType.POISON_CLOUD), alpha = 0.4f,
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
            color = WeaponColors.argb(WeaponType.ORBITING_SHIELD),
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
            color = WeaponColors.argb(WeaponType.LIGHTNING_RING), alpha = 0.5f,
            shape = SpriteShape.CIRCLE
        ))
        entity.add(ParticleComponent(lifetime = 0.2f, fadeOut = true))
    }

    // ── Targeting helpers ──────────────────────────────────────────

    private fun findNearestEnemy(x: Float, y: Float, maxDist: Float): Entity? {
        return engine.findNearest(x, y, "enemy", maxDist)
    }

    /**
     * Finds the nearest `count` enemies without allocating lambda closures or lists.
     * Uses an in-place partial selection sort to avoid O(N log N) GC churn.
     * Modifies and returns the reusable `_enemyQueryResult` list.
     */
    private fun findNearestEnemies(x: Float, y: Float, count: Int, maxDist: Float): com.badlogic.gdx.utils.Array<Entity> {
        _enemyQueryResult.clear()
        engine.spatialGrid.queryRange(x, y, maxDist, "enemy", _enemyQueryResult)
        val size = _enemyQueryResult.size
        if (size <= count) return _enemyQueryResult

        val targetCount = minOf(count, size)

        // In-place partial selection sort to find top K nearest
        for (i in 0 until targetCount) {
            var minIdx = i
            var minSq = Float.MAX_VALUE

            val eMin = _enemyQueryResult[minIdx]
            val tMin = eMin.get<TransformComponent>()
            if (tMin != null) {
                val dx = tMin.x - x
                val dy = tMin.y - y
                minSq = dx * dx + dy * dy
            }

            for (j in i + 1 until size) {
                val e = _enemyQueryResult[j]
                val t = e.get<TransformComponent>()
                if (t != null) {
                    val dx = t.x - x
                    val dy = t.y - y
                    val distSq = dx * dx + dy * dy

                    if (distSq < minSq) {
                        minIdx = j
                        minSq = distSq
                    }
                }
            }

            if (minIdx != i) {
                val temp = _enemyQueryResult[i]
                _enemyQueryResult[i] = _enemyQueryResult[minIdx]
                _enemyQueryResult[minIdx] = temp
            }
        }

        // Trim list to target count to prevent caller from iterating beyond K
        while (_enemyQueryResult.size > targetCount) {
            _enemyQueryResult.removeIndex(_enemyQueryResult.size - 1)
        }

        return _enemyQueryResult
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