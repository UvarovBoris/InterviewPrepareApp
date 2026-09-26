package com.uvarov.interviewprepareapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.uvarov.interviewprepareapp.data.local.dao.QuestionDao
import com.uvarov.interviewprepareapp.data.local.entity.QuestionEntity

@Database(
    entities = [QuestionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao
}
