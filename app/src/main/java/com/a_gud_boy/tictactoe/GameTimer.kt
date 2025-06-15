package com.a_gud_boy.tictactoe

class GameTimer {
    private var currentRoundStartTime: Long? = null
    private var accumulatedMatchDuration: Long = 0L

    /**
     * Starts the timer for the current round if it's not already running.
     * This should be called when a round begins or resumes (e.g., on the first move).
     */
    fun startRoundTimer() {
        if (currentRoundStartTime == null) {
            currentRoundStartTime = System.currentTimeMillis()
        }
    }

    /**
     * Pauses the timer for the current round.
     * Calculates the duration of the active part of the round, adds it to the
     * total accumulated match duration, and then stops the current round timer.
     * This should be called when a round ends (win, draw, or manual reset of round)
     * or when the game should effectively pause.
     */
    fun pauseRoundTimer() {
        if (currentRoundStartTime != null) {
            val roundDuration = System.currentTimeMillis() - currentRoundStartTime!!
            accumulatedMatchDuration += roundDuration
            currentRoundStartTime = null
        }
    }

    /**
     * Finalizes and returns the total accumulated match duration.
     * If the timer is currently running for a round (e.g., match ends mid-round),
     * this method will first pause the timer (adding that last segment's duration)
     * before returning the total.
     * This should be called when the entire match is over and the duration is needed.
     */
    fun getFinalMatchDuration(): Long {
        // If the timer is still running for the current round (e.g., match reset/ended without explicit pause),
        // pause it to capture the last segment.
        if (currentRoundStartTime != null) {
            pauseRoundTimer() // This will add the remaining time and nullify currentRoundStartTime
        }
        return accumulatedMatchDuration
    }

    /**
     * Resets the timer state completely for a new match.
     * Clears any accumulated duration and stops any ongoing round timer.
     */
    fun reset() {
        currentRoundStartTime = null
        accumulatedMatchDuration = 0L
    }
}
