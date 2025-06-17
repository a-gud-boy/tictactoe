package com.a_gud_boy.tictactoe

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameHistoryScreen(
    mainNavController: NavHostController,
    initialTab: String
) {
    val historyViewModel: HistoryViewModel = viewModel(factory = LocalViewModelFactory.current)
    var selectedTab by rememberSaveable { mutableStateOf(initialTab) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.background))
    ) {

        // Content Area
        Box(
            modifier = Modifier
                .weight(1.0f)
                .fillMaxWidth()
        ) {
            when (selectedTab) {
                "history" -> {
                    HistoryPageContent(
                        historyViewModel = historyViewModel,
                        navController = mainNavController,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                "stats" -> {
                    StatisticsPageContent(
                        historyViewModel = historyViewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                else -> {
                    Text(
                        "Unknown tab: $selectedTab",
                        modifier = Modifier.align(Alignment.Center),
                        fontSize = 16.sp
                    )
                }
            }
        }

        // Custom Bottom Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp), // Approximate height of original Nav Bar
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            // History Tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clickable { selectedTab = "history" }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.List,
                        contentDescription = "History",
                        tint = if (selectedTab == "history") designPrimaryColor else designSubtleText
                    )
                    Text(
                        text = "History",
                        color = if (selectedTab == "history") designPrimaryColor else designSubtleText,
                        fontSize = 12.sp // Typical size for nav labels
                    )
                }
            }

            // Stats Tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clickable { selectedTab = "stats" }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Stats",
                        tint = if (selectedTab == "stats") designPrimaryColor else designSubtleText
                    )
                    Text(
                        text = "Stats",
                        color = if (selectedTab == "stats") designPrimaryColor else designSubtleText,
                        fontSize = 12.sp // Typical size for nav labels
                    )
                }
            }
        }
    }
}
