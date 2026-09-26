package com.uvarov.interviewprepareapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val difficulty: String,
    val answerSummary: String,
    val codeExample: String?,
    val isBookmarked: Boolean
)
