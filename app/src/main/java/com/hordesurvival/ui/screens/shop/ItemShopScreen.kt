package com.hordesurvival.ui.screens.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hordesurvival.ui.theme.HordeColors
import com.hordesurvival.ui.components.HordeScreen
import com.hordesurvival.ui.components.HordeButton
import com.hordesurvival.ui.components.HordeCard

/**
 * Item shop between runs — spend gold on permanent upgrades.
 * Categories: Characters, Weapons (starting), Passives, Skins (future).
 */
@Composable
fun ItemShopScreen(
    gold: Int,
    onPurchase: (String, Int) -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("⚔️ Weapons", "🛡️ Passives", "🎨 Skins")

    HordeScreen {
        Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            // Header
            Text("🛒 Item Shop", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = HordeColors.WarmPeach)
            Spacer(Modifier.height(8.dp))
            Box(Modifier.clip(RoundedCornerShape(20.dp)).background(Color(0xFFFFD700).copy(alpha = 0.1f))
                .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 6.dp)) {
                Text("💰 $gold", fontSize = 18.sp, color = HordeColors.GoldColor, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(20.dp))

            // Tab row
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tabs.forEachIndexed { i, label ->
                    val sel = selectedTab == i
                    Box(
                        Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                            .background(if (sel) HordeColors.SkyBlue.copy(alpha = 0.2f) else HordeColors.DarkCard)
                            .clickable { selectedTab = i }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, color = if (sel) HordeColors.SkyBlue else HordeColors.TextSecondary,
                            fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Shop items
            when (selectedTab) {
                0 -> WeaponShopItems(gold, onPurchase)
                1 -> PassiveShopItems(gold, onPurchase)
                2 -> SkinShopItems(gold)
            }

            Spacer(Modifier.weight(1f))
            HordeButton(text = "← Back", onClick = onBack, color = HordeColors.TextSecondary, isSecondary = true)
        }
    }
}

@Composable
private fun WeaponShopItems(gold: Int, onPurchase: (String, Int) -> Unit) {
    val weapons = listOf(
        ShopItem("start_fireball", "🔥 Fireball", "Start with Fireball weapon", 200),
        ShopItem("start_ice", "❄️ Ice Shard", "Start with Ice Shard weapon", 200),
        ShopItem("start_lightning", "⚡ Lightning Ring", "Start with Lightning Ring", 300),
        ShopItem("start_poison", "☠️ Poison Cloud", "Start with Poison Cloud", 300),
        ShopItem("start_boomerang", "🗡️ Boomerang", "Start with Boomerang Dagger", 250),
        ShopItem("start_spear", "🔱 Divine Spear", "Start with Divine Spear", 400),
    )
    ShopItemList(weapons, gold, onPurchase)
}

@Composable
private fun PassiveShopItems(gold: Int, onPurchase: (String, Int) -> Unit) {
    val passives = listOf(
        ShopItem("perm_regen", "❤️ Regen +0.5/s", "Permanent HP regeneration", 150),
        ShopItem("perm_might", "⚔️ Might +5%", "Permanent damage boost", 200),
        ShopItem("perm_speed", "💨 Speed +5%", "Permanent move speed", 150),
        ShopItem("perm_luck", "🍀 Luck +3%", "Better loot chances", 180),
        ShopItem("perm_armor", "🛡️ Armor +2", "Permanent damage reduction", 250),
        ShopItem("perm_pickup", "🧲 Pickup +20%", "Larger pickup range", 120),
    )
    ShopItemList(passives, gold, onPurchase)
}

@Composable
private fun SkinShopItems(gold: Int, onPurchase: (String, Int) -> Unit = { _, _ -> }) {
    val skins = listOf(
        ShopItem("skin_fire", "🔥 Fire Mage", "Red/orange theme", 100),
        ShopItem("skin_ice", "❄️ Frost Knight", "Blue/white theme", 100),
        ShopItem("skin_shadow", "🌑 Shadow", "Dark purple theme", 150),
        ShopItem("skin_gold", "✨ Golden", "Gold/yellow theme", 200),
        ShopItem("skin_nature", "🌿 Nature", "Green/brown theme", 120),
        ShopItem("skin_void", "🕳️ Void", "Black/dark blue theme", 250),
    )
    ShopItemList(skins, gold, onPurchase)
}

@Composable
private fun ShopItemList(items: List<ShopItem>, gold: Int, onPurchase: (String, Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { item ->
            val canAfford = gold >= item.cost
            HordeCard(
                modifier = Modifier.fillMaxWidth().then(if (canAfford) Modifier.clickable { onPurchase(item.id, item.cost) } else Modifier)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(item.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (canAfford) Color.White else Color.White.copy(alpha = 0.4f))
                        Text(item.description, fontSize = 11.sp, color = HordeColors.TextSecondary)
                    }
                    Spacer(Modifier.width(12.dp))
                    Box(
                        Modifier.clip(RoundedCornerShape(8.dp))
                            .background(if (canAfford) Color(0xFFFFD700).copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.1f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("💰 ${item.cost}", fontSize = 13.sp, fontWeight = FontWeight.Bold,
                            color = if (canAfford) HordeColors.GoldColor else Color.Gray)
                    }
                }
            }
        }
    }
}

private data class ShopItem(
    val id: String,
    val name: String,
    val description: String,
    val cost: Int
)
