package com.example.player;

import static com.example.player.ApplicationClass.ACTION_DISMISS;
import static com.example.player.ApplicationClass.ACTION_PLAY;
import static com.example.player.ApplicationClass.CHANNEL_ID;

import android.Manifest;
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
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.agrawalsuneet.dotsloader.loaders.TashieLoader;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity implements ActionPlaying, ServiceConnection, ConnectedDeviceInterface {

    public static UUID myUUID;
    public static BluetoothDevice device;
    public static BluetoothSocket socket;
    public static OutputStream outputStream;
    ImageView playPause;
    TextView titleTxt;
    int position;
    boolean isPlaying = false;
    VoiceService voiceService;
    MediaSessionCompat mediaSessionCompat;
    SpeechRecognizer speechRecognizerl = null;
    Intent spechrecognzerIntent;
    TashieLoader tashieLoader;
    ProgressBar progressBar;
    Intent intent;
    BluetoothAdapter adapter;
    String MACaddress = "";
    MainActivity activity;
    String message = "";
    boolean status = false;


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = MainActivity.this;
        adapter = BluetoothAdapter.getDefaultAdapter();
        myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        Paper.init(MainActivity.this);
        if (Paper.book().contains("mcaddress")) {
            if (Paper.book().read("mcaddress") == "") {
            }
        } else {
            Paper.book().write("mcaddress", "");
        }
        if (Paper.book().contains("serviceOn")) {
            if (Paper.book().read("serviceOn").equals("") || Paper.book().read("serviceOn").equals(null)) {
                Paper.book().write("serviceOn", "off");
            } else {
            }
        } else {
            Paper.book().write("serviceOn", "off");
        }

        intent = new Intent(getApplicationContext(), VoiceService.class);

        if (Paper.book().read("serviceOn").equals("off")) {
            if (socket == null) {
                if (adapter.isEnabled()) {
                    socketConnection();
                } else {
                    adapter.enable();
                    socketConnection();
                }
            }
        }

        playPause = findViewById(R.id.playpause);
        tashieLoader = findViewById(R.id.tashieLoaderID);
        progressBar = findViewById(R.id.progressBarID);
        titleTxt = findViewById(R.id.titleTxt);
        progressBar.setVisibility(View.VISIBLE);

        titleTxt.setText("Device is being connected");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }

        speechRecognizerl = SpeechRecognizer.createSpeechRecognizer(MainActivity.this);
        spechrecognzerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        mediaSessionCompat = new MediaSessionCompat(this, "PlayerAudio");

        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Paper.book().read("serviceOn").equals("off")) {
                    intent.putExtra("myActionName", "START_R");
                    ContextCompat.startForegroundService(MainActivity.this, intent);
                    Paper.book().write("serviceOn", "on");
                }
                playClicked();
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
                isPlaying = false;
            }

            @Override
            public void onError(int i) {
                tashieLoader.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onResults(Bundle bundle) {
                String data = bundle.getStringArrayList(speechRecognizerl.RESULTS_RECOGNITION).get(0);
                message = String.format("*%s#", data);
                Toast.makeText(getApplicationContext(), data, Toast.LENGTH_SHORT).show();
                try {
                    if (outputStream != null) {
                        outputStream.write(message.getBytes());
                    }
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, "Data can not be sent, please try again", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onPartialResults(Bundle bundle) {
            }

            @Override
            public void onEvent(int i, Bundle bundle) {
            }
        });
        if (socket != null) {
            if (socket.isConnected()) {
                progressBar.setVisibility(View.INVISIBLE);
                titleTxt.setText("device connected");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, VoiceService.class);
        bindService(intent, this, BIND_AUTO_CREATE);

    }


    @Override
    public void playClicked() {
        speechRecognizerl.startListening(spechrecognzerIntent);
        isPlaying = true;
        tashieLoader.setVisibility(View.VISIBLE);
        playPause.setImageResource(R.drawable.mic1);
        showNotification(R.drawable.mic_24);
    }

    @Override
    public void playClickedNotification() {
        speechRecognizerl.startListening(spechrecognzerIntent);
        isPlaying = true;
        playPause.setImageResource(R.drawable.mic1);
        showNotification(R.drawable.mic_off_24);
        tashieLoader.setVisibility(View.VISIBLE);
        Toast.makeText(getApplicationContext(), "notification", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void closeService() {
        Paper.book().write("serviceOn", "off");
        if (socket != null) {
            if (socket.isConnected()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        voiceService.stopForeground(true);
        myUUID = null;
        device = null;
        socket = null;
        outputStream = null;
        finish();
    }


    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        VoiceService.MyBinder binder = (VoiceService.MyBinder) iBinder;
        voiceService = binder.getService();
        voiceService.setCallBack(MainActivity.this);
        voiceService.setCallBackBluetooth(MainActivity.this);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        voiceService = null;
    }

    public void showNotification(int playpauseBtn) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent contentinten = PendingIntent.getActivity(this, 0, intent, 0);
        Intent playintent = new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_PLAY);
        PendingIntent playpendingIntent = PendingIntent.getBroadcast(this, 1, playintent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Intent closeintent = new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_DISMISS);
        PendingIntent closependingIntent = PendingIntent.getBroadcast(this, 0, closeintent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Bitmap picture = BitmapFactory.decodeResource(getResources(),
                R.drawable.arduino);

        NotificationCompat.Builder notificationBldr = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBldr = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                    .setSmallIcon(R.drawable.arduino)
                    .setLargeIcon(picture)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setContentTitle("Control Your Home  E-equipments")
                    .setColor(getResources().getColor(R.color.black))
                    .addAction(playpauseBtn, "Listen", playpendingIntent)
                    .addAction(R.drawable.close, "Dismiss", closependingIntent)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(contentinten)
                    .setAutoCancel(true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            voiceService.startForeground(1, notificationBldr.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE);
        } else {
            voiceService.startForeground(1,notificationBldr.build());
        }

    }


    @Override
    public void pairedDevices() {
        Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals("HC-05")) {
                    MACaddress = device.getAddress();
                    Paper.book().write("mcaddress", MACaddress);
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
                        Paper.book().write("mcaddress", MACaddress);
                        adapter.cancelDiscovery();
                    }
                }
            }
        };
        activity.registerReceiver(broadcastReceiver, filter);
        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
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
                pairedDevices();
                if (MACaddress != "") {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            titleTxt.setText("Device is being connected");
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
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            titleTxt.setText("device  connected");
                                            progressBar.setVisibility(View.INVISIBLE);
                                        }
                                    });
                                } else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            titleTxt.setText("Device is not connected");
                                            progressBar.setVisibility(View.INVISIBLE);
                                        }
                                    });
                                }
                            } catch (IOException e) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        titleTxt.setText("Failed to connect. Get back and try again");
                                        progressBar.setVisibility(View.INVISIBLE);
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
            }
        }).start();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (Paper.book().read("serviceOn").equals("off")) {
            if (socket != null) {
                if (socket.isConnected()) {
                    try {
                        socket.close();
                        myUUID = null;
                        device = null;
                        socket = null;
                        outputStream = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }
}