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
import com.a_gud_boy.tictactoe.GameType // Import GameType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray // Import JSONArray

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

    private val gameTimer = GameTimer()

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

        gameTimer.startRoundTimer()

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
            null -> "Round Over"
        }
    }

    fun resetRound() {
        gameTimer.pauseRoundTimer()

        if (_currentRoundMoves.value.isNotEmpty()) {
            val roundNumber = _currentMatchRounds.value.size + 1
            val currentWinnerInfo = _winnerInfo.value
            val roundWinner = currentWinnerInfo?.winner
            val roundWinnerName = determineRoundWinnerNameInfinite(roundWinner)
            val winningComboJson = currentWinnerInfo?.orderedWin?.let { orderedWinList ->
                if (orderedWinList.isNotEmpty()) JSONArray(orderedWinList).toString() else null
            }

            val tempRoundEntity = RoundEntity(
                roundId = 0, ownerMatchId = 0,
                roundNumber = roundNumber,
                winner = roundWinner?.name,
                roundWinnerName = roundWinnerName,
                winningCombinationJson = winningComboJson
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

    fun resetScores() {
        viewModelScope.launch {
            if (_currentRoundMoves.value.isNotEmpty() || _winnerInfo.value != null) {
                val roundNumber = _currentMatchRounds.value.size + 1
                val finalRoundWinnerInfo = _winnerInfo.value
                val finalRoundWinner = finalRoundWinnerInfo?.winner
                val finalRoundWinnerName = determineRoundWinnerNameInfinite(finalRoundWinner)
                val finalRoundWinningComboJson = finalRoundWinnerInfo?.orderedWin?.let { orderedWinList ->
                    if (orderedWinList.isNotEmpty()) JSONArray(orderedWinList).toString() else null
                }

                val finalTempRoundEntity = RoundEntity(
                    roundId = 0,
                    ownerMatchId = 0,
                    roundNumber = roundNumber,
                    winner = finalRoundWinner?.name,
                    roundWinnerName = finalRoundWinnerName,
                    winningCombinationJson = finalRoundWinningComboJson
                )
                if(_currentRoundMoves.value.isNotEmpty() || finalRoundWinner != null) {
                     val lastRoundWithMoves = RoundWithMoves(
                        round = finalTempRoundEntity,
                        moves = _currentRoundMoves.value
                    )
                    _currentMatchRounds.value = _currentMatchRounds.value + lastRoundWithMoves
                }
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
                winner = winner,
                isAgainstAi = _isAIMode.value,
                gameType = GameType.INFINITE,
                timestamp = System.currentTimeMillis(),
                duration = gameTimer.getFinalMatchDuration()
            )

            if (AISettingsManager.saveHistoryEnabled && _currentMatchRounds.value.isNotEmpty()) {
                val matchId = matchDao.insertMatch(matchEntity)
                _currentMatchRounds.value.forEach { roundWithMoves ->
                    val actualRoundEntity = roundWithMoves.round.copy(ownerMatchId = matchId)
                    val actualRoundId = roundDao.insertRound(actualRoundEntity)
                    roundWithMoves.moves.forEach { move ->
                        moveDao.insertMove(move.copy(ownerRoundId = actualRoundId))
                    }
                }
            }

            _player1Wins.value = 0
            _player2Wins.value = 0
            _currentMatchRounds.value = emptyList()
            resetRound()
            gameTimer.reset()
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
                gameTimer.pauseRoundTimer()
                return
            }
            if (p2CurrentVisibleMovesSet.containsAll(combination)) {
                val orderedWin = combination.toList()
                _winnerInfo.value = InfiniteWinnerInfo(Player.O, combination, orderedWin)
                _player2Wins.value += 1
                _isGameConcluded.value = true
                _gameStarted.value = false
                soundManager.playLoseSound(volume)
                gameTimer.pauseRoundTimer()
                return
            }
        }
    }

    fun makeAIMove() {
        if (!_gameStarted.value || _isGameConcluded.value || _player1Turn.value || !_isAIMode.value) return
        viewModelScope.launch {
            delay(100)
            soundManager.playComputerMoveSound(volume)

            val move = withContext(Dispatchers.Default) {
                when (_aiDifficulty.value) {
                    AIDifficulty.EASY -> getRandomMove()
                    AIDifficulty.MEDIUM -> if (Math.random() < 0.6) getBestMove() else getRandomMove()
                    AIDifficulty.HARD -> getBestMove()
                }
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
        // AISettingsManager.isAiModeEnabled = enabled // This line is handled by SettingsPage now
        resetScores() // Reset scores and round
    }

    fun setAIDifficulty(difficulty: AIDifficulty) {
        _aiDifficulty.value = difficulty
        // AISettingsManager.currentDifficulty = difficulty // This line is handled by SettingsPage now
        if (_isAIMode.value) {
            resetScores() // Reset scores and round
        }
    }

    fun refreshSettingsFromManager() {
        val newAiMode = AISettingsManager.isAiModeEnabled
        val newDifficulty = AISettingsManager.currentDifficulty
        var settingsChanged = false

        if (newAiMode != _isAIMode.value) {
            _isAIMode.value = newAiMode
            settingsChanged = true
        }

        if (newAiMode && newDifficulty != _aiDifficulty.value) {
            _aiDifficulty.value = newDifficulty
            settingsChanged = true
        } else if (!newAiMode && _aiDifficulty.value != newDifficulty) {
            _aiDifficulty.value = newDifficulty
        }

        if (settingsChanged) {
            resetScores()
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
    }
}
