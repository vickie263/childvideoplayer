package com.example.super_simple_song;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.super_simple_song.tools.PreferenceUtil;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener,
        RadioGroup.OnCheckedChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initView();
    }

    private void initTitle()
    {
        ImageView backbutton = (ImageView)findViewById(R.id.title_img);
        backbutton.setOnClickListener(SettingActivity.this);
        TextView title = (TextView)findViewById(R.id.title_tv);
        title.setText(getString(R.string.settingpage_title));
    }

    private void initView()
    {
        initTitle();
        RadioGroup rg = (RadioGroup)findViewById(R.id.usingtime_rg);
        rg.setOnCheckedChangeListener(SettingActivity.this);
        RadioButton rb0 = (RadioButton)findViewById(R.id.rb_0);
        RadioButton rb15 = (RadioButton)findViewById(R.id.rb_15min);
        RadioButton rb20 = (RadioButton)findViewById(R.id.rb_20min);
        RadioButton rb30 = (RadioButton)findViewById(R.id.rb_30min);
        RadioButton rb45 = (RadioButton)findViewById(R.id.rb_45min);
        int interval = TimeModel.getInstance().mInterval;
        rb0.setChecked(interval == TimeModel.INTERVAL_0 ? true:false);
        rb15.setChecked(interval == TimeModel.INTERVAL_15 ? true:false);
        rb20.setChecked(interval == TimeModel.INTERVAL_20 ? true:false);
        rb30.setChecked(interval == TimeModel.INTERVAL_30 ? true:false);
        rb45.setChecked(interval == TimeModel.INTERVAL_45 ? true:false);
        rg = (RadioGroup)findViewById(R.id.playmode_rg);
        rg.setOnCheckedChangeListener(SettingActivity.this);
        RadioButton rb_vedio = (RadioButton)findViewById(R.id.rb_vedio);
        RadioButton rb_audio = (RadioButton)findViewById(R.id.rb_audio);
        int playmode = PreferenceUtil.getInt(SettingActivity.this,SongsConstants.FILE_SETTING,
                SongsConstants.KEY_PLAYMODE, SongsConstants.VALUE_PLAYMODE_VEDIO);
        rb_vedio.setChecked(playmode == SongsConstants.VALUE_PLAYMODE_VEDIO ? true:false);
        rb_audio.setChecked(playmode == SongsConstants.VALUE_PLAYMODE_AUDIO ? true:false);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.title_img:
                finish();
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        switch (i)
        {
            case R.id.rb_0:
                TimeModel.getInstance().writeInterval(TimeModel.INTERVAL_0);
                break;
            case R.id.rb_15min:
                TimeModel.getInstance().writeInterval(TimeModel.INTERVAL_15);
                break;
            case R.id.rb_20min:
                TimeModel.getInstance().writeInterval(TimeModel.INTERVAL_20);
                break;
            case R.id.rb_30min:
                TimeModel.getInstance().writeInterval(TimeModel.INTERVAL_30);
                break;
            case R.id.rb_45min:
                TimeModel.getInstance().writeInterval(TimeModel.INTERVAL_45);
                break;

            case R.id.rb_vedio:
                PreferenceUtil.putInt(SettingActivity.this,SongsConstants.FILE_SETTING,
                        SongsConstants.KEY_PLAYMODE,SongsConstants.VALUE_PLAYMODE_VEDIO);
                break;
            case R.id.rb_audio:
                PreferenceUtil.putInt(SettingActivity.this,SongsConstants.FILE_SETTING,
                        SongsConstants.KEY_PLAYMODE,SongsConstants.VALUE_PLAYMODE_AUDIO);
                break;
        }
    }
}