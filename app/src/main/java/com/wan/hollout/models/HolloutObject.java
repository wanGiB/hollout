package com.wan.hollout.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * @author Wan Clem
 */

public class HolloutObject implements Serializable, Parcelable {

    public JSONObject jsonObject;


    //Utility String field to help parcel and unparcel a jsonobject

    private String jsonObjectString;

    /**
     * A no argument constructor is needed for Kryo serialization
     **/
    public HolloutObject() {

    }

    public HolloutObject(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
        this.jsonObjectString = jsonObject.toString();
    }

    protected HolloutObject(Parcel in) {
        jsonObjectString = in.readString();
        if (StringUtils.isNotEmpty(jsonObjectString)) {
            try {
                jsonObject = new JSONObject(jsonObjectString);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static final Creator<HolloutObject> CREATOR = new Creator<HolloutObject>() {
        @Override
        public HolloutObject createFromParcel(Parcel in) {
            return new HolloutObject(in);
        }

        @Override
        public HolloutObject[] newArray(int size) {
            return new HolloutObject[size];
        }
    };

    public String getId() {
        return jsonObject.optString("id") + "hollout";
    }

    private long getCreatedOn() {
        return jsonObject != null ? jsonObject.optInt("created_on", 0) : 0;
    }


    public JSONObject getJsonObject() {
        return jsonObject;
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
        HolloutObject another = (HolloutObject) obj;
        String anotherObjectId = another.getId();
        return getId().equals(anotherObjectId);
    }

    @Override
    public int hashCode() {
        int result;
        result = getId().hashCode();
        final String name = getClass().getName();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(jsonObjectString);
    }

}
