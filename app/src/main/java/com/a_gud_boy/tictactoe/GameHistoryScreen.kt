package com.a_gud_boy.tictactoe

import androidx.compose.foundation.background // Keep for Box background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
// import androidx.compose.foundation.layout.padding // Not used directly by root Column/Box from Scaffold anymore
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material3.* // For M3 components including NavigationBar, NavigationBarItem, Text, Icon
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp // For HorizontalDivider if re-added
import androidx.compose.ui.unit.sp // For Text if specific size needed for Unknown tab
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController

// Assuming HistoryPageContent and StatisticsPageContent are correctly imported
import com.a_gud_boy.tictactoe.HistoryPageContent
import com.a_gud_boy.tictactoe.StatisticsPageContent

// Define design colors locally as per prompt.
// These were previously defined in HistoryPage.kt and used in GameHistoryScreen styling.
// If these are moved to a central theme file (e.g., ui/theme/Color.kt),
// they should be imported from there instead.
val designPrimaryColor = Color(0xFF141414)
val designNeutralCardBg = Color(0xFFFFFFFF) // Used for NavigationBar background
val designSubtleText = Color(0xFF6B7280)
// val designBorderColor = Color(0xFFE5E7EB) // Needed if re-adding divider for Nav Bar top border
val designNeutralBg = Color(0xFFF8F8F8)      // Screen background for the content area (Box)


@OptIn(ExperimentalMaterial3Api::class) // Retained for M3 components
@Composable
fun GameHistoryScreen(
    mainNavController: NavHostController,
    initialTab: String
) {
    val historyViewModel: HistoryViewModel = viewModel(factory = LocalViewModelFactory.current)
    var selectedTab by rememberSaveable { mutableStateOf(initialTab) }

    Column(modifier = Modifier.fillMaxSize().background(designNeutralBg)) { // Root is a Column, set overall background here

        // Content Area
        Box(
            modifier = Modifier
                .weight(1.0f) // Takes up available space above the NavigationBar
                .fillMaxWidth()
                // Background is now set on the parent Column, or could be set here if different
        ) {
            when (selectedTab) {
                "history" -> {
                    HistoryPageContent(
                        historyViewModel = historyViewModel,
                        navController = mainNavController,
                        modifier = Modifier.fillMaxSize() // Content fills the Box
                    )
                }
                "stats" -> {
                    StatisticsPageContent(
                        historyViewModel = historyViewModel,
                        modifier = Modifier.fillMaxSize() // Content fills the Box
                    )
                }
                else -> {
                    Text(
                        "Unknown tab: $selectedTab",
                        modifier = Modifier.align(Alignment.Center),
                        fontSize = 16.sp // Example, ensure Text uses M3 if this is an M3 project
                    )
                }
            }
        }

        // Optional: Re-add top border for NavigationBar if it was part of the previous design
        // HorizontalDivider(color = designBorderColor, thickness = 1.dp) // designBorderColor needs to be defined/imported

        // Bottom Navigation Bar
        NavigationBar(
            containerColor = designNeutralCardBg // Use design color
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
