package com.example.player;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

public class ApplicationClass extends Application {

    public static final String CHANNEL_ID_1="channel1";
    public static final String CHANNEL_ID_2="channel2";
    public static final String ACTION_NEXT="NEXT";
    public static final String ACTION_PLAY="PLAY";
    public static final String ACTION_DISMISS="DISMISS";
    public static final String ACTION_PREV="PREVIOUS";



    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }



    private  void createNotificationChannel(){

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
//

            NotificationChannel notificationChannel2=new NotificationChannel(CHANNEL_ID_2,
                    "channel (2)",NotificationManager.IMPORTANCE_HIGH);
            notificationChannel2.setDescription(" channel 2 description");

            NotificationManager notificationManager=getSystemService(NotificationManager.class);

            notificationManager.createNotificationChannel(notificationChannel2);
            Toast.makeText(getApplicationContext(), "Channel created", Toast.LENGTH_SHORT).show();

        }
    }
}
