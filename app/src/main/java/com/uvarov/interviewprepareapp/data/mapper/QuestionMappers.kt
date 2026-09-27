package com.uvarov.interviewprepareapp.data.mapper

import com.uvarov.interviewprepareapp.data.local.entity.QuestionEntity
import com.uvarov.interviewprepareapp.data.remote.dto.QuestionDto
import com.uvarov.interviewprepareapp.domain.model.Difficulty
import com.uvarov.interviewprepareapp.domain.model.InterviewQuestion
import com.uvarov.interviewprepareapp.domain.model.QuestionCategory

fun QuestionDto.toEntity(): QuestionEntity {
    return QuestionEntity(
        id = id,
        title = title,
        category = category,
        difficulty = difficulty,
        answerSummary = answerSummary,
        codeExample = codeExample
    )
}

fun QuestionEntity.toDomain(): InterviewQuestion {
    val domainCategory = runCatching { QuestionCategory.valueOf(category) }
        .getOrDefault(QuestionCategory.ANDROID)

    val domainDifficulty = runCatching { Difficulty.valueOf(difficulty) }
        .getOrDefault(Difficulty.MEDIUM)

    return InterviewQuestion(
        id = id,
        title = title,
        category = domainCategory,
        difficulty = domainDifficulty,
        answerSummary = answerSummary,
        codeExample = codeExample
    )
}

fun InterviewQuestion.toEntity(): QuestionEntity {
    return QuestionEntity(
        id = id,
        title = title,
        category = category.name,
        difficulty = difficulty.name,
        answerSummary = answerSummary,
        codeExample = codeExample
    )
}
