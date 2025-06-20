package com.a_gud_boy.tictactoe

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query // Keep Query import

@Dao
interface RoundDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRound(round: RoundEntity): Long // Returns new roundId

    @Query("DELETE FROM rounds WHERE roundId = :roundId")
    suspend fun deleteRoundById(roundId: Long)
}
