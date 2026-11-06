package com.example.notesnap.data.model

data class Note(
    val id: Long = 0L,
    val title: String,
    val content: String,
    val category: String = "General",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
