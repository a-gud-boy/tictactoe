package com.a_gud_boy.tictactoe

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailsPage(
    innerPadding: PaddingValues, // Added innerPadding parameter
    navController: NavController, // navController is still needed for internal logic if any, or can be removed if MainPage handles all nav
    matchDetailsViewModel: MatchDetailsViewModel = viewModel(factory = LocalViewModelFactory.current)
) {
    val matchWithRoundsAndMoves by matchDetailsViewModel.matchDetails.collectAsState()
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()) }

    // Removed Scaffold and TopAppBar

    Box(
        modifier = Modifier
            .padding(
                start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                bottom = innerPadding.calculateBottomPadding()
            )
            .fillMaxSize()
    ) { // Apply specific padding components here
        matchWithRoundsAndMoves?.let { details ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colorResource(R.color.background))
                    .padding(8.dp) // This padding is for the content within the LazyColumn itself
            ) {
                item {
                    MatchSummaryCard(match = details.match, dateFormatter = dateFormatter)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (details.roundsWithMoves.isNotEmpty()) {
                    items(details.roundsWithMoves.size) { index ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(containerColor = colorResource(R.color.constraint_background))
                        ) {
                            RoundHistoryItem(
                                roundWithMoves = details.roundsWithMoves[index],
                                navController = navController,
                                matchId = details.match.matchId,
                                gameType = details.match.gameType // Pass the GameType enum instance
                            )
                        }
                        if (index < details.roundsWithMoves.size - 1) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
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
    // Removed closing brace for Scaffold
}

@Composable
fun MatchSummaryCard(match: MatchEntity, dateFormatter: SimpleDateFormat) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.constraint_background))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Determine result text color and icon based on winner
            val resultColor: Color
            val resultIcon: androidx.compose.ui.graphics.vector.ImageVector?

            when (match.matchWinnerName) {
                "You Won" -> {
                    resultColor = MaterialTheme.colorScheme.primary // Greenish color from theme
                    resultIcon = Icons.Filled.Check
                }

                "AI Won" -> {
                    resultColor = MaterialTheme.colorScheme.error // Reddish color from theme
                    resultIcon = Icons.Filled.Clear
                }

                else -> { // Draw or other states
                    resultColor = MaterialTheme.colorScheme.onSurface // Neutral color
                    resultIcon = null // No icon for draw
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = match.matchWinnerName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = resultColor
                )
                resultIcon?.let {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = it,
                        contentDescription = "Match Result Icon",
                        tint = resultColor,
                        modifier = Modifier.size(24.dp) // Adjust size as needed
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp)) // Increased spacing
            Text(
                text = "Date: ${dateFormatter.format(Date(match.timestamp))}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp)) // Consistent spacing

            // Row for Player Icons and Score
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Player Icon
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "You Icon",
                    modifier = Modifier.size(20.dp) // Slightly smaller icon for score line
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "You: ${match.player1Score}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.width(16.dp)) // Space between player scores

                // AI Icon
                Icon(
                    painter = painterResource(R.drawable.ai), // Or Icons.Filled.Adb as fallback
                    contentDescription = "AI Icon",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "AI: ${match.player2Score}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
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


// Copied from HistoryPage.kt for Round details display (if not accessible directly)
@Composable
fun RoundHistoryItem(
    roundWithMoves: RoundWithMoves,
    navController: NavController,
    matchId: Long,
    gameType: GameType // Changed to GameType enum
) {
    val round = roundWithMoves.round
    var expanded by remember { mutableStateOf(false) } // State for expansion

    Column(
        modifier = Modifier
            .padding(start = 8.dp, top = 8.dp, bottom = 8.dp)
            .fillMaxWidth() // Ensure the clickable area covers the width
            .clickable {
                navController.navigate("roundReplay/${matchId}/${round.roundId}/${gameType.name}") // Use gameType.name
            }
    ) {
        Text(
            text = "Round ${round.roundNumber}: ${round.roundWinnerName}",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))

        val moves = roundWithMoves.moves
        if (moves.isNotEmpty()) {
            val itemsToDisplay = if (expanded || moves.size <= 5) moves else moves.take(5)

            itemsToDisplay.forEachIndexed { index, move ->
                val moveText = "  ${index + 1}. Player ${move.player} -> Cell ${move.cellId.replace("button", "")}"

                if (moves.size > 5 && index == itemsToDisplay.lastIndex) {
                    // This is the last item being displayed, and there are more moves than 5 (implying it's either the 5th item or the actual last item)
                    val annotatedText = buildAnnotatedString {
                        append(moveText)
                        pushStringAnnotation(tag = "ACTION", annotation = if (expanded) "LESS" else "MORE")
                        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) {
                            append(if (expanded) " view less" else " view more")
                        }
                        pop()
                    }
                    ClickableText(
                        text = annotatedText,
                        style = MaterialTheme.typography.bodySmall,
                        onClick = { offset ->
                            annotatedText.getStringAnnotations(tag = "ACTION", start = offset, end = offset)
                                .firstOrNull()?.let {
                                    expanded = !expanded // Toggle expansion
                                }
                        }
                    )
                } else {
                    Text(text = moveText, style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            Text("  No moves recorded for this round.", style = MaterialTheme.typography.bodySmall)
        }
    }
}
