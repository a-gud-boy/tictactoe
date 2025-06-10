package com.a_gud_boy.tictactoe

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "moves",
    foreignKeys = [
        ForeignKey(
            entity = RoundEntity::class,
            parentColumns = ["roundId"],
            childColumns = ["ownerRoundId"],
            onDelete = ForeignKey.CASCADE // If a round is deleted, its moves are deleted
        )
    ]
)
data class MoveEntity(
    @PrimaryKey(autoGenerate = true) val moveId: Long = 0,
    val ownerRoundId: Long, // Foreign key to RoundEntity
    val player: String, // "X" or "O"
    val cellId: String // e.g., "button1"
)
