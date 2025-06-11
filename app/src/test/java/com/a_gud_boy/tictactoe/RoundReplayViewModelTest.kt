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

    // @Mock // roundDao is not used by ViewModel currently
    // private lateinit var roundDao: RoundDao

    // @Mock // moveDao is removed from ViewModel
    // private lateinit var moveDao: MoveDao

    @Mock
    private lateinit var savedStateHandle: SavedStateHandle

    private lateinit var viewModel: RoundReplayViewModel

    // Test constants
    private val TEST_MATCH_ID = 1L
    private val TEST_ROUND_ID = 10L
    private val OTHER_ROUND_ID = 11L // For testing round not found

    @Before
    fun setUp() {
        // Basic setup for SavedStateHandle, can be overridden in specific tests
        whenever(savedStateHandle.get<Long>("matchId")).thenReturn(TEST_MATCH_ID)
        whenever(savedStateHandle.get<Long>("roundId")).thenReturn(TEST_ROUND_ID)
    }

    private fun createMockMatchWithRoundsAndMoves(
        matchId: Long,
        roundId: Long,
        moves: List<MoveEntity>
    ): MatchWithRoundsAndMoves {
        val matchEntity = MatchEntity(id = matchId, matchNumber = 1L, player1Score = 0, player2Score = 0, matchWinnerName = "", timestamp = 0L)
        val roundEntity = RoundEntity(id = roundId, ownerMatchId = matchId, roundNumber = 1, roundWinnerName = "")
        val roundWithMoves = RoundWithMoves(round = roundEntity, moves = moves)
        return MatchWithRoundsAndMoves(match = matchEntity, roundsWithMoves = listOf(roundWithMoves))
    }
     private fun createMockMatchWithNoSpecificRound(
        matchId: Long,
        actualRoundId: Long, // The ID of the round that exists
        moves: List<MoveEntity>
    ): MatchWithRoundsAndMoves {
        val matchEntity = MatchEntity(id = matchId, matchNumber = 1L, player1Score = 0, player2Score = 0, matchWinnerName = "", timestamp = 0L)
        val roundEntity = RoundEntity(id = actualRoundId, ownerMatchId = matchId, roundNumber = 1, roundWinnerName = "") // Round with a different ID
        val roundWithMoves = RoundWithMoves(round = roundEntity, moves = moves)
        return MatchWithRoundsAndMoves(match = matchEntity, roundsWithMoves = listOf(roundWithMoves))
    }


    @Test
    fun `testInitialization_loadsMovesAndSetsInitialState`() = runTest {
        // Arrange
        val sampleMoves = listOf(
            MoveEntity(id = 1, roundId = TEST_ROUND_ID, player = "X", cellId = "button1", moveNumber = 1),
            MoveEntity(id = 2, roundId = TEST_ROUND_ID, player = "O", cellId = "button2", moveNumber = 2)
        )
        val mockData = createMockMatchWithRoundsAndMoves(TEST_MATCH_ID, TEST_ROUND_ID, sampleMoves)
        whenever(matchDao.getMatchWithRoundsAndMovesById(TEST_MATCH_ID)).thenReturn(kotlinx.coroutines.flow.flowOf(mockData))

        // Act
        // Pass matchDao, for roundDao pass a simple mock or null if constructor allows (current VM needs it)
        // For this test, roundDao is not strictly needed for interaction but constructor requires it.
        val mockRoundDao: RoundDao = org.mockito.Mockito.mock(RoundDao::class.java) // Create a dummy mock
        viewModel = RoundReplayViewModel(matchDao, mockRoundDao, savedStateHandle)


        // Assert
        viewModel.moves.test {
            assertEquals(sampleMoves, awaitItem())
        }
        viewModel.currentMoveIndex.test {
            assertEquals(-1, awaitItem())
        }
        viewModel.currentGridState.test {
            assertEquals(emptyMap<String, Player?>(), awaitItem())
        }
    }

    @Test
    fun `testInitialization_matchNotFound`() = runTest {
        // Arrange
        whenever(matchDao.getMatchWithRoundsAndMovesById(TEST_MATCH_ID)).thenReturn(kotlinx.coroutines.flow.flowOf(null))
        val mockRoundDao: RoundDao = org.mockito.Mockito.mock(RoundDao::class.java)

        // Act
        viewModel = RoundReplayViewModel(matchDao, mockRoundDao, savedStateHandle)

        // Assert
        viewModel.moves.test {
            assertEquals(emptyList<MoveEntity>(), awaitItem())
        }
        viewModel.currentMoveIndex.test { // Should still be -1
            assertEquals(-1, awaitItem())
        }
        viewModel.currentGridState.test { // Should still be empty
            assertEquals(emptyMap<String, Player?>(), awaitItem())
        }
    }

    @Test
    fun `testInitialization_roundNotFoundInMatch`() = runTest {
        // Arrange
        val sampleMovesForOtherRound = listOf(
            MoveEntity(id = 3, roundId = OTHER_ROUND_ID, player = "X", cellId = "button3", moveNumber = 1)
        )
        // We are looking for TEST_ROUND_ID, but the match data only contains OTHER_ROUND_ID
        val mockData = createMockMatchWithNoSpecificRound(TEST_MATCH_ID, OTHER_ROUND_ID, sampleMovesForOtherRound)
        whenever(matchDao.getMatchWithRoundsAndMovesById(TEST_MATCH_ID)).thenReturn(kotlinx.coroutines.flow.flowOf(mockData))
        val mockRoundDao: RoundDao = org.mockito.Mockito.mock(RoundDao::class.java)

        // Act
        viewModel = RoundReplayViewModel(matchDao, mockRoundDao, savedStateHandle)

        // Assert
        viewModel.moves.test {
            assertEquals(emptyList<MoveEntity>(), awaitItem()) // Moves should be empty as TEST_ROUND_ID was not found
        }
    }


    @Test
    fun `testNextMove_incrementsIndexAndUpdatesGridState`() = runTest {
        // Arrange
        val sampleMoves = listOf(
            MoveEntity(id = 1, roundId = TEST_ROUND_ID, player = "X", cellId = "button1", moveNumber = 1),
            MoveEntity(id = 2, roundId = TEST_ROUND_ID, player = "O", cellId = "button2", moveNumber = 2)
        )
        val mockData = createMockMatchWithRoundsAndMoves(TEST_MATCH_ID, TEST_ROUND_ID, sampleMoves)
        whenever(matchDao.getMatchWithRoundsAndMovesById(TEST_MATCH_ID)).thenReturn(kotlinx.coroutines.flow.flowOf(mockData))
        val mockRoundDao: RoundDao = org.mockito.Mockito.mock(RoundDao::class.java)
        viewModel = RoundReplayViewModel(matchDao, mockRoundDao, savedStateHandle)

        // Act & Assert - First nextMove
        viewModel.currentGridState.test {
            // Wait for initial load of moves to complete and emit the first grid state (empty for index -1)
            // The flow from DAO is hot after VM init, so it should have processed.
            // Depending on timing, we might get initial emptyMap from _currentGridState's declaration,
            // then another emptyMap from combine if moves are loaded but index is -1.
            // Then subsequent states as nextMove is called.
            // Turbine's awaitItem will get the *first* item post-subscription.
            // If moves load fast, the first item might already be the empty grid from combine.
            // To be safe, we can use skipItems if needed or ensure the test setup allows predictable emissions.

            // Assuming the first emission after subscription is the empty grid (index -1)
            assertEquals(emptyMap<String, Player?>(), awaitItem())


            viewModel.nextMove() // Index becomes 0
            assertEquals(0, viewModel.currentMoveIndex.value)
            var expectedGrid = mapOf("button1" to Player.X)
            assertEquals(expectedGrid, awaitItem()) // State after first move

            viewModel.nextMove() // Index becomes 1
            assertEquals(1, viewModel.currentMoveIndex.value)
            expectedGrid = mapOf("button1" to Player.X, "button2" to Player.O)
            assertEquals(expectedGrid, awaitItem()) // State after second move

            viewModel.nextMove() // Try to move past the last move
            assertEquals(1, viewModel.currentMoveIndex.value) // Index should not change
            assertEquals(expectedGrid, viewModel.currentGridState.value) // State should not change
            expectNoEvents()
        }
    }

    @Test
    fun `testPreviousMove_decrementsIndexAndUpdatesGridState`() = runTest {
        // Arrange
        val sampleMoves = listOf(
            MoveEntity(id = 1, roundId = TEST_ROUND_ID, player = "X", cellId = "button1", moveNumber = 1),
            MoveEntity(id = 2, roundId = TEST_ROUND_ID, player = "O", cellId = "button2", moveNumber = 2)
        )
        val mockData = createMockMatchWithRoundsAndMoves(TEST_MATCH_ID, TEST_ROUND_ID, sampleMoves)
        whenever(matchDao.getMatchWithRoundsAndMovesById(TEST_MATCH_ID)).thenReturn(kotlinx.coroutines.flow.flowOf(mockData))
        val mockRoundDao: RoundDao = org.mockito.Mockito.mock(RoundDao::class.java)
        viewModel = RoundReplayViewModel(matchDao, mockRoundDao, savedStateHandle)


        // Wait for moves to load by testing the moves StateFlow first
        viewModel.moves.test {
            assertEquals(sampleMoves, awaitItem())
            cancelAndConsumeRemainingEvents()
        }

        // Setup: Move to the last state
        viewModel.nextMove() // index 0 -> grid {"button1" to X}
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
