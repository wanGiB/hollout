package com.wan.hollout.models;

import android.support.annotation.NonNull;

import com.parse.ParseObject;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AuthUtil;

import java.util.List;

/**
 * @author Wan Clem
 */

public class NearbyPerson implements Comparable<NearbyPerson> {

    private ParseObject person;

    public NearbyPerson(ParseObject person) {
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

    @Override
    public int compareTo(@NonNull NearbyPerson refUser) {
        ParseObject signedInUserObject = AuthUtil.getCurrentUser();
        if (signedInUserObject != null) {
            List<String> aboutSignedInUser = signedInUserObject.getList(AppConstants.ABOUT_USER);
            List<String> aboutRefUser = refUser.getPerson().getList(AppConstants.ABOUT_USER);
            if (aboutSignedInUser != null && aboutRefUser != null) {
                String firstInfoAboutSignedInUser = aboutSignedInUser.get(0);
                String firstInfoAboutRefUser = aboutRefUser.get(0);
                return firstInfoAboutSignedInUser.compareTo(firstInfoAboutRefUser);
            }
        }
        return 0;
    }

}
