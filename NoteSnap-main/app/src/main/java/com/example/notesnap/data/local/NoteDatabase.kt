package com.example.notesnap.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [NoteEntity::class], version = 2, exportSchema = false)
abstract class NoteDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: NoteDatabase? = null

        fun getDatabase(context: Context): NoteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "notesnap.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(NoteDatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class NoteDatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database.noteDao())
                    }
                }
            }

            private suspend fun populateInitialData(dao: NoteDao) {
                val now = System.currentTimeMillis()
                val hour = 3_600_000L
                val sampleNotes = listOf(
                    NoteEntity(
                        title = "Photosynthesis Summary",
                        content = "Photosynthesis is the process by which green plants and some other organisms use sunlight to synthesize foods from carbon dioxide and water. It takes place in the chloroplasts of plant cells and produces glucose as well as oxygen.",
                        category = "Science",
                        createdAt = now - 24 * hour,
                        updatedAt = now - 24 * hour
                    ),
                    NoteEntity(
                        title = "Lecture 3 - Data Structures",
                        content = "Arrays, linked lists, stacks and queues are linear data structures. Trees and graphs are non-linear. Choose a structure based on the operations you need most frequently.",
                        category = "Computer Science",
                        createdAt = now - 48 * hour,
                        updatedAt = now - 12 * hour
                    ),
                    NoteEntity(
                        title = "History Reviewer - World War II",
                        content = "World War II lasted from 1939 to 1945 and involved the Allied Powers and the Axis Powers. Key turning points include the Battle of Stalingrad and D-Day.",
                        category = "History",
                        createdAt = now - 72 * hour,
                        updatedAt = now - 72 * hour
                    ),
                    NoteEntity(
                        title = "Math Formulas",
                        content = "Quadratic formula: x = (-b +/- sqrt(b^2 - 4ac)) / 2a. Area of a circle: A = pi * r^2. Pythagorean theorem: a^2 + b^2 = c^2.",
                        category = "Mathematics",
                        createdAt = now - 96 * hour,
                        updatedAt = now - 96 * hour
                    ),
                    NoteEntity(
                        title = "Lab Report Checklist",
                        content = "1. Title page  2. Abstract  3. Introduction  4. Materials and methods  5. Results  6. Discussion  7. References. Remember to cite all sources.",
                        category = "Academics",
                        createdAt = now - 120 * hour,
                        updatedAt = now - 120 * hour
                    )
                )
                for (note in sampleNotes) {
                    dao.insertNote(note)
                }
            }
        }
    }
}
