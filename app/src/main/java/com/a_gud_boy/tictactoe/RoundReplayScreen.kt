package com.a_gud_boy.tictactoe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.focusable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.a_gud_boy.tictactoe.LocalViewModelFactory

@Composable
fun RoundReplayScreen(
    navController: NavController,
    matchId: Long,
    roundId: Long,
    roundReplayViewModel: RoundReplayViewModel = viewModel(factory = LocalViewModelFactory.current)
) {
    val focusRequester = remember { FocusRequester() }

    val currentGridState by roundReplayViewModel.currentGridState.collectAsState()
    val currentMoveIndex by roundReplayViewModel.currentMoveIndex.collectAsState()
    val moves by roundReplayViewModel.moves.collectAsState()

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
            top.linkTo(parent.top, margin = margin); start.linkTo(parent.start, margin = margin); end.linkTo(button2.start, margin = margin); bottom.linkTo(button4.top, margin = margin)
        }
        constrain(button2) {
            top.linkTo(parent.top, margin = margin); start.linkTo(button1.end, margin = margin); end.linkTo(button3.start, margin = margin); bottom.linkTo(button5.top, margin = margin)
        }
        constrain(button3) {
            top.linkTo(parent.top, margin = margin); start.linkTo(button2.end, margin = margin); end.linkTo(parent.end, margin = margin); bottom.linkTo(button6.top, margin = margin)
        }
        constrain(button4) {
            top.linkTo(button1.bottom, margin = margin); start.linkTo(parent.start, margin = margin); end.linkTo(button5.start, margin = margin); bottom.linkTo(button7.top, margin = margin)
        }
        constrain(button5) {
            top.linkTo(button2.bottom, margin = margin); start.linkTo(button4.end, margin = margin); end.linkTo(button6.start, margin = margin); bottom.linkTo(button8.top, margin = margin)
        }
        constrain(button6) {
            top.linkTo(button3.bottom, margin = margin); start.linkTo(button5.end, margin = margin); end.linkTo(parent.end, margin = margin); bottom.linkTo(button9.top, margin = margin)
        }
        constrain(button7) {
            top.linkTo(button4.bottom, margin = margin); start.linkTo(parent.start, margin = margin); end.linkTo(button8.start, margin = margin); bottom.linkTo(parent.bottom, margin = margin)
        }
        constrain(button8) {
            top.linkTo(button5.bottom, margin = margin); start.linkTo(button7.end, margin = margin); end.linkTo(button9.start, margin = margin); bottom.linkTo(parent.bottom, margin = margin)
        }
        constrain(button9) {
            top.linkTo(button6.bottom, margin = margin); start.linkTo(button8.end, margin = margin); end.linkTo(parent.end, margin = margin); bottom.linkTo(parent.bottom, margin = margin)
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

        Text(
            text = "Replaying Match ID: $matchId, Round ID: $roundId",
            modifier = Modifier.padding(bottom = 8.dp)
        )
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
                .padding(10.dp) // Added padding around the grid cells
        ) {
            val buttonIds = List(9) { i -> "button${i + 1}" }
            buttonIds.forEach { buttonId ->
                TicTacToeCell(
                    modifier = Modifier
                        .layoutId(buttonId)
                        // Individual cell background can be set here if needed, e.g., .background(Color.White, RoundedCornerShape(8.dp))
                        // For now, using the ConstraintLayout background.
                        .width(80.dp) // Adjust size as needed, considering padding
                        .height(80.dp),// Adjust size as needed, considering padding
                    player = currentGridState[buttonId],
                    isOldMove = false, // This could be enhanced later if needed
                    iconSize = iconSize,
                    buttonId = buttonId,
                    onClick = {
                        // Click listener is empty for now, replay controlled by arrows
                    }
                )
            }
        }
        // Placeholder for navigation buttons (Next/Previous move)
        // Will be added in a later step
        // For example, actual buttons could also call viewModel.previousMove() and viewModel.nextMove()
    }
}
