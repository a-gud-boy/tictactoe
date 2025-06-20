package com.a_gud_boy.tictactoe

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch // Added for viewModelScope.launch

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

    fun deleteMatch() {
        viewModelScope.launch {
            matchId.value?.let { id ->
                matchDao.deleteMatchById(id)
            } ?: run {
                // Optional: Log or handle the case where matchId is null
                android.util.Log.w("MatchDetailsViewModel", "Attempted to delete match but matchId was null.")
            }
        }
    }
}
