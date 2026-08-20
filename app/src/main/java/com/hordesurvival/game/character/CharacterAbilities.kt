package com.hordesurvival.game.character

import com.hordesurvival.game.component.*
import com.hordesurvival.game.engine.GameEngine
import com.hordesurvival.game.engine.ecs.Entity
import com.hordesurvival.utils.GameMath

/**
 * Character Abilities — unique active abilities per character.
 * Each character has a signature ability with cooldown.
 */
class CharacterAbilities(private val engine: GameEngine) {

    enum class AbilityType(
        val displayName: String,
        val description: String,
        val icon: String,
        val cooldown: Float,
        val color: Long
    ) {
        TIME_FREEZE(
            "Time Freeze", "Freeze all enemies for 3 seconds",
            "⏸️", 20f, 0xFF00BCD4
        ),
        METEOR_STRIKE(
            "Meteor Strike", "Call down a meteor dealing massive AOE damage",
            "☄️", 25f, 0xFFFF5722
        ),
        SHADOW_CLONE(
            "Shadow Clone", "Create a clone that fights for you for 5 seconds",
            "👤", 18f, 0xFF9C27B0
        ),
        HEALING_AURA(
            "Healing Aura", "Heal 50% HP and gain 3s invincibility",
            "💚", 30f, 0xFF4CAF50
        ),
        LIGHTNING_STORM(
            "Lightning Storm", "Strike all enemies with lightning",
            "⚡", 22f, 0xFFFFEB3B
        ),
        BERSERKER_RAGE(
            "Berserker Rage", "2x damage and speed for 5 seconds",
            "🔥", 20f, 0xFFFF5722
        ),
        FROST_NOVA(
            "Frost Nova", "Freeze and damage all enemies nearby",
            "❄️", 15f, 0xFF80CBC4
        ),
        SOUL_HARVEST(
            "Soul Harvest", "Kill all enemies below 30% HP",
            "💀", 35f, 0xFFE040FB
        )
    }

    data class AbilityState(
        val type: AbilityType,
        var cooldownTimer: Float = 0f,
        var isActive: Boolean = false,
        var activeTimer: Float = 0f
    ) {
        val isReady: Boolean get() = cooldownTimer <= 0f && !isActive
        val cooldownProgress: Float get() = (cooldownTimer / type.cooldown).coerceIn(0f, 1f)
    }

    private var currentAbility: AbilityState? = null
    private var cloneEntity: Entity? = null

    /** Set ability based on starting weapon/character */
    fun setAbility(weapon: com.hordesurvival.game.weapon.WeaponType) {
        val type = when (weapon) {
            com.hordesurvival.game.weapon.WeaponType.MAGIC_MISSILE -> AbilityType.TIME_FREEZE
            com.hordesurvival.game.weapon.WeaponType.FIREBALL -> AbilityType.METEOR_STRIKE
            com.hordesurvival.game.weapon.WeaponType.ICE_SHARD -> AbilityType.FROST_NOVA
            com.hordesurvival.game.weapon.WeaponType.LIGHTNING_RING -> AbilityType.LIGHTNING_STORM
            com.hordesurvival.game.weapon.WeaponType.POISON_CLOUD -> AbilityType.SOUL_HARVEST
            com.hordesurvival.game.weapon.WeaponType.BOOMERANG_DAGGER -> AbilityType.SHADOW_CLONE
            com.hordesurvival.game.weapon.WeaponType.ORBITING_SHIELD -> AbilityType.HEALING_AURA
            com.hordesurvival.game.weapon.WeaponType.DIVINE_SPEAR -> AbilityType.BERSERKER_RAGE
        }
        currentAbility = AbilityState(type = type)
    }

    /** Activate the ability */
    fun activate(player: Entity): Boolean {
        val ability = currentAbility ?: return false
        if (!ability.isReady) return false

        val pos = player.get<TransformComponent>() ?: return false
        val health = player.get<HealthComponent>() ?: return false
        val comp = player.get<PlayerComponent>() ?: return false

        ability.cooldownTimer = ability.type.cooldown
        ability.isActive = true
        ability.activeTimer = getActiveDuration(ability.type)

        when (ability.type) {
            AbilityType.TIME_FREEZE -> {
                // Freeze all enemies
                for (e in engine.getActiveEntities()) {
                    if (e.tag == "enemy") {
                        e.get<EnemyComponent>()?.let {
                            it.slowTimer = 3f
                            it.slowFactor = 0f  // complete freeze
                        }
                    }
                }
                spawnGlobalEffect(0xFF00BCD4.toInt(), 3f)
            }

            AbilityType.METEOR_STRIKE -> {
                // Damage all enemies
                val damage = comp.might * 100f
                for (e in engine.getActiveEntities()) {
                    if (e.tag == "enemy") {
                        e.get<HealthComponent>()?.takeDamage(damage)
                    }
                }
                engine.shake(intensity = 15f, duration = 0.5f)
                spawnMeteorEffect(pos.x, pos.y)
            }

            AbilityType.SHADOW_CLONE -> {
                // Create a clone entity
                cloneEntity = engine.createEntity("player")
                cloneEntity?.add(TransformComponent(pos.x + 50f, pos.y))
                cloneEntity?.add(VelocityComponent(speed = 200f))
                cloneEntity?.add(SpriteComponent(
                    width = 28f, height = 28f,
                    color = 0xFF9C27B0.toInt(),
                    alpha = 0.7f,
                    shape = SpriteShape.CIRCLE
                ))
                cloneEntity?.add(HealthComponent(currentHp = 999f, maxHp = 999f))
            }

            AbilityType.HEALING_AURA -> {
                health.heal(health.maxHp * 0.5f)
                health.invincibleTimer = 3f
                spawnHealEffect(pos.x, pos.y)
            }

            AbilityType.LIGHTNING_STORM -> {
                val damage = comp.might * 50f
                for (e in engine.getActiveEntities()) {
                    if (e.tag == "enemy") {
                        e.get<HealthComponent>()?.takeDamage(damage)
                        val ePos = e.get<TransformComponent>()
                        if (ePos != null) spawnLightningEffect(ePos.x, ePos.y)
                    }
                }
                engine.shake(intensity = 10f, duration = 0.3f)
            }

            AbilityType.BERSERKER_RAGE -> {
                comp.might *= 2f
                comp.moveSpeed *= 1.5f
                comp.attackSpeed *= 1.5f
                spawnBerserkerEffect(pos.x, pos.y)
            }

            AbilityType.FROST_NOVA -> {
                val damage = comp.might * 40f
                val nearby = engine.findInRange(pos.x, pos.y, 300f, "enemy")
                for (e in nearby) {
                    e.get<HealthComponent>()?.takeDamage(damage)
                    e.get<EnemyComponent>()?.let {
                        it.slowTimer = 4f
                        it.slowFactor = 0.2f
                    }
                }
                spawnFrostEffect(pos.x, pos.y)
            }

            AbilityType.SOUL_HARVEST -> {
                var killed = 0
                for (e in engine.getActiveEntities()) {
                    if (e.tag == "enemy") {
                        val hp = e.get<HealthComponent>() ?: continue
                        if (hp.currentHp < hp.maxHp * 0.3f) {
                            hp.currentHp = 0f
                            hp.isDead = true
                            killed++
                        }
                    }
                }
                if (killed > 0) spawnSoulEffect(pos.x, pos.y, killed)
            }
        }

        return true
    }

    fun update(dt: Float) {
        val ability = currentAbility ?: return

        if (ability.cooldownTimer > 0f) {
            ability.cooldownTimer -= dt
        }

        if (ability.isActive) {
            ability.activeTimer -= dt
            if (ability.activeTimer <= 0f) {
                ability.isActive = false
                // Deactivate effects
                when (ability.type) {
                    AbilityType.BERSERKER_RAGE -> {
                        // Reset stats (approximate — doesn't track exact base)
                        val player = engine.getActiveEntities().find { it.tag == "player" && it.has<PlayerComponent>() }
                        player?.get<PlayerComponent>()?.let {
                            it.might /= 2f
                            it.moveSpeed /= 1.5f
                            it.attackSpeed /= 1.5f
                        }
                    }
                    AbilityType.SHADOW_CLONE -> {
                        cloneEntity?.active = false
                        cloneEntity = null
                    }
                    else -> {}
                }
            }
        }
    }

    private fun getActiveDuration(type: AbilityType): Float = when (type) {
        AbilityType.TIME_FREEZE -> 3f
        AbilityType.SHADOW_CLONE -> 5f
        AbilityType.BERSERKER_RAGE -> 5f
        else -> 0.1f  // instant abilities
    }

    fun getAbilityState(): AbilityState? = currentAbility
    fun getAbilityName(): String = currentAbility?.type?.displayName ?: ""
    fun getAbilityIcon(): String = currentAbility?.type?.icon ?: ""
    fun isReady(): Boolean = currentAbility?.isReady ?: false
    fun getCooldownProgress(): Float = currentAbility?.cooldownProgress ?: 0f

    // ── Visual Effects ────────────────────────────────────────────
    private fun spawnGlobalEffect(color: Int, duration: Float) {
        val p = engine.createEntity("particle")
        p.add(TransformComponent(0f, 0f))
        p.add(SpriteComponent(width = 2000f, height = 2000f, color = color, alpha = 0.15f))
        p.add(ParticleComponent(lifetime = duration, fadeOut = true))
    }

    private fun spawnMeteorEffect(x: Float, y: Float) {
        val ring = engine.createEntity("particle")
        ring.add(TransformComponent(x, y))
        ring.add(SpriteComponent(width = 500f, height = 500f, color = 0xFFFF5722.toInt(), alpha = 0.4f, shape = SpriteShape.CIRCLE))
        ring.add(ParticleComponent(lifetime = 0.8f, fadeOut = true))
        repeat(12) {
            val angle = it * Math.PI.toFloat() * 2f / 12f
            val spark = engine.createEntity("particle")
            spark.add(TransformComponent(x, y))
            spark.add(VelocityComponent(vx = kotlin.math.cos(angle) * 200f, vy = kotlin.math.sin(angle) * 200f, speed = 1f))
            spark.add(SpriteComponent(width = 8f, height = 8f, color = 0xFFFFAB91.toInt(), alpha = 0.9f))
            spark.add(ParticleComponent(lifetime = 0.6f, fadeOut = true, shrink = true))
        }
    }

    private fun spawnHealEffect(x: Float, y: Float) {
        repeat(8) {
            val angle = it * Math.PI.toFloat() * 2f / 8f
            val p = engine.createEntity("particle")
            p.add(TransformComponent(x, y))
            p.add(VelocityComponent(vx = kotlin.math.cos(angle) * 60f, vy = kotlin.math.sin(angle) * 60f - 80f, speed = 1f))
            p.add(SpriteComponent(width = 6f, height = 6f, color = 0xFF4CAF50.toInt(), alpha = 0.8f))
            p.add(ParticleComponent(lifetime = 1f, fadeOut = true))
        }
    }

    private fun spawnLightningEffect(x: Float, y: Float) {
        val p = engine.createEntity("particle")
        p.add(TransformComponent(x, y))
        p.add(SpriteComponent(width = 20f, height = 60f, color = 0xFFFFEB3B.toInt(), alpha = 0.8f))
        p.add(ParticleComponent(lifetime = 0.2f, fadeOut = true))
    }

    private fun spawnBerserkerEffect(x: Float, y: Float) {
        val p = engine.createEntity("particle")
        p.add(TransformComponent(x, y))
        p.add(SpriteComponent(width = 80f, height = 80f, color = 0xFFFF5722.toInt(), alpha = 0.3f, shape = SpriteShape.CIRCLE))
        p.add(ParticleComponent(lifetime = 5f, fadeOut = true))
    }

    private fun spawnFrostEffect(x: Float, y: Float) {
        val ring = engine.createEntity("particle")
        ring.add(TransformComponent(x, y))
        ring.add(SpriteComponent(width = 600f, height = 600f, color = 0xFF80CBC4.toInt(), alpha = 0.3f, shape = SpriteShape.CIRCLE))
        ring.add(ParticleComponent(lifetime = 0.6f, fadeOut = true))
    }

    private fun spawnSoulEffect(x: Float, y: Float, count: Int) {
        repeat(count.coerceAtMost(10)) {
            val p = engine.createEntity("particle")
            p.add(TransformComponent(x + GameMath.randomRange(-50f, 50f), y + GameMath.randomRange(-50f, 50f)))
            p.add(VelocityComponent(vx = 0f, vy = -100f, speed = 1f))
            p.add(SpriteComponent(width = 10f, height = 10f, color = 0xFFE040FB.toInt(), alpha = 0.8f))
            p.add(ParticleComponent(lifetime = 1f, fadeOut = true))
        }
    }

    fun reset() {
        currentAbility = null
        cloneEntity?.active = false
        cloneEntity = null
    }
}
