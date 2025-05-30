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

class NormalTicTacToeViewModel(private val gameMode: GameMode) : ViewModel() {

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

        // If PVC mode and now it's computer's turn (O, player 2)
        if (gameMode == GameMode.PVC && !_player1Turn.value && !_isGameConcluded.value) {
            makeComputerMove()
        }
    }

    private fun makeComputerMove() {
        // Ensure computer doesn't play if game is over or not started
        if (_isGameConcluded.value || !_gameStarted.value) return

        val p1Moves = _player1Moves.value
        val p2Moves = _player2Moves.value
        val allCurrentMoves = p1Moves + p2Moves

        val availableCells = (1..9)
            .map { "button$it" }
            .filterNot { allCurrentMoves.contains(it) }

        if (availableCells.isEmpty()) {
            // This should ideally be caught by checkForWinner after human's move leading to a draw
            return
        }

        // 1. Check if Computer (Player O) can win
        for (cell in availableCells) {
            if (canWin(p2Moves, cell)) {
                _player2Moves.value = p2Moves + cell
                _player1Turn.value = true
                checkForWinner()
                return
            }
        }

        // 2. Check if Player X can win (and block them)
        for (cell in availableCells) {
            if (canWin(p1Moves, cell)) {
                _player2Moves.value = p2Moves + cell // Computer plays in X's winning cell to block
                _player1Turn.value = true
                checkForWinner()
                return
            }
        }

        // 3. If neither can win, make a random move
        val computerMove = availableCells.random()
        _player2Moves.value = p2Moves + computerMove
        _player1Turn.value = true
        checkForWinner()
    }

    private fun canWin(currentMoves: List<String>, potentialNextMove: String): Boolean {
        if (currentMoves.contains(potentialNextMove)) return false // Cell already taken by this player
        val simulatedMoves = (currentMoves + potentialNextMove).toSet()
        if (simulatedMoves.size < 3) return false

        for (combination in WINNING_COMBINATIONS) {
            if (simulatedMoves.containsAll(combination)) {
                return true
            }
        }
        return false
    }

    private fun checkForWinner() {
        val p1CurrentMovesList = _player1Moves.value
        val p2CurrentMovesList = _player2Moves.value
        val p1MovesSet = p1CurrentMovesList.toSet()
        val p2MovesSet = p2CurrentMovesList.toSet()

        // Early return if neither player has enough moves for a win
        // Consider the case where one player has 2 moves and the other has 2, then one player makes a 3rd move.
        // The check should be if the player whose turn it just was has enough moves.

        val lastPlayerMadeMoveSet = if (!_player1Turn.value) p1MovesSet else p2MovesSet // Moves of the player who just played
        val lastPlayer = if (!_player1Turn.value) Player.X else Player.O

        if (lastPlayerMadeMoveSet.size < 3) return

        // Check only relevant winning combinations based on the last move made by the player who just played.
        val lastMove = if (!_player1Turn.value) p1CurrentMovesList.lastOrNull() else p2CurrentMovesList.lastOrNull()
        // If lastMove is null, it means the player who just supposedly made a move has an empty list of moves.
        // This shouldn't happen if lastPlayerMadeMoveSet.size >= 3, but as a safeguard:
        if (lastMove == null) return


        // Filter winning combinations that contain the last move
        val relevantCombinations = WINNING_COMBINATIONS.filter { it.contains(lastMove) }

        for (combination in relevantCombinations) {
            if (lastPlayer == Player.X && p1MovesSet.containsAll(combination)) {
                val orderedWin = combination.toList()
                _winnerInfo.value = WinnerInfo(Player.X, combination, orderedWin)
                _player1Wins.value += 1
                _isGameConcluded.value = true
                _gameStarted.value = false
                return
            }
            if (lastPlayer == Player.O && p2MovesSet.containsAll(combination)) {
                val orderedWin = combination.toList()
                _winnerInfo.value = WinnerInfo(Player.O, combination, orderedWin)
                _player2Wins.value += 1
                _isGameConcluded.value = true
                _gameStarted.value = false
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
        // Player 1 (human) always starts a new round.
        // If PVC, and computer just played, player1Turn is already true.
        // If PVP, player1Turn is set based on who's next or a fixed starter.
        // For simplicity, P1 always starts.
        _player1Turn.value = true
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
