package com.example.player;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class ApplicationClass extends Application {

    public static final String CHANNEL_ID_1="channel1";
    public static final String CHANNEL_ID_2="channel2";
    public static final String ACTION_NEXT="NEXT";
    public static final String ACTION_PLAY="PLAY";
    public static final String ACTION_PREV="PREVIOUS";


    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }


    private  void createNotificationChannel(){
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel1=new NotificationChannel(CHANNEL_ID_1,
                    "channel (1)", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel1.setDescription("channel 1 Description");

            NotificationChannel notificationChannel2=new NotificationChannel(CHANNEL_ID_2,
                    "channel (2)",NotificationManager.IMPORTANCE_HIGH);
            notificationChannel2.setDescription(" channel 2 description");

            NotificationManager notificationManager=getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel1);
            notificationManager.createNotificationChannel(notificationChannel2);
        }
    }
}
