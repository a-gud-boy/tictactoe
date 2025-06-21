package com.a_gud_boy.tictactoe

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.PeopleAlt // for people_alt
import androidx.compose.material.icons.filled.Public // for public
import androidx.compose.material.icons.filled.SmartToy // for smart_toy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

// Color definitions from HTML for GameSetupScreen
val gameSetupPrimaryColor = Color(0xFFDCE8F3) // --primary-color (used as a background in html for body)
val gameSetupSecondaryColor = Color(0xFFF9FAFB) // --secondary-color (card background)
val gameSetupTextPrimaryColor = Color(0xFF1F2937) // --text-primary (headings, main text)
val gameSetupTextSecondaryColor = Color(0xFF6B7280) // --text-secondary (subtitle text)
val gameSetupAccentColor = Color(0xFF3B82F6) // --accent-color (interactive elements, selection)
val gameSetupBorderColor = Color(0xFFE5E7EB) // --border-color (default border)
val gameSetupBlue50 = Color(0xFFEFF6FF) // For selected card/label background (bg-blue-50)
// val gameSetupSlate50 = Color(0xFFF8FAFC) // For hover on game-type-label (bg-slate-50) - Hover handled by clickable

// Sample state for preview - will be moved to state management step
private const val ONLINE_MODE = "Online"
private const val OFFLINE_MODE = "Offline"
private const val AI_MODE = "AI"
private const val NORMAL_TYPE = "Normal"
private const val INFINITE_TYPE = "Infinite"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameSetupScreen(navController: NavController) {
    // Dummy state holders for now, will be properly implemented in the state management step
    var selectedGameMode by remember { mutableStateOf(ONLINE_MODE) }
    var selectedGameType by remember { mutableStateOf(NORMAL_TYPE) }

    Scaffold(
        containerColor = Color.White, // body bg-white
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Tic Tac Toe",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        color = gameSetupTextPrimaryColor,
                        fontSize = 20.sp, // text-xl
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Filled.ArrowBackIosNew,
                            contentDescription = "Back",
                            tint = gameSetupTextPrimaryColor
                        )
                    }
                },
                actions = {
                    // Spacer to balance the title since back button takes space
                    Spacer(modifier = Modifier.width(48.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    scrolledContainerColor = Color.White,
                    navigationIconContentColor = gameSetupTextPrimaryColor,
                    titleContentColor = gameSetupTextPrimaryColor,
                ),
                modifier = Modifier.border(BorderStroke(1.dp, gameSetupBorderColor)) // border-b
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Color.White,
                modifier = Modifier.border(BorderStroke(1.dp, gameSetupBorderColor)), // border-t
                contentPadding = PaddingValues(16.dp) // p-5 from footer
            ) {
                Button(
                    onClick = { /* TODO: Start game logic */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp), // h-14
                    colors = ButtonDefaults.buttonColors(containerColor = gameSetupAccentColor, contentColor = Color.White),
                    shape = RoundedCornerShape(50) // rounded-full
                ) {
                    Text("Start Game", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White) // main bg, though body is white
                .padding(20.dp) // p-5 for main
                .let { it }, // Added to resolve linting error for unused it
            verticalArrangement = Arrangement.spacedBy(32.dp) // space-y-8
        ) {
            // Game Mode Section
            Column {
                Text(
                    "Choose Game Mode",
                    color = gameSetupTextPrimaryColor,
                    fontSize = 24.sp, // text-2xl
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp) // mb-6
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp) // gap-4
                ) {
                    GameModeCard(
                        icon = Icons.Filled.Public,
                        title = ONLINE_MODE,
                        isSelected = selectedGameMode == ONLINE_MODE,
                        onClick = { selectedGameMode = ONLINE_MODE },
                        modifier = Modifier.weight(1f)
                    )
                    GameModeCard(
                        icon = Icons.Filled.PeopleAlt,
                        title = OFFLINE_MODE,
                        isSelected = selectedGameMode == OFFLINE_MODE,
                        onClick = { selectedGameMode = OFFLINE_MODE },
                        modifier = Modifier.weight(1f)
                    )
                    GameModeCard(
                        icon = Icons.Filled.SmartToy,
                        title = AI_MODE,
                        isSelected = selectedGameMode == AI_MODE,
                        onClick = { selectedGameMode = AI_MODE },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Game Type Section
            Column {
                Text(
                    "Select Game Type",
                    color = gameSetupTextPrimaryColor,
                    fontSize = 24.sp, // text-2xl
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 24.dp) // mb-6
                )
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) { // gap-4
                    GameTypeLabel(
                        title = NORMAL_TYPE,
                        description = "Classic Tic Tac Toe with a 3x3 grid.",
                        icon = Icons.Filled.Add, // Placeholder icon
                        isSelected = selectedGameType == NORMAL_TYPE,
                        onClick = { selectedGameType = NORMAL_TYPE }
                    )
                    GameTypeLabel(
                        title = INFINITE_TYPE,
                        description = "Play on an infinite grid, first to 5 in a row wins.",
                        icon = Icons.Filled.Add, // Placeholder icon
                        isSelected = selectedGameType == INFINITE_TYPE,
                        onClick = { selectedGameType = INFINITE_TYPE }
                    )
                }
            }
        }
    }
}

@Composable
fun GameModeCard(
    icon: ImageVector,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) gameSetupAccentColor else gameSetupBorderColor
    val backgroundColor = if (isSelected) gameSetupBlue50 else gameSetupSecondaryColor
    val borderWidth = if (isSelected) 2.dp else 1.dp

    Surface( // Using Surface for better control over shape, border, background
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp)) // rounded-2xl
            .border(BorderStroke(borderWidth, borderColor), RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp), // Ensure shape is applied for click ripple if any
        color = backgroundColor, // Explicitly set surface color
        border = BorderStroke(borderWidth, borderColor)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(24.dp) // p-6
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = gameSetupAccentColor,
                modifier = Modifier.size(48.dp) // game-mode-icon text-5xl
            )
            Spacer(modifier = Modifier.height(12.dp)) // gap-3
            Text(
                text = title,
                color = gameSetupTextPrimaryColor,
                fontSize = 18.sp, // text-lg
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

import androidx.compose.material.icons.filled.Add // Import for placeholder icon

@Composable
fun GameTypeLabel(
    title: String,
    description: String,
    icon: ImageVector, // Added icon parameter
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) gameSetupAccentColor else gameSetupBorderColor
    val backgroundColor = if (isSelected) gameSetupBlue50 else Color.Transparent // Or gameSetupSecondaryColor if a bg is always desired
    val borderWidth = if (isSelected) 2.dp else 1.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)) // rounded-xl
            .border(BorderStroke(borderWidth, borderColor), RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(20.dp), // p-5
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null, // Decorative, title provides context
            tint = gameSetupAccentColor, // Or gameSetupTextPrimaryColor if preferred
            modifier = Modifier.size(24.dp) // Adjust size as needed
        )
        Spacer(modifier = Modifier.width(16.dp)) // Spacing between icon and text
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = gameSetupTextPrimaryColor,
                fontSize = 16.sp, // text-base
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                color = gameSetupTextSecondaryColor,
                fontSize = 14.sp // text-sm
            )
        }
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = gameSetupAccentColor,
                unselectedColor = gameSetupBorderColor
            )
        )
    }
}


@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
fun GameSetupScreenPreview() {
    MaterialTheme { // Added MaterialTheme for better preview of Material components
        GameSetupScreen(navController = rememberNavController())
    }
}
