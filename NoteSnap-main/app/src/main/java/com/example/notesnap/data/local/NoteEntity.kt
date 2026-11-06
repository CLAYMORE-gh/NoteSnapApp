package com.example.notesnap.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.notesnap.data.model.Note

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val content: String,
    val category: String = "General",
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toNote() = Note(id, title, content, category, createdAt, updatedAt)

    companion object {
        fun fromNote(note: Note) = NoteEntity(
            id = note.id,
            title = note.title,
            content = note.content,
            category = note.category,
            createdAt = note.createdAt,
            updatedAt = note.updatedAt
        )
    }
}
