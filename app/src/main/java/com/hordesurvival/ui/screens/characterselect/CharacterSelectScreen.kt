package com.hordesurvival.ui.screens.characterselect

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.hordesurvival.data.model.UnlockedCharacter
import com.hordesurvival.ui.theme.HordeColors
import com.hordesurvival.ui.components.HordeScreen
import com.hordesurvival.ui.components.HordeButton
import com.hordesurvival.ui.components.HordeCard

/**
 * Character selection screen with grid of characters.
 */
@Composable
fun CharacterSelectScreen(
    characters: List<UnlockedCharacter>,
    selectedId: Int,
    onSelect: (UnlockedCharacter) -> Unit,
    onConfirm: () -> Unit,
    onBack: () -> Unit,
    onUnlock: (Int, Int) -> Unit,  // characterId, cost
    gold: Int,
    languageCode: String = "en"
) {
    HordeScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Choose Your Hero",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = HordeColors.Lavender
            )

            Spacer(modifier = Modifier.height(24.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(characters) { char ->
                    CharacterCard(
                        character = char,
                        isSelected = char.characterId == selectedId,
                        onClick = {
                            if (char.isUnlocked) onSelect(char)
                        },
                        onUnlock = { id, cost -> onUnlock(id, cost) },
                        gold = gold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HordeButton(text = "← Back", onClick = onBack, color = HordeColors.TextSecondary, isSecondary = true)

                HordeButton(text = "Confirm", onClick = onConfirm, color = HordeColors.SkyBlue)
            }
        }
    }
}

@Composable
private fun CharacterCard(
    character: UnlockedCharacter,
    isSelected: Boolean,
    onClick: () -> Unit,
    onUnlock: (Int, Int) -> Unit,
    gold: Int
) {
    val borderColor = if (isSelected) HordeColors.SkyBlue else Color.Transparent
    val alpha = if (character.isUnlocked) 1f else 0.5f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(HordeColors.CardBg.copy(alpha = alpha))
            .border(2.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(enabled = character.isUnlocked) { onClick() }
            .padding(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // Character icon placeholder
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(HordeColors.Lavender.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getCharacterEmoji(character.characterId),
                    fontSize = 28.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = character.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (character.isUnlocked) Color.White else HordeColors.TextSecondary
            )

            Text(
                text = character.description,
                fontSize = 10.sp,
                color = HordeColors.TextSecondary,
                textAlign = TextAlign.Center,
                maxLines = 2
            )

            if (!character.isUnlocked) {
                Spacer(modifier = Modifier.height(8.dp))
                HordeButton(
                    text = "🔒 Unlock (500💰)",
                    onClick = { onUnlock(character.characterId, 500) },
                    enabled = gold >= 500,
                    modifier = Modifier.height(34.dp)
                )
            }
        }
    }
}

private fun getCharacterEmoji(id: Int): String = when (id) {
    0 -> "🧙"; 1 -> "🛡️"; 2 -> "🗡️"; 3 -> "⚗️"; 4 -> "🔮"
    5 -> "🔥"; 6 -> "❄️"; 7 -> "⚡"; 8 -> "🗡️"; 9 -> "💀"
    else -> "👤"
}
