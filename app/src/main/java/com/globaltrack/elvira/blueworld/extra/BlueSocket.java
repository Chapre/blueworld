package com.globaltrack.elvira.blueworld.extra;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

import static android.content.ContentValues.TAG;

/**
 * Created by ELVIRA III on 2017/03/15.
 */

public class BlueSocket
{
    BluetoothSocket socket;

    InputStream streamIn;

    public BlueSocket(BluetoothSocket socket) {
        this.socket = socket;
        try {
            streamIn = socket.getInputStream();
        } catch (IOException e) {
            streamIn=null;
            e.printStackTrace();
        }
    }

    public BluetoothSocket getSocket() {
        return socket;
    }

    public int byteToRead() {
        int count = 0;
        try {
            count = streamIn.available();
        } catch (IOException e) {
            e.printStackTrace();
            count = 0;
        }

        return count;
    }

    public byte[] readData() {
        byte[] bytes = null;
        int count = byteToRead();

        if (count > 0) {
            bytes = new byte[count];
            try {
                streamIn.read(bytes, 0, bytes.length);
                Log.i(TAG, new String(bytes));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return bytes;
    }

    public void update() {

    }
}
