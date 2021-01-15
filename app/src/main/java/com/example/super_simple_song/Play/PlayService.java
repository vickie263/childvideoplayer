package com.example.super_simple_song.Play;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.myapplication.R;
import com.example.super_simple_song.SongsConstants;
import com.example.super_simple_song.database.Song;

public class PlayService extends Service implements MediaPlayerHelper.OnMediaPlayerActionListener{
    public final static int RESULT_SUCCESS = 0;
    public final static int RESULT_NO_SONGS = -1;
    public final static int RESULT_UNKNOW = -2;

    private MediaPlayerHelper mediaPlayerHelper;
    private PlayDataHelper mPlayDataHelper;
    private IActionCallback mCallback;

    private boolean hasUpdateNumber = false;

    public PlayService() {
    }

    public void setCallback(IActionCallback callback) {
        mCallback = callback;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return new PlayInfoBinder(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayerHelper = new MediaPlayerHelper();
        mediaPlayerHelper.setOnMediaPlayerActionListener(PlayService.this);
        mPlayDataHelper = new PlayDataHelper();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("playservice","onStartCommand");
        if(intent == null)
            return START_STICKY;
        boolean isplaynew = intent.getBooleanExtra(SongsConstants.PLAYNEW,true);
        if(!isplaynew)
            return START_STICKY;

        mediaPlayerHelper.destory();
        int result = mPlayDataHelper.initSong(PlayService.this,intent);
        if(result == PlayDataHelper.RESULT_NO_SONGS)
        {
            Toast.makeText(this,getString(R.string.playpage_novideo),Toast.LENGTH_LONG).show();
            return START_STICKY;
        }
        mediaPlayerHelper.setVideoPath(mPlayDataHelper.getCurrentVideoPath());
        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        if(mediaPlayerHelper != null)
        {
            mediaPlayerHelper.destory();
            mediaPlayerHelper = null;
        }
        Log.d("playservice","onDestroy");
        super.onDestroy();
    }

    public boolean play(@Nullable Context context)
    {
        int result = mediaPlayerHelper.initMediaPlayer();
        if(mediaPlayerHelper.RESULT_IOEXCEPTION == result)
        {
            if(PlayDataHelper.RESULT_NO_SONGS == mPlayDataHelper.dealwithErrorVideo(context))
                return false;
            result = playNextVideo();
            if(result != PlayDataHelper.RESULT_SUCCESS)
                return false;
        }
        else
        {
            result = mediaPlayerHelper.play();
            if(result != mediaPlayerHelper.RESULT_SUCCESS)
                return false;
        }
        return true;
    }

    public int playNextVideo()
    {
        mPlayDataHelper.updateNextVideoIndex();
        int result = mediaPlayerHelper.playNextItem(mPlayDataHelper.getCurrentSong().getName());
        if(result != mediaPlayerHelper.RESULT_SUCCESS)
        {
            if(result == mediaPlayerHelper.RESULT_IOEXCEPTION)
            {
                if(PlayDataHelper.RESULT_NO_SONGS == mPlayDataHelper.dealwithErrorVideo(PlayService.this))
                    return RESULT_NO_SONGS;
                playNextVideo();
            }else
            {
                return RESULT_UNKNOW;
            }
        }
        else
        {
            hasUpdateNumber = false;
            return RESULT_SUCCESS;
        }
        return RESULT_UNKNOW;
    }

    public int playLastVideo()
    {
        mPlayDataHelper.updateLastVideoIndex();
        int result = mediaPlayerHelper.playNextItem(mPlayDataHelper.getCurrentSong().getName());
        if(result != mediaPlayerHelper.RESULT_SUCCESS)
        {
            if(result == mediaPlayerHelper.RESULT_IOEXCEPTION)
            {
                if(PlayDataHelper.RESULT_NO_SONGS == mPlayDataHelper.dealwithErrorVideo(PlayService.this))
                    return RESULT_NO_SONGS;
                playNextVideo();
            }else
            {
                return RESULT_UNKNOW;
            }
        }
        else
        {
            hasUpdateNumber = false;
            return RESULT_SUCCESS;
        }
        return RESULT_UNKNOW;
    }

    public void updateLikeData(boolean isLike)
    {
        mPlayDataHelper.updateLikeDataToDataBase(isLike);
    }

    public void updateSongPlayNumber()
    {
        if(hasUpdateNumber)
            return;
        if(mediaPlayerHelper.getCurrentPosition() > (mediaPlayerHelper.getTotalDuration() * 0.5))
        {
            mPlayDataHelper.addSongPlayNumber();
            hasUpdateNumber = true;
        }
    }

    public String getCurrentVideoTimeString()
    {
        return mediaPlayerHelper.getCurrentVideoTimeString();
    }

    public void seekTo(int grogress)
    {
        mediaPlayerHelper.seekTo(grogress);
    }

    public int getCurrentPosition()
    {
        return mediaPlayerHelper.getCurrentPosition();
    }

    public void resumePlay()
    {
        if(mediaPlayerHelper != null)
            mediaPlayerHelper.resume();
    }

    public void pausePlay()
    {
        if(mediaPlayerHelper != null)
            mediaPlayerHelper.pause();
    }

    public boolean isPlaying()
    {
        if(null == mediaPlayerHelper)
            return false;
        return mediaPlayerHelper.isPlaying();
    }

    public Song getCurrentSong()
    {
        return mPlayDataHelper.getCurrentSong();
    }

    public int getCurrentSongDuration()
    {
        return mediaPlayerHelper.getTotalDuration();
    }

    @Override
    public void onMediaPlayerStart(int position, int totaltime) {
        if(null != mCallback)
            mCallback.onMediaPlayerStart(position,totaltime,mPlayDataHelper.getCurrentSong());
    }

    @Override
    public void onMediaPlayerPause() {
        if(null != mCallback)
            mCallback.onMediaPlayerPause();
    }

    @Override
    public void onMediaPlayerDestroy() {
        if(null != mCallback)
            mCallback.onMediaPlayerDestroy();
    }

    @Override
    public void onMediaPlayerComplation() {
        updateSongPlayNumber();
        if(null != mCallback)
            mCallback.onMediaPlayerComplation();
    }

    public interface IActionCallback {
        void onMediaPlayerStart(int position, int totaltime, Song song);
        void onMediaPlayerPause();
        void onMediaPlayerDestroy();
        void onMediaPlayerComplation();
    }
}