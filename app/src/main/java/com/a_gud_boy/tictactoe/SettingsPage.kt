package com.a_gud_boy.tictactoe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect // Keep this, it's used by other toggles and the new one
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(innerPadding: PaddingValues) {
    val context = LocalContext.current
    val appDatabase = AppDatabase.getDatabase(context)
    // ViewModel factory is provided by LocalViewModelFactory.current in MainActivity's CompositionLocalProvider
    val factory = LocalViewModelFactory.current

    val normalTicTacToeViewModel: NormalTicTacToeViewModel = viewModel(factory = factory)
    val infiniteTicTacToeViewModel: InfiniteTicTacToeViewModel = viewModel(factory = factory)
    val historyViewModel: HistoryViewModel = viewModel(factory = factory)

    var showDeleteHistoryDialog by remember { mutableStateOf(false) }

    // Local state for the Sound Switch
    var localSoundEnabled by remember { mutableStateOf(AISettingsManager.isSoundEnabled) }
    LaunchedEffect(AISettingsManager.isSoundEnabled) {
        if (localSoundEnabled != AISettingsManager.isSoundEnabled) {
            localSoundEnabled = AISettingsManager.isSoundEnabled
        }
    }

    // Local state for the Haptic Feedback Switch
    var localHapticFeedbackEnabled by remember { mutableStateOf(HapticFeedbackManager.isHapticFeedbackEnabled) }
    LaunchedEffect(HapticFeedbackManager.isHapticFeedbackEnabled) {
        if (localHapticFeedbackEnabled != HapticFeedbackManager.isHapticFeedbackEnabled) {
            localHapticFeedbackEnabled = HapticFeedbackManager.isHapticFeedbackEnabled
        }
    }

    // Local state for "Play vs AI" Switch
    var localIsAiModeEnabled by remember { mutableStateOf(AISettingsManager.isAiModeEnabled) }
    LaunchedEffect(AISettingsManager.isAiModeEnabled) {
        if (localIsAiModeEnabled != AISettingsManager.isAiModeEnabled) {
            localIsAiModeEnabled = AISettingsManager.isAiModeEnabled
        }
    }

    var sliderPosition by remember {
        mutableStateOf(
            when (AISettingsManager.currentDifficulty) {
                AIDifficulty.EASY -> 0f
                AIDifficulty.MEDIUM -> 1f
                AIDifficulty.HARD -> 2f
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.background))
            .padding(innerPadding),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sound Setting Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Sound")
                Switch(
                    checked = localSoundEnabled,
                    onCheckedChange = { newCheckedState ->
                        AISettingsManager.isSoundEnabled = newCheckedState
                        localSoundEnabled = newCheckedState
                    }
                )
            }

            // Save History Setting
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Save History")
                Switch(
                    checked = AISettingsManager.saveHistoryEnabled,
                    onCheckedChange = { newValue ->
                        if (!newValue) {
                            showDeleteHistoryDialog = true
                        } else {
                            AISettingsManager.saveHistoryEnabled = true
                        }
                    }
                )
            }

            // Play vs AI Setting
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Play vs AI")
                Switch(
                    checked = localIsAiModeEnabled, // Use the local state
                    onCheckedChange = { enabled ->
                        localIsAiModeEnabled = enabled          // 1. Update local UI state
                        AISettingsManager.isAiModeEnabled = enabled // 2. Update global setting

                        // 3. Notify ViewModels
                        normalTicTacToeViewModel.setAIMode(enabled)
                        infiniteTicTacToeViewModel.setAIMode(enabled)
                    }
                )
            }

            // Haptic Feedback Setting Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Haptic Feedback")
                Switch(
                    checked = localHapticFeedbackEnabled,
                    onCheckedChange = { newCheckedState ->
                        HapticFeedbackManager.isHapticFeedbackEnabled = newCheckedState
                        localHapticFeedbackEnabled = newCheckedState
                    }
                )
            }

            // AI Difficulty Setting
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("AI Difficulty")
                    Text(
                        when (sliderPosition.roundToInt()) {
                            0 -> "Easy"
                            1 -> "Medium"
                            else -> "Hard"
                        }
                    )
                }
                Slider(
                    value = sliderPosition,
                    onValueChange = { newPosition ->
                        sliderPosition = newPosition
                        val newDifficulty = when (newPosition.roundToInt()) {
                            0 -> AIDifficulty.EASY
                            1 -> AIDifficulty.MEDIUM
                            else -> AIDifficulty.HARD
                        }
                        AISettingsManager.currentDifficulty = newDifficulty
                        normalTicTacToeViewModel.setAIDifficulty(newDifficulty)
                        infiniteTicTacToeViewModel.setAIDifficulty(newDifficulty)
                    },
                    valueRange = 0f..2f,
                    steps = 1
                )
            }

            if (showDeleteHistoryDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteHistoryDialog = false },
                    title = { Text("Disable Save History") },
                    text = { Text("Do you also want to delete all previously saved match history?") },
                    confirmButton = {
                        TextButton(onClick = {
                            AISettingsManager.saveHistoryEnabled = false
                            historyViewModel.clearAllHistory()
                            showDeleteHistoryDialog = false
                        }) {
                            Text("Yes, Delete History")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            AISettingsManager.saveHistoryEnabled = false
                            showDeleteHistoryDialog = false
                        }) {
                            Text("No, Keep History")
                        }
                    }
                )
            }
        }
    }
}
