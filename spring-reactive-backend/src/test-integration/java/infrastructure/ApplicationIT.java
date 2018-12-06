package infrastructure;

import org.assertj.core.api.Assertions;
import org.craftedsw.katas.reactive.domain.Word;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static org.craftedsw.katas.reactive.exposition.WordResourcesController.*;
import static org.craftedsw.katas.reactive.infrastructure.AppTestConfig.DEFAULT_USER_NAME_1;
import static org.craftedsw.katas.reactive.infrastructure.AppTestConfig.DEFAULT_USER_NAME_2;

/**
 *
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppTestConfig.class)
@AutoConfigureWebTestClient
public class ApplicationIT {

    @Autowired
    private WebTestClient client;

    @Test
    public void publish_word_should_publish_to_all_subscriber() {
        List<Word> wordsList = Word.listOf("plop", 10);

        WebTestClient.RequestBodySpec publishWordsSpec = client.post().uri(PUBLISH_ENDPOINT);
        wordsList.stream()
                .forEach(word -> publishWordsSpec.syncBody(word).exchange().expectStatus().isOk());


        Flux<Word> wordsStream1 = client.get()
                .uri(uriBuilder -> uriBuilder.path(REGISTER_ENDPOINT)
                        .queryParam(REGISTER_USERNAME_PARAM, DEFAULT_USER_NAME_1).build())
                .accept(MediaType.APPLICATION_STREAM_JSON)
                .exchange()
                .expectStatus().isOk()
                .returnResult(Word.class).getResponseBody();

        StepVerifier.create(wordsStream1)
                .expectNextSequence(wordsList)
                .thenCancel()
                .verifyThenAssertThat().hasNotDroppedElements();

       Word next = new Word("next1");
        Word next2 = new Word("next2");
        publishWordsSpec.syncBody(next).exchange().expectStatus().isOk();


        Flux<Word> wordsStream2 = client.get()
                .uri(uriBuilder -> uriBuilder.path(REGISTER_ENDPOINT)
                        .queryParam(REGISTER_USERNAME_PARAM, DEFAULT_USER_NAME_2).build())
                .accept(MediaType.APPLICATION_STREAM_JSON)
                .exchange()
                .expectStatus().isOk()
                .returnResult(Word.class).getResponseBody();

        StepVerifier.create(wordsStream2)
                .recordWith(ArrayList::new)
                .expectNextCount(wordsList.size())
                .consumeRecordedWith(words -> Assertions.assertThat(words).containsSequence(wordsList))
                .consumeNextWith(word -> Assertions.assertThat(word).isEqualTo(next))
                .then(() ->  publishWordsSpec.syncBody(next2).exchange().expectStatus().isOk())
                .consumeNextWith(word -> Assertions.assertThat(word).isEqualTo(next2))
                .thenCancel()
                .verify();

    }



}
