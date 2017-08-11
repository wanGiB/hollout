package com.wan.hollout.eventbuses;

import com.parse.ParseObject;

/**
 * @author Wan Clem
 */

public class SelectedPerson {

    private ParseObject personObject;
    private boolean selected;

    public SelectedPerson(ParseObject personObject,boolean selected) {
        this.personObject = personObject;
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public ParseObject getPersonObject() {
        return personObject;
    }

}
