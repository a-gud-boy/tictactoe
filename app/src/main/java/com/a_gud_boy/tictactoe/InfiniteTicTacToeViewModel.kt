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

// AIDifficulty is now in its own file: AIDifficulty.kt

// Enum to represent the player (ensure this is present or imported)
//enum class Player {
//    X, O
//}

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
 * It tracks player scores, moves (full history and visible ones), turn information,
 * and game status (e.g., win, active). AI opponent functionality is also included.
 *
 * Key features managed:
 * - **Player Moves**: Stores the complete history of moves for Player X and Player O.
 *   However, only the last [MAX_VISIBLE_MOVES_PER_PLAYER] for each player are
 *   considered "visible" for display and for determining winning conditions.
 * - **Win Tracking**: Detects winning combinations based on the currently "visible" moves.
 * - **Turn Management**: Alternates turns between Player 1 (X) (Human) and Player 2 (O) (Human or AI).
 * - **Game State**: Manages whether the game is started, concluded, or awaiting a new round.
 * - **Score Keeping**: Counts the number of wins for each player.
 * - **AI Opponent**: Provides an AI opponent with varying difficulty levels (Easy, Medium, Hard)
 *   using a minimax algorithm for decision-making in Medium/Hard modes.
 * - **UI State Exposure**: Exposes game state information as [StateFlow]s to be observed by the UI,
 *   including derived states like `turnDenotingText` (e.g., "Your Turn", "AI Won!")
 *   and `resetButtonText` (e.g., "Reset Round", "New Round").
 */
class InfiniteTicTacToeViewModel(private val soundManager: SoundManager) : ViewModel() {

    companion object {
        /**
         * The maximum number of moves per player that remain "visible" on the board
         * and are considered for winning conditions in the Infinite Tic Tac Toe mode.
         * Older moves beyond this count are effectively "gone" for win-checking purposes.
         */
        const val MAX_VISIBLE_MOVES_PER_PLAYER = 3

        /**
         * A list of all possible winning combinations on a 3x3 Tic Tac Toe board.
         * Each combination is a set of button IDs (e.g., "button1", "button2", "button3")
         * representing a line (row, column, or diagonal).
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

    private val volume = 1.0f // Default volume for sound effects.

    // Tracks the number of wins for Player 1 (X).
    private val _player1Wins = MutableStateFlow(0)
    /** StateFlow representing the number of wins for Player 1 (X). */
    val player1Wins: StateFlow<Int> = _player1Wins.asStateFlow()

    // Tracks the number of wins for Player 2 (O or AI).
    private val _player2Wins = MutableStateFlow(0)
    /** StateFlow representing the number of wins for Player 2 (O or AI). */
    val player2Wins: StateFlow<Int> = _player2Wins.asStateFlow()

    // Stores the full history of moves (button IDs) made by Player 1 (X).
    // Only the last MAX_VISIBLE_MOVES_PER_PLAYER are shown/used for win checks.
    private val _player1Moves = MutableStateFlow<List<String>>(emptyList())
    /** StateFlow representing the list of all moves made by Player 1 (X). */
    val player1Moves: StateFlow<List<String>> = _player1Moves.asStateFlow()

    // Stores the full history of moves (button IDs) made by Player 2 (O or AI).
    private val _player2Moves = MutableStateFlow<List<String>>(emptyList())
    /** StateFlow representing the list of all moves made by Player 2 (O or AI). */
    val player2Moves: StateFlow<List<String>> = _player2Moves.asStateFlow()

    // Holds information about the winner and the winning combination, if any. Null if no winner.
    private val _winnerInfo = MutableStateFlow<WinnerInfo?>(null)
    /**
     * StateFlow holding information about the winner of the current round, if any.
     * Contains the winning [Player] and the [Set] of button IDs forming the winning combination
     * from the visible moves. Null if there is no winner yet.
     */
    val winnerInfo: StateFlow<WinnerInfo?> = _winnerInfo.asStateFlow()

    // Indicates whose turn it is: true for Player 1 (X), false for Player 2 (O or AI).
    private val _player1Turn = MutableStateFlow(true)
    /** StateFlow indicating if it is currently Player 1's (X) turn. True if yes, false for Player 2 (O or AI). */
    val player1Turn: StateFlow<Boolean> = _player1Turn.asStateFlow()

    // True if the game is currently active and moves can be made.
    private val _gameStarted = MutableStateFlow(true) // Game starts active by default.
    /** StateFlow indicating if the game is currently active (i.e., players can make moves). */
    val gameStarted: StateFlow<Boolean> = _gameStarted.asStateFlow()

    // True if the current round has concluded (win). No draw condition in this Infinite mode.
    private val _isGameConcluded = MutableStateFlow(false)
    /** StateFlow indicating if the current round of the game has concluded (e.g., due to a win). */
    val isGameConcluded: StateFlow<Boolean> = _isGameConcluded.asStateFlow()

    // True if playing against AI, false for two-player mode.
    private val _isAIMode = MutableStateFlow(AISettingsManager.isAiModeEnabled)
    val isAIMode: StateFlow<Boolean> = _isAIMode.asStateFlow()

    // Current difficulty level for the AI opponent.
    private val _aiDifficulty = MutableStateFlow(AISettingsManager.currentDifficulty) // Default to Medium difficulty.
    val aiDifficulty: StateFlow<AIDifficulty> = _aiDifficulty.asStateFlow()

    /**
     * Derived StateFlow providing a user-friendly text string for the current turn or game result.
     * Considers AI mode for messages like "Your Turn", "AI Won!", "Player 2 Won!".
     */
    val turnDenotingText: StateFlow<String> = combine(
        player1Turn,    // Whose turn?
        winnerInfo,     // Is there a winner?
        isAIMode,       // Is it AI mode?
        isGameConcluded // Has the game concluded?
    ) { isP1Turn, winnerData, aiMode, gameConcluded ->
        when {
            winnerData != null && winnerData.winner == Player.X -> if (aiMode) "You Won!" else "Player 1 Won!"
            winnerData != null && winnerData.winner == Player.O -> if (aiMode) "AI Won!" else "Player 2 Won!"
            // No explicit draw text here as infinite mode doesn't typically have a board-full draw.
            // If a draw condition were added, it would be handled here.
            isP1Turn -> if (aiMode) "Your Turn" else "Player 1's Turn"
            else -> if (aiMode) "AI's Turn" else "Player 2's Turn"
        }
    }.stateIn(
        viewModelScope,
        kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), // Keep active for 5s
        if (_isAIMode.value) "Your Turn" else "Player 1's Turn" // Initial value
    )

    /**
     * Derived StateFlow providing the text for the main action button (e.g., "Reset Round", "New Round").
     * Changes based on whether the game has concluded.
     */
    val resetButtonText: StateFlow<String> = combine(
        isGameConcluded // Depends only on game conclusion state.
    ) { (gameConcluded) -> // Destructure the Boolean
        if (gameConcluded) "New Round" else "Reset Round"
    }.stateIn(
        viewModelScope,
        kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        "Reset Round" // Default text
    )

    /**
     * Sets the AI mode (on or off).
     * Resets the current round whenever the AI mode is changed.
     * @param enabled True to enable AI mode, false for two-player mode.
     */
    fun setAIMode(enabled: Boolean) {
        _isAIMode.value = enabled
        AISettingsManager.isAiModeEnabled = enabled
        resetRound() // Reset the game to apply mode change.
    }

    /**
     * Sets the AI difficulty level.
     * If AI mode is active, resets the current round to apply the new difficulty.
     * @param difficulty The desired [AIDifficulty] (EASY, MEDIUM, HARD).
     */
    fun setAIDifficulty(difficulty: AIDifficulty) {
        _aiDifficulty.value = difficulty
        AISettingsManager.currentDifficulty = difficulty
        if (_isAIMode.value) { // Only reset if AI is active.
            resetRound()
        }
    }

    /**
     * Handles the logic when a button (cell) on the Tic Tac Toe board is clicked.
     *
     * - Ignores clicks if the game is not active or already concluded.
     * - Checks if the clicked cell is already visibly occupied by either player.
     * - If the cell is valid for a move:
     *   1. Adds the move to the current player's full move history.
     *   2. If the player's move history now exceeds [MAX_VISIBLE_MOVES_PER_PLAYER],
     *      it truncates the list to keep only the most recent [MAX_VISIBLE_MOVES_PER_PLAYER] moves.
     *      This creates the "disappearing moves" effect for win checking and display.
     *   3. Switches the turn to the other player.
     *   4. Plays a sound for the move (if human player).
     *   5. Checks if this move results in a win based on visible moves.
     *   6. If AI mode is active and it's now AI's turn, triggers the AI's move.
     *
     * @param buttonId The ID of the button/cell that was clicked (e.g., "button1").
     */
    fun onButtonClick(buttonId: String) {
        // Ignore clicks if game not active or already over.
        if (!_gameStarted.value || _isGameConcluded.value) return

        val currentP1FullMoves = _player1Moves.value
        val currentP2FullMoves = _player2Moves.value

        // Check against only the *visible* moves to see if a cell is "occupied" for clicking purposes.
        val p1VisibleMoves = currentP1FullMoves.takeLast(MAX_VISIBLE_MOVES_PER_PLAYER)
        val p2VisibleMoves = currentP2FullMoves.takeLast(MAX_VISIBLE_MOVES_PER_PLAYER)

        if (p1VisibleMoves.contains(buttonId) || p2VisibleMoves.contains(buttonId)) {
            return // Button is currently visibly occupied by a player.
        }

        val isPlayer1MakingMove = _player1Turn.value

        if (isPlayer1MakingMove) { // Player 1 (Human) is making a move.
            val newP1FullMoves = currentP1FullMoves.toMutableList()
            newP1FullMoves.add(buttonId) // Add to full history.
            // Update player1Moves with the new list, ensuring it's capped at MAX_VISIBLE_MOVES_PER_PLAYER for effective game logic.
            // The actual full list is kept for potential future features, but game logic uses the capped version.
            // Correction: The StateFlow should hold the full list, and `takeLast` is used by consumers (like `checkForWinner` or UI).
            _player1Moves.value = newP1FullMoves // Store the full list.

            _player1Turn.value = false // Switch turn to Player 2 (or AI).
            soundManager.playMoveSound(volume) // Play sound for human move.
            checkForWinner() // Check if this move resulted in a win.

            // If AI mode is on, game is not over, and it's now AI's turn.
            if (_isAIMode.value && !_isGameConcluded.value && !_player1Turn.value) {
                makeAIMove()
            }
        } else { // Player 2 (Human or AI) is making a move.
            val newP2FullMoves = currentP2FullMoves.toMutableList()
            newP2FullMoves.add(buttonId) // Add to full history.
            _player2Moves.value = newP2FullMoves // Store the full list.

            _player1Turn.value = true // Switch turn back to Player 1.
            // Play sound only if it's a human Player 2 (not AI).
            // AI move sound is played in `makeAIMove` before this `onButtonClick` is called by AI.
            if (!_isAIMode.value) {
                soundManager.playMoveSound(volume)
            }
            checkForWinner() // Check if this move resulted in a win.
        }
    }

    /**
     * Checks if the current set of *visible* moves for either player constitutes a win.
     * - It uses the last [MAX_VISIBLE_MOVES_PER_PLAYER] from each player's move history.
     * - Iterates through [WINNING_COMBINATIONS] for a match.
     * - If a win is detected:
     *   - Updates [_winnerInfo] with the winner and the specific winning combination.
     *   - Increments the winner's score.
     *   - Sets [_isGameConcluded] to true and [_gameStarted] to false.
     *   - Plays the appropriate win/lose sound.
     * - Note: Infinite Tic Tac Toe typically doesn't have a "draw" condition from a full board,
     *   as moves disappear. A draw would need a different rule (e.g., move limit).
     */
    private fun checkForWinner() {
        // Get the last MAX_VISIBLE_MOVES_PER_PLAYER moves for each player for win checking.
        val p1VisibleMoves = _player1Moves.value.takeLast(MAX_VISIBLE_MOVES_PER_PLAYER)
        val p2VisibleMoves = _player2Moves.value.takeLast(MAX_VISIBLE_MOVES_PER_PLAYER)
        val p1CurrentVisibleMovesSet = p1VisibleMoves.toSet()
        val p2CurrentVisibleMovesSet = p2VisibleMoves.toSet()

        // Early exit if neither player has enough visible moves to form a line.
        if (p1VisibleMoves.size < 3 && p2VisibleMoves.size < 3) return

        // Determine the last move made (from the visible set) to optimize checking.
        // If player1Turn is true, it means Player 2 (or AI) just made a move.
        // If player1Turn is false, it means Player 1 just made a move.
        val lastPlayerToMoveWasP1 = !_player1Turn.value // If it's P2's turn now, P1 made the last move.
        val lastMove = if (lastPlayerToMoveWasP1) p1VisibleMoves.lastOrNull() else p2VisibleMoves.lastOrNull()

        if (lastMove == null) return // Should not happen if a move was just made.

        // Filter winning combinations to only those containing the last move made.
        val relevantCombinations = WINNING_COMBINATIONS.filter { it.contains(lastMove) }

        for (combination in relevantCombinations) {
            if (p1CurrentVisibleMovesSet.containsAll(combination)) {
                // Player 1 (X) wins.
                // `orderedWin` uses the geometric definition from `combination` for consistent line drawing.
                val orderedWin = combination.toList()
                _winnerInfo.value = WinnerInfo(Player.X, combination, orderedWin)
                _player1Wins.value += 1
                _isGameConcluded.value = true
                _gameStarted.value = false // Stop the game, awaiting reset.
                soundManager.playWinSound(volume) // Sound for Player X winning.
                return // Winner found.
            }
            if (p2CurrentVisibleMovesSet.containsAll(combination)) {
                // Player 2 (O or AI) wins.
                val orderedWin = combination.toList()
                _winnerInfo.value = WinnerInfo(Player.O, combination, orderedWin)
                _player2Wins.value += 1
                _isGameConcluded.value = true
                _gameStarted.value = false // Stop the game.
                soundManager.playLoseSound(volume) // Sound for Player O winning (lose from P1's perspective).
                return // Winner found.
            }
        }
        // No specific draw condition (like full board) in this version of Infinite TicTacToe
        // because old moves disappear. A draw would require other rules (e.g., move limit).
    }

    /**
     * Resets the game board and state for a new round in Infinite Tic Tac Toe.
     * - Clears the full move histories for both players.
     * - Resets winner information.
     * - Sets Player 1 (Human/X) as the starting player.
     * - Marks the game as active ([_gameStarted] = true) and not concluded ([_isGameConcluded] = false).
     * Scores and AI settings (mode, difficulty) are preserved.
     */
    fun resetRound() {
        _player1Moves.value = emptyList() // Clear full move history for Player 1.
        _player2Moves.value = emptyList() // Clear full move history for Player 2.
        _winnerInfo.value = null
        _player1Turn.value = true // Player 1 (Human/X) always starts a new round.
        _isGameConcluded.value = false
        _gameStarted.value = true // The game is now active for the new round.
    }

    /**
     * Resets the scores for both players to zero.
     * Also calls [resetRound] to reset the current game board and state.
     */
    fun resetScores() {
        _player1Wins.value = 0
        _player2Wins.value = 0
        resetRound() // Reset the board for a completely fresh game.
    }

    /**
     * Initiates the AI's move if AI mode is active and it's AI's turn.
     * - Includes a delay for better user experience and to simulate "thinking".
     * - Plays a computer move sound.
     * - Determines the move based on the current AI difficulty level.
     * - Calls `onButtonClick` with the AI's chosen move to process it.
     */
    fun makeAIMove() {
        // AI moves only if game is active, not over, it's AI's turn (P2, so P1 turn is false), and AI mode is on.
        if (!_gameStarted.value || _isGameConcluded.value || _player1Turn.value || !_isAIMode.value) return

        viewModelScope.launch {
            delay(500) // Artificial delay for UX.
            soundManager.playComputerMoveSound(volume) // Sound for AI making a move.
            val move = when (_aiDifficulty.value) {
                AIDifficulty.EASY -> getRandomMove()
                AIDifficulty.MEDIUM -> if (Math.random() < 0.6) getBestMove() else getRandomMove() // 60% best, 40% random
                AIDifficulty.HARD -> getBestMove()
            }
            // `onButtonClick` will handle adding the move to _player2Moves, switching turn, and checking for winner.
            // It correctly identifies AI's move because _player1Turn is false at this point.
            move?.let { onButtonClick(it) }
        }
    }

    /**
     * Selects a random available move for the AI in Infinite Tic Tac Toe.
     * A cell is considered "available" if it is not among the currently *visible*
     * moves of either player (i.e., their last [MAX_VISIBLE_MOVES_PER_PLAYER] moves).
     * @return A string representing the button ID of a randomly chosen available cell,
     *         or null if no such cells are available (highly unlikely in infinite mode).
     */
    private fun getRandomMove(): String? {
        val allBoardCells = (1..9).map { "button$it" }
        // Filter for cells not currently "visible" (occupied) by either player.
        val p1Visible = _player1Moves.value.takeLast(MAX_VISIBLE_MOVES_PER_PLAYER)
        val p2Visible = _player2Moves.value.takeLast(MAX_VISIBLE_MOVES_PER_PLAYER)
        val availableMoves = allBoardCells.filter { buttonId ->
            !p1Visible.contains(buttonId) && !p2Visible.contains(buttonId)
        }
        return availableMoves.randomOrNull()
    }

    /**
     * Determines the best possible move for the AI (Player O) using the minimax algorithm,
     * adapted for Infinite Tic Tac Toe (considers only visible moves for win states).
     * It iterates through all cells not currently visibly occupied, simulates placing a move there,
     * and chooses the move that maximizes its minimax score.
     * @return The button ID of the best move, or a random available move if no best move is clearly determined
     *         (e.g., all moves lead to same score, or no moves available).
     */
    private fun getBestMove(): String? {
        val allBoardCells = (1..9).map { "button$it" }
        val p1Visible = _player1Moves.value.takeLast(MAX_VISIBLE_MOVES_PER_PLAYER)
        val p2Visible = _player2Moves.value.takeLast(MAX_VISIBLE_MOVES_PER_PLAYER)
        // AI considers moves in cells not currently visibly occupied.
        val availableMoves = allBoardCells.filter { buttonId ->
            !p1Visible.contains(buttonId) && !p2Visible.contains(buttonId)
        }

        if (availableMoves.isEmpty()) return null // Should be rare in infinite mode.

        var bestScore = Double.NEGATIVE_INFINITY
        var bestMove: String? = null

        // AI (Player O) is the maximizing player.
        for (move in availableMoves) {
            // Simulate AI (Player O) making a 'move'.
            // Minimax is called with the *full* move histories, as it manages its own 'takeLast' for visible checks.
            val score = minimax(
                p1Moves = _player1Moves.value,         // Player X's full move history.
                p2Moves = _player2Moves.value + move,  // Player O's full history + current simulated move.
                depth = 0,
                isMaximizing = false // Next turn is Player X (Human), who is the minimizing player.
            )
            if (score > bestScore) {
                bestScore = score
                bestMove = move
            }
        }
        // Fallback to a random move if no distinctly "best" move is found (e.g., all scores are equal, or no moves).
        return bestMove ?: availableMoves.randomOrNull()
    }

    /**
     * Implements the minimax algorithm for Infinite Tic Tac Toe.
     * - Evaluates board states based on *visible* moves ([MAX_VISIBLE_MOVES_PER_PLAYER]).
     * - AI (Player O) tries to maximize its score; Human (Player X) tries to minimize AI's score.
     * - Includes depth in scoring to prefer faster wins or later losses.
     * - Has a depth limit to manage complexity.
     * @param p1Moves Full list of moves for Player 1 (X).
     * @param p2Moves Full list of moves for Player 2 (O, AI).
     * @param depth Current depth in the recursive game tree search.
     * @param isMaximizing True if it's AI's (Player O) turn (maximizing score),
     *                     false if it's Human's (Player X) turn (minimizing score).
     * @return The heuristic score of the current board state from AI's perspective.
     */
    private fun minimax(
        p1Moves: List<String>, // Full move history for P1
        p2Moves: List<String>, // Full move history for P2
        depth: Int,
        isMaximizing: Boolean
    ): Double {
        // Win/loss checks are based on the *visible* portion of moves.
        val p1VisibleMoves = p1Moves.takeLast(MAX_VISIBLE_MOVES_PER_PLAYER)
        val p2VisibleMoves = p2Moves.takeLast(MAX_VISIBLE_MOVES_PER_PLAYER)

        // Terminal conditions:
        if (isAIWinningCombination(p2VisibleMoves)) return 10.0 - depth // AI (O) wins - positive score, prefer shallower depth (faster win).
        if (isAIWinningCombination(p1VisibleMoves)) return -10.0 + depth // Human (X) wins - negative score, prefer deeper depth (slower loss).

        // In Infinite Tic Tac Toe, a "draw" by board full is not standard.
        // A draw might be if no player can win after many moves, or if depth limit reached.
        // For simplicity, if depth limit is reached without a win/loss, it's a neutral outcome.
        val allBoardCells = (1..9).map { "button$it" } // All possible cell IDs.
        val occupiedVisibleCells = (p1VisibleMoves + p2VisibleMoves).toSet()
        if (occupiedVisibleCells.size == 9 && !isAIWinningCombination(p1VisibleMoves) && !isAIWinningCombination(p2VisibleMoves)) {
            return 0.0 // All visible cells are full, but no winner: effectively a local draw.
        }

        // Depth limit to prevent excessive computation, crucial for complex game states.
        if (depth > 4) return 0.0 // Heuristic: deeper searches are costly and may not yield much better results.

        // Determine available moves based on the *current simulated visible state*.
        val availableMoves = allBoardCells.filter { buttonId ->
            !p1VisibleMoves.contains(buttonId) && !p2VisibleMoves.contains(buttonId)
        }

        if (availableMoves.isEmpty()) return 0.0 // No valid moves left from this state, treat as draw.

        if (isMaximizing) { // AI's (Player O) turn - maximize score.
            var bestScore = Double.NEGATIVE_INFINITY
            for (move in availableMoves) {
                // AI simulates making 'move'. Recurse with Player O's full history + new move.
                val score = minimax(p1Moves, p2Moves + move, depth + 1, false)
                bestScore = maxOf(bestScore, score)
            }
            return bestScore
        } else { // Human's (Player X) turn - minimize AI's score.
            var bestScore = Double.POSITIVE_INFINITY
            for (move in availableMoves) {
                // Human simulates making 'move'. Recurse with Player X's full history + new move.
                val score = minimax(p1Moves + move, p2Moves, depth + 1, true)
                bestScore = minOf(bestScore, score)
            }
            return bestScore
        }
    }

    /**
     * Checks if a given list of *visible* moves constitutes a winning combination.
     * Used internally by the minimax algorithm for evaluating simulated board states.
     * @param currentVisibleMoves A list of button IDs representing a player's currently visible moves.
     * @return True if these visible moves form any of the predefined winning combinations, false otherwise.
     */
    private fun isAIWinningCombination(currentVisibleMoves: List<String>): Boolean {
        if (currentVisibleMoves.size < 3) return false // Cannot win with less than 3 visible moves.
        val movesSet = currentVisibleMoves.toSet()
        return WINNING_COMBINATIONS.any { combination ->
            movesSet.containsAll(combination)
        }
    }

    /**
     * Called when the ViewModel is about to be destroyed.
     * Releases SoundManager resources to prevent memory leaks.
     */
    override fun onCleared() {
        super.onCleared()
        soundManager.release() // Important to release SoundManager.
    }
}
