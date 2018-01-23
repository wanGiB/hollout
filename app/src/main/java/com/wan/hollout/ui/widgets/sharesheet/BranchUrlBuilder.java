package com.wan.hollout.ui.widgets.sharesheet;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Abstract for creating builder for getting a short url with Branch. This builder provide an easy and flexible way to configure and create
 * a short url Synchronously or asynchronously.
 * </p>
 */
@SuppressWarnings("rawtypes")
abstract class BranchUrlBuilder<T extends BranchUrlBuilder> {

    /* Deep linked params associated with the link that will be passed into a new app session when clicked */
    protected JSONObject params_;
    /* Name of the channel that the link belongs to. */
    protected String channel_;
    /* Name that identifies the feature that the link makes use of. */
    protected String feature_;
    /* Name that identify the stage in an application or user flow process. */
    protected String stage_;
    /* Name of the campaign that the link belongs to. */
    protected String campaign_;
    /* Link 'alias' can be used to label the endpoint on the link. */
    protected String alias_;
    /* Number of times the link should perform deep link */
    protected int type_ = 0;
    /* The amount of time that Branch allows a click to remain outstanding. */
    protected int duration_ = 0;
    /* An iterable collection of name associated with a deep link. */
    protected ArrayList<String> tags_;
    /* Branch Instance */
    /* Default to long url incase of link creation error*/
    private boolean defaultToLongUrl_;
    /* Application context. */
    private final Context context_;


    /**
     * <p>
     * Creates an instance of {@link BranchUrlBuilder} to create short links synchronously.
     * {@see getShortUrl() }
     * </p>
     *
     * @param context A {@link Context} from which this call was made.
     */
    protected BranchUrlBuilder(Context context) {
        context_ = context.getApplicationContext();
        defaultToLongUrl_ = true; /* By default fallback to long url */
    }

    /**
     * <p>Adds a tag to the iterable collection of name associated with a deep link.</p>
     *
     * @param tag {@link String} tags associated with a deep link.
     * @return This Builder object to allow for chaining of calls to set methods.
     */
    @SuppressWarnings("unchecked")
    public T addTag(String tag) {
        if (this.tags_ == null) {
            tags_ = new ArrayList<>();
        }
        this.tags_.add(tag);
        return (T) this;
    }

    /**
     * <p>Adds a tag to the iterable collection of name associated with a deep link.</p>
     *
     * @param tags {@link List} with collection of tags associated with a deep link.
     * @return This Builder object to allow for chaining of calls to set methods.
     */
    @SuppressWarnings("unchecked")
    public T addTags(List<String> tags) {
        if (this.tags_ == null) {
            tags_ = new ArrayList<>();
        }
        this.tags_.addAll(tags);
        return (T) this;
    }

    /**
     * <p>Adds the the given key value pair to the parameters associated with this link.</p>
     *
     * @param key   A {@link String} with value of key for the parameter
     * @param value A {@link String} with value of value for the parameter
     * @return This Builder object to allow for chaining of calls to set methods.
     */
    @SuppressWarnings("unchecked")
    public T addParameters(String key, String value) {
        try {
            if (this.params_ == null) {
                this.params_ = new JSONObject();
            }
            this.params_.put(key, value);
        } catch (JSONException ignore) {

        }
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T addParameters(String key, JSONArray value) {
        try {
            if (this.params_ == null) {
                this.params_ = new JSONObject();
            }
            this.params_.put(key, value);
        } catch (JSONException ignore) {

        }
        return (T) this;
    }

    public T setDefaultToLongUrl(boolean defaultToLongUrl) {
        defaultToLongUrl_ = defaultToLongUrl;
        return (T) this;
    }

    ///------------------------- Link Build methods---------------------------///

}
