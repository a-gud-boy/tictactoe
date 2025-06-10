package com.a_gud_boy.tictactoe

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
fun MainPage(viewModelFactory: TicTacToeViewModelFactory) {
    var showMenu by rememberSaveable { mutableStateOf(false) }
    var showInfiniteMenu by rememberSaveable { mutableStateOf(false) } // Added for Infinite Mode Menu

    val drawerState = rememberDrawerState(
        initialValue = DrawerValue.Closed
    )

    val scope = rememberCoroutineScope()

    var selectedItemIndex by rememberSaveable { mutableIntStateOf(0) }
    val items = listOf("Normal TicTacToe", "Infinite TicTacToe")

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
                                    selectedItemIndex = index
                                    // Potentially navigate to a new screen here
                                    // And close the drawer
                                    scope.launch {
                                        drawerState.close()
                                    }
                                },
                                // icon = { Icon( /* ... */ ) } // Optional icon
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
                TopAppBar(
                    title = {
                        Text(
                            if (selectedItemIndex == 0) "Tic Tac Toe" else "Infinite Tic Tac Toe",
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
                        if (selectedItemIndex == 0) { // Only show for Normal TicTacToe
                            val viewModel: NormalTicTacToeViewModel =
                                viewModel(factory = viewModelFactory)
                            val isAIMode by viewModel.isAIMode.collectAsState()
                            val currentDifficulty by viewModel.aiDifficulty.collectAsState()

                            Box {
                                IconButton(onClick = { showMenu = true }) {
                                    Icon(Icons.Filled.MoreVert, contentDescription = "Settings")
                                }
                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("Play vs AI", Modifier.padding(end = 5.dp))
                                                Switch(
                                                    checked = isAIMode,
                                                    onCheckedChange = {
                                                        viewModel.setAIMode(it)
                                                    }
                                                )
                                            }
                                        },
                                        onClick = { }  // Click is handled by the Switch
                                    )

                                    if (isAIMode) {
                                        AIDifficulty.entries.forEach { difficulty ->
                                            DropdownMenuItem(
                                                text = { Text(difficulty.name) },
                                                onClick = {
                                                    viewModel.setAIDifficulty(difficulty)
                                                    showMenu = false
                                                },
                                                trailingIcon = {
                                                    if (difficulty == currentDifficulty) {
                                                        Icon(
                                                            Icons.Default.Check,
                                                            contentDescription = "Selected"
                                                        )
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        } else if (selectedItemIndex == 1) { // Menu for Infinite TicTacToe
                            val infiniteViewModel: InfiniteTicTacToeViewModel =
                                viewModel(factory = viewModelFactory)
                            val isAIMode by infiniteViewModel.isAIMode.collectAsState()
                            val currentDifficulty by infiniteViewModel.aiDifficulty.collectAsState()

                            Box {
                                IconButton(onClick = { showInfiniteMenu = true }) {
                                    Icon(
                                        Icons.Filled.MoreVert,
                                        contentDescription = "Infinite Settings"
                                    )
                                }
                                DropdownMenu(
                                    expanded = showInfiniteMenu,
                                    onDismissRequest = { showInfiniteMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("Play vs AI", Modifier.padding(end = 5.dp))
                                                Switch(
                                                    checked = isAIMode,
                                                    onCheckedChange = {
                                                        infiniteViewModel.setAIMode(it)
                                                    }
                                                )
                                            }
                                        },
                                        onClick = { } // Click is handled by the Switch
                                    )

                                    if (isAIMode) {
                                        AIDifficulty.entries.forEach { difficulty ->
                                            DropdownMenuItem(
                                                text = { Text(difficulty.name) },
                                                onClick = {
                                                    infiniteViewModel.setAIDifficulty(difficulty)
                                                    showInfiniteMenu = false
                                                },
                                                trailingIcon = {
                                                    if (difficulty == currentDifficulty) {
                                                        Icon(
                                                            Icons.Default.Check,
                                                            contentDescription = "Selected"
                                                        )
                                                    }
                                                }
                                            )
                                        }
                                    }
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
            }) { innerPadding ->
            when (selectedItemIndex) {
                0 -> {
                    val viewModel: NormalTicTacToeViewModel = viewModel(factory = viewModelFactory)
                    NormalTicTacToePage(
                        innerPadding = innerPadding,
                        viewModel = viewModel
                    )
                }

                1 -> {
                    val infiniteViewModel: InfiniteTicTacToeViewModel =
                        viewModel(factory = viewModelFactory) // ensure viewmodel is available for the page
                    InfiniteTicTacToePage(innerPadding, infiniteViewModel)
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
    MainPage(viewModelFactory = TicTacToeViewModelFactory(SoundManager(LocalContext.current)))
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





