package com.example.notesnap.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.notesnap.data.NoteRepository
import com.example.notesnap.data.ServiceLocator
import com.example.notesnap.data.model.Note
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class NoteListViewModel(private val repository: NoteRepository) : ViewModel() {

    private val query = MutableStateFlow("")

    val notes: StateFlow<List<Note>> = query
        .flatMapLatest { repository.searchNotes(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val currentQuery: String
        get() = query.value

    fun setQuery(value: String) {
        query.value = value
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { NoteListViewModel(ServiceLocator.noteRepository) }
        }
    }
}
