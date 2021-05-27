package com.task.noteapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    /*@Query("SELECT * FROM notes_table where title LIKE '%' || :searchString || '%'")
    fun getNotes(searchString: String): Flow<List<Note>>*/ //original search query

    fun getNotes(
        searchString: String,
        sortPriority: SortPriority,
        hideCompleted: Boolean
    ): Flow<List<Note>> {
        return when (sortPriority) {
            SortPriority.BY_TITLE -> getNotesSortedByTitle(searchString, hideCompleted)
            SortPriority.BY_CREATED_DATE -> getNotesSortedByDateCreated(searchString, hideCompleted)
            SortPriority.BY_UPDATED_DATE -> getNotesSortedByDateUpdated(searchString, hideCompleted)
        }
    }

    @Query("SELECT * FROM notes_table WHERE (completed != :hideCompleted OR completed = 0) AND title LIKE '%' || :searchString || '%' ORDER BY title")
    fun getNotesSortedByTitle(searchString: String, hideCompleted: Boolean): Flow<List<Note>>

    @Query("SELECT * FROM notes_table WHERE (completed != :hideCompleted OR completed = 0) AND title LIKE '%' || :searchString || '%' ORDER BY created")
    fun getNotesSortedByDateCreated(searchString: String, hideCompleted: Boolean): Flow<List<Note>>

    @Query("SELECT * FROM notes_table WHERE (completed != :hideCompleted OR completed = 0) AND title LIKE '%' || :searchString || '%' ORDER BY updated")
    fun getNotesSortedByDateUpdated(searchString: String, hideCompleted: Boolean): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note)

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

    @Query("DELETE FROM notes_table WHERE completed == 1")
    suspend fun deleteAllCompleted()

    @Query("SELECT * FROM notes_table WHERE id == :noteId")
    suspend fun getNote(noteId: Int): Note
}