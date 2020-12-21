package com.example.super_simple_song.Play;

import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Environment;
import android.view.Surface;
import java.io.IOException;


public class MediaPlayerHelper {
    public String videoPath = Environment.getExternalStorageDirectory().getPath()+"/cc_long_tate_1021.mp4";

    private Surface mSurface;
    private MediaPlayer mediaPlayer;
    private OnMediaPlayerActionListener mOnMediaPlayerActionListener;
    private String durationstr = null;
    private int mCurrentPos = 0;
    public int mVideoWidth = 0, mVideoHeight = 0;


    public MediaPlayerHelper(String videoPath){
        if(null != videoPath && !videoPath.isEmpty())
        this.videoPath = videoPath;
    }

    public boolean initMediaPlayer()
    {
        if(null != mediaPlayer)
            return false;

        mediaPlayer = new MediaPlayer();
        mediaPlayer.reset();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(videoPath);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean play() {

        try {
            mediaPlayer.setSurface(mSurface);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    try {
                        if (mp != null) {
                            mp.seekTo(mCurrentPos);
//                            mp.start(); //视频开始播放了
//                            if(null != mOnMediaPlayerActionListener)
//                                mOnMediaPlayerActionListener.onMediaPlayerStart(0,mediaPlayer.getDuration());
                        }
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                }
            });
            mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                @Override
                public void onSeekComplete(MediaPlayer mediaPlayer) {
                    if(!mediaPlayer.isPlaying())
                    {
                        mediaPlayer.start();
                    }
                    if(null != mOnMediaPlayerActionListener)
                        mOnMediaPlayerActionListener.onMediaPlayerStart(mCurrentPos,mediaPlayer.getDuration());
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    if(null != mOnMediaPlayerActionListener)
                        mOnMediaPlayerActionListener.onMediaPlayerComplation();
                }
            });
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                    return true;
                }
            });
            mediaPlayer.setLooping(false);
        } catch (IllegalArgumentException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return false;
        } catch (SecurityException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return false;
        } catch (IllegalStateException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return false;
        }
        return true;
    }

    public void setSurface(SurfaceTexture surfaceTexture)
    {
        if(null != mediaPlayer)
        {
            mSurface = new Surface(surfaceTexture);
        }
    }

    public void setOnMediaPlayerActionListener(OnMediaPlayerActionListener l)
    {
        if(null != l)
            mOnMediaPlayerActionListener = l;
    }

    public boolean playNextItem(String videoPath)
    {
        if(null == videoPath || videoPath.isEmpty())
            return false;
        destory();
        mCurrentPos = 0;
        mVideoWidth = 0;
        mVideoHeight = 0;
        this.videoPath = videoPath;
        boolean result = initMediaPlayer();
        if(!result)
            return false;
        return play();
    }

    public void seekTo(int progress)
    {
        mCurrentPos = progress;
        mediaPlayer.seekTo(mCurrentPos);
    }

    public int getCurrentPosition()
    {
        if(null != mediaPlayer)
            return mediaPlayer.getCurrentPosition();
        return 0;
    }

    public String getCurrentVideoTimeString()
    {
        if(null == mediaPlayer)
            return "";
        if(null == durationstr)
        {
            int duration = mediaPlayer.getDuration() / 1000;//获取音乐总时长
            durationstr = calculateTime(duration);
        }

        int position = mediaPlayer.getCurrentPosition();//获取当前播放的位置
        String time = calculateTime(position / 1000) + " / " + durationstr;
//        Log.d("mediahelp","getCurrentVideoTimeString2 position = "+position);
        return time;
    }

    public void destory()
    {
        if (mediaPlayer != null) {
            if(mediaPlayer.isPlaying())
                mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer =null;
            durationstr = null;
        }
        if(null != mOnMediaPlayerActionListener)
            mOnMediaPlayerActionListener.onMediaPlayerDestroy();
    }

    public void pause()
    {
        if(null != mediaPlayer && mediaPlayer.isPlaying())
        {
            mediaPlayer.pause();
            mCurrentPos = mediaPlayer.getCurrentPosition();
            if(null != mOnMediaPlayerActionListener)
                mOnMediaPlayerActionListener.onMediaPlayerPause();
        }
    }

    public void resume()
    {
        if(null != mediaPlayer)
        {
            if(mCurrentPos > 0)
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mediaPlayer.seekTo(mCurrentPos,MediaPlayer.SEEK_CLOSEST);
                }
                else
                    mediaPlayer.seekTo(mCurrentPos);
            }
        }else
        {
            initMediaPlayer();
            play();
        }
    }

    public boolean isPlaying()
    {
        if(null != mediaPlayer)
            return mediaPlayer.isPlaying();
        return false;
    }

    public void onVideoSizeChanged(int screenWidth, int screenHeight, OnGetVideoInfoListener listener)
    {
        mediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
            @Override
            public void onVideoSizeChanged(MediaPlayer mediaPlayer, int i, int i1) {
                mVideoWidth = i;
                mVideoHeight = i1;
                if (screenWidth > 0 && screenHeight > 0 && null != listener) {
                    listener.onGetVideoSizeChanged(mVideoWidth,mVideoHeight);
                }
            }
        });
    }

    //计算播放时间
    public String calculateTime(int time){
        int minute;
        int second;
        if(time > 60){
            minute = time / 60;
            second = time % 60;
            //分钟再0~9
            if(minute >= 0 && minute < 10){
                //判断秒
                if(second >= 0 && second < 10){
                    return "0"+minute+":"+"0"+second;
                }else {
                    return "0"+minute+":"+second;
                }
            }else {
                //分钟大于10再判断秒
                if(second >= 0 && second < 10){
                    return minute+":"+"0"+second;
                }else {
                    return minute+":"+second;
                }
            }
        }else if(time < 60){
            second = time;
            if(second >= 0 && second < 10){
                return "00:"+"0"+second;
            }else {
                return "00:"+ second;
            }
        }
        return "";
    }

    public interface OnGetVideoInfoListener{
        public void onGetVideoSizeChanged(int videowidth, int videoheight);
    }

    public interface OnMediaPlayerActionListener{
        public void onMediaPlayerStart(int position, int totaltime);
        public void onMediaPlayerPause();
        public void onMediaPlayerDestroy();
        public void onMediaPlayerComplation();
    }

}
