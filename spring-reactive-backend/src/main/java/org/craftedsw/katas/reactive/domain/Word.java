package org.craftedsw.katas.reactive.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 *
 */
public class Word {

    private final String content;

    @JsonCreator
    public Word(@JsonProperty("content") String word) {
        this.content = word;
    }

    public static List<Word> listOf(String content, int size) {
        ArrayList<Word> words = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            words.add(new Word(content + i));
        }
        return words;
    }

    public String getContent() {
        return content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Word word = (Word) o;
        return Objects.equals(content, word.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content);
    }

    @Override
    public String toString() {
        return "JWord{" +
                "content='" + content + '\'' +
                '}';
    }
}
