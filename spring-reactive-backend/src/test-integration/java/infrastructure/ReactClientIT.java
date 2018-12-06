package infrastructure;

import org.craftedsw.katas.reactive.domain.Word;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import reactor.core.Disposable;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craftedsw.katas.reactive.exposition.WordResourcesController.*;
import static org.craftedsw.katas.reactive.infrastructure.AppTestConfig.DEFAULT_USER_NAME_1;
import static org.craftedsw.katas.reactive.infrastructure.AppTestConfig.DEFAULT_USER_NAME_2;

/**
 *
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = AppTestConfig.class)
public class ReactClientIT {

    @LocalServerPort
    private int port;

    private String createURLWithPort() {
        return "http://localhost:" + port;
    }


    private URI buildRegisterURI() {
        return new DefaultUriBuilderFactory(createURLWithPort()).builder()
                .path(REGISTER_ENDPOINT)
                .queryParam(REGISTER_USERNAME_PARAM, DEFAULT_USER_NAME_1).build();
    }

    private void publishWordsList(WebClient client, List<Word> words) throws InterruptedException {
        while (!words.isEmpty()) {
            client.post()
                    .uri(uriBuilder -> uriBuilder.path(PUBLISH_ENDPOINT).build())
                    .syncBody(words.remove(0))
                    .accept(MediaType.APPLICATION_STREAM_JSON)
                    .retrieve().bodyToMono(Void.class).subscribe();

            TimeUnit.SECONDS.sleep(2);
        }
    }

    @Test
    public void multiple_subscriber_receive_words() throws InterruptedException {
        WebClient client = WebClient.create(createURLWithPort());

        ArrayList<Word> expectedList1 = new ArrayList<>();
        WebClient.RequestHeadersSpec<?> registerClientCaller =
                client.get().uri(buildRegisterURI())
                        .accept(MediaType.APPLICATION_STREAM_JSON);

        Disposable subscriber1 = registerClientCaller
                .exchange()
                .flatMapMany(clientResponse -> clientResponse.bodyToFlux(Word.class))
                .subscribe(word -> expectedList1.add(word));

        ArrayList<Word> expectedList2 = new ArrayList<>();
        Disposable subscriber2 = registerClientCaller
                .exchange()
                .flatMapMany(clientResponse -> clientResponse.bodyToFlux(Word.class))
                .subscribe(word -> expectedList2.add(word));

        List<Word> source = Word.listOf("ITReactTest", 5);
        List<Word> copy = new ArrayList<>(source);
        publishWordsList(client, copy);

        subscriber1.dispose();
        subscriber2.dispose();
        assertThat(expectedList1).containsAll(source);
        assertThat(expectedList2).containsAll(source);

    }


    @Test
    public void multiple_subscriber_receive_all_words_async() throws InterruptedException {
        WebClient client = WebClient.create(createURLWithPort());
        List<Word> firstWords = Word.listOf("ITReactTest1", 5);
        List<Word> firstWordsSource = new ArrayList<>(firstWords);

        publishWordsList(client, firstWords);

        ArrayList<Word> expectedList1 = new ArrayList<>();
        Disposable subscriber1 = client.get().uri(uriBuilder -> uriBuilder
                .path(REGISTER_ENDPOINT)
                .queryParam(REGISTER_USERNAME_PARAM, DEFAULT_USER_NAME_1).build())
                .accept(MediaType.APPLICATION_STREAM_JSON)
                .exchange()
                .flatMapMany(clientResponse -> clientResponse.bodyToFlux(Word.class))
                .subscribe(word -> expectedList1.add(word));

        TimeUnit.SECONDS.sleep(2);
        assertThat(expectedList1).containsExactlyInAnyOrderElementsOf(firstWordsSource);

        ArrayList<Word> expectedList2 = new ArrayList<>();
        Disposable subscriber2 = client.get().uri(uriBuilder -> uriBuilder
                .path(REGISTER_ENDPOINT)
                .queryParam(REGISTER_USERNAME_PARAM, DEFAULT_USER_NAME_2).build())
                .accept(MediaType.APPLICATION_STREAM_JSON)
                .exchange()
                .flatMapMany(clientResponse -> clientResponse.bodyToFlux(Word.class))
                .subscribe(word -> expectedList2.add(word));


        List<Word> secondWords = Word.listOf("ITReactTest2", 5);
        List<Word> secondWordsSource = new ArrayList<>(secondWords);
        publishWordsList(client, secondWords);

        subscriber1.dispose();
        subscriber2.dispose();
        assertThat(expectedList1).containsAll(secondWordsSource);
        assertThat(expectedList2)
                .containsAll(firstWordsSource)
                .containsAll(secondWordsSource);


    }
}
