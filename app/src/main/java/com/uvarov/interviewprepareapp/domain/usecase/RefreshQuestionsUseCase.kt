package com.uvarov.interviewprepareapp.domain.usecase

import com.uvarov.interviewprepareapp.domain.repository.QuestionRepository
import javax.inject.Inject

class RefreshQuestionsUseCase @Inject constructor(
    private val repository: QuestionRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.refreshQuestions()
    }
}
