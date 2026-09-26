package com.uvarov.interviewprepareapp.domain.repository

import com.uvarov.interviewprepareapp.domain.model.InterviewQuestion
import com.uvarov.interviewprepareapp.domain.model.QuestionCategory
import kotlinx.coroutines.flow.Flow

interface QuestionRepository {
    fun getQuestions(category: QuestionCategory?): Flow<List<InterviewQuestion>>
    suspend fun refreshQuestions(): Result<Unit>
    suspend fun toggleBookmark(id: String): Result<Unit>
}
