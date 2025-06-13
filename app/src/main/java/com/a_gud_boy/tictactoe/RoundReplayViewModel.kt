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
    val matchIdFromNav: Long = savedStateHandle.get<Long>("matchId") ?: throw IllegalStateException(
        "matchId not found in SavedStateHandle"
    )
    val roundId: Long = savedStateHandle.get<Long>("roundId")
        ?: throw IllegalStateException("roundId not found in SavedStateHandle")

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

    private val _winningPlayer = MutableStateFlow<Player?>(null)
    val winningPlayer: StateFlow<Player?> = _winningPlayer.asStateFlow()

    private val _orderedWinningCells = MutableStateFlow<List<String>>(emptyList())
    val orderedWinningCells: StateFlow<List<String>> = _orderedWinningCells.asStateFlow()

    companion object {
        private val winningCombinations = listOf(
            // Rows
            listOf("button1", "button2", "button3"),
            listOf("button4", "button5", "button6"),
            listOf("button7", "button8", "button9"),
            // Columns
            listOf("button1", "button4", "button7"),
            listOf("button2", "button5", "button8"),
            listOf("button3", "button6", "button9"),
            // Diagonals
            listOf("button1", "button5", "button9"),
            listOf("button3", "button5", "button7")
        )
    }

    init {
        loadMoves()
        observeMovesAndIndexChanges()
    }

    private fun loadMoves() {
        viewModelScope.launch {
            println("RoundReplayViewModel: loadMoves called with matchIdFromNav: $matchIdFromNav, roundId: $roundId")
            // matchIdFromNav is the match's primary key (id), not matchNumber.
            // The route was changed to pass matchNumber, but SavedStateHandle for ViewModel still expects "matchId" as key.
            // This needs to be consistent. Assuming "matchId" in SavedStateHandle is indeed the MatchEntity.id
            // If "matchId" from nav args is actually matchNumber, then the DAO call needs to change or ViewModel needs matchNumber.
            // For now, assuming matchIdFromNav is the correct MatchEntity.id for the DAO.
            matchDao.getMatchWithRoundsAndMovesById(matchIdFromNav).collectLatest { matchDetails ->
                println("RoundReplayViewModel: matchDetails is null: ${matchDetails == null}")
                if (matchDetails != null) {
                    println("RoundReplayViewModel: matchDetails.match.matchId: ${matchDetails.match.matchId}, roundsWithMoves count: ${matchDetails.roundsWithMoves.size}")
                    val foundRound =
                        matchDetails.roundsWithMoves.find { it.round.roundId == roundId }
                    if (foundRound != null) {
                        println("RoundReplayViewModel: Round with id $roundId found.")
                        _moves.value = foundRound.moves
                        val winner = Player.fromString(foundRound.round.winner)
                        _winningPlayer.value = winner
                        if (winner != null) {
                            _orderedWinningCells.value =
                                findWinningCombination(foundRound.moves, winner)
                        } else {
                            _orderedWinningCells.value = emptyList()
                        }
                        println("RoundReplayViewModel: Loaded ${_moves.value.size} moves. Winner: ${foundRound.round.winner}. Winning cells: ${_orderedWinningCells.value}")
                    } else {
                        _moves.value = emptyList()
                        _winningPlayer.value = null
                        _orderedWinningCells.value = emptyList()
                        // Log or handle case where specific roundId is not found in the match
                        println("RoundReplayViewModel: Round with id $roundId not found in match ${matchDetails.match.matchId}. Moves not loaded.")
                    }
                } else {
                    _moves.value = emptyList()
                    _winningPlayer.value = null
                    _orderedWinningCells.value = emptyList()
                    // Log or handle case where matchId is not found
                    println("RoundReplayViewModel: Match with id $matchIdFromNav not found. Moves not loaded.")
                }
            }
        }
    }

    private fun findWinningCombination(moves: List<MoveEntity>, winner: Player): List<String> {
        val winnerMoves =
            moves.filter { Player.fromString(it.player) == winner }.map { it.cellId }.toSet()
        for (combination in winningCombinations) {
            if (winnerMoves.containsAll(combination)) {
                return combination
            }
        }
        return emptyList()
    }

    private fun observeMovesAndIndexChanges() {
        viewModelScope.launch {
            combine(_moves, _currentMoveIndex) { movesList, index ->
                val newGridState = mutableMapOf<String, Player?>()
                if (index >= 0 && movesList.isNotEmpty()) {
                    for (i in 0..index.coerceAtMost(movesList.size - 1)) {
                        val move = movesList[i]
                        newGridState[move.cellId] =
                            Player.fromString(move.player) // Assuming Player.fromString exists
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
