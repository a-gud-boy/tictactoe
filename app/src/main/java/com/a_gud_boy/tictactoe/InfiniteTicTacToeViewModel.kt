package com.a_gud_boy.tictactoe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

// The following code was commented out as they already exist in NormalTicTacToe.kt

// Enum to represent the player
//enum class Player {
//    X, O
//}

// Data class to hold winner information
//data class WinnerInfo(val winner: Player, val combination: Set<String>)

/**
 * ViewModel for the Infinite Tic Tac Toe game.
 *
 * This ViewModel manages the state and logic for a Tic Tac Toe game variant
 * where players' marks disappear after a certain number of subsequent moves.
 * It tracks player scores, moves, turn information, and game status (e.g., win, active).
 *
 * Key features managed:
 * - **Player Moves**: Stores the history of moves for Player X and Player O.
 *   Only the last [MAX_VISIBLE_MOVES_PER_PLAYER] are considered for display and win conditions.
 * - **Win Tracking**: Detects winning combinations based on the currently visible moves.
 * - **Turn Management**: Alternates turns between Player 1 (X) and Player 2 (O).
 * - **Game State**: Manages whether the game is started, concluded, or awaiting a new round.
 * - **Score Keeping**: Counts the number of wins for each player.
 * - **UI State Exposure**: Exposes game state information as [StateFlow]s to be observed by the UI,
 *   including derived states like `turnDenotingText` and `resetButtonText`.
 */
class InfiniteTicTacToeViewModel : ViewModel() {

    companion object {
        /**
         * The maximum number of moves per player that remain visible on the board
         * and are considered for winning conditions. Older moves "disappear".
         */
        const val MAX_VISIBLE_MOVES_PER_PLAYER = 3
        /**
         * A list of all possible winning combinations on a 3x3 Tic Tac Toe board.
         * Each combination is a set of button IDs (e.g., "button1", "button2", "button3").
         */
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
    /** StateFlow representing the number of wins for Player 1 (X). */
    val player1Wins: StateFlow<Int> = _player1Wins.asStateFlow()

    private val _player2Wins = MutableStateFlow(0)
    /** StateFlow representing the number of wins for Player 2 (O). */
    val player2Wins: StateFlow<Int> = _player2Wins.asStateFlow()

    // Using List<String> for moves as per current Composable logic
    private val _player1Moves = MutableStateFlow<List<String>>(emptyList())
    /** StateFlow representing the list of moves made by Player 1 (X). */
    val player1Moves: StateFlow<List<String>> = _player1Moves.asStateFlow()

    private val _player2Moves = MutableStateFlow<List<String>>(emptyList())
    /** StateFlow representing the list of moves made by Player 2 (O). */
    val player2Moves: StateFlow<List<String>> = _player2Moves.asStateFlow()

    private val _winnerInfo = MutableStateFlow<WinnerInfo?>(null)
    /**
     * StateFlow holding information about the winner of the current round, if any.
     * Contains the winning [Player] and the [Set] of button IDs forming the winning combination.
     * Null if there is no winner yet.
     */
    val winnerInfo: StateFlow<WinnerInfo?> = _winnerInfo.asStateFlow()

    // True for Player 1 (X), False for Player 2 (O)
    private val _player1Turn = MutableStateFlow(true)
    /** StateFlow indicating if it is currently Player 1's (X) turn. True if yes, false for Player 2 (O). */
    val player1Turn: StateFlow<Boolean> = _player1Turn.asStateFlow()

    private val _gameStarted = MutableStateFlow(true) // Game starts active
    /** StateFlow indicating if the game is currently active (i.e., players can make moves). */
    val gameStarted: StateFlow<Boolean> = _gameStarted.asStateFlow()

    private val _isGameConcluded = MutableStateFlow(false)
    /** StateFlow indicating if the current round of the game has concluded (e.g., due to a win). */
    val isGameConcluded: StateFlow<Boolean> = _isGameConcluded.asStateFlow()

    /**
     * Derived StateFlow providing a text string to display the current turn or game result.
     * Examples: "Player 1's Turn", "Player 2 Won".
     */
    val turnDenotingText: StateFlow<String> = combine(
        player1Turn,
        winnerInfo
        // isGameConcluded is removed as its effect on text is via winnerInfo
    ) { isP1Turn, winnerData -> // Renamed winner to winnerData to avoid any potential scope conflicts
        when {
            winnerData != null -> if (winnerData.winner == Player.X) "Player 1 Won" else "Player 2 Won"
            isP1Turn -> "Player 1\'s Turn"
            else -> "Player 2\'s Turn"
        }
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), "Player 1\'s Turn")

    /**
     * Derived StateFlow providing the text for the reset button.
     * Typically "Reset Round" during a game and "New Round" after a game has concluded.
     */
    val resetButtonText: StateFlow<String> = combine(
        isGameConcluded
    ) { concluded ->
        if (concluded[0]) "New Round" else "Reset Round"
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), "Reset Round")


    /**
     * Handles the logic when a button (cell) on the Tic Tac Toe board is clicked.
     *
     * If the game is active and the clicked cell is not already visibly occupied:
     * 1. Records the move for the current player.
     * 2. If the player's move list exceeds [MAX_VISIBLE_MOVES_PER_PLAYER], the oldest move is removed.
     * 3. Switches the turn to the other player.
     * 4. Checks if the new move results in a win.
     *
     * @param buttonId The ID of the button/cell that was clicked (e.g., "button1").
     */
    fun onButtonClick(buttonId: String) {
        if (!_gameStarted.value || _isGameConcluded.value) return

        val currentP1Moves = _player1Moves.value
        val currentP2Moves = _player2Moves.value

        // Check if button is already played by either player within visible moves
        val isAlreadyPlayedByPlayer1 = currentP1Moves.takeLast(MAX_VISIBLE_MOVES_PER_PLAYER).contains(buttonId)
        val isAlreadyPlayedByPlayer2 = currentP2Moves.takeLast(MAX_VISIBLE_MOVES_PER_PLAYER).contains(buttonId)

        if (isAlreadyPlayedByPlayer1 || isAlreadyPlayedByPlayer2) {
            return // Button already visibly played
        }

        if (_player1Turn.value) {
            val newMoves = currentP1Moves.toMutableList()
            newMoves.add(buttonId)
            _player1Moves.value = if (newMoves.size > MAX_VISIBLE_MOVES_PER_PLAYER) {
                newMoves.drop(newMoves.size - MAX_VISIBLE_MOVES_PER_PLAYER)
            } else {
                newMoves
            }
        } else {
            val newMoves = currentP2Moves.toMutableList()
            newMoves.add(buttonId)
            _player2Moves.value = if (newMoves.size > MAX_VISIBLE_MOVES_PER_PLAYER) {
                newMoves.drop(newMoves.size - MAX_VISIBLE_MOVES_PER_PLAYER)
            } else {
                newMoves
            }
        }

        _player1Turn.value = !_player1Turn.value
        checkForWinner()
    }

    /**
     * Checks if the current set of visible moves for either player constitutes a win.
     * It iterates through [WINNING_COMBINATIONS] and compares them against the
     * last [MAX_VISIBLE_MOVES_PER_PLAYER] moves of each player.
     * If a win is detected, it updates [_winnerInfo], increments the winner's score,
     * sets [_isGameConcluded] to true, and [_gameStarted] to false.
     */
    private fun checkForWinner() {
        // Full move history for determining the order of winning moves
        val p1FullMoveHistory = _player1Moves.value
        val p2FullMoveHistory = _player2Moves.value

        // Visible moves for win condition check
        val p1CurrentVisibleMovesSet = p1FullMoveHistory.takeLast(MAX_VISIBLE_MOVES_PER_PLAYER).toSet()
        val p2CurrentVisibleMovesSet = p2FullMoveHistory.takeLast(MAX_VISIBLE_MOVES_PER_PLAYER).toSet()

        for (combination in WINNING_COMBINATIONS) {
            if (p1CurrentVisibleMovesSet.containsAll(combination)) {
                // Filter the visible moves based on the combination to get the ordered winning moves
                val orderedWin = p1FullMoveHistory.takeLast(MAX_VISIBLE_MOVES_PER_PLAYER).filter { it in combination }
                _winnerInfo.value = WinnerInfo(Player.X, combination, orderedWin)
                _player1Wins.value += 1
                _isGameConcluded.value = true
                _gameStarted.value = false // Stop game, wait for reset
                return
            }
            if (p2CurrentVisibleMovesSet.containsAll(combination)) {
                // Filter the visible moves based on the combination to get the ordered winning moves
                val orderedWin = p2FullMoveHistory.takeLast(MAX_VISIBLE_MOVES_PER_PLAYER).filter { it in combination }
                _winnerInfo.value = WinnerInfo(Player.O, combination, orderedWin)
                _player2Wins.value += 1
                _isGameConcluded.value = true
                _gameStarted.value = false // Stop game, wait for reset
                return
            }
        }
        // Check for draw: if all buttons are conceptually filled by the visible moves of both players
        // This is tricky with infinite mode. A draw is not explicitly handled in the original code,
        // so we'll stick to win conditions for now.
        // No draw condition in Infinite TicTacToe as per original logic, cells can be reused.
    }

    /**
     * Resets the game board and state for a new round.
     * Clears all player moves, resets winner information, sets Player 1 as the starting player,
     * and marks the game as active and not concluded.
     * Player scores are not affected by this function.
     */
    fun resetRound() {
        _player1Moves.value = emptyList()
        _player2Moves.value = emptyList()
        _winnerInfo.value = null
        _player1Turn.value = true // Player 1 starts
        _isGameConcluded.value = false
        _gameStarted.value = true
    }

    /**
     * Resets the scores for both players to zero.
     * Optionally, this function also calls [resetRound] to reset the current game state as well.
     */
    fun resetScores() {
        _player1Wins.value = 0
        _player2Wins.value = 0
        // Optionally, also reset the round
        resetRound()
    }
}
