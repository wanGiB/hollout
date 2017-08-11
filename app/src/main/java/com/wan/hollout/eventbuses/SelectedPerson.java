package com.wan.hollout.eventbuses;

import com.parse.ParseObject;

/**
 * @author Wan Clem
 */

public class SelectedPerson {

    private ParseObject personObject;

    public SelectedPerson(ParseObject personObject) {
        this.personObject = personObject;
    }

    public ParseObject getPersonObject() {
        return personObject;
    }

}
