package com.globaltrack.elvira.blueworld.extra;

import android.bluetooth.BluetoothDevice;

/**
 * Created by ELVIRA III on 2017/03/14.
 */

public class BluetoothDeviceItem {

    public BluetoothDeviceItem(BluetoothDevice device) {
        this.device = device;
    }

    public BluetoothDevice getDevice() {
        return this.device;
    }

    private BluetoothDevice device;

    @Override
    public String toString()
    {
        String msg = device.getName() + " - " + device.getAddress();
        if(device.getBondState()== BluetoothDevice.BOND_BONDED)
        {
            msg = msg+" (paired)";
        }

        return msg;
    }
}
