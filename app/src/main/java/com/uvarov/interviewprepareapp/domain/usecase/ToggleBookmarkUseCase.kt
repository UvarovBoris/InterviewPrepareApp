package com.uvarov.interviewprepareapp.domain.usecase

import com.uvarov.interviewprepareapp.domain.repository.QuestionRepository
import javax.inject.Inject

class ToggleBookmarkUseCase @Inject constructor(
    private val repository: QuestionRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> {
        return repository.toggleBookmark(id)
    }
}
