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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuestionsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val fakeRepository = object : QuestionRepository {
        val sampleQuestions = listOf(
            InterviewQuestion("1", "Title 1", QuestionCategory.ANDROID, Difficulty.EASY, "Summary 1")
        )

        override fun getQuestions(category: QuestionCategory?): Flow<List<InterviewQuestion>> {
            return flowOf(sampleQuestions)
        }

        override suspend fun refreshQuestions(): Result<Unit> = Result.success(Unit)
        override suspend fun toggleBookmark(id: String): Result<Unit> = Result.success(Unit)
    }

    private lateinit var viewModel: QuestionsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val getQuestionsUseCase = GetQuestionsUseCase(fakeRepository)
        val toggleBookmarkUseCase = ToggleBookmarkUseCase(fakeRepository)
        val refreshQuestionsUseCase = RefreshQuestionsUseCase(fakeRepository)

        viewModel = QuestionsViewModel(
            getQuestionsUseCase = getQuestionsUseCase,
            toggleBookmarkUseCase = toggleBookmarkUseCase,
            refreshQuestionsUseCase = refreshQuestionsUseCase,
            ioDispatcher = testDispatcher
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads questions successfully`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.questions.size)
        assertEquals("Title 1", state.questions.first().title)
    }

    @Test
    fun `selecting category updates state`() = runTest {
        viewModel.onEvent(QuestionsUiEvent.SelectCategory(QuestionCategory.KOTLIN))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(QuestionCategory.KOTLIN, viewModel.uiState.value.selectedCategory)
    }
}
