package com.a_gud_boy.tictactoe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class InfiniteTicTacToeViewModelFactory(private val gameMode: GameMode) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InfiniteTicTacToeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InfiniteTicTacToeViewModel(gameMode) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
