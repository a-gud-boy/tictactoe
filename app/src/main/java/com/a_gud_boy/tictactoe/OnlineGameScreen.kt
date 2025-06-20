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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Data class to represent the state of an online game
data class OnlineGameState(
    val gameId: String = "",
    val player1Id: String? = null,
    val player1DisplayName: String? = "Player 1",
    val player2Id: String? = null,
    val player2DisplayName: String? = "Player 2",
    val boardState: List<String> = List(9) { "" }, // "X", "O", or ""
    val currentPlayerId: String? = null,
    val status: String = "loading", // e.g., "loading", "waiting_for_player", "active", "player1_wins", "player2_wins", "draw"
    val winnerId: String? = null,
    val isUserTurn: Boolean = false, // Helper to quickly check if it's the current user's turn
    val turnMessage: String = "Loading game..." // Message like "Your turn" or "Waiting for opponent"
)

// ViewModel for the Online Game Screen
class OnlineGameViewModel(private val gameId: String) : ViewModel() {
    private val db = Firebase.firestore
    private val currentUser = FirebaseAuth.getInstance().currentUser

    private val _gameState = MutableStateFlow(OnlineGameState(gameId = gameId))
    val gameState: StateFlow<OnlineGameState> = _gameState

    private var gameListenerRegistration: ListenerRegistration? = null

    init {
        listenToGameUpdates()
    }

    private fun listenToGameUpdates() {
        if (currentUser == null) {
            _gameState.value = _gameState.value.copy(status = "error", turnMessage = "Authentication error.")
            return
        }
        gameListenerRegistration?.remove() // Remove previous listener if any
        gameListenerRegistration = db.collection("gameSessions").document(gameId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("OnlineGameViewModel", "Listen failed.", e)
                    _gameState.value = _gameState.value.copy(status = "error", turnMessage = "Error loading game.")
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val player1Id = snapshot.getString("player1Id")
                    val player2Id = snapshot.getString("player2Id")
                    val board = snapshot.get("boardState") as? List<String> ?: List(9) { "" }
                    val currentPlayerId = snapshot.getString("currentPlayerId")
                    val status = snapshot.getString("status") ?: "unknown"
                    val winnerId = snapshot.getString("winnerId")
                    val player1Name = snapshot.getString("player1DisplayName") ?: "Player 1"
                    val player2Name = snapshot.getString("player2DisplayName") ?: "Player 2"

                    val isMyTurn = currentPlayerId == currentUser.uid
                    val turnMsg = when {
                        status.contains("_wins") || status == "draw" -> {
                            when {
                                winnerId == currentUser.uid -> "You won!"
                                winnerId != null -> "Opponent won!"
                                else -> "It's a draw!"
                            }
                        }
                        status == "active" -> if (isMyTurn) "Your turn" else "Waiting for opponent..."
                        status == "waiting_for_player" -> "Waiting for opponent to join..."
                        else -> "Loading game..."
                    }

                    _gameState.value = OnlineGameState(
                        gameId = gameId,
                        player1Id = player1Id,
                        player1DisplayName = player1Name,
                        player2Id = player2Id,
                        player2DisplayName = player2Name,
                        boardState = board,
                        currentPlayerId = currentPlayerId,
                        status = status,
                        winnerId = winnerId,
                        isUserTurn = isMyTurn && status == "active",
                        turnMessage = turnMsg
                    )
                } else {
                    Log.w("OnlineGameViewModel", "Game document does not exist.")
                    _gameState.value = _gameState.value.copy(status = "error", turnMessage = "Game not found.")
                }
            }
    }

    fun makeMove(index: Int) {
        if (currentUser == null || !_gameState.value.isUserTurn || _gameState.value.boardState[index].isNotEmpty()) {
            Log.d("OnlineGameViewModel", "Cannot make move: Not user's turn, cell not empty, or user not logged in.")
            return // Not current user's turn, or cell is not empty
        }

        val newBoardState = _gameState.value.boardState.toMutableList()
        val currentPlayerMark = if (_gameState.value.player1Id == currentUser.uid) "X" else "O"
        newBoardState[index] = currentPlayerMark

        // Determine winner and next player
        val (newStatus, newWinnerId) = checkWinCondition(newBoardState, currentUser.uid, _gameState.value.player1Id == currentUser.uid)
        val nextPlayerId = if (newStatus == "active") {
            if (_gameState.value.player1Id == currentUser.uid) _gameState.value.player2Id else _gameState.value.player1Id
        } else {
            null // Game ended or draw
        }


        val gameUpdates = hashMapOf<String, Any?>(
            "boardState" to newBoardState,
            "currentPlayerId" to nextPlayerId,
            "status" to newStatus,
            "lastMoveAt" to FieldValue.serverTimestamp()
        )
        if (newWinnerId != null) {
            gameUpdates["winnerId"] = newWinnerId
        }

        db.collection("gameSessions").document(gameId)
            .update(gameUpdates)
            .addOnSuccessListener { Log.d("OnlineGameViewModel", "Move successfully written!") }
            .addOnFailureListener { e -> Log.w("OnlineGameViewModel", "Error writing move", e) }
    }

    // Basic win condition check for Tic Tac Toe
    private fun checkWinCondition(board: List<String>, currentPlayerIdMakingMove: String, isPlayer1: Boolean): Pair<String, String?> {
        val winPatterns = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // Rows
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // Columns
            listOf(0, 4, 8), listOf(2, 4, 6)  // Diagonals
        )
        val currentPlayerMark = if (isPlayer1) "X" else "O"

        for (pattern in winPatterns) {
            if (pattern.all { board[it] == currentPlayerMark }) {
                val winnerStatus = if (isPlayer1) "player1_wins" else "player2_wins"
                return Pair(winnerStatus, currentPlayerIdMakingMove)
            }
        }
        if (board.all { it.isNotEmpty() }) {
            return Pair("draw", null) // Game is a draw
        }
        return Pair("active", null) // Game continues
    }

    override fun onCleared() {
        super.onCleared()
        gameListenerRegistration?.remove()
    }
}

// ViewModel Factory to pass gameId to OnlineGameViewModel
class OnlineGameViewModelFactory(private val gameId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OnlineGameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OnlineGameViewModel(gameId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineGameScreen(
    gameId: String,
    navController: NavHostController, // Added NavController
    viewModel: OnlineGameViewModel = viewModel(
        factory = OnlineGameViewModelFactory(gameId)
    )
) {
    val gameState by viewModel.gameState.collectAsState()
    // val currentUser = FirebaseAuth.getInstance().currentUser // Not strictly needed here anymore for UI logic

    val isGameOver =
        gameState.status == "player1_wins" || gameState.status == "player2_wins" || gameState.status == "draw"

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Tic Tac Toe - Online") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(R.color.background))
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (gameState.status == "loading") {
                CircularProgressIndicator()
                Text(
                    "Loading game...",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp)
                )
            } else if (gameState.status == "error") {
                Text(
                    "Error: ${gameState.turnMessage}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Button( // <<< ADDED THIS BUTTON FOR ERROR CASE
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Back to Lobby")
                }
            } else {
                Text(
                    text = "${gameState.player1DisplayName ?: "Player 1"} (X) vs ${gameState.player2DisplayName ?: "Waiting..."} (O)",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = gameState.turnMessage,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Game Board
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp) // Added padding below board
                ) {
                    (0..2).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            (0..2).forEach { col ->
                                val index = row * 3 + col
                                Button(
                                    onClick = { viewModel.makeMove(index) },
                                    modifier = Modifier.size(80.dp),
                                    enabled = gameState.boardState[index].isEmpty() && gameState.isUserTurn && !isGameOver, // <<< MODIFIED THIS LINE
                                    shape = MaterialTheme.shapes.medium,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = if (gameState.boardState[index] == "X") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                    )
                                ) {
                                    Text(
                                        gameState.boardState[index],
                                        style = MaterialTheme.typography.headlineLarge
                                    )
                                }
                            }
                        }
                    }
                }

                // "Back to Lobby" button when game is over
                if (isGameOver) { // <<< ADDED THIS CONDITIONAL BLOCK
                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.padding(top = 24.dp)
                    ) {
                        Text("Back to Lobby")
                    }
                }
            }
        }
    }
}


