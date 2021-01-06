package com.example.super_simple_song.Play;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.myapplication.R;
import com.example.super_simple_song.TimeModel;
import com.example.super_simple_song.WaitingActivity;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class VideoPlayActivity extends AppCompatActivity implements GLSurfaceView.Renderer,
        SurfaceTexture.OnFrameAvailableListener, View.OnClickListener,
        SeekBar.OnSeekBarChangeListener, MediaPlayerHelper.OnMediaPlayerActionListener,
        TimerHelper.ITimerDataCallback{

    private static final int TOTAL_COUNT = 5;

    private boolean hasUpdateNumber = false;

    private GLSurfaceView surfaceView;
    private GLRender glRender;
    private SurfaceTexture mSurfaceTexture;
    private MediaPlayerHelper mediaPlayerHelper;

    private float[] mStMatrix = new float[16];

    private PlayDataHelper mPlayDataHelper;
    private TimerHelper mTimerHelper;

    private boolean mIsControlMode = false;
    private boolean mIsFinished = false;
    private RelativeLayout mControllayout;
    private View mClickview;
    private TextView mVideotimeTv;
    private SeekBar mSeekBar;
    private TextView mLastbtn;
    private TextView mNextbtn;
    private ImageView mLikekbtn;
    private ImageView mPausebtn;

    private boolean isSeekbarChaning;//互斥变量，防止进度条和定时器冲突。
    private int mCount = 0;//显示控制面板，在播放状态下，count秒后自动隐藏控制面板

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
        mTimerHelper = new TimerHelper(VideoPlayActivity.this);
        mPlayDataHelper = new PlayDataHelper();
        int result = mPlayDataHelper.initSong(VideoPlayActivity.this,getIntent());
        if(result == PlayDataHelper.RESULT_NO_SONGS)
        {
            Toast.makeText(this,getString(R.string.playpage_novideo),Toast.LENGTH_LONG).show();
            return;
        }
        mLikekbtn.setSelected(mPlayDataHelper.getCurrentSong().getIs_like());

        mediaPlayerHelper = new MediaPlayerHelper(mPlayDataHelper.getCurrentVideoPath());
        mediaPlayerHelper.setOnMediaPlayerActionListener(VideoPlayActivity.this);
        result = mediaPlayerHelper.initMediaPlayer();
        if(mediaPlayerHelper.RESULT_IOEXCEPTION == result)
        {
            if(PlayDataHelper.RESULT_NO_SONGS == mPlayDataHelper.dealwithErrorVideo(VideoPlayActivity.this))
                goBack();
            playNextVideo();
        }
    }

    private void initView()
    {
        surfaceView = findViewById(R.id.surfaceview);
        surfaceView.setEGLContextClientVersion(2);
        surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0); // Alpha used for plane blending.
        surfaceView.setRenderer(this);
        surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        mControllayout = (RelativeLayout)findViewById(R.id.control_layout);
        mControllayout.setOnClickListener(this);

        mPausebtn = (ImageView)findViewById(R.id.pausebtn);
        mPausebtn.setOnClickListener(this);
        mPausebtn.setSelected(false);

        ImageView backbtn = (ImageView)findViewById(R.id.backbtn);
        backbtn.setOnClickListener(this);
        mLikekbtn = (ImageView)findViewById(R.id.likebtn);
        mLikekbtn.setOnClickListener(this);

        mClickview = findViewById(R.id.clickview);
        mClickview.setOnClickListener(this);

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
    }

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
        int textid = 0;
        try {
            textid = glRender.createOnGlThread(this);
            mSurfaceTexture = new SurfaceTexture(textid);//构建用于预览的surfaceTexture
            mSurfaceTexture.setOnFrameAvailableListener(VideoPlayActivity.this);
            mediaPlayerHelper.setSurface(mSurfaceTexture);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        mediaPlayerHelper.play();
        if(mediaPlayerHelper.mVideoWidth > 0 && mediaPlayerHelper.mVideoHeight > 0)
            glRender.computeMatrix(mediaPlayerHelper.mVideoWidth / (float)mediaPlayerHelper.mVideoHeight,
                    width / (float)height);
        else
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
        TimeModel.getInstance().checkTimeAtEnd(VideoPlayActivity.this);
        TimeModel.getInstance().cancelTimer(VideoPlayActivity.this, WaitingActivity.class);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if(null != mSurfaceTexture)
        {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }
        if(mediaPlayerHelper != null)
            mediaPlayerHelper.destory();
        mTimerHelper.destroyTimer();
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
                updateLikeData();
                break;
        }
    }

    private void updateLikeData()
    {
        mPlayDataHelper.updateLikeDataToDataBase(mLikekbtn.isSelected());
    }

    private void playLastVideo()
    {
        mPlayDataHelper.updateLastVideoIndex();
        int result = mediaPlayerHelper.playNextItem(mPlayDataHelper.getCurrentSong().getName());
        if(result != mediaPlayerHelper.RESULT_SUCCESS)
        {
            if(result == mediaPlayerHelper.RESULT_IOEXCEPTION)
            {
                if(PlayDataHelper.RESULT_NO_SONGS == mPlayDataHelper.dealwithErrorVideo(VideoPlayActivity.this))
                    goBack();
                playNextVideo();
            }else
            {
                mLastbtn.setClickable(true);
                mNextbtn.setClickable(true);
            }
        }else
        {
            hasUpdateNumber = false;
        }
    }

    private void playNextVideo()
    {
        mPlayDataHelper.updateNextVideoIndex();
        int result = mediaPlayerHelper.playNextItem(mPlayDataHelper.getCurrentSong().getName());
        if(result != mediaPlayerHelper.RESULT_SUCCESS)
        {
            if(result == mediaPlayerHelper.RESULT_IOEXCEPTION)
            {
                if(PlayDataHelper.RESULT_NO_SONGS == mPlayDataHelper.dealwithErrorVideo(VideoPlayActivity.this))
                    goBack();
                playNextVideo();
            }
            else
            {
                mLastbtn.setClickable(true);
                mNextbtn.setClickable(true);
            }
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
        mTimerHelper.stopTimer();
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
        mTimerHelper.startTimer();
        mLastbtn.setClickable(true);
        mNextbtn.setClickable(true);
        mPausebtn.setSelected(false);
        mLikekbtn.setSelected(mPlayDataHelper.getCurrentSong().getIs_like());
    }

    @Override
    public void onMediaPlayerPause() {
        mTimerHelper.stopTimer();
        mCount = -1;
    }

    @Override
    public void onMediaPlayerDestroy() {
        mTimerHelper.stopTimer();
        mCount = -1;
    }

    @Override
    public void onMediaPlayerComplation() {
        updateSongPlayNumber();
        playNextVideo();
    }

    private void updateSongPlayNumber()
    {
        if(hasUpdateNumber)
            return;
        if(mediaPlayerHelper.getCurrentPosition() > (mSeekBar.getMax() * 0.5))
        {
            mPlayDataHelper.addSongPlayNumber();
            hasUpdateNumber = true;
        }
    }

    private void updateTextView()
    {
        if(mCount <= TOTAL_COUNT)
        {
            mCount++;
            mSeekBar.setProgress(mediaPlayerHelper.getCurrentPosition());
        }
        else
        {
            mCount = -1;
            mIsControlMode = false;
            mControllayout.setVisibility(View.INVISIBLE);
            mClickview.setVisibility(View.VISIBLE);
        }

    }


    @Override
    public void onTimeUp() {
        if(!isSeekbarChaning && mCount >= 0){
            updateTextView();
            updateSongPlayNumber();
        }
    }
}