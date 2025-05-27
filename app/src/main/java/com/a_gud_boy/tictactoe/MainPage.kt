package com.a_gud_boy.tictactoe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicTacToeGame() {

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

    // 0 for no image, 1 for player 1 and 2 for player 2
    var button1_state by rememberSaveable {
        mutableIntStateOf(0)
    }
    var button2_state by rememberSaveable {
        mutableIntStateOf(0)
    }
    var button3_state by rememberSaveable {
        mutableIntStateOf(0)
    }
    var button4_state by rememberSaveable {
        mutableIntStateOf(0)
    }
    var button5_state by rememberSaveable {
        mutableIntStateOf(0)
    }
    var button6_state by rememberSaveable {
        mutableIntStateOf(0)
    }
    var button7_state by rememberSaveable {
        mutableIntStateOf(0)
    }
    var button8_state by rememberSaveable {
        mutableIntStateOf(0)
    }
    var button9_state by rememberSaveable {
        mutableIntStateOf(0)
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
                    ) {
                        IconButton(
                            onClick = {
                                if (gameStarted) {
                                    if (button1_state == 0)
                                        button1_state = if (player1turn)
                                            1
                                        else
                                            2
                                    player1turn = !player1turn
                                }
                            },
                            modifier = Modifier
                                .background(
                                    color = Color.LightGray,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .layoutId("button1")
                        ) {
                            if (button1_state == 1)
                                Icon(Icons.Default.Close, contentDescription = "")
                            else if (button1_state == 2)
                                Icon(
                                    painter = painterResource(R.drawable.player_2),
                                    contentDescription = ""
                                )
                        }

                        IconButton(
                            onClick = {
                                if (gameStarted) {
                                    if (button2_state == 0)
                                        button2_state = if (player1turn)
                                            1
                                        else
                                            2
                                    player1turn = !player1turn
                                }
                            },
                            modifier = Modifier
                                .background(
                                    color = Color.LightGray,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .layoutId("button2")
                        ) {
                            if (button2_state == 1)
                                Icon(Icons.Default.Close, contentDescription = "")
                            else if (button2_state == 2)
                                Icon(
                                    painter = painterResource(R.drawable.player_2),
                                    contentDescription = ""
                                )
                        }

                        IconButton(
                            onClick = {
                                if (gameStarted) {
                                    if (button3_state == 0)
                                        button3_state = if (player1turn)
                                            1
                                        else
                                            2
                                    player1turn = !player1turn
                                }
                            },
                            modifier = Modifier
                                .background(
                                    color = Color.LightGray,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .layoutId("button3")
                        ) {
                            if (button3_state == 1)
                                Icon(Icons.Default.Close, contentDescription = "")
                            else if (button3_state == 2)
                                Icon(
                                    painter = painterResource(R.drawable.player_2),
                                    contentDescription = ""
                                )
                        }

                        IconButton(
                            onClick = {
                                if (gameStarted) {
                                    if (button4_state == 0)
                                        button4_state = if (player1turn)
                                            1
                                        else
                                            2
                                    player1turn = !player1turn
                                }
                            },
                            modifier = Modifier
                                .background(
                                    color = Color.LightGray,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .layoutId("button4")
                        ) {
                            if (button4_state == 1)
                                Icon(Icons.Default.Close, contentDescription = "")
                            else if (button4_state == 2)
                                Icon(
                                    painter = painterResource(R.drawable.player_2),
                                    contentDescription = ""
                                )
                        }

                        IconButton(
                            onClick = {
                                if (gameStarted) {
                                    if (button5_state == 0)
                                        button5_state = if (player1turn)
                                            1
                                        else
                                            2
                                    player1turn = !player1turn
                                }
                            },
                            modifier = Modifier
                                .background(
                                    color = Color.LightGray,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .layoutId("button5")
                        ) {
                            if (button5_state == 1)
                                Icon(Icons.Default.Close, contentDescription = "")
                            else if (button5_state == 2)
                                Icon(
                                    painter = painterResource(R.drawable.player_2),
                                    contentDescription = ""
                                )
                        }

                        IconButton(
                            onClick = {
                                if (gameStarted) {
                                    if (button6_state == 0)
                                        button6_state = if (player1turn)
                                            1
                                        else
                                            2
                                    player1turn = !player1turn
                                }
                            },
                            modifier = Modifier
                                .background(
                                    color = Color.LightGray,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .layoutId("button6")
                        ) {
                            if (button6_state == 1)
                                Icon(Icons.Default.Close, contentDescription = "")
                            else if (button6_state == 2)
                                Icon(
                                    painter = painterResource(R.drawable.player_2),
                                    contentDescription = ""
                                )
                        }

                        IconButton(
                            onClick = {
                                if (gameStarted) {
                                    if (button7_state == 0)
                                        button7_state = if (player1turn)
                                            1
                                        else
                                            2
                                    player1turn = !player1turn
                                }
                            },
                            modifier = Modifier
                                .background(
                                    color = Color.LightGray,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .layoutId("button7")
                        ) {
                            if (button7_state == 1)
                                Icon(Icons.Default.Close, contentDescription = "")
                            else if (button7_state == 2)
                                Icon(
                                    painter = painterResource(R.drawable.player_2),
                                    contentDescription = ""
                                )
                        }

                        IconButton(
                            onClick = {
                                if (gameStarted) {
                                    if (button8_state == 0)
                                        button8_state = if (player1turn)
                                            1
                                        else
                                            2
                                    player1turn = !player1turn
                                }
                            },
                            modifier = Modifier
                                .background(
                                    color = Color.LightGray,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .layoutId("button8")
                        ) {
                            if (button8_state == 1)
                                Icon(Icons.Default.Close, contentDescription = "")
                            else if (button8_state == 2)
                                Icon(
                                    painter = painterResource(R.drawable.player_2),
                                    contentDescription = ""
                                )
                        }

                        IconButton(
                            onClick = {
                                if (gameStarted) {
                                    if (button9_state == 0)
                                        button9_state = if (player1turn)
                                            1
                                        else
                                            2
                                    player1turn = !player1turn
                                }
                            },
                            modifier = Modifier
                                .background(
                                    color = Color.LightGray,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .layoutId("button9")
                        ) {
                            if (button9_state == 1)
                                Icon(Icons.Default.Close, contentDescription = "")
                            else if (button9_state == 2)
                                Icon(
                                    painter = painterResource(R.drawable.player_2),
                                    contentDescription = ""
                                )
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

                    Text(
                        text = turnDenotingText,
                        style = MaterialTheme.typography.labelMedium,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(20.dp)
                    )

                    Button(onClick = {
                        button1_state = 0
                        button2_state = 0
                        button3_state = 0
                        button4_state = 0
                        button5_state = 0
                        button6_state = 0
                        button7_state = 0
                        button8_state = 0
                        button9_state = 0
                        player1turn = true
                        resetButtonText = "Reset Game"
                        gameStarted = true
                    }) {
                        Text(text = resetButtonText)
                    }
                }

                // Check Winning Conditions
                if (button1_state == button2_state && button2_state == button3_state && button1_state != 0) {
                    gameStarted = false
                    resetButtonText = "New Game"
                    turnDenotingText = if (button1_state == 1)
                        "Player 1 Won"
                    else
                        "Player 2 Won"
                }
                if (button4_state == button5_state && button5_state == button6_state && button4_state != 0) {
                    gameStarted = false
                    resetButtonText = "New Game"
                    turnDenotingText = if (button4_state == 1)
                        "Player 1 Won"
                    else
                        "Player 2 Won"
                }
                if (button7_state == button8_state && button8_state == button9_state && button7_state != 0) {
                    gameStarted = false
                    resetButtonText = "New Game"
                    turnDenotingText = if (button7_state == 1)
                        "Player 1 Won"
                    else
                        "Player 2 Won"
                }
                if (button1_state == button4_state && button4_state == button7_state && button1_state != 0) {
                    gameStarted = false
                    resetButtonText = "New Game"
                    turnDenotingText = if (button1_state == 1)
                        "Player 1 Won"
                    else
                        "Player 2 Won"
                }
                if (button2_state == button5_state && button5_state == button8_state && button2_state != 0) {
                    gameStarted = false
                    resetButtonText = "New Game"
                    turnDenotingText = if (button2_state == 1)
                        "Player 1 Won"
                    else
                        "Player 2 Won"
                }
                if (button3_state == button6_state && button6_state == button9_state && button3_state != 0) {
                    gameStarted = false
                    resetButtonText = "New Game"
                    turnDenotingText = if (button3_state == 1)
                        "Player 1 Won"
                    else
                        "Player 2 Won"
                }
                if (button1_state == button5_state && button5_state == button9_state && button1_state != 0) {
                    gameStarted = false
                    resetButtonText = "New Game"
                    turnDenotingText = if (button1_state == 1)
                        "Player 1 Won"
                    else
                        "Player 2 Won"
                }
                if (button3_state == button5_state && button5_state == button7_state && button3_state != 0) {
                    gameStarted = false
                    resetButtonText = "New Game"
                    turnDenotingText = if (button3_state == 1)
                        "Player 1 Won"
                    else
                        "Player 2 Won"
                }

//                AngledDivider(
//                    modifier = Modifier
//                        .width(100.dp)
//                        .height(100.dp), // The Canvas will fill this Box
//                    color = Color.Red,
//                    strokeWidth = 2.dp,
//                    angle = 45f
//                )
            }
        }
    }
}

@Composable
fun AngledDivider(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.outline,
    strokeWidth: Dp = 1.dp,
    angle: Float = 45f
) {
    Spacer( // Or any other composable
        modifier = modifier
            .drawBehind { // Draw behind the content of the Spacer (which is nothing)
                rotate(degrees = angle, pivot = center) { // Rotate the drawing commands
                    val strokePx = strokeWidth.toPx()
                    // Draw a horizontal line centered in the component
                    // Adjust start and end if you want the line to not be centered
                    // or to have a specific length before rotation.
                    // This line will span the full width of the component before rotation.
                    drawLine(
                        color = color,
                        // Draw line across the component's width, at its vertical center
                        start = Offset(0f, size.height / 2),
                        end = Offset(size.width, size.height / 2),
                        strokeWidth = strokePx,
                        cap = StrokeCap.Round
                    )
                }
            }
    )
}

@Preview(showSystemUi = true)
@Composable
fun TicTacToeGamePreview() {
    TicTacToeGame()
}