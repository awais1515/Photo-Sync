package com.example.filesynchor;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

public class SharedPref
{
    private static SharedPreferences mSharedPref;
    public static final String KEY_LAST_SYNC_TIME = "TIME";
    public static final String KEY_LAST_SYNC_NO_OF_FILES = "NO_OF_FILES";
    public static final String KEY_LAST_SYNC_SKIPPED_FILES = "SKIPPED_FILES";
    public static final String KEY_LAST_SYNC_STATUS = "SYNC_STATUS";
    public static final String KEY_LAST_SYNC_DATA_AMOUNT = "DATA_AMOUNT";
    public static final String KEY_LAST_SYNC_FILE_PATHS = "FILE_PATHS";
    public static final String KEY_LAST_SYNC_DURATION = "DURATION";
    public static final String KEY_LAST_SYNC_SPEED = "SPEED";
    public static final String KEY_DESTINATION_FOLDER = "DESTINATION_FOLDER";
    public static final String DEFAULT_DESTINATION_FOLDER = Environment.getExternalStorageDirectory().getAbsolutePath()+"/AutoSync/";

    private SharedPref()
    {
    }
    public static void init(Context context)
    {
        if(mSharedPref == null)
            mSharedPref = context.getSharedPreferences(context.getPackageName(), Activity.MODE_PRIVATE);
    }

    public static String read(String key, String defValue) {
        return mSharedPref.getString(key, defValue);
    }

    public static void write(String key, String value) {
        SharedPreferences.Editor prefsEditor = mSharedPref.edit();
        prefsEditor.putString(key, value);
        prefsEditor.commit();
    }

}
