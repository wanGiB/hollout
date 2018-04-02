package com.wan.hollout.parsers;

import android.support.annotation.NonNull;

import java.util.List;

/**
 * @author Wan Clem
 */

public class Element {
    public enum Type {TEXT, QUOTE, BULLET_POINT, CODE_BLOCK}

    @NonNull
    private final Type type;

    @NonNull
    private final String text;

    @NonNull
    private final List<Element> elements;

    public Element(@NonNull final Type type, @NonNull final String text,
                   @NonNull final List<Element> elements) {
        this.type = type;
        this.text = text;
        this.elements = elements;
    }

    @NonNull
    public Type getType() {
        return type;
    }

    @NonNull
    public String getText() {
        return text;
    }

    @NonNull
    public List<Element> getElements() {
        return elements;
    }

    @Override
    public String toString() {
        return type + " " + text;
    }

}
