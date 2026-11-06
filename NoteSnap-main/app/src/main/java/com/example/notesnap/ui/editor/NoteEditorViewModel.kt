package com.example.notesnap.ui.editor

import android.text.Editable
import android.text.TextWatcher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.notesnap.data.NoteRepository
import com.example.notesnap.data.ServiceLocator
import com.example.notesnap.data.model.Note

class NoteEditorViewModel(private val repository: NoteRepository) : ViewModel() {

    private var originalTitle: String? = null
    private var originalContent: String? = null
    private var originalCategory: String? = null

    suspend fun loadNote(id: Long): Note? {
        val note = repository.getNote(id)
        originalTitle = note?.title
        originalContent = note?.content
        originalCategory = note?.category
        return note
    }

    fun isDirty(noteId: Long, title: String, content: String, category: String): Boolean {
        if (noteId == 0L) return title.isNotBlank() || content.isNotBlank() || category != "General"
        return title != originalTitle || content != originalContent || category != originalCategory
    }

    suspend fun save(noteId: Long, title: String, content: String, category: String) {
        val finalCategory = category.ifBlank { "General" }
        if (noteId == 0L) {
            repository.insertNote(Note(title = title, content = content, category = finalCategory))
        } else {
            val existing = repository.getNote(noteId) ?: return
            repository.updateNote(existing.copy(title = title, content = content, category = finalCategory))
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { NoteEditorViewModel(ServiceLocator.noteRepository) }
        }
    }
}

class SimpleTextWatcher(private val onChanged: () -> Unit) : TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
    override fun afterTextChanged(s: Editable?) = onChanged()
}
