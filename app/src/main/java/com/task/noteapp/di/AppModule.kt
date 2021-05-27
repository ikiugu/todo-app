package com.task.noteapp.di

import android.app.Application
import androidx.room.Room
import com.task.noteapp.data.NoteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(app: Application, callback: NoteDatabase.Callback) =
        Room.databaseBuilder(app, NoteDatabase::class.java, "notes_database")
            .fallbackToDestructiveMigration()
            .addCallback(callback)
            .build()

    @Provides
    @Singleton //not necessary because daos are singletons by default
    fun provideNotesDao(db: NoteDatabase) = db.noteDao()

    @Provides
    @Singleton
    fun providesApplicationScope() = CoroutineScope(SupervisorJob())
}