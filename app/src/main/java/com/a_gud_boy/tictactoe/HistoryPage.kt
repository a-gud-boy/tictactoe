package com.a_gud_boy.tictactoe

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
// It seems Icons.AutoMirrored.Filled.ArrowBack is not available directly.
// If needed, it would typically be: import androidx.compose.material.icons.automirrored.filled.ArrowBack
// For now, I will remove the navigationIcon from TopAppBar as it was commented out in the prompt.
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.lifecycle.viewmodel.compose.viewModel // For viewModel() composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryPage(
    innerPadding: PaddingValues,
    showClearConfirmDialog: Boolean, // New parameter for state
    onShowClearConfirmDialogChange: (Boolean) -> Unit, // New parameter for state change
    historyViewModel: HistoryViewModel = viewModel(factory = LocalViewModelFactory.current),
) {
    val matchHistory by historyViewModel.matchHistory.collectAsState()
    // Removed local state: var showClearConfirmDialog by remember { mutableStateOf(false) }

    // Scaffold has been removed
    // TopAppBar and its actions have been removed

    // The main content Column now uses innerPadding
    Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
        if (matchHistory.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No match history yet.", fontSize = 18.sp)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(matchHistory) { matchWithRoundsAndMoves ->
                        MatchHistoryItem(matchWithRoundsAndMoves = matchWithRoundsAndMoves)
                    }
                }
            }
        }
    } // This curly brace was the end of the Scaffold's content lambda, now it's the end of the Column

    // The AlertDialog now uses the passed-in state and lambda
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { onShowClearConfirmDialogChange(false) }, // Use lambda
            title = { Text("Clear History") },
            text = { Text("Are you sure you want to delete all match history? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        historyViewModel.clearAllHistory()
                        onShowClearConfirmDialogChange(false) // Use lambda
                    }
                ) { Text("Clear All") }
            },
            dismissButton = {
                Button(onClick = { onShowClearConfirmDialogChange(false) }) { Text("Cancel") } // Use lambda
            }
        )
    }
}

@Composable
fun MatchHistoryItem(matchWithRoundsAndMoves: MatchWithRoundsAndMoves) {
    var expanded by remember { mutableStateOf(false) }
    val match = matchWithRoundsAndMoves.match

    // Date formatter
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable { expanded = !expanded },
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
                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }
            Text(
                text = "Date: ${dateFormatter.format(Date(match.timestamp))}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Score: P1 (${match.player1Score}) - P2 (${match.player2Score})",
                style = MaterialTheme.typography.bodySmall
            )

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                matchWithRoundsAndMoves.roundsWithMoves.forEach { roundWithMoves ->
                    RoundHistoryItem(roundWithMoves = roundWithMoves)
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                }
                if (matchWithRoundsAndMoves.roundsWithMoves.isEmpty()){
                     Text("No rounds recorded for this match.", style = MaterialTheme.typography.bodySmall)
                }
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
                    text = "  ${index + 1}. Player ${move.player} -> Cell ${move.cellId.replace("button", "")}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        } else {
             Text("  No moves recorded for this round.", style = MaterialTheme.typography.bodySmall)
        }
    }
}
