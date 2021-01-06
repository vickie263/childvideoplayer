package com.example.super_simple_song.Play;

import android.os.Binder;

public class PlayInfoBinder extends Binder {
    private PlayService mService;

    public PlayService getService() {
        return this.mService;
    }

    public PlayInfoBinder(PlayService service) {
        this.mService = service;
    }
}