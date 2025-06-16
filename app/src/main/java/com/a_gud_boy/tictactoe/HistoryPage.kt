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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Import viewModel if HistoryPageContent were to instantiate it, but it's passed now.
// import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.a_gud_boy.tictactoe.database.MatchWithRoundsAndMoves
import java.text.SimpleDateFormat
import java.util.Locale

// Color Definitions (as per subtask)
val designAccentGreen = Color(0xFF4ADE80)
val designAccentRed = Color(0xFFF87171)
val designAccentYellow = Color(0xFFFACC15)
val designNeutralCardBg = Color(0xFFFFFFFF) // Used for cards, header, footer
val designNeutralText = Color(0xFF1F2937)    // Main text color
val designSubtleText = Color(0xFF6B7280)     // Secondary text color
val designBorderColor = Color(0xFFE5E7EB)    // For dividers
val designNeutralBg = Color(0xFFF8F8F8)      // Screen background
val designPrimaryColor = Color(0xFF141414)   // Active nav icon, header icon text

// Backgrounds for icons (from previous step, ensure they use 'design' prefix for clarity if needed)
val designIconBgGreen = Color(0xFFDCFCE7)
val designIconBgRed = Color(0xFFFEE2E2)
val designIconBgYellow = Color(0xFFFEF9C3)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchHistoryItem(
    matchWithRoundsAndMoves: MatchWithRoundsAndMoves,
    navController: NavHostController
) {
    val match = matchWithRoundsAndMoves.match

    val outcomeText: String
    val outcomeIcon: androidx.compose.ui.graphics.vector.ImageVector
    val iconColor: Color
    val iconBackgroundColor: Color

    when {
        match.winnerName == match.player1Name -> {
            outcomeText = "Win"
            outcomeIcon = Icons.Filled.Check
            iconColor = designAccentGreen
            iconBackgroundColor = designIconBgGreen
        }
        match.winnerName == "Draw" -> {
            outcomeText = "Draw"
            outcomeIcon = Icons.Filled.Remove
            iconColor = designAccentYellow
            iconBackgroundColor = designIconBgYellow
        }
        else -> {
            outcomeText = "Loss"
            outcomeIcon = Icons.Filled.Close
            iconColor = designAccentRed
            iconBackgroundColor = designIconBgRed
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("match_details/${match.matchId}") },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = designNeutralCardBg), // Use design color
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color = iconBackgroundColor, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = outcomeIcon,
                    contentDescription = outcomeText,
                    tint = iconColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = outcomeText,
                        fontWeight = FontWeight.SemiBold,
                        color = designNeutralText, // Use design color
                        fontSize = 16.sp
                    )
                    Text(
                        text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(match.timestamp),
                        color = designSubtleText, // Use design color
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))

                val opponentDetails = when {
                    match.isAgainstAi -> "vs. Computer (${match.aiDifficulty ?: "Standard"})"
                    match.player2Name != null && match.player2Name!!.isNotBlank() -> "vs. ${match.player2Name}"
                    else -> "vs. Player 2"
                }
                Text(
                    text = opponentDetails,
                    color = designSubtleText, // Use design color
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
    modifier: Modifier = Modifier // Note: background is set by GameHistoryScreen
) {
    val matchHistory by historyViewModel.matchHistory.collectAsState()

    if (matchHistory.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(), // Background will be designNeutralBg from GameHistoryScreen
            contentAlignment = Alignment.Center
        ) {
            Text("No match history yet.", fontSize = 18.sp, color = designNeutralText) // Use design color
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(), // Background will be designNeutralBg from GameHistoryScreen
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