package com.uvarov.interviewprepareapp.di

import com.uvarov.interviewprepareapp.data.remote.datasource.FakeQuestionRemoteDataSourceImpl
import com.uvarov.interviewprepareapp.data.remote.datasource.QuestionRemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {

    @Binds
    @Singleton
    abstract fun bindQuestionRemoteDataSource(
        impl: FakeQuestionRemoteDataSourceImpl
    ): QuestionRemoteDataSource
}
