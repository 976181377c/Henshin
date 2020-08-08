package com.example.henshin;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Camera extends CameraActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = "myCamera";
    private static String TGA = "cc";

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };

    private JavaCameraView javaCameraView;
    private com.example.henshin.musicService musicService;
    private ImageView imageView;
    private Mat mat;
    private int myInt = -1;
    private String myInt2;
    private String[] paths ;
    private String path ;

    static {
        System.loadLibrary("native-lib");
    }//加载so库

    native int getInt(long mat_Addr);

    private BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    javaCameraView.enableView();
                }
                break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    //点击事件

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        List<CameraBridgeViewBase> list = new ArrayList<>();
        list.add(javaCameraView);
        return list;
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        //verifyStoragePermissions(this);

        imageView = findViewById(R.id.ImgView);

        musicService = null;

        javaCameraView = findViewById(R.id.CameraView);
        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
        javaCameraView.setCvCameraViewListener(this);

        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File file = getExternalFilesDir(null);
            path = file+"/";
        } else {
            Toast.makeText(Camera.this,"SD卡不可用，请检查SD卡",Toast.LENGTH_LONG).show();
        }

        imageView.setOnTouchListener(new View.OnTouchListener() {

            private float mPosX;
            private float mPosY;
            private float mCurrentPosX;
            private float mCurrentPosY;

            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    // 按下
                    case MotionEvent.ACTION_DOWN:
                        mPosX = event.getX();
                        mPosY = event.getY();
                        break;
                    // 移动
                    case MotionEvent.ACTION_MOVE:
                        mCurrentPosX = event.getX();
                        mCurrentPosY = event.getY();
                        if (mCurrentPosX - mPosX > 0 && Math.abs(mCurrentPosY - mPosY) < 10)
                            Log.i("c", "向右");
                        else if (mCurrentPosX - mPosX < 0 && Math.abs(mCurrentPosY - mPosY) < 10)
                            Log.i("", "向左");
                        else if (mCurrentPosY - mPosY > 0 && Math.abs(mCurrentPosX - mPosX) < 10)
                            Log.i("", "向下");
                        else if (mCurrentPosY - mPosY < 0 && Math.abs(mCurrentPosX - mPosX) < 10){
                            Log.i("", "向上");
                            javaCameraView.enableView();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    imageView.setImageBitmap(null);
                                }
                            });
                            musicService = null;
                        }
                        break;
                    // 拿起
                    case MotionEvent.ACTION_UP:
                        if(musicService!=null){
                            if (musicService.isonDestry ){
                                musicService.Henshin();
                            }
                        }

                        Log.i("Touch", "henshin");
                        break;
                    default:
                        break;
                }

                return true;
            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        if (javaCameraView != null)
            javaCameraView.disableView();
        if(musicService!=null){
            if(musicService.isonDestry){
                musicService.onDestroy();
            }
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, baseLoaderCallback);
        } else {
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mat = inputFrame.gray();
        Core.flip(mat,mat,-1);
        myInt = getInt(mat.getNativeObjAddr());

        if(myInt > 0){
            Log.i(TAG, "myInt = "+myInt);
            playMusic(myInt);
        }
        return mat;
    }

    private void playMusic(int num) {
        String i = String.valueOf(num);
        myInt2 = i;
        File f = new File(path+i);
        if(!f.exists()){
            Log.i(TAG, "未找到"+i);
            return;
        }
        paths = new String[3];
        paths[0] = path+i+"/bgm.mp3";
        f = new File(paths[0]);
        if(!f.exists()){
            Log.i(TGA,"未找到bgm");
            paths[0]="null";
        }
        paths[1] = path+i+"/1.mp3";
        paths[2] = path+i+"/2.mp3";
        if(new File(path+i+"/img.png").exists()){
            runOnUiThread(new Runnable() {
                public void run() {
                    String s =path+myInt2+"/img.png";
                    Bitmap bitmap= BitmapFactory.decodeFile(s);
                    Log.i(TAG, "run: "+myInt2+""+bitmap);
                    imageView.setImageBitmap(bitmap);

                }
            });
        }
        myInt = -1;
        musicService = new musicService(paths);
        new Thread(){
            @Override
            public void run() {
                super.run();
                javaCameraView.disableView();
            }
        }.start();
    }


    public static void verifyStoragePermissions(Activity activity) {

        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}