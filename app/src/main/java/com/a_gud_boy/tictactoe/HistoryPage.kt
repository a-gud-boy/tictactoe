package com.a_gud_boy.tictactoe

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme // Keep if Card shape relies on it
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember // For SimpleDateFormat
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.a_gud_boy.tictactoe.database.MatchWithRoundsAndMoves
import java.text.SimpleDateFormat
import java.util.Date // For Date object from timestamp
import java.util.Locale

// Color Definitions
val designAccentGreen = Color(0xFF4ADE80)
val designAccentRed = Color(0xFFF87171)
val designAccentYellow = Color(0xFFFACC15)
val designNeutralCardBg = Color(0xFFFFFFFF)
val designNeutralText = Color(0xFF1F2937)
val designSubtleText = Color(0xFF6B7280)
val designBorderColor = Color(0xFFE5E7EB)
val designNeutralBg = Color(0xFFF8F8F8)
val designPrimaryColor = Color(0xFF141414)

// Backgrounds for icons (ensure these use design prefix or are clear)
val bgGreen100 = Color(0xFFDCFCE7) // Renamed from designIconBgGreen for consistency with user's example
val bgRed100 = Color(0xFFFEE2E2)   // Renamed from designIconBgRed
val bgYellow100 = Color(0xFFFEF9C3) // Renamed from designIconBgYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchHistoryItem(
    matchWithRoundsAndMoves: MatchWithRoundsAndMoves,
    navController: NavHostController
) {
    val match = matchWithRoundsAndMoves.match

    // Define all derived properties at the top
    val outcomeText = when (match.winner) {
        MatchWinner.PLAYER1 -> "Win"
        MatchWinner.PLAYER2 -> "Loss" // Assuming PLAYER1 is "You"
        MatchWinner.DRAW -> "Draw"
    }
    val outcomeColor = when (match.winner) {
        MatchWinner.PLAYER1 -> designAccentGreen
        MatchWinner.PLAYER2 -> designAccentRed
        MatchWinner.DRAW -> designAccentYellow
    }
    val iconToShow = when (match.winner) {
        MatchWinner.PLAYER1 -> Icons.Filled.Check
        MatchWinner.PLAYER2 -> Icons.Filled.Close
        MatchWinner.DRAW -> Icons.Filled.Remove
    }
    val iconBackgroundColor = when (match.winner) {
        MatchWinner.PLAYER1 -> bgGreen100
        MatchWinner.PLAYER2 -> bgRed100
        MatchWinner.DRAW -> bgYellow100
    }

    // Opponent text logic from previous correct step
    val opponentDisplayName = if (match.isAgainstAi) {
        "Computer" + (match.aiDifficulty?.let { " ($it)" } ?: "")
    } else {
        match.player2Name?.takeIf { it.isNotBlank() } ?: "Player 2"
    }
    val fullOpponentText = "vs. $opponentDisplayName"


    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val formattedDate = dateFormatter.format(Date(match.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("match_details/${match.matchId}") },
        shape = MaterialTheme.shapes.medium, // Ensure MaterialTheme is M3 if Card shape depends on it
        colors = CardDefaults.cardColors(containerColor = designNeutralCardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            // Icon Element
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color = iconBackgroundColor, shape = CircleShape) // Use iconBackgroundColor
                    .padding(8.dp), // Padding for the icon within its background circle
                contentAlignment = Alignment.Center // Center the icon inside the Box
            ) {
                Icon(
                    imageVector = iconToShow, // Use iconToShow
                    contentDescription = outcomeText, // Use outcomeText for content description
                    tint = outcomeColor, // Use outcomeColor for icon tint
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text Details Element
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = outcomeText, // Use outcomeText
                        fontWeight = FontWeight.SemiBold,
                        color = outcomeColor, // Use outcomeColor
                        fontSize = 16.sp
                    )
                    Text(
                        text = formattedDate,
                        color = designSubtleText,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = fullOpponentText,
                    color = designSubtleText,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun HistoryPageContent(
    historyViewModel: HistoryViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val matchHistory by historyViewModel.matchHistory.collectAsState()

    if (matchHistory.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No match history yet.", fontSize = 18.sp, color = designNeutralText)
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(matchHistory) { matchWithRoundsAndMoves ->
                MatchHistoryItem(
                    matchWithRoundsAndMoves = matchWithRoundsAndMoves,
                    navController = navController
                )
            }
        }
    }
}