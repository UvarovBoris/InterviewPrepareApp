package com.uvarov.interviewprepareapp.ui.questions

import com.uvarov.interviewprepareapp.domain.model.Difficulty
import com.uvarov.interviewprepareapp.domain.model.InterviewQuestion
import com.uvarov.interviewprepareapp.domain.model.QuestionCategory
import com.uvarov.interviewprepareapp.domain.repository.QuestionRepository
import com.uvarov.interviewprepareapp.domain.usecase.GetQuestionsUseCase
import com.uvarov.interviewprepareapp.domain.usecase.RefreshQuestionsUseCase
import com.uvarov.interviewprepareapp.domain.usecase.ToggleBookmarkUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuestionsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val fakeRepository = object : QuestionRepository {
        val sampleQuestions = listOf(
            InterviewQuestion("1", "Title 1", QuestionCategory.ANDROID, Difficulty.EASY, "Summary 1")
        )
        var shouldFailRefresh = false
        var bookmarkedId: String? = null

        override fun getQuestions(category: QuestionCategory?): Flow<List<InterviewQuestion>> {
            return flowOf(sampleQuestions)
        }

        override suspend fun refreshQuestions(): Result<Unit> {
            return if (shouldFailRefresh) {
                Result.failure(RuntimeException("Network error"))
            } else {
                Result.success(Unit)
            }
        }

        override suspend fun toggleBookmark(id: String) {
            bookmarkedId = id
        }
    }

    private lateinit var viewModel: QuestionsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository.shouldFailRefresh = false
        fakeRepository.bookmarkedId = null

        val getQuestionsUseCase = GetQuestionsUseCase(fakeRepository)
        val toggleBookmarkUseCase = ToggleBookmarkUseCase(fakeRepository)
        val refreshQuestionsUseCase = RefreshQuestionsUseCase(fakeRepository)

        viewModel = QuestionsViewModel(
            getQuestionsUseCase = getQuestionsUseCase,
            toggleBookmarkUseCase = toggleBookmarkUseCase,
            refreshQuestionsUseCase = refreshQuestionsUseCase,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads questions successfully`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.questions.size)
        assertEquals("Title 1", state.questions.first().title)
    }

    @Test
    fun `selecting category updates state`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(QuestionsUiEvent.SelectCategory(QuestionCategory.KOTLIN))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(QuestionCategory.KOTLIN, viewModel.uiState.value.selectedCategory)
    }

    @Test
    fun `refresh failure exposes error message and can be dismissed`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        fakeRepository.shouldFailRefresh = true
        viewModel.onEvent(QuestionsUiEvent.Refresh)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Network error", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isRefreshing)

        viewModel.onEvent(QuestionsUiEvent.DismissError)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `toggle bookmark invokes use case`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(QuestionsUiEvent.ToggleBookmark("42"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("42", fakeRepository.bookmarkedId)
    }
}
