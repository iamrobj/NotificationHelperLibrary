package com.robj.notificationhelperlibrary;

import android.os.Build;

/**
 * Created by Rob J on 30/09/2016.
 */

public class AppUtils {

    public static boolean isJellyBeanMR2() {
        return Build.VERSION.SDK_INT >= 18;
    }

}
