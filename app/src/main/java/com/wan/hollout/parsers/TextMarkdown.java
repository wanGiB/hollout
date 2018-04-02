package com.wan.hollout.parsers;

import android.support.annotation.NonNull;

import java.util.List;

/**
 * @author Wan Clem
 */

public class TextMarkdown {

    @NonNull
    private final List<Element> elements;

    public TextMarkdown(@NonNull final List<Element> elements) {
        this.elements = elements;
    }

    @NonNull
    public List<Element> getElements() {
        return elements;
    }

}
