package com.a_gud_boy.tictactoe

import android.view.View
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object HapticFeedbackManager {
    var isHapticFeedbackEnabled by mutableStateOf(true) // Default to true

    fun performHapticFeedback(view: View, feedbackConstant: Int) {
        if (isHapticFeedbackEnabled) {
            view.performHapticFeedback(feedbackConstant)
        }
    }
}
