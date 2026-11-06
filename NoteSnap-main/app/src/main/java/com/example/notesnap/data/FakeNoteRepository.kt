package com.example.notesnap.data

import com.example.notesnap.data.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeNoteRepository : NoteRepository {

    private val notes = MutableStateFlow(sampleNotes())
    private var nextId = notes.value.size + 1L

    override fun observeNotes(): Flow<List<Note>> = notes

    override suspend fun getNote(id: Long): Note? = notes.value.find { it.id == id }

    override suspend fun insertNote(note: Note): Long {
        val id = nextId++
        notes.value = notes.value + note.copy(id = id, createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis())
        return id
    }

    override suspend fun updateNote(note: Note) {
        notes.value = notes.value.map {
            if (it.id == note.id) note.copy(updatedAt = System.currentTimeMillis()) else it
        }
    }

    override suspend fun deleteNote(note: Note) {
        notes.value = notes.value.filterNot { it.id == note.id }
    }

    override fun searchNotes(query: String): Flow<List<Note>> = notes.map { list ->
        if (query.isBlank()) list
        else list.filter {
            it.title.contains(query, ignoreCase = true) ||
                it.content.contains(query, ignoreCase = true)
        }
    }

    private fun sampleNotes(): List<Note> {
        val now = System.currentTimeMillis()
        val hour = 3_600_000L
        return listOf(
            Note(
                id = 1,
                title = "Photosynthesis Summary",
                content = "Photosynthesis is the process by which green plants and " +
                    "some other organisms use sunlight to synthesize foods from " +
                    "carbon dioxide and water. It takes place in the chloroplasts " +
                    "of plant cells and produces glucose as well as oxygen.",
                category = "Science",
                createdAt = now - 24 * hour,
                updatedAt = now - 24 * hour,
            ),
            Note(
                id = 2,
                title = "Lecture 3 - Data Structures",
                content = "Arrays, linked lists, stacks and queues are linear data " +
                    "structures. Trees and graphs are non-linear. Choose a structure " +
                    "based on the operations you need most frequently.",
                category = "Computer Science",
                createdAt = now - 48 * hour,
                updatedAt = now - 12 * hour,
            ),
            Note(
                id = 3,
                title = "History Reviewer - World War II",
                content = "World War II lasted from 1939 to 1945 and involved the " +
                    "Allied Powers and the Axis Powers. Key turning points include " +
                    "the Battle of Stalingrad and D-Day.",
                category = "History",
                createdAt = now - 72 * hour,
                updatedAt = now - 72 * hour,
            ),
            Note(
                id = 4,
                title = "Math Formulas",
                content = "Quadratic formula: x = (-b +/- sqrt(b^2 - 4ac)) / 2a. " +
                    "Area of a circle: A = pi * r^2. Pythagorean theorem: " +
                    "a^2 + b^2 = c^2.",
                category = "Mathematics",
                createdAt = now - 96 * hour,
                updatedAt = now - 96 * hour,
            ),
            Note(
                id = 5,
                title = "Lab Report Checklist",
                content = "1. Title page  2. Abstract  3. Introduction  " +
                    "4. Materials and methods  5. Results  6. Discussion  " +
                    "7. References. Remember to cite all sources.",
                category = "Academics",
                createdAt = now - 120 * hour,
                updatedAt = now - 120 * hour,
            ),
        )
    }
}
