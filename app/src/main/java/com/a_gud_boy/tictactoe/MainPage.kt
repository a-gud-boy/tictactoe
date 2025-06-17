package com.a_gud_boy.tictactoe

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
// import androidx.compose.material.icons.automirrored.outlined.List // No longer needed for separate Statistics item
import androidx.compose.material.icons.filled.Build // Icon for Game History
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.R)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPage() {
    val historyViewModel: HistoryViewModel = viewModel(factory = LocalViewModelFactory.current)

    var showInfoDialog by rememberSaveable { mutableStateOf(false) }
    var infoDialogTitle by rememberSaveable { mutableStateOf("") }
    var infoDialogMessage by rememberSaveable { mutableStateOf("") }
    var showClearHistoryDialog by rememberSaveable { mutableStateOf(false) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedItemIndex by rememberSaveable { mutableIntStateOf(0) }

    val items = listOf(
        "Normal TicTacToe",     // Index 0
        "Infinite TicTacToe", // Index 1
        "Game History",         // Index 2
        "Settings",             // Index 3
        "Help"                  // Index 4
    )
    val gameHistoryItemIndex = 2

    val navController = rememberNavController()

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet {
                Column(modifier = Modifier.fillMaxHeight()) {
                    DrawerHeader()
                    Column {
                        items.forEachIndexed { index, itemText ->
                            NavigationDrawerItem(
                                label = { Text(itemText) },
                                selected = index == selectedItemIndex,
                                onClick = {
                                    if (index == gameHistoryItemIndex) {
                                        val startRouteForGameHistoryNavHost =
                                            "game_history_screen/history"
                                        if (selectedItemIndex == gameHistoryItemIndex) {
                                            navController.navigate(startRouteForGameHistoryNavHost) {
                                                popUpTo(startRouteForGameHistoryNavHost) {
                                                    inclusive = true
                                                }
                                                launchSingleTop = true
                                            }
                                        } else {
                                            selectedItemIndex = gameHistoryItemIndex
                                        }
                                    } else {
                                        selectedItemIndex = index
                                    }
                                    scope.launch { drawerState.close() }
                                },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                                colors = NavigationDrawerItemDefaults.colors(
                                    unselectedContainerColor = Color.Transparent,
                                    selectedContainerColor = if (index == selectedItemIndex) MaterialTheme.colorScheme.primaryContainer.copy(
                                        alpha = 0.1f
                                    ) else Color.Transparent,
                                    selectedTextColor = Color.Black
                                ),
                                icon = {
                                    when (index) {
                                        0 -> Icon(
                                            painterResource(R.drawable.normal_tic_tac_toe),
                                            "Normal Tic Tac Toe",
                                            modifier = Modifier.size(30.dp)
                                        )

                                        1 -> Icon(
                                            painterResource(R.drawable.infinite_tic_tac_toe),
                                            "Infinite Tic Tac Toe",
                                            modifier = Modifier.size(30.dp)
                                        )

                                        gameHistoryItemIndex -> Icon(
                                            Icons.Filled.Build,
                                            "Game History",
                                            modifier = Modifier.size(30.dp)
                                        )

                                        3 -> Icon(
                                            Icons.Filled.Settings,
                                            "Settings",
                                            modifier = Modifier.size(30.dp)
                                        )

                                        4 -> Icon(
                                            Icons.Outlined.Info,
                                            "Help",
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    DrawerFooter()
                }
            }
        },
        drawerState = drawerState
    ) {
        Scaffold(
            topBar = {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                TopAppBar(
                    title = {
                        val titleText by remember {
                            derivedStateOf {
                                val currentRoute = navBackStackEntry?.destination?.route
                                when (selectedItemIndex) {
                                    0 -> "Tic Tac Toe"
                                    1 -> "Infinite TicTacToe"
                                    gameHistoryItemIndex -> {
                                        when {
                                            currentRoute?.startsWith("match_details/") == true -> "Match Details"
                                            currentRoute?.startsWith("roundReplay/") == true -> "Match Replay"
                                            else -> "Game History"
                                        }
                                    }

                                    3 -> "Settings"
                                    4 -> "Help"
                                    else -> "Lorem Ipsum"
                                }
                            }
                        }
                        Text(
                            text = titleText,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelMedium,
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        val currentRoute = navBackStackEntry?.destination?.route
                        if (selectedItemIndex == gameHistoryItemIndex && (currentRoute?.startsWith("match_details/") == true || currentRoute?.startsWith(
                                "roundReplay/"
                            ) == true)
                        ) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        } else {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Filled.Menu, contentDescription = "Menu Icon")
                            }
                        }
                    },
                    actions = {
                        val currentRoute = navBackStackEntry?.destination?.route
                        if (selectedItemIndex == gameHistoryItemIndex) {
                            if (currentRoute == "game_history_screen/history") {
                                IconButton(onClick = { showClearHistoryDialog = true }) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "Clear All History"
                                    )
                                }
                            }
                        } else {
                            IconButton(onClick = {
                                when (selectedItemIndex) {
                                    0 -> {
                                        infoDialogTitle = "Normal Tic Tac Toe"; infoDialogMessage =
                                            "This is the classic Tic Tac Toe game. Get three of your marks in a row (horizontally, vertically, or diagonally) to win. Player X goes first."
                                    }

                                    1 -> {
                                        infoDialogTitle =
                                            "Infinite Tic Tac Toe"; infoDialogMessage =
                                            "A twist on the classic! Marks disappear after 3 subsequent moves by any player. Strategy is key as the board constantly changes. Get three of your marks in a row to win."
                                    }

                                    3 -> {
                                        infoDialogTitle = "Settings"; infoDialogMessage =
                                            "Here you can configure various application settings:\n- Sound: Toggle game sounds on or off.\n- Haptic Feedback: Toggle vibrational feedback on or off.\n- AI Mode: Enable or disable playing against the AI.\n- AI Difficulty: Adjust the AI's skill level when AI mode is enabled."
                                    }

                                    4 -> {
                                        infoDialogTitle = "Help"; infoDialogMessage =
                                            "Welcome to Tic Tac Toe!\n\n- Navigation: Use the drawer menu (swipe from left or tap the menu icon) to switch between game modes, view Game History, Settings, and this Help page.\n- Game Play: Follow on-screen instructions for each game mode.\n- Settings: Customize your experience in the Settings page."
                                    }
                                }
                                showInfoDialog = true
                            }) {
                                Icon(Icons.Outlined.Info, contentDescription = "Information")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colorResource(R.color.background),
                        titleContentColor = colorResource(R.color.darkTextColor),
                        navigationIconContentColor = colorResource(R.color.darkTextColor),
                        actionIconContentColor = colorResource(R.color.darkTextColor)
                    )
                )
            }
        ) { innerPadding ->
            if (showClearHistoryDialog) {
                AlertDialog(
                    onDismissRequest = { showClearHistoryDialog = false },
                    title = { Text("Clear All History") },
                    text = { Text("Are you sure you want to delete all match history? This action cannot be undone.") },
                    confirmButton = {
                        Button(onClick = {
                            historyViewModel.clearAllHistory()
                            showClearHistoryDialog = false
                        }) { Text("Clear All") }
                    },
                    dismissButton = {
                        Button(onClick = { showClearHistoryDialog = false }) { Text("Cancel") }
                    }
                )
            }

            if (showInfoDialog) {
                AlertDialog(
                    onDismissRequest = { showInfoDialog = false },
                    title = { Text(text = infoDialogTitle) },
                    text = { Text(text = infoDialogMessage) },
                    confirmButton = { Button(onClick = { showInfoDialog = false }) { Text("OK") } }
                )
            }

            when (selectedItemIndex) {
                0 -> {
                    val viewModel: NormalTicTacToeViewModel =
                        viewModel(factory = LocalViewModelFactory.current)
                    NormalTicTacToePage(innerPadding = innerPadding, viewModel = viewModel)
                }

                1 -> {
                    val infiniteViewModel: InfiniteTicTacToeViewModel =
                        viewModel(factory = LocalViewModelFactory.current)
                    InfiniteTicTacToePage(innerPadding, infiniteViewModel)
                }

                gameHistoryItemIndex -> {
                    NavHost(
                        navController = navController,
                        startDestination = "game_history_screen/history",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(
                            route = "game_history_screen/{initialTab}",
                            arguments = listOf(navArgument("initialTab") {
                                type = NavType.StringType
                            })
                        ) { backStackEntry ->
                            val initialTab =
                                backStackEntry.arguments?.getString("initialTab") ?: "history"
                            GameHistoryScreen(
                                mainNavController = navController,
                                initialTab = initialTab
                            )
                        }
                        composable(
                            route = "match_details/{matchId}",
                            arguments = listOf(navArgument("matchId") { type = NavType.LongType })
                        ) {
                            MatchDetailsPage(
                                innerPadding = innerPadding,
                                navController = navController
                            )
                        }
                        composable(
                            route = "roundReplay/{matchId}/{roundId}/{gameType}",
                            arguments = listOf(
                                navArgument("matchId") { type = NavType.LongType },
                                navArgument("roundId") { type = NavType.LongType },
                                navArgument("gameType") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val matchId = backStackEntry.arguments?.getLong("matchId") ?: 0L
                            val roundId = backStackEntry.arguments?.getLong("roundId") ?: 0L
                            RoundReplayScreen(
                                navController = navController,
                                matchId = matchId,
                                roundId = roundId
                            )
                        }
                    }
                }

                3 -> SettingsPage(innerPadding = innerPadding)
                4 -> HelpPage(innerPadding = innerPadding)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.R)
@Preview
@Composable
fun MainPagePreview() {
    val context = LocalContext.current
    val soundManager = SoundManager(context)
    val dummyAppDatabase = AppDatabase.getDatabase(context.applicationContext)
    val previewViewModelFactory = TicTacToeViewModelFactory(soundManager, dummyAppDatabase)
    CompositionLocalProvider(LocalViewModelFactory provides previewViewModelFactory) {
        MainPage()
    }
}

@Composable
fun DrawerHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp), contentAlignment = Alignment.Center
    ) {
        Text("Tic Tac Toe", style = MaterialTheme.typography.labelMedium, fontSize = 18.sp)
    }
}

@Composable
fun DrawerFooter() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            "Version ${stringResource(R.string.app_version)}",
            style = MaterialTheme.typography.labelMedium
        )
    }
}
