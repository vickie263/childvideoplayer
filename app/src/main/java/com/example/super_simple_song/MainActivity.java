package com.example.super_simple_song;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.example.myapplication.R;
import com.example.super_simple_song.Play.AudioPlayActivity;
import com.example.super_simple_song.Play.PlayService;
import com.example.super_simple_song.Play.VideoPlayActivity;
import com.example.super_simple_song.database.Song;
import com.example.super_simple_song.tools.PreferenceUtil;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends BaseActivity implements EasyPermissions.PermissionCallbacks,
        SongsContactor.IView, View.OnClickListener{
    public static final int RC_STORAGE = 12;

    private SongsContactor.IPresenter mainpagePresenter;
    private RecyclerView mSongsListView;
    private SongsListAdapter mAdapter;

    @Override
    protected void onServiceConnected() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainpagePresenter = new MainPagePresenterImp();
        mainpagePresenter.bindView(this);

        initView();
        requirePermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mainpagePresenter.updateSongs();
        int playmode = PreferenceUtil.getInt(this,SongsConstants.FILE_SETTING,
                SongsConstants.KEY_PLAYMODE,SongsConstants.VALUE_PLAYMODE_VEDIO);
        if(playmode == SongsConstants.VALUE_PLAYMODE_AUDIO)
        {
            mHasPlayBar = true;
            startService();
            bindService();
        }else
        {
            stopService();
        }
    }

    private void initData()
    {
        mainpagePresenter.loadSongs();
    }

    private void initView()
    {
        mSongsListView = (RecyclerView)findViewById(R.id.mainpage_list_rv);
        mSongsListView.setLayoutManager(new GridLayoutManager(this,2));
        ImageView setting = (ImageView) findViewById(R.id.mainpage_setting_img);
        setting.setOnClickListener(this);
        ImageView like = (ImageView) findViewById(R.id.mainpage_like_img);
        like.setOnClickListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);

    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        initData();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void requirePermissions() {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE};

        if (EasyPermissions.hasPermissions(this, perms)) {

            initData();

        } else {

            // Do not have permissions, request them now

            //如果没有获取到权限，在这里获取权限，其中RC_CAMERA_AND_LOCATION是自己定义的一个唯一标识int值

            EasyPermissions.requestPermissions(this, getString(R.string.storage_rationale),

                    RC_STORAGE, perms);

        }

    }

    @Override
    public void showSongsList(List<Song> songs) {
        mAdapter = new SongsListAdapter.SongsListAdapterBuilder()
                .bindData(songs).bindIsLikeList(false).bindButtonClickListener(new SongsListAdapter.OnButtonClickListener() {
                    @Override
                    public void OnClick(int songid) {
                        mainpagePresenter.OnLikeSong(songid);
                    }

                    @Override
                    public void OnItemClick(Song song) {
                        gotoPlayPage(song);
                    }
                }).build(MainActivity.this);
        mSongsListView.setAdapter(mAdapter);
    }

    @Override
    public void updateSongsList(List<Song> songs, int... position) {
        if(null != mAdapter)
            mAdapter.notifyChangeAllData(songs);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.mainpage_setting_img:
                gotoSettingPage();
                break;
            case R.id.mainpage_like_img:
                gotoLikeListPage();
                break;
        }
    }

    private void gotoPlayPage(Song song)
    {
        if(null == song)
            return;
        Intent intent;
        int playmode = PreferenceUtil.getInt(MainActivity.this,SongsConstants.FILE_SETTING,
                SongsConstants.KEY_PLAYMODE,SongsConstants.VALUE_PLAYMODE_VEDIO);
        if(playmode == SongsConstants.VALUE_PLAYMODE_AUDIO)
        {
            intent=new Intent(MainActivity.this, AudioPlayActivity.class);
            intent.putExtra(SongsConstants.SONG_ID, song.getId());
            intent.putExtra(SongsConstants.FROM_WHERE, SongsConstants.FROM_MAIN);
            startActivity(intent);
            return;
        }

        boolean isExpired = mainpagePresenter.checkTimer();
        if(isExpired)
        {
            intent=new Intent(MainActivity.this, VideoPlayActivity.class);
            intent.putExtra(SongsConstants.SONG_ID, song.getId());
            intent.putExtra(SongsConstants.FROM_WHERE, SongsConstants.FROM_MAIN);
            startActivity(intent);
        }
        else
        {
            intent=new Intent(MainActivity.this, WaitingActivity.class);
            intent.putExtra(SongsConstants.FROM_ALARM,false);
            startActivity(intent);
        }
    }

    private void gotoLikeListPage()
    {
        Intent intent=new Intent(MainActivity.this, LikeListActivity.class);
        startActivity(intent);
    }

    private void gotoSettingPage()
    {
        Intent intent=new Intent(MainActivity.this, SettingActivity.class);
        startActivity(intent);
    }
}