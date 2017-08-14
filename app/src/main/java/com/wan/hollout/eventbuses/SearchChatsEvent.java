package com.wan.hollout.eventbuses;

/**
 * @author Wan Clem
 */

public class SearchChatsEvent {

    private String queryString;

    public SearchChatsEvent(String queryString) {
        this.queryString = queryString;
    }

    public String getQueryString() {
        return queryString;
    }

}
