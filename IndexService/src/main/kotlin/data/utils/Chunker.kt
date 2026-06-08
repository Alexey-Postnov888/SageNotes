package ru.sagenotes.indexservice.data.utils

interface Chunker {
    fun chunk(text: String): List<String>
}

class ChunkerImpl : Chunker {
    private val chunkSize: Int = 42
    private val overlap: Int = 10

    override fun chunk(text: String): List<String> {
        if (text.length <= chunkSize) return listOf(text)

        val chunks = mutableListOf<String>()
        var start = 0

        while (start < text.length) {
            val end = minOf(start + chunkSize, text.length)
            var chunk = text.substring(start, end)

            if (end < text.length) {
                val lastSentence = chunk.lastIndexOfAny(listOf(". ", "! ", "? ", ".\n", "!\n", "?\n"))
                if (lastSentence > chunkSize / 2) {
                    chunk = chunk.substring(0, lastSentence + 1)
                }
            }

            chunks.add(chunk.trim())
            start += chunk.length - overlap
        }

        return chunks
    }

    private fun String.lastIndexOfAny(strings: List<String>): Int {
        return strings.map { lastIndexOf(it) }.maxOrNull() ?: -1
    }
}