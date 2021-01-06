package com.example.super_simple_song.Play;

import android.os.Handler;
import android.os.Message;

import androidx.annotation.Nullable;

import java.util.Timer;
import java.util.TimerTask;

public class TimerHelper {
    private static final int UPDATE_TEXTVIEW = 0;

    private ITimerDataCallback mCallback;
    private Timer mTimer;//定时器
    private TimerTask mTimerTask;
    private Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_TEXTVIEW:
                    mCallback.onTimeUp();
                    break;
                default:
                    break;
            }
        }
    };

    public TimerHelper(@Nullable ITimerDataCallback callback)
    {
        mCallback = callback;
    }

    public void destroyTimer()
    {
        stopTimer();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }
    }

    public void startTimer()
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

    public void stopTimer()
    {
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
        mHandler.removeMessages(UPDATE_TEXTVIEW);
    }

    class MyTimerTask extends TimerTask{

        @Override
        public void run() {
            Message msg = new Message();
            msg.what = UPDATE_TEXTVIEW;
            mHandler.sendMessage(msg);
        }

    }

    public interface ITimerDataCallback{
        void onTimeUp();
    }
}
