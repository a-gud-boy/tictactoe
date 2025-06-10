package com.a_gud_boy.tictactoe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Enum to represent the player
enum class Player {
    X, O
}

enum class AIDifficulty {
    EASY, MEDIUM, HARD
}

// Data class to hold winner information
data class WinnerInfo(
    val winner: Player?,
    val combination: Set<String>,
    val orderedWinningMoves: List<String> // Added field for ordered winning moves
)

class NormalTicTacToeViewModel(private val soundManager: SoundManager) : ViewModel() {

    private val _isAIMode = MutableStateFlow(false)
    val isAIMode: StateFlow<Boolean> = _isAIMode.asStateFlow()

    private val _aiDifficulty = MutableStateFlow(AIDifficulty.MEDIUM)
    val aiDifficulty: StateFlow<AIDifficulty> = _aiDifficulty.asStateFlow()

    companion object {
        // Defines all possible winning combinations (lines) on a 3x3 Tic Tac Toe board.
        // Each set contains the button IDs forming a winning line.
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

    private val volume = 1.0f // Default volume for sound effects, can be adjusted.

    // Tracks the number of wins for Player 1 (X).
    private val _player1Wins = MutableStateFlow(0)
    val player1Wins: StateFlow<Int> = _player1Wins.asStateFlow()

    // Tracks the number of wins for Player 2 (O).
    private val _player2Wins = MutableStateFlow(0)
    val player2Wins: StateFlow<Int> = _player2Wins.asStateFlow()

    // Stores the sequence of moves (button IDs) made by Player 1 (X).
    private val _player1Moves = MutableStateFlow<List<String>>(emptyList())
    val player1Moves: StateFlow<List<String>> = _player1Moves.asStateFlow()

    // Stores the sequence of moves (button IDs) made by Player 2 (O).
    private val _player2Moves = MutableStateFlow<List<String>>(emptyList())
    val player2Moves: StateFlow<List<String>> = _player2Moves.asStateFlow()

    // Holds information about the winner and the winning combination, if any. Null if no winner yet.
    private val _winnerInfo = MutableStateFlow<WinnerInfo?>(null)
    val winnerInfo: StateFlow<WinnerInfo?> = _winnerInfo.asStateFlow()

    // Indicates whose turn it is: true for Player 1 (X), false for Player 2 (O) or AI.
    private val _player1Turn = MutableStateFlow(true)
    val player1Turn: StateFlow<Boolean> = _player1Turn.asStateFlow()

    // True if the game is currently active and moves can be made. False if paused or concluded.
    private val _gameStarted = MutableStateFlow(true) // Game starts active by default.
    val gameStarted: StateFlow<Boolean> = _gameStarted.asStateFlow()

    // True if the current round has concluded (either a win or a draw).
    private val _isGameConcluded = MutableStateFlow(false)
    val isGameConcluded: StateFlow<Boolean> = _isGameConcluded.asStateFlow()

    // Derived state providing a user-friendly string for the current turn or game result.
    // It considers if AI mode is active for appropriate messaging (e.g., "AI's Turn", "AI Won!").
    val turnDenotingText: StateFlow<String> = combine(
        player1Turn, // Whose turn is it?
        winnerInfo,  // Is there a winner or a draw?
        isGameConcluded, // Has the game concluded?
        isAIMode         // Is AI mode active?
    ) { isP1Turn, winnerData, gameConcluded, aiMode ->
        when {
            winnerData != null && winnerData.winner == Player.X -> if (aiMode) "You Won!" else "Player 1 Won!" // Player X (Human or P1) wins
            winnerData != null && winnerData.winner == Player.O -> if (aiMode) "AI Won!" else "Player 2 Won!" // Player O (AI or P2) wins
            gameConcluded && winnerData?.winner == null -> "It's a Draw!" // Game is a draw
            isP1Turn -> if (aiMode) "Your Turn" else "Player 1's Turn" // Player X's (Human or P1) turn
            else -> if (aiMode) "AI's Turn" else "Player 2's Turn" // Player O's (AI or P2) turn
        }
    }.stateIn(
        viewModelScope,
        kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), // Keep active for 5s after last subscriber
        if (_isAIMode.value) "Your Turn" else "Player 1's Turn" // Initial value
    )

    // Derived state that determines the text for the reset button (e.g., "Reset Round" or "New Round").
    val resetButtonText: StateFlow<String> = combine(
        isGameConcluded // Depends only on whether the game has concluded.
    ) { (gameConcluded) -> // Destructure the Boolean from the array/list passed by combine
        if (gameConcluded) "New Round" else "Reset Round"
    }.stateIn(
        viewModelScope,
        kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        "Reset Round" // Default/initial text
    )


    /**
     * Handles logic when a cell (button) on the Tic Tac Toe board is clicked.
     * - Validates the move (game active, cell not already played).
     * - Updates player moves.
     * - Switches turns.
     * - Plays a move sound.
     * - Checks for a winner or draw.
     * - If AI mode is active and it's AI's turn, triggers the AI move.
     * @param buttonId The ID of the clicked button (e.g., "button1").
     */
    fun onButtonClick(buttonId: String) {
        // Ignore clicks if the game hasn't started or has already concluded.
        if (!_gameStarted.value || _isGameConcluded.value) return

        val currentP1Moves = _player1Moves.value
        val currentP2Moves = _player2Moves.value

        // Ignore click if the button/cell has already been played by either player.
        if (currentP1Moves.contains(buttonId) || currentP2Moves.contains(buttonId)) {
            return // Button already played.
        }

        if (_player1Turn.value) {
            // Player 1's (Human, usually 'X') move.
            _player1Moves.value = currentP1Moves + buttonId // Add move to Player 1's list.
            _player1Turn.value = false // Switch turn to Player 2 or AI.
            soundManager.playMoveSound(volume) // Play sound for human move.
            checkForWinner() // Check if this move resulted in a win or draw.

            // If AI mode is active and the game is not yet concluded, let the AI make a move.
            if (_isAIMode.value && !_isGameConcluded.value) {
                makeAIMove()
            }
        } else if (!_isAIMode.value) {
            // Player 2's (Human, usually 'O') move, only if not in AI mode.
            _player2Moves.value = currentP2Moves + buttonId // Add move to Player 2's list.
            _player1Turn.value = true // Switch turn back to Player 1.
            soundManager.playMoveSound(volume) // Play sound for human move.
            checkForWinner() // Check if this move resulted in a win or draw.
        } else {
            // This block is executed when it's AI's turn (player1Turn is false and isAIMode is true).
            // The move (buttonId) is coming from the AI's decision logic (makeAIMove -> onButtonClick).
            _player2Moves.value = currentP2Moves + buttonId // Add AI's move to Player 2's list.
            _player1Turn.value = true // Switch turn back to Human Player 1.
            // Sound for AI's move is typically played in `makeAIMove` before this call for better UX.
            checkForWinner() // Check if AI's move resulted in a win or draw.
        }
    }

    /**
     * Checks the current game board state for a winner or a draw condition.
     * - Optimized to check only relevant winning combinations based on the last move made.
     * - If a winner is found, updates `winnerInfo`, player scores, and game state.
     * - If a draw occurs (all cells filled, no winner), updates `winnerInfo` and game state.
     * - Plays appropriate sound effects for win, lose (from P1's perspective), or draw.
     */
    private fun checkForWinner() {
        val p1CurrentMovesList = _player1Moves.value
        val p2CurrentMovesList = _player2Moves.value
        val p1MovesSet = p1CurrentMovesList.toSet() // Use sets for efficient `containsAll` check.
        val p2MovesSet = p2CurrentMovesList.toSet()

        // Early exit if neither player has made enough moves to potentially win (at least 3).
        if (p1MovesSet.size < 3 && p2MovesSet.size < 3) return

        // Determine the last move made to optimize checking.
        // If player1Turn is true, it means Player 2 (or AI) just made a move.
        // If player1Turn is false, it means Player 1 just made a move.
        val lastMove = if (_player1Turn.value) p2CurrentMovesList.lastOrNull() else p1CurrentMovesList.lastOrNull()
        if (lastMove == null) return // Should not happen if moves are made, but good for safety.

        // Filter `WINNING_COMBINATIONS` to only those that include the `lastMove`.
        // This avoids checking all combinations on every turn.
        val relevantCombinations = WINNING_COMBINATIONS.filter { it.contains(lastMove) }

        for (combination in relevantCombinations) {
            if (p1MovesSet.containsAll(combination)) {
                // Player 1 (X) wins.
                // The `orderedWinningMoves` are taken directly from the `combination` set's definition
                // to ensure a consistent geometric order for line drawing, rather than chronological.
                val orderedWin = combination.toList() // Convert set to list for WinnerInfo.
                _winnerInfo.value = WinnerInfo(Player.X, combination, orderedWin)
                _player1Wins.value += 1
                _isGameConcluded.value = true
                _gameStarted.value = false // Stop the game, awaiting reset.
                soundManager.playWinSound(volume) // Sound for Player X winning.
                return // Winner found, no need to check further.
            }
            if (p2MovesSet.containsAll(combination)) {
                // Player 2 (O or AI) wins.
                val orderedWin = combination.toList()
                _winnerInfo.value = WinnerInfo(Player.O, combination, orderedWin)
                _player2Wins.value += 1
                _isGameConcluded.value = true
                _gameStarted.value = false // Stop the game, awaiting reset.
                soundManager.playLoseSound(volume) // Sound for Player O winning (lose from P1's perspective if P1 is human).
                return // Winner found.
            }
        }

        // Check for a draw: all 9 cells are filled and no winner has been determined yet.
        if ((p1MovesSet.size + p2MovesSet.size) == 9 && _winnerInfo.value == null) {
            _winnerInfo.value = WinnerInfo(null, emptySet(), emptyList()) // null winner indicates a draw.
            _isGameConcluded.value = true
            _gameStarted.value = false // Stop the game.
            soundManager.playDrawSound(volume) // Play draw sound.
        }
    }

    /**
     * Resets the game board for a new round.
     * Clears player moves, winner information, and resets the turn to Player 1.
     * Scores and AI mode/difficulty are preserved.
     */
    fun resetRound() {
        _player1Moves.value = emptyList()
        _player2Moves.value = emptyList()
        _winnerInfo.value = null
        _player1Turn.value = true // Player 1 (X or Human) starts the new round.
        _isGameConcluded.value = false
        _gameStarted.value = true // Make the game active for the new round.
    }

    /**
     * Resets all game scores to zero and then resets the current round.
     */
    fun resetScores() {
        _player1Wins.value = 0
        _player2Wins.value = 0
        resetRound() // Also reset the board and game state for a fresh start.
    }

    /**
     * Initiates the AI's move if the game is in AI mode and it's AI's turn.
     * Includes a delay for better user experience.
     * The AI's move difficulty determines the strategy (random, minimax, or mixed).
     */
    private fun makeAIMove() {
        // Ensure AI only moves if game is active, not concluded, and it's AI's (Player O's) turn.
        if (!_gameStarted.value || _isGameConcluded.value || _player1Turn.value) return

        viewModelScope.launch {
            delay(500) // Artificial delay to make AI's move feel more natural.
            soundManager.playComputerMoveSound(volume) // Play sound effect for AI "thinking" or making a move.
            val move = when (_aiDifficulty.value) {
                AIDifficulty.EASY -> getRandomMove() // Easy: AI makes a random valid move.
                AIDifficulty.MEDIUM -> if (Math.random() < 0.5) getBestMove() else getRandomMove() // Medium: 50/50 chance of best or random move.
                AIDifficulty.HARD -> getBestMove() // Hard: AI tries to make the optimal move using minimax.
            }
            // If a valid move is determined, call onButtonClick to apply it.
            // This will trigger turn switch, sound, and winner check for AI's move.
            move?.let { onButtonClick(it) }
        }
    }

    /**
     * Selects a random available cell on the board for the AI's move.
     * @return The button ID of a randomly chosen empty cell, or null if no moves are available.
     */
    private fun getRandomMove(): String? {
        val allMoves = (1..9).map { "button$it" } // All possible cell IDs.
        // Filter out cells already occupied by Player 1 or Player 2.
        val availableMoves = allMoves.filter { buttonId ->
            !_player1Moves.value.contains(buttonId) && !_player2Moves.value.contains(buttonId)
        }
        return availableMoves.randomOrNull() // Return a random available move, or null if none.
    }

    /**
     * Determines the best possible move for the AI (Player O) using the minimax algorithm.
     * It iterates through all available moves, simulates each, and chooses the one with the highest minimax score.
     * @return The button ID of the best move, or null if no moves are available.
     */
    private fun getBestMove(): String? {
        val allMoves = (1..9).map { "button$it" }
        val availableMoves = allMoves.filter { buttonId ->
            !_player1Moves.value.contains(buttonId) && !_player2Moves.value.contains(buttonId)
        }

        var bestScore = Double.NEGATIVE_INFINITY // AI (Player O) is the maximizing player in this minimax context.
        var bestMove: String? = null

        for (move in availableMoves) {
            // Simulate AI making 'move'. Player 2's moves are AI's moves.
            // The next turn in minimax will be for Player 1 (minimizing player).
            val score = minimax(
                p1Moves = _player1Moves.value,
                p2Moves = _player2Moves.value + move, // AI makes this move
                depth = 0,
                isMaximizing = false // Next turn is Player 1's (minimizer)
            )
            if (score > bestScore) {
                bestScore = score
                bestMove = move
            }
        }
        return bestMove
    }

    /**
     * Implements the minimax algorithm to determine the score of a board state.
     * - Player O (AI) is the maximizing player.
     * - Player X (Human) is the minimizing player.
     * @param p1Moves Current moves of Player 1 (X).
     * @param p2Moves Current moves of Player 2 (O, AI).
     * @param depth Current depth in the game tree (not strictly used for termination here, but good for potential enhancements).
     * @param isMaximizing True if it's AI's (Player O) turn to maximize score, false if it's Player X's turn to minimize.
     * @return The heuristic score of the board state: 1.0 for AI win, -1.0 for Human win, 0.0 for draw.
     */
    private fun minimax(
        p1Moves: List<String>,
        p2Moves: List<String>,
        depth: Int,
        isMaximizing: Boolean
    ): Double {
        // Terminal conditions:
        if (isWinningCombination(p2Moves)) return 1.0 // AI (Player O) wins - good for AI.
        if (isWinningCombination(p1Moves)) return -1.0 // Player X wins - bad for AI.
        if (p1Moves.size + p2Moves.size == 9) return 0.0 // Draw - neutral.

        val allMoves = (1..9).map { "button$it" }
        val availableMoves = allMoves.filter { buttonId ->
            !p1Moves.contains(buttonId) && !p2Moves.contains(buttonId)
        }

        if (isMaximizing) { // AI's turn (Player O) - wants to maximize the score.
            var bestScore = Double.NEGATIVE_INFINITY
            for (move in availableMoves) {
                // AI simulates making 'move', then it's Player X's turn (minimizing).
                val score = minimax(p1Moves, p2Moves + move, depth + 1, false)
                bestScore = maxOf(bestScore, score)
            }
            return bestScore
        } else { // Player X's turn - wants to minimize the score (from AI's perspective).
            var bestScore = Double.POSITIVE_INFINITY
            for (move in availableMoves) {
                // Player X simulates making 'move', then it's AI's turn (maximizing).
                val score = minimax(p1Moves + move, p2Moves, depth + 1, true)
                bestScore = minOf(bestScore, score)
            }
            return bestScore
        }
    }

    /**
     * Utility function to check if a given list of moves constitutes a winning combination.
     * @param moves A list of button IDs representing a player's moves.
     * @return True if the moves contain any of the predefined winning combinations, false otherwise.
     */
    private fun isWinningCombination(moves: List<String>): Boolean {
        val movesSet = moves.toSet() // Convert to set for efficient `containsAll`.
        return WINNING_COMBINATIONS.any { combination ->
            movesSet.containsAll(combination)
        }
    }

    /**
     * Enables or disables AI mode.
     * Resets the current round when mode is changed.
     * @param enabled True to enable AI mode, false to disable.
     */
    fun setAIMode(enabled: Boolean) {
        _isAIMode.value = enabled
        resetRound() // Reset the board when switching modes.
    }

    /**
     * Sets the difficulty level for the AI.
     * If AI mode is currently active, resets the round to apply the new difficulty.
     * @param difficulty The desired AI difficulty level (EASY, MEDIUM, HARD).
     */
    fun setAIDifficulty(difficulty: AIDifficulty) {
        _aiDifficulty.value = difficulty
        if (_isAIMode.value) {
            resetRound() // Reset board if AI is active to reflect new difficulty.
        }
    }

    /**
     * Called when the ViewModel is about to be destroyed.
     * Releases SoundManager resources to prevent memory leaks.
     */
    override fun onCleared() {
        super.onCleared()
        soundManager.release() // Release SoundManager resources.
    }
}
