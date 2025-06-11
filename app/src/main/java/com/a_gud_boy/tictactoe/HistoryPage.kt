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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    // No Scaffold or TopAppBar here

    Column( // Or Box, whatever was the root content
        modifier = Modifier
            .padding(
                start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                bottom = innerPadding.calculateBottomPadding()
            )
            .fillMaxSize()
            .background(colorResource(R.color.background)) // Keep background if needed
    ) {
        if (matchHistory.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No match history yet.", fontSize = 18.sp)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) { // This LazyColumn might not need another .fillMaxSize() if parent Column has it
                items(matchHistory) { matchWithRoundsAndMoves ->
                    MatchHistoryItem(
                        matchWithRoundsAndMoves = matchWithRoundsAndMoves,
                        navController = navController
                    )
                }
            }
        }


        // The AlertDialog for clearing history is still triggered by showClearConfirmDialog
        // which is a state managed in MainPage and passed down.
        if (showClearConfirmDialog) {
            AlertDialog(
                onDismissRequest = { onShowClearConfirmDialogChange(false) },
                title = { Text("Clear History") },
                text = { Text("Are you sure you want to delete all match history? This action cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = {
                            historyViewModel.clearAllHistory()
                            onShowClearConfirmDialogChange(false)
                        }
                    ) { Text("Clear All") }
                },
                dismissButton = {
                    Button(onClick = { onShowClearConfirmDialogChange(false) }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun MatchHistoryItem(
    matchWithRoundsAndMoves: MatchWithRoundsAndMoves,
    navController: NavController // Added NavController
) {
    // var expanded by remember { mutableStateOf(false) } // REMOVED

    val match = matchWithRoundsAndMoves.match
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable {
                // Navigate to details page, passing matchId
                navController.navigate("match_details/${match.matchId}")
            }, // MODIFIED
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Match #${match.matchNumber} - ${match.matchWinnerName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                // Icon for expand/collapse REMOVED
            }
            Text(
                text = "Date: ${dateFormatter.format(Date(match.timestamp))}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Score: P1 (${match.player1Score}) - P2 (${match.player2Score})",
                style = MaterialTheme.typography.bodySmall
            )

            // if (expanded) { ... } block REMOVED
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


