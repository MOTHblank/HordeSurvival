package com.hordesurvival.game.pet

import com.hordesurvival.game.component.*
import com.hordesurvival.game.engine.GameEngine
import com.hordesurvival.game.engine.ecs.Entity
import com.hordesurvival.utils.GameMath
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Companion Pet System — pets follow the player and attack enemies.
 * Each pet has unique behavior and can be upgraded.
 */
class CompanionPet(private val engine: GameEngine) {

    enum class PetType {
        DRAGON,     // Shoots fireballs at nearest enemy
        FAIRY,      // Heals player periodically
        WOLF,       // Charges at enemies for melee damage
        OWL,        // Increases XP gain passively
        PHOENIX     // Revives player once per run
    }

    data class PetState(
        val type: PetType,
        var level: Int = 1,
        var followAngle: Float = 0f,
        var attackTimer: Float = 0f,
        var healTimer: Float = 0f,
        var chargeTimer: Float = 0f,
        var isCharging: Boolean = false,
        var chargeTarget: Entity? = null
    )

    private val activePets = mutableListOf<PetState>()
    private val petEntities = mutableMapOf<PetType, Entity>()

    companion object {
        const val FOLLOW_DISTANCE = 60f
        const val FOLLOW_SPEED = 250f
        const val DRAGON_ATTACK_INTERVAL = 2f
        const val FAIRY_HEAL_INTERVAL = 5f
        const val WOLF_CHARGE_INTERVAL = 3f
        const val WOLF_CHARGE_DAMAGE = 20f
        const val WOLF_CHARGE_SPEED = 600f
    }

    fun addPet(type: PetType) {
        if (activePets.any { it.type == type }) return
        val state = PetState(type = type, followAngle = activePets.size * 2.094f) // 120° apart
        activePets.add(state)
        spawnPetEntity(state)
    }

    fun update(dt: Float, playerPos: TransformComponent, playerComp: PlayerComponent) {
        for (pet in activePets) {
            val entity = petEntities[pet.type] ?: continue
            val petTransform = entity.get<TransformComponent>() ?: continue

            // Follow player
            val targetX = playerPos.x + cos(pet.followAngle.toDouble()).toFloat() * FOLLOW_DISTANCE
            val targetY = playerPos.y + sin(pet.followAngle.toDouble()).toFloat() * FOLLOW_DISTANCE
            val dx = targetX - petTransform.x
            val dy = targetY - petTransform.y
            val dist = sqrt(dx * dx + dy * dy)
            if (dist > 5f) {
                petTransform.x += (dx / dist) * FOLLOW_SPEED * dt
                petTransform.y += (dy / dist) * FOLLOW_SPEED * dt
            }

            // Rotate follow angle slowly
            pet.followAngle += dt * 0.5f

            // Pet-specific behavior
            when (pet.type) {
                PetType.DRAGON -> updateDragon(pet, petTransform, playerComp, dt)
                PetType.FAIRY -> updateFairy(pet, playerPos, playerComp, dt)
                PetType.WOLF -> updateWolf(pet, petTransform, playerComp, dt)
                PetType.OWL -> { /* Passive XP bonus handled in applyPetBonuses */ }
                PetType.PHOENIX -> { /* Revive handled in onPlayerDeath */ }
            }
        }
    }

    private fun updateDragon(pet: PetState, pos: TransformComponent, player: PlayerComponent, dt: Float) {
        pet.attackTimer += dt
        if (pet.attackTimer >= DRAGON_ATTACK_INTERVAL / pet.level) {
            pet.attackTimer = 0f
            val target = engine.findNearest(pos.x, pos.y, "enemy", 300f)
            if (target != null) {
                val tPos = target.get<TransformComponent>() ?: return
                val angle = GameMath.angleTo(pos.x, pos.y, tPos.x, tPos.y)
                val proj = engine.createEntity("projectile")
                proj.add(TransformComponent(pos.x, pos.y))
                proj.add(VelocityComponent(cos(angle), sin(angle), 400f))
                proj.add(ProjectileComponent(
                    damage = 8f * player.might * pet.level,
                    lifetime = 2f,
                    weaponType = com.hordesurvival.game.weapon.WeaponType.FIREBALL
                ))
                proj.add(SpriteComponent(width = 8f, height = 8f, color = 0xFFFF7043.toInt()))
                proj.add(CollisionComponent(radius = 6f, isTrigger = true))
            }
        }
    }

    private fun updateFairy(pet: PetState, playerPos: TransformComponent, player: PlayerComponent, dt: Float) {
        pet.healTimer += dt
        if (pet.healTimer >= FAIRY_HEAL_INTERVAL) {
            pet.healTimer = 0f
            val playerEntity = engine.getActiveEntities().find { it.tag == "player" && it.has<HealthComponent>() }
            playerEntity?.get<HealthComponent>()?.heal(5f * pet.level)
        }
    }

    private fun updateWolf(pet: PetState, pos: TransformComponent, player: PlayerComponent, dt: Float) {
        pet.chargeTimer += dt
        if (pet.chargeTimer >= WOLF_CHARGE_INTERVAL && !pet.isCharging) {
            val target = engine.findNearest(pos.x, pos.y, "enemy", 200f)
            if (target != null) {
                pet.isCharging = true
                pet.chargeTarget = target
                pet.chargeTimer = 0f
            }
        }
        if (pet.isCharging) {
            val target = pet.chargeTarget
            if (target != null && target.active) {
                val tPos = target.get<TransformComponent>()
                if (tPos != null) {
                    val dx = tPos.x - pos.x
                    val dy = tPos.y - pos.y
                    val dist = sqrt(dx * dx + dy * dy)
                    if (dist > 10f) {
                        pos.x += (dx / dist) * WOLF_CHARGE_SPEED * dt
                        pos.y += (dy / dist) * WOLF_CHARGE_SPEED * dt
                    } else {
                        // Hit!
                        target.get<HealthComponent>()?.takeDamage(WOLF_CHARGE_DAMAGE * player.might * pet.level)
                        pet.isCharging = false
                        pet.chargeTarget = null
                    }
                }
            } else {
                pet.isCharging = false
                pet.chargeTarget = null
            }
        }
    }

    private fun spawnPetEntity(state: PetState) {
        val entity = engine.createEntity("pet")
        entity.add(TransformComponent(0f, 0f))
        val (color, size) = when (state.type) {
            PetType.DRAGON -> 0xFFFF7043.toInt() to 14f
            PetType.FAIRY -> 0xFFCE93D8.toInt() to 10f
            PetType.WOLF -> 0xFF8D6E63.toInt() to 16f
            PetType.OWL -> 0xFFFFDAC1.toInt() to 12f
            PetType.PHOENIX -> 0xFFFFAB91.toInt() to 14f
        }
        entity.add(SpriteComponent(width = size, height = size, color = color, shape = com.hordesurvival.game.component.SpriteShape.CIRCLE))
        petEntities[state.type] = entity
    }

    fun applyBonuses(player: PlayerComponent) {
        for (pet in activePets) {
            when (pet.type) {
                PetType.OWL -> player.xpGain += 0.1f * pet.level
                else -> {}
            }
        }
    }

    fun hasPhoenix(): Boolean = activePets.any { it.type == PetType.PHOENIX }

    fun getActivePets(): List<PetState> = activePets.toList()

    fun reset() {
        activePets.clear()
        petEntities.clear()
    }
}
