package com.harman.johngan.activity;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.harman.johngan.R;
import com.harman.johngan.adpter.DeviceListAdapter;
import com.harman.johngan.application.MyApplication;
import com.harman.johngan.btmodule.Model;
import com.harman.johngan.constants.Constants;
import com.harman.johngan.entity.DeviceItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DiscoveryActivity extends AppCompatActivity {
    private final static String TAG = Constants.PRETAG+DiscoveryActivity.class.getSimpleName();
    private BluetoothAdapter mBluetoothAdapter;
    private int REQUEST_ENABLE_BT = 1;
    private MyHandler mHandler;
    private static final long SCAN_PERIOD = 10000;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private ListView mlistView = null;
    private List<DeviceItem> deviceList;
    private DeviceListAdapter deviceListAdapter;

    private final static int MSG_REDISCOVERY = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovery);
        Log.i(TAG,"onCreate");
        requestPermission();
        initData();
        initView();
        initBluetooth();
    }

    private void requestPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, Constants.PERMISSION_REQUEST_COARSE_LOCATION);
            }
            if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                Toast.makeText(this, "BLE Not Supported",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void initView(){
        mlistView = (ListView) findViewById(R.id.listview_device);
        deviceListAdapter =new DeviceListAdapter(this,deviceList);
        mlistView.setAdapter(deviceListAdapter);
        mlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                scanLeDevice(false);
                MyApplication.setCunrrentItem(deviceList.get(position));
                mHandler.removeMessages(MSG_REDISCOVERY);
                Intent intent = new Intent(DiscoveryActivity.this,BLECmdActivity.class);
                intent.putExtra(Constants.KEY_MAC,deviceList.get(position).btDevice.getAddress());
                startActivity(intent);
            }
        });
    }

    private void initData(){
        if (mHandler == null) {
            mHandler = new MyHandler();
        }
        if (deviceList == null) {
            deviceList = new ArrayList<>();
        }
    }

    private void initBluetooth(){
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHandler.sendEmptyMessage(MSG_REDISCOVERY);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            scanLeDevice(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                finish();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startLeDiscovery(){
        if (Build.VERSION.SDK_INT >= 21) {
            mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                settings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                        .build();
            }
            else {
                settings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build();
            }
            filters = new ArrayList<>();
        }
        scanLeDevice(true);
    }

    private void scanLeDevice(final boolean enable) {
        Log.i(TAG,"scanLeDevice enable = "+enable);
        if (mBluetoothAdapter == null||mLEScanner == null ||mHandler== null){
            return;
        }
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT < 21) {
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    } else {
                        mLEScanner.stopScan(mScanCallback);

                    }
                }
            }, SCAN_PERIOD);
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            } else {
                Log.i(TAG,"startScan");
                mLEScanner.startScan(filters, settings, mScanCallback);
            }
        } else {
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            } else {
                mLEScanner.stopScan(mScanCallback);
            }
        }
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            Log.i(TAG, "onScanResult  = "+String.valueOf(callbackType) +",---->onScanResult  string= "+result.toString());
            boolean hasManufacture = false;
            SparseArray<byte[]> manufacturerSpecificData = result.getScanRecord().getManufacturerSpecificData();
            if (manufacturerSpecificData!= null) {
                for(int i = 0;i<manufacturerSpecificData.size();i++){
                    if (manufacturerSpecificData.keyAt(i) == Constants.JBL_MANUFACTURE) {
                        hasManufacture = true;
                    }
                }
            }

            if (hasManufacture) {
                String deviceMac = result.getDevice().getAddress();
                BluetoothDevice bluetoothDevice = result.getDevice();
                boolean found = false;
                for (int i = 0; i < deviceList.size(); i++) {
                    if (deviceList.get(i).btDevice.getAddress().equalsIgnoreCase(deviceMac)) {
                        found = true;
                        deviceList.get(i).flag = true;
                    }
                }
                if (!found) {
                    DeviceItem deviceItem = new DeviceItem();
                    deviceItem.btDevice = bluetoothDevice;
                    deviceItem.connectStr = getString(R.string.disconnected);
                    deviceItem.flag = true;
                    deviceList.add(deviceItem);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            deviceListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                Log.i(TAG, "onBatchScanResults ="+sr.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e(TAG, "Error Code: " + errorCode);
        }
    };

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi,byte[] scanRecord) {
            Log.i(TAG, device.toString());
        }
    };

    private class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_REDISCOVERY:{
                    Log.i(TAG,"handleMessage ");
                    List<String> tempList = new ArrayList<>();
                    for (Map.Entry<String, Model> entry : MyApplication.getConnectedMap().entrySet()) {
                        if (entry.getValue().getDeviceItem().connectStr.equals(getString(R.string.connected))){
                            if (!deviceList.contains(entry.getValue().getDeviceItem())){
                                deviceList.add(entry.getValue().getDeviceItem());
                            }
                        }else{
                            if (!tempList.contains(entry.getKey())) {
                                tempList.add(entry.getKey());
                            }
                        }
                    }
                    for (int i = 0;i<tempList.size();i++){
                        MyApplication.getConnectedMap().remove(tempList.get(i));
                    }
                    for (int i = 0; i < deviceList.size(); i++) {
                        if (deviceList.get(i).flag == false
                                && deviceList.get(i).connectStr.equals(getString(R.string.disconnected))){
                            deviceList.remove(i);
                        }else{
                            deviceList.get(i).flag = false;
                        }
                        deviceListAdapter.notifyDataSetChanged();
                    }
                    startLeDiscovery();
                    mHandler.sendEmptyMessageDelayed(MSG_REDISCOVERY,Constants.DISCOVERY_TIME_MISE);
                    break;
                }
            }
        }
    }

}
