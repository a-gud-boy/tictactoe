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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
 * @param buttonId The ID of the button, used for accessibility.
 * @param onClick Callback to be invoked when the cell is clicked.
 */
@Composable
fun TicTacToeCell(
    modifier: Modifier = Modifier,
    player: Player?, // Using Player enum from ViewModel
    isOldMove: Boolean,
    iconSize: Dp = 70.dp,
    buttonId: String, // Added for content description
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

    val positionDescription = remember(buttonId) {
        when (buttonId) {
            "button1" -> "Top-Left cell"
            "button2" -> "Top-Center cell"
            "button3" -> "Top-Right cell"
            "button4" -> "Middle-Left cell"
            "button5" -> "Middle-Center cell"
            "button6" -> "Middle-Right cell"
            "button7" -> "Bottom-Left cell"
            "button8" -> "Bottom-Center cell"
            "button9" -> "Bottom-Right cell"
            else -> "Cell $buttonId" // Fallback, though should not happen with current IDs
        }
    }

    val stateDescription = when (player) {
        Player.X -> "Contains X"
        Player.O -> "Contains O"
        null -> "Empty"
    }

    val oldMoveSuffix = if (isOldMove && player != null) " (fading)" else ""
    val fullContentDescription = "$positionDescription - $stateDescription$oldMoveSuffix"

    Box(
        modifier = modifier // This will include layoutId, size, onGloballyPositioned, background, shape
            .semantics { contentDescription = fullContentDescription }
            .clickable(
                onClick = onClick,
                interactionSource = interactionSource,
                indication = null // No ripple effect
            ),
        contentAlignment = Alignment.Center
    ) {
        // The Box has the full description. Icons inside are decorative or part of the Box's state.
        // If further granularity is needed, individual icons could have simpler descriptions,
        // but the overall cell state is primary for accessibility here.
        when (player) {
            Player.X -> Icon(
                Icons.Default.Close,
                contentDescription = null, // Description is on the Box
                tint = iconTint, // Apply calculated tint
                modifier = iconModifier
            )

            Player.O -> Icon(
                painter = painterResource(R.drawable.player_2),
                contentDescription = null, // Description is on the Box
                tint = iconTint, // Apply calculated tint
                modifier = iconModifier
            )

            null -> {
                // Empty cell, Box already has "Empty" description
            }
        }
    }
}
