package com.wan.hollout.eventbuses;

/**
 * @author Wan Clem
 */

public class SearchPeopleEvent {
    private String queryString;

    public SearchPeopleEvent(String queryString) {
        this.queryString = queryString;
    }

    public String getQueryString() {
        return queryString;
    }

}
