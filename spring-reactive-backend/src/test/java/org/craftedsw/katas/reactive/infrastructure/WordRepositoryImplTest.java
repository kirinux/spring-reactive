package org.craftedsw.katas.reactive.infrastructure;

import org.craftedsw.katas.reactive.domain.Word;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 *
 */
public class WordRepositoryImplTest {

    private WordRepositoryImpl repo;

    @Before
    public void setUp() {
        this.repo = new WordRepositoryImpl();
    }


    @Test
    public void new_repository_should_be_empty() {
        List<Word> words = repo.getHistory();
        assertThat(words).isNotNull().isEmpty();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void words_list_should_be_unmodifiable() {
        repo.addWord(new Word("plop"));
        List words = repo.getHistory();
        words.remove(0);
    }

    @Test
    public void should_retrieve_all_words() {
        int expectedWordCount = 10;
        Stream.iterate(0, i -> i + 1)
                .limit(expectedWordCount)
                .forEach(i -> repo.addWord(new Word("word" + i)));

        assertThat(repo.getHistory()).isNotNull().isNotEmpty()
                .hasSize(expectedWordCount);
    }


    @Test
    public void words_list_size_should_increase_with_new_word() {
        int startSize = repo.getHistory().size();
        repo.addWord(new Word("first"));
        assertThat(repo.getHistory()).hasSize(startSize + 1);
    }

    @Test
    public void words_list_should_contains_exact_words() {
        Word first = new Word("first");
        Word second = new Word("second");
        Word third = new Word("third");
        repo.addWord(first);
        repo.addWord(second);
        repo.addWord(third);

        assertThat(repo.getHistory())
                .containsExactly(first, second, third);
    }


    @Test
    public void add_word_should_preserve_insertions_order() {
        Word first = new Word("first");
        repo.addWord(first);
        assertThat(repo.getHistory()).last().isEqualTo(first);

        Word second = new Word("second");
        repo.addWord(second);
        Word third = new Word("third");
        repo.addWord(third);
        assertThat(repo.getHistory()).last().isEqualTo(third);
    }

}
