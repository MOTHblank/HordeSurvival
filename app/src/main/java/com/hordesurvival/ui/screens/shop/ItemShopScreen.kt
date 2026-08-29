package com.hordesurvival.ui.screens.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hordesurvival.ui.Locales
import com.hordesurvival.ui.components.HordeBackButton
import com.hordesurvival.ui.components.HordeHeader
import com.hordesurvival.ui.components.HordeItemCard
import com.hordesurvival.ui.components.HordeScreen
import com.hordesurvival.ui.components.SmallCutShape
import com.hordesurvival.ui.theme.HordeColors
import com.hordesurvival.ui.theme.HordeTypography

/**
 * Item shop between runs — spend gold on permanent upgrades.
 * Categories: Weapons (starting), Passives, Skins.
 * Stylized arcade theme with custom header and tabs.
 */
@Composable
fun ItemShopScreen(
    gold: Int,
    languageCode: String = "en",
    onPurchase: (String, Int) -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("⚔️ Weap", "🛡️ Pass", "🎨 Skins")

    HordeScreen(contentAlignment = Alignment.TopCenter) {
        Column(Modifier.widthIn(max = 600.dp).fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(16.dp))

            HordeHeader(
                title = "ARMORY SHOP",
                subtitle = "Unlock starting gear, passives & skins",
                icon = "🛒",
                accentColor = HordeColors.WarmPeach
            )

            Spacer(Modifier.height(12.dp))

            Box(
                Modifier
                    .clip(SmallCutShape)
                    .background(HordeColors.GoldColor.copy(alpha = 0.12f))
                    .border(1.dp, HordeColors.GoldColor.copy(alpha = 0.25f), SmallCutShape)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    "💰 $gold Gold",
                    style = HordeTypography.Value.copy(color = HordeColors.GoldColor, fontWeight = FontWeight.Black)
                )
            }

            Spacer(Modifier.height(20.dp))

            // Tab row
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tabs.forEachIndexed { i, label ->
                    val sel = selectedTab == i
                    HordeItemCard(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 2.dp),
                        onClick = { selectedTab = i },
                        selected = sel
                    ) {
                        Text(
                            label,
                            style = HordeTypography.Body.copy(
                                color = if (sel) HordeColors.SkyBlue else HordeColors.TextSecondary,
                                fontWeight = if (sel) FontWeight.Black else FontWeight.Normal
                            )
                        )
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
            HordeBackButton(
                text = "← ${Locales.getString("back", languageCode)}",
                onClick = onBack
            )
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
            HordeItemCard(
                onClick = if (canAfford) { { onPurchase(item.id, item.cost) } } else null
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            item.name,
                            style = HordeTypography.Body.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (canAfford) Color.White else Color.White.copy(alpha = 0.4f)
                            )
                        )
                        Text(
                            item.description,
                            style = HordeTypography.Label.copy(
                                fontSize = 11.sp,
                                color = HordeColors.TextSecondary
                            )
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Box(
                        Modifier
                            .clip(SmallCutShape)
                            .background(if (canAfford) HordeColors.GoldColor.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.1f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "💰 ${item.cost}",
                            style = HordeTypography.Label.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (canAfford) HordeColors.GoldColor else Color.Gray
                            )
                        )
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
