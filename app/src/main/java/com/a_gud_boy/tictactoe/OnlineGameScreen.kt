package com.a_gud_boy.tictactoe

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
// FirebaseFirestore import is not directly used, can be removed if not needed by other parts of the file.
// import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class OnlineGameState(
    val gameId: String = "",
    val player1Id: String? = null,
    val player1DisplayName: String? = "Player 1",
    val player2Id: String? = null,
    val player2DisplayName: String? = "Player 2",
    val boardState: List<String> = List(9) { "" },
    val currentPlayerId: String? = null,
    val status: String = "loading",
    val winnerId: String? = null,
    val isUserTurn: Boolean = false,
    val turnMessage: String = "Loading game...",
    val gameType: String = GameType.NORMAL.name,
    val player1Moves: List<Int> = emptyList(),
    val player2Moves: List<Int> = emptyList(),
    val winningLine: List<Int>? = null // Added for winning line display
)

class OnlineGameViewModel(
    private val gameId: String,
    private val soundManager: SoundManager
) : ViewModel() {
    private val db = Firebase.firestore
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val _gameState = MutableStateFlow(OnlineGameState(gameId = gameId))
    val gameState: StateFlow<OnlineGameState> = _gameState
    private var gameListenerRegistration: ListenerRegistration? = null

    companion object {
        const val MAX_VISIBLE_MOVES_PER_PLAYER = 3
    }

    init {
        listenToGameUpdates()
    }

    private fun listenToGameUpdates() {
        if (currentUser == null) {
            _gameState.value = _gameState.value.copy(status = "error", turnMessage = "Authentication error.")
            return
        }
        gameListenerRegistration?.remove()
        gameListenerRegistration = db.collection("gameSessions").document(gameId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _gameState.value = _gameState.value.copy(status = "error", turnMessage = "Error loading game.")
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val player1Id = snapshot.getString("player1Id")
                    val player2Id = snapshot.getString("player2Id") // Corrected: Was player1Id again
                    val board = snapshot.get("boardState") as? List<String> ?: List(9) { "" }
                    val currentPlayerId = snapshot.getString("currentPlayerId")
                    val status = snapshot.getString("status") ?: "unknown"
                    val winnerId = snapshot.getString("winnerId")
                    val player1Name = snapshot.getString("player1DisplayName") ?: "Player 1"
                    val player2Name = snapshot.getString("player2DisplayName") ?: "Player 2" // Corrected: Was player1DisplayName
                    val isMyTurn = currentPlayerId == currentUser.uid
                    val turnMsg = when {
                        status.contains("_wins") || status == "draw" -> when {
                            winnerId == currentUser.uid -> "You won!"
                            winnerId != null -> "Opponent won!"
                            else -> "It's a draw!"
                        }
                        status == "active" -> if (isMyTurn) "Your turn" else "Waiting for opponent..."
                        status == "waiting_for_player" -> "Waiting for opponent to join..."
                        else -> "Loading game..."
                    }
                    val gameTypeString = snapshot.getString("gameType") ?: GameType.NORMAL.name
                    val p1MovesRaw = snapshot.get("player1Moves") as? List<*> ?: emptyList<Long>()
                    val p2MovesRaw = snapshot.get("player2Moves") as? List<*> ?: emptyList<Long>()
                    val player1Moves = p1MovesRaw.filterIsInstance<Long>().map { it.toInt() }
                    val player2Moves = p2MovesRaw.filterIsInstance<Long>().map { it.toInt() }

                    val currentWinningLine = if (status.contains("_wins") && winnerId != null) {
                        determineWinningLine(board, winnerId, player1Id)
                    } else {
                        null
                    }

                    _gameState.value = OnlineGameState(
                        gameId = gameId, player1Id = player1Id, player1DisplayName = player1Name,
                        player2Id = player2Id, player2DisplayName = player2Name, boardState = board,
                        currentPlayerId = currentPlayerId, status = status, winnerId = winnerId,
                        isUserTurn = isMyTurn && status == "active", turnMessage = turnMsg,
                        gameType = gameTypeString, player1Moves = player1Moves, player2Moves = player2Moves,
                        winningLine = currentWinningLine
                    )
                } else {
                    _gameState.value = _gameState.value.copy(status = "error", turnMessage = "Game not found.")
                }
            }
    }

    fun makeMove(index: Int) {
        if (currentUser == null || !_gameState.value.isUserTurn || _gameState.value.boardState[index].isNotEmpty()) {
            return
        }
        soundManager.playMoveSound()

        val currentGameState = _gameState.value
        val newBoardState = currentGameState.boardState.toMutableList()
        val currentPlayerMark = if (currentGameState.player1Id == currentUser.uid) "X" else "O"
        val isPlayer1MakingMove = currentGameState.player1Id == currentUser.uid

        newBoardState[index] = currentPlayerMark

        val currentP1Moves = currentGameState.player1Moves.toMutableList()
        val currentP2Moves = currentGameState.player2Moves.toMutableList()

        if (currentGameState.gameType == GameType.INFINITE.name) {
            if (isPlayer1MakingMove) {
                currentP1Moves.add(index)
                if (currentP1Moves.size > MAX_VISIBLE_MOVES_PER_PLAYER) {
                    newBoardState[currentP1Moves.removeAt(0)] = ""
                }
            } else {
                currentP2Moves.add(index)
                if (currentP2Moves.size > MAX_VISIBLE_MOVES_PER_PLAYER) {
                    newBoardState[currentP2Moves.removeAt(0)] = ""
                }
            }
        }

        val (newStatus, newWinnerId, _) = checkWinCondition( // detectedWinningLine is not used here
            newBoardState,
            currentUser.uid,
            isPlayer1MakingMove
        )

        if (newStatus.contains("_wins")) {
            soundManager.playWinSound()
        } else if (newStatus == "draw") {
            soundManager.playDrawSound()
        }

        val nextPlayerId = if (newStatus == "active") {
            if (isPlayer1MakingMove) currentGameState.player2Id else currentGameState.player1Id
        } else null

        val gameUpdates = hashMapOf<String, Any?>(
            "boardState" to newBoardState, "currentPlayerId" to nextPlayerId,
            "status" to newStatus, "lastMoveAt" to FieldValue.serverTimestamp(),
            "player1Moves" to currentP1Moves, "player2Moves" to currentP2Moves
        )
        if (newWinnerId != null) gameUpdates["winnerId"] = newWinnerId
        // winningLine is NOT sent to Firestore, it's derived locally by clients

        db.collection("gameSessions").document(gameId).update(gameUpdates)
    }

    private fun checkWinCondition(board: List<String>, currentPlayerIdMakingMove: String?, isPlayer1: Boolean): Triple<String, String?, List<Int>?> {
        val winPatterns = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // Rows
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // Columns
            listOf(0, 4, 8), listOf(2, 4, 6)  // Diagonals
        )
        val currentPlayerMark = if (isPlayer1) "X" else "O"
        for (pattern in winPatterns) {
            if (pattern.all { board[it] == currentPlayerMark }) {
                val winnerStatus = if (isPlayer1) "player1_wins" else "player2_wins"
                return Triple(winnerStatus, currentPlayerIdMakingMove, pattern)
            }
        }
        if (board.all { it.isNotEmpty() }) {
            return Triple("draw", null, null)
        }
        return Triple("active", null, null)
    }

    private fun determineWinningLine(board: List<String>, actualWinnerId: String, p1Id: String?): List<Int>? {
        if (p1Id == null) return null
        val winPatterns = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
            listOf(0, 4, 8), listOf(2, 4, 6)
        )
        val winnerMark = if (actualWinnerId == p1Id) "X" else "O"
        for (pattern in winPatterns) {
            if (pattern.all { board[it] == winnerMark }) {
                return pattern
            }
        }
        return null
    }

    override fun onCleared() {
        super.onCleared()
        gameListenerRegistration?.remove()
        soundManager.release()
    }
}

// OnlineGameViewModelFactory is removed as TicTacToeViewModelFactory handles it.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineGameScreen(
    gameId: String,
    navController: NavHostController,
) {
    val factory = LocalViewModelFactory.current
    val viewModel: OnlineGameViewModel = viewModel(factory = factory, key = gameId)
    val gameState by viewModel.gameState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Tic Tac Toe - Online") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().background(colorResource(R.color.background))
                .padding(paddingValues).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (gameState.status == "loading") {
                CircularProgressIndicator()
                Text("Loading game...", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
            } else if (gameState.status == "error") {
                Text("Error: ${gameState.turnMessage}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                Button(onClick = { navController.popBackStack() }, modifier = Modifier.padding(top = 16.dp)) {
                    Text("Back to Lobby")
                }
            } else {
                // Player display names and turn messages are handled by sub-pages
                if (gameState.gameType == GameType.NORMAL.name) {
                    OnlineNormalTicTacToePage(
                        gameState = gameState,
                        onCellClick = { index -> viewModel.makeMove(index) },
                        onNavigateBackToLobby = { navController.popBackStack() },
                        winningLine = gameState.winningLine // Now correctly passed
                    )
                } else if (gameState.gameType == GameType.INFINITE.name) {
                    OnlineInfiniteTicTacToePage(
                        gameState = gameState,
                        onCellClick = { index -> viewModel.makeMove(index) },
                        onNavigateBackToLobby = { navController.popBackStack() },
                        maxVisibleMovesPerPlayer = OnlineGameViewModel.MAX_VISIBLE_MOVES_PER_PLAYER,
                        winningLine = gameState.winningLine // Pass winningLine
                    )
                } else {
                    Text("Unknown game type: ${gameState.gameType}")
                }
            }
        }
    }
}
