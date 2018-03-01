package com.wan.hollout.eventbuses;

/**
 * @author Wan Clem
 */

public class CallTerminationCause {

    private String terminationCause;

    public CallTerminationCause(String terminationCause) {
        this.terminationCause = terminationCause;
    }

    public String getTerminationCause() {
        return terminationCause;
    }

}
