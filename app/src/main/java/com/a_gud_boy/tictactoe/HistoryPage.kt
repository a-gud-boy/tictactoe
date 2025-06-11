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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination // For FAB navigation
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

        // FloatingActionButton
        FloatingActionButton(
            onClick = {
                navController.navigate("MainPage") { // Ensure "MainPage" is correct
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp) // Padding for the FAB itself
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = "Play Again")
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
    Column(modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 8.dp)) {
        Text(
            text = "Round ${round.roundNumber}: ${round.roundWinnerName}",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        if (roundWithMoves.moves.isNotEmpty()) {
            roundWithMoves.moves.forEachIndexed { index, move ->
                Text(
                    text = "  ${index + 1}. Player ${move.player} -> Cell ${
                        move.cellId.replace(
                            "button",
                            ""
                        )
                    }",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        } else {
            Text("  No moves recorded for this round.", style = MaterialTheme.typography.bodySmall)
        }
    }
}


