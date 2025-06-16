package com.a_gud_boy.tictactoe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
// import androidx.compose.material3.IconButton // No longer needed if TopAppBar is removed
// import androidx.compose.material3.MaterialTheme // M3 MaterialTheme - Keep if used by NavigationBar
import androidx.compose.material3.NavigationBar // M3 NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold // M3 Scaffold
import androidx.compose.material3.Text // M3 Text
// import androidx.compose.material3.TopAppBar // M3 TopAppBar - REMOVED
// import androidx.compose.material3.TopAppBarDefaults // REMOVED
import androidx.compose.material.icons.Icons
// import androidx.compose.material.icons.automirrored.filled.ArrowBack // REMOVED (was in TopAppBar)
import androidx.compose.material.icons.filled.History // New Icon
import androidx.compose.material.icons.filled.Leaderboard // New Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp // For Divider thickness
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.a_gud_boy.tictactoe.HistoryPageContent // Existing import
import com.a_gud_boy.tictactoe.StatisticsPageContent // Added import

// Color constants expected to be available from HistoryPage.kt or a theme file
// designNeutralBg, designNeutralCardBg, designPrimaryColor, designSubtleText, designBorderColor

@Composable
fun GameHistoryScreen(
    mainNavController: NavHostController, // NavController from MainPage's NavHost
    initialTab: String
) {
    val historyViewModel: HistoryViewModel = viewModel(factory = LocalViewModelFactory.current)
    var selectedTab by rememberSaveable { mutableStateOf(initialTab) }

    Scaffold(
        // topBar = { ... } // ENTIRE topBar PARAMETER REMOVED
        bottomBar = {
            Column {
                HorizontalDivider(color = designBorderColor, thickness = 1.dp)
                NavigationBar(
                    containerColor = designNeutralCardBg,
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.History, contentDescription = "History") },
                        selected = selectedTab == "history",
                        onClick = { selectedTab = "history" },
                        label = { Text("History") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = designPrimaryColor,
                            selectedTextColor = designPrimaryColor,
                            unselectedIconColor = designSubtleText,
                            unselectedTextColor = designSubtleText,
                            indicatorColor = Color.Transparent
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Leaderboard, contentDescription = "Stats") },
                        selected = selectedTab == "stats",
                        onClick = { selectedTab = "stats" },
                        label = { Text("Stats") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = designPrimaryColor,
                            selectedTextColor = designPrimaryColor,
                            unselectedIconColor = designSubtleText,
                            unselectedTextColor = designSubtleText,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues) // paddingValues will adjust for the absence of TopAppBar
                .fillMaxSize()
                .background(designNeutralBg), // Set overall screen background
            contentAlignment = Alignment.Center
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
                    Text("Unknown tab: $selectedTab", fontSize = 16.sp)
                }
            }
        }
    }
}
