package com.a_gud_boy.tictactoe

import android.annotation.SuppressLint
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
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

    val player1Wins by viewModel.player1Wins.collectAsStateWithLifecycle()
    val player2Wins by viewModel.player2Wins.collectAsStateWithLifecycle()
    val player1Moves by viewModel.player1Moves.collectAsStateWithLifecycle()
    val player2Moves by viewModel.player2Moves.collectAsStateWithLifecycle()
    val winnerInfo by viewModel.winnerInfo.collectAsStateWithLifecycle()
    val player1Turn by viewModel.player1Turn.collectAsStateWithLifecycle()
    val turnDenotingText by viewModel.turnDenotingText.collectAsStateWithLifecycle()
    val gameStarted by viewModel.gameStarted.collectAsStateWithLifecycle()
    val resetButtonText by viewModel.resetButtonText.collectAsStateWithLifecycle()
    // isGameConcluded is implicitly handled by winnerInfo and gameStarted states in ViewModel

    val iconSize = 70.dp
    val buttonCoordinates = remember { mutableStateMapOf<String, LayoutCoordinates>() }

    val constraints = ConstraintSet {
        // Id for all the buttons
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
            .padding(20.dp)
    ) {
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
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
                    .drawWithContent {
                        drawContent() // Draw buttons first

                        winnerInfo?.let { currentWinnerInfo -> // Renamed to avoid conflict
                            val winningButtonIds = currentWinnerInfo.combination.toList()
                            // Ensure there are exactly 3 buttons for a standard line,
                            // though the logic in VM ensures combination is valid.
                            if (winningButtonIds.size >= 2) { // Need at least 2 points to draw a line
                                // For simplicity, we'll use the first and last button of the combination
                                // for drawing the line, assuming combinations are ordered or this is visually acceptable.
                                // A more robust approach might require specific start/end points from the combination.
                                val startButtonId = winningButtonIds.first()
                                val endButtonId = winningButtonIds.last()

                                val startCoordinates = buttonCoordinates[startButtonId]
                                val endCoordinates = buttonCoordinates[endButtonId]

                                if (startCoordinates != null && endCoordinates != null) {
                                    // 1. Get original center points (relative to this ConstraintLayout)
                                    val originalLineStart = Offset(
                                        startCoordinates.size.width / 2f + startCoordinates.positionInParent().x,
                                        startCoordinates.size.height / 2f + startCoordinates.positionInParent().y
                                    )
                                    val originalLineEnd = Offset(
                                        endCoordinates.size.width / 2f + endCoordinates.positionInParent().x,
                                        endCoordinates.size.height / 2f + endCoordinates.positionInParent().y
                                    )

                                    val lineExtensionLengthDp = 30.dp

                                    // Convert extension length from Dp to Px
                                    val lineExtensionPx = lineExtensionLengthDp.toPx()

                                    // 2. Calculate direction vector (from start to end)
                                    val directionVector =
                                        originalLineEnd - originalLineStart

                                    // Check for zero vector to avoid division by zero if buttons are at the same spot
                                    if (directionVector.getDistanceSquared() == 0f) {
                                        // Buttons are at the same spot, draw a point or very short line if needed
                                        // Or simply draw the original line (which would be a point)
                                        drawLine(
                                            color = Color.Black.copy(alpha = 0.6f),
                                            start = originalLineStart,
                                            end = originalLineEnd,
                                            strokeWidth = 5.dp.toPx(), // Your chosen thickness
                                            cap = StrokeCap.Round
                                        )
                                        return@drawWithContent // Exit early
                                    }

                                    // 3. Normalize the direction vector (to get a unit vector)
                                    val normalizedDirection =
                                        directionVector / directionVector.getDistance()

                                    // 4. Calculate new extended start and end points
                                    val extendedLineStart =
                                        originalLineStart - (normalizedDirection * lineExtensionPx)
                                    val extendedLineEnd =
                                        originalLineEnd + (normalizedDirection * lineExtensionPx)

                                    drawLine(
                                        color = Color.Black.copy(alpha = 0.6f),
                                        start = extendedLineStart, // Use extended start
                                        end = extendedLineEnd,     // Use extended end
                                        strokeWidth = 5.dp.toPx(), // Your chosen thickness
                                        cap = StrokeCap.Round
                                    )
                                }
                            }
                        }
                    }
            ) {
                // Create a list of button IDs for iteration
                val buttonIds = List(9) { i -> "button${i + 1}" }

                // Collect ViewModel states once for the loop
                val currentP1Moves = player1Moves
                val currentP2Moves = player2Moves
                val isP1Turn = player1Turn
                val gameIsActive =
                    gameStarted && (winnerInfo == null) // Game is active if started and no winner

                buttonIds.forEach { buttonId ->
                    // Determine player for the cell (current visible moves)
                    val p1VisibleOnlyMoves =
                        currentP1Moves.takeLast(InfiniteTicTacToeViewModel.MAX_VISIBLE_MOVES_PER_PLAYER)
                    val p2VisibleOnlyMoves =
                        currentP2Moves.takeLast(InfiniteTicTacToeViewModel.MAX_VISIBLE_MOVES_PER_PLAYER)

                    val cellPlayer: Player? = when {
                        p1VisibleOnlyMoves.contains(buttonId) -> Player.X
                        p2VisibleOnlyMoves.contains(buttonId) -> Player.O
                        else -> null // Not a currently visible move
                    }

                    // Determine if the cell should be dimmed (about to disappear effect)
                    val isDimmedAboutToDisappear = gameIsActive && cellPlayer?.let { player ->
                        when (player) {
                            Player.X ->
                                isP1Turn &&
                                        currentP1Moves.size == InfiniteTicTacToeViewModel.MAX_VISIBLE_MOVES_PER_PLAYER &&
                                        currentP1Moves.firstOrNull() == buttonId

                            Player.O ->
                                !isP1Turn &&
                                        currentP2Moves.size == InfiniteTicTacToeViewModel.MAX_VISIBLE_MOVES_PER_PLAYER &&
                                        currentP2Moves.firstOrNull() == buttonId
                        }
                    } ?: false

                    // In the previous refactor, `isOldMove` was used to dim any icon not in the visible list.
                    // That general dimming of past, non-visible moves is removed to focus on the "about to disappear" effect only.
                    // If general dimming of all non-visible past moves is also desired, that logic would need to be combined.
                    // For this task, we focus only on the "about to disappear" for a *visible* mark.

                    TicTacToeCell(
                        modifier = Modifier
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .width(80.dp)
                            .height(80.dp)
                            .layoutId(buttonId)
                            .onGloballyPositioned { coordinates ->
                                buttonCoordinates[buttonId] = coordinates
                            },
                        player = cellPlayer, // This is the player whose mark is currently visible in the cell
                        isOldMove = isDimmedAboutToDisappear, // Pass the "about to disappear" state
                        iconSize = iconSize,
                        onClick = { viewModel.onButtonClick(buttonId) }
                    )
                }
            }

            // turnDenotingText is now a collected state from ViewModel
            // The Card UI below will use the collected turnDenotingText

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
                    // Denotes the turn
                    Row(
                        modifier = Modifier
                            .padding(top = 10.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "$turnDenotingText (", // Use collected state
                            style = MaterialTheme.typography.labelMedium,
                            fontSize = 20.sp
                        )
                        // Logic for icon based on turn/winner
                        if (player1Turn || winnerInfo?.winner == Player.X) { // Check player1Turn from VM
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Turn Denoting Icon"
                            )
                        } else {
                            Icon(
                                painterResource(R.drawable.player_2),
                                contentDescription = "Turn Denoting Icon"
                            )
                        }
                        Text(
                            text = ")",
                            style = MaterialTheme.typography.labelMedium,
                            fontSize = 20.sp
                        )
                    }
                    Row(
                        modifier = Modifier
                            .padding(bottom = 10.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Cross",
                            tint = colorResource(R.color.red_x_icon),
                            modifier = Modifier.padding(0.dp, 6.dp, 6.dp, 6.dp)
                        )
                        Text(
                            player1Wins.toString(), // Use collected state
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
                            modifier = Modifier.padding(10.dp)
                        )
                        Icon(
                            painterResource(R.drawable.player_2),
                            contentDescription = "Circle",
                            tint = colorResource(R.color.blue_o_icon),
                            modifier = Modifier.padding(0.dp, 6.dp, 6.dp, 6.dp)
                        )
                        Text(
                            player2Wins.toString(), // Use collected state
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




            Button(
                onClick = {
                    viewModel.resetRound()
                    buttonCoordinates.clear() // Still need to clear local UI coordinates if kept here
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(10.dp), // Apply the shape directly to the Button
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.blue_o_icon), // Set the button's container color
                    contentColor = Color.White // Optionally set the color for the text inside
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = "New Round")
                    Text(
                        text = resetButtonText, // Use collected state
                        modifier = Modifier
                            .padding(start = 8.dp)
                    )
                }
            }

            Button(
                onClick = {
                    viewModel.resetScores()
                    buttonCoordinates.clear() // Also clear coordinates on full score reset
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(10.dp), // Apply the shape directly to the Button
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.resetScoresButtonBackground), // Set the button's container color
                    contentColor = colorResource(R.color.darkTextColor) // Optionally set the color for the text inside
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = "New Round")
                    Text(
                        text = "Reset Scores",
                        modifier = Modifier
                            .padding(start = 8.dp)
                    )
                }
            }
        }
        // Winning combinations and checking logic are now in the ViewModel.
        // The UI will react to changes in viewModel.winnerInfo.
    }
}
// WinnerInfo data class is now in InfiniteTicTacToeViewModel.kt
