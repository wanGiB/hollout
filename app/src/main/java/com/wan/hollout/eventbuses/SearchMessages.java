package com.wan.hollout.eventbuses;

/**
 * @author Wan Clem
 */

public class SearchMessages {

    private String searchString;

    public SearchMessages(String searchString) {
        this.searchString = searchString;
    }

    public String getSearchString() {
        return searchString;
    }

}
