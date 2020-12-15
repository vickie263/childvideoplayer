package com.example.super_simple_song;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.super_simple_song.app.SongsApp;
import com.example.super_simple_song.tools.PreferenceUtil;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.content.Context.ALARM_SERVICE;

public class TimeModel {
    public final static int IDLE_TIME1 = 2;//120;
    public final static int IDLE_TIME2 = 1;//30;
    public final static int INTERVAL_0 = 0;
    public final static int INTERVAL_15 = 1;//15;
    public final static int INTERVAL_20 = 2;//20;
    public final static int INTERVAL_30 = 3;//30;
    public final static int INTERVAL_45 = 4;//45;
    public final static String FILE_TIME = "timefile";
    public final static String KEY_TIME_BEGIN = "begin_time";
    public final static String KEY_INTERVAL = "interval";
    public final static String KEY_TOTALUSINGTIME = "total_using_time";
    public final static String KEY_TOTALIDELTIME = "total_idle_time";
    public int mInterval;

    private static TimeModel Singleton;
    public static TimeModel getInstance() {
        if (Singleton == null) {
            synchronized (TimeModel.class) {
                if (Singleton == null) {
                    Singleton = new TimeModel();
                }
            }
        }

        return Singleton;
    }

    public void writeCurrentTime(@Nullable Context context)
    {
        DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String datestr = simpleDateFormat.format(date);
        PreferenceUtil.putString(context, FILE_TIME, KEY_TIME_BEGIN,datestr);
    }

    public void writeInterval(int interval)
    {
        mInterval = interval;
    }

    public void saveInterval(@Nullable Context context)
    {
        PreferenceUtil.putInt(context,FILE_TIME,KEY_INTERVAL,mInterval);
    }

    public boolean bHasInterval(@Nullable Context context)
    {
        return PreferenceUtil.getInt(context,FILE_TIME,KEY_INTERVAL,INTERVAL_0) != INTERVAL_0 ? true : false;
    }

    public void initInterval(@Nullable Context context)
    {
        mInterval = PreferenceUtil.getInt(context,FILE_TIME,KEY_INTERVAL,INTERVAL_0);
    }

    //记录当前时间，并累计此次使用时间,一般退出app或者切换到后台时调用
    public void writeTotalUsingTimeAndCurrentTime(@Nullable Context context)
    {
        int lasttotal = PreferenceUtil.getInt(context,FILE_TIME,KEY_TOTALUSINGTIME,0);
        Date endTime = new Date(System.currentTimeMillis());
        String lastdatestr = PreferenceUtil.getString(context,FILE_TIME, KEY_TIME_BEGIN,"");
        if(lastdatestr.isEmpty())
            return;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date startTime = df.parse(lastdatestr);
            long minutes = getCurrentInterval(startTime,endTime);
            if(minutes < 1)
                minutes = 1;
            int total = (int)minutes + lasttotal;
            PreferenceUtil.putInt(context,FILE_TIME,KEY_TOTALUSINGTIME,total);
            Log.d("timemodel","writeTotalUsingTimeAndCurrentTime usingtime ="+total);
            String datestr = df.format(endTime);
            PreferenceUtil.putString(context, FILE_TIME, KEY_TIME_BEGIN,datestr);
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    public void clearTotalUsingTime(@Nullable Context context)
    {
        Log.d("timemodel","clearTotalUsingTime");
        PreferenceUtil.putInt(context,FILE_TIME,KEY_TOTALUSINGTIME,0);
    }

//    app启动：计算累计空闲时间： 1. 第一次启动和再次启动（退出时间大于IDLE_TIME1）：
//                                记录当前时间，清空累计空闲时间，返回true,并打开timer ，
//                             2. 再次启动（退出时间小于IDLE_TIME2）：
//                                  a.上次退出的累计使用时间超过设定时间了，或者累计使用时间为0，启动直接进入waiting页，返回false，累计使用时间清零
//                                  b. 上次退出的累计使用时间没有超过设定时间，记录当前时间。清空累计空闲时间，返回true
//		                       3.再次启动（退出时间小于IDLE_TIME1，大于IDLE_TIME2）：
//		                          启动直接进入waiting页，认为到时间了，返回false，累计使用时间清零
    public boolean checkTimeAtBegin(@Nullable Context context)
    {
        Log.d("timemodel","checkTimeAtBegin1");
        int lasttotalidle = PreferenceUtil.getInt(context,FILE_TIME,KEY_TOTALIDELTIME,0);
        int interval = PreferenceUtil.getInt(context,FILE_TIME,KEY_INTERVAL,0);
        if(interval == 0)
        {
            PreferenceUtil.putInt(context,FILE_TIME,KEY_TOTALIDELTIME,0);
            writeCurrentTime(context);
            Log.d("timemodel","checkTimeAtBegin interval = 0");
            return true;
        }
        Date endTime = new Date(System.currentTimeMillis());
        String lastdatestr = PreferenceUtil.getString(context,FILE_TIME, KEY_TIME_BEGIN,"");
        if(lastdatestr.isEmpty())
        {//没有记录过当前时间，说明是第一次启动
            writeCurrentTime(context);
            Log.d("timemodel","checkTimeAtBegin first launch");
            return true;
        }
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date startTime = df.parse(lastdatestr);
            long minutes = getCurrentInterval(startTime,endTime);
            int totalidle = (int)minutes + lasttotalidle;
            Log.d("timemodel","checkTimeAtBegin minutes = "+minutes);

            PreferenceUtil.putInt(context,FILE_TIME,KEY_TOTALIDELTIME,totalidle);
            if(totalidle > IDLE_TIME1) {
                PreferenceUtil.putInt(context,FILE_TIME,KEY_TOTALIDELTIME,0);
                writeCurrentTime(context);
                Log.d("timemodel","checkTimeAtBegin totalidle > IDLE_TIME1,totalidle = "+totalidle);
                return true;
            }
            else if(totalidle > IDLE_TIME2 && totalidle <= IDLE_TIME1)
            {
                clearTotalUsingTime(context);
                Log.d("timemodel","checkTimeAtBegin totalidle > IDLE_TIME2 && totalidle <= IDLE_TIME1,totalidle = "+totalidle);
                return false;
            }
            else if(totalidle <= IDLE_TIME2)
            {
                int total = PreferenceUtil.getInt(context,FILE_TIME,KEY_TOTALUSINGTIME,0);
                Log.d("timemodel","checkTimeAtBegin totalidle <= IDLE_TIME2,totalidle = "+totalidle
                +" totalusing = "+total);

                if(total >= interval || total == 0)
                {
                    clearTotalUsingTime(context);
                    return false;
                }
                else {
                    PreferenceUtil.putInt(context,FILE_TIME,KEY_TOTALIDELTIME,0);
                    writeCurrentTime(context);
                    return true;
                }
            }
            else
            {
                Log.d("timemodel","checkTimeAtBegin totalidle else,totalidle = "+totalidle);
                clearTotalUsingTime(context);
                return false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            writeCurrentTime(context);
            return true;
        }
    }

    //play退出或者切换到后台：1.没有到设定时间：记录当前时间，并累计此次使用时间
    //	                   2.到设定时间：记录当前时间，清空累计使用时间。
    public void checkTimeAtEnd(@Nullable Context context)
    {
        Log.d("timemodel","checkTimeAtEnd1");

        int interval = PreferenceUtil.getInt(context,FILE_TIME,KEY_INTERVAL,0);
        if(interval == 0)
        {
            Log.d("timemodel","checkTimeAtEnd interval == 0");
            return;
        }
        writeTotalUsingTimeAndCurrentTime(context);
        int totalusing = PreferenceUtil.getInt(context,FILE_TIME,KEY_TOTALUSINGTIME,0);
        Log.d("timemodel","checkTimeAtEnd totalusing = "+totalusing+" interval = "+interval);
        if(totalusing > interval)
        {
            clearTotalUsingTime(context);
        }
    }

    private long getCurrentInterval(Date startTime, Date endTime)
    {
        long diff = endTime.getTime() - startTime.getTime();
        long days = diff / (1000 * 60 * 60 * 24);
        long hours = (diff-days*(1000 * 60 * 60 * 24))/(1000* 60 * 60);
        return  (diff-days*(1000 * 60 * 60 * 24)-hours*(1000* 60 * 60))/(1000* 60);
    }

    public void beginTimer(@Nullable Context context, @Nullable Class dstactivity)
    {
        Log.d("timemodel","beginTimer1");
        int interval = PreferenceUtil.getInt(context,FILE_TIME,KEY_INTERVAL,0);
        if(interval == 0)
            return;
        int usingtime = PreferenceUtil.getInt(context,FILE_TIME,KEY_TOTALUSINGTIME,0);
        interval = interval - usingtime;
        if(interval == 0)
            return;
        Log.d("timemodel","beginTimer2 interval = "+interval);
        Intent perIntent = new Intent(context, dstactivity);
        perIntent.putExtra(RouterConstants.FROM_ALARM,true);
        PendingIntent sender = PendingIntent.getActivity(context,0,
                perIntent,PendingIntent.FLAG_ONE_SHOT);
        Calendar calendar=Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.MINUTE, interval);
        AlarmManager alarm=(AlarmManager)context.getSystemService(ALARM_SERVICE);
        alarm.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
    }

    public void cancelTimer(@Nullable Context context, @Nullable Class dstactivity)
    {
        Log.d("timemodel","cancelTimer");
        Intent perIntent = new Intent(context, dstactivity);
        PendingIntent sender = PendingIntent.getActivity(context,0,
                perIntent,PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarm=(AlarmManager)context.getSystemService(ALARM_SERVICE);
        alarm.cancel(sender);
    }

}
