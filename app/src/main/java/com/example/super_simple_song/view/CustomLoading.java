package com.example.super_simple_song.view;

import android.app.Dialog;
import android.content.Context;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.myapplication.R;

public class CustomLoading extends Dialog {
    private ProgressBar mLoadingProgress;
    private TextView mLoadingText;
    private View mLoadingView;

    public CustomLoading(Context context) {
        super(context, R.style.style_custom_loading);
        init(context);
    }

    public CustomLoading(Context context, int themeResId) {
        super(context, R.style.style_custom_loading);
        init(context);
    }

    protected CustomLoading(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init(context);
    }

    public void setText(@NonNull String text) {
        if (mLoadingText != null) {
            mLoadingText.setText(text);
            mLoadingText.setVisibility(View.VISIBLE);
        }
    }

    private void init(Context context) {
        mLoadingView = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null);

        mLoadingProgress = mLoadingView.findViewById(R.id.dialog_loading_loading);
        mLoadingText = mLoadingView.findViewById(R.id.dialog_loading_text);

        this.setContentView(mLoadingView);
        this.setCancelable(true);
        this.setCanceledOnTouchOutside(false);
    }
}
