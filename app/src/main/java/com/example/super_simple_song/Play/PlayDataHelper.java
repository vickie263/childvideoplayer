package com.example.super_simple_song.Play;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.myapplication.R;
import com.example.super_simple_song.SongsConstants;
import com.example.super_simple_song.database.Song;
import com.example.super_simple_song.database.SongDataBaseHelper;

import java.util.List;

public class PlayDataHelper {
    public final static int RESULT_SUCCESS = 0;
    public final static int RESULT_NO_SONGS = 1;
    private List<Song> mAllSongs = null;
    private int mVideoIndex = -1;
    private String mVideopath;

    public int initSong(Context context, Intent intent)
    {
        int id = intent.getIntExtra(SongsConstants.SONG_ID,-1);
        if(id < 0)
            id = 0;
        int fromwhere = intent.getIntExtra(SongsConstants.FROM_WHERE,-1);
        if(SongsConstants.FROM_MAIN == fromwhere)
            mAllSongs = SongDataBaseHelper.getInstance().songDataBase.getSongDao().getAllSongsOrderbyPlaynumberDesc();
        else if(SongsConstants.FROM_LIKE == fromwhere)
            mAllSongs = SongDataBaseHelper.getInstance().songDataBase.getSongDao().getLikeSongsOrderbyPlaynumberDesc();

        if(mAllSongs == null || mAllSongs.size() <= 0)
        {
            return RESULT_NO_SONGS;
        }
        mVideoIndex = -1;
        for(Song song: mAllSongs)
        {
            mVideoIndex++;
            if(song == null)
                continue;
            if(id == song.getId())
            {
                mVideopath = song.getName();
                break;
            }
        }
        return RESULT_SUCCESS;
    }

    public int dealwithErrorVideo(@Nullable Context context)
    {
        Toast.makeText(context,context.getString(R.string.error_playvideo),Toast.LENGTH_SHORT).show();
        Song song = mAllSongs.get(mVideoIndex);
        song.setIs_valid(false);
        mAllSongs.remove(mVideoIndex);
        if(mAllSongs.size() <= 0)
        {
            Toast.makeText(context,context.getString(R.string.playpage_novideo),Toast.LENGTH_SHORT).show();
            return RESULT_NO_SONGS;
        }
        if(mVideoIndex != 0)
            mVideoIndex--;
        else
            mVideoIndex = mAllSongs.size() - 1;
        SongDataBaseHelper.getInstance().songDataBase.getSongDao().update(song);
        return RESULT_SUCCESS;
    }

    public void updateLikeDataToDataBase(boolean islike)
    {
        mAllSongs.get(mVideoIndex).setIs_like(islike);
        SongDataBaseHelper.getInstance().songDataBase.getSongDao().update(mAllSongs.get(mVideoIndex));
    }

    public void updateNextVideoIndex()
    {
        int index = mVideoIndex + 1;
        if(index < 0)
            index = 0;
        if(index >= mAllSongs.size())
            index = 0;
        mVideoIndex = index;
    }

    public void updateLastVideoIndex()
    {
        int index = mVideoIndex - 1;
        if(index < 0)
            index = mAllSongs.size() - 1;
        if(index >= mAllSongs.size())
            index = mAllSongs.size() - 1;
        mVideoIndex = index;
    }

    public void addSongPlayNumber()
    {
        Song song = mAllSongs.get(mVideoIndex);
        song.setNumber(song.getNumber()+1);
        SongDataBaseHelper.getInstance().songDataBase.getSongDao().update(song);
    }

    public Song getCurrentSong()
    {
        if(null != mAllSongs && mAllSongs.size() > mVideoIndex)
            return mAllSongs.get(mVideoIndex);
        else
            return null;
    }

    public String getCurrentVideoPath()
    {
        return mVideopath;
    }
}
