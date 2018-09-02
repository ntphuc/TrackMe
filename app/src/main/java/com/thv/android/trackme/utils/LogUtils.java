package com.thv.android.trackme.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class LogUtils {
    public final static boolean IS_SHOW_LOG =true;
    public final static String TAG_ROUTE_CREATE_SERVICE = "create_service";
    public static void i(String logTag, String mes) {
        if (IS_SHOW_LOG)
            Log.i(logTag, mes);
    }

    public static void showToast(Context applicationContext, String s) {
        Toast.makeText(applicationContext,s,Toast.LENGTH_LONG).show();
    }
}
