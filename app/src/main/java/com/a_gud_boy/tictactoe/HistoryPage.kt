package com.a_gud_boy.tictactoe

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete // Added import for Delete icon
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton // Added import for IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold // Added import for Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton // Added import for TextButton
import androidx.compose.material3.TopAppBar // Added import for TopAppBar
import androidx.compose.material3.TopAppBarDefaults // Added import for TopAppBarDefaults
import androidx.compose.material3.AlertDialog // Added import for AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf // Added import for mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue // Added import for setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Color Definitions (as per successful refactor of MatchHistoryItem)
val accentGreen = Color(0xFF4ADE80)
val accentRed = Color(0xFFF87171)
val accentYellow = Color(0xFFFACC15)
val neutralCardBg = Color(0xFFFFFFFF)
val neutralText = Color(0xFF1F2937)
val subtleText = Color(0xFF6B7280)

// Backgrounds for icons in MatchHistoryItem
val bgGreen100 = Color(0xFFDCFCE7)
val bgRed100 = Color(0xFFFEE2E2)
val bgYellow100 = Color(0xFFFEF9C3)
// These were also added in a later step, ensuring they are here for consistency if any other part uses them.

val designPrimaryColor = Color(0xFF141414)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchHistoryItem(
    matchWithRoundsAndMoves: MatchWithRoundsAndMoves,
    navController: NavHostController
) {
    val match = matchWithRoundsAndMoves.match

    val outcomeText: String
    val currentOutcomeColor: Color // Renamed from outcomeColor to avoid conflict with outer scope if any
    val iconToShow: androidx.compose.ui.graphics.vector.ImageVector
    val currentIconBackgroundColor: Color // Renamed from iconBackgroundColor

    when (match.winner) {
        MatchWinner.PLAYER1 -> {
            outcomeText = "Win"
            iconToShow = Icons.Filled.Check
            currentOutcomeColor = accentGreen // Use local/restored color name
            currentIconBackgroundColor = bgGreen100 // Use local/restored color name
        }

        MatchWinner.PLAYER2 -> {
            outcomeText = "Loss"
            iconToShow = Icons.Filled.Close
            currentOutcomeColor = accentRed // Use local/restored color name
            currentIconBackgroundColor = bgRed100 // Use local/restored color name
        }

        MatchWinner.DRAW -> {
            outcomeText = "Draw"
            iconToShow = Icons.Filled.Add
            currentOutcomeColor = accentYellow // Use local/restored color name
            currentIconBackgroundColor = bgYellow100 // Use local/restored color name
        }
    }

    val opponentDisplayName = if (match.isAgainstAi) "Computer" else "Player 2"
    val fullOpponentText = "vs. $opponentDisplayName"

    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val formattedDate = dateFormatter.format(Date(match.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("match_details/${match.matchId}") },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = neutralCardBg), // Use local/restored color name
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color = currentIconBackgroundColor, shape = CircleShape)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconToShow,
                    contentDescription = outcomeText,
                    tint = currentOutcomeColor,
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
                        color = currentOutcomeColor,
                        fontSize = 16.sp
                    )
                    Text(
                        text = formattedDate,
                        color = subtleText, // Use local/restored color name
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = fullOpponentText,
                    color = subtleText, // Use local/restored color name
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
    var showConfirmationDialog by remember { mutableStateOf(false) }

    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete all match history? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        historyViewModel.clearAllHistory()
                        showConfirmationDialog = false
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmationDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Match History", color = neutralText) },
                actions = {
                    IconButton(onClick = { showConfirmationDialog = true }) { // Updated onClick
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Clear All History",
                            tint = neutralText // Ensure icon is visible
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer // Or any other suitable color
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        if (matchHistory.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues), // Apply padding from Scaffold
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No match history yet.",
                    fontSize = 18.sp,
                    color = neutralText
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues), // Apply padding from Scaffold
                contentPadding = PaddingValues(16.dp), // Additional padding for content inside LazyColumn
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
}