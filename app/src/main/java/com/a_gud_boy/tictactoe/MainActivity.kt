package com.a_gud_boy.tictactoe

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import com.a_gud_boy.tictactoe.AISettingsManager
import com.a_gud_boy.tictactoe.HapticFeedbackManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.a_gud_boy.tictactoe.ui.theme.TictactoeTheme

/**
 * A [ViewModelProvider.Factory] responsible for creating instances of [NormalTicTacToeViewModel]
 * and [InfiniteTicTacToeViewModel].
 *
 * This factory allows the ViewModels to be instantiated with a [SoundManager] dependency,
 * which is used for providing audio feedback during gameplay.
 *
 * @param soundManager The [SoundManager] instance to be used by the created ViewModels.
 */
class TicTacToeViewModelFactory(private val soundManager: SoundManager) :
    ViewModelProvider.Factory {
    /**
     * Creates a new instance of the given `Class`.
     *
     * This method checks the requested `modelClass` and returns an instance of either
     * [NormalTicTacToeViewModel] or [InfiniteTicTacToeViewModel] if they are assignable
     * from the given class. If the `modelClass` is not recognized, it throws an
     * [IllegalArgumentException].
     *
     * @param modelClass A `Class` whose instance is requested.
     * @return A newly created ViewModel.
     * @throws IllegalArgumentException if `modelClass` is not a recognized ViewModel class.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NormalTicTacToeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NormalTicTacToeViewModel(soundManager) as T
        }
        if (modelClass.isAssignableFrom(InfiniteTicTacToeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InfiniteTicTacToeViewModel(soundManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

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
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create SoundManager instance
        val soundManager = SoundManager(this)

        // Create the custom factory
        val viewModelFactory = TicTacToeViewModelFactory(soundManager)

        // Set content. MainPage will need to be able to use this factory
        // if it or its children directly call viewModel().
        // For example, by passing viewModelFactory to MainPage.
        // Or, if ViewModels are to be scoped to MainActivity, they could be created here:
        // val normalTicTacToeViewModel: NormalTicTacToeViewModel by viewModels { viewModelFactory }
        // val infiniteTicTacToeViewModel: InfiniteTicTacToeViewModel by viewModels { viewModelFactory }
        // And then passed to MainPage.
        // For now, just making the factory available for potential use in composables.

        // Initialize settings managers
        AISettingsManager.init(this)
        HapticFeedbackManager.init(this)

        setContent {
            TictactoeTheme {
                // MainPage() // Original
                // Updated MainPage call if it needs the factory:
                MainPage(viewModelFactory = viewModelFactory)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // It's good practice to release SoundManager resources here if it's tied to the Activity lifecycle
        // However, ViewModels now call release() in onCleared(), which is generally preferred.
        // If SoundManager were a singleton or shared beyond ViewModels tied to this Activity,
        // then releasing here or in Application.onTerminate would be more appropriate.
        // For this setup, ViewModel.onCleared() is sufficient.
    }
}
