package com.a_gud_boy.tictactoe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.a_gud_boy.tictactoe.Player // Ensure Player is imported
import com.a_gud_boy.tictactoe.MatchWinner // Import MatchWinner

// AIDifficulty is now in its own file: AIDifficulty.kt
// Player enum is now in Player.kt

// Data class to hold winner information for Infinite Mode
data class InfiniteWinnerInfo( // Renamed to avoid conflict if Player.kt also had WinnerInfo
    val winner: Player, // Winner is non-null in Infinite Mode's win condition
    val combination: Set<String>,
    val orderedWin: List<String>
)

class InfiniteTicTacToeViewModel(
    private val soundManager: SoundManager,
    private val matchDao: MatchDao,
    private val roundDao: RoundDao,
    private val moveDao: MoveDao
) : ViewModel() {

    companion object {
        const val MAX_VISIBLE_MOVES_PER_PLAYER = 3
        val WINNING_COMBINATIONS: List<Set<String>> = listOf(
            setOf("button1", "button2", "button3"),
            setOf("button4", "button5", "button6"),
            setOf("button7", "button8", "button9"),
            setOf("button1", "button4", "button7"),
            setOf("button2", "button5", "button8"),
            setOf("button3", "button6", "button9"),
            setOf("button1", "button5", "button9"),
            setOf("button3", "button5", "button7")
        )
    }

    private val volume = 1.0f

    private val _player1Wins = MutableStateFlow(0)
    val player1Wins: StateFlow<Int> = _player1Wins.asStateFlow()

    private val _player2Wins = MutableStateFlow(0)
    val player2Wins: StateFlow<Int> = _player2Wins.asStateFlow()

    private val _player1Moves = MutableStateFlow<List<String>>(emptyList())
    val player1Moves: StateFlow<List<String>> = _player1Moves.asStateFlow()

    private val _player2Moves = MutableStateFlow<List<String>>(emptyList())
    val player2Moves: StateFlow<List<String>> = _player2Moves.asStateFlow()

    private val _winnerInfo = MutableStateFlow<InfiniteWinnerInfo?>(null) // Uses InfiniteWinnerInfo
    val winnerInfo: StateFlow<InfiniteWinnerInfo?> = _winnerInfo.asStateFlow()

    private val _player1Turn = MutableStateFlow(true)
    val player1Turn: StateFlow<Boolean> = _player1Turn.asStateFlow()

    private val _gameStarted = MutableStateFlow(true)
    val gameStarted: StateFlow<Boolean> = _gameStarted.asStateFlow()

    private val _isGameConcluded = MutableStateFlow(false)
    val isGameConcluded: StateFlow<Boolean> = _isGameConcluded.asStateFlow()

    private val _isAIMode = MutableStateFlow(AISettingsManager.isAiModeEnabled)
    val isAIMode: StateFlow<Boolean> = _isAIMode.asStateFlow()

    private val _aiDifficulty = MutableStateFlow(AISettingsManager.currentDifficulty)
    val aiDifficulty: StateFlow<AIDifficulty> = _aiDifficulty.asStateFlow()

    // New StateFlows for Room integration
    private val _currentRoundMoves = MutableStateFlow<List<MoveEntity>>(emptyList())
    private val _currentMatchRounds = MutableStateFlow<List<RoundWithMoves>>(emptyList())


    val turnDenotingText: StateFlow<String> = combine(
        player1Turn, winnerInfo, isAIMode, isGameConcluded
    ) { isP1Turn, winnerData, aiMode, gameConcluded ->
        when {
            winnerData != null && winnerData.winner == Player.X -> if (aiMode) "You Won!" else "Player 1 Won!"
            winnerData != null && winnerData.winner == Player.O -> if (aiMode) "AI Won!" else "Player 2 Won!"
            isP1Turn -> if (aiMode) "Your Turn" else "Player 1's Turn"
            else -> if (aiMode) "AI's Turn" else "Player 2's Turn"
        }
    }.stateIn(
        viewModelScope,
        kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        if (_isAIMode.value) "Your Turn" else "Player 1's Turn"
    )

    val resetButtonText: StateFlow<String> = combine(isGameConcluded) { (gameConcluded) ->
        if (gameConcluded) "New Round" else "Reset Round"
    }.stateIn(
        viewModelScope,
        kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        "Reset Round"
    )

    fun onButtonClick(buttonId: String) {
        if (!_gameStarted.value || _isGameConcluded.value) return

        val currentP1FullMoves = _player1Moves.value
        val currentP2FullMoves = _player2Moves.value

        val p1VisibleMoves = currentP1FullMoves.takeLast(MAX_VISIBLE_MOVES_PER_PLAYER)
        val p2VisibleMoves = currentP2FullMoves.takeLast(MAX_VISIBLE_MOVES_PER_PLAYER)

        if (p1VisibleMoves.contains(buttonId) || p2VisibleMoves.contains(buttonId)) {
            return
        }

        val currentPlayer = if (_player1Turn.value) Player.X else Player.O
        val newMove = MoveEntity(
            ownerRoundId = 0, // Placeholder
            player = currentPlayer.name,
            cellId = buttonId
        )
        _currentRoundMoves.value = _currentRoundMoves.value + newMove

        if (_player1Turn.value) {
            _player1Moves.value = currentP1FullMoves + buttonId
            _player1Turn.value = false
            soundManager.playMoveSound(volume)
            checkForWinner()
            if (_isAIMode.value && !_isGameConcluded.value && !_player1Turn.value) {
                makeAIMove()
            }
        } else { // Player 2 (Human or AI)
            _player2Moves.value = currentP2FullMoves + buttonId
            _player1Turn.value = true
            if (!_isAIMode.value) {
                soundManager.playMoveSound(volume)
            }
            checkForWinner()
        }
    }

    private fun determineRoundWinnerNameInfinite(winner: Player?): String {
        return when (winner) {
            Player.X -> if (isAIMode.value) "You Won" else "Player 1 Won"
            Player.O -> if (isAIMode.value) "AI Won" else "Player 2 Won"
            // Infinite mode typically doesn't have a "draw" state in the same way as normal.
            // A round ends when a player achieves a line with their visible moves.
            // If _winnerInfo is null (which it would be if no line is formed),
            // but resetRound is called (e.g. by resetScores), then "Round Over" or similar.
            null -> "Round Over"
        }
    }

    fun resetRound() { // End of a round in Infinite mode
        if (_currentRoundMoves.value.isNotEmpty() || _winnerInfo.value != null) {
            val roundNumber = _currentMatchRounds.value.size + 1
            val roundWinner = _winnerInfo.value?.winner // winner is non-null in InfiniteWinnerInfo
            val roundWinnerName = determineRoundWinnerNameInfinite(roundWinner)

            val tempRoundEntity = RoundEntity(
                roundId = 0, ownerMatchId = 0,
                roundNumber = roundNumber,
                winner = roundWinner?.name,
                roundWinnerName = roundWinnerName
            )
            val completedRoundWithMoves = RoundWithMoves(
                round = tempRoundEntity,
                moves = _currentRoundMoves.value
            )
            _currentMatchRounds.value = _currentMatchRounds.value + completedRoundWithMoves
        }

        _player1Moves.value = emptyList()
        _player2Moves.value = emptyList()
        _winnerInfo.value = null
        _currentRoundMoves.value = emptyList()
        _player1Turn.value = true
        _isGameConcluded.value = false
        _gameStarted.value = true
    }

    fun resetScores() { // End of a match in Infinite mode
        viewModelScope.launch {
            if (_isGameConcluded.value && _currentRoundMoves.value.isNotEmpty()) {
                val roundNumber = _currentMatchRounds.value.size + 1
                val roundWinner = _winnerInfo.value?.winner
                val roundWinnerName = determineRoundWinnerNameInfinite(roundWinner)
                val tempRoundEntity = RoundEntity(
                    roundId = 0,
                    ownerMatchId = 0,
                    roundNumber = roundNumber,
                    winner = roundWinner?.name,
                    roundWinnerName = roundWinnerName
                )
                val lastRoundWithMoves =
                    RoundWithMoves(round = tempRoundEntity, moves = _currentRoundMoves.value)
                _currentMatchRounds.value = _currentMatchRounds.value + lastRoundWithMoves
            }

            val p1FinalScore = _player1Wins.value
            val p2FinalScore = _player2Wins.value
            val matchWinnerName = when {
                p1FinalScore > p2FinalScore -> if (isAIMode.value) "You Won $p1FinalScore-$p2FinalScore" else "Player 1 Won $p1FinalScore-$p2FinalScore"
                p2FinalScore > p1FinalScore -> if (isAIMode.value) "AI Won $p2FinalScore-$p1FinalScore" else "Player 2 Won $p2FinalScore-$p1FinalScore"
                else -> "Match Tied $p1FinalScore-$p2FinalScore"
            }

            val winner = when {
                p1FinalScore > p2FinalScore -> MatchWinner.PLAYER1
                p2FinalScore > p1FinalScore -> MatchWinner.PLAYER2
                else -> MatchWinner.DRAW
            }

            val currentMatchNumber = matchDao.getMatchesCount() + 1
            val matchEntity = MatchEntity(
                matchNumber = currentMatchNumber,
                player1Score = p1FinalScore,
                player2Score = p2FinalScore,
                matchWinnerName = matchWinnerName,
                winner = winner, // Pass the determined winner
                isAgainstAi = _isAIMode.value, // <<< THIS LINE IS ADDED/MODIFIED
                timestamp = System.currentTimeMillis()
            )
            val matchId = matchDao.insertMatch(matchEntity)

            _currentMatchRounds.value.forEach { roundWithMoves ->
                val actualRoundEntity = roundWithMoves.round.copy(ownerMatchId = matchId)
                val roundId = roundDao.insertRound(actualRoundEntity)
                roundWithMoves.moves.forEach { move ->
                    moveDao.insertMove(move.copy(ownerRoundId = roundId))
                }
            }

            _player1Wins.value = 0
            _player2Wins.value = 0
            _currentMatchRounds.value = emptyList()
            _currentRoundMoves.value = emptyList()

            resetRound()
        }
    }

    private fun checkForWinner() {
        val p1VisibleMoves = _player1Moves.value.takeLast(MAX_VISIBLE_MOVES_PER_PLAYER)
        val p2VisibleMoves = _player2Moves.value.takeLast(MAX_VISIBLE_MOVES_PER_PLAYER)
        val p1CurrentVisibleMovesSet = p1VisibleMoves.toSet()
        val p2CurrentVisibleMovesSet = p2VisibleMoves.toSet()

        if (p1VisibleMoves.size < 3 && p2VisibleMoves.size < 3) return

        val lastPlayerToMoveWasP1 = !_player1Turn.value
        val lastMove =
            if (lastPlayerToMoveWasP1) p1VisibleMoves.lastOrNull() else p2VisibleMoves.lastOrNull()
        if (lastMove == null) return

        val relevantCombinations = WINNING_COMBINATIONS.filter { it.contains(lastMove) }

        for (combination in relevantCombinations) {
            if (p1CurrentVisibleMovesSet.containsAll(combination)) {
                val orderedWin = combination.toList()
                _winnerInfo.value = InfiniteWinnerInfo(Player.X, combination, orderedWin)
                _player1Wins.value += 1
                _isGameConcluded.value = true
                _gameStarted.value = false
                soundManager.playWinSound(volume)
                return
            }
            if (p2CurrentVisibleMovesSet.containsAll(combination)) {
                val orderedWin = combination.toList()
                _winnerInfo.value = InfiniteWinnerInfo(Player.O, combination, orderedWin)
                _player2Wins.value += 1
                _isGameConcluded.value = true
                _gameStarted.value = false
                soundManager.playLoseSound(volume)
                return
            }
        }
    }

    fun makeAIMove() {
        if (!_gameStarted.value || _isGameConcluded.value || _player1Turn.value || !_isAIMode.value) return
        viewModelScope.launch {
            delay(100)
            soundManager.playComputerMoveSound(volume)
            val move = when (_aiDifficulty.value) {
                AIDifficulty.EASY -> getRandomMove()
                AIDifficulty.MEDIUM -> if (Math.random() < 0.6) getBestMove() else getRandomMove()
                AIDifficulty.HARD -> getBestMove()
            }
            move?.let { onButtonClick(it) }
        }
    }

    private fun getRandomMove(): String? {
        val allBoardCells = (1..9).map { "button$it" }
        val p1Visible = _player1Moves.value.takeLast(MAX_VISIBLE_MOVES_PER_PLAYER)
        val p2Visible = _player2Moves.value.takeLast(MAX_VISIBLE_MOVES_PER_PLAYER)
        val availableMoves = allBoardCells.filter { buttonId ->
            !p1Visible.contains(buttonId) && !p2Visible.contains(buttonId)
        }
        return availableMoves.randomOrNull()
    }

    private fun getBestMove(): String? {
        val allBoardCells = (1..9).map { "button$it" }
        val p1Visible = _player1Moves.value.takeLast(MAX_VISIBLE_MOVES_PER_PLAYER)
        val p2Visible = _player2Moves.value.takeLast(MAX_VISIBLE_MOVES_PER_PLAYER)
        val availableMoves = allBoardCells.filter { buttonId ->
            !p1Visible.contains(buttonId) && !p2Visible.contains(buttonId)
        }
        if (availableMoves.isEmpty()) return null

        var bestScore = Double.NEGATIVE_INFINITY
        var bestMove: String? = null
        for (move in availableMoves) {
            val score = minimax(_player1Moves.value, _player2Moves.value + move, 0, false)
            if (score > bestScore) {
                bestScore = score
                bestMove = move
            }
        }
        return bestMove ?: availableMoves.randomOrNull()
    }

    private fun minimax(
        p1Moves: List<String>,
        p2Moves: List<String>,
        depth: Int,
        isMaximizing: Boolean
    ): Double {
        val p1VisibleMoves = p1Moves.takeLast(MAX_VISIBLE_MOVES_PER_PLAYER)
        val p2VisibleMoves = p2Moves.takeLast(MAX_VISIBLE_MOVES_PER_PLAYER)

        if (isAIWinningCombination(p2VisibleMoves)) return 10.0 - depth
        if (isAIWinningCombination(p1VisibleMoves)) return -10.0 + depth

        val allBoardCells = (1..9).map { "button$it" }
        val occupiedVisibleCells = (p1VisibleMoves + p2VisibleMoves).toSet()
        if (occupiedVisibleCells.size == 9 && !isAIWinningCombination(p1VisibleMoves) && !isAIWinningCombination(
                p2VisibleMoves
            )
        ) {
            return 0.0
        }
        if (depth > 4) return 0.0

        val availableMoves = allBoardCells.filter { buttonId ->
            !p1VisibleMoves.contains(buttonId) && !p2VisibleMoves.contains(buttonId)
        }
        if (availableMoves.isEmpty()) return 0.0

        if (isMaximizing) {
            var bestScore = Double.NEGATIVE_INFINITY
            for (moveString in availableMoves) {
                val score = minimax(p1Moves, p2Moves + moveString, depth + 1, false)
                bestScore = maxOf(bestScore, score)
            }
            return bestScore
        } else {
            var bestScore = Double.POSITIVE_INFINITY
            for (moveString in availableMoves) {
                val score = minimax(p1Moves + moveString, p2Moves, depth + 1, true)
                bestScore = minOf(bestScore, score)
            }
            return bestScore
        }
    }

    private fun isAIWinningCombination(currentVisibleMoves: List<String>): Boolean {
        if (currentVisibleMoves.size < 3) return false
        val movesSet = currentVisibleMoves.toSet()
        return WINNING_COMBINATIONS.any { combination -> movesSet.containsAll(combination) }
    }

    fun setAIMode(enabled: Boolean) {
        _isAIMode.value = enabled
        AISettingsManager.isAiModeEnabled = enabled
        resetScores() // Reset scores and round
    }

    fun setAIDifficulty(difficulty: AIDifficulty) {
        _aiDifficulty.value = difficulty
        AISettingsManager.currentDifficulty = difficulty
        if (_isAIMode.value) {
            resetScores() // Reset scores and round
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
    }
}
