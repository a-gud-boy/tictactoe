package com.a_gud_boy.tictactoe

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log // For logging errors

/**
 * Manages loading and playing sound effects for the Tic Tac Toe game using [SoundPool].
 * This class handles various game event sounds like moves, wins, draws, etc.
 *
 * @param context The application context, used for loading sound resources.
 */
class SoundManager(private val context: Context) {
    // The SoundPool instance used to play short audio clips. Nullified on release.
    private var soundPool: SoundPool? = null
    // Sound ID for the player's move sound.
    private var moveSoundId: Int = 0
    // Sound ID for the game win sound.
    private var winSoundId: Int = 0
    // Sound ID for the game draw sound.
    private var drawSoundId: Int = 0
    // Sound ID for the game lose sound (optional, e.g., when AI wins).
    private var loseSoundId: Int? = 0
    // Sound ID for the AI opponent's move sound.
    private var computerMoveSoundId: Int? = 0

    // Tracks whether each sound resource has been successfully loaded into the SoundPool.
    // Key is the sound ID, Value is true if loaded, false otherwise.
    private var soundsLoadedSuccessfully = mutableMapOf<Int, Boolean>()

    var isSoundEnabled: Boolean = true

    init {
        Log.d("SoundManagerInit", "SoundManager initializing...")
        try {
            // Define audio attributes for game sounds (e.g., respect ringer mode, usage type).
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME) // Specifies that audio is for game sounds.
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION) // Type of audio content.
                .build()
            // Build the SoundPool instance.
            soundPool = SoundPool.Builder()
                .setMaxStreams(3) // Allow up to 3 sounds to play simultaneously.
                .setAudioAttributes(audioAttributes)
                .build()

            // Listener for when sounds are finished loading.
            soundPool?.setOnLoadCompleteListener { _, sampleId, status ->
                if (status == 0) { // Status 0 means success.
                    Log.d("SoundManager", "Sound loaded successfully: $sampleId")
                    soundsLoadedSuccessfully[sampleId] = true
                    // "Prime" the move sound by playing it silently once immediately after loading.
                    // This is a common workaround for SoundPool latency on its first playback.
                    if (sampleId == moveSoundId) {
                        Log.d("SoundManager", "Priming move sound (ID: $sampleId)")
                        soundPool?.play(moveSoundId, 0f, 0f, 0, 0, 1.0f) // Play with 0 volume.
                    }
                } else {
                    Log.e("SoundManager", "Error loading sound $sampleId, status: $status. Marking as not loaded.")
                    soundsLoadedSuccessfully[sampleId] = false // Mark as not loaded on error.
                }
            }

            // Load sound resources. The IDs are stored for later playback.
            // A result of 0 from load() indicates an error.
            Log.d("SoundManagerDebug", "Loading move sound (R.raw.move)...")
            moveSoundId = soundPool?.load(context, R.raw.move, 1) ?: 0
            if (moveSoundId == 0) Log.e("SoundManager", "Error loading move sound: moveSoundId is 0")

            Log.d("SoundManagerDebug", "Loading win sound (R.raw.win)...")
            winSoundId = soundPool?.load(context, R.raw.win, 1) ?: 0
            if (winSoundId == 0) Log.e("SoundManager", "Error loading win sound: winSoundId is 0")

            Log.d("SoundManagerDebug", "Loading draw sound (R.raw.draw)...")
            drawSoundId = soundPool?.load(context, R.raw.draw, 1) ?: 0
            if (drawSoundId == 0) Log.e("SoundManager", "Error loading draw sound: drawSoundId is 0")

            Log.d("SoundManagerDebug", "Loading lose sound (R.raw.lose)...")
            loseSoundId = soundPool?.load(context, R.raw.lose, 1) ?: 0
            if (loseSoundId == 0) Log.e("SoundManager", "Error loading lose sound: loseSoundId is 0")

            Log.d("SoundManagerDebug", "Loading computer move sound (R.raw.click)...")
            computerMoveSoundId = soundPool?.load(context, R.raw.click, 1) ?: 0
            if (computerMoveSoundId == 0) Log.e("SoundManager", "Error loading computer move sound: computerMoveSoundId is 0")

        } catch (e: Exception) {
            // Catch any other exceptions during SoundPool initialization.
            Log.e("SoundManager", "Error initializing SoundPool", e)
            soundPool = null // Ensure soundPool is null if initialization failed.
        }
        Log.d("SoundManagerInit", "SoundManager initialization complete.")
    }

    /**
     * Checks if a specific sound is ready to be played.
     * A sound is ready if the SoundPool is initialized, the sound ID is valid (not 0),
     * and the sound has been successfully loaded.
     *
     * @param soundId The ID of the sound to check.
     * @return True if the sound is ready, false otherwise.
     */
    private fun isSoundReady(soundId: Int): Boolean {
        val isReady = soundPool != null && soundId != 0 && soundsLoadedSuccessfully[soundId] == true
        // Verbose logging for debugging sound readiness.
        Log.v("SoundManager", "isSoundReady for ID $soundId: poolNotNull=${soundPool != null}, idNotZero=${soundId != 0}, loadedSuccessfully=${soundsLoadedSuccessfully[soundId] == true}, result=$isReady")
        return isReady
    }

    /**
     * Plays the sound effect for a player making a move.
     * @param volume The volume level for the sound (0.0 to 1.0). Defaults to 1.0f.
     */
    fun playMoveSound(volume: Float = 1.0f) {
        if (!isSoundEnabled) return
        if (isSoundReady(moveSoundId)) { // Check if sound is loaded and SoundPool available.
            soundPool?.play(moveSoundId, volume, volume, 1, 0, 1.0f)
            Log.d("SoundManager", "Played move sound")
        } else {
            Log.d("SoundManager", "Move sound not ready or not loaded, cannot play.")
        }
    }

    /**
     * Plays the sound effect for winning the game.
     * @param volume The volume level for the sound (0.0 to 1.0). Defaults to 1.0f.
     */
    fun playWinSound(volume: Float = 1.0f) {
        if (!isSoundEnabled) return
        if (isSoundReady(winSoundId)) {
            soundPool?.play(winSoundId, volume, volume, 1, 0, 1.0f)
            Log.d("SoundManager", "Played win sound")
        } else {
            Log.d("SoundManager", "Win sound not ready or not loaded, cannot play.")
        }
    }

    /**
     * Plays the sound effect for a draw game.
     * @param volume The volume level for the sound (0.0 to 1.0). Defaults to 1.0f.
     */
    fun playDrawSound(volume: Float = 1.0f) {
        if (!isSoundEnabled) return
        if (isSoundReady(drawSoundId)) {
            soundPool?.play(drawSoundId, volume, volume, 1, 0, 1.0f)
            Log.d("SoundManager", "Played draw sound")
        } else {
            Log.d("SoundManager", "Draw sound not ready or not loaded, cannot play.")
        }
    }

    /**
     * Plays the sound effect for losing the game (e.g., when AI wins).
     * @param volume The volume level for the sound (0.0 to 1.0). Defaults to 1.0f.
     */
    fun playLoseSound(volume: Float = 1.0f) {
        if (!isSoundEnabled) return
        loseSoundId?.let { // Ensure loseSoundId is not null.
            if (isSoundReady(it)) {
                soundPool?.play(it, volume, volume, 1, 0, 1.0f)
                Log.d("SoundManager", "Played lose sound")
            } else {
                Log.d("SoundManager", "Lose sound not ready or not loaded, cannot play.")
            }
        }
    }

    /**
     * Plays the sound effect for the AI making a move.
     * @param volume The volume level for the sound (0.0 to 1.0). Defaults to 1.0f.
     */
    fun playComputerMoveSound(volume: Float = 1.0f) {
        if (!isSoundEnabled) return
        computerMoveSoundId?.let { // Ensure computerMoveSoundId is not null.
            if (isSoundReady(it)) {
                soundPool?.play(it, volume, volume, 1, 0, 1.0f)
                Log.d("SoundManager", "Played computer move sound")
            } else {
                Log.d("SoundManager", "Computer move sound not ready or not loaded, cannot play.")
            }
        }
    }

    /**
     * Releases the [SoundPool] resources.
     * This method should be called when the SoundManager is no longer needed (e.g., in ViewModel's onCleared)
     * to free up system resources and prevent memory leaks.
     */
    fun release() {
        Log.d("SoundManager", "Releasing SoundPool and clearing loaded sounds map.")
        soundPool?.release() // Release all memory and native resources used by SoundPool.
        soundPool = null     // Set to null to prevent further use.
        soundsLoadedSuccessfully.clear() // Clear the tracking map.
    }
}
