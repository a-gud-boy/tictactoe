package com.a_gud_boy.tictactoe

// AIDifficulty Enum
enum class AIDifficulty {
    EASY, MEDIUM, HARD
}

// Enum to represent the player (ensure this is present or imported)
enum class Player {
    X, O
}

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Data class to hold winner information (ensure this is compatible)
// It is: data class WinnerInfo(val winner: Player, val combination: Set<String>, val orderedWinningMoves: List<String>)
// The existing WinnerInfo in InfiniteTicTacToeViewModel is:
// data class WinnerInfo(val winner: Player, val combination: Set<String>, val orderedWin: List<String>)
// This is compatible.

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

    private val _isAIMode = MutableStateFlow(false)
    val isAIMode: StateFlow<Boolean> = _isAIMode.asStateFlow()

    private val _aiDifficulty = MutableStateFlow(AIDifficulty.MEDIUM) // Default to Medium
    val aiDifficulty: StateFlow<AIDifficulty> = _aiDifficulty.asStateFlow()

    /**
     * Derived StateFlow providing a text string to display the current turn or game result.
     * Examples: "Player 1's Turn", "Player 2 Won".
     */
    val turnDenotingText: StateFlow<String> = combine(
        player1Turn,
        winnerInfo,
        isAIMode,
        isGameConcluded
    ) { isP1Turn, winnerData, aiMode, gameConcluded ->
        when {
            winnerData != null && winnerData.winner == Player.X -> "You Won!"
            winnerData != null && winnerData.winner == Player.O -> if (aiMode) "AI Won!" else "Player 2 Won!"
            gameConcluded && winnerData?.winner == null -> "It's a Draw!" // Explicit draw check
            isP1Turn -> "Your Turn"
            else -> if (aiMode) "AI's Turn" else "Player 2's Turn"
        }
    }.stateIn(
        viewModelScope,
        kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        "Your Turn" // Initial value
    )

    /**
     * Derived StateFlow providing the text for the reset button.
     * Typically "Reset Round" during a game and "New Round" after a game has concluded.
     */
    val resetButtonText: StateFlow<String> = combine(
        isGameConcluded
    ) { concluded ->
        if (concluded[0]) "New Round" else "Reset Round"
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), "Reset Round")

    fun setAIMode(enabled: Boolean) {
        _isAIMode.value = enabled
        resetRound() // Reset round when AI mode changes
    }

    fun setAIDifficulty(difficulty: AIDifficulty) {
        _aiDifficulty.value = difficulty
        if (_isAIMode.value) {
            resetRound() // Reset round if AI mode is active and difficulty changes
        }
    }

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

        val isProcessingPlayer1Move = _player1Turn.value

        if (isProcessingPlayer1Move) { // Human Player 1's turn
            val newMoves = currentP1Moves.toMutableList()
            newMoves.add(buttonId)
            _player1Moves.value = if (newMoves.size > MAX_VISIBLE_MOVES_PER_PLAYER) {
                newMoves.drop(newMoves.size - MAX_VISIBLE_MOVES_PER_PLAYER)
            } else {
                newMoves
            }
            _player1Turn.value = false // Switch to Player 2 (potentially AI)
            checkForWinner() // Check if Player 1 won

            // If AI mode is on, game is not over, and it's now AI's turn (player1Turn is now false)
            if (_isAIMode.value && !_isGameConcluded.value && !_player1Turn.value) {
                makeAIMove()
            }
        } else { // Player 2's turn (either Human Player 2 or AI if makeAIMove called this)
            val newMoves = currentP2Moves.toMutableList()
            newMoves.add(buttonId)
            _player2Moves.value = if (newMoves.size > MAX_VISIBLE_MOVES_PER_PLAYER) {
                newMoves.drop(newMoves.size - MAX_VISIBLE_MOVES_PER_PLAYER)
            } else {
                newMoves
            }
            _player1Turn.value = true // Switch back to Player 1
            checkForWinner() // Check if Player 2 (or AI) won
        }
        // Note: _player1Turn.value and checkForWinner() are handled within each branch now.
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

        // Get visible moves once to avoid multiple calls to takeLast
        val p1VisibleMoves = p1FullMoveHistory.takeLast(MAX_VISIBLE_MOVES_PER_PLAYER)
        val p2VisibleMoves = p2FullMoveHistory.takeLast(MAX_VISIBLE_MOVES_PER_PLAYER)
        val p1CurrentVisibleMovesSet = p1VisibleMoves.toSet()
        val p2CurrentVisibleMovesSet = p2VisibleMoves.toSet()

        // Early return if neither player has enough moves for a win
        if (p1VisibleMoves.size < 3 && p2VisibleMoves.size < 3) return

        // Check only relevant winning combinations based on the last move
        val lastMove = if (_player1Turn.value) p2VisibleMoves.lastOrNull() else p1VisibleMoves.lastOrNull()
        if (lastMove == null) return

        // Filter winning combinations that contain the last move
        val relevantCombinations = WINNING_COMBINATIONS.filter { it.contains(lastMove) }

        for (combination in relevantCombinations) {
            if (p1CurrentVisibleMovesSet.containsAll(combination)) {
                // Order the winning moves based on the geometric pattern in the combination
                val orderedWin = combination.toList()
                _winnerInfo.value = WinnerInfo(Player.X, combination, orderedWin)
                _player1Wins.value += 1
                _isGameConcluded.value = true
                _gameStarted.value = false // Stop game, wait for reset
                return
            }
            if (p2CurrentVisibleMovesSet.containsAll(combination)) {
                // Order the winning moves based on the geometric pattern in the combination
                val orderedWin = combination.toList()
                _winnerInfo.value = WinnerInfo(Player.O, combination, orderedWin)
                _player2Wins.value += 1
                _isGameConcluded.value = true
                _gameStarted.value = false // Stop game, wait for reset
                return
            }
        }
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

    // AI related functions adapted for InfiniteTicTacToe

    /**
     * Initiates the AI's move based on the selected difficulty.
     * This function is typically called after the human player makes a move
     * and it's determined to be the AI's turn.
     */
    fun makeAIMove() {
        if (!_gameStarted.value || _isGameConcluded.value || _player1Turn.value || !_isAIMode.value) return

        viewModelScope.launch {
            delay(500) // Delay for UX
            val move = when (_aiDifficulty.value) {
                AIDifficulty.EASY -> getRandomMove()
                AIDifficulty.MEDIUM -> if (Math.random() < 0.6) getBestMove() else getRandomMove() // 60% chance for best move
                AIDifficulty.HARD -> getBestMove()
            }
            // IMPORTANT: The AI should use its own player identity (Player.O) for onButtonClick.
            // However, onButtonClick in InfiniteTicTacToeViewModel currently deduces player based on _player1Turn.
            // Since makeAIMove is called when it's AI's (Player O) turn, _player1Turn is false.
            // So, calling onButtonClick(move) should correctly register the move for Player O.
            move?.let { onButtonClick(it) }
        }
    }

    /**
     * Selects a random available move for the AI.
     * A move is available if it's not part of the currently visible moves of either player.
     * @return A string representing the button ID of the chosen move, or null if no moves are available.
     */
    private fun getRandomMove(): String? {
        val allMoves = (1..9).map { "button$it" }
        // Available if not in the last MAX_VISIBLE_MOVES_PER_PLAYER moves of P1 AND not in P2's
        val availableMoves = allMoves.filter { buttonId ->
            !_player1Moves.value.takeLast(MAX_VISIBLE_MOVES_PER_PLAYER).contains(buttonId) &&
            !_player2Moves.value.takeLast(MAX_VISIBLE_MOVES_PER_PLAYER).contains(buttonId)
        }
        return availableMoves.randomOrNull()
    }

    /**
     * Determines the best possible move for the AI using the minimax algorithm.
     * It evaluates available moves based on their potential to lead to a win or block a player's win.
     * @return A string representing the button ID of the best move, or null if no moves are available.
     */
    private fun getBestMove(): String? {
        val allMoves = (1..9).map { "button$it" }
        // Available if not in the last MAX_VISIBLE_MOVES_PER_PLAYER moves of P1 AND not in P2's
        val availableMoves = allMoves.filter { buttonId ->
            !_player1Moves.value.takeLast(MAX_VISIBLE_MOVES_PER_PLAYER).contains(buttonId) &&
            !_player2Moves.value.takeLast(MAX_VISIBLE_MOVES_PER_PLAYER).contains(buttonId)
        }

        if (availableMoves.isEmpty()) return null

        var bestScore = Double.NEGATIVE_INFINITY
        var bestMove: String? = null

        // AI is Player O (minimizing player in standard minimax, but here we treat AI as maximizing its own score)
        for (move in availableMoves) {
            // Simulate AI (Player O) making a move. Pass full history.
            val score = minimax(
                p1Moves = _player1Moves.value, // Full history for Player X
                p2Moves = _player2Moves.value + move, // Full history for Player O + new move
                depth = 0,
                isMaximizing = false // Next turn is Player X (human), who will try to minimize AI's score
            )
            if (score > bestScore) {
                bestScore = score
                bestMove = move
            }
        }
        return bestMove ?: availableMoves.randomOrNull() // Fallback if all scores are equally bad
    }

    /**
     * Implements the minimax algorithm to find the optimal move.
     * @param p1Moves Current list of moves for Player 1 (X).
     * @param p2Moves Current list of moves for Player 2 (O).
     * @param depth Current depth in the game tree.
     * @param isMaximizing True if the current turn is for the maximizing player (AI - O), false for minimizing (Human - X).
     * @return The score of the board state.
     */
    private fun minimax(p1Moves: List<String>, p2Moves: List<String>, depth: Int, isMaximizing: Boolean): Double {
        // Visible moves for win checking
        val p1VisibleMoves = p1Moves.takeLast(MAX_VISIBLE_MOVES_PER_PLAYER)
        val p2VisibleMoves = p2Moves.takeLast(MAX_VISIBLE_MOVES_PER_PLAYER)

        // Check for terminal states (Win/Loss) based on VISIBLE moves
        // AI (Player O) is the maximizing player. Human (Player X) is the minimizing player.
        when {
            isAIWinningCombination(p2VisibleMoves) -> return 10.0 - depth // AI (O) wins, score is positive
            isAIWinningCombination(p1VisibleMoves) -> return -10.0 + depth // Player (X) wins, score is negative
        }

        // Check for draw: if all cells are visibly occupied and no one won
        val allBoardCells = (1..9).map { "button$it" }
        val occupiedVisibleCells = (p1VisibleMoves + p2VisibleMoves).toSet()
        if (occupiedVisibleCells.size == 9 && !isAIWinningCombination(p1VisibleMoves) && !isAIWinningCombination(p2VisibleMoves)) {
            return 0.0 // Draw
        }

        // Limit search depth to prevent long computation, especially in infinite mode
        // Adjust depth limit as needed for performance vs AI strength
        if (depth > 4) return 0.0 // Heuristic: deeper searches are costly

        // Available moves for recursion, based on visible moves in the current SIMULATED state
        val currentSimP1Visible = p1Moves.takeLast(MAX_VISIBLE_MOVES_PER_PLAYER)
        val currentSimP2Visible = p2Moves.takeLast(MAX_VISIBLE_MOVES_PER_PLAYER)
        val availableMoves = allBoardCells.filter { buttonId -> // Changed allMoves to allBoardCells
            !currentSimP1Visible.contains(buttonId) && !currentSimP2Visible.contains(buttonId)
        }

        if (availableMoves.isEmpty()) return 0.0 // No moves left, treat as draw for this path

        if (isMaximizing) { // AI's turn (Player O) - wants to maximize score
            var bestScore = Double.NEGATIVE_INFINITY
            for (move in availableMoves) {
                // AI (Player O) makes a move. Pass FULL histories for recursion.
                val score = minimax(p1Moves, p2Moves + move, depth + 1, false)
                bestScore = maxOf(bestScore, score)
            }
            return bestScore
        } else { // Human's turn (Player X) - wants to minimize AI's score
            var bestScore = Double.POSITIVE_INFINITY
            for (move in availableMoves) {
                // Human (Player X) makes a move. Pass FULL histories for recursion.
                val score = minimax(p1Moves + move, p2Moves, depth + 1, true)
                bestScore = minOf(bestScore, score)
            }
            return bestScore
        }
    }

    /**
     * Checks if a given list of moves (assumed to be visible moves) constitutes a win.
     * This is used by the AI's minimax algorithm.
     * @param currentVisibleMoves A list of button IDs representing a player's visible moves.
     * @return True if the moves form a winning combination, false otherwise.
     */
    private fun isAIWinningCombination(currentVisibleMoves: List<String>): Boolean {
        if (currentVisibleMoves.size < 3) return false // Optimization
        val movesSet = currentVisibleMoves.toSet()
        // WINNING_COMBINATIONS is accessible from companion object
        return WINNING_COMBINATIONS.any { combination ->
            movesSet.containsAll(combination)
        }
    }
}
