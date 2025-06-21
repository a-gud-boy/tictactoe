package com.a_gud_boy.tictactoe

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.a_gud_boy.tictactoe.ui.theme.TictactoeTheme
import kotlinx.coroutines.delay
import com.google.firebase.auth.FirebaseAuth
import android.util.Log

// Define LocalViewModelFactory, can be in MainActivity.kt or a separate file
val LocalViewModelFactory = staticCompositionLocalOf<ViewModelProvider.Factory> {
    error("ViewModelFactory not provided")
}

class TicTacToeViewModelFactory(
    private val soundManager: SoundManager,
    private val appDatabase: AppDatabase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
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
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(appDatabase.matchDao()) as T
        }
        if (modelClass.isAssignableFrom(MatchDetailsViewModel::class.java)) {
            throw IllegalArgumentException("MatchDetailsViewModel requires SavedStateHandle. Use create(modelClass, extras) instead.")
        }
        if (modelClass.isAssignableFrom(OnlineGameViewModel::class.java)) {
            throw IllegalArgumentException("OnlineGameViewModel requires gameId and SoundManager. Use create(modelClass, extras) with CreationExtras providing SavedStateHandle for gameId.")
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
                MatchDetailsViewModel(
                    appDatabase.matchDao(),
                    savedStateHandle
                ) as T
            modelClass.isAssignableFrom(RoundReplayViewModel::class.java) ->
                RoundReplayViewModel(
                    appDatabase.matchDao(),
                    appDatabase.roundDao(),
                    savedStateHandle
                ) as T
            modelClass.isAssignableFrom(OnlineGameViewModel::class.java) -> {
                val gameId = savedStateHandle.get<String>("gameId")
                    ?: throw IllegalStateException("gameId not found in SavedStateHandle for OnlineGameViewModel")
                @Suppress("UNCHECKED_CAST")
                OnlineGameViewModel(gameId, soundManager) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val soundManager = SoundManager(this)
        val appDatabase = AppDatabase.getDatabase(applicationContext)
        val viewModelFactory = TicTacToeViewModelFactory(soundManager, appDatabase)

        AISettingsManager.init(this)
        HapticFeedbackManager.init(this)

        setContent {
            CompositionLocalProvider(LocalViewModelFactory provides viewModelFactory) {
                TictactoeTheme {
                    var isSignedIn by remember { mutableStateOf(false) }
                    val navController = rememberNavController()

                    LaunchedEffect(Unit) {
                    val auth = FirebaseAuth.getInstance()
                    if (auth.currentUser == null) {
                        auth.signInAnonymously()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d("MainPageAuth", "signInAnonymously:success. User ID: ${auth.currentUser?.uid}")
                                    isSignedIn = true
                                } else {
                                    Log.w("MainPageAuth", "signInAnonymously:failure", task.exception)
                                    // Optionally, handle sign-in failure (e.g., show an error, retry, etc.)
                                    // For now, it will remain on the loading screen if sign-in fails.
                                }
                            }
                    } else {
                        Log.d("MainPageAuth", "User already signed in. User ID: ${auth.currentUser?.uid}")
                        isSignedIn = true // Already signed in
                    }
                }

                if (isSignedIn) {
                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            HomeScreen(navController = navController)
                        }
                        composable("game_setup") {
                            GameSetupScreen(navController = navController)
                        }
                        composable("normal_game") {
                            NormalTicTacToePage(innerPadding = PaddingValues()) // Pass empty PaddingValues for now
                        }
                        composable("infinite_game") {
                            InfiniteTicTacToePage(innerPadding = PaddingValues()) // Pass empty PaddingValues for now
                        }
                    }
                } else {
                    LoadingScreen()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
