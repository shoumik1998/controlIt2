package com.example.player;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.Random;

public class MusicService extends Service {
    private  IBinder mBinder= new MyBinder();

    public static final String ACTION_NEXT="NEXT";
    public static final String ACTION_PLAY="PLAY";
    public static final String ACTION_PREV="PREVIOUS";

    ActionPlaying actionPlaying;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public  class MyBinder extends Binder{
        MusicService getService(){
            return  MusicService.this;
        }

    }

    public  int getRand(){
        return  new Random().nextInt();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String actionName=intent.getStringExtra("myActionName");
        if (actionName != null) {
            switch (actionName){
                case ACTION_PLAY:
                    if (actionPlaying != null) {
                        actionPlaying.playClicked();
                    }

                    break;

                case ACTION_PREV:
                    if (actionPlaying != null) {
                        actionPlaying.prevClicked();
                    }

                    break;
                case ACTION_NEXT:
                    if (actionPlaying != null) {
                        actionPlaying.nextClicked();
                    }

                    break;
            }
        }


        return START_STICKY;
    }

    public  void  setCallBack(ActionPlaying actionPlaying){
        this.actionPlaying=actionPlaying;

    }
}
