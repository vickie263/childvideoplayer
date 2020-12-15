package com.example.super_simple_song.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface  SongDao {
    @Insert
    void insertSongs(Song... songs);//传入多个参数

    @Insert
    void insertSong(Song song);//传入多个参数

    @Update
    void update(Song song);

    @Delete
    void delete(Song song);

    @Query("SELECT * FROM Song WHERE id = :uid")
    List<Song> getSongById(int uid);

    @Query("SELECT * FROM Song WHERE file_name = :name")
    List<Song> getSongByName(String name);

    @Query("SELECT * FROM Song ORDER BY play_number desc")
    List<Song> getAllSongsOrderbyPlaynumberDesc();

    @Query("SELECT * FROM Song WHERE is_like = '1' ORDER BY play_number desc")
    List<Song> getLikeSongsOrderbyPlaynumberDesc();
}
