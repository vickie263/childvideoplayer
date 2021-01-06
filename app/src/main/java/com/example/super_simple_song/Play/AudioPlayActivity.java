package com.example.super_simple_song.Play;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.super_simple_song.BaseActivity;
import com.example.super_simple_song.SongsConstants;
import com.example.super_simple_song.database.Song;
import com.example.super_simple_song.view.AudioPlayBar;

import java.io.File;

public class AudioPlayActivity extends BaseActivity implements View.OnClickListener,
        SeekBar.OnSeekBarChangeListener,TimerHelper.ITimerDataCallback{

    private TextView mVideotimeTv;
    private SeekBar mSeekBar;
    private TextView mLastbtn;
    private TextView mNextbtn;
    private ImageView mLikekbtn;
    private ImageView mPausebtn;
    private ImageView mImage;
    private TextView mName;

    private boolean isSeekbarChaning;//互斥变量，防止进度条和定时器冲突。
    private boolean mIsPlayNew;
    private TimerHelper mTimerHelper;

    @Override
    protected void onServiceConnected() {
        play();
        updateSongData(mBinder.getService().getCurrentSong());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_play);
        mTimerHelper = new TimerHelper(AudioPlayActivity.this);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        if(null == intent)
            return;
        mHasPlayBar = false;
        mIsPlayNew = intent.getBooleanExtra(SongsConstants.PLAYNEW,true);
        if(mIsPlayNew)
            startService(intent.getIntExtra(SongsConstants.SONG_ID,-1),
                intent.getIntExtra(SongsConstants.FROM_WHERE,-1));
        else
            startService();
        bindService();
    }

    private void initView()
    {
        mImage = (ImageView)findViewById(R.id.audio_image);
        mName = (TextView)findViewById(R.id.audio_showname);

        mPausebtn = (ImageView)findViewById(R.id.pausebtn);
        mPausebtn.setOnClickListener(this);
        mPausebtn.setSelected(false);

        ImageView backbtn = (ImageView)findViewById(R.id.backbtn);
        backbtn.setOnClickListener(this);
        mLikekbtn = (ImageView)findViewById(R.id.likebtn);
        mLikekbtn.setOnClickListener(this);
        mSeekBar = (SeekBar)findViewById(R.id.seekbar);
        mSeekBar.setOnSeekBarChangeListener(this);

        mVideotimeTv = (TextView)findViewById(R.id.videotime_tv);

        mLastbtn = (TextView)findViewById(R.id.lastbtn);
        mLastbtn.setOnClickListener(this);
        mLastbtn.setClickable(true);
        mNextbtn = (TextView)findViewById(R.id.nextbtn);
        mNextbtn.setOnClickListener(this);
        mNextbtn.setClickable(true);
    }

    private void updateSongData(Song song)
    {
        if(null == song)
            return;
        int strindex = song.getName().lastIndexOf("/");
        mName.setText(song.getName().substring(strindex+1));

        File videoFile = new File(song.getName());
        Glide.with(AudioPlayActivity.this).load(Uri.fromFile(videoFile)).centerCrop().into(mImage);
    }

    private void play()
    {
        if(null == mBinder)
            return;

        if(!mIsPlayNew)
        {
            mTimerHelper.startTimer();
            mSeekBar.setMax(mBinder.getService().getCurrentSongDuration());
            return;
        }
        boolean result = mBinder.getService().play(AudioPlayActivity.this);
        if(!result)
            goBack();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.pausebtn:
                if(view.isSelected())
                {//暂停转播放
                    if(null != mBinder)
                        mBinder.getService().resumePlay();
                    view.setSelected(false);
                }else
                {
                    if(null != mBinder)
                        mBinder.getService().pausePlay();
                    view.setSelected(true);
                }
                break;
            case R.id.lastbtn:
                if(view.isClickable()== false)
                    return;
                mLastbtn.setClickable(false);
                mNextbtn.setClickable(false);
                playLastVideo();
                break;
            case R.id.nextbtn:
                if(view.isClickable()== false)
                    return;
                mLastbtn.setClickable(false);
                mNextbtn.setClickable(false);
                playNextVideo();
                break;
            case R.id.backbtn:
                goBack();
                break;
            case R.id.likebtn:
                mLikekbtn.setSelected(!mLikekbtn.isSelected());
                if(null != mBinder)
                    mBinder.getService().updateLikeData(mLikekbtn.isSelected());
                break;
        }
    }

    @Override
    protected void goBack(){
        finish();
    }

    private void playNextVideo()
    {
        if(null == mBinder)
            return;
        int result = mBinder.getService().playNextVideo();
        if(result == PlayService.RESULT_UNKNOW)
        {
            mLastbtn.setClickable(true);
            mNextbtn.setClickable(true);
        }
    }

    private void playLastVideo()
    {
        if(null == mBinder)
            return;
        int result = mBinder.getService().playLastVideo();
        if(result == PlayService.RESULT_UNKNOW)
        {
            mLastbtn.setClickable(true);
            mNextbtn.setClickable(true);
        }
    }

    private void updateTextView()
    {
        if(null != mBinder)
            mSeekBar.setProgress(mBinder.getService().getCurrentPosition());
    }

    @Override
    protected void onDestroy() {
        mTimerHelper.destroyTimer();
        super.onDestroy();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if(null != mBinder)
            mVideotimeTv.setText(mBinder.getService().getCurrentVideoTimeString());
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        isSeekbarChaning = true;
        mTimerHelper.stopTimer();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        isSeekbarChaning = false;
        if(null != mBinder)
        {
            mBinder.getService().seekTo(seekBar.getProgress());
            mVideotimeTv.setText(mBinder.getService().getCurrentVideoTimeString());
        }
    }

    @Override
    public void onMediaPlayerStart(int position, int totaltime, Song song) {
        mSeekBar.setMax(totaltime);
        mTimerHelper.startTimer();
        mLastbtn.setClickable(true);
        mNextbtn.setClickable(true);
        mPausebtn.setSelected(false);
        if(null != song)
        {
            mLikekbtn.setSelected(song.getIs_like());
            updateSongData(song);
        }
    }

    @Override
    public void onMediaPlayerPause() {
        mTimerHelper.stopTimer();
    }

    @Override
    public void onMediaPlayerDestroy() {
        mTimerHelper.stopTimer();
    }

    @Override
    public void onMediaPlayerComplation() {
        super.onMediaPlayerComplation();
    }

    @Override
    public void onTimeUp() {
        if(!isSeekbarChaning){
            updateTextView();
            mBinder.getService().updateSongPlayNumber();
        }
    }
}