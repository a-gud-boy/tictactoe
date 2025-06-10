package com.a_gud_boy.tictactoe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(private val matchDao: MatchDao) : ViewModel() {

    // Expose the list of all matches (with their rounds and moves) as a StateFlow
    val matchHistory: StateFlow<List<MatchWithRoundsAndMoves>> =
        matchDao.getAllMatchesWithRoundsAndMoves()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000), // Keep flow active for 5s after last subscriber
                initialValue = emptyList() // Initial value while loading from DB
            )

    // Optional: Function to clear all history
    fun clearAllHistory() {
        viewModelScope.launch {
            matchDao.clearAllMatches()
        }
    }
}
