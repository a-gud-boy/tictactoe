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
// Removed DisposableEffect as it's not used
import androidx.compose.runtime.LaunchedEffect // Ensure this is imported
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Canvas
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext // Not used directly, consider removing
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
    viewModel: InfiniteTicTacToeViewModel = viewModel(factory = LocalViewModelFactory.current) // Use central factory
) {
    // LaunchedEffect to refresh settings when the composable enters the composition
    LaunchedEffect(Unit) { // Using Unit ensures this runs once when the composable is first displayed
        viewModel.refreshSettingsFromManager()
    }

    val volume = 1.0f

    val playerXColor = colorResource(R.color.red_x_icon)
    val playerOColor = colorResource(R.color.blue_o_icon)

    val player1Wins by viewModel.player1Wins.collectAsStateWithLifecycle()
    val player2Wins by viewModel.player2Wins.collectAsStateWithLifecycle()
    val player1Moves by viewModel.player1Moves.collectAsStateWithLifecycle()
    val player2Moves by viewModel.player2Moves.collectAsStateWithLifecycle()

    val p1VisibleMoves =
        player1Moves.takeLast(InfiniteTicTacToeViewModel.MAX_VISIBLE_MOVES_PER_PLAYER)
    val p2VisibleMoves =
        player2Moves.takeLast(InfiniteTicTacToeViewModel.MAX_VISIBLE_MOVES_PER_PLAYER)
    val winnerInfo by viewModel.winnerInfo.collectAsStateWithLifecycle()
    val player1Turn by viewModel.player1Turn.collectAsStateWithLifecycle()
    val turnDenotingText by viewModel.turnDenotingText.collectAsStateWithLifecycle()
    val resetButtonText by viewModel.resetButtonText.collectAsStateWithLifecycle()
    val isAIMode by viewModel.isAIMode.collectAsStateWithLifecycle()

    val iconSize = 70.dp
    val buttonCoordinates = remember { mutableStateMapOf<String, LayoutCoordinates>() }
    val view = LocalView.current
    val lineAnimationProgress = remember { Animatable(0f) }
    val orderedWinningCombination = remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(winnerInfo) {
        if (winnerInfo != null) {
            HapticFeedbackManager.performHapticFeedback(view, HapticFeedbackConstants.CONFIRM)
            orderedWinningCombination.value = winnerInfo!!.orderedWin
            lineAnimationProgress.snapTo(0f)
            lineAnimationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 600)
            )
        } else {
            lineAnimationProgress.snapTo(0f)
            orderedWinningCombination.value = emptyList()
        }
    }

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

        constrain(button1) { top.linkTo(parent.top, margin = margin); start.linkTo(parent.start, margin = margin); end.linkTo(button2.start, margin = margin); bottom.linkTo(button4.top, margin = margin) }
        constrain(button2) { top.linkTo(parent.top, margin = margin); start.linkTo(button1.end, margin = margin); end.linkTo(button3.start, margin = margin); bottom.linkTo(button5.top, margin = margin) }
        constrain(button3) { top.linkTo(parent.top, margin = margin); start.linkTo(button2.end, margin = margin); end.linkTo(parent.end, margin = margin); bottom.linkTo(button6.top, margin = margin) }
        constrain(button4) { top.linkTo(button1.bottom, margin = margin); start.linkTo(parent.start, margin = margin); end.linkTo(button5.start, margin = margin); bottom.linkTo(button7.top, margin = margin) }
        constrain(button5) { top.linkTo(button2.bottom, margin = margin); start.linkTo(button4.end, margin = margin); end.linkTo(button6.start, margin = margin); bottom.linkTo(button8.top, margin = margin) }
        constrain(button6) { top.linkTo(button3.bottom, margin = margin); start.linkTo(button5.end, margin = margin); end.linkTo(parent.end, margin = margin); bottom.linkTo(button9.top, margin = margin) }
        constrain(button7) { top.linkTo(button4.bottom, margin = margin); start.linkTo(parent.start, margin = margin); end.linkTo(button8.start, margin = margin); bottom.linkTo(parent.bottom, margin = margin) }
        constrain(button8) { top.linkTo(button5.bottom, margin = margin); start.linkTo(button7.end, margin = margin); end.linkTo(button9.start, margin = margin); bottom.linkTo(parent.bottom, margin = margin) }
        constrain(button9) { top.linkTo(button6.bottom, margin = margin); start.linkTo(button8.end, margin = margin); end.linkTo(parent.end, margin = margin); bottom.linkTo(parent.bottom, margin = margin) }
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
            Box(
                modifier = Modifier
                    .padding(20.dp, 10.dp, 20.dp, 20.dp)
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
                    val buttonIds = List(9) { i -> "button${i + 1}" }
                    buttonIds.forEach { buttonId ->
                        val cellPlayer: Player? = remember(p1VisibleMoves, p2VisibleMoves, buttonId) {
                            if (p2VisibleMoves.contains(buttonId)) Player.O
                            else if (p1VisibleMoves.contains(buttonId)) Player.X
                            else null
                        }
                        val isOldMove = remember(cellPlayer, player1Turn, p1VisibleMoves, p2VisibleMoves, buttonId) {
                            cellPlayer != null &&
                                ((player1Turn && cellPlayer == Player.X && p1VisibleMoves.size == InfiniteTicTacToeViewModel.MAX_VISIBLE_MOVES_PER_PLAYER && p1VisibleMoves.firstOrNull() == buttonId) ||
                                        (!player1Turn && cellPlayer == Player.O && p2VisibleMoves.size == InfiniteTicTacToeViewModel.MAX_VISIBLE_MOVES_PER_PLAYER && p2VisibleMoves.firstOrNull() == buttonId))
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
                        isOldMove = isOldMove,
                        iconSize = iconSize,
                        buttonId = buttonId,
                        onClick = remember(buttonId, viewModel, view) { // Consider removing viewModel from key if onButtonClick doesn't change
                            {
                                HapticFeedbackManager.performHapticFeedback(
                                    view,
                                    HapticFeedbackConstants.VIRTUAL_KEY
                                )
                                viewModel.onButtonClick(buttonId)
                            }
                        })
                    }
                }
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (orderedWinningCombination.value.size >= 2 && lineAnimationProgress.value > 0f) {
                        val currentWinner = winnerInfo?.winner
                            ?: return@Canvas
                        val lineAppearStartButtonId =
                            orderedWinningCombination.value.first()
                        val lineAppearEndButtonId =
                            orderedWinningCombination.value.last()
                        val lineStartCellLayoutCoordinates =
                            buttonCoordinates[lineAppearStartButtonId]
                        val lineEndCellLayoutCoordinates = buttonCoordinates[lineAppearEndButtonId]

                        if (lineStartCellLayoutCoordinates != null && lineEndCellLayoutCoordinates != null) {
                            val actualLineStartPoint = Offset(
                                lineStartCellLayoutCoordinates.size.width / 2f + lineStartCellLayoutCoordinates.positionInParent().x,
                                lineStartCellLayoutCoordinates.size.height / 2f + lineStartCellLayoutCoordinates.positionInParent().y
                            )
                            val actualLineEndPoint = Offset(
                                lineEndCellLayoutCoordinates.size.width / 2f + lineEndCellLayoutCoordinates.positionInParent().x,
                                lineEndCellLayoutCoordinates.size.height / 2f + lineEndCellLayoutCoordinates.positionInParent().y
                            )
                            val lineColor = when (currentWinner) {
                                Player.X -> playerXColor
                                Player.O -> playerOColor
                            }
                            val overallDirectionVector =
                                actualLineEndPoint - actualLineStartPoint
                            val normalizedOverallDirection =
                                if (overallDirectionVector.getDistance() > 0) {
                                    overallDirectionVector / overallDirectionVector.getDistance()
                                } else {
                                    Offset(0f, 0f)
                                }
                            val lineExtensionPx = 30.dp.toPx()
                            val extendedVisualLineStart =
                                actualLineStartPoint - (normalizedOverallDirection * lineExtensionPx)
                            val extendedVisualLineEndTarget =
                                actualLineEndPoint + (normalizedOverallDirection * lineExtensionPx)
                            val finalAnimatedEnd = lerp(
                                extendedVisualLineStart,
                                extendedVisualLineEndTarget,
                                lineAnimationProgress.value
                            )
                            drawLine(
                                color = lineColor.copy(alpha = 0.6f),
                                start = extendedVisualLineStart,
                                end = finalAnimatedEnd,
                                strokeWidth = 5.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }
            }
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
                        val turnIconContentDescription = when {
                            winnerInfo?.winner == Player.X -> if (isAIMode) "You are the winner" else "Player X is the winner"
                            winnerInfo?.winner == Player.O -> if (isAIMode) "AI is the winner" else "Player O is the winner"
                            winnerInfo != null && winnerInfo?.winner == null -> "Game is a draw" // Should not happen in Infinite
                            player1Turn -> if (isAIMode) "Your turn (Player X)" else "Player X's turn"
                            else -> if (isAIMode) "AI's turn (Player O)" else "Player O's turn"
                        }
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
                            contentDescription = "Player X score icon",
                            tint = playerXColor,
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
                            "-",
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
            Button(
                onClick = {
                    viewModel.resetRound()
                    buttonCoordinates.clear()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.blue_o_icon),
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
                        text = resetButtonText,
                        modifier = Modifier
                            .padding(start = 8.dp)
                    )
                }
            }
            Button(
                onClick = {
                    viewModel.resetScores()
                    buttonCoordinates.clear()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.resetScoresButtonBackground),
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
