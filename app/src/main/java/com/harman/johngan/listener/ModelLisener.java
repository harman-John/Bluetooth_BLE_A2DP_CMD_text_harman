package com.harman.johngan.listener;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Johngan on 24/05/2017.
 */

public interface ModelLisener {
    void onStateConnected(BluetoothDevice device);
    void onStateDisconnected(BluetoothDevice device);
    void onServicesDiscovered(BluetoothDevice device);

    void onCharacteristicWrite(String writeCmd);
    void onCharacteristicChanged(String receiveCmd);
}
