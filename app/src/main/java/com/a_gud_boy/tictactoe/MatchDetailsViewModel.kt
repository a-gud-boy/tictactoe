package com.a_gud_boy.tictactoe

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

class MatchDetailsViewModel(
    private val matchDao: MatchDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val matchId: StateFlow<Long?> = savedStateHandle.getStateFlow("matchId", null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val matchDetails: StateFlow<MatchWithRoundsAndMoves?> = matchId.flatMapLatest { id ->
        if (id != null) {
            matchDao.getMatchWithRoundsAndMovesById(id)
        } else {
            kotlinx.coroutines.flow.flowOf(null) // Emit null if no id
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
}
