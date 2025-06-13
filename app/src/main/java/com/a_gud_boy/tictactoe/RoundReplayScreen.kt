package com.a_gud_boy.tictactoe

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.a_gud_boy.tictactoe.GameType // Import GameType
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@Composable
fun RoundReplayScreen(
    navController: NavController,
    matchId: Long,
    roundId: Long,
    roundReplayViewModel: RoundReplayViewModel = viewModel(factory = LocalViewModelFactory.current)
) {
    val focusRequester = remember { FocusRequester() }

    val gameType = roundReplayViewModel.gameType // Access gameType from ViewModel

    val currentGridState by roundReplayViewModel.currentGridState.collectAsState()
    val currentMoveIndex by roundReplayViewModel.currentMoveIndex.collectAsState()
    val moves by roundReplayViewModel.moves.collectAsState()
    val winningPlayer by roundReplayViewModel.winningPlayer.collectAsState() // Using collectAsState for simplicity
    val orderedWinningCells by roundReplayViewModel.orderedWinningCells.collectAsState() // Using collectAsState for simplicity

    val replayCellCoordinates = remember { mutableStateMapOf<String, LayoutCoordinates>() }
    val replayLineAnimationProgress = remember { Animatable(0f) }
    val playerXColor = colorResource(R.color.red_x_icon)
    val playerOColor = colorResource(R.color.blue_o_icon)


    // Defines the constraints for the 3x3 Tic Tac Toe grid using ConstraintLayout.
    val constraints = ConstraintSet {
        val button1 = createRefFor("button1")
        val button2 = createRefFor("button2")
        val button3 = createRefFor("button3")
        val button4 = createRefFor("button4")
        val button5 = createRefFor("button5")
        val button6 = createRefFor("button6")
        val button7 = createRefFor("button7")
        val button8 = createRefFor("button8")
        val button9 = createRefFor("button9")
        val margin = 0.dp

        constrain(button1) {
            top.linkTo(parent.top, margin = margin); start.linkTo(
            parent.start,
            margin = margin
        ); end.linkTo(button2.start, margin = margin); bottom.linkTo(button4.top, margin = margin)
        }
        constrain(button2) {
            top.linkTo(parent.top, margin = margin); start.linkTo(
            button1.end,
            margin = margin
        ); end.linkTo(button3.start, margin = margin); bottom.linkTo(button5.top, margin = margin)
        }
        constrain(button3) {
            top.linkTo(parent.top, margin = margin); start.linkTo(
            button2.end,
            margin = margin
        ); end.linkTo(parent.end, margin = margin); bottom.linkTo(button6.top, margin = margin)
        }
        constrain(button4) {
            top.linkTo(button1.bottom, margin = margin); start.linkTo(
            parent.start,
            margin = margin
        ); end.linkTo(button5.start, margin = margin); bottom.linkTo(button7.top, margin = margin)
        }
        constrain(button5) {
            top.linkTo(button2.bottom, margin = margin); start.linkTo(
            button4.end,
            margin = margin
        ); end.linkTo(button6.start, margin = margin); bottom.linkTo(button8.top, margin = margin)
        }
        constrain(button6) {
            top.linkTo(button3.bottom, margin = margin); start.linkTo(
            button5.end,
            margin = margin
        ); end.linkTo(parent.end, margin = margin); bottom.linkTo(button9.top, margin = margin)
        }
        constrain(button7) {
            top.linkTo(button4.bottom, margin = margin); start.linkTo(
            parent.start,
            margin = margin
        ); end.linkTo(button8.start, margin = margin); bottom.linkTo(parent.bottom, margin = margin)
        }
        constrain(button8) {
            top.linkTo(button5.bottom, margin = margin); start.linkTo(
            button7.end,
            margin = margin
        ); end.linkTo(button9.start, margin = margin); bottom.linkTo(parent.bottom, margin = margin)
        }
        constrain(button9) {
            top.linkTo(button6.bottom, margin = margin); start.linkTo(
            button8.end,
            margin = margin
        ); end.linkTo(parent.end, margin = margin); bottom.linkTo(parent.bottom, margin = margin)
        }
    }

    val iconSize = 70.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.background))
            .padding(16.dp)
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.key) {
                        Key.DirectionLeft -> {
                            roundReplayViewModel.previousMove()
                            return@onKeyEvent true
                        }

                        Key.DirectionRight -> {
                            roundReplayViewModel.nextMove()
                            return@onKeyEvent true
                        }
                    }
                }
                false
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        LaunchedEffect(currentMoveIndex, winningPlayer, orderedWinningCells, moves.size) {
            if (currentMoveIndex == moves.size - 1 && moves.isNotEmpty() && winningPlayer != null && orderedWinningCells.isNotEmpty()) {
                replayLineAnimationProgress.snapTo(0f)
                replayLineAnimationProgress.animateTo(
                    1f,
                    animationSpec = tween(durationMillis = 600)
                )
            } else {
                replayLineAnimationProgress.snapTo(0f)
            }
        }

        val totalMoves = moves.size
        val displayMoveIndex = if (currentMoveIndex == -1) 0 else currentMoveIndex + 1
        Text(
            text = if (totalMoves > 0) "Move: $displayMoveIndex / $totalMoves" else "No moves in this round",
            modifier = Modifier.padding(bottom = 20.dp)
        )

        ConstraintLayout(
            constraintSet = constraints,
            modifier = Modifier
                .width(300.dp)
                .height(300.dp)
                .shadow(4.dp, shape = RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(colorResource(R.color.constraint_background))
                .drawWithContent {
                    drawContent()
                    if (currentMoveIndex == moves.size - 1 && moves.isNotEmpty() && winningPlayer != null && orderedWinningCells.isNotEmpty() && replayLineAnimationProgress.value > 0f) {
                        val startCellId =
                            orderedWinningCells.first() // Draw from first to last or last to first as preferred
                        val endCellId = orderedWinningCells.last()

                        val startCoordinates = replayCellCoordinates[startCellId]
                        val endCoordinates = replayCellCoordinates[endCellId]

                        if (startCoordinates != null && endCoordinates != null) {
                            val startOffsetInParent = startCoordinates.positionInParent()
                            val endOffsetInParent = endCoordinates.positionInParent()

                            val startCellCenter = Offset(
                                startOffsetInParent.x + startCoordinates.size.width / 2f,
                                startOffsetInParent.y + startCoordinates.size.height / 2f
                            )
                            val endCellCenter = Offset(
                                endOffsetInParent.x + endCoordinates.size.width / 2f,
                                endOffsetInParent.y + endCoordinates.size.height / 2f
                            )

                            // Lerp for animation
                            val animatedEndCellCenter = lerp(
                                startCellCenter,
                                endCellCenter,
                                replayLineAnimationProgress.value
                            )

                            // Extend the line
                            val lineExtensionPx = 30.dp.toPx() // Adjust as needed
                            val direction = (animatedEndCellCenter - startCellCenter)
                            val normalizedDirection =
                                if (direction.getDistanceSquared() > 0) direction / direction.getDistance() else Offset.Zero

                            val extendedStart =
                                startCellCenter - normalizedDirection * lineExtensionPx
                            val extendedEnd =
                                animatedEndCellCenter + normalizedDirection * lineExtensionPx


                            val lineColor =
                                if (winningPlayer == Player.X) playerXColor else playerOColor
                            val lineStrokeWidth = 5.dp.toPx()

                            drawLine(
                                color = lineColor.copy(alpha = 0.6f),
                                start = extendedStart,
                                end = extendedEnd,
                                strokeWidth = lineStrokeWidth,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }
        ) {
            val buttonIds = List(9) { i -> "button${i + 1}" }
            buttonIds.forEach { buttonId ->
                val playerOnCell = currentGridState[buttonId] // Player occupying this cell from ViewModel's perspective
                var isOldMoveValue = false

                if (playerOnCell != null && currentMoveIndex >= 0 && moves.value.isNotEmpty()) {
                    if (gameType == GameType.INFINITE) {
                        // Infinite Mode: Dim only the oldest of 3 visible moves for the player who just moved.
                        if (currentMoveIndex < moves.value.size) { // Make sure currentMoveIndex is valid
                            val lastMoveMade = moves.value[currentMoveIndex]
                            val lastPlayer = Player.fromString(lastMoveMade.player)

                            if (playerOnCell == lastPlayer) { // Cell belongs to the player who just made a move
                                val allMovesOfLastPlayerUpToCurrent = moves.value
                                    .subList(0, currentMoveIndex + 1)
                                    .filter { Player.fromString(it.player) == lastPlayer }
                                    .map { it.cellId }

                                if (allMovesOfLastPlayerUpToCurrent.size == 3) { // Player has exactly 3 visible moves
                                    val oldestVisibleMoveCellId = allMovesOfLastPlayerUpToCurrent.first()
                                    if (buttonId == oldestVisibleMoveCellId) {
                                        isOldMoveValue = true
                                    }
                                }
                                // If not 3 moves, or not the oldest, isOldMoveValue remains false.
                            }
                            // If cell does not belong to lastPlayer, isOldMoveValue remains false.
                        }
                    } else { // GameType.NORMAL (or any other type)
                        // Normal Mode: Dim any move that is not the current move.
                        val moveInstance = moves.value.findLast { mv ->
                            mv.cellId == buttonId && Player.fromString(mv.player) == playerOnCell
                        }
                        if (moveInstance != null) {
                            val indexOfThisMoveInFullList = moves.value.indexOf(moveInstance)
                            if (indexOfThisMoveInFullList < currentMoveIndex) {
                                isOldMoveValue = true
                            }
                        }
                    }
                }

                TicTacToeCell(
                    modifier = Modifier
                        .layoutId(buttonId)
                        .background(Color.White, RoundedCornerShape(10.dp))
                        .onGloballyPositioned { coordinates ->
                            replayCellCoordinates[buttonId] = coordinates
                        }
                        // For now, using the ConstraintLayout background.
                        .width(80.dp) // Adjust size as needed, considering padding
                        .height(80.dp),// Adjust size as needed, considering padding
                    player = playerOnCell,
                    isOldMove = isOldMoveValue, // Use the new logic
                    iconSize = iconSize,
                    buttonId = buttonId,
                    onClick = {
                        // Click listener is empty for now, replay controlled by arrows
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp)) // Add space between grid and buttons

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { roundReplayViewModel.previousMove() },
                enabled = currentMoveIndex > -1,
                modifier = Modifier.weight(1f)
            ) {
                Text("Previous")
            }

            Spacer(modifier = Modifier.width(16.dp)) // Space between buttons

            Button(
                onClick = { roundReplayViewModel.nextMove() },
                enabled = currentMoveIndex < moves.size - 1,
                modifier = Modifier.weight(1f)
            ) {
                Text("Next")
            }
        }
    }
}
