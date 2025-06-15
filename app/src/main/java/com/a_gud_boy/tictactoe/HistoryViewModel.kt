package com.a_gud_boy.tictactoe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine // Added import for combine
import kotlinx.coroutines.flow.map // Added import
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit // Added for time conversion

// Data class for match statistics
data class MatchStatistics(
    val totalMatches: Int = 0,
    val playerWins: Int = 0,
    val aiWins: Int = 0,
    val draws: Int = 0,
    val winRate: Float = 0.0f,
    val averageGameDuration: String = "N/A"
)

class HistoryViewModel(private val matchDao: MatchDao) : ViewModel() {

    // Expose the list of all matches (with their rounds and moves) as a StateFlow
    val matchHistory: StateFlow<List<MatchWithRoundsAndMoves>> =
        matchDao.getAllMatchesWithRoundsAndMoves()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // Flow for average match duration
    private val averageDurationFlow: StateFlow<Double?> =
        matchDao.getAverageMatchDuration()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )

    // Statistics StateFlow derived from matchHistory and averageDurationFlow
    val matchStatistics: StateFlow<MatchStatistics> = combine(
        matchHistory,
        averageDurationFlow
    ) { history, avgDurationMs ->
        val total = history.size
        val pWins = history.count { it.match.winner == MatchWinner.PLAYER1 }
        val aiWinsCount = history.count { it.match.winner == MatchWinner.PLAYER2 }
        val drawsCount = history.count { it.match.winner == MatchWinner.DRAW }
        val calculatedWinRate = if (total == 0) 0.0f else (pWins.toFloat() / total.toFloat()) * 100.0f
        val avgGameDuration = formatDuration(avgDurationMs)
        MatchStatistics(
            totalMatches = total,
            playerWins = pWins,
            aiWins = aiWinsCount,
            draws = drawsCount,
            winRate = calculatedWinRate,
            averageGameDuration = avgGameDuration
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MatchStatistics()
    )

    private fun formatDuration(milliseconds: Double?): String {
        if (milliseconds == null || milliseconds == 0.0) {
            return "N/A"
        }

        val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds.toLong())
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60

        return when {
            minutes > 0 && seconds > 0 -> "${minutes}m ${seconds}s"
            minutes > 0 -> "${minutes}m"
            seconds > 0 -> "${seconds}s"
            else -> "<1s" // Handle very short durations
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            matchDao.clearAllMatches()
        }
    }

    fun deleteMatch(match: MatchWithRoundsAndMoves) {
        viewModelScope.launch {
            matchDao.deleteMatchById(match.match.matchId)
        }
    }
}
