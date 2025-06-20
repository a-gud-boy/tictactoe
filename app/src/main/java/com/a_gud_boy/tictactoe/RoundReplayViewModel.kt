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
import com.a_gud_boy.tictactoe.GameType // Import GameType
import android.util.Log // Import Log
import org.json.JSONArray // Import JSONArray
import org.json.JSONException // Import JSONException

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

    private val gameTypeString: String? = savedStateHandle.get<String>("gameType")
    val gameType: GameType = try {
        gameTypeString?.let { GameType.valueOf(it.uppercase()) } ?: GameType.NORMAL
    } catch (e: IllegalArgumentException) {
        GameType.NORMAL // Fallback if string is not a valid GameType
    }

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

    private val _roundWinnerNameDisplay = MutableStateFlow<String?>(null)
    val roundWinnerNameDisplay: StateFlow<String?> = _roundWinnerNameDisplay.asStateFlow()

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
                        _roundWinnerNameDisplay.value = foundRound.round.roundWinnerName // Add this line
                        if (winner != null) {
                            val storedComboJson = foundRound.round.winningCombinationJson
                            if (!storedComboJson.isNullOrEmpty()) {
                                try {
                                    val jsonArray = JSONArray(storedComboJson)
                                    val cellsList = mutableListOf<String>()
                                    for (i in 0 until jsonArray.length()) {
                                        cellsList.add(jsonArray.getString(i))
                                    }
                                    if (cellsList.isNotEmpty()) { // Ensure the parsed list is not empty
                                        _orderedWinningCells.value = cellsList
                                        Log.d("RoundReplayVM", "Using stored winning combination: $cellsList")
                                    } else {
                                        // JSON array was empty "[]", or content was otherwise not yielding cells.
                                        // This case might imply a draw was stored with "[]" or bad data.
                                        // Fallback or set empty if appropriate.
                                        Log.w("RoundReplayVM", "Stored winning combination JSON was empty or invalid: $storedComboJson. Falling back.")
                                        _orderedWinningCells.value = findWinningCombination(foundRound.moves, winner)
                                    }
                                } catch (e: JSONException) {
                                    Log.e("RoundReplayVM", "Error parsing stored winning combination JSON: $storedComboJson", e)
                                    // Fallback to old method if JSON parsing fails
                                    _orderedWinningCells.value = findWinningCombination(foundRound.moves, winner)
                                }
                            } else {
                                // Stored combo is null or empty string, use fallback for older data or if not set.
                                Log.d("RoundReplayVM", "No stored winning combination found. Using findWinningCombination fallback.")
                                _orderedWinningCells.value = findWinningCombination(foundRound.moves, winner)
                            }
                        } else { // No winner
                            _orderedWinningCells.value = emptyList()
                        }
                        println("RoundReplayViewModel: Loaded ${_moves.value.size} moves. Winner: ${foundRound.round.winner}. Winning cells: ${_orderedWinningCells.value}")
                    } else {
                        _moves.value = emptyList()
                        _winningPlayer.value = null
                        _orderedWinningCells.value = emptyList()
                        _roundWinnerNameDisplay.value = null // Add this line
                        // Log or handle case where specific roundId is not found in the match
                        println("RoundReplayViewModel: Round with id $roundId not found in match ${matchDetails.match.matchId}. Moves not loaded.")
                    }
                } else {
                    _moves.value = emptyList()
                    _winningPlayer.value = null
                    _orderedWinningCells.value = emptyList()
                    _roundWinnerNameDisplay.value = null // Add this line
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
                // Ensure movesList is not empty and index is valid before subList
                if (movesList.isNotEmpty() && index >= 0) {
                    val currentMovesInSequence = movesList.subList(0, (index + 1).coerceAtMost(movesList.size))

                    if (gameType == GameType.INFINITE) { // Use the enum here
                        // Separate moves by player
                        val playerXMoves = currentMovesInSequence.filter { Player.fromString(it.player) == Player.X }
                        val playerOMoves = currentMovesInSequence.filter { Player.fromString(it.player) == Player.O }

                        // Get only the cellIds for taking the last 3
                        val playerXCellIds = playerXMoves.map { it.cellId }
                        val playerOCellIds = playerOMoves.map { it.cellId }

                        val visiblePlayerXCellIds = playerXCellIds.takeLast(3) // MAX_VISIBLE_MOVES_PER_PLAYER is 3
                        val visiblePlayerOCellIds = playerOCellIds.takeLast(3) // MAX_VISIBLE_MOVES_PER_PLAYER is 3

                        // Populate grid state for visible moves
                        visiblePlayerXCellIds.forEach { cellId -> newGridState[cellId] = Player.X }
                        visiblePlayerOCellIds.forEach { cellId -> newGridState[cellId] = Player.O }

                    } else { // NORMAL game type
                        currentMovesInSequence.forEach { move ->
                            newGridState[move.cellId] = Player.fromString(move.player)
                        }
                    }
                } else if (index == -1) {
                    // Grid is empty, newGridState remains empty (or explicitly clear if needed)
                    // newGridState.clear() // Depending on desired behavior for index -1
                }
                newGridState // Return the calculated state
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

    fun deleteRound() {
        viewModelScope.launch {
            // roundId is guaranteed non-null by the class's init block via SavedStateHandle
            roundDao.deleteRoundById(roundId)
            // Optionally, could set a flag or use a Channel to notify UI of deletion
            Log.d("RoundReplayVM", "Round with ID $roundId deletion initiated.")
        }
    }
}
