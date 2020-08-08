package com.example.henshin;

import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

public class musicService extends Service {

    public MediaPlayer mediaPlayer;
    public MediaPlayer mediaPlayer_bgm;
    public String[] paths;
    public boolean isonDestry = true;

    public musicService(String[] path) {
        paths = path;
        if(!path[0].equals("null")){
            Log.i("Touch","找到bgm");
            try {
                mediaPlayer_bgm = new MediaPlayer();
                mediaPlayer = new MediaPlayer();
                mediaPlayer_bgm.setAudioStreamType(AudioManager.STREAM_MUSIC);

                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

                mediaPlayer_bgm.setVolume(0.5f, 0.5f);
                mediaPlayer_bgm.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        if(mediaPlayer.isPlaying()){
                            mediaPlayer.stop();
                            mediaPlayer.release();
                            mediaPlayer = new MediaPlayer();
                            try {
                                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                mediaPlayer.setDataSource(paths[2]);
                                mediaPlayer.prepare();
                                mediaPlayer.start();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                // 指定播放的路径
                mediaPlayer.setDataSource(paths[1]);
                mediaPlayer_bgm.setDataSource(paths[0]);

                // 准备播放
                mediaPlayer_bgm.prepare();
                mediaPlayer.prepare();
                mediaPlayer_bgm.start();
                try {
                    Thread.sleep(1500);
                    mediaPlayer.start();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            mediaPlayer_bgm = new MediaPlayer();
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setDataSource(paths[1]);
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    public void Henshin(){
        if(mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
//        if(mediaPlayer_bgm.isPlaying()){
//            mediaPlayer_bgm.setVolume(0.2f, 0.2f);
//        }
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(paths[2]);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    onDestroy();
                }
            });
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onDestroy(){
        super.onDestroy();
        if(mediaPlayer_bgm.isPlaying()){
            mediaPlayer_bgm.stop();
        }
        if(mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
        mediaPlayer.release();
        mediaPlayer_bgm.release();
        isonDestry = false;
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
