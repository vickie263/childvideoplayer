package com.example.super_simple_song;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Constraints;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.super_simple_song.Play.AudioPlayActivity;
import com.example.super_simple_song.Play.PlayInfoBinder;
import com.example.super_simple_song.Play.PlayService;
import com.example.super_simple_song.database.Song;
import com.example.super_simple_song.view.AudioPlayBar;

import java.io.File;

public abstract class BaseActivity extends AppCompatActivity implements PlayService.IActionCallback,
AudioPlayBar.onPlayStateChangeListener{
    protected PlayInfoBinder mBinder;
    private ServiceConnection mConn;
    private Intent mIntentforservice;

    protected boolean mHasPlayBar = false;

    protected AudioPlayBar mPlayBar;

    protected abstract void onServiceConnected();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPause() {
        goBack();
        super.onPause();
    }

    protected void startService(int songid, int fromwhere)
    {
        mIntentforservice = new Intent(BaseActivity.this,
                PlayService.class);
        mIntentforservice.putExtra(SongsConstants.PLAYNEW, true);
        mIntentforservice.putExtra(SongsConstants.SONG_ID, songid);
        mIntentforservice.putExtra(SongsConstants.FROM_WHERE,fromwhere);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            //ps 8.0 后一定要通过这种方式启动
//            startForegroundService(mIntentforservice);
//        } else {
        startService(mIntentforservice);
//        }
    }

    protected void startService()
    {
        mIntentforservice = new Intent(BaseActivity.this,
                PlayService.class);
        mIntentforservice.putExtra(SongsConstants.PLAYNEW, false);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            //ps 8.0 后一定要通过这种方式启动
//            startForegroundService(mIntentforservice);
//        } else {
        startService(mIntentforservice);
//        }
    }

    protected void bindService()
    {
        mConn = new MyServiceConn();
        bindService(mIntentforservice, mConn, BIND_AUTO_CREATE);
    }

    protected void goBack(){
        if(null != mBinder)
        {
            Log.d("playservice","goBack");
            if(mBinder.getService().isPlaying())
                unbindService(mConn);
            else
                stopService(mIntentforservice);
        }
    }

    protected void stopService()
    {
        if(null != mIntentforservice && null != mConn)
        {
            Log.d("playservice","stopService");
            mBinder.getService().pausePlay();
            unbindService(mConn);
            removePlayBar();
            stopService(mIntentforservice);
            mIntentforservice = null;
            mBinder = null;
        }
    }

    private void addPlayBar()
    {
        if(null == mBinder)
            return;
        Song song = mBinder.getService().getCurrentSong();
        mPlayBar = new AudioPlayBar(BaseActivity.this);
        RelativeLayout rootview = (RelativeLayout)((ViewGroup)findViewById(android.R.id.content)).getChildAt(0);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        rootview.addView(mPlayBar,params);
        mPlayBar.setOnPlayStateChangeListener(this);
        mPlayBar.setOnRootViewClickListener(view -> {
            Intent intent=new Intent(BaseActivity.this, AudioPlayActivity.class);
            intent.putExtra(SongsConstants.SONG_ID, song.getId());
            intent.putExtra(SongsConstants.FROM_WHERE, SongsConstants.FROM_MAIN);
            intent.putExtra(SongsConstants.PLAYNEW,false);
            startActivity(intent);
        });
        updatePlayBarInfo(song);
    }

    private void removePlayBar()
    {
        RelativeLayout rootview = (RelativeLayout)((ViewGroup)findViewById(android.R.id.content)).getChildAt(0);
        rootview.removeView(mPlayBar);
    }

    private void updatePlayBarInfo(Song song)
    {
        File videoFile = new File(song.getName());
        mPlayBar.setImageUri(Uri.fromFile(videoFile));
        int strindex = song.getName().lastIndexOf("/");
        mPlayBar.setText(song.getName().substring(strindex+1));
        mPlayBar.setPlayed(true);
    }

    private void playNextVideo()
    {
        if(null == mBinder)
            return;
        mBinder.getService().playNextVideo();
    }

    @Override
    public void onSongPlay() {
        if(null != mBinder)
            mBinder.getService().resumePlay();
    }

    @Override
    public void onSongPause() {
        if(null != mBinder)
            mBinder.getService().pausePlay();
    }

    @Override
    public void onMediaPlayerStart(int position, int totaltime, Song song) {
        if(null != song && mHasPlayBar)
            updatePlayBarInfo(song);
    }

    @Override
    public void onMediaPlayerPause() {

    }

    @Override
    public void onMediaPlayerDestroy() {

    }

    @Override
    public void onMediaPlayerComplation() {
        playNextVideo();
    }

    private class MyServiceConn implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mBinder = (PlayInfoBinder) iBinder;
            mBinder.getService().setCallback(BaseActivity.this);
            if(mHasPlayBar && mBinder.getService().isPlaying())
            {
                addPlayBar();
            }
            BaseActivity.this.onServiceConnected();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBinder.getService().setCallback(null);
            Log.d("playservice","onServiceDisconnected");
            if(mHasPlayBar)
                removePlayBar();
            mBinder = null;
            unbindService(mConn);
        }
    }
}