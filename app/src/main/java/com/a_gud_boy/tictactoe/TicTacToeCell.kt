package com.a_gud_boy.tictactoe

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
// import androidx.compose.material3.LocalContentColor // No longer needed for O's base color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
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
    // Remember the interaction source to prevent recomposition
    val interactionSource = remember { MutableInteractionSource() }

    // Get colors outside remember block since they're Composable functions
    val playerXColor = colorResource(R.color.red_x_icon)
    val playerOColor = colorResource(R.color.blue_o_icon)

    // Remember the icon tint based on player and isOldMove
    val iconTint = remember(player, isOldMove, playerXColor, playerOColor) {
        if (player == null) {
            Color.Transparent
        } else {
            val baseColor = when (player) {
                Player.X -> playerXColor
                Player.O -> playerOColor
            }
            if (isOldMove) baseColor.copy(alpha = 0.4f) else baseColor
        }
    }

    // Remember the icon modifier to prevent recreation
    val iconModifier = remember(iconSize) {
        Modifier.size(iconSize)
    }

    Box(
        modifier = modifier // This will include layoutId, size, onGloballyPositioned, background, shape, clickable
            .clickable(
                onClick = onClick,
                interactionSource = interactionSource,
                indication = null // No ripple effect
            ),
        contentAlignment = Alignment.Center
    ) {
        when (player) {
            Player.X -> Icon(
                Icons.Default.Close,
                contentDescription = "Player X move",
                tint = iconTint, // Apply calculated tint
                modifier = iconModifier
            )

            Player.O -> Icon(
                painter = painterResource(R.drawable.player_2),
                contentDescription = "Player O move",
                tint = iconTint, // Apply calculated tint
                modifier = iconModifier
            )

            null -> {
                // Empty cell
            }
        }
    }
}
