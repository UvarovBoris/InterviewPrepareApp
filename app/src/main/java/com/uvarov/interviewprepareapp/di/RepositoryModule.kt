package com.uvarov.interviewprepareapp.di

import com.uvarov.interviewprepareapp.data.repository.QuestionRepositoryImpl
import com.uvarov.interviewprepareapp.domain.repository.QuestionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindQuestionRepository(
        impl: QuestionRepositoryImpl
    ): QuestionRepository
}
