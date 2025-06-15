package com.a_gud_boy.tictactoe

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
// import androidx.navigation.NavController // Uncomment when NavController is available
import com.a_gud_boy.tictactoe.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsPage(
    historyViewModel: HistoryViewModel = viewModel(factory = LocalViewModelFactory.current)
    // navController: NavController // Uncomment when NavController is available
) {
    val matchStatistics by historyViewModel.matchStatistics.collectAsState()

    Scaffold(
        topBar = {
            AppHeader(
                title = "Statistics",
                onBackClicked = { /* TODO: Implement navigation */ }
            )
        },
        containerColor = surfaceSecondary
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OverallStatsSection(stats = matchStatistics)
            Spacer(modifier = Modifier.height(24.dp))
            GameOutcomesSection(stats = matchStatistics)
            Spacer(modifier = Modifier.height(24.dp))
            GameResultsBreakdownSection(stats = matchStatistics)
        }
    }
}

@Composable
fun AppHeader(title: String, onBackClicked: () -> Unit) {
    TopAppBar(
        title = { Text(text = title, color = textPrimary) },
        navigationIcon = {
            IconButton(onClick = onBackClicked) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = textPrimary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = surfacePrimary)
    )
}

@Composable
fun OverallStatsSection(stats: MatchStatistics) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            StatisticCard(title = "Total Games Played", value = stats.totalMatches.toString())
        }
        item {
            StatisticCard(title = "Win Rate", value = String.format("%.1f%%", stats.winRate), valueColor = primaryColor)
        }
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
            StatisticCard(title = "Average Game Duration", value = stats.averageGameDuration)
        }
    }
}

@Composable
fun GameOutcomesSection(stats: MatchStatistics) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            StatisticCard(title = "Games Won", value = stats.playerWins.toString(), valueColor = primaryColor)
        }
        item {
            StatisticCard(title = "Games Lost", value = stats.aiWins.toString(), valueColor = Color(0xFFEF4444))
        }
        item {
            StatisticCard(title = "Games Drawn", value = stats.draws.toString(), valueColor = Color(0xFFEAB308))
        }
    }
}

@Composable
fun GameResultsBreakdownSection(stats: MatchStatistics) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Game Results Breakdown",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = textPrimary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        val totalGames = stats.totalMatches.toFloat()
        val wonPercentage = if (totalGames == 0f) 0f else (stats.playerWins / totalGames)
        val lostPercentage = if (totalGames == 0f) 0f else (stats.aiWins / totalGames)
        val drawnPercentage = if (totalGames == 0f) 0f else (stats.draws / totalGames)

        val barData = listOf(
            BarData("Won", wonPercentage, primaryColor, "Won (${(wonPercentage * 100).toPrettyPercentage()}%)", stats.playerWins),
            BarData("Lost", lostPercentage, Color(0xFFEF4444), "Lost (${(lostPercentage * 100).toPrettyPercentage()}%)", stats.aiWins),
            BarData("Drawn", drawnPercentage, Color(0xFFEAB308), "Drawn (${(drawnPercentage * 100).toPrettyPercentage()}%)", stats.draws)
        )

        // Parent Row for Y-axis and Bars
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp) // Adjust height as needed
        ) {
            // Y-axis Column
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(40.dp) // Fixed width for Y-axis labels
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End // Align text to the right for Y-axis
            ) {
                Text("100%", fontSize = 10.sp, color = textSecondary, textAlign = TextAlign.End)
                Text("75%", fontSize = 10.sp, color = textSecondary, textAlign = TextAlign.End)
                Text("50%", fontSize = 10.sp, color = textSecondary, textAlign = TextAlign.End)
                Text("25%", fontSize = 10.sp, color = textSecondary, textAlign = TextAlign.End)
                Text("0%", fontSize = 10.sp, color = textSecondary, textAlign = TextAlign.End)
            }

            // Bars Row
            Row(
                modifier = Modifier
                    .weight(1f) // Takes remaining horizontal space
                    .fillMaxHeight(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                barData.forEach { data ->
                    Bar(data = data, count = data.count)
                }
            }
        }
        // X-axis baseline
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp), // Space between bar labels and the line
            thickness = 1.dp,
            color = borderPrimary
        )
    }
}

@Composable
fun RowScope.Bar(data: BarData, count: Int) {
    var animationPlayed by remember { mutableStateOf(false) }
    val barHeightPercentage by animateFloatAsState(
        targetValue = if (animationPlayed) data.value else 0f,
        animationSpec = tween(durationMillis = 1000), label = "${data.label}BarAnimation"
    )

    LaunchedEffect(Unit) {
        animationPlayed = true
    }

    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .padding(horizontal = 4.dp) // Add some spacing between bars
            .semantics(mergeDescendants = true) {
                // For screen readers, announce the count and label
                contentDescription = "${data.label}: $count games, ${data.labelBottom}"
            },
        horizontalAlignment = Alignment.CenterHorizontally
        // verticalArrangement = Arrangement.Bottom // Not strictly needed here due to weighted Box
    ) {
        // This Box takes up the available space for the bar to grow into
        Box(
            modifier = Modifier
                .weight(1f) // Takes available vertical space
                .fillMaxWidth(0.8f) // Bar width
                .padding(bottom = 0.dp), // Ensure no padding eats into the bar space from bottom
            contentAlignment = Alignment.BottomCenter
        ) {
            // This is the actual colored bar that animates
            Box(
                modifier = Modifier
                    .fillMaxWidth() // Fills the width of its parent Box (0.8f of the column)
                    .fillMaxHeight(barHeightPercentage) // Animated height
                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                    .background(data.color)
            )
        }
        Spacer(modifier = Modifier.height(4.dp)) // Reduced spacer for tighter layout
        Text(
            text = data.labelBottom,
            fontSize = 12.sp,
            color = textSecondary,
            textAlign = TextAlign.Center,
            maxLines = 2 // Allow wrapping for longer labels if necessary
        )
    }
}

@Composable
fun StatisticCard(title: String, value: String, valueColor: Color = textPrimary) {
    Card(
        modifier = Modifier.fillMaxWidth(), // Each card takes full width of its grid cell
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = surfacePrimary),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // Subtle shadow
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // Center content vertically
        ) {
            Text(
                text = title,
                fontSize = 13.sp, // Slightly smaller for title
                color = textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 6.dp) // Increased spacing
            )
            Text(
                text = value,
                fontSize = 22.sp, // Slightly smaller for value
                fontWeight = FontWeight.Bold, // Bolder value
                color = valueColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

data class BarData(
    val label: String, // e.g., "Won", "Lost", "Drawn"
    val value: Float,  // Percentage value (0.0 to 1.0) for bar height
    val color: Color,
    val labelBottom: String, // e.g., "Won (53.6%)"
    val count: Int // Actual count for accessibility
)

// Helper to format percentage nicely
fun Float.toPrettyPercentage(): String {
    // Handles potential NaN or Infinity if totalGames is 0 and calculations lead to such, though handled earlier.
    if (this.isNaN() || this.isInfinite()) {
        return "0.0"
    }
    return String.format("%.1f", this)
}

// Preview for StatisticsPage
// To enable preview, you might need to provide a mock HistoryViewModel
// or use a simpler version of StatisticsPage for preview purposes.
// @Preview(showBackground = true, widthDp = 380, heightDp = 800)
// @Composable
// fun StatisticsPagePreview() {
//    TicTacToeTheme { // Ensure your theme is applied
//        // Mocking HistoryViewModel and its state for preview
//        val mockStats = MatchStatistics(
//            totalMatches = 28,
//            playerWins = 15,
//            aiWins = 8,
//            draws = 5,
//            winRate = 53.6f,
//            averageGameDuration = "3m 15s"
//        )
//        val mockViewModel: HistoryViewModel = viewModel() // This might not work directly without DI setup for preview
//        // A simpler way for preview is to pass data directly if ViewModel setup is complex:
//
//        val stats = MatchStatistics(
//            totalMatches = 100, playerWins = 50, aiWins = 30, draws = 20,
//            winRate = 50.0f, averageGameDuration = "2m 45s"
//        )
//        Column(modifier = Modifier.background(surfaceSecondary).fillMaxSize()) { // Added background for context
//            AppHeader(title = "Statistics Preview", onBackClicked = {})
//            Column(modifier = Modifier.padding(16.dp)){
//                OverallStatsSection(stats = stats)
//                Spacer(modifier = Modifier.height(24.dp))
//                GameOutcomesSection(stats = stats)
//                Spacer(modifier = Modifier.height(24.dp))
//                GameResultsBreakdownSection(stats = stats)
//            }
//        }
//    }
// }
