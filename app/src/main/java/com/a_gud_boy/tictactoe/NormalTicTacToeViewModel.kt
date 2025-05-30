package com.a_gud_boy.tictactoe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

// Enum to represent the player
enum class Player {
    X, O
}

// Data class to hold winner information
data class WinnerInfo(
    val winner: Player?,
    val combination: Set<String>,
    val orderedWinningMoves: List<String> // Added field for ordered winning moves
)

class NormalTicTacToeViewModel : ViewModel() {

    companion object {
        val WINNING_COMBINATIONS: List<Set<String>> = listOf(
            // Rows
            setOf("button1", "button2", "button3"),
            setOf("button4", "button5", "button6"),
            setOf("button7", "button8", "button9"),
            // Columns
            setOf("button1", "button4", "button7"),
            setOf("button2", "button5", "button8"),
            setOf("button3", "button6", "button9"),
            // Diagonals
            setOf("button1", "button5", "button9"),
            setOf("button3", "button5", "button7")
        )
    }

    private val _player1Wins = MutableStateFlow(0)
    val player1Wins: StateFlow<Int> = _player1Wins.asStateFlow()

    private val _player2Wins = MutableStateFlow(0)
    val player2Wins: StateFlow<Int> = _player2Wins.asStateFlow()

    // Using List<String> for moves as per current Composable logic
    private val _player1Moves = MutableStateFlow<List<String>>(emptyList())
    val player1Moves: StateFlow<List<String>> = _player1Moves.asStateFlow()

    private val _player2Moves = MutableStateFlow<List<String>>(emptyList())
    val player2Moves: StateFlow<List<String>> = _player2Moves.asStateFlow()

    private val _winnerInfo = MutableStateFlow<WinnerInfo?>(null)
    val winnerInfo: StateFlow<WinnerInfo?> = _winnerInfo.asStateFlow()

    // True for Player 1 (X), False for Player 2 (O)
    private val _player1Turn = MutableStateFlow(true)
    val player1Turn: StateFlow<Boolean> = _player1Turn.asStateFlow()

    private val _gameStarted = MutableStateFlow(true) // Game starts active
    val gameStarted: StateFlow<Boolean> = _gameStarted.asStateFlow()

    private val _isGameConcluded = MutableStateFlow(false)
    val isGameConcluded: StateFlow<Boolean> = _isGameConcluded.asStateFlow()

    // Derived state for turn denoting text
    val turnDenotingText: StateFlow<String> = combine(
        player1Turn,
        winnerInfo,
        isGameConcluded
    ) { isP1Turn, winnerData, gameConcluded ->
        when {
            winnerData != null && winnerData.winner == Player.X -> "Player 1 Won"
            winnerData != null && winnerData.winner == Player.O -> "Player 2 Won"
            gameConcluded && winnerData?.winner == null -> "It's a Draw!"
            isP1Turn -> "Player 1's Turn"
            else -> "Player 2's Turn"
        }
    }.stateIn(
        viewModelScope,
        kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        "Player 1's Turn"
    )

    // Derived state for reset button text
    val resetButtonText: StateFlow<String> = combine(
        isGameConcluded
    ) { concluded ->
        if (concluded[0]) "New Round" else "Reset Round"
    }.stateIn(
        viewModelScope,
        kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        "Reset Round"
    )


    // Game Logic Functions

    fun onButtonClick(buttonId: String) {
        if (!_gameStarted.value || _isGameConcluded.value) return

        val currentP1Moves = _player1Moves.value
        val currentP2Moves = _player2Moves.value

        // Check if button is already played by either player
        if (currentP1Moves.contains(buttonId) || currentP2Moves.contains(buttonId)) {
            return // Button already played
        }

        if (_player1Turn.value) {
            _player1Moves.value = currentP1Moves + buttonId
        } else {
            _player2Moves.value = currentP2Moves + buttonId
        }

        _player1Turn.value = !_player1Turn.value
        checkForWinner()
    }

    private fun checkForWinner() {
        val p1CurrentMovesList = _player1Moves.value
        val p2CurrentMovesList = _player2Moves.value
        val p1MovesSet = p1CurrentMovesList.toSet()
        val p2MovesSet = p2CurrentMovesList.toSet()

        for (combination in WINNING_COMBINATIONS) {
            if (p1MovesSet.containsAll(combination)) {
                // Filter the original list to maintain order
                val orderedWin = p1CurrentMovesList.filter { it in combination }
                _winnerInfo.value = WinnerInfo(Player.X, combination, orderedWin)
                _player1Wins.value += 1
                _isGameConcluded.value = true
                _gameStarted.value = false // Stop game, wait for reset
                return
            }
            if (p2MovesSet.containsAll(combination)) {
                // Filter the original list to maintain order
                val orderedWin = p2CurrentMovesList.filter { it in combination }
                _winnerInfo.value = WinnerInfo(Player.O, combination, orderedWin)
                _player2Wins.value += 1
                _isGameConcluded.value = true
                _gameStarted.value = false // Stop game, wait for reset
                return
            }
        }

        // Check for draw
        if ((p1MovesSet.size + p2MovesSet.size) == 9) {
            _winnerInfo.value = WinnerInfo(null, emptySet(), emptyList()) // Draw
            _isGameConcluded.value = true
            _gameStarted.value = false // Stop game, wait for reset
        }
    }

    fun resetRound() {
        _player1Moves.value = emptyList()
        _player2Moves.value = emptyList()
        _winnerInfo.value = null
        _player1Turn.value = true // Player 1 starts
        _isGameConcluded.value = false
        _gameStarted.value = true
    }

    fun resetScores() {
        _player1Wins.value = 0
        _player2Wins.value = 0
        // Optionally, also reset the round
        resetRound()
    }
}
