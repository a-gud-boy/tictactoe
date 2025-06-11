package com.a_gud_boy.tictactoe

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class RoundReplayViewModel(
    private val matchDao: MatchDao, // Keep if match-level data might be needed later
    private val roundDao: RoundDao, // Keep if round-level data might be needed later
    private val moveDao: MoveDao,
    savedStateHandle: SavedStateHandle // Renamed to avoid conflict with class member
) : ViewModel() {

    val matchId: Long = savedStateHandle.get<Long>("matchId") ?: throw IllegalStateException("matchId not found in SavedStateHandle")
    val roundId: Long = savedStateHandle.get<Long>("roundId") ?: throw IllegalStateException("roundId not found in SavedStateHandle")

    private val _moves = MutableStateFlow<List<MoveEntity>>(emptyList())
    val moves: StateFlow<List<MoveEntity>> = _moves.asStateFlow()

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
            // Assuming getMovesForRound returns List<MoveEntity> directly, not a Flow
            // If it returns a Flow, collect it: _moves.value = moveDao.getMovesForRound(roundId).first()
            _moves.value = moveDao.getMovesForRound(roundId)
            // After loading moves, currentMoveIndex is still -1, so grid will be empty
            // updateGridState will be triggered by the combine operator
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
