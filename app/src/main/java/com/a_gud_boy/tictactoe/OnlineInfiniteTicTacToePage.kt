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
// Assuming OnlineGameViewModel.MAX_VISIBLE_MOVES_PER_PLAYER is accessible via maxVisibleMovesPerPlayer parameter

@SuppressLint("MutableCollectionMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineInfiniteTicTacToePage(
    gameState: OnlineGameState,
    onCellClick: (Int) -> Unit,
    onNavigateBackToLobby: () -> Unit,
    maxVisibleMovesPerPlayer: Int,
    winningLine: List<Int>? = null
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
        createHorizontalChain(refs[0], refs[1], refs[2], chainStyle = ChainStyle.SpreadInside)
        createHorizontalChain(refs[3], refs[4], refs[5], chainStyle = ChainStyle.SpreadInside)
        createHorizontalChain(refs[6], refs[7], refs[8], chainStyle = ChainStyle.SpreadInside)
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
                modifier = Modifier
                    .width(300.dp)
                    .height(300.dp)
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

                        val isOldMoveRevised = remember(gameState, index, cellPlayer, maxVisibleMovesPerPlayer) {
                            when (cellPlayer) {
                                Player.X -> {
                                    gameState.currentPlayerId == gameState.player1Id && // Check if it's Player X's turn
                                    gameState.player1Moves.size == maxVisibleMovesPerPlayer &&
                                    gameState.player1Moves.firstOrNull() == index
                                }
                                Player.O -> {
                                    gameState.currentPlayerId == gameState.player2Id && // Check if it's Player O's turn
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
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (winningLine != null && winningLine.size >= 2 && buttonCoordinates.isNotEmpty()) {
                        val startCellIndex = winningLine.first()
                        val endCellIndex = winningLine.last()
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
                            val winnerMarkString = gameState.boardState.getOrNull(startCellIndex)
                            val winnerPlayer = Player.fromString(winnerMarkString)
                            val lineColor = when (winnerPlayer) {
                                Player.X -> playerXColor
                                Player.O -> playerOColor
                                null -> Color.Transparent
                            }
                            if (lineColor != Color.Transparent) {
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
        }

        val isGameOver = gameState.status == "player1_wins" || gameState.status == "player2_wins" || gameState.status == "draw"
        if (isGameOver) {
            Button(
                onClick = onNavigateBackToLobby,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 8.dp),
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
