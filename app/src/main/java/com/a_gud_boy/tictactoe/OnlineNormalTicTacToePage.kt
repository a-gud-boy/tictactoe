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
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension

// Using project's OnlineGameState, Player, TicTacToeCell

/**
 * OnlineNormalTicTacToePage Composable.
 * Displays the game board for a normal online Tic Tac Toe match.
 *
 * @param gameState The current state of the online game.
 * @param onCellClick Callback for when a cell is clicked, passing the cell index.
 * @param onNavigateBackToLobby Callback to navigate back to the lobby.
 * @param winningLine Optional list of cell indices representing the winning line.
 */
@SuppressLint("MutableCollectionMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineNormalTicTacToePage(
    gameState: OnlineGameState, // from com.a_gud_boy.tictactoe.OnlineGameState
    onCellClick: (Int) -> Unit,
    onNavigateBackToLobby: () -> Unit,
    winningLine: List<Int>? = null // Added as per subtask requirement
) {
    val view = LocalView.current
    val playerXColor = colorResource(R.color.red_x_icon)
    val playerOColor = colorResource(R.color.blue_o_icon)
    val iconSize = 70.dp

    val buttonCoordinates = remember { mutableMapOf<Int, LayoutCoordinates>() }

    val constraints = ConstraintSet {
        val refs = (0..8).map { createRefFor("cell$it") }
        val margin = 0.dp
        for (i in 0..8) {
            val row = i / 3
            val col = i % 3
            constrain(refs[i]) {
                width = Dimension.value(80.dp); height = Dimension.value(80.dp)
                if (row == 0) top.linkTo(parent.top, margin) else top.linkTo(refs[i - 3].bottom, margin)
                if (col == 0) start.linkTo(parent.start, margin) else start.linkTo(refs[i - 1].end, margin)
            }
        }
        createHorizontalChain(refs[0], refs[1], refs[2], chainStyle = ChainStyle.Spread)
        createHorizontalChain(refs[3], refs[4], refs[5], chainStyle = ChainStyle.Spread)
        createHorizontalChain(refs[6], refs[7], refs[8], chainStyle = ChainStyle.Spread)
        createVerticalChain(refs[0], refs[3], refs[6], chainStyle = ChainStyle.Spread)
        createVerticalChain(refs[1], refs[4], refs[7], chainStyle = ChainStyle.Spread)
        createVerticalChain(refs[2], refs[5], refs[8], chainStyle = ChainStyle.Spread)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.background))
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
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
                modifier = Modifier.width(300.dp).height(300.dp)
            ) {
                ConstraintLayout(
                    constraintSet = constraints,
                    modifier = Modifier.fillMaxSize().shadow(4.dp, RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp)).background(colorResource(R.color.constraint_background))
                ) {
                    (0..8).forEach { index ->
                        val cellPlayer = Player.fromString(gameState.boardState[index])
                        val isWinningCell = winningLine?.contains(index) ?: false

                        // Use the existing TicTacToeCell, adapting its parameters
                        TicTacToeCell(
                            modifier = Modifier
                                .background(
                                    color = if (isWinningCell) Color.Yellow.copy(alpha = 0.3f) else Color.White, // Highlight winning cells
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .layoutId("cell$index")
                                .onGloballyPositioned { coordinates ->
                                    buttonCoordinates[index] = coordinates
                                },
                            player = cellPlayer,
                            // Normal mode doesn't have "old" moves in the same way Infinite does.
                            // Pass false, or adapt TicTacToeCell if a different visual is needed for winning cells beyond background.
                            isOldMove = false,
                            iconSize = iconSize,
                            buttonId = "cell$index",
                            onClick = {
                                HapticFeedbackManager.performHapticFeedback(view, HapticFeedbackConstants.VIRTUAL_KEY)
                                onCellClick(index)
                            }
                        )
                    }
                }

                // Winning Line Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (winningLine != null && winningLine.size >= 2) {
                        val startCellIndex = winningLine.first()
                        val endCellIndex = winningLine.last() // For a 3-in-a-row, first and last define the line

                        val startCoords = buttonCoordinates[startCellIndex]
                        val endCoords = buttonCoordinates[endCellIndex]

                        if (startCoords != null && endCoords != null) {
                            val startOffset = Offset(
                                startCoords.positionInParent().x + startCoords.size.width / 2f,
                                startCoords.positionInParent().y + startCoords.size.height / 2f
                            )
                            val endOffset = Offset(
                                endCoords.positionInParent().x + endCoords.size.width / 2f,
                                endCoords.positionInParent().y + endCoords.size.height / 2f
                            )
                            val winnerMark = Player.fromString(gameState.boardState[startCellIndex])
                            val lineColor = if (winnerMark == Player.X) playerXColor else playerOColor

                            // Extend line slightly for better visuals
                            val direction = endOffset - startOffset
                            val normalizedDirection = if (direction.getDistanceSquared() > 0) direction / direction.getDistance() else Offset.Zero
                            val extension = 30.dp.toPx()

                            drawLine(
                                color = lineColor.copy(alpha = 0.7f),
                                start = startOffset - normalizedDirection * extension,
                                end = endOffset + normalizedDirection * extension,
                                strokeWidth = 5.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }
            }
        }

        val isGameOver = gameState.status.contains("_wins") || gameState.status == "draw"
        if (isGameOver) {
            Button(
                onClick = onNavigateBackToLobby,
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 8.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back to Lobby icon")
                    Text(text = "Back to Lobby", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}
