package com.example.super_simple_song.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.super_simple_song.app.SongsApp;

import java.io.File;
import java.util.Locale;

public class SongDataBaseHelper {

    private static SongDataBaseHelper holder;
    public SongDataBase songDataBase;


    public synchronized static SongDataBaseHelper getInstance() {
        if (holder == null) {
            holder = new SongDataBaseHelper();
        }
        return holder;
    }

    private SongDataBaseHelper() {
        switchDatabase(SongsApp.getAppContext());
    }

    public synchronized void switchDatabase(Context context) {
        if (songDataBase != null) {
            songDataBase.close();
        }
        File parentFile = new File(context.getExternalFilesDir(null), "/database/");
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }
        String fileName = new File(parentFile, "/songshome.db").getAbsolutePath();
        songDataBase = Room.databaseBuilder(context, SongDataBase.class, fileName)
                .allowMainThreadQueries().build();
    }
}
