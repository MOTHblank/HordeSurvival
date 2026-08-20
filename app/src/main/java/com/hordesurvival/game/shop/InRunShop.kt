package com.hordesurvival.game.shop

import com.hordesurvival.game.component.PlayerComponent
import com.hordesurvival.game.weapon.WeaponType

/**
 * In-Run Shop — appears every 2 minutes during gameplay.
 * Player can buy upgrades with gold earned from kills.
 * Game does NOT pause while shopping — risk vs reward!
 */
class InRunShop {

    data class ShopItem(
        val id: String,
        val name: String,
        val icon: String,
        val description: String,
        val cost: Int,
        val category: Category,
        var purchased: Boolean = false,
        val maxPurchases: Int = 1,
        var purchaseCount: Int = 0
    ) {
        enum class Category { WEAPON, PASSIVE, HEAL, SPECIAL }
    }

    var isVisible = false
    var showTimer = 0f
    var timeSinceLastShop = 0f
    val shopInterval = 120f  // every 2 minutes

    var currentItems = listOf<ShopItem>()
    var playerGold = 0f

    // Track what's been offered to avoid repeats
    private val offeredItems = mutableSetOf<String>()

    fun update(dt: Float, gold: Float): Boolean {
        playerGold = gold
        if (isVisible) return false

        timeSinceLastShop += dt
        if (timeSinceLastShop >= shopInterval) {
            timeSinceLastShop = 0f
            openShop()
            return true  // signal to show UI
        }
        return false
    }

    fun openShop() {
        isVisible = true
        showTimer = 15f  // 15 seconds to buy (game keeps running!)
        currentItems = generateOffers()
    }

    fun closeShop() {
        isVisible = false
        showTimer = 0f
    }

    fun tickTimer(dt: Float): Boolean {
        if (!isVisible) return false
        showTimer -= dt
        if (showTimer <= 0f) {
            closeShop()
            return true  // auto-closed
        }
        return false
    }

    fun tryPurchase(itemId: String): Boolean {
        val item = currentItems.find { it.id == itemId } ?: return false
        if (item.purchased && item.purchaseCount >= item.maxPurchases) return false
        if (playerGold < item.cost) return false

        playerGold -= item.cost
        item.purchaseCount++
        if (item.purchaseCount >= item.maxPurchases) item.purchased = true
        return true
    }

    private fun generateOffers(): List<ShopItem> {
        val offers = mutableListOf<ShopItem>()
        val rng = (0..100).random()

        // Always offer a heal
        offers.add(ShopItem(
            "heal_25", "Quick Heal", "❤️",
            "Restore 25% max HP instantly",
            cost = 15, category = ShopItem.Category.HEAL,
            maxPurchases = 3
        ))

        // Weapon upgrade
        offers.add(ShopItem(
            "weapon_dmg", "Damage Boost", "⚔️",
            "+20% weapon damage for this run",
            cost = 30 + (rng % 20), category = ShopItem.Category.WEAPON,
            maxPurchases = 5
        ))

        // Speed boost
        offers.add(ShopItem(
            "speed_boost", "Speed Surge", "💨",
            "+15% movement speed for this run",
            cost = 25, category = ShopItem.Category.PASSIVE,
            maxPurchases = 3
        ))

        // Magnet (pickup range)
        offers.add(ShopItem(
            "magnet", "Super Magnet", "🧲",
            "+50% pickup range for this run",
            cost = 20, category = ShopItem.Category.PASSIVE,
            maxPurchases = 2
        ))

        // Special items (rotate)
        if (rng > 70) {
            offers.add(ShopItem(
                "shield_bubble", "Shield Bubble", "🛡️",
                "Invincible for 5 seconds",
                cost = 50, category = ShopItem.Category.SPECIAL,
                maxPurchases = 1
            ))
        } else if (rng > 40) {
            offers.add(ShopItem(
                "xp_magnet", "XP Storm", "💎",
                "All XP gems fly to you for 10 seconds",
                cost = 35, category = ShopItem.Category.SPECIAL,
                maxPurchases = 1
            ))
        } else {
            offers.add(ShopItem(
                "rage_mode", "Rage Mode", "🔥",
                "+50% damage for 15 seconds",
                cost = 40, category = ShopItem.Category.SPECIAL,
                maxPurchases = 1
            ))
        }

        return offers
    }

    fun reset() {
        isVisible = false
        showTimer = 0f
        timeSinceLastShop = 0f
        currentItems = emptyList()
        offeredItems.clear()
        playerGold = 0f
    }
}
