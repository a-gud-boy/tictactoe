package com.a_gud_boy.tictactoe

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class RoundReplayViewModel(
    private val matchDao: MatchDao,
    private val roundDao: RoundDao, // Keep for now, might be useful for round-specific info later
    // private val moveDao: MoveDao, // Removed
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // matchId from SavedStateHandle is now used for fetching via matchDao
    val matchIdFromNav: Long = savedStateHandle.get<Long>("matchId") ?: throw IllegalStateException("matchId not found in SavedStateHandle")
    val roundId: Long = savedStateHandle.get<Long>("roundId") ?: throw IllegalStateException("roundId not found in SavedStateHandle")

    private val _moves = MutableStateFlow<List<MoveEntity>>(emptyList())
    val moves: StateFlow<List<MoveEntity>> = _moves.asStateFlow()

    // It seems matchId was intended for fetching, not direct exposure.
    // If matchId (the database ID) is needed by UI, it can be exposed,
    // but typically matchNumber (human-readable) is preferred for UI if available.
    // For this ViewModel, matchIdFromNav is used internally for fetching.

    private val _currentMoveIndex = MutableStateFlow(-1)
    val currentMoveIndex: StateFlow<Int> = _currentMoveIndex.asStateFlow()

    private val _currentGridState = MutableStateFlow<Map<String, Player?>>(emptyMap())
    val currentGridState: StateFlow<Map<String, Player?>> = _currentGridState.asStateFlow()

    init {
        loadMoves()
        observeMovesAndIndexChanges()
    }

    private fun loadMoves() {
        viewModelScope.launch {
            // matchIdFromNav is the match's primary key (id), not matchNumber.
            // The route was changed to pass matchNumber, but SavedStateHandle for ViewModel still expects "matchId" as key.
            // This needs to be consistent. Assuming "matchId" in SavedStateHandle is indeed the MatchEntity.id
            // If "matchId" from nav args is actually matchNumber, then the DAO call needs to change or ViewModel needs matchNumber.
            // For now, assuming matchIdFromNav is the correct MatchEntity.id for the DAO.
            matchDao.getMatchWithRoundsAndMovesById(matchIdFromNav).collectLatest { matchDetails ->
                if (matchDetails != null) {
                    val foundRound = matchDetails.roundsWithMoves.find { it.round.roundId == roundId }
                    if (foundRound != null) {
                        _moves.value = foundRound.moves
                    } else {
                        _moves.value = emptyList()
                        // Log or handle case where specific roundId is not found in the match
                        println("RoundReplayViewModel: Round with id $roundId not found in match ${matchDetails.match.matchId}")
                    }
                } else {
                    _moves.value = emptyList()
                    // Log or handle case where matchId is not found
                    println("RoundReplayViewModel: Match with id $matchIdFromNav not found.")
                }
            }
        }
    }

    private fun observeMovesAndIndexChanges() {
        viewModelScope.launch {
            combine(_moves, _currentMoveIndex) { movesList, index ->
                val newGridState = mutableMapOf<String, Player?>()
                if (index >= 0 && movesList.isNotEmpty()) {
                    for (i in 0..index.coerceAtMost(movesList.size - 1)) {
                        val move = movesList[i]
                        newGridState[move.cellId] = Player.fromString(move.player) // Assuming Player.fromString exists
                    }
                }
                newGridState
            }.collect { gridState ->
                _currentGridState.value = gridState
            }
        }
    }
    fun previousMove() {
        if (_currentMoveIndex.value > -1) {
            _currentMoveIndex.value--
        }
        // updateGridState() is not needed here due to reactive combine
        println("RoundReplayViewModel: previousMove() called, new index: ${_currentMoveIndex.value}")
    }

    fun nextMove() {
        if (_currentMoveIndex.value < _moves.value.size - 1) {
            _currentMoveIndex.value++
        }
        // updateGridState() is not needed here due to reactive combine
        println("RoundReplayViewModel: nextMove() called, new index: ${_currentMoveIndex.value}")
    }
}
