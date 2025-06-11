package com.a_gud_boy.tictactoe

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.a_gud_boy.tictactoe.ui.theme.TictactoeTheme

// ViewModelProvider.Factory is already imported via androidx.lifecycle.ViewModelProvider

// Define LocalViewModelFactory, can be in MainActivity.kt or a separate file
val LocalViewModelFactory = staticCompositionLocalOf<ViewModelProvider.Factory> {
    error("ViewModelFactory not provided")
}

/**
 * A [ViewModelProvider.Factory] responsible for creating instances of [NormalTicTacToeViewModel],
 * [InfiniteTicTacToeViewModel], and [HistoryViewModel].
 *
 * This factory allows the ViewModels to be instantiated with dependencies like [SoundManager]
 * and [AppDatabase] (via its DAOs), which are used for audio feedback and data persistence.
 *
 * @param soundManager The [SoundManager] instance.
 * @param appDatabase The [AppDatabase] instance for accessing DAOs.
 */
class TicTacToeViewModelFactory(
    private val soundManager: SoundManager,
    private val appDatabase: AppDatabase // Added AppDatabase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // This version of create is kept for compatibility if still called directly by older framework versions
        // or if CreationExtras are not available. It cannot create MatchDetailsViewModel.
        if (modelClass.isAssignableFrom(NormalTicTacToeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NormalTicTacToeViewModel(
                soundManager,
                appDatabase.matchDao(),
                appDatabase.roundDao(),
                appDatabase.moveDao()
            ) as T
        }
        if (modelClass.isAssignableFrom(InfiniteTicTacToeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InfiniteTicTacToeViewModel(
                soundManager,
                appDatabase.matchDao(),
                appDatabase.roundDao(),
                appDatabase.moveDao()
            ) as T
        }
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) { // Added HistoryViewModel
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(appDatabase.matchDao()) as T
        }
        if (modelClass.isAssignableFrom(MatchDetailsViewModel::class.java)) {
            throw IllegalArgumentException("MatchDetailsViewModel requires SavedStateHandle. Use create(modelClass, extras) instead.")
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val savedStateHandle = extras.createSavedStateHandle()

        return when {
            modelClass.isAssignableFrom(NormalTicTacToeViewModel::class.java) ->
                NormalTicTacToeViewModel(
                    soundManager,
                    appDatabase.matchDao(),
                    appDatabase.roundDao(),
                    appDatabase.moveDao()
                ) as T
            modelClass.isAssignableFrom(InfiniteTicTacToeViewModel::class.java) ->
                InfiniteTicTacToeViewModel(
                    soundManager,
                    appDatabase.matchDao(),
                    appDatabase.roundDao(),
                    appDatabase.moveDao()
                ) as T
            modelClass.isAssignableFrom(HistoryViewModel::class.java) ->
                HistoryViewModel(appDatabase.matchDao()) as T
            modelClass.isAssignableFrom(MatchDetailsViewModel::class.java) ->
                MatchDetailsViewModel(appDatabase.matchDao(), savedStateHandle) as T // Pass the created SavedStateHandle
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
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

        // Create AppDatabase instance
        val appDatabase = AppDatabase.getDatabase(applicationContext)

        // Create the custom factory
        val viewModelFactory = TicTacToeViewModelFactory(soundManager, appDatabase)

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
                CompositionLocalProvider(LocalViewModelFactory provides viewModelFactory) { // Provide factory
                    MainPage() // MainPage will now have access via LocalViewModelFactory.current
                    // Assuming MainPage is updated to not expect viewModelFactory as a parameter
                    // or can still accept it for other purposes but new ViewModels use the Local.
                }
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
