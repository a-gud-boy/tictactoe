package com.a_gud_boy.tictactoe

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton // For FAB
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color // Import Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.LayoutDirection
import android.text.format.DateUtils // Import for relative time
import androidx.compose.foundation.BorderStroke // Required for border
import androidx.compose.foundation.border // Required for border
import androidx.compose.foundation.layout.size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Define colors for win, loss, and draw
val winColor = Color(0xFF4CAF50) // Green
val lossColor = Color(0xFFF44336) // Red
val drawColor = Color(0xFF9E9E9E) // Gray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryPage(
    innerPadding: PaddingValues, // RE-ADDED
    showClearConfirmDialog: Boolean,
    onShowClearConfirmDialogChange: (Boolean) -> Unit,
    historyViewModel: HistoryViewModel = viewModel(factory = LocalViewModelFactory.current),
    navController: NavController
    // onShowInfoDialog: (title: String, message: String) -> Unit // REMOVED
) {
    val matchHistory by historyViewModel.matchHistory.collectAsState()
    val statistics by historyViewModel.matchStatistics.collectAsState() // Collect statistics
    var showDeleteMatchConfirmDialog by remember { mutableStateOf<MatchWithRoundsAndMoves?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.background))
    ) { // Root Box for FAB alignment
        Column(
            modifier = Modifier
                .padding(
                    start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                    end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                    bottom = innerPadding.calculateBottomPadding()
                )
                .fillMaxSize()
        ) {
            if (matchHistory.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(), // Takes full space of the Column
                    contentAlignment = Alignment.Center
                ) {
                    Text("No match history yet.", fontSize = 18.sp)
                    // OverallStatsSection will not be shown here as per logic below
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) { // LazyColumn takes available space
                    items(matchHistory) { matchWithRoundsAndMoves ->
                        MatchHistoryItem(
                            matchWithRoundsAndMoves = matchWithRoundsAndMoves,
                            navController = navController,
                            onDeleteClicked = {
                                showDeleteMatchConfirmDialog = matchWithRoundsAndMoves
                            }
                        )
                    }
                }
                OverallStatsSection(stats = statistics) // Display stats below the list
            }
        }

        // Dialogs remain at this level, within the Box but outside the Column
        if (showClearConfirmDialog) {
            AlertDialog(
                onDismissRequest = { onShowClearConfirmDialogChange(false) },
                title = { Text("Clear All History") },
                text = { Text("Are you sure you want to delete all match history? This action cannot be undone.") },
                confirmButton = {
                    Button(onClick = {
                        historyViewModel.clearAllHistory()
                        onShowClearConfirmDialogChange(false)
                    }) { Text("Clear All") }
                },
                dismissButton = {
                    Button(onClick = { onShowClearConfirmDialogChange(false) }) { Text("Cancel") }
                }
            )
        }

        // Dialog for deleting a SINGLE match
        showDeleteMatchConfirmDialog?.let { matchToDelete ->
            AlertDialog(
                onDismissRequest = { showDeleteMatchConfirmDialog = null },
                title = { Text("Delete Match") },
                text = { Text("Are you sure you want to delete this match history? This action cannot be undone.") },
                confirmButton = {
                    Button(onClick = {
                        historyViewModel.deleteMatch(matchToDelete)
                        showDeleteMatchConfirmDialog = null
                    }) { Text("Delete") }
                },
                dismissButton = {
                    Button(onClick = { showDeleteMatchConfirmDialog = null }) { Text("Cancel") }
                }
            )
        }
    } // End of Root Box
}

@Composable
fun OverallStatsSection(stats: MatchStatistics) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(colorResource(R.color.constraint_background))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Overall Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Total Matches: ${stats.totalMatches}")
            Text("You Won: ${stats.playerWins}")
            Text("AI Won: ${stats.aiWins}")
            Text("Draws: ${stats.draws}")
        }
    }
}

@Composable
fun MatchHistoryItem(
    matchWithRoundsAndMoves: MatchWithRoundsAndMoves,
    navController: NavController,
    onDeleteClicked: () -> Unit // New parameter
) {
    val match = matchWithRoundsAndMoves.match
    val opponentName = if (match.isAgainstAi) "AI" else "Player 2" // Added opponentName
    // Updated date formatter for 12-hour format with AM/PM
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()) }

    val matchTimeMillis = match.timestamp
    val now = System.currentTimeMillis()
    val timeToDisplay = if (now - matchTimeMillis < 2 * 24 * 60 * 60 * 1000) { // Less than 2 days
        DateUtils.getRelativeTimeSpanString(
            matchTimeMillis,
            now,
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE // Use abbreviated format like "2 hr. ago"
        ).toString()
    } else {
        dateFormatter.format(Date(matchTimeMillis))
    }

    val (textColor, borderColor) = when (match.winner) {
        MatchWinner.PLAYER1 -> colorResource(R.color.numberOfWinsTextColor_x) to winColor
        MatchWinner.PLAYER2 -> colorResource(R.color.numberOfWinsTextColor_o) to lossColor
        MatchWinner.DRAW -> colorResource(R.color.darkTextColor) to drawColor
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp) // Apply vertical padding to the Row
            .clickable {
                navController.navigate("match_details/${match.matchId}")
            }
    ) {
        Box(
            modifier = Modifier
                .width(6.dp)
                .fillMaxHeight()
                .background(borderColor)
        )
        Card(
            modifier = Modifier
                .weight(1f) // Card takes remaining space
                .padding(
                    start = 8.dp,
                    end = 8.dp
                ), // Padding for the card itself, if needed, but might be better on content
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = colorResource(R.color.constraint_background))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Match #${match.matchNumber} - ${match.matchWinnerName}",
                        color = textColor,
                        fontSize = 20.sp, // Increased font size
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f) // Allow text to take space
                    )
                    IconButton(onClick = onDeleteClicked) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete Match",
//                            tint = MaterialTheme.colorScheme.error // Optional: color the icon
                        )
                    }
                }
                Text(
                    text = "Date: $timeToDisplay", // Use the new timeToDisplay string
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "You: ${match.player1Score} – $opponentName: ${match.player2Score}", // Updated score label
                    style = MaterialTheme.typography.bodySmall
                )

                // if (expanded) { ... } block REMOVED
            }
        }
    }
}


@Composable
fun RoundHistoryItem(roundWithMoves: RoundWithMoves) {
    val round = roundWithMoves.round
    val movesCount = roundWithMoves.moves.size
    val winnerText = if (round.roundWinnerName == "Draw") "Draw" else "${round.roundWinnerName} Won"

    Column(modifier = Modifier.padding(16.dp)) { // Increased padding for Card content
        Text(
            text = "Round ${round.roundNumber}: $winnerText in $movesCount moves",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        // Spacer(modifier = Modifier.height(4.dp)) // Original Spacer, can be adjusted or removed
        if (roundWithMoves.moves.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp)) // Add more space before move list
            roundWithMoves.moves.forEachIndexed { index, move ->
                val playerName = if (move.player == "X") "You" else "AI" // Assuming X is You, O is AI

                val cellNumber = move.cellId.replace("button", "")
                val descriptiveCellName = when (cellNumber) {
                    "1" -> "top-left"
                    "2" -> "top-center"
                    "3" -> "top-right"
                    "4" -> "middle-left"
                    "5" -> "middle-center"
                    "6" -> "middle-right"
                    "7" -> "bottom-left"
                    "8" -> "bottom-center"
                    "9" -> "bottom-right"
                    else -> "Cell $cellNumber" // Fallback for unknown cellId
                }

                Text(
                    text = "  ${index + 1}. $playerName placed ${move.player} in $descriptiveCellName",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        } else {
            Spacer(modifier = Modifier.height(8.dp)) // Add space even if no moves
            Text("  No moves recorded for this round.", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun GameBoard(moves: List<MoveEntity>, playerXSymbol: String = "X", playerOSymbol: String = "O") {
    val boardSize = 3
    val boardState = remember { Array(boardSize) { arrayOfNulls<String>(boardSize) } }

    // Reset board state for each recomposition if moves change
    for (i in 0 until boardSize) {
        for (j in 0 until boardSize) {
            boardState[i][j] = null
        }
    }

    moves.forEach { move ->
        val cellNum = move.cellId.replace("button", "").toIntOrNull()
        if (cellNum != null && cellNum in 1..(boardSize * boardSize)) {
            val row = (cellNum - 1) / boardSize
            val col = (cellNum - 1) % boardSize
            boardState[row][col] = move.player
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        for (i in 0 until boardSize) {
            Row {
                for (j in 0 until boardSize) {
                    val symbol = boardState[i][j]
                    val cellColor = when (symbol) {
                        playerXSymbol -> MaterialTheme.colorScheme.primary // Blue for X (You)
                        playerOSymbol -> MaterialTheme.colorScheme.error   // Red for O (AI)
                        else -> MaterialTheme.colorScheme.onSurface // Default color for empty or other
                    }
                    Box(
                        modifier = Modifier
                            .size(48.dp) // Cell size
                            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)))
                            .padding(4.dp), // Padding inside the cell
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = symbol ?: "",
                            fontSize = 32.sp, // Large font for X/O
                            color = cellColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}