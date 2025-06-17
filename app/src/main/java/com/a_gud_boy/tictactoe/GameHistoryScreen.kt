package com.a_gud_boy.tictactoe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding // Import for padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController

import com.a_gud_boy.tictactoe.HistoryPageContent
import com.a_gud_boy.tictactoe.StatisticsPageContent

val designPrimaryColor = Color(0xFF141414)
val designNeutralCardBg = Color(0xFFFFFFFF)
val designSubtleText = Color(0xFF6B7280)
val designNeutralBg = Color(0xFFF8F8F8)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameHistoryScreen(
    mainNavController: NavHostController,
    initialTab: String
) {
    val historyViewModel: HistoryViewModel = viewModel(factory = LocalViewModelFactory.current)
    var selectedTab by rememberSaveable { mutableStateOf(initialTab) }

    Column(modifier = Modifier.fillMaxSize().background(designNeutralBg)) {

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

        // Bottom Navigation Bar
        NavigationBar(
            modifier = Modifier.height(64.dp),
            containerColor = designNeutralCardBg
        ) {
            NavigationBarItem(
                icon = { Icon(Icons.Filled.History, contentDescription = "History") },
                selected = selectedTab == "history",
                onClick = { selectedTab = "history" },
                label = {
                    Box(modifier = Modifier.padding(top = 2.dp)) { // Applied padding
                        Text("History")
                    }
                },
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
                label = {
                    Box(modifier = Modifier.padding(top = 2.dp)) { // Applied padding
                        Text("Stats")
                    }
                },
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
