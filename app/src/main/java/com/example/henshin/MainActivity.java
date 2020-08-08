package com.example.henshin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private String TAG = "CC";
    private Context context;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.Camera_open).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.Camera_open:
                Log.i(TAG,"open_Camera");
                Intent intent = new Intent();
                intent.setClass(MainActivity.this,Camera.class);
                startActivity(intent);
                break;
        }
    }
}