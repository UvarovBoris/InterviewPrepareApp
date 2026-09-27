package com.uvarov.interviewprepareapp.ui.questions

import com.uvarov.interviewprepareapp.domain.model.QuestionCategory

sealed interface QuestionsUiEvent {
    data class SelectCategory(val category: QuestionCategory?) : QuestionsUiEvent
    data object Refresh : QuestionsUiEvent
    data object DismissError : QuestionsUiEvent
}
