package com.a_gud_boy.tictactoe

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(
    tableName = "rounds",
    foreignKeys = [
        ForeignKey(
            entity = MatchEntity::class,
            parentColumns = ["matchId"],
            childColumns = ["ownerMatchId"],
            onDelete = ForeignKey.CASCADE // If a match is deleted, its rounds are deleted
        )
    ]
)
data class RoundEntity(
    @PrimaryKey(autoGenerate = true) val roundId: Long = 0,
    val ownerMatchId: Long, // Foreign key to MatchEntity
    val roundNumber: Int,
    val winner: String?, // "X", "O", or null for draw
    val roundWinnerName: String // e.g., "Player 1 Won", "AI Won", "Draw"
)

data class RoundWithMoves(
    @Embedded val round: RoundEntity,
    @Relation(
        parentColumn = "roundId",
        entityColumn = "ownerRoundId"
    )
    val moves: List<MoveEntity>
)
