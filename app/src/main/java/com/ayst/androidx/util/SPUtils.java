package com.ayst.androidx.util;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SPUtils {
    private static final String SP = "config";

    public static final String KEY_4G_KEEP_LIVE = "4g_keep_live";
    public static final String KEY_LOG2FILE = "log2file";
    public static final String KEY_KEY_INTERCEPT = "key_intercept";

    private static SPUtils instance;
    private static SharedPreferences mSp = null;

    private SPUtils(Context context) {
        mSp = context.getSharedPreferences(SP, Context.MODE_PRIVATE);
    }

    public static SPUtils get(Context context) {
        if (instance == null)
            instance = new SPUtils(context);
        return instance;
    }

    /**
     * Save data
     *
     * @param key preference key
     * @param value preference value
     */
    public void saveData(String key, String value) {
        Editor editor = mSp.edit();
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * Save data
     *
     * @param key preference key
     * @param value preference value
     */
    public void saveData(String key, boolean value) {
        Editor editor = mSp.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    /**
     * Save data
     *
     * @param key preference key
     * @param value preference value
     */
    public void saveData(String key, int value) {
        Editor editor = mSp.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    /**
     * Save data
     *
     * @param key preference key
     * @param value preference value
     */
    public void saveData(String key, float value) {
        Editor editor = mSp.edit();
        editor.putFloat(key, value);
        editor.apply();
    }

    /**
     * Get data
     *
     * @param key preference key
     * @param defValue default value
     * @return value
     */
    public String getData(String key, String defValue) {
        return mSp.getString(key, defValue);
    }

    /**
     * Get data
     *
     * @param key preference key
     * @param defValue default value
     * @return value
     */
    public boolean getData(String key, boolean defValue) {
        return mSp.getBoolean(key, defValue);
    }

    /**
     * Get data
     *
     * @param key preference key
     * @param defValue default value
     * @return value
     */
    public int getData(String key, int defValue) {
        return mSp.getInt(key, defValue);
    }

    /**
     * Get data
     *
     * @param key preference key
     * @param defValue default value
     * @return value
     */
    public float getData(String key, float defValue) {
        return mSp.getFloat(key, defValue);
    }
}
