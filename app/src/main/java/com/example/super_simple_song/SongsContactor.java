package com.example.super_simple_song;

import com.example.super_simple_song.database.Song;

import java.util.List;

public class SongsContactor {
    interface IView{
        void requirePermissions();
        void showSongsList(List<Song> songs);
        void updateSongsList(List<Song> songs, int... position);
    }

    interface IPresenter{
        void bindView(IView view);
        void loadSongs();
        void updateSongs();
        void OnLikeSong(int songid);//仅显示like按钮的页面使用
        void OnDeleteSong(int songid);//仅显示delete按钮的页面使用
        boolean checkTimer();
    }

}
