package org.craftedsw.katas.reactive.infrastructure;

import org.craftedsw.katas.reactive.application.WordService;
import org.craftedsw.katas.reactive.domain.Word;
import org.craftedsw.katas.reactive.domain.WordRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.craftedsw.katas.reactive.infrastructure.AppTestConfig.DEFAULT_USER_1;
import static org.craftedsw.katas.reactive.infrastructure.AppTestConfig.DEFAULT_USER_2;
import static org.mockito.Mockito.*;

/**
 *
 *
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = AppTestConfig.class)
public class WordServiceImplTest {

    @MockBean
    private WordRepository repo;

    @Autowired
    private WordService wordService;

    @Before
    public void setUp() {
        wordService = new WordServiceImpl(repo);
    }

    @Test(expected = AssertionError.class)
    public void subscriber_should_subscribe_to_infinite_stream() {
        when(repo.getHistory()).thenReturn(Collections.emptyList());
        Flux<Word> words = wordService.register(DEFAULT_USER_1);

        assertThat(words).isNotNull();
        StepVerifier.withVirtualTime(() -> words)
                .expectSubscription()
                .expectComplete()
                .verify(Duration.ofSeconds(1));
    }

    @Test
    public void subscriber_should_subscribe_to_empty_stream_at_beginning() {
        when(repo.getHistory()).thenReturn(Collections.emptyList());
        Flux<Word> words = wordService.register(DEFAULT_USER_1);

        assertThat(words).isNotNull();
        StepVerifier.create(words)
                .expectSubscription()
                .thenCancel()
                .verifyThenAssertThat().hasNotDroppedElements();
    }

    @Test
    public void subscriber_should_receive_words_history_at_subscription() {
        List<Word> words = Word.listOf("word", 10);
        when(repo.getHistory()).thenReturn(words);

        StepVerifier.create(wordService.register(DEFAULT_USER_1))
                .expectNextSequence(words)
                .thenCancel()
                .verifyThenAssertThat()
                .hasNotDroppedErrors()
                .hasNotDiscardedElements();
    }

    @Test
    public void multiple_subscription_should_retrieve_word_history_only_once() {
        List<Word> words = Word.listOf("word", 10);
        when(repo.getHistory()).thenReturn(words);

        StepVerifier.create(wordService.register(DEFAULT_USER_1))
                .expectSubscription()
                .expectNextSequence(words)
                .thenCancel()
                .verifyThenAssertThat()
                .hasNotDroppedElements()
                .hasNotDiscardedElements()
                .hasNotDroppedErrors();

        StepVerifier.create(wordService.register(DEFAULT_USER_1))
                .expectSubscription()
                .thenCancel()
                .verifyThenAssertThat().hasNotDroppedElements();
    }


    @Test(expected = IllegalArgumentException.class)
    public void add_null_word_should_throw_exception() {
        wordService.register(DEFAULT_USER_1).subscribe();

        wordService.publish(null);
        verify(repo, times(0)).addWord(null);
    }

    @Test
    public void add_word_does_not_throw_any_exception() {
        List<Word> words = Word.listOf("word", 10);
        when(repo.getHistory()).thenReturn(words);

        wordService.register(DEFAULT_USER_1).subscribe();

        assertThatCode(() -> wordService.publish(new Word("plop")))
                .doesNotThrowAnyException();

    }


    @Test
    public void subscriber_should_receive_new_word_since_their_subscription() {
        List<Word> words = Word.listOf("word", 10);
        when(repo.getHistory()).thenReturn(words);

        Flux<Word> wordsStream = wordService.register(DEFAULT_USER_1);
        Word w1 = new Word("w1");
        Word w2 = new Word("w2");

        StepVerifier.create(wordsStream)
                .expectSubscription()
                .expectNextSequence(words)
                .then(() -> wordService.publish(w1))
                .expectNext(w1)
                .then(() -> wordService.publish(w2))
                .expectNext(w2)
                .thenCancel()
                .verifyThenAssertThat()
                .hasNotDiscardedElements()
                .hasNotDroppedElements();

    }

    @Test(timeout = 3000)
    public void sequential_call_to_register_should_restart_history() {
        List<Word> words = Word.listOf("word", 10);
        when(repo.getHistory()).thenReturn(words);

        Flux<Word> wordsStream1 = wordService.register(DEFAULT_USER_1);

        StepVerifier.create(wordsStream1)
                .expectSubscription()
                .expectNext(words.get(0))
                // .consumeNextWith(word -> assertThat(word).isEqualTo(words.get(0)))
                .thenCancel()
                .verifyThenAssertThat()
                .hasDroppedElements()
                .hasDropped(words.get(1));

        Flux<Word> wordsStream2 = wordService.register(DEFAULT_USER_1);
        StepVerifier.create(wordsStream2)
                .expectSubscription()
                .expectNext(words.get(0))
                .thenCancel()
                .verifyThenAssertThat()
                .hasDroppedElements()
                .hasDropped(words.get(1));;

    }

    @Test
    public void multiple_call_should_return_full_words_history() {
        List<Word> words = Word.listOf("word", 10);
        when(repo.getHistory()).thenReturn(words);

        Flux<Word> wordsStream1 = wordService.register(DEFAULT_USER_1);

        StepVerifier.create(wordsStream1)
                .expectSubscription()
                .consumeNextWith(word -> assertThat(word).isNotNull())
                .thenCancel()
                .verifyThenAssertThat()
                .hasDroppedElements();

        Flux<Word> wordsStream2 = wordService.register(DEFAULT_USER_2);

        StepVerifier.create(wordsStream2)
                .expectSubscription()
                .expectNextSequence(words)
                .thenCancel()
                .verifyThenAssertThat()
                .hasNotDroppedElements();
    }

}
