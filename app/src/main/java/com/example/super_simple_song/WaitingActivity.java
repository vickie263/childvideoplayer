package com.example.super_simple_song;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.myapplication.R;

public class WaitingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);
        initData();
    }

    private void initData()
    {
        Intent intent = getIntent();
        boolean isFromAlarm = intent.getBooleanExtra(RouterConstants.FROM_ALARM,false);
        if(isFromAlarm)
            TimeModel.getInstance().writeCurrentTime(WaitingActivity.this);
    }

    @Override
    public void onBackPressed()
    {
        Intent intent=new Intent(WaitingActivity.this, MainActivity.class);
        startActivity(intent);
    }

}