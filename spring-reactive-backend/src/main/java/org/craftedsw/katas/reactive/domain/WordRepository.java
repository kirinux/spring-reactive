package org.craftedsw.katas.reactive.domain;

import java.util.List;

/**
 *
 *
 */

public interface WordRepository {
    List<Word> getHistory();
    void addWord(Word word);

}
