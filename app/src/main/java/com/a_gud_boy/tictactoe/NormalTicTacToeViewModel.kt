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
        isGameConcluded,
        isAIMode
    ) { isP1Turn, winnerData, gameConcluded, aiMode ->
        when {
            winnerData != null && winnerData.winner == Player.X -> "You Won!"
            winnerData != null && winnerData.winner == Player.O -> if (aiMode) "AI Won!" else "Player 2 Won"
            gameConcluded && winnerData?.winner == null -> "It's a Draw!"
            isP1Turn -> "Your Turn"
            else -> if (aiMode) "AI's Turn" else "Player 2's Turn"
        }
    }.stateIn(
        viewModelScope,
        kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        "Your Turn"
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

        var moveMade = false
        if (_player1Turn.value) {
            // Player's move
            _player1Moves.value = currentP1Moves + buttonId
            _player1Turn.value = false
            moveMade = true
            checkForWinner()

            // Make AI move if game is in AI mode and game is not concluded
            if (_isAIMode.value && !_isGameConcluded.value) {
                makeAIMove()
            }
        } else if (!_isAIMode.value) {
            // Only allow player 2 moves if not in AI mode
            _player2Moves.value = currentP2Moves + buttonId
            _player1Turn.value = true
            moveMade = true
            checkForWinner()
        } else {
            // This is AI's move
            _player2Moves.value = currentP2Moves + buttonId
            _player1Turn.value = true
            moveMade = true
            checkForWinner()
        }

        if (moveMade) {
            soundManager.playMoveSound()
        }
    }

    private fun checkForWinner() {
        val p1CurrentMovesList = _player1Moves.value
        val p2CurrentMovesList = _player2Moves.value
        val p1MovesSet = p1CurrentMovesList.toSet()
        val p2MovesSet = p2CurrentMovesList.toSet()

        // Early return if neither player has enough moves for a win
        if (p1MovesSet.size < 3 && p2MovesSet.size < 3) return

        // Check only relevant winning combinations based on the last move
        val lastMove =
            if (_player1Turn.value) p2CurrentMovesList.lastOrNull() else p1CurrentMovesList.lastOrNull()
        if (lastMove == null) return

        // Filter winning combinations that contain the last move
        val relevantCombinations = WINNING_COMBINATIONS.filter { it.contains(lastMove) }

        for (combination in relevantCombinations) {
            if (p1MovesSet.containsAll(combination)) {
                // Use geometric order from the combination instead of chronological order
                val orderedWin = combination.toList()
                _winnerInfo.value = WinnerInfo(Player.X, combination, orderedWin)
                _player1Wins.value += 1
                _isGameConcluded.value = true
                _gameStarted.value = false // Stop game, wait for reset
                soundManager.playWinSound() // Player X wins
                return
            }
            if (p2MovesSet.containsAll(combination)) {
                // Use geometric order from the combination instead of chronological order
                val orderedWin = combination.toList()
                _winnerInfo.value = WinnerInfo(Player.O, combination, orderedWin)
                _player2Wins.value += 1
                _isGameConcluded.value = true
                _gameStarted.value = false // Stop game, wait for reset
                soundManager.playLoseSound() // Player O wins
                return
            }
        }

        // Check for draw
        if ((p1MovesSet.size + p2MovesSet.size) == 9 && _winnerInfo.value == null) { // Ensure no winner was set before declaring draw
            _winnerInfo.value = WinnerInfo(null, emptySet(), emptyList()) // Draw
            _isGameConcluded.value = true
            _gameStarted.value = false // Stop game, wait for reset
            soundManager.playDrawSound()
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

    // AI related functions
    private fun makeAIMove() {
        if (!_gameStarted.value || _isGameConcluded.value || _player1Turn.value) return
        soundManager.playComputerMoveSound() // Play sound when AI starts its move

        // Add a small delay to make the AI move feel more natural
        viewModelScope.launch {
            delay(500) // 500ms delay for better UX
            val move = when (_aiDifficulty.value) {
                AIDifficulty.EASY -> getRandomMove()
                AIDifficulty.MEDIUM -> if (Math.random() < 0.5) getBestMove() else getRandomMove()
                AIDifficulty.HARD -> getBestMove()
            }
            move?.let { onButtonClick(it) }
        }
    }

    private fun getRandomMove(): String? {
        val allMoves = (1..9).map { "button$it" }
        val availableMoves = allMoves.filter { buttonId ->
            !_player1Moves.value.contains(buttonId) && !_player2Moves.value.contains(buttonId)
        }
        return availableMoves.randomOrNull()
    }

    private fun getBestMove(): String? {
        val allMoves = (1..9).map { "button$it" }
        val availableMoves = allMoves.filter { buttonId ->
            !_player1Moves.value.contains(buttonId) && !_player2Moves.value.contains(buttonId)
        }

        var bestScore = Double.NEGATIVE_INFINITY
        var bestMove: String? = null

        for (move in availableMoves) {
            val score = minimax(
                p1Moves = _player1Moves.value,
                p2Moves = _player2Moves.value + move,
                depth = 0,
                isMaximizing = false
            )
            if (score > bestScore) {
                bestScore = score
                bestMove = move
            }
        }

        return bestMove
    }

    private fun minimax(
        p1Moves: List<String>,
        p2Moves: List<String>,
        depth: Int,
        isMaximizing: Boolean
    ): Double {
        // Check for terminal states
        when {
            isWinningCombination(p2Moves) -> return 1.0
            isWinningCombination(p1Moves) -> return -1.0
            p1Moves.size + p2Moves.size == 9 -> return 0.0
        }

        val allMoves = (1..9).map { "button$it" }
        val availableMoves = allMoves.filter { buttonId ->
            !p1Moves.contains(buttonId) && !p2Moves.contains(buttonId)
        }

        if (isMaximizing) {
            var bestScore = Double.NEGATIVE_INFINITY
            for (move in availableMoves) {
                val score = minimax(p1Moves, p2Moves + move, depth + 1, false)
                bestScore = maxOf(bestScore, score)
            }
            return bestScore
        } else {
            var bestScore = Double.POSITIVE_INFINITY
            for (move in availableMoves) {
                val score = minimax(p1Moves + move, p2Moves, depth + 1, true)
                bestScore = minOf(bestScore, score)
            }
            return bestScore
        }
    }

    private fun isWinningCombination(moves: List<String>): Boolean {
        val movesSet = moves.toSet()
        return WINNING_COMBINATIONS.any { combination ->
            movesSet.containsAll(combination)
        }
    }

    // Functions to control AI mode
    fun setAIMode(enabled: Boolean) {
        _isAIMode.value = enabled
        resetRound()
    }

    fun setAIDifficulty(difficulty: AIDifficulty) {
        _aiDifficulty.value = difficulty
        if (_isAIMode.value) {
            resetRound()
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
    }
}
