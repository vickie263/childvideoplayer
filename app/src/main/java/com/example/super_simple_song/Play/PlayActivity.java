package com.example.super_simple_song.Play;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.super_simple_song.RouterConstants;
import com.example.super_simple_song.TimeModel;
import com.example.super_simple_song.WaitingActivity;
import com.example.super_simple_song.database.Song;
import com.example.super_simple_song.database.SongDataBaseHelper;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class PlayActivity extends AppCompatActivity implements GLSurfaceView.Renderer,
        SurfaceTexture.OnFrameAvailableListener, View.OnClickListener,
        SeekBar.OnSeekBarChangeListener, MediaPlayerHelper.OnMediaPlayerActionListener{

    private static final int TOTAL_COUNT = 5;
    private static final int UPDATE_TEXTVIEW = 0;
    private List<Song> mAllSongs = null;
    private int mVideoIndex = -1;
    private boolean hasUpdateNumber = false;

    private GLSurfaceView surfaceView;
    private GLRender glRender;
    private SurfaceTexture mSurfaceTexture;
    private MediaPlayerHelper mediaPlayerHelper;
    private String mVideopath;
    private float[] mStMatrix = new float[16];

    private boolean mIsControlMode = false;
    private boolean mIsFinished = false;
    private RelativeLayout mControllayout;
    private View mClickview;
    private TextView mVideotimeTv;
    private SeekBar mSeekBar;
    private TextView mLastbtn;
    private TextView mNextbtn;
    private ImageView mLikekbtn;

    private Timer mTimer;//定时器
    private TimerTask mTimerTask;
    private boolean isSeekbarChaning;//互斥变量，防止进度条和定时器冲突。
    private int mCount = 0;//显示控制面板，在播放状态下，count秒后自动隐藏控制面板
    private Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_TEXTVIEW:
                    updateTextView();
//                    mSeekBar.setProgress(mediaPlayerHelper.getCurrentPosition());
                    updateSongPlayNumber();

                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        initView();
        initData();
    }

    private void initData()
    {
        glRender = new GLRender();
        Intent intent = getIntent();
        int id = intent.getIntExtra(RouterConstants.SONG_ID,-1);
        if(id < 0)
            id = 0;
        int fromwhere = intent.getIntExtra(RouterConstants.FROM_WHERE,-1);
        if(RouterConstants.FROM_MAIN == fromwhere)
            mAllSongs = SongDataBaseHelper.getInstance().songDataBase.getSongDao().getAllSongsOrderbyPlaynumberDesc();
        else if(RouterConstants.FROM_LIKE == fromwhere)
            mAllSongs = SongDataBaseHelper.getInstance().songDataBase.getSongDao().getLikeSongsOrderbyPlaynumberDesc();

        if(mAllSongs == null || mAllSongs.size() <= 0)
        {
            Toast.makeText(this,getString(R.string.playpage_novideo),Toast.LENGTH_LONG).show();
            return;
        }
        for(Song song: mAllSongs)
        {
            mVideoIndex++;
            if(song == null)
                continue;
            if(id == song.getId())
            {
                mVideopath = song.getName();
                mLikekbtn.setSelected(song.getIs_like());
                break;
            }
        }
        mediaPlayerHelper = new MediaPlayerHelper(mVideopath);
        mediaPlayerHelper.setOnMediaPlayerActionListener(PlayActivity.this);
        mediaPlayerHelper.initMediaPlayer();
    }

    private void initView()
    {
        surfaceView = findViewById(R.id.surfaceview);
        surfaceView.setEGLContextClientVersion(2);
        surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0); // Alpha used for plane blending.
        surfaceView.setRenderer(this);
        surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        ImageView pausebtn = (ImageView)findViewById(R.id.pausebtn);
        pausebtn.setOnClickListener(this);
        pausebtn.setSelected(false);

        ImageView backbtn = (ImageView)findViewById(R.id.backbtn);
        backbtn.setOnClickListener(this);
        mLikekbtn = (ImageView)findViewById(R.id.likebtn);
        mLikekbtn.setOnClickListener(this);

        mClickview = findViewById(R.id.clickview);
        mClickview.setOnClickListener(this);

        mControllayout = (RelativeLayout)findViewById(R.id.control_layout);
        mControllayout.setOnClickListener(this);

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

    private void goBack(){
        mIsFinished = true;
        finish();
    };

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        surfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                if(mIsFinished)
                    return;
                try {
                    mSurfaceTexture.updateTexImage();
                    //得到图像的纹理矩阵
                    mSurfaceTexture.getTransformMatrix(mStMatrix);
                    surfaceView.requestRender();
                }catch (IllegalStateException e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        Log.d("mediahelp","onSurfaceCreated");
        int textid = 0;
        try {
            textid = glRender.createOnGlThread(this);
            mSurfaceTexture = new SurfaceTexture(textid);//构建用于预览的surfaceTexture
            mSurfaceTexture.setOnFrameAvailableListener(PlayActivity.this);
            mediaPlayerHelper.setSurface(mSurfaceTexture);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        Log.d("mediahelp","onSurfaceChanged");
        mediaPlayerHelper.play();
        mediaPlayerHelper.onVideoSizeChanged(width, height, new MediaPlayerHelper.OnGetVideoInfoListener() {
            @Override
            public void onGetVideoSizeChanged(int videowidth, int videoheight) {
                glRender.computeMatrix(videowidth / (float)videoheight,
                        width / (float)height);
            }
        });
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        glRender.draw(mStMatrix);
    }

    @Override
    public void onPause() {
        super.onPause();
        pausePlay();
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumePlay();
    }

    @Override
    public void onStop() {
        TimeModel.getInstance().checkTimeAtEnd(PlayActivity.this);
        TimeModel.getInstance().cancelTimer(PlayActivity.this, WaitingActivity.class);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        stopTimer();
        if(null != mSurfaceTexture)
        {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }
        Log.d("mediahelp","onDestroy");
        if(mediaPlayerHelper != null)
            mediaPlayerHelper.destory();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed()
    {
        goBack();
        super.onBackPressed();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.pausebtn:
                if(!mIsControlMode)
                    return;
                if(view.isSelected())
                {//暂停转播放
                    resumePlay();
                    view.setSelected(false);
                }else
                {
                    pausePlay();
                    view.setSelected(true);
                }
                break;
            case R.id.clickview:
                mIsControlMode = true;
                mControllayout.setVisibility(View.VISIBLE);
                view.setVisibility(View.GONE);
                mCount = 0;
                mSeekBar.setProgress(mediaPlayerHelper.getCurrentPosition());
                mVideotimeTv.setText(mediaPlayerHelper.getCurrentVideoTimeString());
                break;
            case R.id.control_layout:
                mIsControlMode = false;
                view.setVisibility(View.INVISIBLE);
                mClickview.setVisibility(View.VISIBLE);
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
                mAllSongs.get(mVideoIndex).setIs_like(mLikekbtn.isSelected());
                updateLikeData();
                break;
        }
    }

    private void updateLikeData()
    {
        mAllSongs.get(mVideoIndex).setIs_like(mLikekbtn.isSelected());
        SongDataBaseHelper.getInstance().songDataBase.getSongDao().update(mAllSongs.get(mVideoIndex));
    }

    private void playLastVideo()
    {
        int index = mVideoIndex - 1;
        if(index < 0)
            index = mAllSongs.size() - 1;
        if(index >= mAllSongs.size())
            index = mAllSongs.size() - 1;
        mVideoIndex = index;
        boolean result = mediaPlayerHelper.playNextItem(mAllSongs.get(index).getName());
        if(!result)
        {
            mLastbtn.setClickable(true);
            mNextbtn.setClickable(true);
        }else
        {
            hasUpdateNumber = false;
        }
    }

    private void playNextVideo()
    {
        int index = mVideoIndex + 1;
        if(index < 0)
            index = 0;
        if(index >= mAllSongs.size())
            index = 0;
        mVideoIndex = index;
        boolean result = mediaPlayerHelper.playNextItem(mAllSongs.get(index).getName());
        if(!result)
        {
            mLastbtn.setClickable(true);
            mNextbtn.setClickable(true);
        }else
        {
            hasUpdateNumber = false;
        }
    }

    private void resumePlay()
    {
        surfaceView.requestRender();
        surfaceView.onResume();
        if(mediaPlayerHelper != null)
            mediaPlayerHelper.resume();
    }

    private void pausePlay()
    {
        surfaceView.onPause();
        if(mediaPlayerHelper != null)
            mediaPlayerHelper.pause();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        Log.d("mediahelp","onProgressChanged");
        if(null != mediaPlayerHelper)
            mVideotimeTv.setText(mediaPlayerHelper.getCurrentVideoTimeString());
    }

    /*
     * 通知用户已经开始一个触摸拖动手势。
     * */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        isSeekbarChaning = true;
        mCount = 0;
        stopTimer();
    }

    /*
     * 当手停止拖动进度条时执行该方法
     * 首先获取拖拽进度
     * 将进度对应设置给MediaPlayer
     * */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        isSeekbarChaning = false;
        if(null != mediaPlayerHelper)
        {
            mediaPlayerHelper.seekTo(seekBar.getProgress());
            mVideotimeTv.setText(mediaPlayerHelper.getCurrentVideoTimeString());
        }
    }

    @Override
    public void onMediaPlayerStart(int position, int totaltime) {
        mSeekBar.setMax(totaltime);
        mCount = 0;
        startTimer();
        mLastbtn.setClickable(true);
        mNextbtn.setClickable(true);
        mLikekbtn.setSelected(mAllSongs.get(mVideoIndex).getIs_like());
        Log.d("mediahelp","onMediaPlayerStart");
    }

    @Override
    public void onMediaPlayerPause() {
        stopTimer();
        mCount = -1;
        Log.d("mediahelp","onMediaPlayerPause");
    }

    @Override
    public void onMediaPlayerDestroy() {
        stopTimer();
        mCount = -1;
    }

    @Override
    public void onMediaPlayerComplation() {
        updateSongPlayNumber();
        playNextVideo();
    }

    private void startTimer()
    {
        stopTimer();
        if (mTimer == null) {
            mTimer = new Timer();
        }

        if (mTimerTask == null) {
            mTimerTask = new MyTimerTask();
        }

        if(mTimer != null && mTimerTask != null )
            mTimer.schedule(mTimerTask, 0, 1000);

    }

    private void stopTimer()
    {
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
        mHandler.removeMessages(UPDATE_TEXTVIEW);
    }

    private void updateSongPlayNumber()
    {
        if(hasUpdateNumber)
            return;
        if(mediaPlayerHelper.getCurrentPosition() > (mSeekBar.getMax() * 0.75))
        {
            Song song = mAllSongs.get(mVideoIndex);
            song.setNumber(song.getNumber()+1);
            SongDataBaseHelper.getInstance().songDataBase.getSongDao().update(song);
            hasUpdateNumber = true;
        }
    }

    private void updateTextView()
    {
        if(mCount <= TOTAL_COUNT)
        {
            mCount++;
            Log.d("mediahelp","updateTextView");
            mSeekBar.setProgress(mediaPlayerHelper.getCurrentPosition());
//            mVideotimeTv.setText(mediaPlayerHelper.getCurrentVideoTimeString());
        }
        else
        {
            mCount = -1;
            mIsControlMode = false;
            mControllayout.setVisibility(View.INVISIBLE);
            mClickview.setVisibility(View.VISIBLE);
        }

    }

    class MyTimerTask extends TimerTask{

        @Override
        public void run() {
            if(!isSeekbarChaning && mCount >= 0){
                Message msg = new Message();
                msg.what = UPDATE_TEXTVIEW;
                mHandler.sendMessage(msg);
            }
        }

    };

}