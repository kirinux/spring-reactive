package org.craftedsw.katas.reactive.application;

import org.craftedsw.katas.reactive.domain.User;
import org.craftedsw.katas.reactive.domain.Word;
import reactor.core.publisher.Flux;

/**
 *
 *
 */
public interface WordService {
    Flux<Word> register(User user);

    void publish(Word plop);
}
