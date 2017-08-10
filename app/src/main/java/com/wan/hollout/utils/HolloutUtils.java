package com.wan.hollout.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.wan.hollout.R;
import com.wan.hollout.callbacks.DoneCallback;
import com.wan.hollout.components.ApplicationLoader;
import com.wan.hollout.models.HolloutObject;

import org.apache.commons.lang3.StringUtils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

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

}
