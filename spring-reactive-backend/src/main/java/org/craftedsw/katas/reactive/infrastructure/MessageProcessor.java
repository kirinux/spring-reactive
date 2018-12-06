package org.craftedsw.katas.reactive.infrastructure;

import org.craftedsw.katas.reactive.domain.Word;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 */
public class MessageProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageProcessor.class);

    @FunctionalInterface
    public interface WordListener {
        void onWord(Word word);
    }

    private final List<WordListener> listeners = new ArrayList<>();


    public void register(WordListener wordListener) {
        LOGGER.info("Register listener on {}", this);
        listeners.add(wordListener);
    }

    public void publishAll(Word word) {
        LOGGER.debug("Publish on {} listeners", listeners.size());
        listeners.stream().forEach(l -> l.onWord(word));
    }
}
