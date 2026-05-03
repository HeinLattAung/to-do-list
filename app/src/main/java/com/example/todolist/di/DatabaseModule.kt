package com.example.todolist.di

import android.content.Context
import androidx.room.Room
import com.example.todolist.data.local.TaskDatabase
import com.example.todolist.data.local.dao.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing the Room database and the DAO.
 * Repository / ViewModel are wired automatically via @Inject constructors.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideTaskDatabase(
        @ApplicationContext context: Context
    ): TaskDatabase = Room.databaseBuilder(
        context,
        TaskDatabase::class.java,
        TaskDatabase.DATABASE_NAME
    )
        .fallbackToDestructiveMigration()  // Replace with proper migrations in production
        .build()

    @Provides
    fun provideTaskDao(db: TaskDatabase): TaskDao = db.taskDao()
}
