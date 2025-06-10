package com.a_gud_boy.tictactoe

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: MatchEntity): Long // Returns new matchId

    @Transaction // Ensures that the query to get rounds and their moves is atomic
    @Query("SELECT * FROM matches ORDER BY timestamp DESC")
    fun getAllMatchesWithRoundsAndMoves(): Flow<List<MatchWithRoundsAndMoves>>

    @Transaction
    @Query("SELECT * FROM matches WHERE matchId = :matchId")
    fun getMatchWithRoundsAndMovesById(matchId: Long): Flow<MatchWithRoundsAndMoves?> // Can be nullable if ID not found

    @Query("DELETE FROM matches")
    suspend fun clearAllMatches() // For clearing history

    // It might be good to have a way to get the count of matches, e.g. for the next matchNumber
    @Query("SELECT COUNT(matchId) FROM matches")
    suspend fun getMatchesCount(): Int
}
