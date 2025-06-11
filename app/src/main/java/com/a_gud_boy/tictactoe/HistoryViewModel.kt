package com.a_gud_boy.tictactoe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val draws: Int = 0
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
        MatchStatistics(total, pWins, aiWinsCount, drawsCount)
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
