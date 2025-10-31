package com.example.myapplication.util;

import android.util.Log;

public final class LogUtil {
    private LogUtil() {}

    private static boolean isDebug() {
        try {
            return com.example.myapplication.BuildConfig.DEBUG;
        } catch (Throwable t) {
            return true;
        }
    }

    public static void d(String tag, String msg) {
        if (isDebug()) Log.d(tag, msg);
    }

    public static void i(String tag, String msg) {
        Log.i(tag, msg);
    }

    public static void w(String tag, String msg) {
        Log.w(tag, msg);
    }

    public static void e(String tag, String msg) {
        Log.e(tag, msg);
    }

    public static void e(String tag, String msg, Throwable tr) {
        Log.e(tag, msg, tr);
    }
}


