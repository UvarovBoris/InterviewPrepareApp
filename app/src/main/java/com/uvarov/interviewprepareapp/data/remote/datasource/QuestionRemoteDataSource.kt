package com.uvarov.interviewprepareapp.data.remote.datasource

import com.uvarov.interviewprepareapp.data.remote.dto.QuestionDto

interface QuestionRemoteDataSource {
    suspend fun fetchQuestions(): List<QuestionDto>
}
