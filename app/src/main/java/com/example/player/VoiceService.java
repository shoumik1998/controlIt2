package com.example.player;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.Random;

public class VoiceService extends Service {
    private  IBinder mBinder= new MyBinder();
    boolean status;

    public static final String ACTION_PLAY="PLAY";
    public static final String ACTION_DISMISS="DISMISS";


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
                        //actionPlaying.closeService();
                        status=false;
                        stopForeground(true);
                        stopService(intent);

                    }
                    break;
                    case "START_R":
                    if (actionPlaying != null) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                while (status) {
                                    try {

                                        Thread.sleep(1000);
                                        if (status) {
                                            int number=new Random().nextInt(100)+0;
                                            Log.i("Random number", String.valueOf(number));
                                        }

                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }


                                }
                            }
                        }).start();

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
