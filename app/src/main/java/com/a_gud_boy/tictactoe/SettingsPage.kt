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
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage() {
    var soundEnabled by remember { mutableStateOf(true) }
    var hapticFeedbackEnabled by remember { mutableStateOf(true) }
    var aiDifficulty by remember { mutableStateOf(1f) }

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
                checked = soundEnabled,
                onCheckedChange = { soundEnabled = it }
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
                checked = hapticFeedbackEnabled,
                onCheckedChange = { hapticFeedbackEnabled = it }
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
                    when (aiDifficulty.roundToInt()) {
                        0 -> "Easy"
                        1 -> "Medium"
                        else -> "Hard"
                    }
                )
            }
            Slider(
                value = aiDifficulty,
                onValueChange = { aiDifficulty = it },
                valueRange = 0f..2f,
                steps = 1 // 0 (Easy), 1 (Medium), 2 (Hard) -> 2 steps means 3 possible values
            )
        }
    }
}
