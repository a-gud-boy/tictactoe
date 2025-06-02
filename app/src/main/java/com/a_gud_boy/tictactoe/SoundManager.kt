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
                    Log.e("SoundManager", "Error loading sound $sampleId, status: $status")
                    soundsLoadedSuccessfully[sampleId] = false
                }
            }

            // Load the sounds
            moveSoundId = soundPool?.load(context, R.raw.move, 1) ?: 0
            winSoundId = soundPool?.load(context, R.raw.win, 1) ?: 0
            drawSoundId = soundPool?.load(context, R.raw.draw, 1) ?: 0

            if (moveSoundId == 0 || winSoundId == 0 || drawSoundId == 0) {
                Log.e("SoundManager", "Error loading one or more sounds: IDs are 0")
            }

        } catch (e: Exception) {
            Log.e("SoundManager", "Error initializing SoundPool", e)
        }
    }

    private fun isSoundReady(soundId: Int): Boolean {
        return soundPool != null && soundId != 0 && soundsLoadedSuccessfully[soundId] == true
    }

    fun playMoveSound() {
        if (isSoundReady(moveSoundId)) {
            soundPool?.play(moveSoundId, 1.0f, 1.0f, 1, 0, 1.0f)
            Log.d("SoundManager", "Played move sound")
        } else {
            Log.d("SoundManager", "Move sound not ready or not loaded")
        }
    }

    fun playWinSound() {
        if (isSoundReady(winSoundId)) {
            soundPool?.play(winSoundId, 1.0f, 1.0f, 1, 0, 1.0f)
            Log.d("SoundManager", "Played win sound")
        } else {
            Log.d("SoundManager", "Win sound not ready or not loaded")
        }
    }

    fun playDrawSound() {
        if (isSoundReady(drawSoundId)) {
            soundPool?.play(drawSoundId, 1.0f, 1.0f, 1, 0, 1.0f)
            Log.d("SoundManager", "Played draw sound")
        } else {
            Log.d("SoundManager", "Draw sound not ready or not loaded")
        }
    }

    fun release() {
        Log.d("SoundManager", "Releasing SoundPool")
        soundPool?.release()
        soundPool = null
        soundsLoadedSuccessfully.clear()
    }
}
