package com.uvarov.interviewprepareapp.data.repository

import com.uvarov.interviewprepareapp.data.local.dao.QuestionDao
import com.uvarov.interviewprepareapp.data.mapper.toDomain
import com.uvarov.interviewprepareapp.data.mapper.toEntity
import com.uvarov.interviewprepareapp.data.remote.datasource.QuestionRemoteDataSource
import com.uvarov.interviewprepareapp.domain.model.InterviewQuestion
import com.uvarov.interviewprepareapp.domain.model.QuestionCategory
import com.uvarov.interviewprepareapp.domain.repository.QuestionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class QuestionRepositoryImpl @Inject constructor(
    private val questionDao: QuestionDao,
    private val remoteDataSource: QuestionRemoteDataSource,
) : QuestionRepository {

    override fun getQuestions(category: QuestionCategory?): Flow<List<InterviewQuestion>> {
        val flow = if (category == null || category == QuestionCategory.ALL) {
            questionDao.observeAllQuestions()
        } else {
            questionDao.observeQuestionsByCategory(category.name)
        }
        return flow.map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun refreshQuestions(): Result<Unit> {
        return runCatching {
            val remoteDtos = remoteDataSource.fetchQuestions()
            val entities = remoteDtos.map { it.toEntity() }
            questionDao.insertQuestions(entities)
        }
    }

    override suspend fun toggleBookmark(id: String) {
        questionDao.toggleBookmark(id)
    }
}
