package com.uvarov.interviewprepareapp.data.remote.dto

data class QuestionDto(
    val id: String,
    val title: String,
    val category: String,
    val difficulty: String,
    val answerSummary: String,
    val codeExample: String? = null
)
