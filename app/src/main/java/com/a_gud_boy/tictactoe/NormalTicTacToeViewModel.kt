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
import org.json.JSONArray // Import JSONArray

// Player enum is now in its own file: Player.kt
// AIDifficulty is now in its own file: AIDifficulty.kt

// Data class to hold winner information
data class WinnerInfo( // This is the existing WinnerInfo, ensure it's compatible.
    val winner: Player?,
    val combination: Set<String>,
    val orderedWinningMoves: List<String>
)

class NormalTicTacToeViewModel(
    private val soundManager: SoundManager,
    private val matchDao: MatchDao,
    private val roundDao: RoundDao,
    private val moveDao: MoveDao
) : ViewModel() {

    private val gameTimer = GameTimer()

    companion object {
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

    private val _winnerInfo = MutableStateFlow<WinnerInfo?>(null)
    val winnerInfo: StateFlow<WinnerInfo?> = _winnerInfo.asStateFlow()

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
        player1Turn, winnerInfo, isGameConcluded, isAIMode
    ) { isP1Turn, winnerData, gameConcluded, aiMode ->
        when {
            winnerData != null && winnerData.winner == Player.X -> if (aiMode) "You Won!" else "Player 1 Won!"
            winnerData != null && winnerData.winner == Player.O -> if (aiMode) "AI Won!" else "Player 2 Won!"
            gameConcluded && winnerData?.winner == null -> "It's a Draw!"
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

        if (currentP1FullMoves.contains(buttonId) || currentP2FullMoves.contains(buttonId)) {
            return
        }

        val currentPlayer = if (_player1Turn.value) Player.X else Player.O
        val newMove = MoveEntity(
            ownerRoundId = 0, // Will be set when the round is saved
            player = currentPlayer.name,
            cellId = buttonId
        )
        _currentRoundMoves.value = _currentRoundMoves.value + newMove

        if (_player1Turn.value) {
            _player1Moves.value = currentP1FullMoves + buttonId
            _player1Turn.value = false
            soundManager.playMoveSound(volume)
            checkForWinner()

            if (_isAIMode.value && !_isGameConcluded.value) {
                makeAIMove()
            }
        } else if (!_isAIMode.value) { // Human Player 2
            _player2Moves.value = currentP2FullMoves + buttonId
            _player1Turn.value = true
            soundManager.playMoveSound(volume)
            checkForWinner()
        } else { // AI's move (already determined and passed as buttonId)
            _player2Moves.value = currentP2FullMoves + buttonId
            _player1Turn.value = true
            // Sound for AI move is in makeAIMove
            checkForWinner()
        }
    }

    private fun determineRoundWinnerName(winner: Player?): String {
        return when (winner) {
            Player.X -> if (isAIMode.value) "You Won" else "Player 1 Won"
            Player.O -> if (isAIMode.value) "AI Won" else "Player 2 Won"
            null -> "Draw"
        }
    }

    fun resetRound() { // End of a round
        gameTimer.pauseRoundTimer()

        if (_currentRoundMoves.value.isNotEmpty()) {
            val roundNumber = _currentMatchRounds.value.size + 1
            val currentWinnerInfo = _winnerInfo.value // Capture current winner info
            val roundWinner = currentWinnerInfo?.winner
            val roundWinnerName = determineRoundWinnerName(roundWinner)

            // Convert winning combination to JSON string
            // For normal mode, winnerInfo.orderedWinningMoves holds the combination
            val winningComboJson = currentWinnerInfo?.orderedWinningMoves?.let { orderedMovesList ->
                if (orderedMovesList.isNotEmpty()) JSONArray(orderedMovesList).toString() else null
            }

            val tempRoundEntity = RoundEntity(
                roundId = 0, ownerMatchId = 0,
                roundNumber = roundNumber,
                winner = roundWinner?.name,
                roundWinnerName = roundWinnerName,
                winningCombinationJson = winningComboJson // Set the new field
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

    fun resetScores() { // End of a match
        viewModelScope.launch {
            // gameTimer.getFinalMatchDuration() will handle pausing if active.
            // Handle the currently ongoing round's data
            if (_currentRoundMoves.value.isNotEmpty() || _winnerInfo.value != null) { // Ensure even a decided but not fully saved round is processed
                val roundNumber = _currentMatchRounds.value.size + 1
                val finalRoundWinnerInfo = _winnerInfo.value
                val finalRoundWinner = finalRoundWinnerInfo?.winner
                val finalRoundWinnerName = determineRoundWinnerName(finalRoundWinner)
                val finalRoundWinningComboJson = finalRoundWinnerInfo?.orderedWinningMoves?.let { orderedMovesList ->
                    if (orderedMovesList.isNotEmpty()) JSONArray(orderedMovesList).toString() else null
                }

                val finalTempRoundEntity = RoundEntity(
                    roundId = 0,
                    ownerMatchId = 0,
                    roundNumber = roundNumber,
                    winner = finalRoundWinner?.name,
                    roundWinnerName = finalRoundWinnerName,
                    winningCombinationJson = finalRoundWinningComboJson
                )
                // Only add if there were moves or a winner, to avoid empty rounds on immediate resetScores
                if (_currentRoundMoves.value.isNotEmpty() || finalRoundWinner != null) {
                    val lastRoundWithMoves =
                        RoundWithMoves(round = finalTempRoundEntity, moves = _currentRoundMoves.value)
                    _currentMatchRounds.value = _currentMatchRounds.value + lastRoundWithMoves
                }
            }


            val p1FinalScore = _player1Wins.value
            val p2FinalScore = _player2Wins.value
            val matchWinnerName = when {
                p1FinalScore > p2FinalScore -> if (isAIMode.value) "You Won $p1FinalScore-$p2FinalScore" else "Player 1 Won $p1FinalScore-$p2FinalScore"
                p2FinalScore > p1FinalScore -> if (isAIMode.value) "AI Won $p2FinalScore-$p1FinalScore" else "Player 2 Won $p2FinalScore-$p1FinalScore"
                else -> "Match Drawn $p1FinalScore-$p2FinalScore"
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
                isAgainstAi = _isAIMode.value,
                gameType = GameType.NORMAL, // Use GameType.NORMAL
                timestamp = System.currentTimeMillis(),
                duration = gameTimer.getFinalMatchDuration()
            )

            if (AISettingsManager.saveHistoryEnabled && _currentMatchRounds.value.isNotEmpty()) { // Only save if there are rounds
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
        val p1CurrentMovesList = _player1Moves.value
        val p2CurrentMovesList = _player2Moves.value
        val p1MovesSet = p1CurrentMovesList.toSet()
        val p2MovesSet = p2CurrentMovesList.toSet()

        if (p1MovesSet.size < 3 && p2MovesSet.size < 3) return

        val lastMove =
            if (_player1Turn.value) p2CurrentMovesList.lastOrNull() else p1CurrentMovesList.lastOrNull()
        if (lastMove == null) return

        val relevantCombinations = WINNING_COMBINATIONS.filter { it.contains(lastMove) }

        for (combination in relevantCombinations) {
            if (p1MovesSet.containsAll(combination)) {
                val orderedWin = combination.toList() // Assuming WINNING_COMBINATIONS stores them in order or order doesn't matter for this use.
                                                 // If specific order based on moves is needed, this needs adjustment.
                _winnerInfo.value = WinnerInfo(Player.X, combination, orderedWin)
                _player1Wins.value += 1
                _isGameConcluded.value = true
                _gameStarted.value = false
                soundManager.playWinSound(volume)
                gameTimer.pauseRoundTimer()
                return
            }
            if (p2MovesSet.containsAll(combination)) {
                val orderedWin = combination.toList()
                _winnerInfo.value = WinnerInfo(Player.O, combination, orderedWin)
                _player2Wins.value += 1
                _isGameConcluded.value = true
                _gameStarted.value = false
                soundManager.playLoseSound(volume)
                gameTimer.pauseRoundTimer()
                return
            }
        }

        if ((p1MovesSet.size + p2MovesSet.size) == 9 && _winnerInfo.value == null) {
            _winnerInfo.value = WinnerInfo(null, emptySet(), emptyList())
            _isGameConcluded.value = true
            _gameStarted.value = false
            soundManager.playDrawSound(volume)
            gameTimer.pauseRoundTimer()
        }
    }

    private fun makeAIMove() {
        if (!_gameStarted.value || _isGameConcluded.value || _player1Turn.value) return
        viewModelScope.launch {
            delay(100)
            soundManager.playComputerMoveSound(volume)
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
            val score = minimax(_player1Moves.value, _player2Moves.value + move, 0, false)
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
        if (isWinningCombination(p2Moves)) return 1.0
        if (isWinningCombination(p1Moves)) return -1.0
        if (p1Moves.size + p2Moves.size == 9) return 0.0

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
        return WINNING_COMBINATIONS.any { combination -> movesSet.containsAll(combination) }
    }

    fun setAIMode(enabled: Boolean) {
        _isAIMode.value = enabled
        // AISettingsManager.isAiModeEnabled = enabled // This line is handled by SettingsPage now
        resetScores() // Reset scores and round when AI mode changes
    }

    fun setAIDifficulty(difficulty: AIDifficulty) {
        _aiDifficulty.value = difficulty
        // AISettingsManager.currentDifficulty = difficulty // This line is handled by SettingsPage now
        if (_isAIMode.value) {
            resetScores() // Reset scores and round if AI is active and difficulty changes
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

        // Only update difficulty if AI mode is enabled, and if difficulty actually changed
        if (newAiMode && newDifficulty != _aiDifficulty.value) {
            _aiDifficulty.value = newDifficulty
            settingsChanged = true
        } else if (!newAiMode && _aiDifficulty.value != newDifficulty) {
            // If AI mode was just turned off, also update the stored difficulty
            // so it's correct for next time AI mode is enabled.
            // No game reset needed here if AI mode is off.
            _aiDifficulty.value = newDifficulty
        }

        if (settingsChanged) {
            resetScores() // Reset game if AI mode was toggled, or if difficulty changed while AI mode is on.
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
    }
}
