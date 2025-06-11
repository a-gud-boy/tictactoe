package com.a_gud_boy.tictactoe

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import org.junit.Assert.* // For assertions like assertEquals

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class RoundReplayViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var matchDao: MatchDao

    @Mock
    private lateinit var roundDao: RoundDao

    @Mock
    private lateinit var moveDao: MoveDao

    @Mock
    private lateinit var savedStateHandle: SavedStateHandle

    private lateinit var viewModel: RoundReplayViewModel

    // Test constants
    private val TEST_MATCH_ID = 1L
    private val TEST_ROUND_ID = 10L

    @Before
    fun setUp() {
        // Basic setup for SavedStateHandle, can be overridden in specific tests
        whenever(savedStateHandle.get<Long>("matchId")).thenReturn(TEST_MATCH_ID)
        whenever(savedStateHandle.get<Long>("roundId")).thenReturn(TEST_ROUND_ID)
    }

    @Test
    fun `testInitialization_loadsMovesAndSetsInitialState`() = runTest {
        // Arrange
        val sampleMoves = listOf(
            MoveEntity(id = 1, roundId = TEST_ROUND_ID, player = "X", cellId = "button1", moveNumber = 1),
            MoveEntity(id = 2, roundId = TEST_ROUND_ID, player = "O", cellId = "button2", moveNumber = 2)
        )
        // Note: If getMovesForRound is a suspend function, use coWhenever.
        // Assuming it's a normal function based on current ViewModel implementation.
        whenever(moveDao.getMovesForRound(TEST_ROUND_ID)).thenReturn(sampleMoves)

        // Act
        viewModel = RoundReplayViewModel(matchDao, roundDao, moveDao, savedStateHandle)

        // Assert
        viewModel.moves.test {
            assertEquals(sampleMoves, awaitItem())
        }
        viewModel.currentMoveIndex.test {
            assertEquals(-1, awaitItem())
        }
        viewModel.currentGridState.test {
            assertEquals(emptyMap<String, Player?>(), awaitItem())
            // Current implementation of combine will emit initial empty map then the calculated one.
            // If the init logic was more complex, this might need adjustment or to skip initial emissions.
        }
    }

    @Test
    fun `testNextMove_incrementsIndexAndUpdatesGridState`() = runTest {
        // Arrange
        val sampleMoves = listOf(
            MoveEntity(id = 1, roundId = TEST_ROUND_ID, player = "X", cellId = "button1", moveNumber = 1),
            MoveEntity(id = 2, roundId = TEST_ROUND_ID, player = "O", cellId = "button2", moveNumber = 2)
        )
        whenever(moveDao.getMovesForRound(TEST_ROUND_ID)).thenReturn(sampleMoves)
        viewModel = RoundReplayViewModel(matchDao, roundDao, moveDao, savedStateHandle)

        // Act & Assert - First nextMove
        viewModel.currentGridState.test {
            assertEquals(emptyMap<String, Player?>(), awaitItem()) // Initial state due to index -1

            viewModel.nextMove() // Index becomes 0

            assertEquals(0, viewModel.currentMoveIndex.value)
            var expectedGrid = mapOf("button1" to Player.X)
            assertEquals(expectedGrid, awaitItem()) // State after first move

            // Act & Assert - Second nextMove
            viewModel.nextMove() // Index becomes 1

            assertEquals(1, viewModel.currentMoveIndex.value)
            expectedGrid = mapOf("button1" to Player.X, "button2" to Player.O)
            assertEquals(expectedGrid, awaitItem()) // State after second move

            // Act & Assert - Boundary condition (at the last move)
            viewModel.nextMove() // Try to move past the last move

            assertEquals(1, viewModel.currentMoveIndex.value) // Index should not change
            // Grid state should also not change, no new item emitted to currentGridState by combine
            // but we can check the value directly or ensure no new item if turbine allows.
            // For simplicity, checking current value.
            assertEquals(expectedGrid, viewModel.currentGridState.value)
            expectNoEvents() // Ensure no new state was emitted to gridState after boundary
        }
    }

    @Test
    fun `testPreviousMove_decrementsIndexAndUpdatesGridState`() = runTest {
        // Arrange
        val sampleMoves = listOf(
            MoveEntity(id = 1, roundId = TEST_ROUND_ID, player = "X", cellId = "button1", moveNumber = 1),
            MoveEntity(id = 2, roundId = TEST_ROUND_ID, player = "O", cellId = "button2", moveNumber = 2)
        )
        whenever(moveDao.getMovesForRound(TEST_ROUND_ID)).thenReturn(sampleMoves)
        viewModel = RoundReplayViewModel(matchDao, roundDao, moveDao, savedStateHandle)

        // Setup: Move to the last state
        viewModel.nextMove() // index 0
        viewModel.nextMove() // index 1 (last move)

        // Act & Assert - First previousMove
        viewModel.currentGridState.test {
            // Current state is after 2 moves
            var expectedGrid = mapOf("button1" to Player.X, "button2" to Player.O)
            assertEquals(expectedGrid, awaitItem())


            viewModel.previousMove() // Index becomes 0

            assertEquals(0, viewModel.currentMoveIndex.value)
            expectedGrid = mapOf("button1" to Player.X)
            assertEquals(expectedGrid, awaitItem()) // State after first previousMove

            // Act & Assert - Second previousMove
            viewModel.previousMove() // Index becomes -1

            assertEquals(-1, viewModel.currentMoveIndex.value)
            expectedGrid = emptyMap()
            assertEquals(expectedGrid, awaitItem()) // State after second previousMove (initial state)

            // Act & Assert - Boundary condition (at the initial state)
            viewModel.previousMove() // Try to move before the initial state

            assertEquals(-1, viewModel.currentMoveIndex.value) // Index should not change
            assertEquals(expectedGrid, viewModel.currentGridState.value) // State should not change
            expectNoEvents() // Ensure no new state was emitted
        }
    }
}
