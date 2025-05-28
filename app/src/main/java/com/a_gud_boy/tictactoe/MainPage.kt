package com.a_gud_boy.tictactoe

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet

@SuppressLint("MutableCollectionMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicTacToeGame() {
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
        mutableStateOf("Reset Game")
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

        // Id for all the dividers
        val divider1 = createRefFor("divider1")     // horizontal divider 1
        val divider2 = createRefFor("divider2")     // horizontal divider 2
        val divider3 = createRefFor("divider3")     // vertical divider 1
        val divider4 = createRefFor("divider4")     // vertical divider 1

        val margin = 0.dp

        constrain(button1) {
            top.linkTo(parent.top, margin = margin)
            start.linkTo(parent.start, margin = margin)
            end.linkTo(divider3.start, margin = margin)
            bottom.linkTo(divider1.top, margin = margin)
        }

        constrain(button2) {
            top.linkTo(parent.top, margin = margin)
            start.linkTo(divider3.end, margin = margin)
            end.linkTo(divider4.start, margin = margin)
            bottom.linkTo(divider1.top, margin = margin)
        }

        constrain(button3) {
            top.linkTo(parent.top, margin = margin)
            start.linkTo(divider4.end, margin = margin)
            end.linkTo(parent.end, margin = margin)
            bottom.linkTo(divider1.top, margin = margin)
        }

        constrain(button4) {
            top.linkTo(divider1.bottom, margin = margin)
            start.linkTo(parent.start, margin = margin)
            end.linkTo(divider3.start, margin = margin)
            bottom.linkTo(divider2.top, margin = margin)
        }

        constrain(button5) {
            top.linkTo(divider1.bottom, margin = 1.dp)
            start.linkTo(divider3.end, margin = margin)
            end.linkTo(divider4.start, margin = margin)
            bottom.linkTo(divider2.top, margin = 1.dp)
        }

        constrain(button6) {
            top.linkTo(divider1.bottom, margin = margin)
            start.linkTo(divider4.end, margin = margin)
            end.linkTo(parent.end, margin = margin)
            bottom.linkTo(divider2.top, margin = margin)
        }

        constrain(button7) {
            top.linkTo(divider2.bottom, margin = margin)
            start.linkTo(parent.start, margin = margin)
            end.linkTo(divider3.start, margin = margin)
            bottom.linkTo(parent.bottom, margin = margin)
        }

        constrain(button8) {
            top.linkTo(divider2.bottom, margin = margin)
            start.linkTo(divider3.end, margin = margin)
            end.linkTo(divider4.start, margin = margin)
            bottom.linkTo(parent.bottom, margin = margin)
        }

        constrain(button9) {
            top.linkTo(divider2.bottom, margin = margin)
            start.linkTo(divider4.end, margin = margin)
            end.linkTo(parent.end, margin = margin)
            bottom.linkTo(parent.bottom, margin = margin)
        }

        constrain(divider1) {
            top.linkTo(button2.bottom, margin = margin)
            bottom.linkTo(button5.top, margin = margin)
        }

        constrain(divider2) {
            top.linkTo(button5.bottom, margin = margin)
            bottom.linkTo(button8.top, margin = margin)
        }

        constrain(divider3) {
            start.linkTo(button4.end, margin = margin)
            end.linkTo(button5.start, margin = margin)
        }

        constrain(divider4) {
            start.linkTo(button5.end, margin = margin)
            end.linkTo(button6.start, margin = margin)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Tic Tac Toe",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            Box( // Add AngledDivider
                modifier = Modifier
                    .wrapContentSize()
                    .background(Color.White)
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
                            .drawWithContent {
                                drawContent() // Draw buttons first

                                winnerInfo?.let { info ->
                                    val winningButtonIds = info.combination.toList()
                                    if (winningButtonIds.size == 3) {
                                        val startButtonId = winningButtonIds[0]
                                        val endButtonId = winningButtonIds[2]

                                        val startCoords = buttonCoordinates[startButtonId]
                                        val endCoords = buttonCoordinates[endButtonId]

                                        if (startCoords != null && endCoords != null) {
                                            // 1. Get original center points (relative to this ConstraintLayout)
                                            val originalLineStart = Offset(
                                                startCoords.size.width / 2f + startCoords.positionInParent().x,
                                                startCoords.size.height / 2f + startCoords.positionInParent().y
                                            )
                                            val originalLineEnd = Offset(
                                                endCoords.size.width / 2f + endCoords.positionInParent().x,
                                                endCoords.size.height / 2f + endCoords.positionInParent().y
                                            )

                                            val lineExtensionLengthDp = 12.dp

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
                                    color = Color.LightGray,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .layoutId("button1")
                                .onGloballyPositioned { coordinates ->
                                    buttonCoordinates["button1"] = coordinates
                                }
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
                                if (isPlayer1OldMove) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Player 1 move",
                                        tint = Color.White.copy(0.6f)
                                    )
                                } else
                                    Icon(Icons.Default.Close, contentDescription = "Player 1 move")
                            } else if (isPlayer2Move) {
                                if (isPlayer2OldMove)
                                    Icon(
                                        painter = painterResource(R.drawable.player_2), // Make sure this resource exists
                                        contentDescription = "Player 2 move",
                                        tint = Color.White.copy(0.6f)
                                    )
                                else
                                    Icon(
                                        painter = painterResource(R.drawable.player_2), // Make sure this resource exists
                                        contentDescription = "Player 2 move"
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
                                    color = Color.LightGray,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .layoutId("button2")
                                .onGloballyPositioned { coordinates ->
                                    buttonCoordinates["button2"] = coordinates
                                }
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
                                if (isPlayer1OldMove) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Player 1 move",
                                        tint = Color.White.copy(0.6f)
                                    )
                                } else
                                    Icon(Icons.Default.Close, contentDescription = "Player 1 move")
                            } else if (isPlayer2Move) {
                                if (isPlayer2OldMove)
                                    Icon(
                                        painter = painterResource(R.drawable.player_2), // Make sure this resource exists
                                        contentDescription = "Player 2 move",
                                        tint = Color.White.copy(0.6f)
                                    )
                                else
                                    Icon(
                                        painter = painterResource(R.drawable.player_2), // Make sure this resource exists
                                        contentDescription = "Player 2 move"
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
                                    color = Color.LightGray,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .layoutId("button3")
                                .onGloballyPositioned { coordinates ->
                                    buttonCoordinates["button3"] = coordinates
                                }
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
                                if (isPlayer1OldMove) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Player 1 move",
                                        tint = Color.White.copy(0.6f)
                                    )
                                } else
                                    Icon(Icons.Default.Close, contentDescription = "Player 1 move")
                            } else if (isPlayer2Move) {
                                if (isPlayer2OldMove)
                                    Icon(
                                        painter = painterResource(R.drawable.player_2), // Make sure this resource exists
                                        contentDescription = "Player 2 move",
                                        tint = Color.White.copy(0.6f)
                                    )
                                else
                                    Icon(
                                        painter = painterResource(R.drawable.player_2), // Make sure this resource exists
                                        contentDescription = "Player 2 move"
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
                                    color = Color.LightGray,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .layoutId("button4")
                                .onGloballyPositioned { coordinates ->
                                    buttonCoordinates["button4"] = coordinates
                                }
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
                                if (isPlayer1OldMove) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Player 1 move",
                                        tint = Color.White.copy(0.6f)
                                    )
                                } else
                                    Icon(Icons.Default.Close, contentDescription = "Player 1 move")
                            } else if (isPlayer2Move) {
                                if (isPlayer2OldMove)
                                    Icon(
                                        painter = painterResource(R.drawable.player_2), // Make sure this resource exists
                                        contentDescription = "Player 2 move",
                                        tint = Color.White.copy(0.6f)
                                    )
                                else
                                    Icon(
                                        painter = painterResource(R.drawable.player_2), // Make sure this resource exists
                                        contentDescription = "Player 2 move"
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
                                    color = Color.LightGray,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .layoutId("button5")
                                .onGloballyPositioned { coordinates ->
                                    buttonCoordinates["button5"] = coordinates
                                }
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
                                if (isPlayer1OldMove) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Player 1 move",
                                        tint = Color.White.copy(0.6f)
                                    )
                                } else
                                    Icon(Icons.Default.Close, contentDescription = "Player 1 move")
                            } else if (isPlayer2Move) {
                                if (isPlayer2OldMove)
                                    Icon(
                                        painter = painterResource(R.drawable.player_2), // Make sure this resource exists
                                        contentDescription = "Player 2 move",
                                        tint = Color.White.copy(0.6f)
                                    )
                                else
                                    Icon(
                                        painter = painterResource(R.drawable.player_2), // Make sure this resource exists
                                        contentDescription = "Player 2 move"
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
                                    color = Color.LightGray,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .layoutId("button6")
                                .onGloballyPositioned { coordinates ->
                                    buttonCoordinates["button6"] = coordinates
                                }
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
                                if (isPlayer1OldMove) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Player 1 move",
                                        tint = Color.White.copy(0.6f)
                                    )
                                } else
                                    Icon(Icons.Default.Close, contentDescription = "Player 1 move")
                            } else if (isPlayer2Move) {
                                if (isPlayer2OldMove)
                                    Icon(
                                        painter = painterResource(R.drawable.player_2), // Make sure this resource exists
                                        contentDescription = "Player 2 move",
                                        tint = Color.White.copy(0.6f)
                                    )
                                else
                                    Icon(
                                        painter = painterResource(R.drawable.player_2), // Make sure this resource exists
                                        contentDescription = "Player 2 move"
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
                                    color = Color.LightGray,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .layoutId("button7")
                                .onGloballyPositioned { coordinates ->
                                    buttonCoordinates["button7"] = coordinates
                                }
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
                                if (isPlayer1OldMove) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Player 1 move",
                                        tint = Color.White.copy(0.6f)
                                    )
                                } else
                                    Icon(Icons.Default.Close, contentDescription = "Player 1 move")
                            } else if (isPlayer2Move) {
                                if (isPlayer2OldMove)
                                    Icon(
                                        painter = painterResource(R.drawable.player_2), // Make sure this resource exists
                                        contentDescription = "Player 2 move",
                                        tint = Color.White.copy(0.6f)
                                    )
                                else
                                    Icon(
                                        painter = painterResource(R.drawable.player_2), // Make sure this resource exists
                                        contentDescription = "Player 2 move"
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
                                    color = Color.LightGray,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .layoutId("button8")
                                .onGloballyPositioned { coordinates ->
                                    buttonCoordinates["button8"] = coordinates
                                }
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
                                if (isPlayer1OldMove) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Player 1 move",
                                        tint = Color.White.copy(0.6f)
                                    )
                                } else
                                    Icon(Icons.Default.Close, contentDescription = "Player 1 move")
                            } else if (isPlayer2Move) {
                                if (isPlayer2OldMove)
                                    Icon(
                                        painter = painterResource(R.drawable.player_2), // Make sure this resource exists
                                        contentDescription = "Player 2 move",
                                        tint = Color.White.copy(0.6f)
                                    )
                                else
                                    Icon(
                                        painter = painterResource(R.drawable.player_2), // Make sure this resource exists
                                        contentDescription = "Player 2 move"
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
                                    color = Color.LightGray,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .layoutId("button9")
                                .onGloballyPositioned { coordinates ->
                                    buttonCoordinates["button9"] = coordinates
                                }
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
                                if (isPlayer1OldMove) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Player 1 move",
                                        tint = Color.White.copy(0.6f)
                                    )
                                } else
                                    Icon(Icons.Default.Close, contentDescription = "Player 1 move")
                            } else if (isPlayer2Move) {
                                if (isPlayer2OldMove)
                                    Icon(
                                        painter = painterResource(R.drawable.player_2), // Make sure this resource exists
                                        contentDescription = "Player 2 move",
                                        tint = Color.White.copy(0.6f)
                                    )
                                else
                                    Icon(
                                        painter = painterResource(R.drawable.player_2), // Make sure this resource exists
                                        contentDescription = "Player 2 move"
                                    )
                            }
                            // Else, display nothing (empty button)
                        }

                        HorizontalDivider(
                            Modifier.layoutId("divider1"),
                            DividerDefaults.Thickness,
                            DividerDefaults.color
                        )

                        HorizontalDivider(
                            Modifier.layoutId("divider2"),
                            DividerDefaults.Thickness,
                            DividerDefaults.color
                        )

                        VerticalDivider(
                            Modifier.layoutId("divider3"),
                            DividerDefaults.Thickness,
                            DividerDefaults.color
                        )

                        VerticalDivider(
                            Modifier.layoutId("divider4"),
                            DividerDefaults.Thickness,
                            DividerDefaults.color
                        )
                    }

                    if (gameStarted)
                        turnDenotingText = if (player1turn)
                            "Player 1's Turn"
                        else
                            "Player 2's Turn"

                    Row(
                        modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically
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



                    Button(onClick = {
                        player1Moves = mutableListOf()
                        player2Moves = mutableListOf()
                        winnerInfo = null // Reset winnerInfo
                        buttonCoordinates.clear() // Clear stored coordinates (though they'll repopulate)
                        player1turn = true
                        resetButtonText = "Reset Game"
                        gameStarted = true
                    }) {
                        Text(text = resetButtonText)
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
                    if (visiblePlayer1Moves.containsAll(combination)) {
                        turnDenotingText = "Player 1 Won"
                        gameStarted = false
                        resetButtonText =
                            "New Game" // Player 1 has a winning combination among their visible moves
                        winnerInfo = WinnerInfo("Player 1 Wins", combination)
                    }
                    if (visiblePlayer2Moves.containsAll(combination)) {
                        turnDenotingText = "Player 2 Won"
                        gameStarted = false
                        resetButtonText =
                            "New Game" // Player 2 has a winning combination among their visible moves
                        winnerInfo = WinnerInfo("Player 2 Wins", combination)
                    }
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun TicTacToeGamePreview() {
    TicTacToeGame()
}

data class WinnerInfo(val playerName: String, val combination: Set<String>)