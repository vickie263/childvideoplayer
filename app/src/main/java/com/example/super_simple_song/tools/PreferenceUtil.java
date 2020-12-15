package com.example.super_simple_song.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import static android.content.Context.MODE_PRIVATE;


public class PreferenceUtil {
    private static final String TAG = PreferenceUtil.class.getSimpleName();

    public static String getString(@Nullable Context context, @NonNull String fileName, @NonNull String key, String defValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(fileName,MODE_PRIVATE);
        return sharedPreferences.getString(key,defValue);
    }

    public static boolean getBoolean(@Nullable Context context, @NonNull String fileName, @NonNull String key, boolean defValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(fileName,MODE_PRIVATE);
        return sharedPreferences.getBoolean(key,defValue);
    }


    public static float getFloat(@Nullable Context context, @NonNull String fileName, @NonNull String key, float defValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(fileName,MODE_PRIVATE);
        return sharedPreferences.getFloat(key,defValue);
    }

    public static int getInt(@Nullable Context context, @NonNull String fileName, @NonNull String key, int defValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(fileName,MODE_PRIVATE);
        return sharedPreferences.getInt(key,defValue);
    }

    public static long getLong(@Nullable Context context, @NonNull String fileName, @NonNull String key, long defValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(fileName,MODE_PRIVATE);
        return sharedPreferences.getLong(key,defValue);
    }

    public static void putString(@Nullable Context context, @NonNull String fileName, @NonNull String key, @NonNull String value) {
        if (TextUtils.isEmpty(value) || TextUtils.isEmpty(key)) return;
        SharedPreferences sharedPreferences = context.getSharedPreferences(fileName,MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString(key,value);
        editor.commit();
    }

    public static void putBoolean(@Nullable Context context, @NonNull String fileName, @NonNull String key, boolean value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(fileName,MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putBoolean(key,value);
        editor.commit();
    }

    public static void putFloat(@Nullable Context context, @NonNull String fileName, @NonNull String key, float value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(fileName,MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putFloat(key,value);
        editor.commit();
    }

    public static void putLong(@Nullable Context context, @NonNull String fileName, @NonNull String key, long value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(fileName,MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putLong(key,value);
        editor.commit();
    }

    public static void putInt(@Nullable Context context, @NonNull String fileName, @NonNull String key, int value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(fileName,MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putInt(key,value);
        editor.commit();
    }

    public static void removeDataByKey(@Nullable Context context, @NonNull String fileName, @NonNull String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(fileName,MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.remove(key);
        editor.commit();
    }

}
