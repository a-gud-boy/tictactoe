package com.a_gud_boy.tictactoe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HelpPage(innerPadding: PaddingValues) {
    val gameRules = listOf(
        "1. The game is played on a grid that's 3 squares by 3 squares.",
        "2. You are X, your friend (or the computer) is O. Players take turns putting their marks in empty squares.",
        "3. The first player to get 3 of their marks in a row (up, down, across, or diagonally) is the winner.",
        "4. When all 9 squares are full, the game is over. If no player has 3 marks in a row, the game ends in a tie."
    )

    val faqs = listOf(
        "Q1: What is the objective of Tic Tac Toe?" to "A1: The objective is to be the first player to form a line of three of your own marks (either X or O) on a 3x3 grid. The line can be horizontal, vertical, or diagonal.",
        "Q2: How do I start a new game?" to "A2: Look for a \"New Game\" or \"Play Again\" button, usually available on the main screen or after a game ends.",
        "Q3: Can I play against the computer?" to "A3: Yes, you can play against an AI. You might be able to select the difficulty level in the settings.",
        "Q4: How is a tie game determined?" to "A4: A game is a tie if all the squares on the grid are filled, and neither player has successfully formed a line of three marks.",
        "Q5: Are there different modes of Tic Tac Toe in this app?" to "A5: Yes, this app appears to offer both a \"Normal\" (classic 3x3) and an \"Infinite\" version of Tic Tac Toe."
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.background))
            .padding(innerPadding),
        contentAlignment = Alignment.TopCenter
    ) {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 16.dp)
        ) {
            item {
                Text(
                    text = "Game Rules",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(gameRules.size) { index ->
                Text(
                    text = gameRules[index],
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Frequently Asked Questions",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(faqs.size) { index ->
                val (question, answer) = faqs[index]
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = question,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = answer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
