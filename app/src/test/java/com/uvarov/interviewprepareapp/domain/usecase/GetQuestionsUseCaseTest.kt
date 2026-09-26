package com.uvarov.interviewprepareapp.domain.usecase

import com.uvarov.interviewprepareapp.domain.model.Difficulty
import com.uvarov.interviewprepareapp.domain.model.InterviewQuestion
import com.uvarov.interviewprepareapp.domain.model.QuestionCategory
import com.uvarov.interviewprepareapp.domain.repository.QuestionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetQuestionsUseCaseTest {

    private val fakeRepository = object : QuestionRepository {
        val sampleQuestions = listOf(
            InterviewQuestion("1", "Title 1", QuestionCategory.ANDROID, Difficulty.EASY, "Summary 1"),
            InterviewQuestion("2", "Title 2", QuestionCategory.KOTLIN, Difficulty.MEDIUM, "Summary 2")
        )

        override fun getQuestions(category: QuestionCategory?): Flow<List<InterviewQuestion>> {
            return if (category == null) {
                flowOf(sampleQuestions)
            } else {
                flowOf(sampleQuestions.filter { it.category == category })
            }
        }

        override suspend fun refreshQuestions(): Result<Unit> = Result.success(Unit)
        override suspend fun toggleBookmark(id: String): Result<Unit> = Result.success(Unit)
    }

    private val useCase = GetQuestionsUseCase(fakeRepository)

    @Test
    fun `invoke with ALL category passes null category to repository`() = runTest {
        val result = useCase(QuestionCategory.ALL).first()
        assertEquals(2, result.size)
    }

    @Test
    fun `invoke with specific category filters correctly`() = runTest {
        val result = useCase(QuestionCategory.ANDROID).first()
        assertEquals(1, result.size)
        assertEquals("Title 1", result.first().title)
    }
}
