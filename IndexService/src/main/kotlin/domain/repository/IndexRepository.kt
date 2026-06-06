package ru.sagenotes.indexservice.domain.repository

interface IndexRepository {
    suspend fun index(noteId: String, text: String, userId: String)
}