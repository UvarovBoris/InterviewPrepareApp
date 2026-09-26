package com.uvarov.interviewprepareapp.domain.usecase

import com.uvarov.interviewprepareapp.domain.model.Difficulty
import com.uvarov.interviewprepareapp.domain.model.InterviewQuestion
import com.uvarov.interviewprepareapp.domain.model.QuestionCategory
import com.uvarov.interviewprepareapp.domain.repository.QuestionRepository
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GetQuestionsUseCaseMockkTest {

    private val repository: QuestionRepository = mockk()
    private lateinit var getQuestionsUseCase: GetQuestionsUseCase

    private val sampleQuestion = InterviewQuestion(
        id = "1",
        title = "Activity Lifecycle",
        category = QuestionCategory.ANDROID,
        difficulty = Difficulty.EASY,
        answerSummary = "Lifecycle summary"
    )

    @Before
    fun setUp() {
        getQuestionsUseCase = GetQuestionsUseCase(repository)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `invoke with default parameter passes null category to repository`() = runTest {
        every { repository.getQuestions(null) } returns flowOf(listOf(sampleQuestion))

        val result = getQuestionsUseCase().first()

        assertEquals(listOf(sampleQuestion), result)
        verify(exactly = 1) { repository.getQuestions(null) }
    }

    @Test
    fun `invoke with explicit null category passes null category to repository`() = runTest {
        every { repository.getQuestions(null) } returns flowOf(listOf(sampleQuestion))

        val result = getQuestionsUseCase(category = null).first()

        assertEquals(listOf(sampleQuestion), result)
        verify(exactly = 1) { repository.getQuestions(null) }
    }

    @Test
    fun `invoke with ALL category maps to null category for repository`() = runTest {
        every { repository.getQuestions(null) } returns flowOf(listOf(sampleQuestion))

        val result = getQuestionsUseCase(category = QuestionCategory.ALL).first()

        assertEquals(listOf(sampleQuestion), result)
        verify(exactly = 1) { repository.getQuestions(null) }
    }

    @Test
    fun `invoke with specific category passes exact category to repository`() = runTest {
        val categories = QuestionCategory.entries.filter { it != QuestionCategory.ALL }

        for (category in categories) {
            every { repository.getQuestions(category) } returns flowOf(listOf(sampleQuestion.copy(category = category)))

            val result = getQuestionsUseCase(category = category).first()

            assertEquals(1, result.size)
            assertEquals(category, result.first().category)
            verify(exactly = 1) { repository.getQuestions(category) }
        }
    }

    @Test
    fun `invoke emits empty list when repository returns empty flow`() = runTest {
        every { repository.getQuestions(QuestionCategory.ANDROID) } returns flowOf(emptyList())

        val result = getQuestionsUseCase(QuestionCategory.ANDROID).first()

        assertTrue(result.isEmpty())
        verify(exactly = 1) { repository.getQuestions(QuestionCategory.ANDROID) }
    }

    @Test
    fun `invoke propagates sequential flow emissions from repository`() = runTest {
        val q1 = sampleQuestion
        val q2 = sampleQuestion.copy(id = "2", title = "Compose State", category = QuestionCategory.COMPOSE)

        every { repository.getQuestions(null) } returns flowOf(listOf(q1), listOf(q1, q2))

        val emissions = getQuestionsUseCase().toList()

        assertEquals(2, emissions.size)
        assertEquals(listOf(q1), emissions[0])
        assertEquals(listOf(q1, q2), emissions[1])
        verify(exactly = 1) { repository.getQuestions(null) }
    }

    @Test(expected = IllegalStateException::class)
    fun `invoke propagates repository flow error`() = runTest {
        every { repository.getQuestions(any()) } returns flow { throw IllegalStateException("Database error") }

        getQuestionsUseCase().first()
    }
}
