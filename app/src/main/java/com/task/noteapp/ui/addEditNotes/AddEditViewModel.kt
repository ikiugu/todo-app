package com.task.noteapp.ui.addEditNotes

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.task.noteapp.data.Note
import com.task.noteapp.data.NoteDao
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class AddEditViewModel @ViewModelInject constructor(
    private val noteDao: NoteDao,
    @Assisted private val state: SavedStateHandle
) : ViewModel() {
    val note = state.get<Note>("note")

    var noteTitle = state.get<String>("noteTitle") ?: note?.title ?: ""
        set(value) {
            field = value
            state.set("noteTitle", value)
        }

    var noteDescription = state.get<String>("noteDescription") ?: note?.description ?: ""
        set(value) {
            field = value
            state.set("noteDescription", value)
        }

    var noteImageUrl = state.get<String>("noteImageUrl") ?: note?.imageUrl ?: ""
        set(value) {
            field = value
            state.set("noteImageUrl", value)
        }


    private val addEditNotesChannel = Channel<AddEditNotesEvent>()
    val addEditNotesEvent = addEditNotesChannel.receiveAsFlow()

    fun onDeleteNote(noteId: Int) = viewModelScope.launch {
        val note = noteDao.getNote(noteId)
        noteDao.delete(note)
        addEditNotesChannel.send(AddEditNotesEvent.NavigateToNotesScreenOnDelete)
    }

    private fun createNote(note: Note) = viewModelScope.launch {
        noteDao.insert(note)
        addEditNotesChannel.send(AddEditNotesEvent.NavigateToNotesScreenOnCreate)
    }

    private fun updateNote(note: Note) = viewModelScope.launch {
        noteDao.update(note)
        addEditNotesChannel.send(AddEditNotesEvent.NavigateToNotesScreenOnUpdate)
    }

    fun onSaveNote() {
        if (noteTitle.isBlank()) {
            return
        }

        if (note != null) {
            val updatedNote =
                note.copy(
                    title = noteTitle,
                    description = noteDescription,
                    imageUrl = noteImageUrl,
                    updated = System.currentTimeMillis()
                )
            updateNote(updatedNote)
        } else {
            val note =
                Note(title = noteTitle, description = noteDescription, imageUrl = noteImageUrl)
            createNote(note)
        }
    }

    sealed class AddEditNotesEvent {
        object NavigateToNotesScreenOnDelete : AddEditNotesEvent()
        object NavigateToNotesScreenOnCreate : AddEditNotesEvent()
        object NavigateToNotesScreenOnUpdate : AddEditNotesEvent()
    }

}