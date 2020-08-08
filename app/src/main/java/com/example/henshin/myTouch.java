package com.example.henshin;

import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class myTouch implements View.OnTouchListener {

    private com.example.henshin.musicService musicService;
    private int n;
    private float mPosX;
    private float mPosY;
    private float mCurrentPosX;
    private float mCurrentPosY;
    private ImageView imageView;

    myTouch(ImageView i, com.example.henshin.musicService m){
        musicService = m;
        imageView = i;
        n = 0;
    }
    public boolean onTouch(View v, MotionEvent event) {

        return true;
    }
}
