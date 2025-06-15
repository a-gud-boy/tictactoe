package com.a_gud_boy.tictactoe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map // Added import
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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

    // Statistics StateFlow derived from matchHistory
    val matchStatistics: StateFlow<MatchStatistics> = matchHistory.map { history ->
        val total = history.size
        val pWins = history.count { it.match.winner == MatchWinner.PLAYER1 }
        val aiWinsCount = history.count { it.match.winner == MatchWinner.PLAYER2 }
        val drawsCount = history.count { it.match.winner == MatchWinner.DRAW }
        val calculatedWinRate = if (total == 0) 0.0f else (pWins.toFloat() / total.toFloat()) * 100.0f
        val avgGameDuration = "3m 15s" // Placeholder as per requirement
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
