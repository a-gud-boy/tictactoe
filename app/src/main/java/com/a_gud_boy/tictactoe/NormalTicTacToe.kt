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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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


@SuppressLint("MutableCollectionMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NormalTicTacToePage(
    innerPadding: PaddingValues,
    viewModel: NormalTicTacToeViewModel = viewModel()
) {
    var showMenu by rememberSaveable { mutableStateOf(false) }
    val playerXColor = colorResource(R.color.red_x_icon)
    val playerOColor = colorResource(R.color.blue_o_icon)
    val isAIMode by viewModel.isAIMode.collectAsState()
    val currentDifficulty by viewModel.aiDifficulty.collectAsState()

    val player1Wins by viewModel.player1Wins.collectAsStateWithLifecycle()
    val player2Wins by viewModel.player2Wins.collectAsStateWithLifecycle()
    val player1Moves by viewModel.player1Moves.collectAsStateWithLifecycle()
    val player2Moves by viewModel.player2Moves.collectAsStateWithLifecycle()
    val winnerInfo by viewModel.winnerInfo.collectAsStateWithLifecycle()
    val player1Turn by viewModel.player1Turn.collectAsStateWithLifecycle()
    val turnDenotingText by viewModel.turnDenotingText.collectAsStateWithLifecycle()
    val resetButtonText by viewModel.resetButtonText.collectAsStateWithLifecycle()

    val iconSize = 70.dp
    val buttonCoordinates = remember { mutableStateMapOf<String, LayoutCoordinates>() }

    // Animation state for the winning line
    val lineAnimationProgress = remember { Animatable(0f) }
    // Store the ordered winning combination for animation
    val orderedWinningCombination = remember { mutableStateOf<List<String>>(emptyList()) }

    val context = LocalContext.current
    val soundManager = remember { SoundManager(context) }

    DisposableEffect(Unit) {
        onDispose {
            soundManager.release()
        }
    }

    val view = LocalView.current
    LaunchedEffect(winnerInfo) {
        if (winnerInfo != null) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            if (winnerInfo?.winner != null) {
                soundManager.playWinSound()
            } else { // Draw condition
                soundManager.playDrawSound()
            }
            orderedWinningCombination.value = winnerInfo!!.orderedWinningMoves

            lineAnimationProgress.snapTo(0f) // Reset before starting
            lineAnimationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 600) // Adjust duration as needed
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
            modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            ConstraintLayout(
                constraintSet = constraints,
                modifier = Modifier
                    .padding(20.dp, 0.dp, 20.dp, 20.dp)
                    .width(300.dp)
                    .height(300.dp)
                    .shadow(4.dp, shape = RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(colorResource(R.color.constraint_background))
                    .drawWithContent {
                        drawContent()
                        if (orderedWinningCombination.value.size >= 2 && lineAnimationProgress.value > 0f) {
                            val currentWinner = winnerInfo?.winner ?: return@drawWithContent // Should have a winner if combination is present

                            // The line should animate from the last move in the combination to the first
                            val animationStartButtonId = orderedWinningCombination.value.last()
                            val animationEndButtonId = orderedWinningCombination.value.first()

                            val animStartCoordinates = buttonCoordinates[animationStartButtonId]
                            val animEndCoordinates = buttonCoordinates[animationEndButtonId]

                            if (animStartCoordinates != null && animEndCoordinates != null) {
                                val animOriginalLineStart = Offset(
                                    animStartCoordinates.size.width / 2f + animStartCoordinates.positionInParent().x,
                                    animStartCoordinates.size.height / 2f + animStartCoordinates.positionInParent().y
                                )
                                val animOriginalLineEnd = Offset(
                                    animEndCoordinates.size.width / 2f + animEndCoordinates.positionInParent().x,
                                    animEndCoordinates.size.height / 2f + animEndCoordinates.positionInParent().y
                                )

                                // Interpolate the end point of the line based on animation progress
                                val animatedLineEnd = lerp(animOriginalLineStart, animOriginalLineEnd, lineAnimationProgress.value)

                                val lineExtensionPx = 30.dp.toPx()
                                val directionVector = animatedLineEnd - animOriginalLineStart // Vector based on animated end

                                val lineColor = when (currentWinner) {
                                    Player.X -> playerXColor
                                    Player.O -> playerOColor
                                }

                                if (directionVector.getDistanceSquared() == 0f && lineAnimationProgress.value < 1f) {
                                    // Avoid drawing a zero-length line unless animation is complete and it's a single point (should not happen for a line)
                                    // Or, if it's meant to draw from one point to itself (e.g. a very short line), handle as needed
                                    // For now, let's just draw if progress is full for such a case.
                                    if (lineAnimationProgress.value == 1f) {
                                         drawLine(
                                            color = lineColor.copy(alpha = 0.6f),
                                            start = animOriginalLineStart,
                                            end = animatedLineEnd, // which is animOriginalLineStart if vector is zero
                                            strokeWidth = 5.dp.toPx(),
                                            cap = StrokeCap.Round
                                        )
                                    }
                                } else if (directionVector.getDistanceSquared() > 0f) { // Only draw if there's a direction
                                    val normalizedDirection = directionVector / directionVector.getDistance()
                                    // Extend the line from the *start* of the animation (most recent move)
                                    // and from the *animated end point* (towards the oldest move)
                                    val extendedLineStart = animOriginalLineStart - (normalizedDirection * lineExtensionPx)
                                    val extendedAnimatedLineEnd = animatedLineEnd + (normalizedDirection * lineExtensionPx)

                                    drawLine(
                                        color = lineColor.copy(alpha = 0.6f),
                                        start = extendedLineStart,
                                        end = extendedAnimatedLineEnd,
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
                        isOldMove = false, // In Normal TicTacToe, moves are not "old" or dimmed
                        iconSize = iconSize,
                        buttonId = buttonId, // Pass buttonId for accessibility
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                            // TODO: Check if move is valid before playing sound,
                            // or play sound optimistically and handle invalid move UI separately.
                            // For now, play sound before ViewModel action.
                            soundManager.playMoveSound()
                            viewModel.onButtonClick(buttonId)
                        }
                    )
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
                        // Dynamic content description for turn denoting icons
                        val turnIconContentDescription = when {
                            winnerInfo?.winner == Player.X -> "Player X is the winner"
                            winnerInfo?.winner == Player.O -> "Player O is the winner"
                            winnerInfo != null && winnerInfo?.winner == null -> "Game is a draw"
                            player1Turn -> "Player X's turn"
                            else -> "Player O's turn"
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
                        } else { // Draw case or other states where both icons might be shown or a general state
                            // This specific draw case shows X, handshake, O.
                            // The overall state is "Game is a draw", individual icons are decorative in this specific combined view.
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Player X icon for draw display" // Or null if purely decorative next to text
                            )
                            Text("\uD83E\uDD1D", Modifier.width(24.dp)) // Placeholder for icon space if needed, decorative
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
                            tint = colorResource(R.color.red_x_icon),
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
                            modifier = Modifier.padding(10.dp)
                        )
                        Icon(
                            painterResource(R.drawable.player_2),
                            contentDescription = "Player O score icon",
                            tint = colorResource(R.color.blue_o_icon),
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
                    Icon(Icons.Filled.Refresh, contentDescription = "Refresh icon for new or reset round")
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



