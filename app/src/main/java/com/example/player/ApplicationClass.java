package com.example.player;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class ApplicationClass extends Application {

    public static final String CHANNEL_ID ="channel";
    public static final String ACTION_PLAY="PLAY";
    public static final String ACTION_DISMISS="DISMISS";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }
    private  void createNotificationChannel(){

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel=new NotificationChannel(CHANNEL_ID,
                    "channel",NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription(" Listening from notification");
            NotificationManager notificationManager=getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);

        }
    }
}
