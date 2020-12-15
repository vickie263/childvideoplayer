package com.example.super_simple_song;

import com.example.super_simple_song.app.SongsApp;
import com.example.super_simple_song.database.Song;
import com.example.super_simple_song.database.SongDataBaseHelper;
import java.util.ArrayList;
import java.util.List;

public class LikePagePresenterImp  implements SongsContactor.IPresenter {
    private SongsContactor.IView likepageView;
    private List<Song> mAllLikeSongs = null;

    @Override
    public void bindView(SongsContactor.IView view) {
        likepageView = view;
    }

    @Override
    public void loadSongs() {
        mAllLikeSongs = SongDataBaseHelper.getInstance().songDataBase.getSongDao().getLikeSongsOrderbyPlaynumberDesc();
        if(null == mAllLikeSongs)
            mAllLikeSongs = new ArrayList<>();
        likepageView.showSongsList(mAllLikeSongs);
    }

    @Override
    public void updateSongs() {
        mAllLikeSongs = SongDataBaseHelper.getInstance().songDataBase.getSongDao().getLikeSongsOrderbyPlaynumberDesc();
        likepageView.updateSongsList(mAllLikeSongs);
    }

    @Override
    public void OnLikeSong(int songid) {

    }

    @Override
    public void OnDeleteSong(int songid) {
        for(int i = 0; i < mAllLikeSongs.size(); i++)
        {
            Song song = mAllLikeSongs.get(i);
            if(song.getId() == songid)
            {
                song.setIs_like(false);
                SongDataBaseHelper.getInstance().songDataBase.getSongDao().update(song);
                break;
            }
        }
        mAllLikeSongs = SongDataBaseHelper.getInstance().songDataBase.getSongDao().getLikeSongsOrderbyPlaynumberDesc();
    }

    @Override
    public boolean checkTimer() {
        boolean isExpired = TimeModel.getInstance().checkTimeAtBegin(SongsApp.getAppContext());
        if(isExpired)
        {
            if(TimeModel.getInstance().bHasInterval(SongsApp.getAppContext()))
            {
                TimeModel.getInstance().beginTimer(SongsApp.getAppContext(),WaitingActivity.class);
            }
        }
        return isExpired;
    }
}
