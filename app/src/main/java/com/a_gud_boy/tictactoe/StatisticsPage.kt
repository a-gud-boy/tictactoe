package com.a_gud_boy.tictactoe

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope // For AnimatedBar
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card // Keep for StatisticCard
import androidx.compose.material3.CardDefaults // Keep for StatisticCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.* // For remember, mutableStateOf, getValue, setValue, LaunchedEffect
import androidx.compose.runtime.collectAsState
// import androidx.compose.runtime.getValue // Already imported by wildcard
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Assuming HistoryViewModel and MatchStatistics are correctly imported/defined elsewhere

// Color definitions - These are essential for this file's composables.
// These should ideally be in a central theme file (e.g., ui/theme/Color.kt) and imported.
// For self-containment of this overwrite, they are listed here.
val designNeutralText = Color(0xFF1F2937)
val designSubtleText = Color(0xFF6B7280)
val designAccentGreen = Color(0xFF4ADE80)
val designAccentRed = Color(0xFFF87171)
val designAccentYellow = Color(0xFFFACC15)
val designBorderColor = Color(0xFFE5E7EB)
val designNeutralBg = Color(0xFFF8F8F8)
val designNeutralCardBg = Color(0xFFFFFFFF)

// New Data class for Chart Items
data class ChartBarItem(
    val label: String,      // e.g., "Won", "Lost", "Drawn"
    val count: Int,         // Actual count of games
    val percentage: Float,  // Percentage (0.0 to 1.0 for bar height factor)
    val color: Color,       // Color of the bar
    val labelBottom: String // e.g., "Won (50.5%)"
)

@Composable
fun StatisticsPageContent(
    historyViewModel: HistoryViewModel,
    modifier: Modifier = Modifier
) {
    val matchStatistics by historyViewModel.matchStatistics.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(designNeutralBg)
            .padding(16.dp)
    ) {
        OverallStatsSection(stats = matchStatistics)
        Spacer(modifier = Modifier.height(24.dp))
        GameOutcomesSection(stats = matchStatistics)
        Spacer(modifier = Modifier.height(24.dp))
        GameResultsBreakdownSection(stats = matchStatistics)
    }
}

@Composable
fun OverallStatsSection(stats: MatchStatistics) {
    Column {
        Text(
            "Overall Stats",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = designNeutralText
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatisticCard("Total Matches", stats.totalMatches.toString(), Modifier.weight(1f), contentColor = designNeutralText)
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
            color = designNeutralText
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatisticCard("Wins", stats.playerWins.toString(), Modifier.weight(1f), contentColor = designAccentGreen)
            StatisticCard("Losses", stats.aiWins.toString(), Modifier.weight(1f), contentColor = designAccentRed)
            StatisticCard("Draws", stats.draws.toString(), Modifier.weight(1f), contentColor = designAccentYellow)
        }
    }
}

@Composable
fun RowScope.AnimatedBar(
    item: ChartBarItem,
    modifier: Modifier = Modifier
) {
    var animationPlayed by remember { mutableStateOf(false) }
    val barHeightFactor by animateFloatAsState(
        targetValue = if (animationPlayed) item.percentage else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "${item.label}BarAnimation"
    )

    LaunchedEffect(Unit) {
        animationPlayed = true
    }

    Box(
        modifier = modifier
            .fillMaxHeight(barHeightFactor)
            .background(item.color)
    )
}

@Composable
fun GameResultsBreakdownSection(stats: MatchStatistics) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Results Breakdown",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = designNeutralText,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        val totalGames = stats.totalMatches.toFloat().coerceAtLeast(1.0f)

        val chartItems = listOf(
            ChartBarItem(
                label = "Won",
                count = stats.playerWins,
                percentage = (stats.playerWins / totalGames),
                color = designAccentGreen,
                labelBottom = "Won (${String.format("%.1f", (stats.playerWins / totalGames) * 100)}%)"
            ),
            ChartBarItem(
                label = "Lost",
                count = stats.aiWins,
                percentage = (stats.aiWins / totalGames),
                color = designAccentRed,
                labelBottom = "Lost (${String.format("%.1f", (stats.aiWins / totalGames) * 100)}%)"
            ),
            ChartBarItem(
                label = "Drawn",
                count = stats.draws,
                percentage = (stats.draws / totalGames),
                color = designAccentYellow,
                labelBottom = "Drawn (${String.format("%.1f", (stats.draws / totalGames) * 100)}%)"
            )
        )

        if (stats.totalMatches == 0) {
             Box(modifier = Modifier.fillMaxWidth().height(200.dp).padding(16.dp), contentAlignment = Alignment.Center){
                Text("No games played yet to show breakdown.", color = designSubtleText)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                chartItems.forEach { _ ->
                     Text("", modifier = Modifier.weight(1f), fontSize = 12.sp, maxLines = 2)
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)
            ) {
                Column( // YAxisLabelsColumn
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(40.dp)
                        .padding(end = 8.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    Text("100%", fontSize = 10.sp, color = designSubtleText)
                    Text("75%", fontSize = 10.sp, color = designSubtleText)
                    Text("50%", fontSize = 10.sp, color = designSubtleText)
                    Text("25%", fontSize = 10.sp, color = designSubtleText)
                    Text("0%", fontSize = 10.sp, color = designSubtleText)
                }

                Column(modifier = Modifier.weight(1f)) { // CenterContentColumn
                    Row( // AnimatedBarsRow
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp), // Fixed height for bar chart area
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        chartItems.forEach { item ->
                            if (item.count > 0) {
                                 AnimatedBar(
                                    item = item,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f).fillMaxHeight())
                            }
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        thickness = 1.dp,
                        color = designBorderColor
                    )

                    Row( // XAxisLabelsRow
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        chartItems.forEach { item ->
                            Text(
                                text = item.labelBottom,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp,
                                color = designSubtleText,
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatisticCard(title: String, value: String, modifier: Modifier = Modifier, contentColor: Color = designNeutralText) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = designNeutralCardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = designSubtleText)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = contentColor)
        }
    }
}
// Old Canvas-based BarChart and old BarData data class are removed.
// The old StatisticsPage composable with Scaffold is also removed.
