package com.wan.hollout.utils;

import android.util.SparseBooleanArray;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * @author Wan Clem
 */

public class AppConstants {

    public static final String APP_USER_DISPLAY_NAME = "app_user_display_name";
    public static final String APP_USER_PROFILE_PHOTO_URL = "app_user_profile_photo_url";
    public static final String APP_USER_ONLINE_STATUS = "app_user_online_status";
    public static final String ONLINE = "online";
    public static final String APP_USER_LAST_SEEN = "app_user_last_seen";
    public static final String OFFLINE = "offline";
    public static final String APP_USER_GEO_POINT = "app_user_geo_point";
    public static final String APP_USER_COUNTRY = "app_user_country";
    public static final String APP_USER_STREET = "app_user_street";
    public static final String APP_USER_LOCALITY = "app_user_locality";
    public static final String APP_USER_ADMIN_AREA = "app_user_admin_area";
    public static final String HOLLOUT_PREFERENCES = "hollout_preferences";
    public static final String CAN_ACCESS_LOCATION = "can_access_location";
    public static final String USERS = "users";

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
    public static final String USER_WELCOMED = "user_here";
    public static final String USE_ID = "user_id";
    public static final String USER_EMAIL = "use_email";
    public static final String JUST_AUTHENTICATED = "just_authenticated";

    public static final String FEED_LIKES = "feed_likes";
    public static final String REACTIONS = "reactions";
    public static final String REACTORS = "reactors";
    public static final String FEED_VIEWS = "feed_views";
    public static final String AUTHENTICATED = "not_authenticated_before";
    public static final String ABOUT_USER = "about_user";

    public static final String APP_USER_NAME = "app_user_name";
    public static final String APP_USER_PASSWORD = "app_user_password";
    public static final String INTERESTS = "interests";
    public static final String NAME = "name";
    public static final String SELECTED = "selected";
    public static final String PEOPLE_TO_MEET_HOST_TYPE_SELECTED = "selected_people_to_meet";
    public static final String PEOPLE_TO_MEET_HOST_TYPE_POTENTIAL = "selected_people_to_meet";

    public static final int ENTITY_TYPE_CLOSEBY = 0;

    public static final String LOCATION_VISIBILITY_PREF = "who_sees_my_location";
    public static final String LAST_SEEN_VISIBILITY_PREF = "who_sees_my_last_seen";
    public static final String STATUS_VISIBILITY_PREF = "who_sees_my_status";
    public static final String AGE_VISIBILITY_PREF = "who_sees_my_age";

    public static final String MESSAGES_TEXT_SIZE = "messages_text_size_preference";
    public static final String SAVE_TO_GALLERY = "save_to_gallery";
    public static final String SETTINGS_FRAGMENT_NAME = "settings_fragment_name";
    public static final String NOTIFICATION_SETTINGS_FRAGMENT = "settings_fragment_type_notification";
    public static final String CHATS_SETTINGS_FRAGMENT = "settings_fragment_type_chat";
    public static final String SUPPORT_SETTINGS_FRAGMENT = "settings_fragment_type_support";
    public static final String PRIVACY_AND_SECURITY_FRAGMENT = "settings_fragment_type_privacy_and_security";
    public static final String WAKE_PHONE_ON_NOTIFICATION = "wake_phone_on_notification";
    public static final String SHOW_MESSAGE_TICKER = "show_message_ticker";
    public static final String VIBRATE_ON_NEW_NOTIFICATION = "vibrate_on_notification";
    public static final String PLAY_SOUND_ON_NEW_MESAGE_NOTIF = "play_sound_on_new_message_notifications";
    public static final String APP_USER_GENDER = "app_user_gender";
    public static final String UNKNOWN = "unknown";
    public static final String APP_USER_AGE = "app_user_age";
    public static final String USER_PROFILE_PHOTO_UPLOAD_TIME = "app_user_profile_photo_upload_time";
    public static final String APP_USER_STATUS = "app_user_status";
    public static final String APP_USER_CHAT_STATES = "app_user_chat_states";
    public static final String APP_USER_ADDITIONAL_USER_PHOTOS = "app_user_additional_photos";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String USER_ID = "user_id";
    public static final String EXTRA_PICTURES = "pictures";
    public static final String USER_PROPERTIES = "user_properties";

    public static boolean ARE_REACTIONS_OPEN = false;
    public static final String CLOSE_REACTIONS = "close_reactions";
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
    public static SparseBooleanArray reactionsOpenPositions = new SparseBooleanArray();
    public static SparseBooleanArray commentPositions = new SparseBooleanArray();
    public static SparseBooleanArray likesPositions = new SparseBooleanArray();

    public static String SAMPLE_HTML = "<div class=\"separator\" style=\"clear: both; text-align: center;\">\n" +
            "    <a href=\"http://alexis.lindaikejisblog.com/photos/shares/5987896c11298.png\" imageanchor=\"1\" style=\"margin-left: 1em; margin-right: 1em;\"><img border=\"0\" data-original-height=\"756\" data-original-width=\"800\" height=\"301\" src=\"https://alexis.lindaikejisblog.com/photos/shares/5987896c11298.png\" width=\"320\" />\n" +
            "    </a>\n" +
            "    <object class=\"BLOG_video_class\" contentid=\"bfbcbe5c2a9d002e\" height=\"266\" id=\"BLOG_video-bfbcbe5c2a9d002e\" width=\"320\"></object>\n" +
            "    <iframe width=\"320\" height=\"266\" class=\"YOUTUBE-iframe-video\" data-thumbnail-src=\"https://i.ytimg.com/vi/tXfPKVFz4HQ/0.jpg\" src=\"https://www.youtube.com/embed/tXfPKVFz4HQ?feature=player_embedded\" frameborder=\"0\" allowfullscreen></iframe>\n" +
            "</div>\n" +
            "There are people who think she had some work done on her face ...What do you think? Lolz.";

    public static String SAMPLE_URL_TO_QUERY_FOR_LABELS = "http://www.blogname.com/search/?q=label:Graphics|label:Identity|label:Brand]";
}

