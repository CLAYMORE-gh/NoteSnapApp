package com.example.notesnap.data

import com.example.notesnap.data.local.NoteDao
import com.example.notesnap.data.local.NoteEntity
import com.example.notesnap.data.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomNoteRepository(private val noteDao: NoteDao) : NoteRepository {

    override fun observeNotes(): Flow<List<Note>> {
        return noteDao.observeNotes().map { entities -> entities.map { it.toNote() } }
    }

    override suspend fun getNote(id: Long): Note? {
        return noteDao.getNoteById(id)?.toNote()
    }

    override suspend fun insertNote(note: Note): Long {
        val now = System.currentTimeMillis()
        val entity = NoteEntity.fromNote(note.copy(createdAt = now, updatedAt = now))
        return noteDao.insertNote(entity)
    }

    override suspend fun updateNote(note: Note) {
        val now = System.currentTimeMillis()
        val entity = NoteEntity.fromNote(note.copy(updatedAt = now))
        noteDao.updateNote(entity)
    }

    override suspend fun deleteNote(note: Note) {
        noteDao.deleteNote(NoteEntity.fromNote(note))
    }

    override fun searchNotes(query: String): Flow<List<Note>> {
        return if (query.isBlank()) {
            observeNotes()
        } else {
            noteDao.searchNotes(query).map { entities -> entities.map { it.toNote() } }
        }
    }
}
