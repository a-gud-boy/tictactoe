package com.a_gud_boy.tictactoe

// NavType and navArgument are already imported or covered by the above
// Ensure RoundReplayScreen is imported if not in the same package,
// but it should be in com.a_gud_boy.tictactoe
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
import androidx.compose.material.icons.automirrored.outlined.List // Will be unused if Statistics item removed, keep for now
import androidx.compose.material.icons.filled.Build // Icon for Game History
import androidx.compose.material.icons.filled.Delete // To be removed from TopAppBar actions here
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
// import androidx.compose.material.icons.filled.Analytics // No longer needed for BottomNav
// import androidx.compose.material.icons.filled.List as FilledList // No longer needed for BottomNav
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
// import androidx.compose.material.BottomNavigation // Removed
// import androidx.compose.material.BottomNavigationItem // Removed
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect // For history_list redirect, if kept
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

/**
 * The main composable function that sets up the application's UI structure.
 *
 * This function creates a [ModalNavigationDrawer] which allows users to switch between
 * different Tic Tac Toe game modes ("Normal TicTacToe" and "Infinite TicTacToe").
 * It uses a [Scaffold] to provide a standard layout structure, including a [TopAppBar]
 * and the main content area where the selected game mode is displayed.
 *
 * The current game mode is determined by `selectedItemIndex`, which is persisted across
 * configuration changes using `rememberSaveable`.
 * The [TopAppBar] displays the title of the currently selected game and provides a navigation
 * icon to open the drawer and an action icon for settings (currently shows a Toast).
 */
@RequiresApi(Build.VERSION_CODES.R)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPage() { // Removed viewModelFactory parameter
    // var showMenu by rememberSaveable { mutableStateOf(false) } // No longer needed for MoreVert
    // var showInfiniteMenu by rememberSaveable { mutableStateOf(false) } // No longer needed for MoreVert

    var showInfoDialog by rememberSaveable { mutableStateOf(false) }
    var infoDialogTitle by rememberSaveable { mutableStateOf("") }
    var infoDialogMessage by rememberSaveable { mutableStateOf("") }
    var showClearHistoryDialog by rememberSaveable { mutableStateOf(false) } // This state will likely move to GameHistoryScreen or be managed there

    // State for Bottom Navigation - REMOVED
    // var selectedBottomTab by remember { mutableStateOf("History") }

    val drawerState = rememberDrawerState(
        initialValue = DrawerValue.Closed
    )

    val scope = rememberCoroutineScope()

    var selectedItemIndex by rememberSaveable { mutableIntStateOf(0) }
    // Updated drawer items
    val items = listOf(
        "Normal TicTacToe",
        "Infinite TicTacToe",
        "Game History", // Replaces Statistics and History
        "Settings",
        "Help"
    )

    val navController = rememberNavController() // NavController for the NavHost handling GameHistoryScreen, MatchDetails, etc.

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier.fillMaxHeight()
                ) {
                    DrawerHeader()
                    Column {
                        items.forEachIndexed { index, itemText ->
                            NavigationDrawerItem(
                                label = { Text(itemText) },
                                selected = index == selectedItemIndex,
                                onClick = {
                                    // New indices: 0: Normal, 1: Infinite, 2: Game History, 3: Settings, 4: Help
                                    if (index == 2) { // Game History
                                        if (selectedItemIndex == 2) { // Already on Game History section
                                            // If on a sub-page of Game History (e.g. match_details, roundReplay),
                                            // navigate to the main Game History screen (defaulting to history tab).
                                            val currentRoute = navController.currentBackStackEntry?.destination?.route
                                            if (currentRoute != "game_history_screen/history" && currentRoute != "game_history_screen/stats") {
                                                navController.navigate("game_history_screen/history") {
                                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        } else {
                                            selectedItemIndex = index // Switch to Game History section
                                            // Navigate to the Game History screen, defaulting to the 'history' tab.
                                            // Clear back stack of the NavHost for a clean entry.
                                            navController.navigate("game_history_screen/history") {
                                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                                launchSingleTop = true
                                            }
                                        }
                                    } else {
                                        // For other items (Normal, Infinite, Settings, Help)
                                        selectedItemIndex = index
                                        // If navigating away from Game History, its NavHost's backstack is preserved.
                                    }
                                    scope.launch { drawerState.close() }
                                },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                                colors = NavigationDrawerItemDefaults.colors(
                                    // For unselected items
                                    unselectedContainerColor = Color.Transparent,
                                    // For selected items (you might want to keep a highlight or also make it transparent)
                                    selectedContainerColor = if (index == selectedItemIndex) MaterialTheme.colorScheme.primaryContainer.copy(
                                        alpha = 0.1f
                                    ) else Color.Transparent,
                                    // You can also customize:
                                    // unselectedIconColor = ...,
                                    // unselectedTextColor = ...,
                                    // selectedIconColor = ...,
                                    selectedTextColor = Color.Black
                                ),
                                icon = {
                                    if (index == 0) {
                                        Icon(
                                            painterResource(R.drawable.normal_tic_tac_toe),
                                            contentDescription = "Navigation Icon for Normal Tic Tac Toe",
                                            modifier = Modifier.size(30.dp)
                                        )
                                    } else if (index == 1) {
                                        Icon(
                                            painterResource(R.drawable.infinite_tic_tac_toe),
                                            contentDescription = "Navigation Icon for Infinite Tic Tac Toe",
                                            modifier = Modifier.size(30.dp)
                                        )
                                    } else if (index == 2) { // Game History (new index 2)
                                        Icon(
                                            Icons.Filled.Build, // Using Build icon; can be changed e.g. Icons.Filled.History
                                            contentDescription = "Navigation Icon for Game History",
                                            modifier = Modifier.size(30.dp)
                                        )
                                    } else if (index == 3) { // Settings (new index 3)
                                        Icon(
                                            Icons.Filled.Settings,
                                            contentDescription = "Navigation Icon for Settings",
                                            modifier = Modifier.size(30.dp)
                                        )
                                    } else if (index == 4) { // Help (new index 4)
                                        Icon(
                                            Icons.Outlined.Info,
                                            contentDescription = "Navigation Icon for Help",
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }
                                }
                            )
                        }
                    }
                    // Creates the free space
                    Spacer(modifier = Modifier.weight(1f))
                    DrawerFooter()
                }
            }
        },
        drawerState = drawerState
    ) {
        Scaffold(
            topBar = {
                // Hoist navBackStackEntry here to be used by title, navigationIcon, and actions
                val navBackStackEntry by navController.currentBackStackEntryAsState()

                // TopAppBar is now always visible, title and actions adapt based on selectedItemIndex
                TopAppBar(
                    title = {
                        val titleText by remember { // Outer remember for the derivedStateOf instance
                            derivedStateOf {
                                // Access the hoisted navBackStackEntry's value here
                                val currentRoute =
                                    navBackStackEntry?.destination?.route // Correct: uses hoisted state
                                when (selectedItemIndex) {
                                    0 -> "Tic Tac Toe" // Normal
                                    1 -> "Infinite TicTacToe" // Infinite
                                    2 -> "Game History" // Game History (new index 2)
                                    // Sub-page titles like "Match Details" will be handled by GameHistoryScreen's TopAppBar.
                                    // MainPage's TopAppBar will show "Game History" when selectedItemIndex is 2.
                                    3 -> "Settings" // Settings (new index 3)
                                    4 -> "Help" // Help (new index 4)
                                    else -> "Lorem Ipsum" // Default
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
                        // val navBackStackEntry by navController.currentBackStackEntryAsState() // Already hoisted
                        val currentRoute = navBackStackEntry?.destination?.route
                        // Back arrow logic for sub-pages of Game History (index 2)
                        if (selectedItemIndex == 2 &&
                            (currentRoute?.startsWith("match_details/") == true || currentRoute?.startsWith("roundReplay/") == true)
                        ) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        } else {
                            // Standard drawer menu icon
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Filled.Menu, contentDescription = "Menu Icon")
                            }
                        }
                    },
                    actions = {
                        // Removed history-specific actions (Delete, Info). These belong in GameHistoryScreen.
                        // Info buttons for Normal (0), Infinite (1), Settings (3), Help (4).
                        if (selectedItemIndex != 2) { // Not Game History page
                            IconButton(onClick = {
                                when (selectedItemIndex) {
                                    0 -> { // Normal TicTacToe
                                        infoDialogTitle = "Normal Tic Tac Toe"
                                        infoDialogMessage = "This is the classic Tic Tac Toe game. Get three of your marks in a row (horizontally, vertically, or diagonally) to win. Player X goes first."
                                    }
                                    1 -> { // Infinite TicTacToe
                                        infoDialogTitle = "Infinite Tic Tac Toe"
                                        infoDialogMessage = "A twist on the classic! Marks disappear after 3 subsequent moves by any player. Strategy is key as the board constantly changes. Get three of your marks in a row to win."
                                    }
                                    // selectedItemIndex == 2 (Game History) is handled by its own screen.
                                    3 -> { // Settings (new index 3)
                                        infoDialogTitle = "Settings"
                                        infoDialogMessage = "Here you can configure various application settings:\n" +
                                                "- Sound: Toggle game sounds on or off.\n" +
                                                "- Haptic Feedback: Toggle vibrational feedback on or off.\n" +
                                                "- AI Mode: Enable or disable playing against the AI.\n" +
                                                "- AI Difficulty: Adjust the AI's skill level when AI mode is enabled."
                                    }
                                    4 -> { // Help (new index 4)
                                        infoDialogTitle = "Help"
                                        infoDialogMessage = "Welcome to Tic Tac Toe!\n\n" +
                                                "- Navigation: Use the drawer menu (swipe from left or tap the menu icon) to switch between game modes, view Game History, Settings, and this Help page.\n" +
                                                "- Game Play: Follow on-screen instructions for each game mode.\n" +
                                                "- Settings: Customize your experience in the Settings page."
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
            // bottomBar = { ... } // REMOVED BottomNavigation
        ) { innerPadding ->

            // Removed Log.d call for PaddingDebug and associated topPaddingValue variable

            // General Info Dialog - This is triggered by the Info icon in TopAppBar for various pages
            if (showInfoDialog) {
                AlertDialog(
                    onDismissRequest = { showInfoDialog = false },
                    title = { Text(text = infoDialogTitle) },
                    text = { Text(text = infoDialogMessage) },
                    confirmButton = {
                        Button(onClick = { showInfoDialog = false }) {
                            Text("OK")
                        }
                    }
                )
            }

            // General Info Dialog - This is triggered by the Info icon in TopAppBar for various pages
            if (showInfoDialog) {
                AlertDialog(
                    onDismissRequest = { showInfoDialog = false },
                    title = { Text(text = infoDialogTitle) },
                    text = { Text(text = infoDialogMessage) },
                    confirmButton = {
                        Button(onClick = { showInfoDialog = false }) {
                            Text("OK")
                        }
                    }
                )
            }

            // Content switching based on drawer selection
            when (selectedItemIndex) {
                0 -> { // Normal TicTacToe
                    val viewModel: NormalTicTacToeViewModel = viewModel(factory = LocalViewModelFactory.current)
                    NormalTicTacToePage(innerPadding = innerPadding, viewModel = viewModel)
                }
                1 -> { // Infinite TicTacToe
                    val infiniteViewModel: InfiniteTicTacToeViewModel = viewModel(factory = LocalViewModelFactory.current)
                    InfiniteTicTacToePage(innerPadding, infiniteViewModel)
                }
                2 -> { // Game History (new index 2)
                    // This NavHost handles navigation for GameHistoryScreen and its sub-pages
                    NavHost(
                        navController = navController, // Use the main navController for this NavHost
                        startDestination = "game_history_screen/history", // Default to history tab
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(
                            route = "game_history_screen/{initialTab}",
                            arguments = listOf(navArgument("initialTab") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val initialTab = backStackEntry.arguments?.getString("initialTab") ?: "history"
                            GameHistoryScreen(
                                mainNavController = navController, // Pass this NavController to GameHistoryScreen
                                initialTab = initialTab
                            )
                        }
                        composable(
                            route = "match_details/{matchId}",
                            arguments = listOf(navArgument("matchId") { type = NavType.LongType })
                        ) {
                            MatchDetailsPage(
                                innerPadding = innerPadding, // This padding might be better handled by GameHistoryScreen
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
                        // The old "history_list" route is removed. GameHistoryScreen is the new entry.
                        // If direct navigation to "history_list" was possible, it should be updated
                        // to "game_history_screen/history". The LaunchedEffect for redirect is removed.
                    }
                }
                3 -> { // Settings (new index 3)
                    SettingsPage(innerPadding = innerPadding)
                }
                4 -> { // Help (new index 4)
                    HelpPage(innerPadding = innerPadding)
                }
            }
        }
    }

}

/**
 * A preview composable for the [MainPage] function.
 *
 * This allows for easy previewing of the [MainPage] layout and functionality within
 * Android Studio's Compose preview tool.
 */
@RequiresApi(Build.VERSION_CODES.R)
@Preview
@Composable
fun MainPagePreview() {
    // For preview, provide a dummy factory or a real one if simple enough
    val context = LocalContext.current
    val soundManager = SoundManager(context)
    // Dummy AppDatabase for preview - this might be complex if DAOs are called immediately.
    // For a simple preview, this might be okay if DAOs are not strictly needed for initial composition.
    // Consider a more robust test/preview setup if this becomes an issue.
    val dummyAppDatabase = AppDatabase.getDatabase(context.applicationContext) // Or a fake/mock
    val previewViewModelFactory = TicTacToeViewModelFactory(soundManager, dummyAppDatabase)

    CompositionLocalProvider(LocalViewModelFactory provides previewViewModelFactory) {
        MainPage()
    }
}

/**
 * A composable function that renders the header content for the navigation drawer.
 *
 * This typically includes the application title or logo.
 * Currently, it displays "Tic Tac Toe" centered within a Box.
 */
@Composable
fun DrawerHeader() {
    // Example header
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("Tic Tac Toe", style = MaterialTheme.typography.labelMedium, fontSize = 18.sp)
    }
}

/**
 * A composable function that renders the footer content for the navigation drawer.
 *
 * This can be used to display information like app version or other links.
 * Currently, it displays the application version string.
 */
@Composable
fun DrawerFooter() {
    // Example footer
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





