package com.example.player;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.Random;

public class VoiceService extends Service {
    private  IBinder mBinder= new MyBinder();
    boolean status=false;

    public static final String ACTION_PLAY="PLAY";
    public static final String ACTION_DISMISS="DISMISS";
    ConnectedDeviceInterface deviceInterface;
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
    public void onCreate() {
        super.onCreate();
        status=true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        status=false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String actionName=intent.getStringExtra("myActionName");
        if (actionName != null) {
            switch (actionName){
                case ACTION_PLAY:
                    if (actionPlaying != null) {
                        actionPlaying.playClickedNotification();
                        status=true;

                    }
                    break;
                case ACTION_DISMISS:
                    if (actionPlaying != null) {
                        actionPlaying.closeService();
//                        status=false;
//                        stopForeground(true);
//                        stopService(intent);

                    }
                    break;


            }
        }
        return START_STICKY;
    }

    public  void  setCallBack(ActionPlaying actionPlaying){
        this.actionPlaying=actionPlaying;
    }

    public  void  setCallBackBluetooth(ConnectedDeviceInterface connectedDeviceInterface){
        deviceInterface = connectedDeviceInterface;
    }

}
