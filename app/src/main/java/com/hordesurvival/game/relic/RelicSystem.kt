package com.hordesurvival.game.relic

import com.hordesurvival.game.component.*
import com.hordesurvival.game.engine.GameEngine
import com.hordesurvival.utils.GameMath

/**
 * Relic System — permanent passive items that spawn on the map.
 * Pick up relics to gain powerful bonuses that last the entire run.
 */
class RelicSystem(private val engine: GameEngine) {

    enum class RelicType(
        val displayName: String,
        val description: String,
        val icon: String,
        val color: Long,
        val rarity: RelicRarity
    ) {
        CROWN("Crown of Wisdom", "+25% XP gain", "👑", 0xFFFFD700, RelicRarity.COMMON),
        WINGS("Swift Wings", "+20% move speed", "🪽", 0xFF81D4FA, RelicRarity.COMMON),
        ARMOR("Iron Armor", "+8 armor", "🛡️", 0xFF90A4AE, RelicRarity.COMMON),
        CLOVER("Golden Clover", "+15% luck", "🍀", 0xFF66BB6A, RelicRarity.UNCOMMON),
        RING("Power Ring", "+15% might", "💍", 0xFFCE93D8, RelicRarity.UNCOMMON),
        AMULET("Life Amulet", "+1 HP/sec regen", "📿", 0xFFEF5350, RelicRarity.UNCOMMON),
        CHALICE("Holy Chalice", "+30% max HP", "🏆", 0xFFFFAB91, RelicRarity.RARE),
        GAUNTLET("War Gauntlet", "+2 projectiles", "🧤", 0xFFFF7043, RelicRarity.RARE),
        ORACLE("Oracle Eye", "See enemy HP bars always", "🔮", 0xFF7E57C2, RelicRarity.RARE),
        PHOENIX_FEATHER("Phoenix Feather", "Revive once with 50% HP", "🪶", 0xFFFF5722, RelicRarity.LEGENDARY),
        TIME_GLASS("Time Glass", "+50% cooldown reduction", "⏳", 0xFF00BCD4, RelicRarity.LEGENDARY),
        VOID_HEART("Void Heart", "Invincibility after kill (0.5s)", "🖤", 0xFF212121, RelicRarity.LEGENDARY)
    }

    enum class RelicRarity(val weight: Float, val color: Long) {
        COMMON(50f, 0xFF9E9E9E),
        UNCOMMON(30f, 0xFF4CAF50),
        RARE(15f, 0xFF2196F3),
        LEGENDARY(5f, 0xFFFF9800)
    }

    data class ActiveRelic(
        val type: RelicType,
        var timer: Float = 0f  // for Void Heart invincibility
    )

    private val activeRelics = mutableListOf<ActiveRelic>()
    private var spawnTimer = 0f
    private val spawnInterval = 45f  // spawn a relic every 45 seconds
    private val maxRelicsOnMap = 3

    fun update(dt: Float, playerPos: TransformComponent, playerComp: PlayerComponent, playerHealth: HealthComponent) {
        spawnTimer += dt
        val activeEntities = engine.getActiveEntities()
        var relicsOnMap = 0
        for (i in 0 until activeEntities.size) {
            if (activeEntities[i].tag == "relic") {
                relicsOnMap++
            }
        }

        if (spawnTimer >= spawnInterval && relicsOnMap < maxRelicsOnMap) {
            spawnTimer = 0f
            spawnRelicOnMap(playerPos)
        }

        // Update active relic timers
        for (relic in activeRelics) {
            if (relic.type == RelicType.VOID_HEART) {
                relic.timer = (relic.timer - dt).coerceAtLeast(0f)
            }
        }
    }

    private fun spawnRelicOnMap(playerPos: TransformComponent) {
        val relicType = rollRelicType()
        val angle = GameMath.randomRange(0f, Math.PI.toFloat() * 2f)
        val dist = GameMath.randomRange(150f, 400f)
        val x = playerPos.x + kotlin.math.cos(angle) * dist
        val y = playerPos.y + kotlin.math.sin(angle) * dist

        val entity = engine.createEntity("relic")
        entity.add(TransformComponent(x, y))
        entity.add(SpriteComponent(
            width = 24f, height = 24f,
            color = relicType.color.toInt(),
            shape = SpriteShape.DIAMOND,
            layer = 3
        ))
        entity.add(RelicComponent(
            relicType = com.hordesurvival.game.component.RelicType.CROWN,  // reuse existing
            lifetime = 120f
        ))
        // Store relic type in a tag for pickup detection
        entity.tag = "relic"
    }

    private fun rollRelicType(): RelicType {
        val totalWeight = RelicType.entries.sumOf { it.rarity.weight.toDouble() }.toFloat()
        var roll = GameMath.randomRange(0f, totalWeight)
        for (type in RelicType.entries) {
            roll -= type.rarity.weight
            if (roll <= 0f) return type
        }
        return RelicType.CROWN
    }

    /** Called when player picks up a relic */
    fun onRelicPickedUp(type: RelicType, playerComp: PlayerComponent, playerHealth: HealthComponent) {
        activeRelics.add(ActiveRelic(type))
        applyRelicEffect(type, playerComp, playerHealth)
    }

    private fun applyRelicEffect(type: RelicType, comp: PlayerComponent, hp: HealthComponent) {
        when (type) {
            RelicType.CROWN -> comp.xpGain += 0.25f
            RelicType.WINGS -> comp.moveSpeed += 0.2f
            RelicType.ARMOR -> hp.armor += 8f
            RelicType.CLOVER -> comp.luck += 0.15f
            RelicType.RING -> comp.might += 0.15f
            RelicType.AMULET -> comp.regenRate += 1f
            RelicType.CHALICE -> {
                hp.maxHp *= 1.3f
                hp.heal(hp.maxHp * 0.3f)
            }
            RelicType.GAUNTLET -> comp.projectileBonus += 2
            RelicType.ORACLE -> {} // handled in renderer
            RelicType.PHOENIX_FEATHER -> {} // handled in death check
            RelicType.TIME_GLASS -> comp.cooldownReduction = (comp.cooldownReduction + 0.5f).coerceAtMost(0.8f)
            RelicType.VOID_HEART -> {} // handled in collision
        }
    }

    fun hasRelic(type: RelicType): Boolean {
        for (i in 0 until activeRelics.size) {
            if (activeRelics[i].type == type) return true
        }
        return false
    }

    fun isVoidHeartActive(): Boolean {
        for (i in 0 until activeRelics.size) {
            val relic = activeRelics[i]
            if (relic.type == RelicType.VOID_HEART) {
                return relic.timer > 0f
            }
        }
        return false
    }

    fun triggerVoidHeart() {
        for (i in 0 until activeRelics.size) {
            val relic = activeRelics[i]
            if (relic.type == RelicType.VOID_HEART) {
                relic.timer = 0.5f
                break
            }
        }
    }

    fun getActiveRelics(): List<ActiveRelic> = activeRelics.toList()

    fun reset() {
        activeRelics.clear()
        spawnTimer = 0f
    }
}
