package com.wan.hollout.ui.utils;

/**
 * @author Wan Clem
 */

public class RichText {

    public static final char NEW_LINE = '\n';
    public static final char SPACE = ' ';
    public static final char BOLD_FLAG = '*';
    public static final char STRIKE_FLAG = '~';
    public static final char ITALIC_FLAG = '_';
    public static final int INVALID_INDEX = -1;

    /**
     * Flag marking a sequence of character to be a specific formatting type.
     */
    public static class Flag {

        public int start;
        public int end;
        public char flag;

        public Flag(int start, int end, char flag) {
            this.start = start;
            this.end = end;
            this.flag = flag;
        }
    }

}
