package com.cxample.download_demo.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by yanqing on 2018/3/23.
 */

public class Utils {
    private static final String PREFERENCES_NAME = "system_setting";

    private static SharedPreferences getSystemSharedPreferences(Context context) {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    private static SharedPreferences.Editor getEditor(Context context) {
        return getSystemSharedPreferences(context).edit();
    }

    public static void saveAutoStartDownload(Context context, boolean autoStart) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putBoolean("auto_start_download", autoStart);
        editor.apply();
    }

    public static boolean getAutoStartDownload(Context context) {
        SharedPreferences preferences = getSystemSharedPreferences(context);
        return preferences.getBoolean("auto_start_download", false);
    }
}
