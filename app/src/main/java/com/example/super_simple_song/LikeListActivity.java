package com.example.super_simple_song;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.super_simple_song.Play.PlayActivity;
import com.example.super_simple_song.database.Song;

import java.util.List;

import javax.xml.transform.Result;

public class LikeListActivity extends AppCompatActivity implements SongsContactor.IView, View.OnClickListener {
    private SongsContactor.IPresenter likepagePresenter;
    private RecyclerView mSongsListView;
    private SongsListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_likelist);
        mSongsListView = (RecyclerView)findViewById(R.id.likepage_list_rv);
        mSongsListView.setLayoutManager(new GridLayoutManager(this,2));
        likepagePresenter = new LikePagePresenterImp();
        likepagePresenter.bindView(this);
        likepagePresenter.loadSongs();

        initTitle();
    }

    private void initTitle()
    {
        ImageView backbutton = (ImageView)findViewById(R.id.title_img);
        backbutton.setOnClickListener(LikeListActivity.this);
        TextView title = (TextView)findViewById(R.id.title_tv);
        title.setText(getString(R.string.likepage_title));
    }

    @Override
    public void requirePermissions() {

    }

    @Override
    public void showSongsList(List<Song> songs) {
        mAdapter = new SongsListAdapter.SongsListAdapterBuilder()
                .bindData(songs).bindIsLikeList(true).bindButtonClickListener(new SongsListAdapter.OnButtonClickListener() {
                    @Override
                    public void OnClick(int songid) {
                        final AlertDialog.Builder normalDialog =
                                new AlertDialog.Builder(LikeListActivity.this);
                        normalDialog.setMessage(getString(R.string.dialog_msg));
                        normalDialog.setPositiveButton(getString(R.string.dialog_positive),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //...To-do
                                        likepagePresenter.OnDeleteSong(songid);
                                        likepagePresenter.updateSongs();
                                        dialog.dismiss();
                                    }
                                });
                        normalDialog.setNegativeButton(getString(R.string.dialog_negative),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //...To-do
                                        dialog.dismiss();
                                    }
                                });
                        // 显示
                        normalDialog.show();
                    }

                    @Override
                    public void OnItemClick(Song song) {
                        gotoPlayPage(song);
                    }
                }).build(LikeListActivity.this);
        mSongsListView.setAdapter(mAdapter);
    }

    @Override
    public void updateSongsList(List<Song> songs, int... position) {
        mAdapter.notifyChangeAllData(songs);
    }

    private void gotoPlayPage(Song song)
    {
        if(null == song)
            return;
        boolean isExpired = likepagePresenter.checkTimer();
        if(isExpired)
        {
            Intent intent=new Intent(LikeListActivity.this, PlayActivity.class);
            intent.putExtra(RouterConstants.SONG_ID, song.getId());
            intent.putExtra(RouterConstants.FROM_WHERE,RouterConstants.FROM_LIKE);
            startActivity(intent);
        }
        else
        {
            Intent intent=new Intent(LikeListActivity.this, WaitingActivity.class);
            intent.putExtra(RouterConstants.FROM_ALARM,false);
            startActivity(intent);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.title_img:
                finish();
                break;
        }
    }

}