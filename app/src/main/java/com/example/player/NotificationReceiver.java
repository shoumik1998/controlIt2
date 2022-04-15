package com.example.player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class NotificationReceiver extends BroadcastReceiver {
    public static final String ACTION_NEXT="NEXT";
    public static final String ACTION_PLAY="PLAY";
    public static final String ACTION_PREV="PREVIOUS";

    ActionPlaying actionPlaying;

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent1=new Intent(context,MusicService.class);
        if (intent.getAction()!=null) {
            switch (intent.getAction()){
                case ACTION_PLAY:
                    //Toast.makeText(context, "Play", Toast.LENGTH_SHORT).show();
                    intent1.putExtra("myActionName",intent.getAction());
                    context.startService(intent1);


                    break;

                case ACTION_PREV:
                    //Toast.makeText(context, "Previous", Toast.LENGTH_SHORT).show();
                    intent1.putExtra("myActionName",intent.getAction());
                    context.startService(intent1);
                    break;
                case ACTION_NEXT:
                    //Toast.makeText(context, "next", Toast.LENGTH_SHORT).show();
                    intent1.putExtra("myActionName",intent.getAction());
                    context.startService(intent1);
                    break;
            }
        }

    }
}
