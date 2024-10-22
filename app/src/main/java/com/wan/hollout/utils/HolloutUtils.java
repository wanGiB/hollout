package com.wan.hollout.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.parse.DeleteCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.wan.hollout.R;
import com.wan.hollout.bean.AudioFile;
import com.wan.hollout.clients.ChatClient;
import com.wan.hollout.components.ApplicationLoader;
import com.wan.hollout.eventbuses.ChatRequestNegotiationResult;
import com.wan.hollout.interfaces.DoneCallback;

import net.alhazmy13.mediapicker.Image.ImagePicker;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;
import static com.wan.hollout.utils.AppConstants.LANGUAGE_PREF;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * @author Wan Clem
 */

@SuppressWarnings({"ResultOfMethodCallIgnored", "unused"})
public class HolloutUtils {

    public static String TAG = "HolloutUtils";

    private static Boolean isTablet = null;
    private static Point displaySize = new Point();
    private static DisplayMetrics displayMetrics = new DisplayMetrics();

    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();

    public static float getFileSizeInMB(long length) {
        return length / (1024 * 1024);
    }

    static {
        checkDisplaySize(ApplicationLoader.getInstance());
    }

    private static boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    public static void assertMainThread() {
        if (!isMainThread()) {
            throw new AssertionError("Main-thread assertion failed.");
        }
    }

    public static String stripDollar(String string) {
        return StringUtils.replace(string, "$", "USD");
    }

    public static int clamp(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }

    public static void bangSound(Context context, int soundId) {
        MediaPlayer mediaPlayer = MediaPlayer.create(context, soundId);
        mediaPlayer.setVolume(0.2f, 0.2f);
        mediaPlayer.start();
    }

    public static String resolveToBestLocation(ParseObject referenceUser) {

        String displayableUserLocation = "";
        String referenceHolloutCountry = (String) referenceUser.get(AppConstants.APP_USER_COUNTRY);
        String referenceHolloutAdmin = (String) referenceUser.get(AppConstants.APP_USER_ADMIN_AREA);
        String referenceHolloutLocality = (String) referenceUser.get(AppConstants.APP_USER_LOCALITY);
        String referenceHolloutStreetAddress = (String) referenceUser.get(AppConstants.APP_USER_STREET);

        ParseObject signedInUser = AuthUtil.getCurrentUser();

        if (signedInUser != null) {

            String currentUserCountry = signedInUser.getString(AppConstants.APP_USER_COUNTRY);
            String currentUserAdminArea = signedInUser.getString(AppConstants.APP_USER_ADMIN_AREA);
            String currentUserLocality = signedInUser.getString(AppConstants.APP_USER_LOCALITY);
            String currentUserStreetAddress = signedInUser.getString(AppConstants.APP_USER_STREET);

            //Lets put a lot of things into consideration before showing location
            if (isNotEmpty(currentUserCountry) && isNotEmpty(referenceHolloutCountry)) {
                if (currentUserCountry.equals(referenceHolloutCountry)) {
                    //If this contact is still in my country,show me his/her discreet location
                    if (isNotEmpty(currentUserAdminArea) && isNotEmpty(referenceHolloutAdmin)) {
                        //Go down to admin level
                        if (currentUserAdminArea.equals(referenceHolloutAdmin)) {
                            //Go down a little bit
                            if (isNotEmpty(currentUserLocality) && isNotEmpty(referenceHolloutLocality)) {
                                if (currentUserLocality.equals(referenceHolloutLocality)) {
                                    //Go down to street level
                                    if (isNotEmpty(currentUserStreetAddress) && isNotEmpty(referenceHolloutStreetAddress)) {
                                        displayableUserLocation = referenceHolloutStreetAddress;
                                    }
                                } else {
                                    displayableUserLocation = referenceHolloutLocality + ",".concat(referenceHolloutAdmin);
                                }
                            } else {
                                displayableUserLocation = referenceHolloutAdmin;
                            }
                        } else {
                            displayableUserLocation = referenceHolloutAdmin;
                        }
                    } else {
                        if (StringUtils.isNotEmpty(referenceHolloutStreetAddress)) {
                            displayableUserLocation = referenceHolloutStreetAddress;
                        }
                    }
                } else {
                    //My contact is no longer in my country,display his new location
                    displayableUserLocation = referenceHolloutCountry;
                }
            }
        }
        return displayableUserLocation;
    }

    public static String formatDistance(double distanceBetweenTwoLocations) {
        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        decimalFormat.setRoundingMode(RoundingMode.CEILING);
        return decimalFormat.format(distanceBetweenTwoLocations);
    }

    private static void checkDisplaySize(Context context) {
        try {
            float density = context.getResources().getDisplayMetrics().density;
            Configuration configuration;
            configuration = context.getResources().getConfiguration();
            WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            if (manager != null) {
                Display display = manager.getDefaultDisplay();
                if (display != null) {
                    display.getMetrics(displayMetrics);
                    display.getSize(displaySize);
                }
            }
            if (configuration.screenWidthDp != Configuration.SCREEN_WIDTH_DP_UNDEFINED) {
                int newSize = (int) Math.ceil(configuration.screenWidthDp * density);
                if (Math.abs(displaySize.x - newSize) > 3) {
                    displaySize.x = newSize;
                }
            }
            if (configuration.screenHeightDp != Configuration.SCREEN_HEIGHT_DP_UNDEFINED) {
                int newSize = (int) Math.ceil(configuration.screenHeightDp * density);
                if (Math.abs(displaySize.y - newSize) > 3) {
                    displaySize.y = newSize;
                }
            }
        } catch (Exception ignored) {

        }
    }

    static {
        suffixes.put(1_000L, "k");
        suffixes.put(1_000_000L, "M");
        suffixes.put(1_000_000_000L, "G");
        suffixes.put(1_000_000_000_000L, "T");
        suffixes.put(1_000_000_000_000_000L, "P");
        suffixes.put(1_000_000_000_000_000_000L, "E");
    }

    public static String format(long value) {
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) return format(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + format(-value);
        if (value < 1000) return Long.toString(value); //deal with easy case

        Map.Entry<Long, String> e = suffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }

    /**
     * check if network avalable
     */
    public static boolean isNetWorkConnected(Context context) {
        if (context != null) {
            ConnectivityManager
                    mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = null;
            if (mConnectivityManager != null) {
                mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            }
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable() && mNetworkInfo.isConnected();
            }
        }
        return false;
    }

    public static ArrayList<String> getAllOfAUserPhotos(String profilePhoto, List<String> additionalPhotos) {
        ArrayList<String> resultantPhotos = new ArrayList<>();
        resultantPhotos.add(profilePhoto);
        if (additionalPhotos != null) {
            for (String additionalPhoto : additionalPhotos) {
                if (!additionalPhoto.equals(profilePhoto) && !resultantPhotos.contains(additionalPhoto)) {
                    resultantPhotos.add(additionalPhoto);
                }
            }
        }
        return resultantPhotos;
    }

    /**
     * Code for composing an email and launching an
     * email client installed on the device
     * Used for sending feedback
     */
    public static void composeEmail(String[] addresses, String subject, Context context) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }
    }

    public static int hashCode(@Nullable Object... objects) {
        return Arrays.hashCode(objects);
    }

    public static boolean equals(@Nullable Object a, @Nullable Object b) {
        return a == b || (a != null && a.equals(b));
    }

    public static void startImagePicker(Activity activity) {
        new ImagePicker.Builder(activity)
                .mode(ImagePicker.Mode.CAMERA_AND_GALLERY)
                .compressLevel(ImagePicker.ComperesLevel.NONE)
                .directory(ImagePicker.Directory.DEFAULT)
                .extension(ImagePicker.Extension.PNG)
                .allowMultipleImages(false)
                .enableDebuggingMode(true)
                .build();
    }

    public static String getEncodedString(String string) {
        return string;
    }

    public static Bitmap convertDrawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        int width = drawable.getIntrinsicWidth();
        width = width > 0 ? width : 80;
        int height = drawable.getIntrinsicHeight();
        height = height > 0 ? height : 80;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private static String[] suffix = new String[]{"", "k", "m", "b", "t"};

    public static String format(double number) {
        String r = new DecimalFormat("##0E0").format(number);
        r = r.replaceAll("E[0-9]", suffix[Character.getNumericValue(r.charAt(r.length() - 1)) / 3]);
        int MAX_LENGTH = 4;
        while (r.length() > MAX_LENGTH || r.matches("[0-9]+\\.[a-z]")) {
            r = r.substring(0, r.length() - 2) + r.substring(r.length() - 1);
        }
        return r;
    }

    public static void updateCurrentParseInstallation(List<String> newChannelProps) {
        ParseObject signedInUser = AuthUtil.getCurrentUser();
        if (signedInUser != null) {
            ParseInstallation parseInstallation = ParseInstallation.getCurrentInstallation();
            parseInstallation.put(AppConstants.REAL_OBJECT_ID, signedInUser.getString(AppConstants.REAL_OBJECT_ID));
            List<String> existingChannels = parseInstallation.getList("channels");
            checkAndUpdateChannels(newChannelProps, parseInstallation, existingChannels);
            parseInstallation.saveInBackground();
        }
    }

    private static void checkAndUpdateChannels(List<String> newChannelProps,
                                               ParseInstallation parseInstallation,
                                               List<String> existingChannels) {
        if (existingChannels != null) {
            for (String newChannelItem : newChannelProps) {
                if (!existingChannels.contains(newChannelItem)) {
                    existingChannels.add(newChannelItem);
                }
            }
            parseInstallation.put("channels", existingChannels);
        } else {
            List<String> newChannels = new ArrayList<>();
            for (String newChannelItem : newChannelProps) {
                if (!newChannels.contains(newChannelItem)) {
                    newChannels.add(newChannelItem);
                }
            }
            parseInstallation.put("channels", newChannels);
        }
    }

    private static String getHashedString(String plainString) {
        if (isNotEmpty(plainString)) {
            return Base64.encodeToString(plainString.getBytes(), Base64.DEFAULT);
        } else {
            return Base64.encodeToString("Default".getBytes(), Base64.DEFAULT);
        }
    }

    public static void uploadFileAsync(final String filePath, String directory, final DoneCallback<String> doneCallback) {
        final String hashedPhotoPath = getHashedString(filePath);
        String probablePreviousUpload = HolloutPreferences.getAPreviousUploadFromPreference(hashedPhotoPath);
        //There was no previous upload...Let's upload the file
        if (probablePreviousUpload.equals(hashedPhotoPath)) {
            Uri uri = Uri.fromFile(new File(filePath));
            StorageReference storageReference = FirebaseUtils.getFirebaseStorageReference().child(directory).child(uri.getLastPathSegment());
            storageReference.putFile(uri.normalizeScheme())
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @SuppressWarnings("unused")
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        }
                    })
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            if (taskSnapshot.getDownloadUrl() != null) {
                                String returnedFileUrl = taskSnapshot.getDownloadUrl().toString();
                                HolloutLogger.e(TAG, "Completed Upload of file = " + filePath + " with upload url = " + returnedFileUrl);
                                HolloutPreferences.persistUploadedFile(hashedPhotoPath, returnedFileUrl);
                                doneCallback.done(returnedFileUrl, null);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    HolloutLogger.e(TAG, "An error occurred while uploading file. Error message = " + e.getMessage());
                    doneCallback.done(null, e);
                }
            });
        } else {
            doneCallback.done(probablePreviousUpload, null);
        }
    }

    public static String getAppVersionName() {
        try {
            PackageInfo packageInfo = ApplicationLoader.getInstance().getPackageManager()
                    .getPackageInfo(ApplicationLoader.getInstance().getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            HolloutLogger.e(HolloutUtils.class.getSimpleName(), "Could not get package name: " + e);
        }
        return null;
    }

    public static String getLanguage() {
        return HolloutPreferences.getLanguage(LANGUAGE_PREF);
    }

    public static long getVideoDuration(String file) {
        MediaPlayer mediaPlayer = MediaPlayer.create(ApplicationLoader.getInstance(), Uri.parse(file));
        return mediaPlayer.getDuration();
    }

    public static String getFileSize(Context context, Uri uri) {
        String sizeInMB = null;
        Cursor returnCursor = context.getContentResolver().query(uri, null, null, null, null);
        try {
            if (returnCursor != null && returnCursor.moveToFirst()) {
                int columnIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                Long fileSize = returnCursor.getLong(columnIndex);
                if (fileSize < 1024) {
                    sizeInMB = (int) (fileSize / (1024 * 1024)) + " B";
                } else if (fileSize < 1024 * 1024) {
                    sizeInMB = (int) (fileSize / (1024)) + " KB";
                } else {
                    sizeInMB = (int) (fileSize / (1024 * 1024)) + " MB";
                }
            }
        } finally {
            if (returnCursor != null) {
                returnCursor.close();
            }
        }
        return sizeInMB;
    }

    public static boolean isFileCode(String fileMime) {
        return StringUtils.endsWithAny(fileMime, "java", "cpp", "c", "c++", "fortran", "cobol", "jar", "asm", "py", "php", "js", "scala", "html", "css", "xml", "xhtml");
    }

    public static boolean isValidDocument(String fileMime) {
        return !StringUtils.startsWithAny(fileMime, "image", "photo", "audio", "video");
    }

    public static boolean isImage(String fileMime) {
        return StringUtils.startsWith("image", fileMime);
    }

    public static boolean isAudio(String fileMime) {
        return StringUtils.startsWith(fileMime, "audio");
    }

    public static boolean isVideo(String fileMime) {
        return StringUtils.startsWith(fileMime, "video");
    }

    public static boolean isFilePdf(String fileMime) {
        return StringUtils.endsWithAny(fileMime, "pdf");
    }

    public static boolean isValidPhotoType(String fileMime) {
        return StringUtils.startsWithAny(fileMime, "image", "photo");
    }

    public static File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        String externalStorageMountedState = Environment.getExternalStorageState();
        File mediaStorageDir = null;

        if (externalStorageMountedState.equals(Environment.MEDIA_MOUNTED)) {
            if (type == AppConstants.CAPTURE_MEDIA_TYPE_IMAGE) {
                mediaStorageDir = new File(Environment.getExternalStorageDirectory().getPath() + "/" + AppConstants.HOLLOUT_MEDIA_PATH, "Hollout Photos");
            } else if (type == AppConstants.CAPTURE_MEDIA_TYPE_AUDIO) {
                mediaStorageDir = new File(Environment.getExternalStorageDirectory().getPath() + "/" + AppConstants.HOLLOUT_MEDIA_PATH, "Hollout Audio");
            } else if (type == AppConstants.CAPTURE_MEDIA_TYPE_VIDEO) {
                mediaStorageDir = new File(Environment.getExternalStorageDirectory().getPath() + "/" + AppConstants.HOLLOUT_MEDIA_PATH, "Hollout Videos");
            }
        } else {
            if (type == AppConstants.CAPTURE_MEDIA_TYPE_IMAGE) {
                mediaStorageDir = new File(Environment.getDownloadCacheDirectory().getPath() + "/" + AppConstants.HOLLOUT_MEDIA_PATH, "Hollout Photos");
            } else if (type == AppConstants.CAPTURE_MEDIA_TYPE_AUDIO) {
                mediaStorageDir = new File(Environment.getDownloadCacheDirectory().getPath() + "/" + AppConstants.HOLLOUT_MEDIA_PATH, "Hollout Audio");
            } else if (type == AppConstants.CAPTURE_MEDIA_TYPE_VIDEO) {
                mediaStorageDir = new File(Environment.getDownloadCacheDirectory().getPath() + "/" + AppConstants.HOLLOUT_MEDIA_PATH, "Hollout Videos");
            }
        }

        // Create the storage directory if it does not exist
        if (mediaStorageDir != null && !mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                HolloutLogger.d("HolloutCamera", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == AppConstants.CAPTURE_MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + "/" +
                    "IMG_" + timeStamp + ".jpg");
        } else if (type == AppConstants.CAPTURE_MEDIA_TYPE_AUDIO) {
            mediaFile = new File(mediaStorageDir.getPath() + "/" + "AUDIO_" + timeStamp + ".mp3");
        } else if (type == AppConstants.CAPTURE_MEDIA_TYPE_VIDEO) {
            //Type is of type video
            mediaFile = new File(mediaStorageDir.getPath() + "/" + "VIDEO" + timeStamp + ".mp4");
        } else {
            mediaFile = null;
        }
        return mediaFile;
    }

    public static boolean hasMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static File getFilePath(String fileName, Context context) {
        return getFilePath(fileName, context, "text/x-vcard", false);
    }

    private static final String IMAGE_DIR = "image";
    private static final String HOLLOUT_IMAGES_FOLDER = "/Image";
    private static final String HOLLOUT_VIDEOS_FOLDER = "/video";
    private static final String HOLLOUT_CONTACT_FOLDER = "/contact";
    private static final String HOLLOUT_OTHER_FILES_FOLDER = "/other";
    private static final String HOLLOUT_THUMBNAIL_SUFFIX = "/.Thumbnail";
    private static final String MAIN_FOLDER_META_DATA = "Hollout";

    public static File getFilePath(String fileName, Context context, String contentType, boolean isThumbnail) {
        File filePath;
        File dir;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            String folder = "/" + getMetaDataValue(context, MAIN_FOLDER_META_DATA) + HOLLOUT_OTHER_FILES_FOLDER;
            if (contentType.startsWith("image")) {
                folder = "/" + getMetaDataValue(context, MAIN_FOLDER_META_DATA) + HOLLOUT_IMAGES_FOLDER;
            } else if (contentType.startsWith("video")) {
                folder = "/" + getMetaDataValue(context, MAIN_FOLDER_META_DATA) + HOLLOUT_VIDEOS_FOLDER;
            } else if (contentType.equalsIgnoreCase("text/x-vCard")) {
                folder = "/" + getMetaDataValue(context, MAIN_FOLDER_META_DATA) + HOLLOUT_CONTACT_FOLDER;
            }
            if (isThumbnail) {
                folder = folder + HOLLOUT_THUMBNAIL_SUFFIX;
            }
            dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + folder);
            if (!dir.exists()) {
                dir.mkdirs();
            }
        } else {
            ContextWrapper cw = new ContextWrapper(context);
            dir = cw.getDir(IMAGE_DIR, Context.MODE_PRIVATE);
        }
        filePath = new File(dir, fileName);
        return filePath;
    }

    public static File getChatBackUpFilePath(String fileName, Context context) {
        File filePath;
        File dir;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            String folder = "/" + MAIN_FOLDER_META_DATA + "/ChatsBackUp";
            dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + folder);
            if (!dir.exists()) {
                dir.mkdirs();
            }
        } else {
            ContextWrapper cw = new ContextWrapper(context);
            dir = cw.getDir("/ChatsBackUp", Context.MODE_PRIVATE);
        }
        filePath = new File(dir, fileName);
        return filePath;
    }

    private static String getMetaDataValue(Context context, String metaDataName) {
        return "Hollout";
    }

    public static String getGifUrl(JSONObject gifObject) {
        JSONObject originalGifProps = gifObject
                .optJSONObject("images")
                .optJSONObject("original");
        return originalGifProps.optString("url");
    }

    public static void deleteOutgoingChatRequests(String recipientId) {
        ParseObject signedInUser = AuthUtil.getCurrentUser();
        if (signedInUser != null) {
            final ParseQuery<ParseObject> requestObjectQuery = ParseQuery.getQuery(AppConstants.HOLLOUT_FEED);
            requestObjectQuery.whereEqualTo(AppConstants.FEED_RECIPIENT_ID, recipientId);
            requestObjectQuery.whereEqualTo(AppConstants.FEED_TYPE, AppConstants.FEED_TYPE_CHAT_REQUEST);
            requestObjectQuery.whereEqualTo(AppConstants.FEED_CREATOR_ID, signedInUser.getString(AppConstants.REAL_OBJECT_ID));
            requestObjectQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(final ParseObject object, ParseException e) {
                    if (e == null && object != null) {
                        object.deleteInBackground(new DeleteCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e != null) {
                                    object.deleteEventually();
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    public static class MediaEntry {
        int bucketId;
        int imageId;
        long dateTaken;
        public String path;
        public int orientation;
        boolean isVideo;
        public String bucketName;
        public String fileName;
        public long fileSize;
        public long fileDuration;
        boolean selected;

        MediaEntry(int bucketId, int imageId, long dateTaken, String path, int orientation, boolean isVideo, String bucketName, String fileName, long fileSize, long fileDuration) {
            this.bucketId = bucketId;
            this.imageId = imageId;
            this.dateTaken = dateTaken;
            this.path = path;
            this.orientation = orientation;
            this.isVideo = isVideo;
            this.bucketName = bucketName;
            this.fileName = fileName;
            this.fileSize = fileSize;
            this.fileDuration = fileDuration;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public boolean isSelected() {
            return selected;
        }

        @Override
        public int hashCode() {
            int result;
            result = path.hashCode();
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

            MediaEntry another = (MediaEntry) obj;

            return this.path.equals(another.path);

        }
    }

    public static ArrayList<AudioFile> getSortedMusicFiles(Context context) {

        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM,
        };

        ArrayList<AudioFile> audioFiles = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, MediaStore.Audio.Media.IS_MUSIC + " != 0", null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    AudioFile audioFile = new AudioFile();

                    audioFile.setAudioId(cursor.getInt(0));
                    audioFile.setAuthor(cursor.getString(1));
                    audioFile.setTitle(cursor.getString(2));
                    audioFile.setPath(cursor.getString(3));
                    audioFile.setDuration((cursor.getLong(4)));
                    audioFile.setGenre(cursor.getString(5));
                    audioFiles.add(audioFile);
                }
            }
        } catch (Exception e) {
            HolloutLogger.e("HOLLOUTUtils", "Error loading audio" + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        Collections.sort(audioFiles);
        return audioFiles;
    }

    public static ArrayList<MediaEntry> getSortedPhotos(Context context) {
        ArrayList<MediaEntry> photoEntriesSorted = new ArrayList<>();
        Cursor cursor = null;
        try {
            if (Build.VERSION.SDK_INT < 23 || Build.VERSION.SDK_INT >= 23 && context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                cursor = MediaStore.Images.Media.query(context.getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI, AppConstants.projectionPhotos, null, null, MediaStore.Images.Media.DATE_TAKEN + " DESC");
                if (cursor != null) {
                    int imageIdColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                    int bucketIdColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID);
                    int bucketNameColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
                    int dataColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                    int dateColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
                    int orientationColumn = cursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION);

                    while (cursor.moveToNext()) {

                        int imageId = cursor.getInt(imageIdColumn);
                        int bucketId = cursor.getInt(bucketIdColumn);
                        String bucketName = cursor.getString(bucketNameColumn);
                        String path = cursor.getString(dataColumn);
                        long dateTaken = cursor.getLong(dateColumn);
                        int orientation = cursor.getInt(orientationColumn);

                        if (path == null || path.length() == 0) {
                            continue;
                        }

                        MediaEntry newMediaEntry = new MediaEntry(bucketId, imageId, dateTaken, path, orientation, false, bucketName, null, 0, 0);
                        photoEntriesSorted.add(newMediaEntry);

                    }

                }

            }

        } catch (Throwable e) {
            HolloutLogger.e(TAG, e.getMessage());

        } finally {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    HolloutLogger.e(TAG, e.getMessage());
                }
            }
        }
        return photoEntriesSorted;
    }

    public static ArrayList<MediaEntry> getSortedVideos(Context context) {

        ArrayList<MediaEntry> sortedVideoEntries = new ArrayList<>();

        Cursor cursor = null;

        try {

            if (Build.VERSION.SDK_INT < 23 || Build.VERSION.SDK_INT >= 23 && context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                cursor = MediaStore.Images.Media.query(context.getContentResolver(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI, AppConstants.projectionVideo, null, null, MediaStore.Video.Media.DATE_TAKEN + " DESC");

                if (cursor != null) {
                    int imageIdColumn = cursor.getColumnIndex(MediaStore.Video.Media._ID);
                    int bucketIdColumn = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_ID);
                    int bucketNameColumn = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);
                    int dataColumn = cursor.getColumnIndex(MediaStore.Video.Media.DATA);
                    int dateColumn = cursor.getColumnIndex(MediaStore.Video.Media.DATE_TAKEN);
                    int fileNameColumn = cursor.getColumnIndex(MediaStore.Video.Media.TITLE);
                    int fileSizeColumn = cursor.getColumnIndex(MediaStore.Video.Media.SIZE);
                    int fileDuration = cursor.getColumnIndex(MediaStore.Video.Media.DURATION);

                    while (cursor.moveToNext()) {

                        int videoId = cursor.getInt(imageIdColumn);
                        int bucketId = cursor.getInt(bucketIdColumn);
                        String bucketName = cursor.getString(bucketNameColumn);
                        String path = cursor.getString(dataColumn);
                        long dateTaken = cursor.getLong(dateColumn);
                        int orientation = 0;
                        String fileName = cursor.getString(fileNameColumn);
                        long fileSize = cursor.getLong(fileSizeColumn);
                        long durationOfFile = cursor.getLong(fileDuration);

                        if (path == null || path.length() == 0) {
                            continue;
                        }
                        MediaEntry newMediaEntry = new MediaEntry(bucketId, videoId, dateTaken, path, orientation, false, bucketName, fileName, fileSize, durationOfFile);
                        sortedVideoEntries.add(newMediaEntry);
                    }
                }
            }
        } catch (Throwable e) {
            HolloutLogger.e(TAG, e.getMessage());

        } finally {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    HolloutLogger.e(TAG, e.getMessage());
                }
            }
        }
        return sortedVideoEntries;
    }

    static String getQualifier(String word) {
        if (StringUtils.startsWithAny(word.toLowerCase(), "a,e,i,o,u")) {
            return "an";
        } else {
            return "a";
        }
    }

    public static List<String> computeAgeRanges(String startAge, String endAge) {
        List<String> ageRanges = new ArrayList<>();
        for (int i = Integer.parseInt(startAge.trim()); i < Integer.parseInt(endAge.trim()); i++) {
            ageRanges.add(String.valueOf(i));
        }
        return ageRanges;
    }

    public static boolean checkGooglePlayServices(Activity activity) {
        final int status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activity);
        if (status != ConnectionResult.SUCCESS) {
            HolloutLogger.d(TAG, GoogleApiAvailability.getInstance().getErrorString(status));
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(activity, status, 1);
            if (!dialog.isShowing()) {
                dialog.show();
            }
            return false;
        } else {
            HolloutLogger.i(TAG, GoogleApiAvailability.getInstance().getErrorString(status));
            return true;
        }
    }

    @SuppressWarnings("ConstantConditions")
    public static void sendChatState(String chatState, String recipientId) {
        ParseObject signedInUserObject = AuthUtil.getCurrentUser();
        if (recipientId != null) {
            JSONObject existingChatStates = signedInUserObject.getJSONObject(AppConstants.APP_USER_CHAT_STATES);
            JSONObject chatStates = existingChatStates != null ? existingChatStates : new JSONObject();
            try {
                chatStates.put(recipientId, chatState);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            signedInUserObject.put(AppConstants.APP_USER_CHAT_STATES, chatStates);
            AuthUtil.updateCurrentLocalUser(signedInUserObject, null);
        }
    }

    public static String getDecimalFormattedString(String value) {
        if (value != null && !value.equalsIgnoreCase("")) {
            StringTokenizer lst = new StringTokenizer(value, ".");
            String str1 = value;
            String str2 = "";
            if (lst.countTokens() > 1) {
                str1 = lst.nextToken();
                str2 = lst.nextToken();
            }
            String str3 = "";
            int i = 0;
            int j = -1 + str1.length();
            if (str1.charAt(-1 + str1.length()) == '.') {
                j--;
                str3 = ".";
            }
            for (int k = j; ; k--) {
                if (k < 0) {
                    if (str2.length() > 0)
                        str3 = str3 + "." + str2;
                    return str3;
                }
                if (i == 3) {
                    str3 = "," + str3;
                    i = 0;
                }
                str3 = str1.charAt(k) + str3;
                i++;
            }
        }
        return "";
    }

    @SuppressLint("SetTextI18n")
    public static String formatDistanceToUser(String value) {
        DecimalFormat df2 = new DecimalFormat(".##");
        String returnable;
        try {
            double valueOfAmount = Double.parseDouble(value);
            String refinedAmount = df2.format(valueOfAmount);
            returnable = HolloutUtils.getDecimalFormattedString(refinedAmount);
            return returnable;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "0";
    }

    public static boolean isAContact(String recipientId) {
        ParseObject signedInUser = AuthUtil.getCurrentUser();
        if (signedInUser != null) {
            List<String> signedInUserChats = signedInUser.getList(AppConstants.APP_USER_CHATS);
            return (signedInUserChats != null && signedInUserChats.contains(recipientId.toLowerCase()));
        }
        return false;
    }

    public static boolean isUserBlocked(String userId) {
        List<String> blockedUsers = ChatClient.getInstance().getBlackList();
        return blockedUsers != null && !blockedUsers.isEmpty() && blockedUsers.contains(userId);
    }

    public String getAppName(Context context, int pID) {
        String processName;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (am != null) {
            List l = am.getRunningAppProcesses();
            for (Object aL : l) {
                ActivityManager.RunningAppProcessInfo info =
                        (ActivityManager.RunningAppProcessInfo) (aL);
                try {
                    if (info.pid == pID) {
                        processName = info.processName;
                        return processName;
                    }
                } catch (Exception ignored) {

                }
            }
        }
        return null;
    }

    public static void blockUser(final String userId, final DoneCallback<Boolean> blockDoneCallBack) {
        ParseObject signedInUserObject = AuthUtil.getCurrentUser();
        if (signedInUserObject != null) {
            List<String> userBlackList = signedInUserObject.getList(AppConstants.USER_BLACK_LIST);
            if (userBlackList == null || userBlackList.isEmpty()) {
                userBlackList = new ArrayList<>();
            }
            if (!userBlackList.contains(userId)) {
                userBlackList.add(userId);
            }
            signedInUserObject.put(AppConstants.USER_BLACK_LIST, userBlackList);
            AuthUtil.updateCurrentLocalUser(signedInUserObject, new DoneCallback<Boolean>() {
                @Override
                public void done(Boolean success, Exception e) {
                    blockDoneCallBack.done(success, e);
                }
            });
        }
    }

    public static void unBlockUser(final String userId, final DoneCallback<Boolean> unblockDoneCallBack) {
        ParseObject signedInUserObject = AuthUtil.getCurrentUser();
        if (signedInUserObject != null) {
            List<String> userBlackList = signedInUserObject.getList(AppConstants.USER_BLACK_LIST);
            if (userBlackList != null && !userBlackList.isEmpty()) {
                if (userBlackList.contains(userId)) {
                    userBlackList.remove(userId);
                    signedInUserObject.put(AppConstants.USER_BLACK_LIST, userBlackList);
                    AuthUtil.updateCurrentLocalUser(signedInUserObject, new DoneCallback<Boolean>() {
                        @Override
                        public void done(Boolean result, Exception e) {
                            unblockDoneCallBack.done(result, e);
                        }
                    });
                }
            }
        }
    }

    public static boolean checkDidUserBlockMe(ParseObject user) {
        if (user != null) {
            List<String> userBlackList = user.getList(AppConstants.USER_BLACK_LIST);
            ParseObject signedInUser = AuthUtil.getCurrentUser();
            return signedInUser != null && userBlackList != null && userBlackList.contains(signedInUser.getString(AppConstants.REAL_OBJECT_ID));
        }
        return false;
    }

    public static void acceptOrDeclineChat(final ParseObject requestObject, final boolean accept, final String requestOriginatorId,
                                           final String requesterName) {
        final ParseQuery<ParseObject> requestObjectQuery = ParseQuery.getQuery(AppConstants.HOLLOUT_FEED);
        requestObjectQuery.whereEqualTo(AppConstants.OBJECT_ID, requestObject.getObjectId());
        requestObjectQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(final ParseObject object, ParseException e) {
                if (e == null && object != null) {
                    object.deleteInBackground(new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                if (accept) {
                                    ParseObject signedInUser = AuthUtil.getCurrentUser();
                                    if (signedInUser != null) {
                                        List<String> signedInUserChats = signedInUser.getList(AppConstants.APP_USER_CHATS);
                                        if (signedInUserChats != null && !signedInUserChats.contains(requestOriginatorId.toLowerCase())) {
                                            signedInUserChats.add(requestOriginatorId.toLowerCase());
                                        }
                                        if (signedInUserChats == null) {
                                            signedInUserChats = new ArrayList<>();
                                            signedInUserChats.add(requestOriginatorId.toLowerCase());
                                        }
                                        signedInUser.put(AppConstants.APP_USER_CHATS, signedInUserChats);
                                        AuthUtil.updateCurrentLocalUser(signedInUser, null);
                                    }
                                    EventBus.getDefault().postSticky(AppConstants.REFRESH_CONVERSATIONS);
                                } else {
                                    //Delete all messages from this user from the local database
                                    DbUtils.deleteConversation(requestOriginatorId, null);
                                }
                                UiUtils.showSafeToast("Chat request from " + WordUtils.capitalize(requesterName) + " " + (accept ? "accepted" : "declined") + " successfully");
                                AppConstants.CHAT_INVITATION_ACCEPTED = true;
                            } else {
                                UiUtils.showSafeToast("Failed to " + (accept ? "accept" : "decline") + " chat request from " + WordUtils.capitalize(requesterName));
                                EventBus.getDefault().post(new ChatRequestNegotiationResult(requestObject, false, 0));
                            }
                        }
                    });
                }
                requestObjectQuery.cancel();
            }
        });
    }

    //This method prevents incessant device vibration on new messages.
    // Vibrate on new message if and only if the last vibration has lasted for more than 45seconds
    static boolean canVibrate() {
        long currentTimeInMills = System.currentTimeMillis();
        long lastVibrateTime = HolloutPreferences.getLastVibrateTime();
        long MAX_DURATION = TimeUnit.MILLISECONDS.convert(45, TimeUnit.SECONDS);
        long duration = currentTimeInMills - lastVibrateTime;
        boolean canVibrate = duration >= MAX_DURATION;
        if (canVibrate) {
            HolloutPreferences.setLastVibrateTime(currentTimeInMills);
        }
        return canVibrate;
    }

    public static String hashString(String inputString) {
        String sha1 = "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.reset();
            digest.update(inputString.getBytes("utf8"));
            sha1 = String.format("%040x", new BigInteger(1, digest.digest()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sha1;
    }

    public static String constructSearch() {
        ParseObject signedInUser = AuthUtil.getCurrentUser();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (signedInUser != null && firebaseUser != null) {
            List<String> aboutUser = signedInUser.getList(AppConstants.ABOUT_USER);
            String userDisplayName = signedInUser.getString(AppConstants.APP_USER_DISPLAY_NAME);
            String userEmail = firebaseUser.getEmail();
            if (aboutUser == null) {
                aboutUser = new ArrayList<>();
            }
            String searchCriteria = TextUtils.join(",", aboutUser) + "," + userDisplayName + "," + (userEmail != null ? userEmail : " ");
            return searchCriteria.toLowerCase();
        }
        return "none";
    }

    public static <T> List<T> safeSubList(List<T> list, int fromIndex, int toIndex) {
        int size = list.size();
        if (fromIndex >= size || toIndex <= 0 || fromIndex >= toIndex) {
            return Collections.emptyList();
        }
        fromIndex = Math.max(0, fromIndex);
        toIndex = Math.min(size, toIndex);
        return list.subList(fromIndex, toIndex);
    }

    public static Bitmap drawTextAsBitmap(String text, float textSize, int textColor, Typeface typeface) {
        Paint paint = new Paint(ANTI_ALIAS_FLAG);
        paint.setTextSize(textSize);
        paint.setColor(textColor);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(typeface);
        float baseline = -paint.ascent(); // ascent() is negative
        int width = (int) (paint.measureText(text) + 0.5f); // round
        int height = (int) (baseline + paint.descent() + 0.5f);
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        canvas.drawText(text, 0, baseline, paint);
        return image;
    }

    @SuppressLint("StringFormatInvalid")
    public static String timeAgo(long millis) {
        long diff = new Date().getTime() - millis;
        Resources r = ApplicationLoader.getInstance().getResources();

        String prefix = r.getString(R.string.time_ago_prefix);
        String suffix = r.getString(R.string.time_ago_suffix);

        double seconds = Math.abs(diff) / 1000;
        double minutes = seconds / 60;
        double hours = minutes / 60;
        double days = hours / 24;
        double years = days / 365;

        String words;

        if (seconds < 45) {
            words = r.getString(R.string.time_ago_seconds, Math.round(seconds));
        } else if (seconds < 90) {
            words = r.getString(R.string.time_ago_minute, 1);
        } else if (minutes < 45) {
            words = r.getString(R.string.time_ago_minutes, Math.round(minutes));
        } else if (minutes < 90) {
            words = r.getString(R.string.time_ago_hour, 1);
        } else if (hours < 24) {
            words = r.getString(R.string.time_ago_hours, Math.round(hours));
        } else if (hours < 42) {
            words = r.getString(R.string.time_ago_day, 1);
        } else if (days < 30) {
            words = r.getString(R.string.time_ago_days, Math.round(days));
        } else if (days < 45) {
            words = r.getString(R.string.time_ago_month, 1);
        } else if (days < 365) {
            words = r.getString(R.string.time_ago_months, Math.round(days / 30));
        } else if (years < 1.5) {
            words = r.getString(R.string.time_ago_year, 1);
        } else {
            words = r.getString(R.string.time_ago_years, Math.round(years));
        }

        StringBuilder sb = new StringBuilder();

        if (prefix != null && prefix.length() > 0) {
            sb.append(prefix).append(" ");
        }

        sb.append(words);

        if (suffix != null && suffix.length() > 0) {
            sb.append(" ").append(suffix);
        }

        return sb.toString().trim();
    }

}
