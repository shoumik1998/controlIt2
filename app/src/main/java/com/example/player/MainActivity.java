package com.example.player;

import static com.example.player.ApplicationClass.ACTION_DISMISS;
import static com.example.player.ApplicationClass.ACTION_PLAY;
import static com.example.player.ApplicationClass.CHANNEL_ID_2;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.agrawalsuneet.dotsloader.loaders.TashieLoader;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity implements ActionPlaying , ServiceConnection , ConnectedDeviceInterface {

    ImageView  playPause,connectedDeviceImg;
    TextView titleTxt;
    int position;
    boolean isPlaying=false;
    VoiceService voiceService;
    MediaSessionCompat mediaSessionCompat;
    SpeechRecognizer speechRecognizerl;
    Intent spechrecognzerIntent;
    TashieLoader tashieLoader;
    Intent intent;
    BluetoothAdapter adapter;
    String MACaddress="";
    MainActivity activity;
    UUID myUUID;
    BluetoothDevice device;
    BluetoothSocket socket;
    OutputStream outputStream;
    String message="";
    boolean status=false;


    ArrayList<TrackFiles> trackFilesArrayList=new ArrayList<>();

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "Granted...", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Denayed...", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = MainActivity.this;
        adapter=BluetoothAdapter.getDefaultAdapter();
        myUUID=UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        Paper.init(this);
        if (Paper.book().contains("mcaddress")) {
            if (Paper.book().read("mcaddress") == "") {

            }
        } else {
            Paper.book().write("mcaddress", "");

        }

        intent=new Intent(getApplicationContext(),VoiceService.class);

        if (socket == null) {
            if (adapter.isEnabled()) {
                socketConnection();


            } else {
                adapter.enable();
                socketConnection();

            }
        }






        playPause=findViewById(R.id.playpause);
        tashieLoader=findViewById(R.id.tashieLoaderID);
        titleTxt=findViewById(R.id.titleTxt);
        connectedDeviceImg = findViewById(R.id.CDimgID);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},1);
        }

        speechRecognizerl=SpeechRecognizer.createSpeechRecognizer(this);
         spechrecognzerIntent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);



        mediaSessionCompat=new MediaSessionCompat(this,"PlayerAudio");




        playPause.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                playClicked();

                if (status == false) {
                    intent.putExtra("myActionName", "START_R");
                    ContextCompat.startForegroundService(MainActivity.this,intent);
                    voiceService.status=true;
                    status = true;
                }


            }
        });

        connectedDeviceImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,ConnectedDevice.class));
            }
        });


        speechRecognizerl.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {
                tashieLoader.setVisibility(View.INVISIBLE);
                showNotification(R.drawable.mic_24);
                isPlaying=true;



            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                String data = bundle.getStringArrayList(speechRecognizerl.RESULTS_RECOGNITION).get(0);
                message=String.format("*%s#", data);
                titleTxt.setText(data);
                try {
                    if (outputStream != null) {
                        outputStream.write(message.getBytes());
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });



    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent=new Intent(this, VoiceService.class);
        bindService(intent,this,BIND_AUTO_CREATE);

    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        unbindService(this);
//    }







    @Override
    public void playClicked() {
        //if (!isPlaying) {
            isPlaying=true;
            speechRecognizerl.startListening(spechrecognzerIntent);
            playPause.setImageResource(R.drawable.mic1);
            showNotification(R.drawable.mic_24);
            tashieLoader.setVisibility(View.VISIBLE);





//        }else {
//            isPlaying=false;
//            speechRecognizerl.stopListening();
//            playPause.setImageResource(R.drawable.mic1);
//            showNotification(R.drawable.mic_off_24);
//            tashieLoader.setVisibility(View.GONE);
//            Toast.makeText(MainActivity.this, "Pause", Toast.LENGTH_SHORT).show();
//
//        }

    }

    @Override
    public void playClickedNotification() {
        isPlaying=true;
        speechRecognizerl.startListening(spechrecognzerIntent);
        playPause.setImageResource(R.drawable.mic1);
        showNotification(R.drawable.mic_off_24);
        tashieLoader.setVisibility(View.VISIBLE);

    }

    @Override
    public void closeService() {
        voiceService.stopForeground(true);
        try {

                socket.close();
                Log.i("socket ", "Socket closed");


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void test(int number) {
        titleTxt.setText(String.valueOf(number));

    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        VoiceService.MyBinder binder= (VoiceService.MyBinder) iBinder;
        voiceService =binder.getService();
        voiceService.setCallBack(MainActivity.this);
        voiceService.setCallBackBluetooth(MainActivity.this);

        //titleTxt.setText(musicService.getRand());



    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        voiceService =null;

    }

    public  void  showNotification(int playpauseBtn){
        Intent intent =new Intent(this,MainActivity.class);
        PendingIntent contentinten=PendingIntent.getActivity(this,0,intent,0);



        Intent playintent=new Intent(this,NotificationReceiver.class)
                .setAction(ACTION_PLAY);
        PendingIntent playpendingIntent=PendingIntent.getBroadcast(this,0,playintent,
                PendingIntent.FLAG_UPDATE_CURRENT);

Intent closeintent=new Intent(this,NotificationReceiver.class)
                .setAction(ACTION_DISMISS);
        PendingIntent closependingIntent=PendingIntent.getBroadcast(this,0,closeintent,
                PendingIntent.FLAG_UPDATE_CURRENT);



        Bitmap picture= BitmapFactory.decodeResource(getResources(),
                R.drawable.arduino);

        Notification notification=new NotificationCompat.Builder(this,CHANNEL_ID_2)
                .setSmallIcon(R.drawable.arduino)
                .setLargeIcon(picture)
                .setContentTitle("Control It")
                .setContentText("Control your equipments")
                .setColor(getResources().getColor(R.color.black))
                .addAction(playpauseBtn,"Play",playpendingIntent)
                .addAction(R.drawable.close,"Dismiss",closependingIntent)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSessionCompat.getSessionToken()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(contentinten)
                .setOnlyAlertOnce(true)
                .build();

//        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        notificationManager.notify(0,notification);

        voiceService.startForeground(1,notification);






    }

    @Override
    public void discoverDevices() {
        Toast.makeText(MainActivity.this, "hmm ok", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void pairDevices() {
        Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals("HC-05")) {
                    MACaddress = device.getAddress();
                    Paper.book().write("mcaddress",MACaddress);
                }
            }
        }

    }

    @Override
    public void connectDevices() {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device.getName().equals("HC-05")) {
                        MACaddress = device.getAddress();
                        Paper.book().write("mcaddress",MACaddress);
                        adapter.cancelDiscovery();
                    }
                }
            }
        };
        activity.registerReceiver(broadcastReceiver, filter);
        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        /* Permission for Bluetooth search */
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        adapter.startDiscovery();

    }

    @Override
    public void socketConnection() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                connectDevices();
                pairDevices();
                if (MACaddress != "") {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            titleTxt.setText(MACaddress);
                        }
                    });

                    device = adapter.getRemoteDevice(MACaddress);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                socket = device.createRfcommSocketToServiceRecord(myUUID);
                                if (!socket.isConnected()) {
                                    socket.connect();
                                }

                                if (socket.isConnected()) {
                                     outputStream = socket.getOutputStream();
                                    Log.i("Socket", "Socket connected");

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            titleTxt.setText("socket is  connected");
                                        }
                                    });

                                }else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            titleTxt.setText("socket is not connected");
                                        }
                                    });
                                }
                            } catch (IOException e) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        titleTxt.setText(e.toString());
                                    }
                                });

                            }
                        }
                    }).start();

                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getBaseContext(), "device not found", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        titleTxt.setText(MACaddress);
                    }
                });
            }
        }).start();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
       // if (status==false) {
            if (socket.isConnected() || socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
       // }
    }
}