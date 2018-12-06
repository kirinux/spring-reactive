package infrastructure;

import org.craftedsw.katas.reactive.application.WordService;
import org.craftedsw.katas.reactive.domain.Word;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import reactor.core.Disposable;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craftedsw.katas.reactive.domain.Word.listOf;
import static org.craftedsw.katas.reactive.infrastructure.AppTestConfig.DEFAULT_USER_1;
import static org.craftedsw.katas.reactive.infrastructure.AppTestConfig.DEFAULT_USER_2;

/**
 *
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AppTestConfig.class)
public class ServiceRepositoryIT {


    @Autowired
    private WordService wordService;


    @Test
    public void all_subscribers_get_words_history() {
        Word w1 = new Word("w1");
        Word w2 = new Word("w2");

        StepVerifier.create(wordService.register(DEFAULT_USER_1))
                .expectSubscription()
                .then(() -> wordService.publish(w1))
                .expectNext(w1)
                .then(() -> wordService.publish(w2))
                .expectNext(w2)
                .thenCancel()
                .verifyThenAssertThat()
                .hasNotDroppedElements();

        StepVerifier.create(wordService.register(DEFAULT_USER_2))
                .expectSubscription()
                .expectNext(w1)
                .expectNext(w2)
                .thenCancel()
                .verifyThenAssertThat()
                .hasNotDroppedElements();


    }


    @Test
    public void all_subscribers_get_word_update() {
        List<Word> words1 = listOf("word", 5);
        List<Word> words2 = listOf("plop", 5);


        List<Word> wordList1 = new ArrayList<>();
        List<Word> wordList2 = new ArrayList<>();

        Disposable disp1 = wordService.register(DEFAULT_USER_1).subscribe(word -> wordList1.add(word));
        words1.stream().forEach(word -> wordService.publish(word));
        assertThat(wordList1).containsAll(words1);

        Disposable disp2 = wordService.register(DEFAULT_USER_2).subscribe(word -> wordList2.add(word));
        assertThat(wordList2).containsAll(words1);

        words2.stream().forEach(word -> wordService.publish(word));
        disp1.dispose();
        disp2.dispose();


        assertThat(wordList1).containsAll(words1).containsAll(words2);
        assertThat(wordList2).containsAll(words1).containsAll(words2);
    }

}
