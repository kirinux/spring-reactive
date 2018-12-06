package org.craftedsw.katas.reactive.exposition;

import org.craftedsw.katas.reactive.application.WordService;
import org.craftedsw.katas.reactive.domain.User;
import org.craftedsw.katas.reactive.domain.Word;
import org.craftedsw.katas.reactive.infrastructure.AppTestConfig;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

/**
 *
 *
 */
@RunWith(SpringRunner.class)
@WebFluxTest()
@ContextConfiguration(classes = AppTestConfig.class)
public class WordResourcesControllerTest {

    @Autowired
    private WebTestClient client;

    @MockBean
    private WordService wordService;


    @Test
    @Ignore("a voir plus tard sur la gestion des exceptions")
    public void registration_error_should_return_http_5xx() {
        Mockito.when(wordService.register(new User("plop"))).thenThrow(new IllegalArgumentException("word service not available"));
        client.get().uri("/register")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError();
    }


    @Test
    public void register_should_return_http_2xx() {
        client.get().uri(uriBuilder -> uriBuilder.path("/register")
                .queryParam("userName", "plop").build())
                .accept(MediaType.APPLICATION_STREAM_JSON)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    public void register_should_return_existing_words() {
        int wordsListSize = 5;
        Mockito.when(wordService.register(new User("plop"))).thenReturn(Flux.fromIterable(Word.listOf("plop", wordsListSize)));
        client.get().uri(uriBuilder -> uriBuilder.path("/register")
                .queryParam("userName", "plop").build())
                .accept(MediaType.APPLICATION_STREAM_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Word.class)
                .hasSize(wordsListSize)
                .contains(new Word("plop1"));
    }


    @Test
    public void add_word_should_return_http_2xx() {
        client.post().uri("/publish")
                .syncBody(new Word("plop"))
                .exchange().expectStatus().isOk();
    }
}
