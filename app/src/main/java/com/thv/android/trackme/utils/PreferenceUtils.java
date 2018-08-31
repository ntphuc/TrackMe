package com.thv.android.trackme.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.thv.android.trackme.BasicApp;
import com.thv.android.trackme.R;

public class PreferenceUtils {
    private static final String TAG = "PreferenceUtils" ;

    /**
     * Initialize location recording settings
     */
    public static boolean isRecordEnable(Context context) {
        Log.v(TAG, "readSettings()");

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);


        return sharedPref.getBoolean(context.getString(R.string.preference_recording_enabled), true);

    }

    /**
     * Save location recording settings
     */
    @SuppressLint("ApplySharedPref")
    public static void putValueRecording(Context context, boolean isRecording) {
        Log.v(TAG, "writeSettings()");

        // remember that we are recording in case of being killed and restarted by Android
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(context.getString(R.string.preference_recording_enabled), isRecording);
        editor.commit();
    }
}
