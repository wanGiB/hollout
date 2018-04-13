package com.wan.hollout.utils;

/**
 * @author Wan Clem
 */
public class Range {
    int start;
    int end;

    public Range(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public boolean contain(int value) {
        return value >= start && value <= end;
    }

    @Override
    public String toString() {
        return "start: " + start + " end: " + end;
    }
}
