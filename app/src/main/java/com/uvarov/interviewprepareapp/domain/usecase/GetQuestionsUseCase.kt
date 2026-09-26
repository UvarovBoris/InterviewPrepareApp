package com.uvarov.interviewprepareapp.domain.usecase

import com.uvarov.interviewprepareapp.domain.model.InterviewQuestion
import com.uvarov.interviewprepareapp.domain.model.QuestionCategory
import com.uvarov.interviewprepareapp.domain.repository.QuestionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetQuestionsUseCase @Inject constructor(
    private val repository: QuestionRepository
) {
    operator fun invoke(category: QuestionCategory? = null): Flow<List<InterviewQuestion>> {
        val filterCategory = if (category == QuestionCategory.ALL) null else category
        return repository.getQuestions(filterCategory)
    }
}
