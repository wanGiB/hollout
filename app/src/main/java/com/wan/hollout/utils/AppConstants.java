package com.wan.hollout.utils;

import android.provider.MediaStore;
import android.util.SparseBooleanArray;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * @author Wan Clem
 */

public class AppConstants {

    public static final String TARGET_COUNTRY = "feed_target_country";
    public static final String INIT_TITLE = "init_title";
    public static final String PEOPLE_TO_SHARE_WITH_HAS_CHANGED = "people_to_share_with_has_changed";
    public static final String INIT_POSITION = "init_position";

    public static final String FILE_MIME_TYPE = "file_mime_type";
    public static final String REPLY_MESSAGE = "reply_message";
    public static final String HIDE_MESSAGE_REPLY_VIEW = "HIDE_MESSAGE_REPLY_VIEW";

    public static final String CONTACT_NAME = "contact_name";
    public static final String CONTACT_NUMBER = "contact_number";

    public static final String REQUEST_STORAGE_ACCESS_FOR_GALLERY = "RequestStoragePermissionsForGalleryAccess";
    public static final String REQUEST_STORAGE_ACCESS_FOR_DOCUMENTS = "RequestStoragePermissionsForDocumentAccess";
    public static final String REQUEST_AUDIO_ACCESS_FOR_RECORDING = "RequestStoragePermissionsForAudioAccess";
    public static final String FILE_TYPE_DOCUMENT = "FileTypeDocument";
    public static final String FILE_TYPE_CONTACT = "FileTypeContact";
    public static final String FILE_TYPE_LOCATION = "FileTypeLocation";

    public static final String HOLLOUT_FEED = "feed";
    public static final String FEED_CREATOR_ID = "feed_creator_id";
    public static final String FEED_TYPE = "feed_type";
    public static final String FEED_BODY = "feed_body";
    public static final String FEED_CREATOR = "feed_creator";
    public static final String FEED_TYPE_CHAT_REQUEST = "feed_type_chat_request";
    public static final String FEED_RECIPIENT_ID = "feed_recipient";
    public static final String RECIPIENT_NAME = "com.app.hollout.RECIPIENT_NAME";
    public static final String LAST_FILE_CAPTION = "last_file_caption";

    public static final String FILE_TYPE = "CustomFileType";
    public static final String AUDIO_DURATION = "audio_duration";

    public static final int CAPTURE_MEDIA_TYPE_IMAGE = 1;
    public static final int CAPTURE_MEDIA_TYPE_VIDEO = 2;
    public static final int CAPTURE_MEDIA_TYPE_AUDIO = 3;

    private static final String HOLLOUT_FOLDER_PATH = "Hollout";
    public static final String HOLLOUT_MEDIA_PATH = HOLLOUT_FOLDER_PATH + "/" + "Media";

    public static final int CHAT_TYPE_SINGLE = 0;
    public static final int CHAT_TYPE_GROUP = 1;
    public static final int CHAT_TYPE_ROOM = 2;

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
    public static final String PEOPLE_TO_MEET_HOST_TYPE_POTENTIAL = "people_to_meet_host_type_potential";

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
    public static final String APP_USER_FEATURED_PHOTOS = "app_user_featured_photos";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String REAL_OBJECT_ID = "real_object_id";
    public static final String EXTRA_PICTURES = "pictures";
    public static final String USER_PROPERTIES = "user_properties";
    public static final String APP_USER_CHATS = "app_user_chats";
    public static final String APP_USERS = "hollout_app_users";
    public static final String DISABLE_NESTED_SCROLLING = "disable_nested_scrolling";
    public static final String ENABLE_NESTED_SCROLLING = "enable_nested_scrolling";
    public static final String REFRESH_PEOPLE = "refresh_people";
    public static final String PLEASE_REQUEST_LOCATION_ACCESSS = "request_location_access";
    public static final String APP_USER_COVER_PHOTO = "app_user_cover_photo_url";
    public static final java.lang.String CAN_LAUNCH_MAIN = "can_launch_main";
    public static final String HOLLOUT_FILES_BUCKET = "gs://hollout-825b4.appspot.com";
    public static final String PHOTO_DIRECTORY = "Photos";
    public static final String USER_COVER_PHOTO_UPLOAD_TIME = "app_user_cover_photo_upload_time";

    public static final String SEARCH_VIEW_CLOSED = "search_view_closed_event";
    public static final String READ = "read";
    public static final String DELIVERED = "delivered";
    public static final String DELIVERY_STATUS = "delivery_status";
    public static final String REFRESH_MESSAGES_ADAPTER = "refresh_messages_adapter";
    public static final String MESSAGES = "messages";
    public static final String SENT = "sent";
    public static final String SENDER_ID = "sender_id";
    public static final String MESSAGE_MEDIA = "message_media";
    public static final String ATTACHMENT_TYPE = "attachment_type";
    public static final String ATTACHMENT_TYPE_PHOTO = "attachment_type_photo";
    public static final String ATTACHMENT_TYPE_VIDEO = "attachment_type_video";
    public static final String ATTACHMENT_TYPE_LOCATION = "attachment_type_location";
    public static final String ATTACHMENT_TYPE_CONTACT = "attachment_type_contact";
    public static final String ATTACHMENT_TYPE_DOCUMENT = "attachment_type_document";
    public static final String ATTACHEMENT_TYPE_VOICE_NOTE = "attachment_type_voice_note";
    public static final String ATTACHMENT_TYPE_MUSIC = "attachment_type_music";
    public static final String ATTACHMENT = "attachments";
    public static final String MEET_POINTS = "meet_points";
    public static final String MEET_POINT_WITH = "meet_point_with_";
    public static final String CONVERSATION_ID = "conversation_id";
    public static final String GENERATED_MEET_POINT = "generated_meet_point";
    public static final String RECIPIENT = "recipient";
    public static final String MESSAGE_ID = "message_id";
    public static final String EXTRA_FROM = "from";
    public static final String EXTRA_TYPE = "type";
    public static final String EXTRA_TO = "to";
    public static final String UNREAD_MESSAGES_COUNT = "unread_messages_count";
    public static final String GROUPS_AND_ROOMS = "groups";
    public static final String CHAT_TYPE = "chat_type";
    public static final String EXTRA_USER_ID = "userId";
    public static final String SUSPEND_ALL_USE_OF_AUDIO_MANAGER = "suspend_audio_use";
    public static final String EXTRA_IS_INCOMING_CALL = "isIncomingCall";

    public static final int ENTITY_TYPE_INDIVIDUAL = 0;
    public static final int ENTITY_TYPE_GROUP = 1;
    public static final int ENTITY_TYPE_CHAT_ROOM = 2;

    public static final String GROUP_OR_CHAT_ROOM_NAME = "group_or_chat_room_name";
    public static final String GROUP_OR_CHAT_ROOM_PHOTO_URL = "group_or_chat_room_photo_url";
    public static final String GROUP_OR_CHAT_ROOM_COVER_PHOTO = "group_or_chat_room_cover_photo";
    public static final String ROOM_TYPE = "room_type";
    public static final int ENTITY_TYPE_CHATS = 1;
    public static final String ROOM_DESCRIPTION = "room_description";
    public static final String LAST_CONVERSATION_TIME_WITH = "last_conversation_time_with_";
    public static final String OBJECT_ID = "objectId";
    public static final String FEED_TYPE_JOIN_GROUP_REQUEST = "feed_type_join_group";
    public static final String GROUP_OR_ROOM = "group_or_room";
    public static final String REFRESH_CONVERSATIONS = "refresh_conversations";
    public static final String CONVERSATIONS = "conversations";
    public static final String PEOPLE_GROUPS_AND_ROOMS = "PeopleGroupsAndRooms";
    public static final String REPLICATED_OBJECT_ID = "replicated_object_id";
    public static final String OBJECT_TYPE = "object_type";
    public static final Object OBJECT_TYPE_INDIVIDUAL = "object_type_individual";
    public static final String NOTIFICATION_TYPE_INDIVIDUAL_CHAT_REQUEST = "individual_chat_request";
    public static final int NEARBY_KIND_NOTIFICATION_ID = 0x14;
    public static final String NOTIFICATION_TYPE_AM_NEARBY = "am_nearby";
    public static final String GENDER_FILTER = "gender_filter";

    public static final String AGE_START_FILTER = "age_start_filter";
    public static final String AGE_END_FILTER = "age_end_filter";

    public static final String HAS_PENDING_CHAT_REQUEST = "has_pending_chat_request";
    public static final String PENDING_CHAT_REQUEST = "pending_chat_request";
    public static final String FILE_CAPTION = "file_caption";
    public static final String NOTIFICATION_TYPE_NEW_MESSAGE = "new_message";
    public static final String UNREAD_MESSAGE = "unread_message";
    public static final String UNREAD_MESSAGES_FROM_SAME_SENDER = "unread_messages_from_same_sender";
    public static final String OBJECT_TYPE_GROUP = "object_type_group";
    public static final String OBJECT_TYPE_ROOM = "object_type_room";

    public static final String MASKED_OBJECT_ID = "masked_objectId";
    public static final String ATTEMPT_LOGOUT = "attempt_logout";
    public static final String AUTHENTICATED_USER_DETAILS = "authenticated_user_details";
    public static final String LAST_ATTEMPTED_MESSAGE_FOR = "last_attempted_msg_for_";
    public static final String TURN_OFF_ALL_TAB_LAYOUTS = "turn_off_all_tablayouts";

    public static int UNACKNOWLEDGED_CHAT_REQUESTS_COUNT = 0;
    public static int CHAT_REQUEST_NOTIFICATION_ID = 0x11;

    public static String NOTIFICATION_TYPE = "notification_type";

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
    public static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

    public static int ANIMATION_DURATION_MEDIUM = 400;

    public static final String SYSTEM_EMOJI_PREF = "hollout_pref_system_emoji";

    public static final String LANGUAGE_PREF = "pref_language";
    public static final String ENTER_SENDS_PREF = "pref_enter_sends";

    public static final String FILE_TYPE_VIDEO = "FileTypeVideo";
    public static final String FILE_TYPE_PHOTO = "FileTypePhoto";
    public static final String FILE_TYPE_AUDIO = "FileTypeAudio";
    public static final int REQUEST_CODE_PICK_FROM_GALLERY = 10;
    public static final String GALLERY_RESULTS = "EXTRA_GALLERY_RESULTS";
    public static final String PICKED_MEDIA_FILES_EMPTY_NOW = "PickedMediaFilesEmptyNow";

    public static final String[] projectionPhotos = {
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.ORIENTATION
    };
    public static final String[] projectionVideo = {
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.BUCKET_ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DATE_TAKEN,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DURATION
    };

    public static final String CONVERSATION_NAME_APPLY = "em_conversation_apply";
    //0:chat,1:groupChat
    public static final String MESSAGE_ATTR_TYPE = "em_type";
    public static final String MESSAGE_ATTR_USERNAME = "em_username";
    public static final String MESSAGE_ATTR_GROUP_ID = "em_group_id";
    public static final String MESSAGE_ATTR_REASON = "em_reason";
    public static final String MESSAGE_ATTR_STATUS = "em_status";
    //0 : private group,1:public group
    public static final String MESSAGE_ATTR_GROUP_TYPE = "em_group_type";


    public static final String ACCOUNT_CONFLICT = "conflict";

    // Broadcast action
    public static final String BROADCAST_ACTION_CALL = "com.wan.hollout.action.call";
    public static final String BROADCAST_ACTION_CONTACTS = "com.wan.hollout.action.contacts";
    public static final String BROADCAST_ACTION_GROUP = "com.wan.hollout.action.group";
    public static final String BROADCAST_ACTION_APPLY = "com.wan.hollout.action.apply";


    ///MeetPoint Properties
    public static final String MESSAGE_BODY = "message_body";

    public static SparseBooleanArray reactionsBackgroundPositions = new SparseBooleanArray();
    public static SparseBooleanArray reactionsOpenPositions = new SparseBooleanArray();
    public static SparseBooleanArray commentPositions = new SparseBooleanArray();
    public static SparseBooleanArray likesPositions = new SparseBooleanArray();
    public static SparseBooleanArray messageBodyPositions = new SparseBooleanArray();
    public static SparseBooleanArray messageTimeVisibilePositions = new SparseBooleanArray();
    public static SparseBooleanArray unreadMessagesPositions = new SparseBooleanArray();
    public static SparseBooleanArray lastMessageAvailablePositions = new SparseBooleanArray();
    public static SparseBooleanArray parseUserAvailableOnlineStatusPositions = new SparseBooleanArray();
    public static SparseBooleanArray onlinePositions = new SparseBooleanArray();
    public static SparseBooleanArray fileSizeOrDurationPositions = new SparseBooleanArray();
    public static SparseBooleanArray playableVideoPositions = new SparseBooleanArray();

    public static SparseBooleanArray wavePositions = new SparseBooleanArray();

    public static int NEARBY_KIND_NOTIFICATION_COUNT = 0;

    public static final String MALE = "Male";
    public static final String FEMALE = "Female";
    public static String Both = "Both";

    public static final String START_AGE_FILTER_VALUE = "start_age_filter_value";
    public static final String END_AGE_FILTER_VALUE = "end_age_filter_value";

}

