package com.example.super_simple_song;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.myapplication.R;
import com.example.super_simple_song.database.Song;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SongsListAdapter extends RecyclerView.Adapter<SongsListAdapter.SongViewHold> {
    private final int TYPE_ALL = 1;
    private final int TYPE_LIKE = 2;

    private Context mContext;
    private List<Song> mDatas;
    private boolean isLikelist;
    private OnButtonClickListener listener;


    public SongsListAdapter(Context context, SongsListAdapterBuilder builder) {
        mContext = context;
        parseBuilder(builder);
    }

    private void parseBuilder(SongsListAdapterBuilder builder) {
        mDatas = builder.mDatas;
        isLikelist = builder.isLikelist;
        listener = builder.listener;
    }

    @Override
    public int getItemViewType(int position) {
        return isLikelist == true ? TYPE_LIKE: TYPE_ALL;
    }

    @NonNull
    @Override
    public SongViewHold onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemview = LayoutInflater.from(mContext).inflate(R.layout
                .item_song, parent, false);
        return new SongViewHold(itemview);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHold holder, int position) {
        if (holder == null) {
            return;
        }

        if (position < 0 || position >= mDatas.size()) {
            return;
        }

        Song song = mDatas.get(position);

        if (song == null) {
            return;
        }

        int strindex = song.getName().lastIndexOf("/");
        holder.name.setText(song.getName().substring(strindex+1));

        File videoFile = new File(song.getName());
        Glide.with(mContext).load(Uri.fromFile(videoFile)).centerCrop().into(holder.thumb);

        if(isLikelist)
        {
            holder.button_like.setVisibility(View.GONE);
            holder.button_delete.setVisibility(View.VISIBLE);
            holder.button_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Song ss = mDatas.get(position);
                    if(null != listener)
                        listener.OnClick(ss.getId());
                }
            });
        }else {
            holder.button_delete.setVisibility(View.GONE);
            holder.button_like.setVisibility(View.VISIBLE);
            holder.button_like.setSelected(song.getIs_like());
            holder.button_like.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Song ss = mDatas.get(position);
                    ss.setIs_like(!ss.getIs_like());
                    view.setSelected(ss.getIs_like());
                    if(null != listener)
                        listener.OnClick(ss.getId());
                }
            });
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Song ss = mDatas.get(position);
                if(null != listener)
                    listener.OnItemClick(ss);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    public void notifyChangeAllData(List<Song> songs)
    {
        mDatas = songs;
        notifyDataSetChanged();
    }

    public static class SongsListAdapterBuilder {

        private List<Song> mDatas;
        private boolean isLikelist;
        private OnButtonClickListener listener;

        public SongsListAdapterBuilder bindData(List<Song> list) {
            if(null == list)
                this.mDatas = new ArrayList<>();
            else
                this.mDatas = list;
            return this;
        }

        public SongsListAdapterBuilder bindIsLikeList(boolean isLikelist) {
            this.isLikelist = isLikelist;
            return this;
        }

        public SongsListAdapterBuilder bindButtonClickListener(OnButtonClickListener listener) {
            this.listener = listener;
            return this;
        }

        public SongsListAdapter build(Context context) {
            return new SongsListAdapter(context, this);
        }
    }

    public interface OnButtonClickListener{
        void OnClick(int songid);
        void OnItemClick(Song song);
    }

    public static class SongViewHold extends RecyclerView.ViewHolder {
        private ImageView thumb;
        private TextView name;
        private ImageView button_like;
        private ImageView button_delete;

        public SongViewHold(View itemView) {
            super(itemView);
            this.thumb = (ImageView) itemView.findViewById(R.id.song_thumb_img);
            this.name = (TextView) itemView.findViewById(R.id.song_name_tv);
            this.button_like = (ImageView) itemView.findViewById(R.id.song_like_img);
            this.button_delete = (ImageView) itemView.findViewById(R.id.song_delete_img);
        }
    }

}
