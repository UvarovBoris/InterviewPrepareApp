package com.uvarov.interviewprepareapp.domain.usecase

import com.uvarov.interviewprepareapp.domain.model.Difficulty
import com.uvarov.interviewprepareapp.domain.model.InterviewQuestion
import com.uvarov.interviewprepareapp.domain.model.QuestionCategory
import com.uvarov.interviewprepareapp.domain.repository.QuestionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GetQuestionsUseCaseTest {

    private lateinit var fakeRepository: FakeQuestionRepository
    private lateinit var getQuestionsUseCase: GetQuestionsUseCase

    private val sampleAndroidQuestion = InterviewQuestion(
        id = "1",
        title = "Activity Lifecycle",
        category = QuestionCategory.ANDROID,
        difficulty = Difficulty.EASY,
        answerSummary = "Summary 1"
    )

    private val sampleKotlinQuestion = InterviewQuestion(
        id = "2",
        title = "Coroutines vs Threads",
        category = QuestionCategory.KOTLIN,
        difficulty = Difficulty.MEDIUM,
        answerSummary = "Summary 2"
    )

    private val sampleComposeQuestion = InterviewQuestion(
        id = "3",
        title = "Recomposition",
        category = QuestionCategory.COMPOSE,
        difficulty = Difficulty.HARD,
        answerSummary = "Summary 3"
    )

    @Before
    fun setUp() {
        fakeRepository = FakeQuestionRepository(
            initialQuestions = listOf(
                sampleAndroidQuestion,
                sampleKotlinQuestion,
                sampleComposeQuestion
            )
        )
        getQuestionsUseCase = GetQuestionsUseCase(fakeRepository)
    }

    @Test
    fun `invoke with default parameter returns all questions`() = runTest {
        val result = getQuestionsUseCase().first()
        assertEquals(3, result.size)
        assertEquals(listOf(sampleAndroidQuestion, sampleKotlinQuestion, sampleComposeQuestion), result)
    }

    @Test
    fun `invoke with explicit null category returns all questions`() = runTest {
        val result = getQuestionsUseCase(category = null).first()
        assertEquals(3, result.size)
        assertEquals(listOf(sampleAndroidQuestion, sampleKotlinQuestion, sampleComposeQuestion), result)
    }

    @Test
    fun `invoke with ALL category returns all questions`() = runTest {
        val result = getQuestionsUseCase(category = QuestionCategory.ALL).first()
        assertEquals(3, result.size)
        assertEquals(listOf(sampleAndroidQuestion, sampleKotlinQuestion, sampleComposeQuestion), result)
    }

    @Test
    fun `invoke with specific category filters questions correctly`() = runTest {
        val androidResult = getQuestionsUseCase(QuestionCategory.ANDROID).first()
        assertEquals(1, androidResult.size)
        assertEquals(sampleAndroidQuestion, androidResult.first())

        val kotlinResult = getQuestionsUseCase(QuestionCategory.KOTLIN).first()
        assertEquals(1, kotlinResult.size)
        assertEquals(sampleKotlinQuestion, kotlinResult.first())

        val composeResult = getQuestionsUseCase(QuestionCategory.COMPOSE).first()
        assertEquals(1, composeResult.size)
        assertEquals(sampleComposeQuestion, composeResult.first())
    }

    @Test
    fun `invoke with category containing no matching questions returns empty list`() = runTest {
        val result = getQuestionsUseCase(QuestionCategory.COROUTINES).first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `invoke on empty repository returns empty list`() = runTest {
        fakeRepository = FakeQuestionRepository(initialQuestions = emptyList())
        getQuestionsUseCase = GetQuestionsUseCase(fakeRepository)

        val result = getQuestionsUseCase().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `invoke emits updated items when repository data changes`() = runTest {
        val newQuestion = InterviewQuestion(
            id = "4",
            title = "StateFlow",
            category = QuestionCategory.COROUTINES,
            difficulty = Difficulty.MEDIUM,
            answerSummary = "Summary 4"
        )

        var result = getQuestionsUseCase(QuestionCategory.COROUTINES).first()
        assertTrue(result.isEmpty())

        fakeRepository.addQuestion(newQuestion)

        result = getQuestionsUseCase(QuestionCategory.COROUTINES).first()
        assertEquals(1, result.size)
        assertEquals(newQuestion, result.first())
    }

    @Test(expected = IllegalStateException::class)
    fun `invoke propagates repository flow error`() = runTest {
        fakeRepository.shouldThrowError = true
        getQuestionsUseCase().first()
    }

    /**
     * Stateful Fake Repository with real filtering logic
     */
    private class FakeQuestionRepository(
        initialQuestions: List<InterviewQuestion> = emptyList()
    ) : QuestionRepository {

        private val questionsStream = MutableStateFlow(initialQuestions)
        var shouldThrowError = false

        fun addQuestion(question: InterviewQuestion) {
            questionsStream.value = questionsStream.value + question
        }

        override fun getQuestions(category: QuestionCategory?): Flow<List<InterviewQuestion>> {
            if (shouldThrowError) {
                return flow { throw IllegalStateException("Database query failure") }
            }
            return questionsStream.map { questions ->
                if (category == null) {
                    questions
                } else {
                    questions.filter { it.category == category }
                }
            }
        }

        override suspend fun refreshQuestions(): Result<Unit> = Result.success(Unit)
    }
}
