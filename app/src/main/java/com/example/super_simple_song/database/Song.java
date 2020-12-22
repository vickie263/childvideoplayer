package com.example.super_simple_song.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Song {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "file_name")
    @NonNull
    private String name;
    @ColumnInfo(name = "duration")
    @NonNull
    private String duration;
    @ColumnInfo(name = "is_like")
    private boolean is_like;
    @ColumnInfo(name = "play_number")
    private int number;
    @ColumnInfo(name = "is_valid")
    private boolean is_valid;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public boolean getIs_like() {
        return is_like;
    }

    public void setIs_like(boolean islike) {
        this.is_like = islike;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public boolean getIs_valid() {
        return is_valid;
    }

    public void setIs_valid(boolean isvalid) {
        this.is_valid = isvalid;
    }

}
