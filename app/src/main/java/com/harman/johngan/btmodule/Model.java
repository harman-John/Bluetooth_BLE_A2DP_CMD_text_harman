package com.harman.johngan.btmodule;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import com.harman.johngan.R;
import com.harman.johngan.constants.Constants;
import com.harman.johngan.entity.DeviceItem;
import com.harman.johngan.listener.ModelLisener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Johngan on 22/05/2017.
 */

public class Model {
    private final String TAG= Constants.PRETAG+"Model";
    private BluetoothGatt mgatt = null;
    private BluetoothDevice bluetoothDevice;
    private Context  mContext;
    private List<ModelLisener> listListener;
    private DeviceItem mDeviceItem;

    public Model(Context context){
        mContext = context;
        if (mDeviceItem == null){
            mDeviceItem = new DeviceItem();
        }
    }

    public void connectGatt(DeviceItem deviceItem) {
        if (mgatt == null) {
            mDeviceItem = deviceItem;
            mgatt = deviceItem.btDevice.connectGatt(mContext, false, gattCallback, BluetoothDevice.TRANSPORT_LE);
            if (mgatt != null) {
                Log.i(TAG, "connectGatt macaddress = " + deviceItem.btDevice.getAddress());
                mgatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);
            } else {
                Log.i(TAG, "I am not connected to GATT, macaddress = " + deviceItem.btDevice.getAddress());
            }
            //scanLeDevice(false);// will stop after first device detection
        }
    }

    public DeviceItem getDeviceItem(){
        return mDeviceItem;
    }

    public void requestMTU(){
        if (mgatt != null) {
            Log.i(TAG, "gatt connect");
            mgatt.requestMtu(517);
        }
    }

    public void connect(){
        if (mgatt != null) {
            Log.i(TAG, "gatt connect");
            mgatt.connect();
        }
    }

    public void disConnect(){
        if (mgatt != null) {
            Log.i(TAG, "gatt disconnect");
            mgatt.disconnect();
        }
    }

    public void closeGatt(){
        if (mgatt != null){
            mgatt.close();
            mgatt = null;
        }
    }

    private int brightness = -1;
    public int getBrightness(){
        return brightness;
    }

    public void addLisener(ModelLisener l){
        if (l == null) {
            return;
        }
        if (listListener ==  null){
            listListener = new ArrayList<>();
        }
        if (!listListener.contains(l)){
            listListener.add(l);
        }

    }

    public void removeLisener(ModelLisener l){
        if (l == null){
            return;
        }
        if (listListener == null || listListener.size() < 1){
            return;
        }
        listListener.remove(l);
    }

    public void sendCmd(final byte[] msg){
        new Thread(new Runnable() {
            @Override
            public void run() {

                if (mWriteCharacteristic != null) {
                    boolean isValueSet = mWriteCharacteristic.setValue(msg);
                    if (isValueSet) {
                        if (mgatt == null){
                            Log.i(TAG,"mGatt is null");
                            return;
                        }
                        boolean ret = mgatt.writeCharacteristic(mWriteCharacteristic);
                        mgatt.setCharacteristicNotification(mWriteCharacteristic, true);
                        Log.i(TAG,"writeCharacteristic msg = "+ Commands.bytesToHexString(msg)+", ret="+ret);
                    } else {
                        Log.i(TAG, " writeCharacteristic() sending packet : set Value is false");
                    }
                }
            }
        }).start();
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i(TAG," <-------onConnectionStateChange ------>Status = " + status+",newState = "+newState+",mac="+gatt.getDevice().getAddress());

            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    mDeviceItem.connectStr = mContext.getString(R.string.connected);
                    Log.i(TAG, "STATE_CONNECTED");
                    gatt.discoverServices();
                    for (int i= 0;i<listListener.size();i++){
                        listListener.get(i).onStateConnected(gatt.getDevice());
                    }
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    mDeviceItem.connectStr = mContext.getString(R.string.disconnected);
                    disConnect();
                    closeGatt();
                    for (int i= 0;i<listListener.size();i++){
                        listListener.get(i).onStateDisconnected(gatt.getDevice());
                    }
                    Log.e(TAG, "STATE_DISCONNECTED");
                    break;
                default:
                    Log.e(TAG, "STATE_OTHER");
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            List<BluetoothGattService> services = gatt.getServices();
            Log.i(TAG,"<------onServicesDiscovered------>addr = "+gatt.getDevice().getAddress());
            for (int i = 0; i < services.size(); i++) {
                Log.i(TAG, i+",UUID = " + services.get(i).getUuid());
                List<BluetoothGattCharacteristic> characteristics = services.get(i).getCharacteristics();
                for(BluetoothGattCharacteristic cha: characteristics) {
                    Log.i(TAG, "      Characteric = " + cha.getUuid());
                    Log.i(TAG, "      Characteric = " + cha.getProperties());
                }
            }
            for (int i= 0;i<listListener.size();i++){
                listListener.get(i).onServicesDiscovered(gatt.getDevice());
            }
            //gatt.readCharacteristic(services.get(1).getCharacteristics().get(1));
            if (status == BluetoothGatt.GATT_SUCCESS) {
                setWriteControl(mgatt);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic
                                                 characteristic, int status) {
            super.onCharacteristicRead(gatt,characteristic,status);
            if (characteristic != null) {
                Log.i(TAG, "<------onCharacteristicRead------> uuid= " + characteristic.getUuid());
                byte[] msgs = characteristic.getValue();
                if (msgs.length> 0){
                    Log.i(TAG, "onCharacteristicRead value= " + Commands.bytesToHexString(msgs));
                }
            }
            //gatt.disconnect();
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            if (characteristic != null){
                byte[] msgs = characteristic.getValue();
                if (msgs.length> 0){
                    String cmd = Commands.bytesToHexString(msgs);
                    Log.i(TAG,"<------onCharacteristicChanged------> addr = "+gatt.getDevice().getAddress()+",cmd ="+cmd);
                    for (int i= 0;i<listListener.size();i++){
                        listListener.get(i).onCharacteristicChanged(cmd);
                    }
                }
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);

            String cmdStr = Commands.bytesToHexString(characteristic.getValue());
            Log.i(TAG,"<------onCharacteristicWrite------> status ="+status+",addr = "+gatt.getDevice().getAddress() +",value ="+cmdStr);
            for (int i= 0;i<listListener.size();i++){
                listListener.get(i).onCharacteristicWrite(cmdStr);
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            Log.i(TAG,"<------onMtuChanged------> mtu="+mtu+",addr = "+gatt.getDevice().getAddress());
        }
    };

    private void setWriteControl(BluetoothGatt gatt){
        BluetoothGattService bluetoothGaPService = gatt.getService(UUID.fromString(Constants.GAP_UUID));

        BluetoothGattCharacteristic manufactureDataCharacteristic = bluetoothGaPService.getCharacteristic(UUID.fromString(Constants.MANUFACTURE_SPECIFIC_DATA_CHAR_UUID));
        if (manufactureDataCharacteristic != null) {
            Log.i(TAG, " manufactureDataCharacteristic = "+manufactureDataCharacteristic.toString());
            gatt.readCharacteristic(manufactureDataCharacteristic);
        }

        BluetoothGattService bluetoothRxTxService = gatt.getService(UUID.fromString(Constants.BLE_RX_TX_UUID));

        BluetoothGattCharacteristic readCharacteristic = bluetoothRxTxService.getCharacteristic(UUID.fromString(Constants.RX_UUID));
        gatt.readCharacteristic(readCharacteristic);
        gatt.setCharacteristicNotification(readCharacteristic,true);
        mWriteCharacteristic = bluetoothRxTxService.getCharacteristic(UUID.fromString(Constants.TX_UUID));

        //sendCmd(Utils.REQ_PULSE3_SET_LED_BRIGHTNESS);
    }

    private BluetoothGattCharacteristic mWriteCharacteristic;
}
