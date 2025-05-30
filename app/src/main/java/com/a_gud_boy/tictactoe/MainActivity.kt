package com.a_gud_boy.tictactoe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.a_gud_boy.tictactoe.ui.theme.TictactoeTheme

/**
 * The main activity for the Tic Tac Toe application.
 *
 * This activity serves as the entry point of the application and sets up the initial UI content.
 * It uses Jetpack Compose for building the user interface.
 *
 * On creation, it enables edge-to-edge display and then sets the content to the [MainPage]
 * composable, wrapped within the application's [TictactoeTheme].
 */
class MainActivity : ComponentActivity() {
    /**
     * Called when the activity is first created.
     *
     * This method initializes the activity, enables edge-to-edge display for a modern look and feel,
     * and sets the main content view to be the [MainPage] composable function, which provides
     * the core UI structure including navigation between different game modes.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied in [onSaveInstanceState].
     *                           Otherwise, it is null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TictactoeTheme {
                MainPage()
            }
        }
    }
}
