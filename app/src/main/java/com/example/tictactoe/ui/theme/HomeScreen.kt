package com.example.tictactoe.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.SportsEsports // Using SportsEsports as a proxy for stadia_controller
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Color definitions based on CSS variables
val primaryColor = Color(0xFF0C7FF2)
val textSecondaryColor = Color.White // --text-secondary: #ffffff;
val backgroundColor = Color(0xFFF0F2F5) // --background-color: #f0f2f5;
val textPrimaryColor = Color(0xFF111418) // --text-primary: #111418;
val slate600 = Color(0xFF475569) // Approx for text-slate-600
val slate300 = Color(0xFFCBD5E1) // Approx for border-slate-300

@Composable
fun HomeScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tic Tac Toe",
                        color = textPrimaryColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                backgroundColor = Color.White,
                elevation = 4.dp // shadow-sm
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply padding from Scaffold
                .padding(top = 32.dp, bottom = 64.dp, start = 16.dp, end = 16.dp), // pt-8 pb-16 px-4
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp), // mb-8
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.SportsEsports, // Using a similar available icon
                    contentDescription = "Game Controller Icon",
                    tint = primaryColor,
                    modifier = Modifier.size(72.dp) // text-6xl is roughly 72.dp (6 * 12)
                )
            }

            Text(
                text = "Welcome to Tic Tac Toe",
                color = textPrimaryColor,
                fontSize = 30.sp, // text-3xl
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 12.dp) // mb-3
            )

            Text(
                text = "Challenge your friends or play against random opponents in this classic game.",
                color = slate600, // text-slate-600
                fontSize = 18.sp, // text-lg
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 40.dp) // mb-10
            )

            Column(
                modifier = Modifier.fillMaxWidth(0.8f), // Simulating max-w-md within a centered column
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp) // gap-4
            ) {
                FullWidthButton(
                    text = "Start New Game",
                    icon = Icons.Filled.PlayArrow,
                    backgroundColor = primaryColor,
                    textColor = textSecondaryColor,
                    onClick = { /* TODO */ }
                )

                FullWidthButton(
                    text = "Join Game with Code",
                    icon = Icons.Filled.GroupAdd,
                    backgroundColor = backgroundColor,
                    textColor = textPrimaryColor,
                    borderColor = slate300,
                    onClick = { /* TODO */ }
                )

                FullWidthButton(
                    text = "Game Rules",
                    icon = Icons.Filled.MenuBook,
                    backgroundColor = backgroundColor,
                    textColor = textPrimaryColor,
                    borderColor = slate300,
                    onClick = { /* TODO */ }
                )
            }
        }
    }
}

@Composable
fun FullWidthButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color,
    textColor: Color,
    borderColor: Color? = null,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp), // h-14
        colors = ButtonDefaults.buttonColors(backgroundColor = backgroundColor),
        shape = MaterialTheme.shapes.medium, // rounded-xl
        elevation = ButtonDefaults.elevation(defaultElevation = 2.dp), // shadow-md
        border = borderColor?.let { androidx.compose.foundation.BorderStroke(1.dp, it) }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.padding(end = 8.dp) // mr-2
        )
        Text(
            text = text,
            color = textColor,
            fontSize = 18.sp, // text-lg
            fontWeight = FontWeight.SemiBold
        )
    }
}


@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
fun DefaultPreview() {
    MaterialTheme { // Wrap with MaterialTheme for preview
        HomeScreen()
    }
}
