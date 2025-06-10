package com.a_gud_boy.tictactoe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage() {
    val context = LocalContext.current
    // val soundManager = remember { SoundManager(context) } // Removed: SoundManager instance no longer created here

    // ViewModel instances - In a real app, provide a proper factory or use Hilt for DI
    // SoundManager is still needed by ViewModels, so it's instantiated inside the factory.
    // This is acceptable as SoundManager itself doesn't hold the isSoundEnabled state anymore.
    val normalTicTacToeViewModel: NormalTicTacToeViewModel = viewModel(
        factory = TicTacToeViewModelFactory(SoundManager(context))
    )
    val infiniteTicTacToeViewModel: InfiniteTicTacToeViewModel = viewModel(
        factory = TicTacToeViewModelFactory(SoundManager(context))
    )

    // var soundEnabled by remember { mutableStateOf(soundManager.isSoundEnabled) } // Removed: State now from AISettingsManager
    // Haptic feedback state is managed by HapticFeedbackManager

    // AI settings are now managed by AISettingsManager
    // Local state for slider position might still be useful for immediate UI response
    // then update AISettingsManager and ViewModels.
    var sliderPosition by remember {
        mutableStateOf(
            when (AISettingsManager.currentDifficulty) {
                AIDifficulty.EASY -> 0f
                AIDifficulty.MEDIUM -> 1f
                AIDifficulty.HARD -> 2f
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        // Sound Setting
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Sound")
            Switch(
                checked = AISettingsManager.isSoundEnabled, // Read from AISettingsManager
                onCheckedChange = {
                    AISettingsManager.isSoundEnabled = it // Update AISettingsManager
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
                checked = AISettingsManager.isAiModeEnabled,
                onCheckedChange = { enabled ->
                    AISettingsManager.isAiModeEnabled = enabled
                    normalTicTacToeViewModel.setAIMode(enabled)
                    infiniteTicTacToeViewModel.setAIMode(enabled)
                }
            )
        }

        // Haptic Feedback Setting
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Haptic Feedback")
            Switch(
                checked = HapticFeedbackManager.isHapticFeedbackEnabled,
                onCheckedChange = { HapticFeedbackManager.isHapticFeedbackEnabled = it }
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
                steps = 1 // 0 (Easy), 1 (Medium), 2 (Hard)
            )
        }
    }
}
