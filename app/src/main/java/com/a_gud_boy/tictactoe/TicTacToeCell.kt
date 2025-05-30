package com.a_gud_boy.tictactoe

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Renders a single cell in the Tic Tac Toe grid.
 *
 * @param modifier Modifier to be applied to the cell.
 * @param player The player who has made a move in this cell, or null if the cell is empty.
 * @param isOldMove True if the move in this cell is an old move and should be dimmed, false otherwise.
 * @param iconSize The size of the X or O icon.
 * @param onClick Callback to be invoked when the cell is clicked.
 */
@Composable
fun TicTacToeCell(
    modifier: Modifier = Modifier,
    player: Player?, // Using Player enum from ViewModel
    isOldMove: Boolean,
    iconSize: Dp = 70.dp,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier // This will include layoutId, size, onGloballyPositioned, background, shape, clickable
            .clickable(
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = null // No ripple effect
            ),
        contentAlignment = Alignment.Center
    ) {
        // Determine the tint based on whether the move is old/dimmed.
        val iconTint =
            if (isOldMove) { // isOldMove now represents the "about to disappear" or generally dimmed state
                when (player) {
                    Player.X -> Color.Black.copy(alpha = 0.4f)
                    Player.O -> LocalContentColor.current.copy(alpha = 0.4f) // Ensure O also dims from its default
                    null -> Color.Transparent // Should not be reached if player is non-null for an old move
                }
            } else {
                when (player) {
                    Player.X -> Color.Black
                    Player.O -> LocalContentColor.current
                    null -> Color.Transparent
                }
            }

        when (player) {
            Player.X -> {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Player X move",
                    tint = iconTint, // Apply calculated tint
                    modifier = Modifier
                        .width(iconSize)
                        .height(iconSize)
                )
            }

            Player.O -> {
                Icon(
                    painter = painterResource(R.drawable.player_2),
                    contentDescription = "Player O move",
                    tint = iconTint, // Apply calculated tint
                    modifier = Modifier
                        .width(iconSize)
                        .height(iconSize)
                )
            }

            null -> {
                // Empty cell
            }
        }
    }
}
