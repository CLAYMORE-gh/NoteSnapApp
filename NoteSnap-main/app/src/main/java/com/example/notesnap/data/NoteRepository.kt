package com.example.notesnap.data

import com.example.notesnap.data.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {

    fun observeNotes(): Flow<List<Note>>

    suspend fun getNote(id: Long): Note?

    suspend fun insertNote(note: Note): Long

    suspend fun updateNote(note: Note)

    suspend fun deleteNote(note: Note)

    fun searchNotes(query: String): Flow<List<Note>>
}
