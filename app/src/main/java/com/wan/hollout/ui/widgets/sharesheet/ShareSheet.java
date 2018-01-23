package com.wan.hollout.ui.widgets.sharesheet;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * @author Wan Clem
 */
public class ShareSheet {

    /* Canonical identifier for the content referred. */
    private String canonicalIdentifier_;
    /* Canonical url for the content referred. This would be the corresponding website URL */
    private String canonicalUrl_;
    private String title_;
    /* Description for the content referred */
    private String description_;
    /* An image url associated with the content referred */
    /* Meta data provided for the content. This meta data is used as the link parameters for links created from this object */
    private final HashMap<String, String> metadata_;
    /* Mime type for the content referred */
    private String type_;
    /* Content index mode */
    /* Any keyword associated with the content. Used for indexing */
    private final ArrayList<String> keywords_;
    /* Expiry date for the content and any associated links. Represented as epoch milli second */
    /* Price associated with the content of this BUO */
    private Double price_;
    /* Type of the currency associated with the price */

    private final ArrayList<SharingHelper.SHARE_WITH> preferredOptions_;
    private String imageUrl;

    /**
     * <p>
     * </p>
     */
    public ShareSheet() {
        metadata_ = new HashMap<>();
        keywords_ = new ArrayList<>();
        canonicalIdentifier_ = "";
        canonicalUrl_ = "";
        title_ = "";
        description_ = "";
        type_ = "";
        preferredOptions_ = new ArrayList<>();
    }

    //------------------ Share sheet -------------------------------------//

    public void showShareSheet(@NonNull Activity activity, @NonNull LinkProperties linkProperties,
                               @NonNull ShareSheetStyle style,
                               @Nullable BranchLinkShareListener callback) {

        ShareLinkBuilder shareLinkBuilder = new ShareLinkBuilder(activity, getLinkBuilder(activity, linkProperties))
                .setCallback(new LinkShareListenerWrapper(callback))
                .setSubject(style.getMessageTitle())
                .setMessage(style.getMessageBody());

        if (style.getCopyUrlIcon() != null) {
            shareLinkBuilder.setCopyUrlStyle(style.getCopyUrlIcon(), style.getCopyURlText(), style.getUrlCopiedMessage());
        }
        if (style.getMoreOptionIcon() != null) {
            shareLinkBuilder.setMoreOptionStyle(style.getMoreOptionIcon(), style.getMoreOptionText());
        }
        if (style.getDefaultURL() != null) {
            shareLinkBuilder.setDefaultURL(style.getDefaultURL());
        }
        if (style.getPreferredOptions().size() > 0) {
            shareLinkBuilder.addPreferredSharingOptions(getPreferredOptions());
        }
        if (style.getStyleResourceID() > 0) {
            shareLinkBuilder.setStyleResourceID(style.getStyleResourceID());
        }
        shareLinkBuilder.setDividerHeight(style.getDividerHeight());
        shareLinkBuilder.setAsFullWidthStyle(style.getIsFullWidthStyle());
        shareLinkBuilder.setSharingTitle(style.getSharingTitle());
        shareLinkBuilder.setSharingTitle(style.getSharingTitleView());

        if (style.getIncludedInShareSheet() != null && style.getIncludedInShareSheet().size() > 0) {
            shareLinkBuilder.includeInShareSheet(style.getIncludedInShareSheet());
        }
        if (style.getExcludedFromShareSheet() != null && style.getExcludedFromShareSheet().size() > 0) {
            shareLinkBuilder.excludeFromShareSheet(style.getExcludedFromShareSheet());
        }

        shareLinkBuilder.shareLink();
    }

    private ArrayList<SharingHelper.SHARE_WITH> getPreferredOptions() {
        return preferredOptions_;
    }

    private BranchShortLinkBuilder getLinkBuilder(@NonNull Context context, @NonNull LinkProperties linkProperties) {
        BranchShortLinkBuilder shortLinkBuilder = new BranchShortLinkBuilder(context);
        if (linkProperties.getTags() != null) {
            shortLinkBuilder.addTags(linkProperties.getTags());
        }
        if (linkProperties.getFeature() != null) {
            shortLinkBuilder.setFeature(linkProperties.getFeature());
        }
        if (linkProperties.getAlias() != null) {
            shortLinkBuilder.setAlias(linkProperties.getAlias());
        }
        if (linkProperties.getChannel() != null) {
            shortLinkBuilder.setChannel(linkProperties.getChannel());
        }
        if (linkProperties.getStage() != null) {
            shortLinkBuilder.setStage(linkProperties.getStage());
        }
        if (linkProperties.getCampaign() != null) {
            shortLinkBuilder.setCampaign(linkProperties.getCampaign());
        }
        if (linkProperties.getMatchDuration() > 0) {
            shortLinkBuilder.setDuration(linkProperties.getMatchDuration());
        }
        if (!TextUtils.isEmpty(title_)) {
            shortLinkBuilder.addParameters(Defines.Jsonkey.ContentTitle.getKey(), title_);
        }
        if (!TextUtils.isEmpty(canonicalIdentifier_)) {
            shortLinkBuilder.addParameters(Defines.Jsonkey.CanonicalIdentifier.getKey(), canonicalIdentifier_);
        }
        if (!TextUtils.isEmpty(canonicalUrl_)) {
            shortLinkBuilder.addParameters(Defines.Jsonkey.CanonicalUrl.getKey(), canonicalUrl_);
        }
        JSONArray keywords = getKeywordsJsonArray();
        if (keywords.length() > 0) {
            shortLinkBuilder.addParameters(Defines.Jsonkey.ContentKeyWords.getKey(), keywords);
        }
        if (!TextUtils.isEmpty(description_)) {
            shortLinkBuilder.addParameters(Defines.Jsonkey.ContentDesc.getKey(), description_);
        }
        if (!TextUtils.isEmpty(imageUrl)) {
            shortLinkBuilder.addParameters(Defines.Jsonkey.ContentImgUrl.getKey(), imageUrl);
        }
        if (!TextUtils.isEmpty(type_)) {
            shortLinkBuilder.addParameters(Defines.Jsonkey.ContentType.getKey(), type_);
        }
        for (String key : metadata_.keySet()) {
            shortLinkBuilder.addParameters(key, metadata_.get(key));
        }
        HashMap<String, String> controlParam = linkProperties.getControlParams();
        for (String key : controlParam.keySet()) {
            shortLinkBuilder.addParameters(key, controlParam.get(key));
        }
        return shortLinkBuilder;
    }

    /**
     *
     */
    public JSONArray getKeywordsJsonArray() {
        JSONArray keywordArray = new JSONArray();
        for (String keyword : keywords_) {
            keywordArray.put(keyword);
        }
        return keywordArray;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setCanonicalUrl_(String canonicalUrl_) {
        this.canonicalUrl_ = canonicalUrl_;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * <p> Class for building a share link dialog.This creates a chooser for selecting application for
     * sharing a link created with given parameters. </p>
     */
    public static class ShareLinkBuilder {

        private final Activity activity_;

        private String shareMsg_;
        private String shareSub_;
        private BranchLinkShareListener callback_ = null;

        private ArrayList<SharingHelper.SHARE_WITH> preferredOptions_;
        private String defaultURL_;

        //Customise more and copy url option
        private Drawable moreOptionIcon_;
        private String moreOptionText_;
        private Drawable copyUrlIcon_;
        private String copyURlText_;
        private String urlCopiedMessage_;
        private int styleResourceID_;
        private boolean setFullWidthStyle_;
        private int dividerHeight = -1;
        private String sharingTitle = null;
        private View sharingTitleView = null;

        BranchShortLinkBuilder shortLinkBuilder_;
        private List<String> includeInShareSheet = new ArrayList<>();
        private List<String> excludeFromShareSheet = new ArrayList<>();

        /**
         * <p>Creates options for sharing a link with other Applications. Creates a builder for sharing the link with
         * user selected clients</p>
         *
         * @param activity   The {@link Activity} to show the dialog for choosing sharing application.
         * @param parameters A {@link JSONObject} value containing the deep link params.
         */
        public ShareLinkBuilder(Activity activity, JSONObject parameters) {
            this.activity_ = activity;
            shortLinkBuilder_ = new BranchShortLinkBuilder(activity);
            try {
                Iterator<String> keys = parameters.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    shortLinkBuilder_.addParameters(key, (String) parameters.get(key));
                }
            } catch (Exception ignore) {
            }
            shareMsg_ = "";
            callback_ = null;
            preferredOptions_ = new ArrayList<>();
            defaultURL_ = null;

            moreOptionIcon_ = ContextCompat.getDrawable(activity.getApplicationContext(), android.R.drawable.ic_menu_more);
            moreOptionText_ = "More...";

            copyUrlIcon_ = ContextCompat.getDrawable(activity.getApplicationContext(), android.R.drawable.ic_menu_save);
            copyURlText_ = "Copy link";
            urlCopiedMessage_ = "Copied link to clipboard!";
        }

        /**
         * *<p>Creates options for sharing a link with other Applications. Creates a builder for sharing the link with
         * user selected clients</p>
         *
         * @param activity         The {@link Activity} to show the dialog for choosing sharing application.
         * @param shortLinkBuilder An instance of {@link BranchShortLinkBuilder} to create link to be shared
         */
        public ShareLinkBuilder(Activity activity, BranchShortLinkBuilder shortLinkBuilder) {
            this(activity, new JSONObject());
            shortLinkBuilder_ = shortLinkBuilder;
        }

        /**
         * <p>Sets the message to be shared with the link.</p>
         *
         * @param message A {@link String} to be shared with the link
         */
        public ShareLinkBuilder setMessage(String message) {
            this.shareMsg_ = message;
            return this;
        }

        /**
         * <p>Sets the subject of this message. This will be added to Email and SMS Application capable of handling subject in the message.</p>
         *
         * @param subject A {@link String} subject of this message.
         */
        public ShareLinkBuilder setSubject(String subject) {
            this.shareSub_ = subject;
            return this;
        }

        /**
         * <p>Adds the given tag an iterable {@link Collection} of {@link String} tags associated with a deep
         * link.</p>
         *
         * @param tag A {@link String} to be added to the iterable {@link Collection} of {@link String} tags associated with a deep
         *            link.
         */
        public ShareLinkBuilder addTag(String tag) {
            this.shortLinkBuilder_.addTag(tag);
            return this;
        }

        /**
         * <p>Adds the given tag an iterable {@link Collection} of {@link String} tags associated with a deep
         * link.</p>
         *
         * @param tags A {@link List} of tags to be added to the iterable {@link Collection} of {@link String} tags associated with a deep
         *             link.
         */
        public ShareLinkBuilder addTags(ArrayList<String> tags) {
            this.shortLinkBuilder_.addTags(tags);
            return this;
        }

        /**
         * <p>Adds a feature that make use of the link.</p>
         *
         * @param feature A {@link String} value identifying the feature that the link makes use of.
         *                Should not exceed 128 characters.
         */
        public ShareLinkBuilder setFeature(String feature) {
            this.shortLinkBuilder_.setFeature(feature);
            return this;
        }

        /**
         * <p>Adds a stage application or user flow associated with this link.</p>
         *
         * @param stage A {@link String} value identifying the stage in an application or user flow
         *              process. Should not exceed 128 characters.
         */
        public ShareLinkBuilder setStage(String stage) {
            this.shortLinkBuilder_.setStage(stage);
            return this;
        }

        /**
         * <p>Adds a callback to get the sharing status.</p>
         *
         * @param callback A {@link BranchLinkShareListener} instance for getting sharing status.
         */
        public ShareLinkBuilder setCallback(BranchLinkShareListener callback) {
            this.callback_ = callback;
            return this;
        }

        /**
         * <p>Adds application to the preferred list of applications which are shown on share dialog.
         * Only these options will be visible when the application selector dialog launches. Other options can be
         * accessed by clicking "More"</p>
         *
         * @param preferredOption A list of applications to be added as preferred options on the app chooser.
         */
        public ShareLinkBuilder addPreferredSharingOption(SharingHelper.SHARE_WITH preferredOption) {
            this.preferredOptions_.add(preferredOption);
            return this;
        }

        /**
         * <p>Adds application to the preferred list of applications which are shown on share dialog.
         * Only these options will be visible when the application selector dialog launches. Other options can be
         * accessed by clicking "More"</p>
         *
         * @param preferredOptions A list of applications to be added as preferred options on the app chooser.
         */
        public ShareLinkBuilder addPreferredSharingOptions(ArrayList<SharingHelper.SHARE_WITH> preferredOptions) {
            this.preferredOptions_.addAll(preferredOptions);
            return this;
        }

        /**
         * Add the given key value to the deep link parameters
         *
         * @param key   A {@link String} with value for the key for the deep link params
         * @param value A {@link String} with deep link parameters value
         */
        public ShareLinkBuilder addParam(String key, String value) {
            try {
                this.shortLinkBuilder_.addParameters(key, value);
            } catch (Exception ignore) {

            }
            return this;
        }

        /**
         * <p> Set a default url to share in case there is any error creating the deep link </p>
         *
         * @param url A {@link String} with value of default url to be shared with the selected application in case deep link creation fails.
         */
        public ShareLinkBuilder setDefaultURL(String url) {
            defaultURL_ = url;
            return this;
        }

        /**
         * <p> Set the icon and label for the option to expand the application list to see more options.
         * Default label is set to "More" </p>
         *
         * @param icon  Drawable to set as the icon for more option. Default icon is system menu_more icon.
         * @param label A {@link String} with value for the more option label. Default label is "More"
         */
        public ShareLinkBuilder setMoreOptionStyle(Drawable icon, String label) {
            moreOptionIcon_ = icon;
            moreOptionText_ = label;
            return this;
        }

        /**
         * <p> Set the icon and label for the option to expand the application list to see more options.
         * Default label is set to "More" </p>
         *
         * @param drawableIconID Resource ID for the drawable to set as the icon for more option. Default icon is system menu_more icon.
         * @param stringLabelID  Resource ID for String label for the more option. Default label is "More"
         */
        public ShareLinkBuilder setMoreOptionStyle(int drawableIconID, int stringLabelID) {
            moreOptionIcon_ = ContextCompat.getDrawable(activity_.getApplicationContext(), drawableIconID);
            moreOptionText_ = activity_.getResources().getString(stringLabelID);
            return this;
        }

        /**
         * <p> Set the icon, label and success message for copy url option. Default label is "Copy link".</p>
         *
         * @param icon    Drawable to set as the icon for copy url  option. Default icon is system menu_save icon
         * @param label   A {@link String} with value for the copy url option label. Default label is "Copy link"
         * @param message A {@link String} with value for a toast message displayed on copying a url.
         *                Default message is "Copied link to clipboard!"
         */
        public ShareLinkBuilder setCopyUrlStyle(Drawable icon, String label, String message) {
            copyUrlIcon_ = icon;
            copyURlText_ = label;
            urlCopiedMessage_ = message;
            return this;
        }

        /**
         * <p> Set the icon, label and success message for copy url option. Default label is "Copy link".</p>
         *
         * @param drawableIconID  Resource ID for the drawable to set as the icon for copy url  option. Default icon is system menu_save icon
         * @param stringLabelID   Resource ID for the string label the copy url option. Default label is "Copy link"
         * @param stringMessageID Resource ID for the string message to show toast message displayed on copying a url
         */
        public ShareLinkBuilder setCopyUrlStyle(int drawableIconID, int stringLabelID, int stringMessageID) {
            copyUrlIcon_ = ContextCompat.getDrawable(activity_.getApplicationContext(), drawableIconID);
            copyURlText_ = activity_.getResources().getString(stringLabelID);
            urlCopiedMessage_ = activity_.getResources().getString(stringMessageID);
            return this;

        }

        /**
         * <p> Sets the alias for this link. </p>
         *
         * @param alias Link 'alias' can be used to label the endpoint on the link.
         *              <p>
         *              For example:
         *              http://bnc.lt/AUSTIN28.
         *              Should not exceed 128 characters
         *              </p>
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public ShareLinkBuilder setAlias(String alias) {
            this.shortLinkBuilder_.setAlias(alias);
            return this;
        }

        /**
         * <p> Sets the amount of time that Branch allows a click to remain outstanding.</p>
         *
         * @param matchDuration A {@link Integer} value specifying the time that Branch allows a click to
         *                      remain outstanding and be eligible to be matched with a new app session.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public ShareLinkBuilder setMatchDuration(int matchDuration) {
            this.shortLinkBuilder_.setDuration(matchDuration);
            return this;
        }

        /**
         * <p>
         * Sets the share dialog to full width mode. Full width mode will show a non modal sheet with entire screen width.
         * </p>
         *
         * @param setFullWidthStyle {@link Boolean} With value true if a full width style share sheet is desired.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public ShareLinkBuilder setAsFullWidthStyle(boolean setFullWidthStyle) {
            this.setFullWidthStyle_ = setFullWidthStyle;
            return this;
        }

        /**
         * Set the height for the divider for the sharing channels in the list. Set this to zero to remove the dividers
         *
         * @param height The new height of the divider in pixels.
         * @return this Builder object to allow for chaining of calls to set methods.
         */
        public ShareLinkBuilder setDividerHeight(int height) {
            this.dividerHeight = height;
            return this;
        }

        /**
         * Set the title for the sharing dialog
         *
         * @param title {@link String} containing the value for the title text.
         * @return this Builder object to allow for chaining of calls to set methods.
         */
        public ShareLinkBuilder setSharingTitle(String title) {
            this.sharingTitle = title;
            return this;
        }

        /**
         * Set the title for the sharing dialog
         *
         * @param titleView {@link View} for setting the title.
         * @return this Builder object to allow for chaining of calls to set methods.
         */
        public ShareLinkBuilder setSharingTitle(View titleView) {
            this.sharingTitleView = titleView;
            return this;
        }

        /**
         * Exclude items from the ShareSheet by package name String.
         *
         * @param packageName {@link String} package name to be excluded.
         * @return this Builder object to allow for chaining of calls to set methods.
         */
        public ShareLinkBuilder excludeFromShareSheet(@NonNull String packageName) {
            this.excludeFromShareSheet.add(packageName);
            return this;
        }

        /**
         * Exclude items from the ShareSheet by package name array.
         *
         * @param packageName {@link String[]} package name to be excluded.
         * @return this Builder object to allow for chaining of calls to set methods.
         */
        public ShareLinkBuilder excludeFromShareSheet(@NonNull String[] packageName) {
            this.excludeFromShareSheet.addAll(Arrays.asList(packageName));
            return this;
        }

        /**
         * Exclude items from the ShareSheet by package name List.
         *
         * @param packageNames {@link List} package name to be excluded.
         * @return this Builder object to allow for chaining of calls to set methods.
         */
        public ShareLinkBuilder excludeFromShareSheet(@NonNull List<String> packageNames) {
            this.excludeFromShareSheet.addAll(packageNames);
            return this;
        }

        /**
         * Include items from the ShareSheet by package name String. If only "com.Slack"
         * is included, then only preferred sharing options + Slack
         * will be displayed, for example.
         *
         * @param packageName {@link String} package name to be included.
         * @return this Builder object to allow for chaining of calls to set methods.
         */
        public ShareLinkBuilder includeInShareSheet(@NonNull String packageName) {
            this.includeInShareSheet.add(packageName);
            return this;
        }

        /**
         * Include items from the ShareSheet by package name Array. If only "com.Slack"
         * is included, then only preferred sharing options + Slack
         * will be displayed, for example.
         *
         * @param packageName {@link String[]} package name to be included.
         * @return this Builder object to allow for chaining of calls to set methods.
         */
        public ShareLinkBuilder includeInShareSheet(@NonNull String[] packageName) {
            this.includeInShareSheet.addAll(Arrays.asList(packageName));
            return this;
        }

        /**
         * Include items from the ShareSheet by package name List. If only "com.Slack"
         * is included, then only preferred sharing options + Slack
         * will be displayed, for example.
         *
         * @param packageNames {@link List} package name to be included.
         * @return this Builder object to allow for chaining of calls to set methods.
         */
        public ShareLinkBuilder includeInShareSheet(@NonNull List<String> packageNames) {
            this.includeInShareSheet.addAll(packageNames);
            return this;
        }

        /**
         * <p> Set the given style to the List View showing the share sheet</p>
         *
         * @param resourceID A Styleable resource to be applied to the share sheet list view
         */
        public void setStyleResourceID(@StyleRes int resourceID) {
            styleResourceID_ = resourceID;
        }

        public void setShortLinkBuilderInternal(BranchShortLinkBuilder shortLinkBuilder) {
            this.shortLinkBuilder_ = shortLinkBuilder;
        }

        /**
         * <p>Creates an application selector dialog and share a link with user selected sharing option.
         * The link is created with the parameters provided to the builder. </p>
         */
        public void shareLink() {
            ShareSheetManager.shareLink(this);
        }

        public Activity getActivity() {
            return activity_;
        }

        public ArrayList<SharingHelper.SHARE_WITH> getPreferredOptions() {
            return preferredOptions_;
        }

        List<String> getExcludedFromShareSheet() {
            return excludeFromShareSheet;
        }

        List<String> getIncludedInShareSheet() {
            return includeInShareSheet;
        }

        public String getShareMsg() {
            return shareMsg_;
        }

        public String getShareSub() {
            return shareSub_;
        }

        public BranchLinkShareListener getCallback() {
            return callback_;
        }

        public String getDefaultURL() {
            return defaultURL_;
        }

        public Drawable getMoreOptionIcon() {
            return moreOptionIcon_;
        }

        public String getMoreOptionText() {
            return moreOptionText_;
        }

        public Drawable getCopyUrlIcon() {
            return copyUrlIcon_;
        }

        public String getCopyURlText() {
            return copyURlText_;
        }

        public String getUrlCopiedMessage() {
            return urlCopiedMessage_;
        }

        public BranchShortLinkBuilder getShortLinkBuilder() {
            return shortLinkBuilder_;
        }

        public boolean getIsFullWidthStyle() {
            return setFullWidthStyle_;
        }

        public int getDividerHeight() {
            return dividerHeight;
        }

        public String getSharingTitle() {
            return sharingTitle;
        }

        public View getSharingTitleView() {
            return sharingTitleView;
        }

        public int getStyleResourceID() {
            return styleResourceID_;
        }
    }

    /**
     * Class for intercepting share sheet events to report auto events on BUO
     */
    private class LinkShareListenerWrapper implements BranchLinkShareListener {
        private final BranchLinkShareListener originalCallback_;

        public LinkShareListenerWrapper(BranchLinkShareListener originalCallback) {
            originalCallback_ = originalCallback;
        }

        @Override
        public void onShareLinkDialogLaunched() {
            if (originalCallback_ != null) {
                originalCallback_.onShareLinkDialogLaunched();
            }
        }

        @Override
        public void onShareLinkDialogDismissed() {
            if (originalCallback_ != null) {
                originalCallback_.onShareLinkDialogDismissed();
            }
        }

        @Override
        public void onLinkShareResponse(String sharedLink, String sharedChannel, Exception error) {
            HashMap<String, String> metaData = new HashMap<>();
            if (error == null) {
                metaData.put(Defines.Jsonkey.SharedLink.getKey(), sharedLink);
            } else {
                metaData.put(Defines.Jsonkey.ShareError.getKey(), error.getMessage());
            }
            if (originalCallback_ != null) {
                originalCallback_.onLinkShareResponse(sharedLink, sharedChannel, error);
            }
        }

        @Override
        public void onChannelSelected(String channelName) {
            if (originalCallback_ != null) {
                originalCallback_.onChannelSelected(channelName);
            }
        }
    }

    /**
     * <p>An Interface class that is implemented by all classes that make use of
     * {@link BranchLinkShareListener}, defining methods to listen for link sharing status.</p>
     */
    public interface BranchLinkShareListener {
        /**
         * <p> Callback method to update when share link dialog is launched.</p>
         */
        void onShareLinkDialogLaunched();

        /**
         * <p> Callback method to update when sharing dialog is dismissed.</p>
         */
        void onShareLinkDialogDismissed();

        /**
         * <p> Callback method to update the sharing status. Called on sharing completed or on error.</p>
         *
         * @param sharedLink    The link shared to the channel.
         * @param sharedChannel Channel selected for sharing.
         */
        void onLinkShareResponse(String sharedLink, String sharedChannel, Exception error);

        /**
         * <p>Called when user select a channel for sharing a deep link.
         * Branch will create a deep link for the selected channel and share with it after calling this
         * method. On sharing complete, status is updated by onLinkShareResponse() callback. Consider
         * having a sharing in progress UI if you wish to prevent user activity in the window between selecting a channel
         * and sharing complete.</p>
         *
         * @param channelName Name of the selected application to share the link. An empty string is returned if unable to resolve selected client name.
         */
        void onChannelSelected(String channelName);
    }
}
