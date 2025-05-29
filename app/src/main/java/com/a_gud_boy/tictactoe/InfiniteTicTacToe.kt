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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
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

@SuppressLint("MutableCollectionMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfiniteTicTacToePage(innerPadding: PaddingValues) {

    var player1Wins by rememberSaveable { mutableIntStateOf(0) }
    var player2Wins by rememberSaveable { mutableIntStateOf(0) }

    val iconSize = 70.dp

    // At the top of your MainPage composable (or in a ViewModel)
// For Player 1 (X)
    var player1Moves by rememberSaveable { mutableStateOf(mutableListOf<String>()) }
// Stores the layoutId of the buttons player 1 clicked, in order. e.g., ["button1", "button5", "button9"]
// For Player 2 (O)
    var player2Moves by rememberSaveable { mutableStateOf(mutableListOf<String>()) }
// Stores the layoutId of the buttons player 2 clicked, in order.
    val maxVisibleMovesPerPlayer = 3 // Only show the last 3 moves for each player

    // At the top of your MainPage composable (or in a ViewModel)
// ...

    val winnerInfoSaver = Saver<WinnerInfo?, Any>(
        save = { winnerInfo ->
            winnerInfo?.let {
                // Convert to something Bundle-able: List of player name and list of combination strings
                listOf(it.playerName, it.combination.toList())
            }
        },
        restore = { saved ->
            if (saved is List<*>) {
                val playerName = saved[0] as String

                @Suppress("UNCHECKED_CAST")
                val combinationList = saved[1] as List<String>
                WinnerInfo(playerName, combinationList.toSet())
            } else {
                null
            }
        }
    )

    var winnerInfo by rememberSaveable(stateSaver = winnerInfoSaver) {
        mutableStateOf(
            null
        )
    }


// We'll define WinnerInfoSaver below
// To store coordinates of all buttons for drawing the line
    val buttonCoordinates = remember { mutableStateMapOf<String, LayoutCoordinates>() }

    // True for player 1 and false for player 2
    var player1turn by rememberSaveable {
        mutableStateOf(true)
    }

    var turnDenotingText by rememberSaveable {
        mutableStateOf("Player 1's Turn")
    }

    var gameStarted by rememberSaveable {
        mutableStateOf(true)
    }

    var resetButtonText by rememberSaveable {
        mutableStateOf("Reset Round")
    }

    var isGameConcluded by rememberSaveable {
        mutableStateOf(false)
    }

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
        Column(
            modifier = Modifier.fillMaxSize(),
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

                        winnerInfo?.let { info ->
                            val winningButtonIds = info.combination.toList()
                            if (winningButtonIds.size == 3) {
                                val startButtonId = winningButtonIds[0]
                                val endButtonId = winningButtonIds[2]

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
                IconButton(
                    onClick = {
                        if (gameStarted) {
                            val buttonId = "button1" // The layoutId of this button

                            // Check if this button is already an active move for either player
                            val isAlreadyPlayedByPlayer1 =
                                player1Moves.takeLast(maxVisibleMovesPerPlayer)
                                    .contains(buttonId)
                            val isAlreadyPlayedByPlayer2 =
                                player2Moves.takeLast(maxVisibleMovesPerPlayer)
                                    .contains(buttonId)

                            if (!isAlreadyPlayedByPlayer1 && !isAlreadyPlayedByPlayer2) {
                                if (player1turn) {
                                    player1Moves.add(buttonId)
                                    if (player1Moves.size > maxVisibleMovesPerPlayer) {
                                        player1Moves.removeAt(0) // Remove the oldest move
                                    }
                                } else {
                                    player2Moves.add(buttonId)
                                    if (player2Moves.size > maxVisibleMovesPerPlayer) {
                                        player2Moves.removeAt(0) // Remove the oldest move
                                    }
                                }
                                player1turn = !player1turn
                            }
                        }
                    },
                    modifier = Modifier
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .width(80.dp)
                        .height(80.dp)
                        .layoutId("button1")
                        .onGloballyPositioned { coordinates ->
                            buttonCoordinates["button1"] = coordinates
                        },
                    interactionSource = remember { MutableInteractionSource() }, // Added
                    indication = null, // Added
                ) {
                    // Determine what to display based on the lists
                    val buttonId = "button1"
                    val isPlayer1Move =
                        player1Moves.takeLast(maxVisibleMovesPerPlayer).contains(buttonId)
                    val isPlayer2Move =
                        player2Moves.takeLast(maxVisibleMovesPerPlayer).contains(buttonId)

                    val isPlayer1OldMove =
                        if (player1Moves.size >= 3 && gameStarted && player1turn) player1Moves[0] == buttonId else false
                    val isPlayer2OldMove =
                        if (player2Moves.size >= 3 && gameStarted && !player1turn) player2Moves[0] == buttonId else false

                    if (isPlayer1Move) {
                        val scalePlayer1 = remember { Animatable(0.5f) }
                        val alphaPlayer1 = remember { Animatable(0f) }
                        LaunchedEffect(key1 = isPlayer1Move) {
                            if (isPlayer1Move) {
                                scalePlayer1.animateTo(1f, animationSpec = tween(300))
                                alphaPlayer1.animateTo(1f, animationSpec = tween(300))
                            } else {
                                scalePlayer1.snapTo(0.5f)
                                alphaPlayer1.snapTo(0f)
                            }
                        }
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Player 1 move",
                            tint = if (isPlayer1OldMove) Color.Black.copy(0.4f) else Color.Black,
                            modifier = Modifier
                                .width(iconSize)
                                .height(iconSize)
                                .scale(scalePlayer1.value)
                                .alpha(alphaPlayer1.value)
                        )
                    } else if (isPlayer2Move) {
                        val scalePlayer2 = remember { Animatable(0.5f) }
                        val alphaPlayer2 = remember { Animatable(0f) }
                        LaunchedEffect(key1 = isPlayer2Move) {
                            if (isPlayer2Move) {
                                scalePlayer2.animateTo(1f, animationSpec = tween(300))
                                alphaPlayer2.animateTo(1f, animationSpec = tween(300))
                            } else {
                                scalePlayer2.snapTo(0.5f)
                                alphaPlayer2.snapTo(0f)
                            }
                        }
                        Icon(
                            painter = painterResource(R.drawable.player_2),
                            contentDescription = "Player 2 move",
                            tint = if (isPlayer2OldMove) Color.Black.copy(0.4f) else LocalContentColor.current,
                            modifier = Modifier
                                .width(iconSize)
                                .height(iconSize)
                                .scale(scalePlayer2.value)
                                .alpha(alphaPlayer2.value)
                        )
                    }
                    // Else, display nothing (empty button)
                }

                IconButton(
                    onClick = {
                        if (gameStarted) {
                            val buttonId = "button2" // The layoutId of this button

                            // Check if this button is already an active move for either player
                            val isAlreadyPlayedByPlayer1 =
                                player1Moves.takeLast(maxVisibleMovesPerPlayer)
                                    .contains(buttonId)
                            val isAlreadyPlayedByPlayer2 =
                                player2Moves.takeLast(maxVisibleMovesPerPlayer)
                                    .contains(buttonId)

                            if (!isAlreadyPlayedByPlayer1 && !isAlreadyPlayedByPlayer2) {
                                if (player1turn) {
                                    player1Moves.add(buttonId)
                                    if (player1Moves.size > maxVisibleMovesPerPlayer) {
                                        player1Moves.removeAt(0) // Remove the oldest move
                                    }
                                } else {
                                    player2Moves.add(buttonId)
                                    if (player2Moves.size > maxVisibleMovesPerPlayer) {
                                        player2Moves.removeAt(0) // Remove the oldest move
                                    }
                                }
                                player1turn = !player1turn
                            }
                        }
                    },
                    modifier = Modifier
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .layoutId("button2")
                        .width(80.dp)
                        .height(80.dp)
                        .onGloballyPositioned { coordinates ->
                            buttonCoordinates["button2"] = coordinates
                        },
                    interactionSource = remember { MutableInteractionSource() }, // Added
                    indication = null, // Added
                ) {
                    // Determine what to display based on the lists
                    val buttonId = "button2"
                    val isPlayer1Move =
                        player1Moves.takeLast(maxVisibleMovesPerPlayer).contains(buttonId)
                    val isPlayer2Move =
                        player2Moves.takeLast(maxVisibleMovesPerPlayer).contains(buttonId)

                    val isPlayer1OldMove =
                        if (player1Moves.size >= 3 && gameStarted && player1turn) player1Moves[0] == buttonId else false
                    val isPlayer2OldMove =
                        if (player2Moves.size >= 3 && gameStarted && !player1turn) player2Moves[0] == buttonId else false

                    if (isPlayer1Move) {
                        val scalePlayer1 = remember { Animatable(0.5f) }
                        val alphaPlayer1 = remember { Animatable(0f) }
                        LaunchedEffect(key1 = isPlayer1Move) {
                            if (isPlayer1Move) {
                                scalePlayer1.animateTo(1f, animationSpec = tween(300))
                                alphaPlayer1.animateTo(1f, animationSpec = tween(300))
                            } else {
                                scalePlayer1.snapTo(0.5f)
                                alphaPlayer1.snapTo(0f)
                            }
                        }
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Player 1 move",
                            tint = if (isPlayer1OldMove) Color.Black.copy(0.4f) else Color.Black,
                            modifier = Modifier
                                .width(iconSize)
                                .height(iconSize)
                                .scale(scalePlayer1.value)
                                .alpha(alphaPlayer1.value)
                        )
                    } else if (isPlayer2Move) {
                        val scalePlayer2 = remember { Animatable(0.5f) }
                        val alphaPlayer2 = remember { Animatable(0f) }
                        LaunchedEffect(key1 = isPlayer2Move) {
                            if (isPlayer2Move) {
                                scalePlayer2.animateTo(1f, animationSpec = tween(300))
                                alphaPlayer2.animateTo(1f, animationSpec = tween(300))
                            } else {
                                scalePlayer2.snapTo(0.5f)
                                alphaPlayer2.snapTo(0f)
                            }
                        }
                        Icon(
                            painter = painterResource(R.drawable.player_2),
                            contentDescription = "Player 2 move",
                            tint = if (isPlayer2OldMove) Color.Black.copy(0.4f) else LocalContentColor.current,
                            modifier = Modifier
                                .width(iconSize)
                                .height(iconSize)
                                .scale(scalePlayer2.value)
                                .alpha(alphaPlayer2.value)
                        )
                    }
                    // Else, display nothing (empty button)
                }

                IconButton(
                    onClick = {
                        if (gameStarted) {
                            val buttonId = "button3" // The layoutId of this button

                            // Check if this button is already an active move for either player
                            val isAlreadyPlayedByPlayer1 =
                                player1Moves.takeLast(maxVisibleMovesPerPlayer)
                                    .contains(buttonId)
                            val isAlreadyPlayedByPlayer2 =
                                player2Moves.takeLast(maxVisibleMovesPerPlayer)
                                    .contains(buttonId)

                            if (!isAlreadyPlayedByPlayer1 && !isAlreadyPlayedByPlayer2) {
                                if (player1turn) {
                                    player1Moves.add(buttonId)
                                    if (player1Moves.size > maxVisibleMovesPerPlayer) {
                                        player1Moves.removeAt(0) // Remove the oldest move
                                    }
                                } else {
                                    player2Moves.add(buttonId)
                                    if (player2Moves.size > maxVisibleMovesPerPlayer) {
                                        player2Moves.removeAt(0) // Remove the oldest move
                                    }
                                }
                                player1turn = !player1turn
                            }
                        }
                    },
                    modifier = Modifier
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .layoutId("button3")
                        .width(80.dp)
                        .height(80.dp)
                        .onGloballyPositioned { coordinates ->
                            buttonCoordinates["button3"] = coordinates
                        },
                    interactionSource = remember { MutableInteractionSource() }, // Added
                    indication = null, // Added
                ) {
                    // Determine what to display based on the lists
                    val buttonId = "button3"
                    val isPlayer1Move =
                        player1Moves.takeLast(maxVisibleMovesPerPlayer).contains(buttonId)
                    val isPlayer2Move =
                        player2Moves.takeLast(maxVisibleMovesPerPlayer).contains(buttonId)

                    val isPlayer1OldMove =
                        if (player1Moves.size >= 3 && gameStarted && player1turn) player1Moves[0] == buttonId else false
                    val isPlayer2OldMove =
                        if (player2Moves.size >= 3 && gameStarted && !player1turn) player2Moves[0] == buttonId else false

                    if (isPlayer1Move) {
                        val scalePlayer1 = remember { Animatable(0.5f) }
                        val alphaPlayer1 = remember { Animatable(0f) }
                        LaunchedEffect(key1 = isPlayer1Move) {
                            if (isPlayer1Move) {
                                scalePlayer1.animateTo(1f, animationSpec = tween(300))
                                alphaPlayer1.animateTo(1f, animationSpec = tween(300))
                            } else {
                                scalePlayer1.snapTo(0.5f)
                                alphaPlayer1.snapTo(0f)
                            }
                        }
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Player 1 move",
                            tint = if (isPlayer1OldMove) Color.Black.copy(0.4f) else Color.Black,
                            modifier = Modifier
                                .width(iconSize)
                                .height(iconSize)
                                .scale(scalePlayer1.value)
                                .alpha(alphaPlayer1.value)
                        )
                    } else if (isPlayer2Move) {
                        val scalePlayer2 = remember { Animatable(0.5f) }
                        val alphaPlayer2 = remember { Animatable(0f) }
                        LaunchedEffect(key1 = isPlayer2Move) {
                            if (isPlayer2Move) {
                                scalePlayer2.animateTo(1f, animationSpec = tween(300))
                                alphaPlayer2.animateTo(1f, animationSpec = tween(300))
                            } else {
                                scalePlayer2.snapTo(0.5f)
                                alphaPlayer2.snapTo(0f)
                            }
                        }
                        Icon(
                            painter = painterResource(R.drawable.player_2),
                            contentDescription = "Player 2 move",
                            tint = if (isPlayer2OldMove) Color.Black.copy(0.4f) else LocalContentColor.current,
                            modifier = Modifier
                                .width(iconSize)
                                .height(iconSize)
                                .scale(scalePlayer2.value)
                                .alpha(alphaPlayer2.value)
                        )
                    }
                    // Else, display nothing (empty button)
                }

                IconButton(
                    onClick = {
                        if (gameStarted) {
                            val buttonId = "button4" // The layoutId of this button

                            // Check if this button is already an active move for either player
                            val isAlreadyPlayedByPlayer1 =
                                player1Moves.takeLast(maxVisibleMovesPerPlayer)
                                    .contains(buttonId)
                            val isAlreadyPlayedByPlayer2 =
                                player2Moves.takeLast(maxVisibleMovesPerPlayer)
                                    .contains(buttonId)

                            if (!isAlreadyPlayedByPlayer1 && !isAlreadyPlayedByPlayer2) {
                                if (player1turn) {
                                    player1Moves.add(buttonId)
                                    if (player1Moves.size > maxVisibleMovesPerPlayer) {
                                        player1Moves.removeAt(0) // Remove the oldest move
                                    }
                                } else {
                                    player2Moves.add(buttonId)
                                    if (player2Moves.size > maxVisibleMovesPerPlayer) {
                                        player2Moves.removeAt(0) // Remove the oldest move
                                    }
                                }
                                player1turn = !player1turn
                            }
                        }
                    },
                    modifier = Modifier
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .layoutId("button4")
                        .width(80.dp)
                        .height(80.dp)
                        .onGloballyPositioned { coordinates ->
                            buttonCoordinates["button4"] = coordinates
                        },
                    interactionSource = remember { MutableInteractionSource() }, // Added
                    indication = null, // Added
                ) {
                    // Determine what to display based on the lists
                    val buttonId = "button4"
                    val isPlayer1Move =
                        player1Moves.takeLast(maxVisibleMovesPerPlayer).contains(buttonId)
                    val isPlayer2Move =
                        player2Moves.takeLast(maxVisibleMovesPerPlayer).contains(buttonId)

                    val isPlayer1OldMove =
                        if (player1Moves.size >= 3 && gameStarted && player1turn) player1Moves[0] == buttonId else false
                    val isPlayer2OldMove =
                        if (player2Moves.size >= 3 && gameStarted && !player1turn) player2Moves[0] == buttonId else false

                    if (isPlayer1Move) {
                        val scalePlayer1 = remember { Animatable(0.5f) }
                        val alphaPlayer1 = remember { Animatable(0f) }
                        LaunchedEffect(key1 = isPlayer1Move) {
                            if (isPlayer1Move) {
                                scalePlayer1.animateTo(1f, animationSpec = tween(300))
                                alphaPlayer1.animateTo(1f, animationSpec = tween(300))
                            } else {
                                scalePlayer1.snapTo(0.5f)
                                alphaPlayer1.snapTo(0f)
                            }
                        }
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Player 1 move",
                            tint = if (isPlayer1OldMove) Color.Black.copy(0.4f) else Color.Black,
                            modifier = Modifier
                                .width(iconSize)
                                .height(iconSize)
                                .scale(scalePlayer1.value)
                                .alpha(alphaPlayer1.value)
                        )
                    } else if (isPlayer2Move) {
                        val scalePlayer2 = remember { Animatable(0.5f) }
                        val alphaPlayer2 = remember { Animatable(0f) }
                        LaunchedEffect(key1 = isPlayer2Move) {
                            if (isPlayer2Move) {
                                scalePlayer2.animateTo(1f, animationSpec = tween(300))
                                alphaPlayer2.animateTo(1f, animationSpec = tween(300))
                            } else {
                                scalePlayer2.snapTo(0.5f)
                                alphaPlayer2.snapTo(0f)
                            }
                        }
                        Icon(
                            painter = painterResource(R.drawable.player_2),
                            contentDescription = "Player 2 move",
                            tint = if (isPlayer2OldMove) Color.Black.copy(0.4f) else LocalContentColor.current,
                            modifier = Modifier
                                .width(iconSize)
                                .height(iconSize)
                                .scale(scalePlayer2.value)
                                .alpha(alphaPlayer2.value)
                        )
                    }
                    // Else, display nothing (empty button)
                }

                IconButton(
                    onClick = {
                        if (gameStarted) {
                            val buttonId = "button5" // The layoutId of this button

                            // Check if this button is already an active move for either player
                            val isAlreadyPlayedByPlayer1 =
                                player1Moves.takeLast(maxVisibleMovesPerPlayer)
                                    .contains(buttonId)
                            val isAlreadyPlayedByPlayer2 =
                                player2Moves.takeLast(maxVisibleMovesPerPlayer)
                                    .contains(buttonId)

                            if (!isAlreadyPlayedByPlayer1 && !isAlreadyPlayedByPlayer2) {
                                if (player1turn) {
                                    player1Moves.add(buttonId)
                                    if (player1Moves.size > maxVisibleMovesPerPlayer) {
                                        player1Moves.removeAt(0) // Remove the oldest move
                                    }
                                } else {
                                    player2Moves.add(buttonId)
                                    if (player2Moves.size > maxVisibleMovesPerPlayer) {
                                        player2Moves.removeAt(0) // Remove the oldest move
                                    }
                                }
                                player1turn = !player1turn
                            }
                        }
                    },
                    modifier = Modifier
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .layoutId("button5")
                        .width(80.dp)
                        .height(80.dp)
                        .onGloballyPositioned { coordinates ->
                            buttonCoordinates["button5"] = coordinates
                        },
                    interactionSource = remember { MutableInteractionSource() }, // Added
                    indication = null, // Added
                ) {
                    // Determine what to display based on the lists
                    val buttonId = "button5"
                    val isPlayer1Move =
                        player1Moves.takeLast(maxVisibleMovesPerPlayer).contains(buttonId)
                    val isPlayer2Move =
                        player2Moves.takeLast(maxVisibleMovesPerPlayer).contains(buttonId)

                    val isPlayer1OldMove =
                        if (player1Moves.size >= 3 && gameStarted && player1turn) player1Moves[0] == buttonId else false
                    val isPlayer2OldMove =
                        if (player2Moves.size >= 3 && gameStarted && !player1turn) player2Moves[0] == buttonId else false

                    if (isPlayer1Move) {
                        val scalePlayer1 = remember { Animatable(0.5f) }
                        val alphaPlayer1 = remember { Animatable(0f) }
                        LaunchedEffect(key1 = isPlayer1Move) {
                            if (isPlayer1Move) {
                                scalePlayer1.animateTo(1f, animationSpec = tween(300))
                                alphaPlayer1.animateTo(1f, animationSpec = tween(300))
                            } else {
                                scalePlayer1.snapTo(0.5f)
                                alphaPlayer1.snapTo(0f)
                            }
                        }
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Player 1 move",
                            tint = if (isPlayer1OldMove) Color.Black.copy(0.4f) else Color.Black,
                            modifier = Modifier
                                .width(iconSize)
                                .height(iconSize)
                                .scale(scalePlayer1.value)
                                .alpha(alphaPlayer1.value)
                        )
                    } else if (isPlayer2Move) {
                        val scalePlayer2 = remember { Animatable(0.5f) }
                        val alphaPlayer2 = remember { Animatable(0f) }
                        LaunchedEffect(key1 = isPlayer2Move) {
                            if (isPlayer2Move) {
                                scalePlayer2.animateTo(1f, animationSpec = tween(300))
                                alphaPlayer2.animateTo(1f, animationSpec = tween(300))
                            } else {
                                scalePlayer2.snapTo(0.5f)
                                alphaPlayer2.snapTo(0f)
                            }
                        }
                        Icon(
                            painter = painterResource(R.drawable.player_2),
                            contentDescription = "Player 2 move",
                            tint = if (isPlayer2OldMove) Color.Black.copy(0.4f) else LocalContentColor.current,
                            modifier = Modifier
                                .width(iconSize)
                                .height(iconSize)
                                .scale(scalePlayer2.value)
                                .alpha(alphaPlayer2.value)
                        )
                    }
                    // Else, display nothing (empty button)
                }

                IconButton(
                    onClick = {
                        if (gameStarted) {
                            val buttonId = "button6" // The layoutId of this button

                            // Check if this button is already an active move for either player
                            val isAlreadyPlayedByPlayer1 =
                                player1Moves.takeLast(maxVisibleMovesPerPlayer)
                                    .contains(buttonId)
                            val isAlreadyPlayedByPlayer2 =
                                player2Moves.takeLast(maxVisibleMovesPerPlayer)
                                    .contains(buttonId)

                            if (!isAlreadyPlayedByPlayer1 && !isAlreadyPlayedByPlayer2) {
                                if (player1turn) {
                                    player1Moves.add(buttonId)
                                    if (player1Moves.size > maxVisibleMovesPerPlayer) {
                                        player1Moves.removeAt(0) // Remove the oldest move
                                    }
                                } else {
                                    player2Moves.add(buttonId)
                                    if (player2Moves.size > maxVisibleMovesPerPlayer) {
                                        player2Moves.removeAt(0) // Remove the oldest move
                                    }
                                }
                                player1turn = !player1turn
                            }
                        }
                    },
                    modifier = Modifier
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .layoutId("button6")
                        .width(80.dp)
                        .height(80.dp)
                        .onGloballyPositioned { coordinates ->
                            buttonCoordinates["button6"] = coordinates
                        },
                    interactionSource = remember { MutableInteractionSource() }, // Added
                    indication = null, // Added
                ) {
                    // Determine what to display based on the lists
                    val buttonId = "button6"
                    val isPlayer1Move =
                        player1Moves.takeLast(maxVisibleMovesPerPlayer).contains(buttonId)
                    val isPlayer2Move =
                        player2Moves.takeLast(maxVisibleMovesPerPlayer).contains(buttonId)

                    val isPlayer1OldMove =
                        if (player1Moves.size >= 3 && gameStarted && player1turn) player1Moves[0] == buttonId else false
                    val isPlayer2OldMove =
                        if (player2Moves.size >= 3 && gameStarted && !player1turn) player2Moves[0] == buttonId else false

                    if (isPlayer1Move) {
                        val scalePlayer1 = remember { Animatable(0.5f) }
                        val alphaPlayer1 = remember { Animatable(0f) }
                        LaunchedEffect(key1 = isPlayer1Move) {
                            if (isPlayer1Move) {
                                scalePlayer1.animateTo(1f, animationSpec = tween(300))
                                alphaPlayer1.animateTo(1f, animationSpec = tween(300))
                            } else {
                                scalePlayer1.snapTo(0.5f)
                                alphaPlayer1.snapTo(0f)
                            }
                        }
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Player 1 move",
                            tint = if (isPlayer1OldMove) Color.Black.copy(0.4f) else Color.Black,
                            modifier = Modifier
                                .width(iconSize)
                                .height(iconSize)
                                .scale(scalePlayer1.value)
                                .alpha(alphaPlayer1.value)
                        )
                    } else if (isPlayer2Move) {
                        val scalePlayer2 = remember { Animatable(0.5f) }
                        val alphaPlayer2 = remember { Animatable(0f) }
                        LaunchedEffect(key1 = isPlayer2Move) {
                            if (isPlayer2Move) {
                                scalePlayer2.animateTo(1f, animationSpec = tween(300))
                                alphaPlayer2.animateTo(1f, animationSpec = tween(300))
                            } else {
                                scalePlayer2.snapTo(0.5f)
                                alphaPlayer2.snapTo(0f)
                            }
                        }
                        Icon(
                            painter = painterResource(R.drawable.player_2),
                            contentDescription = "Player 2 move",
                            tint = if (isPlayer2OldMove) Color.Black.copy(0.4f) else LocalContentColor.current,
                            modifier = Modifier
                                .width(iconSize)
                                .height(iconSize)
                                .scale(scalePlayer2.value)
                                .alpha(alphaPlayer2.value)
                        )
                    }
                    // Else, display nothing (empty button)
                }

                IconButton(
                    onClick = {
                        if (gameStarted) {
                            val buttonId = "button7" // The layoutId of this button

                            // Check if this button is already an active move for either player
                            val isAlreadyPlayedByPlayer1 =
                                player1Moves.takeLast(maxVisibleMovesPerPlayer)
                                    .contains(buttonId)
                            val isAlreadyPlayedByPlayer2 =
                                player2Moves.takeLast(maxVisibleMovesPerPlayer)
                                    .contains(buttonId)

                            if (!isAlreadyPlayedByPlayer1 && !isAlreadyPlayedByPlayer2) {
                                if (player1turn) {
                                    player1Moves.add(buttonId)
                                    if (player1Moves.size > maxVisibleMovesPerPlayer) {
                                        player1Moves.removeAt(0) // Remove the oldest move
                                    }
                                } else {
                                    player2Moves.add(buttonId)
                                    if (player2Moves.size > maxVisibleMovesPerPlayer) {
                                        player2Moves.removeAt(0) // Remove the oldest move
                                    }
                                }
                                player1turn = !player1turn
                            }
                        }
                    },
                    modifier = Modifier
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .layoutId("button7")
                        .width(80.dp)
                        .height(80.dp)
                        .onGloballyPositioned { coordinates ->
                            buttonCoordinates["button7"] = coordinates
                        },
                    interactionSource = remember { MutableInteractionSource() }, // Added
                    indication = null, // Added
                ) {
                    // Determine what to display based on the lists
                    val buttonId = "button7"
                    val isPlayer1Move =
                        player1Moves.takeLast(maxVisibleMovesPerPlayer).contains(buttonId)
                    val isPlayer2Move =
                        player2Moves.takeLast(maxVisibleMovesPerPlayer).contains(buttonId)

                    val isPlayer1OldMove =
                        if (player1Moves.size >= 3 && gameStarted && player1turn) player1Moves[0] == buttonId else false
                    val isPlayer2OldMove =
                        if (player2Moves.size >= 3 && gameStarted && !player1turn) player2Moves[0] == buttonId else false

                    if (isPlayer1Move) {
                        val scalePlayer1 = remember { Animatable(0.5f) }
                        val alphaPlayer1 = remember { Animatable(0f) }
                        LaunchedEffect(key1 = isPlayer1Move) {
                            if (isPlayer1Move) {
                                scalePlayer1.animateTo(1f, animationSpec = tween(300))
                                alphaPlayer1.animateTo(1f, animationSpec = tween(300))
                            } else {
                                scalePlayer1.snapTo(0.5f)
                                alphaPlayer1.snapTo(0f)
                            }
                        }
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Player 1 move",
                            tint = if (isPlayer1OldMove) Color.Black.copy(0.4f) else Color.Black,
                            modifier = Modifier
                                .width(iconSize)
                                .height(iconSize)
                                .scale(scalePlayer1.value)
                                .alpha(alphaPlayer1.value)
                        )
                    } else if (isPlayer2Move) {
                        val scalePlayer2 = remember { Animatable(0.5f) }
                        val alphaPlayer2 = remember { Animatable(0f) }
                        LaunchedEffect(key1 = isPlayer2Move) {
                            if (isPlayer2Move) {
                                scalePlayer2.animateTo(1f, animationSpec = tween(300))
                                alphaPlayer2.animateTo(1f, animationSpec = tween(300))
                            } else {
                                scalePlayer2.snapTo(0.5f)
                                alphaPlayer2.snapTo(0f)
                            }
                        }
                        Icon(
                            painter = painterResource(R.drawable.player_2),
                            contentDescription = "Player 2 move",
                            tint = if (isPlayer2OldMove) Color.Black.copy(0.4f) else LocalContentColor.current,
                            modifier = Modifier
                                .width(iconSize)
                                .height(iconSize)
                                .scale(scalePlayer2.value)
                                .alpha(alphaPlayer2.value)
                        )
                    }
                    // Else, display nothing (empty button)
                }

                IconButton(
                    onClick = {
                        if (gameStarted) {
                            val buttonId = "button8" // The layoutId of this button

                            // Check if this button is already an active move for either player
                            val isAlreadyPlayedByPlayer1 =
                                player1Moves.takeLast(maxVisibleMovesPerPlayer)
                                    .contains(buttonId)
                            val isAlreadyPlayedByPlayer2 =
                                player2Moves.takeLast(maxVisibleMovesPerPlayer)
                                    .contains(buttonId)

                            if (!isAlreadyPlayedByPlayer1 && !isAlreadyPlayedByPlayer2) {
                                if (player1turn) {
                                    player1Moves.add(buttonId)
                                    if (player1Moves.size > maxVisibleMovesPerPlayer) {
                                        player1Moves.removeAt(0) // Remove the oldest move
                                    }
                                } else {
                                    player2Moves.add(buttonId)
                                    if (player2Moves.size > maxVisibleMovesPerPlayer) {
                                        player2Moves.removeAt(0) // Remove the oldest move
                                    }
                                }
                                player1turn = !player1turn
                            }
                        }
                    },
                    modifier = Modifier
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .layoutId("button8")
                        .width(80.dp)
                        .height(80.dp)
                        .onGloballyPositioned { coordinates ->
                            buttonCoordinates["button8"] = coordinates
                        },
                    interactionSource = remember { MutableInteractionSource() }, // Added
                    indication = null, // Added
                ) {
                    // Determine what to display based on the lists
                    val buttonId = "button8"
                    val isPlayer1Move =
                        player1Moves.takeLast(maxVisibleMovesPerPlayer).contains(buttonId)
                    val isPlayer2Move =
                        player2Moves.takeLast(maxVisibleMovesPerPlayer).contains(buttonId)

                    val isPlayer1OldMove =
                        if (player1Moves.size >= 3 && gameStarted && player1turn) player1Moves[0] == buttonId else false
                    val isPlayer2OldMove =
                        if (player2Moves.size >= 3 && gameStarted && !player1turn) player2Moves[0] == buttonId else false

                    if (isPlayer1Move) {
                        val scalePlayer1 = remember { Animatable(0.5f) }
                        val alphaPlayer1 = remember { Animatable(0f) }
                        LaunchedEffect(key1 = isPlayer1Move) {
                            if (isPlayer1Move) {
                                scalePlayer1.animateTo(1f, animationSpec = tween(300))
                                alphaPlayer1.animateTo(1f, animationSpec = tween(300))
                            } else {
                                scalePlayer1.snapTo(0.5f)
                                alphaPlayer1.snapTo(0f)
                            }
                        }
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Player 1 move",
                            tint = if (isPlayer1OldMove) Color.Black.copy(0.4f) else Color.Black,
                            modifier = Modifier
                                .width(iconSize)
                                .height(iconSize)
                                .scale(scalePlayer1.value)
                                .alpha(alphaPlayer1.value)
                        )
                    } else if (isPlayer2Move) {
                        val scalePlayer2 = remember { Animatable(0.5f) }
                        val alphaPlayer2 = remember { Animatable(0f) }
                        LaunchedEffect(key1 = isPlayer2Move) {
                            if (isPlayer2Move) {
                                scalePlayer2.animateTo(1f, animationSpec = tween(300))
                                alphaPlayer2.animateTo(1f, animationSpec = tween(300))
                            } else {
                                scalePlayer2.snapTo(0.5f)
                                alphaPlayer2.snapTo(0f)
                            }
                        }
                        Icon(
                            painter = painterResource(R.drawable.player_2),
                            contentDescription = "Player 2 move",
                            tint = if (isPlayer2OldMove) Color.Black.copy(0.4f) else LocalContentColor.current,
                            modifier = Modifier
                                .width(iconSize)
                                .height(iconSize)
                                .scale(scalePlayer2.value)
                                .alpha(alphaPlayer2.value)
                        )
                    }
                    // Else, display nothing (empty button)
                }

                IconButton(
                    onClick = {
                        if (gameStarted) {
                            val buttonId = "button9" // The layoutId of this button

                            // Check if this button is already an active move for either player
                            val isAlreadyPlayedByPlayer1 =
                                player1Moves.takeLast(maxVisibleMovesPerPlayer)
                                    .contains(buttonId)
                            val isAlreadyPlayedByPlayer2 =
                                player2Moves.takeLast(maxVisibleMovesPerPlayer)
                                    .contains(buttonId)

                            if (!isAlreadyPlayedByPlayer1 && !isAlreadyPlayedByPlayer2) {
                                if (player1turn) {
                                    player1Moves.add(buttonId)
                                    if (player1Moves.size > maxVisibleMovesPerPlayer) {
                                        player1Moves.removeAt(0) // Remove the oldest move
                                    }
                                } else {
                                    player2Moves.add(buttonId)
                                    if (player2Moves.size > maxVisibleMovesPerPlayer) {
                                        player2Moves.removeAt(0) // Remove the oldest move
                                    }
                                }
                                player1turn = !player1turn
                            }
                        }
                    },
                    modifier = Modifier
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .layoutId("button9")
                        .width(80.dp)
                        .height(80.dp)
                        .onGloballyPositioned { coordinates ->
                            buttonCoordinates["button9"] = coordinates
                        },
                    interactionSource = remember { MutableInteractionSource() }, // Added
                    indication = null, // Added
                ) {
                    // Determine what to display based on the lists
                    val buttonId = "button9"
                    val isPlayer1Move =
                        player1Moves.takeLast(maxVisibleMovesPerPlayer).contains(buttonId)
                    val isPlayer2Move =
                        player2Moves.takeLast(maxVisibleMovesPerPlayer).contains(buttonId)

                    val isPlayer1OldMove =
                        if (player1Moves.size >= 3 && gameStarted && player1turn) player1Moves[0] == buttonId else false
                    val isPlayer2OldMove =
                        if (player2Moves.size >= 3 && gameStarted && !player1turn) player2Moves[0] == buttonId else false

                    if (isPlayer1Move) {
                        val scalePlayer1 = remember { Animatable(0.5f) }
                        val alphaPlayer1 = remember { Animatable(0f) }
                        LaunchedEffect(key1 = isPlayer1Move) {
                            if (isPlayer1Move) {
                                scalePlayer1.animateTo(1f, animationSpec = tween(300))
                                alphaPlayer1.animateTo(1f, animationSpec = tween(300))
                            } else {
                                scalePlayer1.snapTo(0.5f)
                                alphaPlayer1.snapTo(0f)
                            }
                        }
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Player 1 move",
                            tint = if (isPlayer1OldMove) Color.Black.copy(0.4f) else Color.Black,
                            modifier = Modifier
                                .width(iconSize)
                                .height(iconSize)
                                .scale(scalePlayer1.value)
                                .alpha(alphaPlayer1.value)
                        )
                    } else if (isPlayer2Move) {
                        val scalePlayer2 = remember { Animatable(0.5f) }
                        val alphaPlayer2 = remember { Animatable(0f) }
                        LaunchedEffect(key1 = isPlayer2Move) {
                            if (isPlayer2Move) {
                                scalePlayer2.animateTo(1f, animationSpec = tween(300))
                                alphaPlayer2.animateTo(1f, animationSpec = tween(300))
                            } else {
                                scalePlayer2.snapTo(0.5f)
                                alphaPlayer2.snapTo(0f)
                            }
                        }
                        Icon(
                            painter = painterResource(R.drawable.player_2),
                            contentDescription = "Player 2 move",
                            tint = if (isPlayer2OldMove) Color.Black.copy(0.4f) else LocalContentColor.current,
                            modifier = Modifier
                                .width(iconSize)
                                .height(iconSize)
                                .scale(scalePlayer2.value)
                                .alpha(alphaPlayer2.value)
                        )
                    }
                    // Else, display nothing (empty button)
                }
            }

            if (gameStarted)
                turnDenotingText = if (player1turn)
                    "Player 1's Turn"
                else
                    "Player 2's Turn"

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
                            text = "$turnDenotingText (",
                            style = MaterialTheme.typography.labelMedium,
                            fontSize = 20.sp
                        )
                        if (turnDenotingText == "Player 1's Turn" || turnDenotingText == "Player 1 Won")
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Turn Denoting Icon"
                            )
                        else
                            Icon(
                                painterResource(R.drawable.player_2),
                                contentDescription = "Turn Denoting Icon"
                            )
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
                            contentDescription = "Circle",
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
                    player1Moves = mutableListOf()
                    player2Moves = mutableListOf()
                    winnerInfo = null // Reset winnerInfo
                    buttonCoordinates.clear() // Clear stored coordinates (though they'll repopulate)
                    player1turn = true
                    resetButtonText = "Reset Round"
                    gameStarted = true
                    isGameConcluded = false // Reset game concluded state
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
                        text = resetButtonText,
                        modifier = Modifier
                            .padding(start = 8.dp)
                    )
                }
            }

            Button(
                onClick = {
                    player1Wins = 0
                    player2Wins = 0
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

        val winningCombinations = listOf(
            // Rows
            setOf("button1", "button2", "button3"),
            setOf("button4", "button5", "button6"),
            setOf("button7", "button8", "button9"),
            // Columns
            setOf("button1", "button4", "button7"),
            setOf("button2", "button5", "button8"),
            setOf("button3", "button6", "button9"),
            // Diagonals
            setOf("button1", "button5", "button9"),
            setOf("button3", "button5", "button7")
        )

        val visiblePlayer1Moves = player1Moves.takeLast(maxVisibleMovesPerPlayer).toSet()
        val visiblePlayer2Moves = player2Moves.takeLast(maxVisibleMovesPerPlayer).toSet()

        for (combination in winningCombinations) {
            if (!isGameConcluded) { // Check if the game hasn't been marked as concluded yet
                if (visiblePlayer1Moves.containsAll(combination)) {
                    // All existing actions for Player 1 win:
                    turnDenotingText = "Player 1 Won"
                    gameStarted = false
                    resetButtonText = "New Round"
                    winnerInfo = WinnerInfo("Player 1 Wins", combination)
                    player1Wins++
                    // Add this line:
                    isGameConcluded = true
                }
                // Add a similar check for Player 2, also setting isGameConcluded = true
                if (visiblePlayer2Moves.containsAll(combination)) {
                    // All existing actions for Player 2 win:
                    turnDenotingText = "Player 2 Won"
                    gameStarted = false
                    resetButtonText = "New Round"
                    winnerInfo = WinnerInfo("Player 2 Wins", combination)
                    player2Wins++
                    // Add this line:
                    isGameConcluded = true
                }
            }
        }
    }

}

data class WinnerInfo(val playerName: String, val combination: Set<String>)