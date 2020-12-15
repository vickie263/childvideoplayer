package com.example.super_simple_song.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Song.class}, version = 1)
public abstract class SongDataBase extends RoomDatabase {
    public abstract SongDao getSongDao();
}
