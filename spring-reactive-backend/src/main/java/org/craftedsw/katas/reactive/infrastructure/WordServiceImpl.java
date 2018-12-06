package org.craftedsw.katas.reactive.infrastructure;

import org.craftedsw.katas.reactive.application.WordService;
import org.craftedsw.katas.reactive.domain.User;
import org.craftedsw.katas.reactive.domain.Word;
import org.craftedsw.katas.reactive.domain.WordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 *
 */
@Service
public class WordServiceImpl implements WordService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WordServiceImpl.class);

    private WordRepository wordRepo;

    private MessageProcessor processor = new MessageProcessor();

    private final HashMap<User, List<Word>> wordsHistorySnapshots = new HashMap<>();

    @Autowired
    public WordServiceImpl(WordRepository wordRepo) {
        this.wordRepo = wordRepo;
    }

    private void deleteHistorySnapshotForUSer(User user) {
        LOGGER.info("Delete history for user {}", user);
        wordsHistorySnapshots.remove(user);
    }

    private void makeHistorySnapshotForUSer(User user) {
        LOGGER.info("Make history snapshot for user {}", user);
        ArrayList<Word> copy = new ArrayList<>(wordRepo.getHistory());
        wordsHistorySnapshots.putIfAbsent(user, copy);
    }

    private long computeRequestMaxSize(long requestedSize, long historySize) {
        long size;
        if (requestedSize > historySize) {
            size = historySize;
        } else {
            size = requestedSize;
        }
        return size;
    }

    private List<Word> computeHistorySubList(long maxRequestSize, List<Word> userCurrentHistory) {
        ArrayList<Word> sublist = new ArrayList<>();
        int count = 0;
        while (count < maxRequestSize) {
            sublist.add(userCurrentHistory.remove(0));
            count++;
        }
        return sublist;
    }

    private List<Word> getHistoryForUser(User user, long requestedSize) {
        if (!wordsHistorySnapshots.containsKey(user)) {
            throw new IllegalArgumentException("User does not exists " + user);
        }

        List<Word> userCurrentHistory = wordsHistorySnapshots.get(user);
        long maxRequestSize = computeRequestMaxSize(requestedSize, userCurrentHistory.size());
        LOGGER.debug("history requestedSize {}, request requestedSize {}, compute max requestedSize {}", userCurrentHistory.size(), requestedSize, maxRequestSize);
        LOGGER.debug("User current history {}", userCurrentHistory);

        List<Word> sublist = computeHistorySubList(maxRequestSize, userCurrentHistory);
        LOGGER.debug("History retrieved {}", sublist);
        LOGGER.debug("History remaining {}", userCurrentHistory);

        return sublist;
    }

    @Override
    public Flux<Word> register(User user) {
        if (user == null) {
            throw new IllegalArgumentException("null user not allowed");
        }

        return Flux.<Word>create(wordFluxSink -> {
            LOGGER.debug("sink creation user {}", user);
            processor.register(word -> {
                LOGGER.info("new word from listener {}", word);
                wordFluxSink.next(word);
            });

            wordFluxSink.onRequest(nb -> {
                LOGGER.debug("onRequest {}", nb);
                List<Word> words = getHistoryForUser(user, nb);
                LOGGER.debug("Words list {}", words);

                for (Word w : words) {
                    wordFluxSink.next(w);
                }
                LOGGER.debug("onRequest ended");
            });
        })
                .doOnCancel(() -> {
                    LOGGER.info("Cancel Stream for user {}", user);
                    deleteHistorySnapshotForUSer(user);
                })
                .doOnSubscribe(subscription -> {
                    LOGGER.info("Subscribe to service for {}", user);
                    makeHistorySnapshotForUSer(user);
                })
                .doOnError(throwable -> {
                    LOGGER.error("Error with stream", throwable);
                });

    }


    @Override
    public void publish(Word word) {
        if (word == null) {
            throw new IllegalArgumentException("Can't add null word");
        }
        LOGGER.info("publish word {}", word);
        wordRepo.addWord(word);
        processor.publishAll(word);
    }
}
