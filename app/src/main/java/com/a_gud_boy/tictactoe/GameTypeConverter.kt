package com.a_gud_boy.tictactoe

import androidx.room.TypeConverter

class GameTypeConverter {
    @TypeConverter
    fun fromGameType(gameType: GameType?): String? {
        return gameType?.name
    }

    @TypeConverter
    fun toGameType(name: String?): GameType? {
        return name?.let { GameType.valueOf(it) }
    }
}
