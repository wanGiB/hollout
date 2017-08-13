package com.wan.hollout.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
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
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.wan.hollout.R;
import com.wan.hollout.callbacks.DoneCallback;
import com.wan.hollout.components.ApplicationLoader;
import com.wan.hollout.models.HolloutObject;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

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

    static {
        leftBaseline = isTablet() ? 80 : 72;
        checkDisplaySize(ApplicationLoader.getInstance(), null);
    }

    public static boolean isTablet() {
        if (isTablet == null) {
            isTablet = ApplicationLoader.getInstance().getResources().getBoolean(R.bool.isTablet);
        }
        return isTablet;
    }

    public static String stripDollar(String string) {
        return StringUtils.replace(string, "$", "USD");
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

        ParseUser signedInUser = ParseUser.getCurrentUser();

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


    public static Kryo getKryoInstance() {
        return new Kryo();
    }

    public static synchronized void serializeListContent(List<HolloutObject> tObjects, String serializableName) {
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
    public static synchronized void deserializeListContent(String fileName, DoneCallback<List<HolloutObject>> doneCallback) {
        Kryo kryo = getKryoInstance();
        try {
            Input input = new Input(ApplicationLoader.getInstance().openFileInput(fileName));
            ArrayList<HolloutObject> previouslySerializedChats = kryo.readObject(input, ArrayList.class);
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

    public static void sendChatState(String chatState, String recipientId) {
        ParseUser signedInUserObject = ParseUser.getCurrentUser();
        JSONObject existingChatStates = signedInUserObject.getJSONObject(AppConstants.APP_USER_CHAT_STATES);
        JSONObject chatStates = existingChatStates != null ? existingChatStates : new JSONObject();
        try {
            chatStates.put(recipientId, chatState);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        signedInUserObject.put(AppConstants.APP_USER_CHAT_STATES, chatStates);
        signedInUserObject.saveInBackground();
    }

    public static String convertAdditionalPhotosToString(List<Object> additionalPhotosOfUser) {
        Type listType = new TypeToken<List<String>>() {
        }.getType();
        return new Gson().toJson(additionalPhotosOfUser, listType);
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
        parseInstallation.put(AppConstants.APP_USER_ID,ParseUser.getCurrentUser().getObjectId());
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

}
