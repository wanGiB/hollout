package com.wan.hollout.models;

import com.parse.ParseObject;
import com.wan.hollout.utils.AppConstants;

/**
 *@author Wan Clem
 */

public class NearbyPerson {

    private ParseObject person;

    public NearbyPerson(ParseObject person){
        this.person = person;
    }

    public ParseObject getPerson() {
        return person;
    }

    private String getPersonId() {
        return person.getString(AppConstants.REAL_OBJECT_ID);
    }

    @Override
    public int hashCode() {
        int result;
        result = getPersonId().hashCode();
        final String name = getClass().getName();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        NearbyPerson another = (NearbyPerson) obj;
        return getPersonId().equals(another.getPersonId());
    }

}
