package com.a_gud_boy.tictactoe

import android.annotation.SuppressLint
import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel


/**
 * Composable function responsible for rendering the UI of the standard Tic Tac Toe game.
 *
 * This function displays the main game interface, including:
 * - A 3x3 grid for the Tic Tac Toe board.
 * - Scores for both Player X and Player O.
 * - An indicator showing whose turn it is or the game result (win/draw).
 * - Buttons to reset the current round or start a new game (reset scores).
 *
 * @param innerPadding PaddingValues provided by the Scaffold, used for layout to avoid system UI elements.
 * @param viewModel The NormalTicTacToeViewModel instance that manages the game's state and logic.
 */
@RequiresApi(Build.VERSION_CODES.R)
@SuppressLint("MutableCollectionMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NormalTicTacToePage(
    innerPadding: PaddingValues,
    viewModel: NormalTicTacToeViewModel = viewModel()
) {
    val volume = 1.0f // Adjust volume as needed, or make it a parameter

    val playerXColor = colorResource(R.color.red_x_icon)
    val playerOColor = colorResource(R.color.blue_o_icon)

    val player1Wins by viewModel.player1Wins.collectAsStateWithLifecycle()
    val player2Wins by viewModel.player2Wins.collectAsStateWithLifecycle()
    val player1Moves by viewModel.player1Moves.collectAsStateWithLifecycle()
    val player2Moves by viewModel.player2Moves.collectAsStateWithLifecycle()
    val winnerInfo by viewModel.winnerInfo.collectAsStateWithLifecycle()
    val player1Turn by viewModel.player1Turn.collectAsStateWithLifecycle()
    val turnDenotingText by viewModel.turnDenotingText.collectAsStateWithLifecycle()
    val resetButtonText by viewModel.resetButtonText.collectAsStateWithLifecycle()

    val iconSize = 70.dp
    // Stores the layout coordinates of each cell button. Used for drawing the winning line.
    val buttonCoordinates = remember { mutableStateMapOf<String, LayoutCoordinates>() }

    // Animatable progress for drawing the winning line (0f to 1f).
    val lineAnimationProgress = remember { Animatable(0f) }
    // Holds the button IDs of the winning combination in order, to correctly draw/animate the line.
    val orderedWinningCombination = remember { mutableStateOf<List<String>>(emptyList()) }

    val context = LocalContext.current
    val soundManager = remember { SoundManager(context) }

    DisposableEffect(Unit) {
        onDispose {
            soundManager.release()
        }
    }

    val view = LocalView.current
    // This LaunchedEffect observes changes in winnerInfo.
    // When a game concludes (win or draw), it triggers haptic feedback, plays a sound,
    // sets up the winning line coordinates, and starts the line drawing animation.
    LaunchedEffect(winnerInfo) {
        if (winnerInfo != null) {
            // Provide haptic feedback for game conclusion.
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            // Play win or draw sound.
            if (winnerInfo?.winner != null) {
                soundManager.playWinSound(volume)
            } else { // Draw condition
                soundManager.playDrawSound(volume)
            }
            // Store the winning move combination to draw the line.
            orderedWinningCombination.value = winnerInfo!!.orderedWinningMoves

            // Reset animation progress and start the animation for the winning line.
            lineAnimationProgress.snapTo(0f) // Reset before starting
            lineAnimationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 600) // Duration of the line drawing animation.
            )
        } else {
            // If winnerInfo becomes null (e.g., game reset), reset animation state.
            lineAnimationProgress.snapTo(0f)
            orderedWinningCombination.value = emptyList()
        }
    }

    // Defines the constraints for the 3x3 Tic Tac Toe grid using ConstraintLayout.
    // Each button (cell) is constrained relative to its neighbors and the parent.
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

    Box(
        modifier = Modifier
            .padding(innerPadding)
            .wrapContentSize()
            .background(colorResource(R.color.background))
            .padding(horizontal = 20.dp)
    ) {
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            ConstraintLayout(
                constraintSet = constraints,
                modifier = Modifier
                    .padding(20.dp, 10.dp, 20.dp, 20.dp)
                    .width(300.dp)
                    .height(300.dp)
                    .shadow(4.dp, shape = RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(colorResource(R.color.constraint_background))
                    // Custom drawing logic for the winning line.
                    .drawWithContent {
                        drawContent() // Draw the ConstraintLayout children (the cells) first.
                        // Condition to draw the line: there must be a winning combination and animation progress > 0.
                        if (orderedWinningCombination.value.size >= 2 && lineAnimationProgress.value > 0f) {
                            val currentWinner = winnerInfo?.winner
                                ?: return@drawWithContent // Exit if no winner (should not happen if combination is present).

                            // The line animates from the most recent move in the winning combo to the oldest.
                            // This order (last to first) makes the animation feel more natural as if drawing from the last move.
                            val animationDrawStartButtonId = orderedWinningCombination.value.last() // Line draws from this cell's center.
                            val animationDrawEndButtonId = orderedWinningCombination.value.first()   // Line draws towards this cell's center.

                            val animStartCellCoordinates = buttonCoordinates[animationDrawStartButtonId]
                            val animEndCellCoordinates = buttonCoordinates[animationDrawEndButtonId]

                            if (animStartCellCoordinates != null && animEndCellCoordinates != null) {
                                // Calculate the center of the start cell for the line.
                                val actualLineStartPoint = Offset(
                                    animStartCellCoordinates.size.width / 2f + animStartCellCoordinates.positionInParent().x,
                                    animStartCellCoordinates.size.height / 2f + animStartCellCoordinates.positionInParent().y
                                )
                                // Calculate the center of the end cell for the line.
                                val actualLineEndPoint = Offset(
                                    animEndCellCoordinates.size.width / 2f + animEndCellCoordinates.positionInParent().x,
                                    animEndCellCoordinates.size.height / 2f + animEndCellCoordinates.positionInParent().y
                                )

                                // Interpolate the line's end point based on animation progress.
                                // The line "grows" from actualLineStartPoint towards actualLineEndPoint.
                                val animatedLineVisualEndPoint = lerp(
                                    actualLineStartPoint, // Start of the segment for lerp
                                    actualLineEndPoint,   // End of the segment for lerp
                                    lineAnimationProgress.value // Current animation fraction
                                )

                                // Extend the line slightly beyond the centers of the start and end cells for better visual appearance.
                                val lineExtensionPx = 30.dp.toPx()
                                // Vector from the fixed start point to the currently animated end point.
                                val currentDirectionVector = animatedLineVisualEndPoint - actualLineStartPoint

                                val lineColor = when (currentWinner) {
                                    Player.X -> playerXColor
                                    Player.O -> playerOColor
                                }

                                // Avoid division by zero if the vector is zero length (e.g., start and end points are the same).
                                // This might happen if animation progress is 0 or if somehow start and end cells are the same.
                                if (currentDirectionVector.getDistanceSquared() == 0f) {
                                    // If progress is full, it implies we should draw something, even if it's a point (though a line is expected).
                                    // This case is unlikely for a winning line but handled defensively.
                                    if (lineAnimationProgress.value == 1f) {
                                        drawLine(
                                            color = lineColor.copy(alpha = 0.6f),
                                            start = actualLineStartPoint,
                                            end = animatedLineVisualEndPoint, // Same as actualLineStartPoint here
                                            strokeWidth = 5.dp.toPx(),
                                            cap = StrokeCap.Round
                                        )
                                    }
                                } else { // Normal case: there is a direction, so calculate extensions.
                                    val normalizedDirection = currentDirectionVector / currentDirectionVector.getDistance()

                                    // Extend the line outwards from the true start and animated end points.
                                    val extendedVisualLineStart = actualLineStartPoint - (normalizedDirection * lineExtensionPx)
                                    val extendedVisualLineEnd = animatedLineVisualEndPoint + (normalizedDirection * lineExtensionPx)

                                    drawLine(
                                        color = lineColor.copy(alpha = 0.6f),
                                        start = extendedVisualLineStart,
                                        end = extendedVisualLineEnd,
                                        strokeWidth = 5.dp.toPx(),
                                        cap = StrokeCap.Round
                                    )
                                }
                            }
                        }
                    }
            ) {
                val buttonIds = List(9) { i -> "button${i + 1}" }
                buttonIds.forEach { buttonId ->
                    val cellPlayer: Player? = when {
                        player1Moves.contains(buttonId) -> Player.X
                        player2Moves.contains(buttonId) -> Player.O
                        else -> null
                    }
                    TicTacToeCell(
                        modifier = Modifier
                            .background(color = Color.White, shape = RoundedCornerShape(10.dp))
                            .width(80.dp)
                            .height(80.dp)
                            .layoutId(buttonId)
                            .onGloballyPositioned { coordinates ->
                                buttonCoordinates[buttonId] = coordinates
                            },
                        player = cellPlayer,
                        isOldMove = false, // In Normal TicTacToe, moves don't "disappear" or become "old".
                        iconSize = iconSize,
                        buttonId = buttonId, // Pass buttonId for accessibility, e.g., "button1", "button2", etc.
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY) // Haptic feedback on cell tap.
                            // TODO: Consider moving sound playing logic into ViewModel after move validation,
                            // or play sound optimistically and handle invalid move UI separately.
                            // For now, play sound before ViewModel action.
                            // soundManager.playMoveSound()
                            viewModel.onButtonClick(buttonId)
                        }
                    )
                }
            }

            // Card displaying game scores and current turn information.
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceAround
                ) {
                    Row(
                        modifier = Modifier
                            .padding(top = 10.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "$turnDenotingText (",
                            style = MaterialTheme.typography.labelMedium,
                            fontSize = 20.sp
                        )
                        // Provides a dynamic content description for the turn indicator icon,
                        // aiding accessibility by announcing the current game state (whose turn, winner, or draw).
                        val turnIconContentDescription = when {
                            winnerInfo?.winner == Player.X -> "Player X is the winner"
                            winnerInfo?.winner == Player.O -> "Player O is the winner"
                            winnerInfo != null && winnerInfo?.winner == null -> "Game is a draw"
                            player1Turn -> "Player X's turn"
                            else -> "Player O's turn"
                        }

                        // Display appropriate icon based on whose turn it is or if there's a winner/draw.
                        if (winnerInfo?.winner == Player.X || (winnerInfo == null && player1Turn)) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = turnIconContentDescription
                            )
                        } else if (winnerInfo?.winner == Player.O || (winnerInfo == null && !player1Turn)) {
                            Icon(
                                painterResource(R.drawable.player_2),
                                contentDescription = turnIconContentDescription
                            )
                        } else { // Draw case: display X icon, a handshake emoji, and O icon.
                            // The overall state "Game is a draw" is primary; individual icons are more decorative in this combined view.
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Player X icon for draw display" // Or null if purely decorative next to text
                            )
                            Text(
                                "\uD83E\uDD1D", // Handshake emoji
                                Modifier.width(24.dp) // Provides some spacing for the emoji.
                            )
                            Icon(
                                painterResource(R.drawable.player_2),
                                contentDescription = "Player O icon for draw display" // Or null
                            )
                        }
                        Text(
                            text = ")",
                            style = MaterialTheme.typography.labelMedium,
                            fontSize = 20.sp
                        )
                    }
                    // Row displaying player scores (X vs O).
                    Row(
                        modifier = Modifier
                            .padding(bottom = 10.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Player X score icon",
                            tint = playerXColor, // Use defined player X color
                            modifier = Modifier.padding(0.dp, 6.dp, 6.dp, 6.dp)
                        )
                        Text(
                            player1Wins.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.numberOfWinsTextColor_x),
                            modifier = Modifier
                                .background(
                                    colorResource(R.color.numberOfWinsBackgroundColor_x),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .padding(12.dp)
                        )
                        Text(
                            "-", style = MaterialTheme.typography.labelMedium,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.hyphenColor),
                            modifier = Modifier.padding(10.dp) // Hyphen separating scores
                        )
                        Icon(
                            painterResource(R.drawable.player_2),
                            contentDescription = "Player O score icon",
                            tint = playerOColor, // Use defined player O color
                            modifier = Modifier.padding(0.dp, 6.dp, 6.dp, 6.dp)
                        )
                        Text(
                            player2Wins.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.numberOfWinsTextColor_o),
                            modifier = Modifier
                                .background(
                                    colorResource(R.color.numberOfWinsBackgroundColor_o),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .padding(12.dp)
                        )
                    }
                }
            }

            // Button to reset the current round (clears the board but keeps scores).
            Button(
                onClick = {
                    viewModel.resetRound()
                    buttonCoordinates.clear() // Clear stored coordinates as the board is reset.
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.blue_o_icon), // Standard button color
                    contentColor = Color.White
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Refresh,
                        contentDescription = "Refresh icon for new or reset round"
                    )
                    Text(
                        text = resetButtonText, // Text changes based on game state (e.g., "New Round", "Play Again?")
                        modifier = Modifier
                            .padding(start = 8.dp)
                    )
                }
            }
            // Button to reset all scores and start a completely new game.
            Button(
                onClick = {
                    viewModel.resetScores()
                    buttonCoordinates.clear() // Clear stored coordinates as the game and board are fully reset.
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.resetScoresButtonBackground), // Distinct color for score reset
                    contentColor = colorResource(R.color.darkTextColor)
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Refresh icon for reset scores")
                    Text(
                        text = "Reset Scores",
                        modifier = Modifier
                            .padding(start = 8.dp)
                    )
                }
            }
        }
    }
}



