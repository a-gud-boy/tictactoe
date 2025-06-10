package com.a_gud_boy.tictactoe

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// Assuming AIDifficulty enum is defined elsewhere, e.g., in a common file or one of the ViewModels.
// If not, it would need to be defined:
// enum class AIDifficulty { EASY, MEDIUM, HARD }

object AISettingsManager {
    var currentDifficulty by mutableStateOf(AIDifficulty.MEDIUM) // Default to Medium
    var isAiModeEnabled by mutableStateOf(false) // Default AI mode to off
}
