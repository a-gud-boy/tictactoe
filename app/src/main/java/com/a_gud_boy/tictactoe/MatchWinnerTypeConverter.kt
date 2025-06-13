package com.a_gud_boy.tictactoe

import androidx.room.TypeConverter

class MatchWinnerTypeConverter {
    @TypeConverter
    fun fromMatchWinner(winner: MatchWinner): String {
        return winner.name
    }

    @TypeConverter
    fun toMatchWinner(winnerString: String): MatchWinner {
        return MatchWinner.valueOf(winnerString)
    }
}
