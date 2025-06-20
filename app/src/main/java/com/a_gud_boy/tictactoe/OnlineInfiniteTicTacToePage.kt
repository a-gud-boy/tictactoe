package com.a_gud_boy.tictactoe

import android.annotation.SuppressLint
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet

// Assuming OnlineGameState is defined in OnlineGameScreen.kt
// Assuming Player is defined in Player.kt
// Assuming TicTacToeCell is defined in TicTacToeCell.kt
// Assuming OnlineGameViewModel.MAX_VISIBLE_MOVES_PER_PLAYER is accessible

@SuppressLint("MutableCollectionMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineInfiniteTicTacToePage(
    gameState: OnlineGameState,
    onCellClick: (Int) -> Unit,
    onNavigateBackToLobby: () -> Unit,
    maxVisibleMovesPerPlayer: Int // Added parameter
) {
    val view = LocalView.current
    val playerXColor = colorResource(R.color.red_x_icon)
    val playerOColor = colorResource(R.color.blue_o_icon)
    val iconSize = 70.dp

    // Stores the layout coordinates of each cell button. Used for drawing the winning line.
    // Even if winning line is omitted for now, this might be useful for future enhancements.
    val buttonCoordinates = remember { mutableMapOf<Int, LayoutCoordinates>() }


    // Defines the constraints for the 3x3 Tic Tac Toe grid using ConstraintLayout.
    val constraints = ConstraintSet {
        val refs = (0..8).map { createRefFor("cell$it") }
        val margin = 0.dp

        // Constrain rows and columns
        for (i in 0..8) {
            val row = i / 3
            val col = i % 3

            constrain(refs[i]) {
                width = Dimension.value(80.dp) // Cell size
                height = Dimension.value(80.dp)

                // Top constraint
                if (row == 0) top.linkTo(parent.top, margin = margin)
                else top.linkTo(refs[i - 3].bottom, margin = margin)

                // Bottom constraint (optional, can help with stability)
                if (row == 2) bottom.linkTo(parent.bottom, margin = margin)
                else bottom.linkTo(refs[i + 3].top, margin = margin)

                // Start constraint
                if (col == 0) start.linkTo(parent.start, margin = margin)
                else start.linkTo(refs[i - 1].end, margin = margin)

                // End constraint (optional)
                if (col == 2) end.linkTo(parent.end, margin = margin)
                else end.linkTo(refs[i + 1].start, margin = margin)
            }
        }
        // Create horizontal chains for rows to distribute space
        createHorizontalChain(refs[0], refs[1], refs[2], chainStyle = ChainStyle.SpreadInside)
        createHorizontalChain(refs[3], refs[4], refs[5], chainStyle = ChainStyle.SpreadInside)
        createHorizontalChain(refs[6], refs[7], refs[8], chainStyle = ChainStyle.SpreadInside)

        // Create vertical chains for columns
        createVerticalChain(refs[0], refs[3], refs[6], chainStyle = ChainStyle.SpreadInside)
        createVerticalChain(refs[1], refs[4], refs[7], chainStyle = ChainStyle.SpreadInside)
        createVerticalChain(refs[2], refs[5], refs[8], chainStyle = ChainStyle.SpreadInside)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.background))
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween // Push button to bottom if possible
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "${gameState.player1DisplayName ?: "Player 1"} (X) vs ${gameState.player2DisplayName ?: "Player 2"} (O)",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = gameState.turnMessage,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            Box(
                modifier = Modifier
                    .width(300.dp) // Fixed width for the game area
                    .height(300.dp) // Fixed height for the game area
            ) {
                ConstraintLayout(
                    constraintSet = constraints,
                    modifier = Modifier
                        .fillMaxSize()
                        .shadow(4.dp, shape = RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(colorResource(R.color.constraint_background))
                ) {
                    (0..8).forEach { index ->
                        val cellPlayer = Player.fromString(gameState.boardState[index])

                        // Removed the first isOldMove calculation block.

                        // A different way to calculate isOldMove, directly based on whose mark it is
                        // and if it's their oldest visible move.
                        // Using the new maxVisibleMovesPerPlayer parameter.
                        val isOldMoveRevised = remember(gameState, index, cellPlayer, maxVisibleMovesPerPlayer) {
                            when (cellPlayer) {
                                Player.X -> {
                                    gameState.player1Moves.size == maxVisibleMovesPerPlayer &&
                                    gameState.player1Moves.firstOrNull() == index
                                }
                                Player.O -> {
                                    gameState.player2Moves.size == maxVisibleMovesPerPlayer &&
                                    gameState.player2Moves.firstOrNull() == index
                                }
                                null -> false
                            }
                        }


                        TicTacToeCell(
                            modifier = Modifier
                                .background(color = Color.White, shape = RoundedCornerShape(10.dp))
                                .layoutId("cell$index")
                                .onGloballyPositioned { coordinates ->
                                    buttonCoordinates[index] = coordinates
                                },
                            player = cellPlayer,
                            isOldMove = isOldMoveRevised,
                            iconSize = iconSize,
                            buttonId = "cell$index",
                            onClick = {
                                HapticFeedbackManager.performHapticFeedback(
                                    view,
                                    HapticFeedbackConstants.VIRTUAL_KEY
                                )
                                onCellClick(index)
                            }
                        )
                    }
                }
                // Winning Line Canvas (omitted for now as gameState doesn't provide winningLine)
                // If it were to be added, it would be similar to InfiniteTicTacToePage's Canvas,
                // but would need gameState.winningLine (e.g., List<Int> of cell indices)
            }
        }

        val isGameOver = gameState.status == "player1_wins" || gameState.status == "player2_wins" || gameState.status == "draw"
        if (isGameOver) {
            Button(
                onClick = onNavigateBackToLobby,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 8.dp), // Ensure some padding
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "Back to Lobby icon"
                    )
                    Text(
                        text = "Back to Lobby",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

[end of app/src/main/java/com/a_gud_boy/tictactoe/OnlineInfiniteTicTacToePage.kt]
