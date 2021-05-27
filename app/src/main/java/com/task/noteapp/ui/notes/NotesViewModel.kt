package com.task.noteapp.ui.notes

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.task.noteapp.data.Note
import com.task.noteapp.data.NoteDao
import com.task.noteapp.data.PreferenceManager
import com.task.noteapp.data.SortPriority
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class NotesViewModel @ViewModelInject constructor( //@HiltViewModel not working
    private val notesDao: NoteDao,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    val searchString = MutableStateFlow("")

    var selectionFlow = preferenceManager.preferencesFlow

    private val notesEventChannel = Channel<NotesEvent>()
    val notesEvent = notesEventChannel.receiveAsFlow()

    private val notesFlow = combine(
        searchString,
        selectionFlow
    ) { searchString, selectionFlow ->
        Pair(searchString, selectionFlow)
    }.flatMapLatest { (searchString, selectionFlow) ->
        notesDao.getNotes(searchString, selectionFlow.sortPriority, selectionFlow.hideCompleted)
    }

    val notes = notesFlow.asLiveData()

    fun onAddNewNoteClick() = viewModelScope.launch {
        notesEventChannel.send(NotesEvent.NavigateToAddNoteScreen)
    }

    fun onNoteSelected(note: Note) = viewModelScope.launch {
        notesEventChannel.send(NotesEvent.NavigateToNoteEditScreen(note))
    }

    fun onNoteCompleted(note: Note) = viewModelScope.launch {
        notesDao.update(note.copy(completed = true))
        notesEventChannel.send(NotesEvent.ShowUpdateCompleteMessage(note))
    }

    fun onNoteUpdated(note: Note) = viewModelScope.launch {
        notesDao.update(note.copy(completed = false))
    }

    fun onNoteSwiped(note: Note) = viewModelScope.launch {
        notesDao.delete(note)
        notesEventChannel.send(NotesEvent.ShowDeleteCompleteMessage(note))

    }

    fun undoDeleteClick(note: Note) = viewModelScope.launch {
        notesDao.insert(note)
    }

    fun deleteAllCompletedNotes() = viewModelScope.launch {
        notesEventChannel.send(NotesEvent.NavigateToDeleteAllCompletedScreen)
    }

    fun onSortOrderSelected(sortPriority: SortPriority) = viewModelScope.launch {
        preferenceManager.updateSortPriority(sortPriority)
    }

    fun onHideCompletedClick(hideCompleted: Boolean) = viewModelScope.launch {
        preferenceManager.updateHideCompleted(hideCompleted)
    }

    sealed class NotesEvent {
        data class ShowUpdateCompleteMessage(val note: Note) : NotesEvent()
        object NavigateToAddNoteScreen : NotesEvent()
        object NavigateToDeleteAllCompletedScreen : NotesEvent()
        data class NavigateToNoteEditScreen(val note: Note) : NotesEvent()
        data class ShowDeleteCompleteMessage(val note: Note) : NotesEvent()
    }
}