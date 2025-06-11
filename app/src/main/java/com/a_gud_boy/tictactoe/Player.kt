package com.a_gud_boy.tictactoe

enum class Player {
    X, O;

    companion object {
        fun fromString(name: String): Player? {
            return when (name) {
                "X" -> X
                "O" -> O
                else -> null // Or throw an IllegalArgumentException if strict parsing is needed
            }
        }
    }
}
