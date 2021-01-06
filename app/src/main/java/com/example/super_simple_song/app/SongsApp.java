package com.example.super_simple_song.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.super_simple_song.TimeModel;

public class SongsApp extends Application {
    private static Application mApplication;
    private int activityAount = 0;

    public SongsApp() {
        super();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
//        CrashManager.getInstance().installSelfLooper();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;
        registerActivityLifecycleCallbacks(activityLifecycleCallbacks);
        TimeModel.getInstance().initInterval(SongsApp.getAppContext());
    }

    public static Context getAppContext() {
        return mApplication.getApplicationContext();
    }

    /**
     * Activity 生命周期监听，用于监控app前后台状态切换
     */
    private ActivityLifecycleCallbacks activityLifecycleCallbacks = new ActivityLifecycleCallbacks() {

        @Override
        public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {

        }

        @Override
        public void onActivityStarted(@NonNull Activity activity) {
            activityAount++;
        }

        @Override
        public void onActivityResumed(@NonNull Activity activity) {

        }

        @Override
        public void onActivityPaused(@NonNull Activity activity) {

        }

        @Override
        public void onActivityStopped(@NonNull Activity activity) {
            activityAount--;
            if (activityAount == 0) {
//                isForeground = false;
                TimeModel.getInstance().saveInterval(getAppContext());
            }
        }

        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {

        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {

        }
    };
}
