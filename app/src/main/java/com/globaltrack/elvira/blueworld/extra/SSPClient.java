package com.globaltrack.elvira.blueworld.extra;

import android.bluetooth.BluetoothSocket;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ELVIRA III on 2017/03/16.
 */

public class SSPClient
{
    BlueSocket blueSocket;
    private Thread clientThread;

    private boolean mustExit=false;

    ISSPListenner listenner;

    public void setListener(ISSPListenner toAdd) {
        listenner = toAdd;
    }

    public SSPClient(BluetoothSocket socket) {
        this.blueSocket = new BlueSocket(socket);
    }

    public void startClientThread()
    {
        if (clientThread != null) {
            clientThread.interrupt();
        }
        clientThread = this.createClientThread();
        clientThread.start();
    }

    public void stopClientThread(boolean force)
    {
        this.mustExit = true;
        if (clientThread != null && force)
        {
            clientThread.interrupt();
        }
    }

    private  void  postToListenners(byte[] bytes)
    {
        if (listenner!=null) {
            listenner.OnSSPPost(this, bytes);
        }
    }

    private Thread createClientThread()
    {
        Thread t = new Thread() {
            public void run() {
                // Keep listening until exception occurs or a socket is returned.
                while (true)
                {
                    byte []bytes= blueSocket.readData();
                    if (bytes!=null)
                    {
                        postToListenners(bytes);
                    }
                    else
                    {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    if (mustExit)
                        break;;
                }
            }
        };

        return t;
    }
}
