package com.a_gud_boy.tictactoe

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.a_gud_boy.tictactoe.ui.theme.TictactoeTheme
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Locale

// Data class to represent a game in the lobby
data class GameLobbyItem(
    val gameId: String = "",
    val player1DisplayName: String = "Unknown Player",
    val createdAt: Timestamp? = null // Firestore Timestamp
) {
    // Helper to format timestamp, can be expanded
    fun getFormattedCreationTime(): String {
        return createdAt?.toDate()?.let {
            SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(it)
        } ?: "N/A"
    }
}

@Composable
fun OnlineLobbyScreen(innerPadding: PaddingValues, onNavigateToGame: (String) -> Unit) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val db = Firebase.firestore

    var availableGames by remember { mutableStateOf<List<GameLobbyItem>>(emptyList()) }
    var isLoadingGames by remember { mutableStateOf(true) }
    var gamesListenerRegistration by remember { mutableStateOf<ListenerRegistration?>(null) }

    // Listener for available games
    LaunchedEffect(currentUser?.uid) {
        if (currentUser == null) {
            isLoadingGames = false
            availableGames = emptyList()
            gamesListenerRegistration?.remove() // Clean up previous listener if any
            gamesListenerRegistration = null
            return@LaunchedEffect
        }

        isLoadingGames = true
        // Remove any existing listener before attaching a new one
        gamesListenerRegistration?.remove()

        val query = db.collection("gameSessions")
            .whereEqualTo("status", "waiting_for_player")
            // .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING) // Optional: order by creation time

        gamesListenerRegistration = query.addSnapshotListener { snapshots, e ->
            if (e != null) {
                Log.w("OnlineLobby", "Listen failed.", e)
                isLoadingGames = false
                availableGames = emptyList() // Clear games on error
                return@addSnapshotListener
            }

            val gamesList = snapshots?.documents?.mapNotNull { doc ->
                // Filter out games created by the current user
                if (doc.getString("player1Id") == currentUser.uid) {
                    return@mapNotNull null
                }
                GameLobbyItem(
                    gameId = doc.id,
                    player1DisplayName = doc.getString("player1DisplayName") ?: "Player 1",
                    createdAt = doc.getTimestamp("createdAt")
                )
            } ?: emptyList()

            availableGames = gamesList
            isLoadingGames = false
        }
    }
    
    // Ensure listener is removed when the composable leaves the composition
    DisposableEffect(Unit) {
        onDispose {
            gamesListenerRegistration?.remove()
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.background))
            .padding(innerPadding) // Apply padding from Scaffold
            .padding(16.dp), // Additional padding for content
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Online Multiplayer Lobby", style = MaterialTheme.typography.headlineSmall)

        if (currentUser == null) {
            Text("Authenticating... Please wait.")
            // Optionally, add a retry button or more robust auth handling here
            return
        }

        Button(
            onClick = {
                val gameSession = hashMapOf(
                    "player1Id" to currentUser.uid,
                    "player1DisplayName" to (currentUser.displayName ?: currentUser.email ?: "Anonymous P1"),
                    "player2Id" to null,
                    "player2DisplayName" to null,
                    "boardState" to List(9) { "" }, // Empty 3x3 board
                    "currentPlayerId" to currentUser.uid, // Creator starts
                    "status" to "waiting_for_player",
                    "winnerId" to null,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "lastMoveAt" to FieldValue.serverTimestamp()
                )

                db.collection("gameSessions")
                    .add(gameSession)
                    .addOnSuccessListener { documentReference ->
                        Log.d("OnlineLobby", "Game session created with ID: ${'$'}{documentReference.id}")
                        onNavigateToGame(documentReference.id)
                    }
                    .addOnFailureListener { ex ->
                        Log.w("OnlineLobby", "Error creating game session", ex)
                        // TODO: Show error to user (e.g., Toast)
                    }
            },
            // enabled = !isLoadingGames // Disable if games are loading, or allow creating while loading
        ) {
            Text("Create New Game")
        }

        Divider()
        Text("Available Games to Join", style = MaterialTheme.typography.titleMedium)

        if (isLoadingGames) {
            CircularProgressIndicator()
        } else if (availableGames.isEmpty()) {
            Text("No games available to join. Create one!")
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(availableGames, key = { it.gameId }) { game ->
                    GameListItem(game = game, currentUserId = currentUser.uid) { gameIdToJoin ->
                        // Join game logic
                        val gameRef = db.collection("gameSessions").document(gameIdToJoin)
                        db.runTransaction { transaction ->
                            val snapshot = transaction.get(gameRef)
                            if (snapshot.getString("player2Id") != null) {
                                // Game already taken
                                throw FirebaseFirestoreException(
                                    "Game already joined by another player.",
                                    FirebaseFirestoreException.Code.ABORTED
                                )
                            }
                            transaction.update(gameRef, mapOf(
                                "player2Id" to currentUser.uid,
                                "player2DisplayName" to (currentUser.displayName ?: currentUser.email ?: "Anonymous P2"),
                                "status" to "active", // Game starts
                                "lastMoveAt" to FieldValue.serverTimestamp()
                            ))
                            null // Transaction must return null or a result
                        }.addOnSuccessListener {
                            Log.d("OnlineLobby", "Successfully joined game: $gameIdToJoin")
                            onNavigateToGame(gameIdToJoin)
                        }.addOnFailureListener { ex ->
                            Log.w("OnlineLobby", "Error joining game: $gameIdToJoin", ex)
                            // TODO: Show error to user (e.g., game already taken, network error)
                        }
                    }
                    Divider()
                }
            }
        }
    }
}

@Composable
fun GameListItem(game: GameLobbyItem, currentUserId: String, onJoinGame: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Game by: ${'$'}{game.player1DisplayName}", style = MaterialTheme.typography.bodyLarge)
            Text("Created: ${'$'}{game.getFormattedCreationTime()}", style = MaterialTheme.typography.bodySmall)
        }
        // Button to join the game
        // Ensure user cannot join their own game (already filtered in query, but good for UI too)
        if (game.gameId.isNotEmpty() /* basic check */ ) {
            Button(onClick = { onJoinGame(game.gameId) }) {
                Text("Join")
            }
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OnlineLobbyScreenPreview() {
    TictactoeTheme { // Assuming your theme is TicTacToeTheme
        // Mock data for preview
        val mockGames = listOf(
            GameLobbyItem("game1", "PlayerOne", Timestamp.now()),
            GameLobbyItem("game2", "AnotherPlayer", Timestamp.now())
        )
        var availableGames by remember { mutableStateOf(mockGames) }
        var isLoadingGames by remember { mutableStateOf(false) }

        Scaffold { padding -> // Use Scaffold for preview if your screen uses it
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding) // Apply padding from Scaffold
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Online Multiplayer Lobby", style = MaterialTheme.typography.headlineSmall)
                Button(onClick = {}) { Text("Create New Game") }
                Divider()
                Text("Available Games to Join", style = MaterialTheme.typography.titleMedium)

                if (isLoadingGames) {
                    CircularProgressIndicator()
                } else if (availableGames.isEmpty()) {
                    Text("No games available. Create one!")
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(availableGames, key = { it.gameId }) { game ->
                            GameListItem(game = game, currentUserId = "previewUser") { /* onJoinGame */ }
                            Divider()
                        }
                    }
                }
            }
        }
    }
}
