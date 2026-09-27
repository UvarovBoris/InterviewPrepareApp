package com.uvarov.interviewprepareapp.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class InterviewQuestion(
    val id: String,
    val title: String,
    val category: QuestionCategory,
    val difficulty: Difficulty,
    val answerSummary: String,
    val codeExample: String? = null
)
