package com.neonex.nsok.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.neonex.nsok.common.Nsok;

/**
 * Created by yun on 2017-10-31.
 */

public class NsokPreferences {
    private static final String FCM_TOKEN = "FCM_TOKEN";
    private static final String TARGET_URL = "TARGET_URL";

    public static final String PREF_NAME = "nsok.pref";

    private static void setString(Context context, String key, String value) {
        assert context != null;
        assert key != null;

        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, value);
        editor.commit();
    }


    private static String getString(Context context, String key, String defValue) {
        assert context != null;
        assert key != null;

        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, 0);
        return pref.getString(key, defValue);
    }

    public static void setFcmToken(Context context, String fcmToken) {
        assert context != null;
        setString(context, FCM_TOKEN, fcmToken);
    }

    public static String getFcmToken(Context context) {
        assert context != null;
        return getString(context, FCM_TOKEN, "");
    }

    public static void setTargetUrl(Context context, String targetUrl) {
        assert context != null;
        setString(context, TARGET_URL, targetUrl);
    }

    public static String getTargetUrl(Context context) {
        assert context != null;
        return getString(context, TARGET_URL, Nsok.connWebUrl);
    }
}
