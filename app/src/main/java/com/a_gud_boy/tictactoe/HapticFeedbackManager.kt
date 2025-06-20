package com.a_gud_boy.tictactoe

import android.content.Context
import android.content.SharedPreferences
import android.view.View
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object HapticFeedbackManager {
    private const val PREFS_NAME = "HapticSettingsPrefs"
    private const val KEY_HAPTIC_ENABLED = "key_haptic_enabled"

    private var sharedPreferences: SharedPreferences? = null

    // Default value is true, will be updated by loadSettings if a preference exists
    private var _isHapticFeedbackEnabled by mutableStateOf(true)
    var isHapticFeedbackEnabled: Boolean
        get() = _isHapticFeedbackEnabled
        set(value) {
            _isHapticFeedbackEnabled = value
            sharedPreferences?.edit()?.putBoolean(KEY_HAPTIC_ENABLED, value)?.commit() // Changed to commit
        }

    fun init(context: Context) {
        if (sharedPreferences == null) {
            sharedPreferences =
                context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            loadSettings()
        }
    }

    private fun loadSettings() {
        sharedPreferences?.let { prefs ->
            _isHapticFeedbackEnabled = prefs.getBoolean(KEY_HAPTIC_ENABLED, true) // Default to true
        }
    }

    fun performHapticFeedback(view: View, feedbackConstant: Int) {
        if (isHapticFeedbackEnabled) {
            view.performHapticFeedback(feedbackConstant)
        }
    }
}
