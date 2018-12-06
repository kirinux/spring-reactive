package org.craftedsw.katas.reactive.exposition;

import org.craftedsw.katas.reactive.application.WordService;
import org.craftedsw.katas.reactive.domain.User;
import org.craftedsw.katas.reactive.domain.Word;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 *
 *
 */
@RestController
public class WordResourcesController {

    public static final String REGISTER_ENDPOINT = "/register";
    public static final String PUBLISH_ENDPOINT = "/publish";

    public static final String REGISTER_USERNAME_PARAM = "userName";

    private static final Logger LOGGER = LoggerFactory.getLogger(WordResourcesController.class);

    private WordService wordService;

    @Autowired
    public WordResourcesController(WordService wordService) {
        this.wordService = wordService;
    }

    @GetMapping(value = "/register", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<Word> register(@RequestParam String userName) {
        LOGGER.info("register user {}", userName);
        return wordService.register(new User(userName));
    }

    @PostMapping(value = "/publish" )
    public void publish(@RequestBody Word word) {
        LOGGER.info("Publish word {}", word);
        wordService.publish(word);
    }




}
