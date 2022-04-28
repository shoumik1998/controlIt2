package com.example.player;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import io.paperdb.Paper;

public class ConnectedDevice extends AppCompatActivity implements ConnectedDeviceInterface, ServiceConnection {

    TextView statetxt;
    BluetoothAdapter adapter;
    String MACaddress="";
    ConnectedDevice activity;
    UUID myUUID;
    BluetoothDevice device;
    BluetoothSocket socket;
    VoiceService voiceService;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected_device);
        Paper.init(this);
        myUUID=UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        statetxt=findViewById(R.id.stateID);
        activity=ConnectedDevice.this;
        adapter=BluetoothAdapter.getDefaultAdapter();



        if (adapter.isEnabled()) {
            socketConnection();

        } else {
            adapter.enable();
            socketConnection();
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (  socket!=null) {
                if (socket.isConnected()) {
                    socket.close();
                }


            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public  void  getPairedDevices(){
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
    public  void  getAvailableDevices(){
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
    public void socketConnection(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                getAvailableDevices();
                getPairedDevices();
                if (MACaddress != "") {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            statetxt.setText(MACaddress);
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
                                   // outputStream = socket.getOutputStream();
                                    Log.i("Socket", "Socket connected");

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            statetxt.setText("socket is  connected");
                                        }
                                    });

                                }else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            statetxt.setText("socket is not connected");
                                        }
                                    });
                                }
                            } catch (IOException e) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

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
                        statetxt.setText(MACaddress);
                    }
                });
            }
        }).start();
    }

    @Override
    public void stayConnection() {

    }


    @Override
    public void discoverDevices() {
        Toast.makeText(getApplicationContext(), "triggered connected", Toast.LENGTH_SHORT).show();


    }

    @Override
    public void pairDevices() {

    }

    @Override
    public void connectDevices() {

    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        VoiceService.MyBinder binder= (VoiceService.MyBinder) iBinder;
        voiceService =binder.getService();
        voiceService.setCallBackBluetooth(ConnectedDevice.this);

    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        voiceService = null;

    }
}