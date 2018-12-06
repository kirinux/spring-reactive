package org.craftedsw.katas.reactive.infrastructure;

import org.craftedsw.katas.reactive.domain.Word;
import org.craftedsw.katas.reactive.domain.WordRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 *
 */
@Repository
public class WordRepositoryImpl implements WordRepository {

    private final List<Word> words = new ArrayList<>();

    @Override
    public List<Word> getHistory() {
        return Collections.unmodifiableList(new ArrayList<>(words));
    }

    @Override
    public void addWord(Word word) {
        words.add(word);
    }


}
