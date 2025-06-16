package com.a_gud_boy.tictactoe

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Import ViewModel if needed directly, though passed as param to StatisticsPageContent
// import androidx.lifecycle.viewmodel.compose.viewModel
// import com.a_gud_boy.tictactoe.ui.theme.surfaceSecondary // Will be replaced by designNeutralBg

// Data class for Bar Chart
data class BarData(val label: String, val value: Float, val color: Color)

// designNeutralBg, designNeutralCardBg, designNeutralText, designSubtleText are used from HistoryPage.kt (same package)

// New StatisticsPageContent composable
@Composable
fun StatisticsPageContent(
    historyViewModel: HistoryViewModel, // Pass ViewModel explicitly
    modifier: Modifier = Modifier
) {
    val matchStatistics by historyViewModel.matchStatistics.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(designNeutralBg) // Changed from surfaceSecondary
            .padding(16.dp) // Overall padding for the content area
    ) {
        OverallStatsSection(stats = matchStatistics)
        Spacer(modifier = Modifier.height(24.dp))
        GameOutcomesSection(stats = matchStatistics)
        Spacer(modifier = Modifier.height(24.dp))
        GameResultsBreakdownSection(stats = matchStatistics)
    }
}

// Existing helper composables (OverallStatsSection, GameOutcomesSection, GameResultsBreakdownSection, StatisticCard, Bar)
// remain below. The old StatisticsPage and AppHeader are removed.

@Composable
fun OverallStatsSection(stats: MatchStatistics) {
    Column {
        Text(
            "Overall Stats",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = designNeutralText // Use design color
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatisticCard("Total Matches", stats.totalMatches.toString(), Modifier.weight(1f), contentColor = designNeutralText)
            // Assuming winRate is a Float like 50.0 for 50%.
            StatisticCard("Win Rate", "${String.format("%.1f", stats.winRate)}%", Modifier.weight(1f), contentColor = designNeutralText)
        }
    }
}

@Composable
fun GameOutcomesSection(stats: MatchStatistics) {
    Column {
        Text(
            "Game Outcomes",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = designNeutralText // Use design color
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatisticCard("Wins", stats.playerWins.toString(), Modifier.weight(1f), contentColor = designAccentGreen)
            StatisticCard("Losses", stats.aiWins.toString(), Modifier.weight(1f), contentColor = designAccentRed) // Player's losses are AI wins
            StatisticCard("Draws", stats.draws.toString(), Modifier.weight(1f), contentColor = designAccentYellow)
        }
    }
}

@Composable
fun GameResultsBreakdownSection(stats: MatchStatistics) {
    Column {
        Text(
            "Results Breakdown",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = designNeutralText // Use design color
        )
        Spacer(modifier = Modifier.height(16.dp))

        val barDataList = listOfNotNull(
            BarData("Won", stats.playerWins.toFloat(), designAccentGreen),
            BarData("Lost", stats.aiWins.toFloat(), designAccentRed), // Player's losses
            BarData("Drawn", stats.draws.toFloat(), designAccentYellow)
        ).filter { it.value > 0 } // Only show bars with value > 0

        if (barDataList.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = designNeutralCardBg), // Use design color
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    BarChart(barDataList)
                }
            }
        } else {
            Text(
                "No breakdown data available yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = designSubtleText // Use design color
            )
        }
    }
}

@Composable
fun StatisticCard(title: String, value: String, modifier: Modifier = Modifier, contentColor: Color = designNeutralText) { // Default contentColor updated
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = designNeutralCardBg), // Use design color
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = designSubtleText) // Use design color
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = contentColor)
        }
    }
}

@Composable
fun BarChart(bars: List<BarData>) {
    val maxValue = bars.maxOfOrNull { it.value } ?: 0f
    if (maxValue == 0f) { // Avoid division by zero if all values are 0
        Text("No data to display in chart.", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp))
        return
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min), // Ensures Row respects children's intrinsic height for text alignment
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        bars.forEach { bar ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
            ) {
                // Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp) // Fixed height for the bar area
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val barHeight = (bar.value / maxValue) * size.height
                        drawLine(
                            color = bar.color,
                            start = Offset(x = size.width / 2, y = size.height),
                            end = Offset(x = size.width / 2, y = size.height - barHeight),
                            strokeWidth = size.width * 0.5f // Adjust bar width relative to available space
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                // Label
                Text(
                    text = bar.label,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    maxLines = 2 // Allow label to wrap if too long
                )
                // Value Text (optional, can be added above or below label)
                Text(
                    text = bar.value.toInt().toString(), // Display value as Int
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// Old StatisticsPage composable and AppHeader are removed.
// surfaceSecondary is imported from ui.theme.Color
// MatchStatistics data class should be available (defined in HistoryViewModel.kt or similar)
// HistoryViewModel is passed as a parameter.
