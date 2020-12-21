package com.example.super_simple_song;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.super_simple_song.Play.PlayActivity;
import com.example.super_simple_song.app.SongsApp;
import com.example.super_simple_song.database.Song;
import com.example.super_simple_song.database.SongDataBaseHelper;
import com.example.super_simple_song.tools.FileHelper;

import java.util.ArrayList;
import java.util.List;

public class MainPagePresenterImp implements SongsContactor.IPresenter {
    public static final String FOLDERNAME = "/sss";
    private SongsContactor.IView mainpageView;
    private List<Song> mAllSongs = null;

    @Override
    public void bindView(SongsContactor.IView view) {
        mainpageView = view;
    }

    @Override
    public void loadSongs() {
        loadFiles();
        if(null != mAllSongs)
            mainpageView.showSongsList(mAllSongs);
    }

    @Override
    public void updateSongs() {
        mAllSongs = SongDataBaseHelper.getInstance().songDataBase.getSongDao().getAllSongsOrderbyPlaynumberDesc();
        mainpageView.updateSongsList(mAllSongs);
    }

    @Override
    public void OnLikeSong(int songid) {
        mAllSongs = SongDataBaseHelper.getInstance().songDataBase.getSongDao().getAllSongsOrderbyPlaynumberDesc();
        for(Song song : mAllSongs)
        {
            if(song.getId() == songid)
            {
                song.setIs_like(!song.getIs_like());
                SongDataBaseHelper.getInstance().songDataBase.getSongDao().update(song);
            }
        }
    }

    @Override
    public void OnDeleteSong(int songid) {

    }

    @Override
    public boolean checkTimer() {
        boolean isExpired = TimeModel.getInstance().checkTimeAtBegin(SongsApp.getAppContext());
        if(isExpired)
        {
            if(TimeModel.getInstance().bHasInterval(SongsApp.getAppContext()))
            {
                Log.d("mediahelp","beginTimer WaitingActivity");
                TimeModel.getInstance().beginTimer(SongsApp.getAppContext(),WaitingActivity.class);
            }
        }
        return isExpired;
    }

    private void loadFiles()
    {
        List<String> fileslist = new ArrayList<>();
        int result = FileHelper.getFileList(FOLDERNAME, ".mp4", fileslist);
        if(FileHelper.CANNOT_FIND_FILE == result)
        {
            Toast.makeText(SongsApp.getAppContext(), SongsApp.getAppContext().getString(R.string.toast_cannot_find_file),
                    Toast.LENGTH_LONG).show();
        }
        else if(FileHelper.PERMISSION_DENIED == result)
        {
            Toast.makeText(SongsApp.getAppContext(), SongsApp.getAppContext().getString(R.string.toast_permission_denied),
                    Toast.LENGTH_LONG).show();
            mainpageView.requirePermissions();
        }
        else {
            for(int i = 0; i < fileslist.size(); i++)
            {
                Song song = new Song();
                song.setName(fileslist.get(i));
                song.setDuration("0s");
                List<Song> songs = SongDataBaseHelper.getInstance().songDataBase.getSongDao().getSongByName(song.getName());
                if(songs.size() == 0)
                {
                    SongDataBaseHelper.getInstance().songDataBase.getSongDao().insertSong(song);
                }
            }
            mAllSongs = SongDataBaseHelper.getInstance().songDataBase.getSongDao().getAllSongsOrderbyPlaynumberDesc();
        }
    }


}
