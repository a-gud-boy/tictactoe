package com.a_gud_boy.tictactoe


import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.a_gud_boy.tictactoe.GameType // Import GameType
import java.util.Date // Import for Timestamp

enum class MatchWinner {
    PLAYER1,
    PLAYER2,
    DRAW
}

@Entity(tableName = "matches", indices = [Index(value = ["timestamp"])])
data class MatchEntity(
    @PrimaryKey(autoGenerate = true) val matchId: Long = 0,
    val matchNumber: Int, // Could be a sequence or based on count
    val player1Score: Int,
    val player2Score: Int,
    val matchWinnerName: String, // e.g., "You Won 2-1", "AI Won 3-0"
    val winner: MatchWinner, // New field for the actual winner
    val isAgainstAi: Boolean = false, // New field
    val gameType: GameType, // Changed to GameType enum
    val timestamp: Long = System.currentTimeMillis(), // Store as Long
    val duration: Long = 0L // Duration of the match in milliseconds
)

data class MatchWithRoundsAndMoves(
    @Embedded val match: MatchEntity,
    @Relation(
        entity = RoundEntity::class, // Explicitly define the entity for clarity
        parentColumn = "matchId",
        entityColumn = "ownerMatchId"
    )
    val roundsWithMoves: List<RoundWithMoves>
)
