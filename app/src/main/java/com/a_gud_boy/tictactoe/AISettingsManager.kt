package com.a_gud_boy.tictactoe

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object AISettingsManager {
    private const val PREFS_NAME = "AISettingsPrefs"
    private const val KEY_DIFFICULTY = "key_difficulty"
    private const val KEY_AI_MODE = "key_ai_mode"
    private const val KEY_SOUND_ENABLED = "key_sound_enabled"
    private const val KEY_SAVE_HISTORY = "key_save_history"

    private var sharedPreferences: SharedPreferences? = null

    // Default values are set here, but will be overwritten by loadSettings if prefs exist
    private var _currentDifficulty by mutableStateOf(AIDifficulty.MEDIUM)
    var currentDifficulty: AIDifficulty
        get() = _currentDifficulty
        set(value) {
            _currentDifficulty = value
            sharedPreferences?.edit()?.putString(KEY_DIFFICULTY, value.name)?.commit() // Changed to commit
        }

    private var _isAiModeEnabled by mutableStateOf(false)
    var isAiModeEnabled: Boolean
        get() = _isAiModeEnabled
        set(value) {
            _isAiModeEnabled = value
            sharedPreferences?.edit()?.putBoolean(KEY_AI_MODE, value)?.commit() // Changed to commit
        }

    private var _isSoundEnabled by mutableStateOf(true)
    private var _saveHistoryEnabled by mutableStateOf(true) // Default to true
    var isSoundEnabled: Boolean
        get() = _isSoundEnabled
        set(value) {
            _isSoundEnabled = value
            sharedPreferences?.edit()?.putBoolean(KEY_SOUND_ENABLED, value)?.commit() // Changed to commit
        }

    var saveHistoryEnabled: Boolean
        get() = _saveHistoryEnabled
        set(value) {
            _saveHistoryEnabled = value
            sharedPreferences?.edit()?.putBoolean(KEY_SAVE_HISTORY, value)?.commit() // Changed to commit
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
            // Load difficulty
            val difficultyName = prefs.getString(KEY_DIFFICULTY, AIDifficulty.MEDIUM.name)
            _currentDifficulty = try {
                AIDifficulty.valueOf(difficultyName ?: AIDifficulty.MEDIUM.name)
            } catch (e: IllegalArgumentException) {
                // Handle cases where the stored name is invalid, fallback to default
                AIDifficulty.MEDIUM
            }

            // Load AI mode
            _isAiModeEnabled = prefs.getBoolean(KEY_AI_MODE, false)

            // Load sound enabled
            _isSoundEnabled = prefs.getBoolean(KEY_SOUND_ENABLED, true)
            // Load save history enabled
            _saveHistoryEnabled = prefs.getBoolean(KEY_SAVE_HISTORY, true) // Default to true
        }
    }
}
