package com.wan.hollout.utils;

import android.util.SparseBooleanArray;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * @author Wan Clem
 */

public class AppConstants {

    public static final String USER_DISPLAY_NAME = "user_display_name";
    public static final String USER_PHOTO_URL = "user_photo_url";
    public static final String ONLINE_STATUS = "online_status";
    public static final String ONLINE = "online";
    public static final String USER_LAST_SEEN = "user_last_seen";
    public static final String OFFLINE = "offline";
    public static final String APP_USER_GEO_POINT = "app_user_geo_point";
    public static final String APP_USER_COUNTRY = "app_user_country";
    public static final String APP_USER_STREET = "app_user_street";
    public static final String APP_USER_LOCALITY = "app_user_locality";
    public static final String APP_USER_ADMIN_AREA = "app_user_admin_area";
    public static final String HOLLOUT_PREFERENCES = "hollout_preferences";
    public static final String CAN_ACCESS_LOCATION = "can_access_location";
    public static final String USERS = "users";

    public static final String USER_LATITUDE = "user_latitude";
    public static final String USER_LONGITUDE = "user_longitude";

    public static final String START_PAGE_INDEX = "start_page_index";

    public static final String NEXT_PAGE_TOKEN = "nextPageToken";
    public static final String PREVIOUS_PAGE_TOKEN = "previousPageToken";
    public static final String POST_PUBLISHED_DATE = "published";
    public static final String POST_ID = "id";
    public static final String BLOG = "blog";
    public static final String BLOG_ID = "id";
    public static final String PUBLIC_POST_LINK = "url";
    public static final String POST_SELF_LINK = "selfLink";
    public static final String POST_TITLE = "title";
    public static final String POST_CONTENT = "content";

    public static final String AUTHOR = "author";
    public static final String POST_REPLIES = "replies";
    public static final String REPLIES_COUNT = "totalItems";
    public static final String LABELS = "labels";
    public static String AUTHOR_ID = "id";
    public static String AUTHOR_DISPLAY_NAME = "displayName";
    public static String AUTHOR_PUBLIC_URL = "url";
    public static String AUTHOR_IMAGE = "image";
    public static String AUTHOR_IMAGE_URL = "url";

    public static final SimpleDateFormat DATE_FORMATTER_IN_GEN_FORMAT = new SimpleDateFormat("d/MM/yyyy", Locale.getDefault());
    public static final SimpleDateFormat DATE_FORMATTER_IN_12HRS = new SimpleDateFormat("h:mm a", Locale.getDefault());
    public static final SimpleDateFormat DATE_FORMATTER_IN_BIRTHDAY_FORMAT = new SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault());
    public static final SimpleDateFormat DATE_FORMATTER_IN_YEARS = new SimpleDateFormat("yyyy", Locale.getDefault());

    public static SparseBooleanArray reactionsBackgroundPositions = new SparseBooleanArray();

    public static String SAMPLE_HTML = "<div class=\"separator\" style=\"clear: both; text-align: center;\">\n" +
            "    <a href=\"http://alexis.lindaikejisblog.com/photos/shares/5987896c11298.png\" imageanchor=\"1\" style=\"margin-left: 1em; margin-right: 1em;\"><img border=\"0\" data-original-height=\"756\" data-original-width=\"800\" height=\"301\" src=\"https://alexis.lindaikejisblog.com/photos/shares/5987896c11298.png\" width=\"320\" />\n" +
            "    </a>\n" +
            "    <object class=\"BLOG_video_class\" contentid=\"bfbcbe5c2a9d002e\" height=\"266\" id=\"BLOG_video-bfbcbe5c2a9d002e\" width=\"320\"></object>\n" +
            "    <iframe width=\"320\" height=\"266\" class=\"YOUTUBE-iframe-video\" data-thumbnail-src=\"https://i.ytimg.com/vi/tXfPKVFz4HQ/0.jpg\" src=\"https://www.youtube.com/embed/tXfPKVFz4HQ?feature=player_embedded\" frameborder=\"0\" allowfullscreen></iframe>\n" +
            "</div>\n" +
            "There are people who think she had some work done on her face ...What do you think? Lolz.";

}
