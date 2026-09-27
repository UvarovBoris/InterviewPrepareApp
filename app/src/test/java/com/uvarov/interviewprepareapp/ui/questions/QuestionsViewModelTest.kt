package com.uvarov.interviewprepareapp.ui.questions

import app.cash.turbine.test
import com.uvarov.interviewprepareapp.domain.model.Difficulty
import com.uvarov.interviewprepareapp.domain.model.InterviewQuestion
import com.uvarov.interviewprepareapp.domain.model.QuestionCategory
import com.uvarov.interviewprepareapp.domain.repository.QuestionRepository
import com.uvarov.interviewprepareapp.domain.usecase.GetQuestionsUseCase
import com.uvarov.interviewprepareapp.domain.usecase.RefreshQuestionsUseCase
import com.uvarov.interviewprepareapp.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuestionsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sampleQuestion1 = InterviewQuestion(
        id = "1",
        title = "Activity Lifecycle",
        category = QuestionCategory.ANDROID,
        difficulty = Difficulty.EASY,
        answerSummary = "Lifecycle summary"
    )

    private val sampleQuestion2 = InterviewQuestion(
        id = "2",
        title = "Coroutines vs Threads",
        category = QuestionCategory.KOTLIN,
        difficulty = Difficulty.MEDIUM,
        answerSummary = "Coroutines summary"
    )

    private val sampleQuestions = listOf(sampleQuestion1, sampleQuestion2)

    private class FakeQuestionRepository : QuestionRepository {
        var sampleQuestions = listOf<InterviewQuestion>()
        var androidQuestions = listOf<InterviewQuestion>()
        var kotlinQuestions = listOf<InterviewQuestion>()

        var customFlow: Flow<List<InterviewQuestion>>? = null

        var refreshDelayMs: Long = 0L
        var refreshResult: Result<Unit> = Result.success(Unit)

        var lastRequestedCategory: QuestionCategory? = null
        val requestedCategories = mutableListOf<QuestionCategory?>()

        override fun getQuestions(category: QuestionCategory?): Flow<List<InterviewQuestion>> {
            lastRequestedCategory = category
            requestedCategories.add(category)
            customFlow?.let { return it }

            return when (category) {
                QuestionCategory.ANDROID -> flowOf(androidQuestions)
                QuestionCategory.KOTLIN -> flowOf(kotlinQuestions)
                null -> flowOf(sampleQuestions)
                else -> flowOf(sampleQuestions)
            }
        }

        override suspend fun refreshQuestions(): Result<Unit> {
            if (refreshDelayMs > 0) {
                delay(refreshDelayMs)
            }
            return refreshResult
        }
    }

    private lateinit var fakeRepository: FakeQuestionRepository

    private fun createViewModel(): QuestionsViewModel {
        val getQuestionsUseCase = GetQuestionsUseCase(fakeRepository)
        val refreshQuestionsUseCase = RefreshQuestionsUseCase(fakeRepository)

        return QuestionsViewModel(
            getQuestionsUseCase = getQuestionsUseCase,
            refreshQuestionsUseCase = refreshQuestionsUseCase
        )
    }

    @Before
    fun setUp() {
        fakeRepository = FakeQuestionRepository().apply {
            sampleQuestions = this@QuestionsViewModelTest.sampleQuestions
            androidQuestions = listOf(sampleQuestion1)
            kotlinQuestions = listOf(sampleQuestion2)
        }
    }

    // ==========================================
    // Group 1: Initialization & Loading State
    // ==========================================

    @Test
    fun `initial state emits loading before upstream produces and then loads questions for ALL category`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()

            viewModel.uiState.test {
                // First emission from stateIn initialValue
                val initial = awaitItem()
                assertTrue(initial.isLoading)
                assertTrue(initial.questions.isEmpty())
                assertEquals(QuestionCategory.ALL, initial.selectedCategory)

                // Advance virtual time to complete upstream combine and init refresh
                advanceUntilIdle()

                val loaded = expectMostRecentItem()
                assertFalse(loaded.isLoading)
                assertFalse(loaded.isRefreshing)
                assertEquals(sampleQuestions, loaded.questions)
                assertEquals(QuestionCategory.ALL, loaded.selectedCategory)
                assertNull(loaded.errorMessage)

                cancelAndIgnoreRemainingEvents()
            }

            assertEquals(null, fakeRepository.lastRequestedCategory) // ALL category maps to null in GetQuestionsUseCase
        }

    @Test
    fun `initial refresh failure displays localized error message in state`() =
        runTest(mainDispatcherRule.testDispatcher) {
            fakeRepository.refreshResult = Result.failure(RuntimeException("Initial network error"))

            val viewModel = createViewModel()

            viewModel.uiState.test {
                val initial = awaitItem()
                assertTrue(initial.isLoading)

                advanceUntilIdle()

                val stateWithError = expectMostRecentItem()
                assertFalse(stateWithError.isLoading)
                assertFalse(stateWithError.isRefreshing)
                assertEquals("Initial network error", stateWithError.errorMessage)
                assertEquals(sampleQuestions, stateWithError.questions)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `initial refresh failure with null message falls back to default error text`() =
        runTest(mainDispatcherRule.testDispatcher) {
            fakeRepository.refreshResult = Result.failure(Exception())

            val viewModel = createViewModel()

            viewModel.uiState.test {
                awaitItem() // initial loading

                advanceUntilIdle()

                val state = expectMostRecentItem()
                assertFalse(state.isRefreshing)
                assertEquals("Failed to fetch questions from network", state.errorMessage)

                cancelAndIgnoreRemainingEvents()
            }
        }

    // ==========================================
    // Group 2: Category Selection & Reactivity
    // ==========================================

    @Test
    fun `SelectCategory updates selectedCategory and queries repository with selected category`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()

            viewModel.uiState.test {
                awaitItem() // initial loading
                advanceUntilIdle()
                val loadedInitial = expectMostRecentItem()
                assertEquals(QuestionCategory.ALL, loadedInitial.selectedCategory)

                viewModel.onEvent(QuestionsUiEvent.SelectCategory(QuestionCategory.ANDROID))
                advanceUntilIdle()

                val updatedState = expectMostRecentItem()
                assertEquals(QuestionCategory.ANDROID, updatedState.selectedCategory)
                assertEquals(listOf(sampleQuestion1), updatedState.questions)

                cancelAndIgnoreRemainingEvents()
            }

            assertEquals(QuestionCategory.ANDROID, fakeRepository.lastRequestedCategory)
        }

    @Test
    fun `SelectCategory with null updates selectedCategory to null and passes null category to repository`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()

            viewModel.uiState.test {
                awaitItem() // initial loading
                advanceUntilIdle()
                expectMostRecentItem()

                viewModel.onEvent(QuestionsUiEvent.SelectCategory(null))
                advanceUntilIdle()

                val updatedState = expectMostRecentItem()
                assertNull(updatedState.selectedCategory)

                cancelAndIgnoreRemainingEvents()
            }

            assertNull(fakeRepository.lastRequestedCategory)
        }

    @Test
    fun `switching categories sequentially updates uiState with respective questions`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()

            viewModel.uiState.test {
                awaitItem() // initial loading
                advanceUntilIdle()
                assertEquals(sampleQuestions, expectMostRecentItem().questions)

                // Switch to ANDROID
                viewModel.onEvent(QuestionsUiEvent.SelectCategory(QuestionCategory.ANDROID))
                advanceUntilIdle()
                val androidState = expectMostRecentItem()
                assertEquals(QuestionCategory.ANDROID, androidState.selectedCategory)
                assertEquals(listOf(sampleQuestion1), androidState.questions)

                // Switch to KOTLIN
                viewModel.onEvent(QuestionsUiEvent.SelectCategory(QuestionCategory.KOTLIN))
                advanceUntilIdle()
                val kotlinState = expectMostRecentItem()
                assertEquals(QuestionCategory.KOTLIN, kotlinState.selectedCategory)
                assertEquals(listOf(sampleQuestion2), kotlinState.questions)

                // Switch back to ALL
                viewModel.onEvent(QuestionsUiEvent.SelectCategory(QuestionCategory.ALL))
                advanceUntilIdle()
                val allState = expectMostRecentItem()
                assertEquals(QuestionCategory.ALL, allState.selectedCategory)
                assertEquals(sampleQuestions, allState.questions)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `uiState reactively updates when repository emits new questions without category change`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val questionsSharedFlow = MutableSharedFlow<List<InterviewQuestion>>(replay = 1)
            questionsSharedFlow.emit(listOf(sampleQuestion1))
            fakeRepository.customFlow = questionsSharedFlow

            val viewModel = createViewModel()

            viewModel.uiState.test {
                awaitItem() // initial loading
                advanceUntilIdle()
                val firstEmission = expectMostRecentItem()
                assertEquals(listOf(sampleQuestion1), firstEmission.questions)

                // Emit new data from database/network
                questionsSharedFlow.emit(listOf(sampleQuestion1, sampleQuestion2))
                advanceUntilIdle()

                val secondEmission = expectMostRecentItem()
                assertEquals(listOf(sampleQuestion1, sampleQuestion2), secondEmission.questions)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `uiState handles empty questions list gracefully`() =
        runTest(mainDispatcherRule.testDispatcher) {
            fakeRepository.sampleQuestions = emptyList()

            val viewModel = createViewModel()

            viewModel.uiState.test {
                awaitItem() // initial loading
                advanceUntilIdle()

                val state = expectMostRecentItem()
                assertFalse(state.isLoading)
                assertTrue(state.questions.isEmpty())

                cancelAndIgnoreRemainingEvents()
            }
        }

    // ==========================================
    // Group 3: Pull-to-Refresh & Error Handling
    // ==========================================

    @Test
    fun `Refresh event sets isRefreshing true while active and false upon completion`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()

            viewModel.uiState.test {
                awaitItem() // initial loading
                advanceUntilIdle()
                val idleState = expectMostRecentItem()
                assertFalse(idleState.isRefreshing)

                // Configure virtual delay for the manual refresh
                fakeRepository.refreshDelayMs = 1_000L

                // Trigger manual refresh
                viewModel.onEvent(QuestionsUiEvent.Refresh)
                runCurrent() // Runs up to delay(1_000L)

                // State while refresh is in-flight
                val refreshingState = awaitItem()
                assertTrue(refreshingState.isRefreshing)
                assertNull(refreshingState.errorMessage)

                // Advance virtual clock past the delay
                advanceTimeBy(1_000L)
                runCurrent()

                // State after completion
                val finishedState = awaitItem()
                assertFalse(finishedState.isRefreshing)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `Refresh event clears previous error message at the start of refresh`() =
        runTest(mainDispatcherRule.testDispatcher) {
            // Initial refresh fails
            fakeRepository.refreshResult = Result.failure(RuntimeException("Previous error"))

            val viewModel = createViewModel()

            viewModel.uiState.test {
                awaitItem() // initial loading
                advanceUntilIdle()
                val stateWithError = expectMostRecentItem()
                assertEquals("Previous error", stateWithError.errorMessage)

                // Configure delay for the next refresh
                fakeRepository.refreshDelayMs = 1_000L
                fakeRepository.refreshResult = Result.success(Unit)

                viewModel.onEvent(QuestionsUiEvent.Refresh)
                runCurrent() // Runs up to delay(1_000L)

                // When refresh starts, error must be cleared immediately
                val refreshingState = awaitItem()
                assertTrue(refreshingState.isRefreshing)
                assertNull(refreshingState.errorMessage)

                // Advance virtual clock past the delay
                advanceTimeBy(1_000L)
                runCurrent()

                val finalState = awaitItem()
                assertFalse(finalState.isRefreshing)
                assertNull(finalState.errorMessage)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `Refresh failure updates errorMessage with localizedMessage`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()

            viewModel.uiState.test {
                awaitItem() // initial loading
                advanceUntilIdle()
                val idleState = expectMostRecentItem()
                assertNull(idleState.errorMessage)

                fakeRepository.refreshResult = Result.failure(RuntimeException("Network timeout"))
                viewModel.onEvent(QuestionsUiEvent.Refresh)
                advanceUntilIdle()

                val errorState = expectMostRecentItem()
                assertFalse(errorState.isRefreshing)
                assertEquals("Network timeout", errorState.errorMessage)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `Refresh failure with null localizedMessage falls back to default error text`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()

            viewModel.uiState.test {
                awaitItem() // initial loading
                advanceUntilIdle()
                val idleState = expectMostRecentItem()
                assertNull(idleState.errorMessage)

                fakeRepository.refreshResult = Result.failure(Exception())
                viewModel.onEvent(QuestionsUiEvent.Refresh)
                advanceUntilIdle()

                val errorState = expectMostRecentItem()
                assertFalse(errorState.isRefreshing)
                assertEquals("Failed to fetch questions from network", errorState.errorMessage)

                cancelAndIgnoreRemainingEvents()
            }
        }

    // ==========================================
    // Group 4: Error Dismissal
    // ==========================================

    @Test
    fun `DismissError event clears existing error message in uiState`() =
        runTest(mainDispatcherRule.testDispatcher) {
            fakeRepository.refreshResult = Result.failure(RuntimeException("Dismissible error"))

            val viewModel = createViewModel()

            viewModel.uiState.test {
                awaitItem() // initial loading
                advanceUntilIdle()
                val errorState = expectMostRecentItem()
                assertEquals("Dismissible error", errorState.errorMessage)

                viewModel.onEvent(QuestionsUiEvent.DismissError)
                advanceUntilIdle()

                val dismissedState = expectMostRecentItem()
                assertNull(dismissedState.errorMessage)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `DismissError when errorMessage is already null maintains null state without error`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()

            viewModel.uiState.test {
                awaitItem() // initial loading
                advanceUntilIdle()
                val loadedState = expectMostRecentItem()
                assertNull(loadedState.errorMessage)

                viewModel.onEvent(QuestionsUiEvent.DismissError)
                advanceUntilIdle()

                expectNoEvents()
                assertNull(viewModel.uiState.value.errorMessage)

                cancelAndIgnoreRemainingEvents()
            }
        }
}
