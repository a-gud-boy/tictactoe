package com.a_gud_boy.tictactoe

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class NormalTicTacToeViewModelTest {

    private lateinit var viewModel: NormalTicTacToeViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = NormalTicTacToeViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test initial state`() = runTest {
        assertEquals(true, viewModel.player1Turn.value)
        assertTrue("Player 1 moves should be empty", viewModel.player1Moves.value.isEmpty())
        assertTrue("Player 2 moves should be empty", viewModel.player2Moves.value.isEmpty())
        assertEquals(0, viewModel.player1Wins.value)
        assertEquals(0, viewModel.player2Wins.value)
        assertNull("Winner info should be null initially", viewModel.winnerInfo.value)
        assertEquals(false, viewModel.isGameConcluded.value)
        assertEquals(true, viewModel.gameStarted.value) // Assuming game starts immediately
        assertEquals("Player 1's Turn", viewModel.turnDenotingText.value)
        assertEquals("Reset Round", viewModel.resetButtonText.value)
    }

    @Test
    fun `onButtonClick updates moves and alternates turns`() = runTest {
        // Player 1's turn
        viewModel.onButtonClick("button1")
        assertEquals(listOf("button1"), viewModel.player1Moves.value)
        assertTrue(viewModel.player2Moves.value.isEmpty())
        assertEquals(false, viewModel.player1Turn.value) // Player 2's turn

        // Player 2's turn
        viewModel.onButtonClick("button2")
        assertEquals(listOf("button1"), viewModel.player1Moves.value)
        assertEquals(listOf("button2"), viewModel.player2Moves.value)
        assertEquals(true, viewModel.player1Turn.value) // Player 1's turn
    }

    @Test
    fun `onButtonClick on occupied cell has no effect`() = runTest {
        viewModel.onButtonClick("button1") // P1
        val p1MovesAfterFirstClick = viewModel.player1Moves.value.toList()
        val p2MovesAfterFirstClick = viewModel.player2Moves.value.toList()
        val turnAfterFirstClick = viewModel.player1Turn.value

        viewModel.onButtonClick("button1") // Try clicking again (should be P2's turn but on P1's cell)
        
        assertEquals(p1MovesAfterFirstClick, viewModel.player1Moves.value)
        assertEquals(p2MovesAfterFirstClick, viewModel.player2Moves.value)
        assertEquals(turnAfterFirstClick, viewModel.player1Turn.value) // Turn should not change

        // Player 2's actual turn
        viewModel.onButtonClick("button2") 
        val p1MovesAfterSecondClick = viewModel.player1Moves.value.toList()
        val p2MovesAfterSecondClick = viewModel.player2Moves.value.toList()
        val turnAfterSecondClick = viewModel.player1Turn.value

        viewModel.onButtonClick("button2") // P1 tries to click P2's cell
        assertEquals(p1MovesAfterSecondClick, viewModel.player1Moves.value)
        assertEquals(p2MovesAfterSecondClick, viewModel.player2Moves.value)
        assertEquals(turnAfterSecondClick, viewModel.player1Turn.value)
    }

    @Test
    fun `test player X wins row 1`() = runTest {
        // P1: button1, P2: button4, P1: button2, P2: button5, P1: button3 (win)
        viewModel.onButtonClick("button1") // P1
        viewModel.onButtonClick("button4") // P2
        viewModel.onButtonClick("button2") // P1
        viewModel.onButtonClick("button5") // P2
        viewModel.onButtonClick("button3") // P1 wins

        assertEquals(Player.X, viewModel.winnerInfo.value?.winner)
        assertEquals(setOf("button1", "button2", "button3"), viewModel.winnerInfo.value?.combination)
        assertEquals(true, viewModel.isGameConcluded.value)
        assertEquals(1, viewModel.player1Wins.value)
        assertEquals(0, viewModel.player2Wins.value)
        assertEquals("Player 1 Won", viewModel.turnDenotingText.value)
    }
    
    @Test
    fun `test player O wins column 2`() = runTest {
        // P1: button1, P2: button2, P1: button3, P2: button5, P1: button7, P2: button8 (win)
        viewModel.onButtonClick("button1") // P1
        viewModel.onButtonClick("button2") // P2
        viewModel.onButtonClick("button3") // P1
        viewModel.onButtonClick("button5") // P2
        viewModel.onButtonClick("button7") // P1
        viewModel.onButtonClick("button8") // P2 wins

        assertEquals(Player.O, viewModel.winnerInfo.value?.winner)
        assertEquals(setOf("button2", "button5", "button8"), viewModel.winnerInfo.value?.combination)
        assertEquals(true, viewModel.isGameConcluded.value)
        assertEquals(0, viewModel.player1Wins.value)
        assertEquals(1, viewModel.player2Wins.value)
        assertEquals("Player 2 Won", viewModel.turnDenotingText.value)
    }

    @Test
    fun `test player X wins diagonal 1`() = runTest {
        // P1: button1, P2: button2, P1: button5, P2: button3, P1: button9 (win)
        viewModel.onButtonClick("button1") // P1
        viewModel.onButtonClick("button2") // P2
        viewModel.onButtonClick("button5") // P1
        viewModel.onButtonClick("button3") // P2
        viewModel.onButtonClick("button9") // P1 wins

        assertEquals(Player.X, viewModel.winnerInfo.value?.winner)
        assertEquals(setOf("button1", "button5", "button9"), viewModel.winnerInfo.value?.combination)
        assertEquals(true, viewModel.isGameConcluded.value)
        assertEquals(1, viewModel.player1Wins.value)
    }

    @Test
    fun `test player O wins diagonal 2`() = runTest {
        // P1: button1, P2: button3, P1: button2, P2: button5, P1: button4, P2: button7 (win)
        viewModel.onButtonClick("button1") // P1
        viewModel.onButtonClick("button3") // P2
        viewModel.onButtonClick("button2") // P1
        viewModel.onButtonClick("button5") // P2
        viewModel.onButtonClick("button4") // P1
        viewModel.onButtonClick("button7") // P2 wins

        assertEquals(Player.O, viewModel.winnerInfo.value?.winner)
        assertEquals(setOf("button3", "button5", "button7"), viewModel.winnerInfo.value?.combination)
        assertEquals(true, viewModel.isGameConcluded.value)
        assertEquals(1, viewModel.player2Wins.value)
    }
    
    // Helper for win tests
    private fun testWinScenario(moves: List<String>, expectedWinner: Player, expectedCombination: Set<String>) = runTest {
        moves.forEach { viewModel.onButtonClick(it) }

        assertEquals(expectedWinner, viewModel.winnerInfo.value?.winner)
        assertEquals(expectedCombination, viewModel.winnerInfo.value?.combination)
        assertEquals(true, viewModel.isGameConcluded.value)
        if (expectedWinner == Player.X) {
            assertEquals(1, viewModel.player1Wins.value)
        } else {
            assertEquals(1, viewModel.player2Wins.value)
        }
    }

    @Test fun `player X wins row 2`() = testWinScenario(listOf("button4","button1","button5","button2","button6"), Player.X, setOf("button4","button5","button6"))
    @Test fun `player X wins row 3`() = testWinScenario(listOf("button7","button1","button8","button2","button9"), Player.X, setOf("button7","button8","button9"))
    @Test fun `player X wins col 1`() = testWinScenario(listOf("button1","button2","button4","button5","button7"), Player.X, setOf("button1","button4","button7"))
    @Test fun `player X wins col 3`() = testWinScenario(listOf("button3","button2","button6","button5","button9"), Player.X, setOf("button3","button6","button9"))

    @Test fun `player O wins row 1`() = testWinScenario(listOf("button4","button1","button5","button2","button8","button3"), Player.O, setOf("button1","button2","button3"))
    @Test fun `player O wins row 2`() = testWinScenario(listOf("button1","button4","button2","button5","button9","button6"), Player.O, setOf("button4","button5","button6"))
    @Test fun `player O wins row 3`() = testWinScenario(listOf("button1","button7","button2","button8","button6","button9"), Player.O, setOf("button7","button8","button9"))
    @Test fun `player O wins col 1`() = testWinScenario(listOf("button2","button1","button5","button4","button8","button7"), Player.O, setOf("button1","button4","button7"))
    @Test fun `player O wins col 3`() = testWinScenario(listOf("button1","button3","button2","button6","button8","button9"), Player.O, setOf("button3","button6","button9"))
    @Test fun `player O wins diag 1`() = testWinScenario(listOf("button2","button1","button3","button5","button4","button9"), Player.O, setOf("button1","button5","button9"))
    @Test fun `player O wins diag 2`() = testWinScenario(listOf("button1","button3","button2","button5","button9","button7"), Player.O, setOf("button3","button5","button7"))


    @Test
    fun `test draw condition`() = runTest {
        // P1: b1, P2: b2, P1: b3
        // P2: b4, P1: b5, P2: b7
        // P1: b6, P2: b9, P1: b8
        // Board:
        // X O X
        // O X X
        // O X O -> Draw
        viewModel.onButtonClick("button1") // P1
        viewModel.onButtonClick("button2") // P2
        viewModel.onButtonClick("button3") // P1

        viewModel.onButtonClick("button4") // P2
        viewModel.onButtonClick("button5") // P1
        viewModel.onButtonClick("button7") // P2

        viewModel.onButtonClick("button6") // P1
        viewModel.onButtonClick("button9") // P2
        viewModel.onButtonClick("button8") // P1 - Results in Draw

        assertNull("Winner should be null in a draw", viewModel.winnerInfo.value?.winner)
        assertTrue("Winning combination should be empty in a draw", viewModel.winnerInfo.value?.combination?.isEmpty() ?: false)
        assertEquals(true, viewModel.isGameConcluded.value)
        assertEquals(0, viewModel.player1Wins.value) // No change in scores for draw
        assertEquals(0, viewModel.player2Wins.value)
        assertEquals("It's a Draw!", viewModel.turnDenotingText.value)
    }

    @Test
    fun `resetRound clears board and game state but preserves scores`() = runTest {
        // Make some moves and win to change state
        viewModel.onButtonClick("button1") // P1
        viewModel.onButtonClick("button4") // P2
        viewModel.onButtonClick("button2") // P1
        viewModel.onButtonClick("button5") // P2
        viewModel.onButtonClick("button3") // P1 wins

        assertEquals(1, viewModel.player1Wins.value) // Score updated

        viewModel.resetRound()

        // Check if board and game state are reset
        assertTrue(viewModel.player1Moves.value.isEmpty())
        assertTrue(viewModel.player2Moves.value.isEmpty())
        assertNull(viewModel.winnerInfo.value)
        assertEquals(false, viewModel.isGameConcluded.value)
        assertEquals(true, viewModel.player1Turn.value) // Player X starts
        assertEquals(true, viewModel.gameStarted.value)
        assertEquals("Player 1's Turn", viewModel.turnDenotingText.value)
        assertEquals("Reset Round", viewModel.resetButtonText.value) // Should be "Reset Round" as game is not concluded by a win

        // Check if scores are preserved
        assertEquals(1, viewModel.player1Wins.value)
        assertEquals(0, viewModel.player2Wins.value)
    }
    
    @Test
    fun `resetButtonText is New Round when game is concluded`() = runTest {
        viewModel.onButtonClick("button1")
        viewModel.onButtonClick("button4")
        viewModel.onButtonClick("button2")
        viewModel.onButtonClick("button5")
        viewModel.onButtonClick("button3") // P1 wins, game concluded
        assertEquals("New Round", viewModel.resetButtonText.value)

        viewModel.resetRound() // Game not concluded
        assertEquals("Reset Round", viewModel.resetButtonText.value)
    }


    @Test
    fun `resetScores resets scores and the round`() = runTest {
        // Make some moves and win to change state and scores
        viewModel.onButtonClick("button1")
        viewModel.onButtonClick("button4")
        viewModel.onButtonClick("button2")
        viewModel.onButtonClick("button5")
        viewModel.onButtonClick("button3") // P1 wins

        assertEquals(1, viewModel.player1Wins.value)

        viewModel.resetScores()

        // Check if scores are reset
        assertEquals(0, viewModel.player1Wins.value)
        assertEquals(0, viewModel.player2Wins.value)

        // Check if round is also reset (similar to resetRound assertions)
        assertTrue(viewModel.player1Moves.value.isEmpty())
        assertTrue(viewModel.player2Moves.value.isEmpty())
        assertNull(viewModel.winnerInfo.value)
        assertEquals(false, viewModel.isGameConcluded.value)
        assertEquals(true, viewModel.player1Turn.value)
        assertEquals("Player 1's Turn", viewModel.turnDenotingText.value)
    }
}
