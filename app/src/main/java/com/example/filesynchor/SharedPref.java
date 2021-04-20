package com.example.filesynchor;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

public class SharedPref
{
    private static SharedPreferences mSharedPref;
    public static final String KEY_LAST_SYNC_TIME = "KEY_TIME";
    public static final String KEY_LAST_SYNC_NO_OF_FILES = "KEY_NO_OF_FILES";
    public static final String KEY_LAST_SYNC_SKIPPED_FILES = "KEY_SKIPPED_FILES";
    public static final String KEY_LAST_SYNC_STATUS = "KEY_SYNC_STATUS";
    public static final String KEY_LAST_SYNC_DATA_AMOUNT = "KEY_DATA_AMOUNT";
    public static final String KEY_LAST_SYNC_FILE_PATHS = "KEY_FILE_PATHS";
    public static final String KEY_LAST_SYNC_DURATION = "KEY_DURATION";
    public static final String KEY_LAST_SYNC_SPEED = "KEY_SPEED";
    public static final String KEY_DESTINATION_FOLDER = "KEY_DESTINATION_FOLDER";
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
