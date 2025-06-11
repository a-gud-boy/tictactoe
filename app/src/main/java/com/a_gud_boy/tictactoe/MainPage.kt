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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete // Ensure Delete is imported
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import androidx.navigation.NavType // Added
import androidx.navigation.compose.NavHost // Added
import androidx.navigation.compose.composable // Added
import androidx.navigation.compose.rememberNavController // Added
import androidx.navigation.navArgument // Added
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
    var showClearHistoryDialog by rememberSaveable { mutableStateOf(false) } // Added state for clear history dialog


    val drawerState = rememberDrawerState(
        initialValue = DrawerValue.Closed
    )

    val scope = rememberCoroutineScope()

    var selectedItemIndex by rememberSaveable { mutableIntStateOf(0) }
    // Add "History" to the list of items for the drawer
    // Order: Normal, Infinite, History, Settings, Help
    val items = listOf("Normal TicTacToe", "Infinite TicTacToe", "History", "Settings", "Help")

    val navController = rememberNavController() // NavController for History section

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
                                    if (index == 2) { // History item
                                        if (selectedItemIndex == 2) { // Already on History section
                                            // If on match_details, pop back to history_list
                                            if (navController.currentBackStackEntry?.destination?.route != "history_list") {
                                                navController.navigate("history_list") {
                                                    popUpTo(navController.graph.startDestinationId) {
                                                        inclusive = true
                                                    }
                                                }
                                            }
                                            // If already on history_list, do nothing extra, just close drawer.
                                        } else {
                                            selectedItemIndex = index // Switch to History section
                                            // NavHost will show "history_list" by default when selectedItemIndex becomes 2
                                        }
                                    } else {
                                        selectedItemIndex = index
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
                                    } else if (index == 2) { // History
                                        Icon(
                                            Icons.Filled.Build,
                                            contentDescription = "Navigation Icon for History",
                                            modifier = Modifier.size(30.dp)
                                        )
                                    } else if (index == 3) { // Settings
                                        Icon(
                                            Icons.Filled.Settings,
                                            contentDescription = "Navigation Icon for Settings",
                                            modifier = Modifier.size(30.dp)
                                        )
                                    } else if (index == 4) { // Help
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
                // TopAppBar is now always visible, title and actions adapt based on selectedItemIndex
                TopAppBar(
                    title = {
                        Text(
                            when (selectedItemIndex) {
                                0 -> "Tic Tac Toe"
                                1 -> "Infinite Tic Tac Toe"
                                2 -> "History" // Title for History when MainPage's TopAppBar is shown
                                3 -> "Settings"
                                4 -> "Help"
                                else -> "Lorem Ipsum"
                            },
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelMedium,
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu Icon")
                        }
                    },
                    actions = {
                        if (selectedItemIndex == 2) { // History Actions
                            IconButton(onClick = { showClearHistoryDialog = true }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Clear History")
                            }
                            IconButton(onClick = {
                                infoDialogTitle = "Match History"
                                infoDialogMessage = "View your past matches, including scores, rounds, and individual moves. You can also clear all history from this page."
                                showInfoDialog = true
                            }) {
                                Icon(Icons.Outlined.Info, contentDescription = "Information")
                            }
                        } else { // Actions for other pages
                            IconButton(onClick = {
                                when (selectedItemIndex) {
                                    0 -> { // Normal TicTacToe
                                        infoDialogTitle = "Normal Tic Tac Toe"
                                        infoDialogMessage =
                                            "This is the classic Tic Tac Toe game. Get three of your marks in a row (horizontally, vertically, or diagonally) to win. Player X goes first."
                                    }
                                    1 -> { // Infinite TicTacToe
                                        infoDialogTitle = "Infinite Tic Tac Toe"
                                        infoDialogMessage =
                                            "A twist on the classic! Marks disappear after 3 subsequent moves by any player. Strategy is key as the board constantly changes. Get three of your marks in a row to win."
                                    }
                                    3 -> { // Settings
                                        infoDialogTitle = "Settings"
                                        infoDialogMessage =
                                            "Here you can configure various application settings:\n" +
                                                    "- Sound: Toggle game sounds on or off.\n" +
                                                    "- Haptic Feedback: Toggle vibrational feedback on or off.\n" +
                                                    "- AI Mode: Enable or disable playing against the AI.\n" +
                                                    "- AI Difficulty: Adjust the AI's skill level when AI mode is enabled."
                                    }
                                    4 -> { // Help
                                        infoDialogTitle = "Help"
                                        infoDialogMessage = "Welcome to Tic Tac Toe!\n\n" +
                                                "- Navigation: Use the drawer menu (swipe from left or tap the menu icon) to switch between Normal Tic Tac Toe, Infinite Tic Tac Toe, Settings, and this Help page.\n" +
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
            }) { innerPadding ->

            if (showInfoDialog) {
            // The rest of the Scaffold content (AlertDialog and when(selectedItemIndex) block) remains the same for now
            // The when(selectedItemIndex) block will be modified in the next step to include the NavHost
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

            when (selectedItemIndex) {
                0 -> {
                    // Use LocalViewModelFactory.current, defined in MainActivity.kt
                    val viewModel: NormalTicTacToeViewModel =
                        viewModel(factory = LocalViewModelFactory.current)
                    NormalTicTacToePage(
                        innerPadding = innerPadding,
                        viewModel = viewModel
                    )
                }

                1 -> {
                    val infiniteViewModel: InfiniteTicTacToeViewModel =
                        viewModel(factory = LocalViewModelFactory.current) // Use LocalViewModelFactory
                    InfiniteTicTacToePage(innerPadding, infiniteViewModel)
                }

                2 -> { // History Page uses NavHost now
                    // Modifier.padding(innerPadding) is applied to the NavHost
                    // so that the NavHost itself is placed correctly within MainPage's Scaffold content area.
                    // When selectedItemIndex == 2, MainPage's TopAppBar is hidden, so innerPadding.top should be 0.
                    // HistoryPage and MatchDetailsPage use their own Scaffolds and will handle their own internal padding.
                    NavHost(navController = navController, startDestination = "history_list", modifier = Modifier.padding(innerPadding)) {
                        composable("history_list") {
                            HistoryPage(
                                innerPadding = innerPadding, // PASS MainPage's Scaffold innerPadding
                                showClearConfirmDialog = showClearHistoryDialog,
                                onShowClearConfirmDialogChange = { showClearHistoryDialog = it },
                                navController = navController
                                // onShowInfoDialog lambda is removed as Info button is now in MainPage's TopAppBar
                            )
                        }
                        composable(
                            route = "match_details/{matchId}",
                            arguments = listOf(navArgument("matchId") { type = NavType.LongType })
                        ) { // backStackEntry is implicitly available from the lambda
                            // MatchDetailsPage also has its own Scaffold.
                            MatchDetailsPage(navController = navController)
                        }
                    }
                }

                3 -> { // Settings
                    SettingsPage(innerPadding = innerPadding)
                }

                4 -> { // Help
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





