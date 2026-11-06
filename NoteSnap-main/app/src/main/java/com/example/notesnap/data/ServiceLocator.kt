package com.example.notesnap.data

import android.content.Context
import com.example.notesnap.data.local.NoteDatabase

object ServiceLocator {

    @Volatile
    private var database: NoteDatabase? = null

    lateinit var noteRepository: NoteRepository
        private set

    fun init(context: Context) {
        if (database == null) {
            synchronized(this) {
                if (database == null) {
                    val db = NoteDatabase.getDatabase(context)
                    database = db
                    noteRepository = RoomNoteRepository(db.noteDao())
                }
            }
        }
    }
}
