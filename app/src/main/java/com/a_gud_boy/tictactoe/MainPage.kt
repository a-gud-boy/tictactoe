package com.a_gud_boy.tictactoe

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues // Added import
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
import androidx.compose.runtime.LaunchedEffect
import com.google.firebase.auth.FirebaseAuth
import android.util.Log // For logging
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.filled.AccountCircle // Icon for Online Multiplayer
import androidx.navigation.NavHostController
import com.a_gud_boy.tictactoe.OnlineLobbyScreen // Added import
import com.a_gud_boy.tictactoe.OnlineGameScreen // Added import

@RequiresApi(Build.VERSION_CODES.R)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPage() {
    val historyViewModel: HistoryViewModel = viewModel(factory = LocalViewModelFactory.current)

    var showInfoDialog by rememberSaveable { mutableStateOf(false) }
    var infoDialogTitle by rememberSaveable { mutableStateOf("") }
    var infoDialogMessage by rememberSaveable { mutableStateOf("") }
    var showClearHistoryDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteMatchDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteRoundDialog by rememberSaveable { mutableStateOf(false) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedItemIndex by rememberSaveable { mutableIntStateOf(0) } // Used for drawer item selection state

    val items = listOf(
        "Normal TicTacToe",     // Index 0
        "Infinite TicTacToe", // Index 1
        "Online Multiplayer",   // Index 2
        "Game History",         // Index 3
        "Settings",             // Index 4
        "Help"                  // Index 5
    )
    // Route constants for navigation
    val routeNormalTicTacToe = "normal_tictactoe"
    val routeInfiniteTicTacToe = "infinite_tictactoe"
    val routeOnlineLobby = "online_lobby"
    val routeGameHistoryWrapper = "game_history_wrapper"
    val routeSettings = "settings"
    val routeHelp = "help"

    val navController = rememberNavController()

    LaunchedEffect(Unit) {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            auth.signInAnonymously()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("MainPageAuth", "signInAnonymously:success. User ID: ${auth.currentUser?.uid}")
                    } else {
                        Log.w("MainPageAuth", "signInAnonymously:failure", task.exception)
                    }
                }
        } else {
            Log.d("MainPageAuth", "User already signed in. User ID: ${auth.currentUser?.uid}")
        }
    }

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet {
                Column(modifier = Modifier.fillMaxHeight()) {
                    DrawerHeader()
                    Column {
                        items.forEachIndexed { index, itemText ->
                            val route = when (index) {
                                0 -> routeNormalTicTacToe
                                1 -> routeInfiniteTicTacToe
                                2 -> routeOnlineLobby
                                3 -> routeGameHistoryWrapper
                                4 -> routeSettings
                                5 -> routeHelp
                                else -> routeNormalTicTacToe
                            }
                            NavigationDrawerItem(
                                label = { Text(itemText) },
                                selected = index == selectedItemIndex,
                                onClick = {
                                    selectedItemIndex = index
                                    scope.launch { drawerState.close() }
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                                colors = NavigationDrawerItemDefaults.colors(
                                    unselectedContainerColor = Color.Transparent,
                                    selectedContainerColor = if (index == selectedItemIndex) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f) else Color.Transparent,
                                    selectedTextColor = Color.Black
                                ),
                                icon = {
                                    when (index) {
                                        0 -> Icon(painterResource(R.drawable.normal_tic_tac_toe), "Normal Tic Tac Toe", modifier = Modifier.size(30.dp))
                                        1 -> Icon(painterResource(R.drawable.infinite_tic_tac_toe), "Infinite Tic Tac Toe", modifier = Modifier.size(30.dp))
                                        2 -> Icon(Icons.Filled.AccountCircle, "Online Multiplayer", modifier = Modifier.size(30.dp))
                                        3 -> Icon(Icons.Filled.Build, "Game History", modifier = Modifier.size(30.dp))
                                        4 -> Icon(Icons.Filled.Settings, "Settings", modifier = Modifier.size(30.dp))
                                        5 -> Icon(Icons.Outlined.Info, "Help", modifier = Modifier.size(30.dp))
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
                val currentRoute = navBackStackEntry?.destination?.route

                val titleText = when (currentRoute) {
                    routeNormalTicTacToe -> "Tic Tac Toe"
                    routeInfiniteTicTacToe -> "Infinite TicTacToe"
                    routeOnlineLobby -> "Online Multiplayer"
                    routeGameHistoryWrapper -> "Game History"
                    routeSettings -> "Settings"
                    routeHelp -> "Help"
                    else -> { 
                        if (currentRoute?.startsWith("match_details/") == true) "Match Details"
                        else if (currentRoute?.startsWith("roundReplay/") == true) "Match Replay"
                        else if (currentRoute?.startsWith("online_game/") == true) "Online Game"
                        else "Tic Tac Toe" 
                    }
                }

                TopAppBar(
                    title = {
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
                        val canPop = navController.previousBackStackEntry != null
                        if (canPop && currentRoute != routeNormalTicTacToe && currentRoute != routeInfiniteTicTacToe && currentRoute != routeOnlineLobby && currentRoute != routeGameHistoryWrapper && currentRoute != routeSettings && currentRoute != routeHelp ) { 
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        } else {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Filled.Menu, contentDescription = "Menu Icon")
                            }
                        }
                    },
                    actions = {
                        when (currentRoute) {
                            routeGameHistoryWrapper -> {
                                IconButton(onClick = { showClearHistoryDialog = true }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Clear All History")
                                }
                            }
                            routeNormalTicTacToe, routeInfiniteTicTacToe, routeSettings, routeHelp -> {
                                IconButton(onClick = {
                                    infoDialogTitle = when(currentRoute) {
                                        routeNormalTicTacToe -> "Normal Tic Tac Toe"
                                        routeInfiniteTicTacToe -> "Infinite Tic Tac Toe"
                                        routeSettings -> "Settings"
                                        routeHelp -> "Help"
                                        else -> ""
                                    }
                                    infoDialogMessage = when(currentRoute) {
                                        routeNormalTicTacToe -> "This is the classic Tic Tac Toe game..."
                                        routeInfiniteTicTacToe -> "A twist on the classic! Marks disappear..."
                                        routeSettings -> "Here you can configure various application settings..."
                                        routeHelp -> "Welcome to Tic Tac Toe!..."
                                        else -> ""
                                    }
                                    showInfoDialog = true
                                }) {
                                    Icon(Icons.Outlined.Info, contentDescription = "Information")
                                }
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
            val currentNavBackStackEntry by navController.currentBackStackEntryAsState() 

            if (showDeleteMatchDialog) {
                val matchIdFromArgs = currentNavBackStackEntry?.arguments?.getLong("matchId")
                val matchDetailsViewModelInstance: MatchDetailsViewModel? = if (matchIdFromArgs != null && currentNavBackStackEntry != null) {
                     if (currentNavBackStackEntry?.destination?.route == "match_details/{matchId}") {
                         viewModel(
                            viewModelStoreOwner = currentNavBackStackEntry!!, 
                            factory = LocalViewModelFactory.current,
                            key = "match_details_vm_$matchIdFromArgs"
                        )
                    } else null
                } else null

                AlertDialog(
                    onDismissRequest = { showDeleteMatchDialog = false },
                    title = { Text("Delete Match") },
                    text = { Text("Are you sure you want to delete this match? This action cannot be undone.") },
                    confirmButton = {
                        Button(onClick = {
                            matchDetailsViewModelInstance?.deleteMatch()
                            if(navController.currentDestination?.route?.startsWith("game_history_wrapper") == true) {
                                navController.popBackStack()
                            }
                            showDeleteMatchDialog = false
                        }) { Text("Delete") }
                    },
                    dismissButton = { Button(onClick = { showDeleteMatchDialog = false }) { Text("Cancel") } }
                )
            }

            if (showDeleteRoundDialog) {
                val roundIdFromArgs = currentNavBackStackEntry?.arguments?.getLong("roundId")
                 val roundReplayViewModelInstance: RoundReplayViewModel? = if (roundIdFromArgs != null && currentNavBackStackEntry != null) {
                    if (currentNavBackStackEntry?.destination?.route == "roundReplay/{matchId}/{roundId}/{gameType}") {
                        viewModel(
                            viewModelStoreOwner = currentNavBackStackEntry!!,
                            factory = LocalViewModelFactory.current,
                            key = "round_replay_vm_$roundIdFromArgs"
                        )
                    } else null
                } else null
                AlertDialog(
                    onDismissRequest = { showDeleteRoundDialog = false },
                    title = { Text("Delete Round") },
                    text = { Text("Are you sure you want to delete this round's data?") },
                    confirmButton = {
                        Button(onClick = {
                            roundReplayViewModelInstance?.deleteRound()
                            navController.popBackStack()
                            showDeleteRoundDialog = false
                        }) { Text("Delete") }
                    },
                    dismissButton = { Button(onClick = { showDeleteRoundDialog = false }) { Text("Cancel") } }
                )
            }

            if (showClearHistoryDialog) {
                AlertDialog(
                    onDismissRequest = { showClearHistoryDialog = false },
                    title = { Text("Clear All History") },
                    text = { Text("Are you sure you want to delete all match history?") },
                    confirmButton = {
                        Button(onClick = {
                            historyViewModel.clearAllHistory() 
                            showClearHistoryDialog = false
                        }) { Text("Clear All") }
                    },
                    dismissButton = { Button(onClick = { showClearHistoryDialog = false }) { Text("Cancel") } }
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

            NavHost(
                navController = navController,
                startDestination = routeNormalTicTacToe
            ) {
                composable(routeNormalTicTacToe) {
                    val normalViewModel: NormalTicTacToeViewModel = viewModel(factory = LocalViewModelFactory.current)
                    NormalTicTacToePage(innerPadding = innerPadding, viewModel = normalViewModel)
                }
                composable(routeInfiniteTicTacToe) {
                    val infiniteViewModel: InfiniteTicTacToeViewModel = viewModel(factory = LocalViewModelFactory.current)
                    InfiniteTicTacToePage(innerPadding = innerPadding, viewModel = infiniteViewModel)
                }
                composable(routeOnlineLobby) { 
                    OnlineLobbyScreen(
                        innerPadding = innerPadding,
                        onNavigateToGame = { gameId ->
                            navController.navigate("online_game/$gameId")
                        }
                    )
                }
                composable(
                    route = "online_game/{gameId}",
                    arguments = listOf(navArgument("gameId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val gameId = backStackEntry.arguments?.getString("gameId")
                    if (gameId != null) {
                        OnlineGameScreen(
                            gameId = gameId,
                            navController = navController
                        ) // Correctly passing navController
                    } else {
                        Text(
                            "Error: Game ID missing. Cannot load game.",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                composable(routeGameHistoryWrapper) {
                    val gameHistoryNavController = rememberNavController()
                    GameHistoryWrapper(
                        paddingValues = innerPadding,
                        mainNavController = navController, 
                        gameHistoryNavController = gameHistoryNavController,
                        showClearHistoryDialog = { showClearHistoryDialog = true },
                        showDeleteMatchDialog = { matchId ->
                            showDeleteMatchDialog = true
                        },
                        showDeleteRoundDialog = { roundId ->
                            showDeleteRoundDialog = true
                        }
                    )
                }
                composable(routeSettings) {
                    SettingsPage(innerPadding = innerPadding)
                }
                composable(routeHelp) {
                    HelpPage(innerPadding = innerPadding)
                }
            }
        }
    }
}

// Wrapper Composable for Game History's nested navigation
@Composable
fun GameHistoryWrapper(
    paddingValues: PaddingValues,
    mainNavController: NavHostController, 
    gameHistoryNavController: NavHostController,
    showClearHistoryDialog: () -> Unit, 
    showDeleteMatchDialog: (Long) -> Unit,
    showDeleteRoundDialog: (Long) -> Unit
) {
    NavHost(
        navController = gameHistoryNavController,
        startDestination = "game_history_screen/history",
        modifier = Modifier 
    ) {
        composable(
            route = "game_history_screen/{initialTab}",
            arguments = listOf(navArgument("initialTab") { type = NavType.StringType })
        ) { backStackEntry ->
            val initialTab = backStackEntry.arguments?.getString("initialTab") ?: "history"
            GameHistoryScreen(
                paddingValues,
                mainNavController = gameHistoryNavController, 
                initialTab = initialTab
            )
        }
        composable(
            route = "match_details/{matchId}",
            arguments = listOf(navArgument("matchId") { type = NavType.LongType })
        ) {
            MatchDetailsPage(
                innerPadding = PaddingValues(), 
                navController = gameHistoryNavController 
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
                navController = gameHistoryNavController, 
                matchId = matchId,
                roundId = roundId
            )
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
