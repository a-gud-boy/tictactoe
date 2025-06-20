package com.a_gud_boy.tictactoe

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Config.OLDEST_SDK]) // Configure Robolectric for a specific SDK if needed
class AISettingsManagerTest {

    private lateinit var mockContext: Context
    private lateinit var mockSharedPreferences: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor

    private val PREFS_NAME = "AISettingsPrefs" // Make sure this matches the constant in AISettingsManager
    private val KEY_SAVE_HISTORY = "key_save_history" // Make sure this matches

    @Before
    fun setUp() {
        mockContext = ApplicationProvider.getApplicationContext<Context>()
        mockSharedPreferences = mock(SharedPreferences::class.java)
        mockEditor = mock(SharedPreferences.Editor::class.java)

        `when`(mockContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)).thenReturn(mockSharedPreferences)
        `when`(mockSharedPreferences.edit()).thenReturn(mockEditor)
        `when`(mockEditor.putBoolean(anyString(), anyBoolean())).thenReturn(mockEditor)
        `when`(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor)
        `when`(mockEditor.putInt(anyString(), anyInt())).thenReturn(mockEditor)

        // Reset static instance for clean tests.
        // This is a simplified approach. Ideally, AISettingsManager would be injectable or have a reset method.
        val sharedPreferencesField = AISettingsManager::class.java.getDeclaredField("sharedPreferences")
        sharedPreferencesField.isAccessible = true
        sharedPreferencesField.set(AISettingsManager, null) // Force re-initialization

        AISettingsManager.init(mockContext)
    }

    @After
    fun tearDown() {
        // Reset AISettingsManager's SharedPreferences instance to null so it can be re-initialized in the next test
        val sharedPreferencesField = AISettingsManager::class.java.getDeclaredField("sharedPreferences")
        sharedPreferencesField.isAccessible = true
        sharedPreferencesField.set(AISettingsManager, null)
    }

    @Test
    fun saveHistoryEnabled_DefaultValue_IsTrue() {
        // Simulate that the key is not present in SharedPreferences, so it should return the default value (true)
        `when`(mockSharedPreferences.getBoolean(KEY_SAVE_HISTORY, true)).thenReturn(true)

        // Re-initialize to load settings (as if app just started and key wasn't there)
        val sharedPreferencesField = AISettingsManager::class.java.getDeclaredField("sharedPreferences")
        sharedPreferencesField.isAccessible = true
        sharedPreferencesField.set(AISettingsManager, null)
        AISettingsManager.init(mockContext)

        assert(AISettingsManager.saveHistoryEnabled) { "Default value for saveHistoryEnabled should be true" }
    }

    @Test
    fun saveHistoryEnabled_WhenSetToFalse_IsSavedAndLoadedCorrectly() {
        AISettingsManager.saveHistoryEnabled = false
        verify(mockEditor).putBoolean(KEY_SAVE_HISTORY, false)
        verify(mockEditor).apply() // Verify that apply is called to save the preference

        // Simulate that SharedPreferences now stores 'false' for this key
        `when`(mockSharedPreferences.getBoolean(KEY_SAVE_HISTORY, true)).thenReturn(false)

        // Re-initialize to load settings (simulating app restart)
        val sharedPreferencesField = AISettingsManager::class.java.getDeclaredField("sharedPreferences")
        sharedPreferencesField.isAccessible = true
        sharedPreferencesField.set(AISettingsManager, null)
        AISettingsManager.init(mockContext)

        assert(!AISettingsManager.saveHistoryEnabled) { "saveHistoryEnabled should be false after setting and reloading" }
    }

    @Test
    fun saveHistoryEnabled_WhenSetToTrue_IsSavedAndLoadedCorrectly() {
        // First set it to false, then to true
        AISettingsManager.saveHistoryEnabled = false
        AISettingsManager.saveHistoryEnabled = true
        verify(mockEditor).putBoolean(KEY_SAVE_HISTORY, true)
        verify(mockEditor, times(2)).apply() // apply called for false then for true

        // Simulate that SharedPreferences now stores 'true' for this key
        `when`(mockSharedPreferences.getBoolean(KEY_SAVE_HISTORY, true)).thenReturn(true)

        // Re-initialize to load settings
        val sharedPreferencesField = AISettingsManager::class.java.getDeclaredField("sharedPreferences")
        sharedPreferencesField.isAccessible = true
        sharedPreferencesField.set(AISettingsManager, null)
        AISettingsManager.init(mockContext)

        assert(AISettingsManager.saveHistoryEnabled) { "saveHistoryEnabled should be true after setting and reloading" }
    }

    @Test
    fun loadSettings_SaveHistoryNotPresent_DefaultsToTrue() {
        `when`(mockSharedPreferences.getBoolean(KEY_SAVE_HISTORY, true)).thenReturn(true) // Default behavior of getBoolean

        val sharedPreferencesField = AISettingsManager::class.java.getDeclaredField("sharedPreferences")
        sharedPreferencesField.isAccessible = true
        sharedPreferencesField.set(AISettingsManager, null)
        AISettingsManager.init(mockContext)

        assert(AISettingsManager.saveHistoryEnabled) { "saveHistoryEnabled should default to true if key not present" }
    }

    @Test
    fun loadSettings_SaveHistoryPresentAsFalse_LoadsFalse() {
        `when`(mockSharedPreferences.getBoolean(KEY_SAVE_HISTORY, true)).thenReturn(false)

        val sharedPreferencesField = AISettingsManager::class.java.getDeclaredField("sharedPreferences")
        sharedPreferencesField.isAccessible = true
        sharedPreferencesField.set(AISettingsManager, null)
        AISettingsManager.init(mockContext)

        assert(!AISettingsManager.saveHistoryEnabled) { "saveHistoryEnabled should load as false if stored as false" }
    }

    @Test
    fun loadSettings_SaveHistoryPresentAsTrue_LoadsTrue() {
        `when`(mockSharedPreferences.getBoolean(KEY_SAVE_HISTORY, true)).thenReturn(true)

        val sharedPreferencesField = AISettingsManager::class.java.getDeclaredField("sharedPreferences")
        sharedPreferencesField.isAccessible = true
        sharedPreferencesField.set(AISettingsManager, null)
        AISettingsManager.init(mockContext)

        assert(AISettingsManager.saveHistoryEnabled) { "saveHistoryEnabled should load as true if stored as true" }
    }
}
