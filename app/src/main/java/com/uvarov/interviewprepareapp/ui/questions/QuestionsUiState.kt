package com.uvarov.interviewprepareapp.ui.questions

import androidx.compose.runtime.Immutable
import com.uvarov.interviewprepareapp.domain.model.InterviewQuestion
import com.uvarov.interviewprepareapp.domain.model.QuestionCategory

@Immutable
data class QuestionsUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val questions: List<InterviewQuestion> = emptyList(),
    val selectedCategory: QuestionCategory? = QuestionCategory.ALL,
    val errorMessage: String? = null
)
