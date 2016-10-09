package com.robj.notificationhelperlibrary;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * Created by jj on 06/10/16.
 */

public class NotificationListenerUtils {

    private final static String LISTENER_SERVICE_ENABLED = "LISTENER_SERVICE_ENABLED";
    private static final String TAG = "NotificationListener";

    //TODO: Multi process prefs was depreciated as of 23, no longer works

    public static void setListenerEnabled(Context context, boolean listenerEnabled) {
        SharedPreferences.Editor editor = context.getSharedPreferences(TAG, Context.MODE_MULTI_PROCESS).edit();
        editor.putBoolean(LISTENER_SERVICE_ENABLED, listenerEnabled);
        editor.commit();
    }

    public static boolean isListenerEnabled(Context context) {
        SharedPreferences sp = context.getSharedPreferences(TAG, Context.MODE_MULTI_PROCESS);
        return sp.getBoolean(LISTENER_SERVICE_ENABLED, false);
    }

    public static void launchNotificationAccessSettings(Activity activity) {
        Intent i = new Intent(AppUtils.isJellyBeanMR2()
                ? "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"
                : android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
        activity.startActivityForResult(i, 0);
    }

}
