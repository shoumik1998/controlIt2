package com.example.player;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import io.paperdb.Paper;

public class ConnectedDevice extends AppCompatActivity implements ConnectedDeviceInterface {

    TextView statetxt;
    BluetoothAdapter adapter;
    String MACaddress="";
    ConnectedDevice activity;
    UUID myUUID;
    BluetoothDevice device;
    BluetoothSocket socket;



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
            threadRun();

        } else {
            adapter.enable();
            threadRun();
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (socket.isConnected() || socket!=null) {
                socket.close();

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
    public void  threadRun(){
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
                                        statetxt.setText(e.toString());
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
    public void discoverDevices() {


    }

    @Override
    public void pairDevices() {

    }

    @Override
    public void connectDevices() {

    }
}