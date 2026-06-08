package ru.sagenotes.indexservice.domain.usecase

import ru.sagenotes.indexservice.domain.repository.IndexRepository

interface IndexUseCase {
    suspend operator fun invoke(noteId: String, text: String, userId: String)
}

class IndexUseCaseImpl(
    private val repository: IndexRepository
) : IndexUseCase {
    override suspend fun invoke(noteId: String, text: String, userId: String) {
        try {
            repository.index(noteId, text, userId)

        } catch (e: Exception) {
            println(e)
        }
    }
}