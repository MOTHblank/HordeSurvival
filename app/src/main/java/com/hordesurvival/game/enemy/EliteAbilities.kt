package com.hordesurvival.game.enemy

import com.hordesurvival.game.component.*
import com.hordesurvival.game.engine.GameEngine
import com.hordesurvival.game.engine.ecs.Entity
import com.hordesurvival.utils.GameMath

/**
 * Elite Enemy Abilities — special behaviors for elite enemies.
 * Elites spawn every 10 levels and have unique dangerous abilities.
 */
class EliteAbilities(private val engine: GameEngine) {

    enum class EliteAbilityType(
        val displayName: String,
        val color: Long,
        val description: String
    ) {
        TELEPORT("Teleporter", 0xFF9C27B0, "Teleports near player every 3s"),
        SHIELDED("Shielded", 0xFF2196F3, "Has a shield that regenerates"),
        EXPLODE_ON_DEATH("Volatile", 0xFFFF5722, "Explodes on death, dealing damage"),
        HEALER_AURA("Healer", 0xFF4CAF50, "Heals nearby enemies"),
        SPEED_BOOST("Swift", 0xFFFFEB3B, "Gets faster when low HP"),
        SPLIT_ON_DEATH("Splitter Elite", 0xFF795548, "Splits into 3 elites on death"),
        DRAIN_HP("Vampiric", 0xFFE91E63, "Heals when hitting player"),
        REFLECT("Thorns", 0xFF607D8B, "Reflects 30% damage back")
    }

    data class EliteState(
        val ability: EliteAbilityType,
        var timer: Float = 0f,
        var shieldHp: Float = 0f,
        var hasExploded: Boolean = false,
        var teleportCooldown: Float = 3f,
        var healCooldown: Float = 2f,
        var reflectPercent: Float = 0.3f
    )

    private val eliteStates = mutableMapOf<Int, EliteState>()  // entity.id → state

    fun assignAbility(entity: Entity): EliteAbilityType {
        val ability = EliteAbilityType.entries.random()
        val state = EliteState(ability = ability)

        when (ability) {
            EliteAbilityType.SHIELDED -> state.shieldHp = 50f
            EliteAbilityType.TELEPORT -> state.teleportCooldown = 3f
            EliteAbilityType.HEALER_AURA -> state.healCooldown = 2f
            else -> {}
        }

        eliteStates[entity.id] = state

        // Visual: add elite component
        entity.add(EliteComponent(
            ability = when (ability) {
                EliteAbilityType.TELEPORT -> com.hordesurvival.game.component.EliteAbility.TELEPORT
                EliteAbilityType.SHIELDED -> com.hordesurvival.game.component.EliteAbility.SHIELDED
                EliteAbilityType.EXPLODE_ON_DEATH -> com.hordesurvival.game.component.EliteAbility.EXPLODE_ON_DEATH
                else -> com.hordesurvival.game.component.EliteAbility.NONE
            },
            shieldHp = state.shieldHp,
            shieldMaxHp = state.shieldHp,
            shieldActive = ability == EliteAbilityType.SHIELDED
        ))

        // Tint the sprite
        entity.get<SpriteComponent>()?.let {
            it.color = ability.color.toInt()
            it.width *= 1.3f
            it.height *= 1.3f
        }

        return ability
    }

    fun update(dt: Float, entities: com.badlogic.gdx.utils.Array<Entity>, playerPos: TransformComponent?, playerHealth: HealthComponent?) {
        for (i in 0 until entities.size) {
            val entity = entities[i]
            if (entity.tag != "enemy" || !entity.active) continue
            val state = eliteStates[entity.id] ?: continue
            val transform = entity.get<TransformComponent>() ?: continue

            when (state.ability) {
                EliteAbilityType.TELEPORT -> {
                    state.timer += dt
                    if (state.timer >= state.teleportCooldown && playerPos != null) {
                        state.timer = 0f
                        // Teleport near player
                        val angle = GameMath.randomRange(0f, Math.PI.toFloat() * 2f)
                        val dist = GameMath.randomRange(100f, 200f)
                        transform.x = playerPos.x + kotlin.math.cos(angle) * dist
                        transform.y = playerPos.y + kotlin.math.sin(angle) * dist
                        // Visual effect
                        spawnTeleportEffect(transform.x, transform.y)
                    }
                }

                EliteAbilityType.SHIELDED -> {
                    // Regenerate shield slowly
                    if (state.shieldHp < 50f) {
                        state.shieldHp += 2f * dt
                    }
                    entity.get<EliteComponent>()?.shieldHp = state.shieldHp
                }

                EliteAbilityType.HEALER_AURA -> {
                    state.timer += dt
                    if (state.timer >= state.healCooldown) {
                        state.timer = 0f
                        // Heal nearby enemies
                        val nearby = engine.findInRange(transform.x, transform.y, 120f, "enemy")
                        for (e in nearby) {
                            if (e.id == entity.id) continue
                            e.get<HealthComponent>()?.heal(5f)
                        }
                        spawnHealEffect(transform.x, transform.y)
                    }
                }

                EliteAbilityType.SPEED_BOOST -> {
                    // Get faster when low HP
                    val hp = entity.get<HealthComponent>()
                    if (hp != null) {
                        val hpRatio = hp.currentHp / hp.maxHp
                        val speedBoost = if (hpRatio < 0.3f) 2f else if (hpRatio < 0.5f) 1.5f else 1f
                        entity.get<VelocityComponent>()?.let {
                            it.speed = it.speed.coerceAtLeast(60f * speedBoost)
                        }
                    }
                }

                EliteAbilityType.DRAIN_HP -> {
                    // Heals when colliding with player (handled in collision)
                }

                EliteAbilityType.REFLECT -> {
                    // Reflects damage (handled in collision)
                }

                else -> {}
            }
        }
    }

    /** Called when an elite enemy takes damage — returns reflected damage */
    fun onEliteDamaged(entityId: Int, damage: Float): Float {
        val state = eliteStates[entityId] ?: return 0f
        if (state.ability == EliteAbilityType.REFLECT) {
            return damage * state.reflectPercent
        }
        return 0f
    }

    /** Called when an elite enemy dies */
    fun onEliteDeath(entity: Entity, playerPos: TransformComponent?) {
        val state = eliteStates[entity.id] ?: return
        val pos = entity.get<TransformComponent>() ?: return

        when (state.ability) {
            EliteAbilityType.EXPLODE_ON_DEATH -> {
                if (!state.hasExploded) {
                    state.hasExploded = true
                    // Deal damage to player if nearby
                    if (playerPos != null) {
                        val dx = playerPos.x - pos.x
                        val dy = playerPos.y - pos.y
                        if (dx * dx + dy * dy < 120f * 120f) {
                            // Player takes explosion damage
                            spawnExplosionEffect(pos.x, pos.y)
                        }
                    }
                    // Damage nearby enemies too
                    val nearby = engine.findInRange(pos.x, pos.y, 100f, "enemy")
                    for (e in nearby) {
                        e.get<HealthComponent>()?.takeDamage(20f)
                    }
                }
            }

            EliteAbilityType.SPLIT_ON_DEATH -> {
                // Spawn 3 mini-elites
                for (i in 0 until 3) {
                    val angle = (i * 120f) * Math.PI.toFloat() / 180f
                    val e = engine.createEntity("enemy")
                    e.add(TransformComponent(pos.x + kotlin.math.cos(angle) * 30f, pos.y + kotlin.math.sin(angle) * 30f))
                    e.add(VelocityComponent(speed = 100f))
                    e.add(HealthComponent(currentHp = 30f, maxHp = 30f))
                    e.add(EnemyComponent(
                        type = EnemyType.SWARM_BAT,
                        damage = 8f,
                        xpValue = 1f, goldValue = 1f,
                        contactCooldown = 0.5f
                    ))
                    e.add(SpriteComponent(width = 14f, height = 14f, color = 0xFF795548.toInt(), shape = SpriteShape.CIRCLE))
                    e.add(CollisionComponent(radius = 8f))
                }
            }

            else -> {}
        }

        eliteStates.remove(entity.id)
    }

    private fun spawnTeleportEffect(x: Float, y: Float) {
        repeat(8) {
            val angle = it * Math.PI.toFloat() * 2f / 8f
            val p = engine.createEntity("particle")
            p.add(TransformComponent(x, y))
            p.add(VelocityComponent(vx = kotlin.math.cos(angle) * 100f, vy = kotlin.math.sin(angle) * 100f, speed = 1f))
            p.add(SpriteComponent(width = 6f, height = 6f, color = 0xFF9C27B0.toInt(), alpha = 0.8f))
            p.add(ParticleComponent(lifetime = 0.4f, fadeOut = true, shrink = true))
        }
    }

    private fun spawnHealEffect(x: Float, y: Float) {
        val p = engine.createEntity("particle")
        p.add(TransformComponent(x, y))
        p.add(SpriteComponent(width = 60f, height = 60f, color = 0xFF4CAF50.toInt(), alpha = 0.3f, shape = SpriteShape.CIRCLE))
        p.add(ParticleComponent(lifetime = 0.5f, fadeOut = true))
    }

    private fun spawnExplosionEffect(x: Float, y: Float) {
        val ring = engine.createEntity("particle")
        ring.add(TransformComponent(x, y))
        ring.add(SpriteComponent(width = 200f, height = 200f, color = 0xFFFF5722.toInt(), alpha = 0.5f, shape = SpriteShape.CIRCLE))
        ring.add(ParticleComponent(lifetime = 0.5f, fadeOut = true))
    }

    fun reset() {
        eliteStates.clear()
    }
}
