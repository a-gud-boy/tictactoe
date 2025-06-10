package com.a_gud_boy.tictactoe

import android.annotation.SuppressLint
import android.view.HapticFeedbackConstants
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
 * Composable function that represents the main screen for the Infinite Tic Tac Toe game.
 * It displays the game board, player scores, turn information, and control buttons.
 *
 * The game features a 3x3 grid where players take turns placing their marks (X or O).
 * Unlike traditional Tic Tac Toe, marks disappear after a set number of subsequent moves,
 * requiring players to strategize around a constantly changing board.
 *
 * This composable observes various states from [InfiniteTicTacToeViewModel] such as
 * player moves, win counts, current turn, and game status to render the UI dynamically.
 * It also handles user interactions like tapping on a cell to make a move or resetting
 * the game/round.
 *
 * A key visual feature is the line drawn across the winning combination of cells when a player wins.
 * Additionally, cells whose marks are about to disappear are visually dimmed to provide a cue to the player.
 *
 * @param innerPadding Padding values to apply to the root Box composable, typically provided by a Scaffold
 *                     or other parent layout, to ensure content is not obscured by system UI elements.
 * @param viewModel The [InfiniteTicTacToeViewModel] instance that holds and manages the game's state
 *                  and business logic. Defaults to a new ViewModel instance provided by `viewModel()`.
 */
@SuppressLint("MutableCollectionMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfiniteTicTacToePage(
    innerPadding: PaddingValues,
    viewModel: InfiniteTicTacToeViewModel = viewModel()
) {
    val volume = 1.0f

    val playerXColor = colorResource(R.color.red_x_icon)
    val playerOColor = colorResource(R.color.blue_o_icon)

    val player1Wins by viewModel.player1Wins.collectAsStateWithLifecycle()
    val player2Wins by viewModel.player2Wins.collectAsStateWithLifecycle()
    val player1Moves by viewModel.player1Moves.collectAsStateWithLifecycle()
    val player2Moves by viewModel.player2Moves.collectAsStateWithLifecycle()
    // Add these lines:
    val p1VisibleMoves =
        player1Moves.takeLast(InfiniteTicTacToeViewModel.MAX_VISIBLE_MOVES_PER_PLAYER)
    val p2VisibleMoves =
        player2Moves.takeLast(InfiniteTicTacToeViewModel.MAX_VISIBLE_MOVES_PER_PLAYER)
    val winnerInfo by viewModel.winnerInfo.collectAsStateWithLifecycle()
    val player1Turn by viewModel.player1Turn.collectAsStateWithLifecycle()
    val turnDenotingText by viewModel.turnDenotingText.collectAsStateWithLifecycle()
    val gameStarted by viewModel.gameStarted.collectAsStateWithLifecycle()
    val resetButtonText by viewModel.resetButtonText.collectAsStateWithLifecycle()
    val isAIMode by viewModel.isAIMode.collectAsStateWithLifecycle()
    val currentAIDifficulty by viewModel.aiDifficulty.collectAsStateWithLifecycle()
    // isGameConcluded is implicitly handled by winnerInfo (being non-null) and gameStarted states in ViewModel.

    val iconSize = 70.dp
    // Stores the layout coordinates of each cell button. Used for drawing the winning line.
    val buttonCoordinates = remember { mutableStateMapOf<String, LayoutCoordinates>() }

    val context = LocalContext.current
    // SoundManager instance for playing game sounds.
    val soundManager = remember { SoundManager(context) }

    DisposableEffect(Unit) {
        onDispose {
            soundManager.release()
        }
    }

    val view = LocalView.current
    // Animatable progress for drawing the winning line (0f to 1f).
    val lineAnimationProgress = remember { Animatable(0f) }
    // Holds the button IDs of the winning combination in order, as determined by the ViewModel.
    // This order is used to fetch coordinates for drawing the line.
    val orderedWinningCombination = remember { mutableStateOf<List<String>>(emptyList()) }

    // This LaunchedEffect observes changes in winnerInfo.
    // When a game concludes (win or draw), it triggers haptic feedback, plays a sound,
    // sets up the winning line coordinates from winnerInfo, and starts the line drawing animation.
    LaunchedEffect(winnerInfo) {
        if (winnerInfo != null) {
            // Provide haptic feedback for game conclusion.
            HapticFeedbackManager.performHapticFeedback(view, HapticFeedbackConstants.CONFIRM)
            // Play win or draw sound based on whether there's a specific winner or it's a draw.
            if (winnerInfo?.winner != null) {
                soundManager.playWinSound(volume)
            } else { // Draw condition
                soundManager.playDrawSound(volume)
            }
            // Store the sequence of button IDs that form the winning line.
            orderedWinningCombination.value = winnerInfo!!.orderedWin

            // Reset animation progress to 0 and then animate to 1 to draw the line.
            lineAnimationProgress.snapTo(0f) // Reset before starting animation.
            lineAnimationProgress.animateTo(
                targetValue = 1f, // Animate to full progress.
                animationSpec = tween(durationMillis = 600) // Duration of the line drawing animation.
            )
        } else {
            // If winnerInfo becomes null (e.g., game reset), clear the winning line and reset animation.
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
            top.linkTo(parent.top, margin = margin)
            start.linkTo(parent.start, margin = margin)
            end.linkTo(button2.start, margin = margin)
            bottom.linkTo(button4.top, margin = margin)
        }

        constrain(button2) {
            top.linkTo(parent.top, margin = margin)
            start.linkTo(button1.end, margin = margin)
            end.linkTo(button3.start, margin = margin)
            bottom.linkTo(button5.top, margin = margin)
        }

        constrain(button3) {
            top.linkTo(parent.top, margin = margin)
            start.linkTo(button2.end, margin = margin)
            end.linkTo(parent.end, margin = margin)
            bottom.linkTo(button6.top, margin = margin)
        }

        constrain(button4) {
            top.linkTo(button1.bottom, margin = margin)
            start.linkTo(parent.start, margin = margin)
            end.linkTo(button5.start, margin = margin)
            bottom.linkTo(button7.top, margin = margin)
        }

        constrain(button5) {
            top.linkTo(button2.bottom, margin = margin)
            start.linkTo(button4.end, margin = margin)
            end.linkTo(button6.start, margin = margin)
            bottom.linkTo(button8.top, margin = margin)
        }

        constrain(button6) {
            top.linkTo(button3.bottom, margin = margin)
            start.linkTo(button5.end, margin = margin)
            end.linkTo(parent.end, margin = margin)
            bottom.linkTo(button9.top, margin = margin)
        }

        constrain(button7) {
            top.linkTo(button4.bottom, margin = margin)
            start.linkTo(parent.start, margin = margin)
            end.linkTo(button8.start, margin = margin)
            bottom.linkTo(parent.bottom, margin = margin)
        }

        constrain(button8) {
            top.linkTo(button5.bottom, margin = margin)
            start.linkTo(button7.end, margin = margin)
            end.linkTo(button9.start, margin = margin)
            bottom.linkTo(parent.bottom, margin = margin)
        }

        constrain(button9) {
            top.linkTo(button6.bottom, margin = margin)
            start.linkTo(button8.end, margin = margin)
            end.linkTo(parent.end, margin = margin)
            bottom.linkTo(parent.bottom, margin = margin)
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
//                Text(
//                    text = "Game Board",
//                    style = MaterialTheme.typography.labelMedium,
//                    fontSize = 25.sp
//                )
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

                        // Condition to draw the line: there must be a valid winning combination (at least 2 cells)
                        // and the animation must be in progress (progress > 0f).
                        if (orderedWinningCombination.value.size >= 2 && lineAnimationProgress.value > 0f) {
                            val currentWinner = winnerInfo?.winner
                                ?: return@drawWithContent // Exit if no winner (should not happen if combination is present).

                            // Get the button IDs for the start and end of the winning line from the ViewModel's ordered list.
                            // The ViewModel determines the actual cells that form the win.
                            // The line will animate visually from the 'lineAppearStartButtonId' towards 'lineAppearEndButtonId'.
                            val lineAppearStartButtonId =
                                orderedWinningCombination.value.first() // Line visually starts here.
                            val lineAppearEndButtonId =
                                orderedWinningCombination.value.last()   // Line visually ends here.

                            val lineStartCellCoordinates =
                                buttonCoordinates[lineAppearStartButtonId]
                            val lineEndCellCoordinates = buttonCoordinates[lineAppearEndButtonId]

                            if (lineStartCellCoordinates != null && lineEndCellCoordinates != null) {
                                // Calculate the center of the starting cell for the line.
                                val actualLineStartPoint = Offset(
                                    lineStartCellCoordinates.size.width / 2f + lineStartCellCoordinates.positionInParent().x,
                                    lineStartCellCoordinates.size.height / 2f + lineStartCellCoordinates.positionInParent().y
                                )
                                // Calculate the center of the ending cell for the line.
                                val actualLineEndPoint = Offset(
                                    lineEndCellCoordinates.size.width / 2f + lineEndCellCoordinates.positionInParent().x,
                                    lineEndCellCoordinates.size.height / 2f + lineEndCellCoordinates.positionInParent().y
                                )

                                // Interpolate the visual end point of the line based on animation progress.
                                // The line "grows" from actualLineStartPoint towards actualLineEndPoint.
                                val animatedVisualLineEnd = lerp(
                                    actualLineStartPoint, // Start of the segment for lerp
                                    actualLineEndPoint,   // End of the segment for lerp
                                    lineAnimationProgress.value // Current animation fraction (0f to 1f)
                                )

                                val lineColor = when (currentWinner) {
                                    Player.X -> playerXColor
                                    Player.O -> playerOColor
                                }

                                // Calculate the direction vector from the true start to the true end of the line.
                                // This is used to extend the line slightly beyond the cell centers for better visuals.
                                val overallDirectionVector =
                                    actualLineEndPoint - actualLineStartPoint
                                val normalizedOverallDirection =
                                    if (overallDirectionVector.getDistance() > 0) {
                                        overallDirectionVector / overallDirectionVector.getDistance()
                                    } else {
                                        Offset(
                                            0f,
                                            0f
                                        ) // Avoid division by zero if start and end are same.
                                    }

                                val lineExtensionPx =
                                    30.dp.toPx() // How much to extend the line on each side.

                                // Extend the line outwards from the true start and true end points.
                                // The animated line will then be drawn between these extended points, but its length
                                // will be controlled by `animatedVisualLineEnd` through lerp.
                                val extendedVisualLineStart =
                                    actualLineStartPoint - (normalizedOverallDirection * lineExtensionPx)
                                val extendedVisualLineEndTarget =
                                    actualLineEndPoint + (normalizedOverallDirection * lineExtensionPx)

                                // The line's visual appearance grows from the extended start towards the extended end,
                                // effectively making the animated part (`lineAnimationProgress.value`) cover the segment
                                // from `extendedVisualLineStart` to `extendedVisualLineEndTarget`.
                                val finalAnimatedEnd = lerp(
                                    extendedVisualLineStart,
                                    extendedVisualLineEndTarget,
                                    lineAnimationProgress.value
                                )


                                // Draw the line.
                                drawLine(
                                    color = lineColor.copy(alpha = 0.6f),
                                    start = extendedVisualLineStart, // Fixed extended start
                                    end = finalAnimatedEnd, // Animated extended end
                                    strokeWidth = 5.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            }
                        }
                    }
            ) {
                val buttonIds = List(9) { i -> "button${i + 1}" }
                buttonIds.forEach { buttonId ->
                    val cellPlayer: Player? = if (p2VisibleMoves.contains(buttonId)) Player.O
                    else if (p1VisibleMoves.contains(buttonId)) Player.X
                    else null

                    // Determine if the current cell represents an "old move" that should be dimmed.
                    // This is specific to Infinite Tic Tac Toe mode.
                    // A move is considered "old" if:
                    // 1. The cell is occupied by a player (cellPlayer != null).
                    // 2. It's the current player's turn.
                    // 3. The current player is Player X, has made MAX_VISIBLE_MOVES_PER_PLAYER,
                    //    and the current cell (buttonId) is the first (oldest) in their list of moves.
                    // OR
                    // 4. The current player is Player O (not player1Turn), has made MAX_VISIBLE_MOVES_PER_PLAYER,
                    //    and the current cell (buttonId) is the first (oldest) in their list of moves.
                    val isOldMove = cellPlayer != null &&
                            ((player1Turn && cellPlayer == Player.X && p1VisibleMoves.size == InfiniteTicTacToeViewModel.MAX_VISIBLE_MOVES_PER_PLAYER && p1VisibleMoves.firstOrNull() == buttonId) ||
                                    (!player1Turn && cellPlayer == Player.O && p2VisibleMoves.size == InfiniteTicTacToeViewModel.MAX_VISIBLE_MOVES_PER_PLAYER && p2VisibleMoves.firstOrNull() == buttonId))

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
                        isOldMove = isOldMove, // If true, the cell's content will be dimmed.
                        iconSize = iconSize,
                        buttonId = buttonId, // Pass buttonId for accessibility, e.g., "button1", "button2", etc.
                        onClick = {
                            HapticFeedbackManager.performHapticFeedback(
                                view,
                                HapticFeedbackConstants.VIRTUAL_KEY
                            ) // Haptic feedback on cell tap.
                            // Sound for move is typically handled by ViewModel after validation.
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
                    // Row displaying whose turn it is or the game result.
                    Row(
                        modifier = Modifier
                            .padding(top = 10.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "$turnDenotingText (", // Text like "Player X's Turn", "You Win!", "AI Wins", "Draw"
                            style = MaterialTheme.typography.labelMedium,
                            fontSize = 20.sp
                        )
                        // Provides a dynamic content description for the turn indicator icon,
                        // aiding accessibility by announcing the current game state, considering AI mode.
                        val turnIconContentDescription = when {
                            winnerInfo?.winner == Player.X -> if (isAIMode) "You are the winner" else "Player X is the winner"
                            winnerInfo?.winner == Player.O -> if (isAIMode) "AI is the winner" else "Player O is the winner"
                            winnerInfo != null && winnerInfo?.winner == null -> "Game is a draw"
                            player1Turn -> if (isAIMode) "Your turn (Player X)" else "Player X's turn"
                            else -> if (isAIMode) "AI's turn (Player O)" else "Player O's turn"
                        }

                        // Display appropriate icon (X or O) based on whose turn it is or if there's a winner.
                        // If it's a draw, the ViewModel's turnDenotingText handles the "Draw" message,
                        // and typically no specific player icon is shown unless desired.
                        if (winnerInfo?.winner == Player.X || (winnerInfo == null && player1Turn)) {
                            Icon(
                                Icons.Default.Close, // Player X icon
                                contentDescription = turnIconContentDescription
                            )
                        } else if (winnerInfo?.winner == Player.O || (winnerInfo == null && !player1Turn)) {
                            Icon(
                                painterResource(R.drawable.player_2), // Player O icon
                                contentDescription = turnIconContentDescription
                            )
                        }
                        // In Infinite mode, a draw is usually just text; specific X-O-Handshake icons for draw are omitted for simplicity,
                        // relying on `turnDenotingText` from the ViewModel to state "Draw".
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
                            tint = playerXColor,
                            modifier = Modifier.padding(0.dp, 6.dp, 6.dp, 6.dp)
                        )
                        Text(
                            player1Wins.toString(), // Player X's win count
                            style = MaterialTheme.typography.labelMedium,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.numberOfWinsTextColor_x), // Specific color for X's score
                            modifier = Modifier
                                .background(
                                    colorResource(R.color.numberOfWinsBackgroundColor_x), // Background for X's score
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .padding(12.dp)
                        )
                        Text(
                            "-", // Separator between scores
                            style = MaterialTheme.typography.labelMedium,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.hyphenColor),
                            modifier = Modifier.padding(10.dp)
                        )
                        Icon(
                            painterResource(R.drawable.player_2),
                            contentDescription = "Player O score icon",
                            tint = playerOColor,
                            modifier = Modifier.padding(0.dp, 6.dp, 6.dp, 6.dp)
                        )
                        Text(
                            player2Wins.toString(), // Player O's win count
                            style = MaterialTheme.typography.labelMedium,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.numberOfWinsTextColor_o), // Specific color for O's score
                            modifier = Modifier
                                .background(
                                    colorResource(R.color.numberOfWinsBackgroundColor_o), // Background for O's score
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .padding(12.dp)
                        )
                    }
                }
            }

            // Button to reset the current round (clears the board but keeps scores and potentially AI difficulty).
            Button(
                onClick = {
                    viewModel.resetRound()
                    buttonCoordinates.clear() // Clear stored cell coordinates as the board is reset.
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

            // Button to reset all scores and start a completely new game (also resets AI difficulty to default).
            Button(
                onClick = {
                    viewModel.resetScores()
                    buttonCoordinates.clear() // Clear stored cell coordinates as the game and board are fully reset.
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
                    Icon(
                        Icons.Filled.Refresh,
                        contentDescription = "Refresh icon for reset scores"
                    )
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

