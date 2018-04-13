package com.wan.hollout.models;

import android.support.annotation.NonNull;

import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AuthUtil;

import org.apache.commons.lang3.builder.CompareToBuilder;

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
    public int compareTo(@NonNull NearbyPerson o) {
        ParseObject otherPerson = o.getPerson();
        ParseObject signedInUser = AuthUtil.getCurrentUser();
        if (otherPerson != null && signedInUser != null) {
            ParseGeoPoint otherPersonGeoPoint = otherPerson.getParseGeoPoint(AppConstants.APP_USER_GEO_POINT);
            ParseGeoPoint signedInUserParseGeoPoint = signedInUser.getParseGeoPoint(AppConstants.APP_USER_GEO_POINT);
            if (otherPersonGeoPoint != null && signedInUserParseGeoPoint != null) {
                return CompareToBuilder.reflectionCompare(signedInUserParseGeoPoint, otherPersonGeoPoint);
            }
        }
        return 0;
    }

}
