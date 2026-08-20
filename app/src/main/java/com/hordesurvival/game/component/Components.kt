package com.hordesurvival.game.component

import com.hordesurvival.game.engine.ecs.Component
import com.hordesurvival.game.enemy.EnemyType
import com.hordesurvival.game.weapon.WeaponType
import com.hordesurvival.utils.Constants

// ── Transform ───────────────────────────────────────────────────────────
data class TransformComponent(
    var x: Float = 0f,
    var y: Float = 0f,
    var rotation: Float = 0f,
    var scaleX: Float = 1f,
    var scaleY: Float = 1f
) : Component

// ── Movement ────────────────────────────────────────────────────────────
data class VelocityComponent(
    var vx: Float = 0f,
    var vy: Float = 0f,
    var speed: Float = 200f
) : Component

// ── Health ──────────────────────────────────────────────────────────────
data class HealthComponent(
    var currentHp: Float = 100f,
    var maxHp: Float = 100f,
    var armor: Float = 0f,
    var invincibleTimer: Float = 0f,
    var isDead: Boolean = false
) : Component {
    fun takeDamage(amount: Float) {
        if (invincibleTimer > 0f || isDead) return
        // Minimum 1 damage even with high armor — prevents invincibility
        val reduced = (amount - armor).coerceAtLeast(1f)
        currentHp -= reduced
        if (currentHp <= 0f) {
            currentHp = 0f
            isDead = true
        }
    }

    fun heal(amount: Float) {
        if (isDead) return
        currentHp = (currentHp + amount).coerceAtMost(maxHp)
    }
}

// ── Player ──────────────────────────────────────────────────────────────
data class PlayerComponent(
    var level: Int = 1,
    var currentXp: Float = 0f,
    var xpToNext: Float = 10f,
    var gold: Float = 0f,
    var totalKills: Int = 0,
    var might: Float = 1f,
    var area: Float = 1f,
    var cooldownReduction: Float = 0f,
    var projectileBonus: Int = 0,
    var luck: Float = 0f,
    var xpGain: Float = 1f,
    var pickupRange: Float = 50f,
    var moveSpeed: Float = 1f,
    var attackSpeed: Float = 1f,
    var regenRate: Float = 0f,
    var goldGainBonus: Float = 0f,  // blessing fortune bonus
    val weapons: MutableList<WeaponType> = mutableListOf(),
    val passiveLevels: MutableMap<String, Int> = mutableMapOf()
) : Component {
    /**
     * Add XP. Does NOT auto-level — just accumulates.
     * Level-up is handled by GameViewModel via shouldLevelUp().
     */
    fun addXp(amount: Float) {
        currentXp += amount * xpGain
    }

    /** Returns true when enough XP to level up */
    fun shouldLevelUp(): Boolean = currentXp >= xpToNext && level < Constants.MAX_LEVEL

    fun calculateXpForLevel(lvl: Int): Float {
        return Constants.XP_BASE_REQUIREMENT *
                Math.pow(Constants.XP_GROWTH_RATE.toDouble(), (lvl - 1).toDouble()).toFloat()
    }
}

// ── Sprite / Render ─────────────────────────────────────────────────────
data class SpriteComponent(
    var width: Float = 32f,
    var height: Float = 32f,
    var color: Int = 0xFFFFFFFF.toInt(),
    var alpha: Float = 1f,
    var layer: Int = 0,
    var shape: SpriteShape = SpriteShape.CIRCLE,
    var scaleX: Float = 1f,
    var scaleY: Float = 1f
) : Component

enum class SpriteShape {
    CIRCLE, RECT, TRIANGLE, DIAMOND, STAR
}

// ── Enemy ───────────────────────────────────────────────────────────────
data class EnemyComponent(
    var type: EnemyType = EnemyType.BASIC_DRONE,
    var damage: Float = 10f,
    var xpValue: Float = 1f,
    var goldValue: Float = 1f,
    var contactCooldown: Float = 0f,
    var contactTimer: Float = 0f,
    var shootTimer: Float = 0f,
    var slowTimer: Float = 0f,
    var slowFactor: Float = 1f,
    var burnTimer: Float = 0f,
    var burnDamage: Float = 0f,
    var phaseTimer: Float = 0f,
    var healCooldown: Float = 0f,
    var healTimer: Float = 0f,
    var splitOnDeath: Boolean = false,
    var splitCount: Int = 0,
    var isBoss: Boolean = false,
    var bossPhase: Int = 0
) : Component

// ── Projectile ──────────────────────────────────────────────────────────
data class ProjectileComponent(
    var damage: Float = 10f,
    var speed: Float = 400f,
    var lifetime: Float = 3f,
    var timer: Float = 0f,
    var pierceCount: Int = 0,
    var pierceRemaining: Int = 0,
    var isHoming: Boolean = false,
    var homingStrength: Float = 2f,
    var targetId: Int = -1,
    var aoeRadius: Float = 0f,
    var burnDamage: Float = 0f,
    var burnDuration: Float = 0f,
    var slowFactor: Float = 1f,
    var slowDuration: Float = 0f,
    var returnsToPlayer: Boolean = false,
    var returnSpeed: Float = 300f,
    var maxDistance: Float = 200f,
    var distanceTraveled: Float = 0f,
    var weaponType: WeaponType = WeaponType.MAGIC_MISSILE
) : Component

// ── Weapon ──────────────────────────────────────────────────────────────
data class WeaponStateComponent(
    var type: WeaponType = WeaponType.MAGIC_MISSILE,
    var tier: Int = 1,
    var cooldownTimer: Float = 0f,
    var baseCooldown: Float = 1f,
    var baseDamage: Float = 10f,
    var projectileCount: Int = 1,
    var area: Float = 1f,
    var specialEffect: String = ""
) : Component

// ── XP Gem ──────────────────────────────────────────────────────────────
data class XpGemComponent(
    var value: Float = 1f,
    var lifetime: Float = 30f,
    var magnetized: Boolean = false
) : Component

// ── Particle ────────────────────────────────────────────────────────────
data class ParticleComponent(
    var lifetime: Float = 1f,
    var timer: Float = 0f,
    var startAlpha: Float = 1f,
    var startWidth: Float = 0f,     // for linear shrink
    var startHeight: Float = 0f,
    var fadeOut: Boolean = true,
    var shrink: Boolean = false
) : Component

// ── Orbit ───────────────────────────────────────────────────────────────
data class OrbitComponent(
    var centerX: Float = 0f,
    var centerY: Float = 0f,
    var radius: Float = 60f,
    var angle: Float = 0f,
    var angularSpeed: Float = 2f,
    var shieldHp: Float = 50f
) : Component

// ── Collision ───────────────────────────────────────────────────────────
data class CollisionComponent(
    var radius: Float = 16f,
    var isTrigger: Boolean = false
) : Component

// ── Poison Cloud ────────────────────────────────────────────────────────
data class PoisonCloudComponent(
    var damagePerTick: Float = 5f,
    var tickInterval: Float = 0.5f,
    var tickTimer: Float = 0f,
    var lifetime: Float = 5f,
    var timer: Float = 0f,
    var radius: Float = 60f
) : Component

// ── Combo ──────────────────────────────────────────────────────────────
data class ComboComponent(
    var count: Int = 0,
    var timer: Float = 0f,
    var maxCombo: Int = 0,
    var comboMultiplier: Float = 1f
) : Component {
    companion object {
        const val COMBO_TIMEOUT = 2.0f  // seconds before combo resets
        const val MULTIPLIER_BONUS = 0.1f  // +10% XP per combo level
    }

    fun addKill() {
        count++
        timer = COMBO_TIMEOUT
        if (count > maxCombo) maxCombo = count
        comboMultiplier = 1f + (count / 5) * MULTIPLIER_BONUS
    }

    fun update(dt: Float) {
        if (count > 0) {
            timer -= dt
            if (timer <= 0f) {
                count = 0
                comboMultiplier = 1f
            }
        }
    }
}

// ── Damage Number ────────────────────────────────────────────────────
data class DamageNumberComponent(
    var amount: Float = 0f,
    var lifetime: Float = 0.8f,
    var timer: Float = 0f,
    var isCrit: Boolean = false,
    var vy: Float = -120f,  // float upward
    // Cached display string — avoids String.format every frame
    var cachedText: String = "",
    var cachedAmount: Float = -1f
) : Component {
    fun getDisplayText(): String {
        if (amount != cachedAmount) {
            cachedAmount = amount
            cachedText = when {
                amount >= 1000 -> "${(amount / 1000).toInt()}k"
                amount >= 100 -> "${amount.toInt()}"
                else -> String.format("%.1f", amount)
            }
        }
        return cachedText
    }
}

// ── Lightning Ring ──────────────────────────────────────────────────────
data class LightningRingComponent(
    var damage: Float = 15f,
    var radius: Float = 80f,
    var tickInterval: Float = 1f,
    var tickTimer: Float = 0f,
    var flashTimer: Float = 0f
) : Component

// ── Loot Box ────────────────────────────────────────────────────────────
enum class LootType { HEALTH, GOLD, MAGNET, DAMAGE_BOOST }

data class LootBoxComponent(
    var lootType: LootType = LootType.HEALTH,
    var value: Float = 20f,
    var lifetime: Float = 20f,
    var timer: Float = 0f,
    var bobPhase: Float = 0f
) : Component

// ── Damage Boost Timer ─────────────────────────────────────────────────
data class DamageBoostComponent(
    var duration: Float = 10f,
    var timer: Float = 0f,
    var mightMultiplier: Float = 1.5f,
    var baseMight: Float = 1f  // player's might when boost was applied
) : Component

// ── Elite Ability ───────────────────────────────────────────────────────
enum class EliteAbility { NONE, TELEPORT, SHIELDED, EXPLODE_ON_DEATH }

data class EliteComponent(
    var ability: EliteAbility = EliteAbility.NONE,
    var teleportTimer: Float = 0f,
    var teleportCooldown: Float = 3f,
    var shieldHp: Float = 0f,
    var shieldMaxHp: Float = 0f,
    var shieldActive: Boolean = false,
    var hasExploded: Boolean = false
) : Component

// ── Relic (passive item on map) ─────────────────────────────────────────
enum class RelicType {
    CROWN,      // +20% XP gain
    WINGS,      // +15% speed
    ARMOR,      // +5 armor
    CLOVER,     // +10% luck
    RING,       // +10% might
    AMULET      // +0.5 HP regen
}

data class RelicComponent(
    var relicType: RelicType = RelicType.CROWN,
    var lifetime: Float = 60f,
    var timer: Float = 0f
) : Component

// ── Achievement ─────────────────────────────────────────────────────────
enum class AchievementType {
    FIRST_KILL, KILL_100, KILL_1000,
    SURVIVE_5MIN, SURVIVE_10MIN, SURVIVE_30MIN,
    REACH_LEVEL_10, REACH_LEVEL_25, REACH_LEVEL_50,
    KILL_FIRST_BOSS,
    COMBO_25, COMBO_50,
    COLLECT_ALL_WEAPONS,
    USE_EVOLUTION
}

data class AchievementState(
    val type: AchievementType,
    val name: String,
    val description: String,
    var unlocked: Boolean = false
)

// ── Turret Projectile ───────────────────────────────────────────────────
data class TurretProjectileComponent(
    var damage: Float = 8f,
    var speed: Float = 200f,
    var lifetime: Float = 3f,
    var timer: Float = 0f
) : Component
