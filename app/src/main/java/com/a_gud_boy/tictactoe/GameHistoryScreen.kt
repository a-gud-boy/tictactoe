package com.a_gud_boy.tictactoe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
            .background(designNeutralBg)
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

        // Bottom Navigation Bar
        NavigationBar(
//            modifier = Modifier.height(64.dp),
            containerColor = designNeutralCardBg
        ) {
            NavigationBarItem(
                icon = { Icon(Icons.Filled.Add, contentDescription = "History") },
                selected = selectedTab == "history",
                onClick = { selectedTab = "history" },
                label = {
//                    Box(modifier = Modifier.padding(top = 2.dp)) { // Applied padding
                    Text("History")
//                    }
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
                icon = { Icon(Icons.Filled.Add, contentDescription = "Stats") },
                selected = selectedTab == "stats",
                onClick = { selectedTab = "stats" },
                label = {
//                    Box(modifier = Modifier.padding(top = 2.dp)) { // Applied padding
                    Text("Stats")
//                    }
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
