package com.github.frozensync.games.wordsnake

import kotlinx.collections.immutable.persistentSetOf
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors

private val DICTIONARY: Set<String> by lazy {
    Thread.currentThread().contextClassLoader.getResource("basiswoorden-gekeurd.txt")
        ?.let { Path.of(it.toURI()) }
        ?.let { Files.lines(it).collect(Collectors.toUnmodifiableSet()) }
        ?: throw IllegalStateException("Cannot load dictionary")
}

internal data class WordSnake(
    val id: Long,
    val players: List<Player>,
    val currentPlayer: Player = players[0],
    val words: Set<String> = persistentSetOf(),
    val currentWord: String? = null,
    val turn: Int = 1
) {
    fun appendWord(word: String): WordSnake {
        when {
            currentWord != null && currentWord.last() != word.first() -> throw InvalidWordException(""""$word" does not start with the last letter of "$currentWord".""")
            words.contains(word) -> throw InvalidWordException(""""$word" has already been used.""")
            !DICTIONARY.contains(word) -> throw InvalidWordException(""""$word" is not in the dictionary.""")
        }

        return copy(
            currentPlayer = nextPlayer(),
            words = words + word,
            currentWord = word,
            turn = turn + 1
        )
    }

    private fun nextPlayer(): Player {
        val currentIndex = players.indexOf(currentPlayer)
        val nextIndex = if (currentIndex == players.size - 1) 0 else currentIndex + 1
        return players[nextIndex]
    }

    fun getStatistics() = WordSnakeStatistics(this)
}

internal class WordSnakeStatistics(
    private val game: WordSnake,
    val numberOfWords: Int = game.turn - 1,
    val snakeLength: Int = game.words.fold(0) { acc, word -> acc + word.length }
)
