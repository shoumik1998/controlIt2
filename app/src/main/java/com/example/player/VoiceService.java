package com.example.player;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.util.Random;

public class VoiceService extends Service {
    private  IBinder mBinder= new MyBinder();


    public static final String ACTION_PLAY="PLAY";


    ActionPlaying actionPlaying;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public  class MyBinder extends Binder{
        VoiceService getService(){
            return  VoiceService.this;
        }

    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String actionName=intent.getStringExtra("myActionName");
        if (actionName != null) {
            switch (actionName){
                case ACTION_PLAY:
                    if (actionPlaying != null) {
                        actionPlaying.playClickedNotification();
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
