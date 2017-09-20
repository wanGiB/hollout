package com.wan.hollout.utils;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
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
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.util.EMLog;
import com.hyphenate.util.PathUtil;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.wan.hollout.R;
import com.wan.hollout.bean.AudioFile;
import com.wan.hollout.callbacks.DoneCallback;
import com.wan.hollout.chat.HolloutCommunicationsManager;
import com.wan.hollout.components.ApplicationLoader;

import net.alhazmy13.mediapicker.Image.ImagePicker;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.RoundingMode;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
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
import java.util.TreeMap;

import static com.wan.hollout.utils.AppConstants.LANGUAGE_PREF;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * @author Wan Clem
 */

public class HolloutUtils {

    public static String TAG = "HolloutUtils";

    public static float density = 1;
    public static int leftBaseline;
    public static boolean usingHardwareInput;
    private static Boolean isTablet = null;
    public static Point displaySize = new Point();

    public static DisplayMetrics displayMetrics = new DisplayMetrics();

    public static float getFileSizeInMB(long length) {
        return length / (1024 * 1024);
    }

    static {
        leftBaseline = isTablet() ? 80 : 72;
        checkDisplaySize(ApplicationLoader.getInstance(), null);
    }


    public static Kryo getKryoInstance() {
        Kryo kryo=new Kryo();
        kryo.register(EMMessage.class,new EMMessageSerializer());
        return kryo;
    }

    public static void removeMessageFromListOfUnread(final EMMessage emMessage){
        HolloutCommunicationsManager.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                HolloutUtils.deserializeMessages(AppConstants.UNREAD_MESSAGES, new DoneCallback<List<EMMessage>>() {
                    @Override
                    public void done(List<EMMessage> result, Exception e) {
                        if (e==null && result!=null){
                            for (EMMessage message:result){
                                if (message.getMsgId().equals(emMessage.getMsgId())){
                                    result.remove(message);
                                }
                            }
                            serializeMessages(result,AppConstants.UNREAD_MESSAGES);
                        }
                    }
                });
            }
        });
    }

    public static synchronized void serializeMessages(List<EMMessage> tObjects, String serializableName) {
        Kryo kryo = getKryoInstance();
        try {
            FileOutputStream fileOutputStream = ApplicationLoader.getInstance().openFileOutput(serializableName, Context.MODE_PRIVATE);
            Output messageOutPuts = new Output(fileOutputStream);
            kryo.writeObject(messageOutPuts, tObjects);
            messageOutPuts.close();
        } catch (IOException | KryoException | BufferOverflowException e) {
            e.printStackTrace();
            HolloutLogger.e(TAG, e.getMessage());
        }

    }

    @SuppressWarnings("unchecked")
    public static synchronized void deserializeMessages(String fileName, DoneCallback<List<EMMessage>> doneCallback) {
        Kryo kryo = getKryoInstance();
        try {
            Input input = new Input(ApplicationLoader.getInstance().openFileInput(fileName));
            ArrayList<EMMessage> previouslySerializedChats = kryo.readObject(input, ArrayList.class);
            if (doneCallback != null) {
                doneCallback.done(previouslySerializedChats, null);
            }
            input.close();
        } catch (FileNotFoundException | KryoException | BufferUnderflowException e) {
            if (doneCallback != null) {
                doneCallback.done(null, null);
            }
            e.printStackTrace();
            HolloutLogger.e(TAG, e.getMessage());
        }
    }

    public static boolean isTablet() {
        if (isTablet == null) {
            isTablet = ApplicationLoader.getInstance().getResources().getBoolean(R.bool.isTablet);
        }
        return isTablet;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean isLowMemory(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && activityManager.isLowRamDevice()) ||
                activityManager.getMemoryClass() <= 64;
    }

    public static boolean isMainThread() {
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

    public static float clamp(float value, float min, float max) {
        return Math.min(Math.max(value, min), max);
    }


    public static void bangSound(Context context, boolean reduceSound, int soundId) {
        MediaPlayer mediaPlayer = MediaPlayer.create(context, soundId);
        if (reduceSound) {
            mediaPlayer.setVolume(0.2f, 0.2f);
        } else {
            mediaPlayer.setVolume(0.5f, 0.5f);
        }
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

    public static void checkDisplaySize(Context context, Configuration newConfiguration) {
        try {
            density = context.getResources().getDisplayMetrics().density;
            Configuration configuration = newConfiguration;
            if (configuration == null) {
                configuration = context.getResources().getConfiguration();
            }
            usingHardwareInput = configuration.keyboard != Configuration.KEYBOARD_NOKEYS && configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO;
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

    public static int dp(float value) {
        if (value == 0) {
            return 0;
        }
        return (int) Math.ceil(density * value);
    }

    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();

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
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable() && mNetworkInfo.isConnected();
            }
        }
        return false;
    }

    @SuppressWarnings("ConstantConditions")
    public static void sendChatState(String chatState, String recipientId) {
        ParseObject signedInUserObject = AuthUtil.getCurrentUser();
        JSONObject existingChatStates = signedInUserObject.getJSONObject(AppConstants.APP_USER_CHAT_STATES);
        JSONObject chatStates = existingChatStates != null ? existingChatStates : new JSONObject();
        try {
            chatStates.put(recipientId, chatState);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        signedInUserObject.put(AppConstants.APP_USER_CHAT_STATES, chatStates);
        AuthUtil.updateCurrentLocalUser(signedInUserObject, new DoneCallback<Boolean>() {
            @Override
            public void done(Boolean result, Exception e) {

            }
        });
    }

    public static String convertAdditionalPhotosToString(List<Object> additionalPhotosOfUser) {
        Type listType = new TypeToken<List<String>>() {
        }.getType();
        return new Gson().toJson(additionalPhotosOfUser, listType);
    }

    public static Gson getGson(){
        return new Gson();
    }

    public static List<String> extractAdditionalPhotosFromString(String string) {
        if (StringUtils.isNotEmpty(string)) {
            Type listType = new TypeToken<List<String>>() {
            }.getType();
            return new Gson().fromJson(string, listType);
        } else {
            return null;
        }
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
    private static int MAX_LENGTH = 4;

    public static String format(double number) {
        String r = new DecimalFormat("##0E0").format(number);
        r = r.replaceAll("E[0-9]", suffix[Character.getNumericValue(r.charAt(r.length() - 1)) / 3]);
        while (r.length() > MAX_LENGTH || r.matches("[0-9]+\\.[a-z]")) {
            r = r.substring(0, r.length() - 2) + r.substring(r.length() - 1);
        }
        return r;
    }

    public static void updateCurrentParseInstallation(List<String> newChannelProps, List<String> removableChannelProps) {
        ParseInstallation parseInstallation = ParseInstallation.getCurrentInstallation();
        parseInstallation.put(AppConstants.REAL_OBJECT_ID, AuthUtil.getCurrentUser().getString(AppConstants.REAL_OBJECT_ID));
        List<String> existingChannels = parseInstallation.getList("channels");
        checkAndUpdateChannels(newChannelProps, removableChannelProps, parseInstallation, existingChannels);
        parseInstallation.saveInBackground();
    }

    private static void checkAndUpdateChannels(List<String> newChannelProps, List<String> removableChannelProps,
                                               ParseInstallation parseInstallation,
                                               List<String> existingChannels) {
        if (existingChannels != null) {
            if (removableChannelProps != null) {
                for (String removableChannelItem : removableChannelProps) {
                    if (existingChannels.contains(removableChannelItem)) {
                        existingChannels.remove(removableChannelItem);
                    }
                }
            }
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

    public static String getHashedString(String plainString) {
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

    public static String getLanguage(Context context) {
        return HolloutPreferences.getLanguage(LANGUAGE_PREF, "zz");
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

    public static long getVideoDuration(Uri fileUri) {
        long durationOfFile = 0;
        Cursor returnCursor = null;
        try {
            returnCursor = ApplicationLoader.getInstance().getContentResolver().query(fileUri, null, null, null, null);
            if (returnCursor != null && returnCursor.moveToFirst()) {
                int fileDuration = returnCursor.getColumnIndex(MediaStore.Video.Media.DURATION);
                durationOfFile = returnCursor.getLong(fileDuration);
            }
        } finally {
            if (returnCursor != null) {
                try {
                    returnCursor.close();
                } catch (Exception e) {
                    HolloutLogger.e(TAG, e.getMessage());
                }
            }
        }
        return durationOfFile;
    }

    public static boolean isFileCode(String fileMime) {
        return StringUtils.endsWithAny(fileMime, "java", "cpp", "c", "c++", "fortran", "cobol", "jar", "asm", "py", "php", "js", "scala", "html", "css", "xml", "xhtml");
    }

    public static boolean isValidDocument(String fileMime) {
        return !StringUtils.startsWithAny(fileMime, "image", "photo", "audio", "video");
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


    public static boolean hasLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean hasMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static File getFilePath(String fileName, Context context, String contentType) {
        return getFilePath(fileName, context, contentType, false);
    }

    private static final String IMAGE_DIR = "image";
    private static final String HOLLOUT_IMAGES_FOLDER = "/image";
    private static final String HOLLOUT_VIDEOS_FOLDER = "/video";
    private static final String HOLLOUT_CONTACT_FOLDER = "/contact";
    private static final String HOLLOUT_OTHER_FILES_FOLDER = "/other";
    private static final String HOLLOUT_THUMBNAIL_SUFFIX = "/.Thumbnail";
    private static final String MAIN_FOLDER_META_DATA = "Hollout";

    private static File getFilePath(String fileName, Context context, String contentType, boolean isThumbnail) {
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

    private static String getMetaDataValue(Context context, String metaDataName) {
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            if (ai.metaData != null) {
                return ai.metaData.getString(metaDataName);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public static String getGifUrl(JSONObject gifObject) {
        JSONObject originalGifProps = gifObject
                .optJSONObject("images")
                .optJSONObject("original");
        return originalGifProps.optString("url");
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

    public static String getQualifier(String word) {
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

    public static String getThumbnailImagePath(String thumbRemoteUrl) {
        String thumbImageName= thumbRemoteUrl.substring(thumbRemoteUrl.lastIndexOf("/") + 1, thumbRemoteUrl.length());
        String path = PathUtil.getInstance().getImagePath()+"/"+ "th"+thumbImageName;
        EMLog.d("msg", "thum image path:" + path);
        return path;
    }

}
