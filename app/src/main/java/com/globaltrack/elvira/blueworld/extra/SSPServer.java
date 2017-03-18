package com.globaltrack.elvira.blueworld.extra;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.globaltrack.elvira.blueworld.JohnActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static android.content.ContentValues.TAG;

/**
 * Created by ELVIRA III on 2017/03/15.
 */

public class SSPServer
{
    private  ISSPListenner listenner;
    private BluetoothAdapter bluetoothAdapter;

    public BluetoothServerSocket mmServerSocket;

    Thread acceptThread, socketThread;

    List<SSPClient> sockets=new ArrayList<SSPClient>();

    List<BluetoothDevice> boodedDevices=new ArrayList<BluetoothDevice>();

    public SSPServer(BluetoothAdapter bluetoothAdapter)
    {
        this.bluetoothAdapter = bluetoothAdapter;
    }

    public void setSSPListenner(ISSPListenner listenner) {
        this.listenner=listenner;
        for (SSPClient client : sockets)
            client.setListener(this.listenner);
    }

    private void enQueueSocket(BluetoothSocket socket) {
        SSPClient client = new SSPClient(socket);
        client.startClientThread();
        if (this.listenner != null) {
            client.setListener(this.listenner);
        }

        sockets.add(client);
    }

    private Thread CreateAcceptThread() {
        Thread t = new Thread() {
            public void run() {
                ConnectingToBondedDevice();
                BluetoothServerSocket tmp = null;
                int fails = 0;
                try {
                    // MY_UUID is the app's UUID string, also used by the client code.
                    tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord("Server", UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                } catch (IOException e) {
                    Log.e(TAG, "Socket's listen() method failed", e);
                }
                mmServerSocket = tmp;
                // Keep listening until exception occurs or a socket is returned.
                while (true) {
                    BluetoothSocket socket;
                    try
                    {
                        socket = mmServerSocket.accept(3000);
                    } catch (IOException e) {
                        Log.e(TAG, "Socket's accept() method failed", e);
                        fails++;
                        socket = null;
                    }

                    if (socket != null) {
                        Log.i(TAG, "SSP Socket was created");
                        enQueueSocket(socket);
                        try {
                            mmServerSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        break;
                    }
                    else {
                        if (fails % 10 == 0) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        };

        return t;
    }

    private void ConnectingToBondedDevice() {
        if (boodedDevices.size() > 0) {

            for (BluetoothDevice device :
                    boodedDevices
                    ) {
                BluetoothSocket socket = connectToDevice(device);
                if (socket != null) {
                    Log.i(TAG, "Connected to "+device.getName());
                    break;
                }
                else
                {
                    Log.i(TAG, "Could not connect to "+device.getName());
                }
            }
        }
    }


    private BluetoothSocket connectToDevice(BluetoothDevice device) {
        bluetoothAdapter.cancelDiscovery();
        BluetoothSocket socket;
        BluetoothSocket tmp = null;
        try {
            tmp = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
        }
        socket = tmp;
        // Cancel discovery because it otherwise slows down the connection.

        if (socket != null) {
            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                socket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    socket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }

                socket=null;
            }
        }

        return socket;
    }

    private void updateBondedDevices() {
        Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
        if (devices != null && devices.size() > 0) {
            boodedDevices.clear();
            boodedDevices.addAll(devices);
        }
    }

    public void startAccepting()
    {
        updateBondedDevices();
        if (acceptThread != null) {
            acceptThread.interrupt();
        }
        acceptThread = this.CreateAcceptThread();
        acceptThread.start();
    }
}
