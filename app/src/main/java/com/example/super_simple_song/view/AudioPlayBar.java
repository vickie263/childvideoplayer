package com.example.super_simple_song.view;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;

public class AudioPlayBar extends RelativeLayout {
    private Context mContext;
    private View mView;
    private ImageView mImage;
    private TextView mNameText;
    private ImageView mPlayBtn;
    private onPlayStateChangeListener mOnPlayStateChangeListener;
    private onClickListener mOnClickListener;
    public AudioPlayBar(Context context) {
        super(context);
        init(context);
    }

    public AudioPlayBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AudioPlayBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context)
    {
        mContext = context;
        mView = LayoutInflater.from(context).inflate(R.layout.view_audioplay, this);
        mImage = mView.findViewById(R.id.audio_thumb);
        mImage.setOnClickListener(view -> {
            if(null != mOnClickListener)
                mOnClickListener.onClick(view);
        });
        mNameText = mView.findViewById(R.id.audio_name);
        mNameText.setOnClickListener(view -> {
            if(null != mOnClickListener)
                mOnClickListener.onClick(view);
        });
        mPlayBtn = mView.findViewById(R.id.audio_playbtn);
        mPlayBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mPlayBtn.isSelected())
                {
                    mPlayBtn.setSelected(false);
                    if(null != mOnPlayStateChangeListener)
                        mOnPlayStateChangeListener.onSongPlay();
                }
                else
                {
                    mPlayBtn.setSelected(true);
                    if(null != mOnPlayStateChangeListener)
                        mOnPlayStateChangeListener.onSongPause();
                }
            }
        });
    }

    public void setOnPlayStateChangeListener(@Nullable onPlayStateChangeListener l)
    {
        mOnPlayStateChangeListener = l;
    }

    public void setOnRootViewClickListener(@Nullable onClickListener l){
        mOnClickListener = l;
    }

    public void setPlayed(boolean isPlay)
    {
        mPlayBtn.setSelected(!isPlay);
    }

    public void setImageUri(@Nullable Uri uri)
    {
        Glide.with(mContext).load(uri).centerCrop().into(mImage);
    }

    public void setText(@Nullable String text)
    {
        mNameText.setText(text);
    }

    public interface onPlayStateChangeListener{
        void onSongPlay();
        void onSongPause();
    }

    public interface onClickListener{
        void onClick(View v);
    }
}
