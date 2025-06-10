package com.a_gud_boy.tictactoe

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log // For logging errors

class SoundManager(private val context: Context) {
    private var soundPool: SoundPool? = null
    private var moveSoundId: Int = 0
    private var winSoundId: Int = 0
    private var drawSoundId: Int = 0
    private var loseSoundId: Int? = 0
    private var computerMoveSoundId: Int? = 0
    // A flag to track if sounds are loaded. More sophisticated tracking can be added.
    private var soundsLoadedSuccessfully = mutableMapOf<Int, Boolean>()


    init {
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            soundPool = SoundPool.Builder()
                .setMaxStreams(3) // Max 3 sounds playing at once
                .setAudioAttributes(audioAttributes)
                .build()

            soundPool?.setOnLoadCompleteListener { _, sampleId, status ->
                if (status == 0) {
                    Log.d("SoundManager", "Sound loaded successfully: $sampleId")
                    soundsLoadedSuccessfully[sampleId] = true
                } else {
                    Log.e("SoundManager", "Error loading sound $sampleId, status: $status. Marking as not loaded.")
                    soundsLoadedSuccessfully[sampleId] = false
                }
            }

            moveSoundId = soundPool?.load(context, R.raw.move, 1) ?: 0
            if (moveSoundId != 0) {
                soundsLoadedSuccessfully[moveSoundId] = false
            } else {
                Log.e("SoundManager", "Error loading move sound: moveSoundId is 0")
            }

            winSoundId = soundPool?.load(context, R.raw.win, 1) ?: 0
            if (winSoundId != 0) {
                soundsLoadedSuccessfully[winSoundId] = false
            } else {
                Log.e("SoundManager", "Error loading win sound: winSoundId is 0")
            }

            drawSoundId = soundPool?.load(context, R.raw.draw, 1) ?: 0
            if (drawSoundId != 0) {
                soundsLoadedSuccessfully[drawSoundId] = false
            } else {
                Log.e("SoundManager", "Error loading draw sound: drawSoundId is 0")
            }

            loseSoundId = soundPool?.load(context, R.raw.lose, 1) ?: 0 // Placeholder
            if (loseSoundId != 0) {
                soundsLoadedSuccessfully[loseSoundId!!] = false
            } else {
                Log.e("SoundManager", "Error loading lose sound: loseSoundId is 0")
            }

            computerMoveSoundId = soundPool?.load(context, R.raw.click, 1) ?: 0 // Placeholder
            if (computerMoveSoundId != 0) {
                soundsLoadedSuccessfully[computerMoveSoundId!!] = false
            } else {
                Log.e("SoundManager", "Error loading computer move sound: computerMoveSoundId is 0")
            }

        } catch (e: Exception) {
            Log.e("SoundManager", "Error initializing SoundPool", e)
        }
    }

    private fun isSoundReady(soundId: Int): Boolean {
        val isReady = soundPool != null && soundId != 0 && soundsLoadedSuccessfully[soundId] == true
        Log.v("SoundManager", "isSoundReady for ID $soundId: poolNotNull=${soundPool != null}, idNotZero=${soundId != 0}, loadedSuccessfully=${soundsLoadedSuccessfully[soundId] == true}, result=$isReady")
        return isReady
    }

    fun playMoveSound(volume: Float = 1.0f) {
        if (isSoundReady(moveSoundId)) {
            soundPool?.play(moveSoundId, volume, volume, 1, 0, 1.0f)
            Log.d("SoundManager", "Played move sound")
        } else {
            Log.d("SoundManager", "Move sound not ready or not loaded")
        }
    }

    fun playWinSound(volume: Float = 1.0f) {
        if (isSoundReady(winSoundId)) {
            soundPool?.play(winSoundId, volume, volume, 1, 0, 1.0f)
            Log.d("SoundManager", "Played win sound")
        } else {
            Log.d("SoundManager", "Win sound not ready or not loaded")
        }
    }

    fun playDrawSound(volume: Float = 1.0f) {
        if (isSoundReady(drawSoundId)) {
            soundPool?.play(drawSoundId, volume, volume, 1, 0, 1.0f)
            Log.d("SoundManager", "Played draw sound")
        } else {
            Log.d("SoundManager", "Draw sound not ready or not loaded")
        }
    }

    fun playLoseSound(volume: Float = 1.0f) {
        loseSoundId?.let {
            if (isSoundReady(it)) {
                soundPool?.play(it, volume, volume, 1, 0, 1.0f)
                Log.d("SoundManager", "Played lose sound")
            } else {
                Log.d("SoundManager", "Lose sound not ready or not loaded")
            }
        }
    }

    fun playComputerMoveSound(volume: Float = 1.0f) {
        computerMoveSoundId?.let {
            if (isSoundReady(it)) {
                soundPool?.play(it, volume, volume, 1, 0, 1.0f)
                Log.d("SoundManager", "Played computer move sound")
            } else {
                Log.d("SoundManager", "Computer move sound not ready or not loaded")
            }
        }
    }

    fun release() {
        Log.d("SoundManager", "Releasing SoundPool")
        soundPool?.release()
        soundPool = null
        soundsLoadedSuccessfully.clear()
    }
}
