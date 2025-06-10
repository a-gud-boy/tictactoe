package com.a_gud_boy.tictactoe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Correct import for ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember // Added for SimpleDateFormat
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
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
fun MatchDetailsPage(
    navController: NavController,
    matchDetailsViewModel: MatchDetailsViewModel = viewModel(factory = LocalViewModelFactory.current)
) {
    val matchWithRoundsAndMoves by matchDetailsViewModel.matchDetails.collectAsState()
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Match Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(R.color.background),
                    titleContentColor = colorResource(R.color.darkTextColor),
                    navigationIconContentColor = colorResource(R.color.darkTextColor)
                )
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            matchWithRoundsAndMoves?.let { details ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colorResource(R.color.background))
                        .padding(8.dp)
                ) {
                    item {
                        MatchSummaryCard(match = details.match, dateFormatter = dateFormatter)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    if (details.roundsWithMoves.isNotEmpty()) {
                        items(details.roundsWithMoves.size) { index ->
                            RoundHistoryItem(roundWithMoves = details.roundsWithMoves[index]) // Re-use existing RoundHistoryItem
                            if (index < details.roundsWithMoves.size - 1) {
                                Divider(modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    } else {
                        item {
                            Text(
                                "No rounds recorded for this match.",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            } ?: run {
                // Show loading indicator or empty state if needed
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator() // Or Text("Loading match details...") or Text("Match not found.")
                }
            }
        }
    }
}

@Composable
fun MatchSummaryCard(match: MatchEntity, dateFormatter: SimpleDateFormat) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Match #${match.matchNumber} - ${match.matchWinnerName}",
                style = MaterialTheme.typography.titleLarge, // Larger title for details page
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Date: ${dateFormatter.format(Date(match.timestamp))}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Final Score: P1 (${match.player1Score}) - P2 (${match.player2Score})",
                style = MaterialTheme.typography.bodyMedium
            )
            // Add any other summary information from MatchEntity if desired
        }
    }
}

// Note: RoundHistoryItem is already defined in HistoryPage.kt.
// If it's intended to be reused and is general enough, ensure it's accessible.
// For this subtask, we assume RoundHistoryItem from HistoryPage.kt can be used.
// If it needs to be duplicated or moved to a common file, that would be a separate consideration.
// For now, this code expects RoundHistoryItem to be available in this scope,
// which might mean moving it or making it public if it's not already.
// For the purpose of this subtask, we'll assume it's accessible.
// If `RoundHistoryItem` is not directly accessible, the subtask should copy its implementation
// from `HistoryPage.kt` into `MatchDetailsPage.kt` or a shared file.
// Let's assume for now it needs to be copied to avoid inter-dependency issues during generation.

/*
// Copied from HistoryPage.kt for Round details display (if not accessible directly)
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
*/
