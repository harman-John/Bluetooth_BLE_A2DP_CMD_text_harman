package com.harman.johngan.activity;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.harman.johngan.R;
import com.harman.johngan.btmodule.Commands;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class A2DPCmdActivity extends AppCompatActivity {
    private final static String TAG = A2DPCmdActivity.class.getSimpleName();
    private ListView mlistView = null;
    private ListAdapter listAdapter = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private final static int MSG_START_A2DP = 0;
    private final static int MSG_INQURY_A2DP = 1;
    private final static int MSG_UPDATE_LIST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a2dpcmd);
        Log.i(TAG,"onCreate");
        mlistView = (ListView) findViewById(R.id.listview_device);
        listAdapter =new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, getData());
        mlistView.setAdapter(listAdapter);
        mlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                myHandler.sendEmptyMessage(MSG_START_A2DP);
            }
        });
        myHandler.sendEmptyMessage(MSG_START_A2DP);
    }

    private void A2DPOpen(){
        Log.i(TAG,"A2DPOpen");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.getProfileProxy(this, mProfileListener, BluetoothProfile.A2DP);
    }

    private BluetoothSocket socket = null;
    private BluetoothA2dp mBluetoothSpeaker = null;
    private OutputStream outputStream = null;
    private InputStream inputStream = null;
    public static String SPP_UUID ="00001101-0000-1000-8000-00805F9B34FB";
    private BluetoothProfile.ServiceListener mProfileListener = new BluetoothProfile.ServiceListener() {
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.A2DP) {
                Log.i(TAG,"onServiceCssonnected -----");
                Log.i(TAG,"isEnabled = "+mBluetoothAdapter.isEnabled()+",name = "+mBluetoothAdapter.getName());
                Log.i(TAG,"mBluetoothAdapter = "+mBluetoothAdapter.toString());
                mBluetoothSpeaker = (BluetoothA2dp) proxy;
                // no devices are connected
                List<BluetoothDevice> connectedDevices = mBluetoothSpeaker.getConnectedDevices();
                Log.i(TAG, "connectDevices = " + connectedDevices.toString());
                if (connectedDevices != null) {
                    for (int i = 0; i < connectedDevices.size(); i++) {
                        BluetoothDevice btConnectedSpeaker = connectedDevices.get(i);
                        Log.i(TAG,"btConnectedSpeaker = "+btConnectedSpeaker.toString());
                        boolean isPlaying = mBluetoothSpeaker.isA2dpPlaying(btConnectedSpeaker);
                        Log.i(TAG,"isplaying = "+isPlaying);
                        UUID uuid = UUID.fromString(SPP_UUID);
                        try {
                            socket = btConnectedSpeaker.createRfcommSocketToServiceRecord(uuid);
                            if (socket != null) {
                                socket.connect();
                                if (socket.isConnected()) {
                                    inputStream = socket.getInputStream();
                                    outputStream = socket.getOutputStream();
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Log.i(TAG,"Thread run ..... "+Thread.currentThread().getId());
                                            while (true) {
                                                if (inputStream == null) {
                                                    Log.i(TAG, "inputstream is null");
                                                    return;
                                                }
                                                if (socket == null){
                                                    Log.i(TAG,"socket is null");
                                                    break;
                                                }
                                                Log.i(TAG,"come back ");
                                                byte[] data = new byte[100];
                                                try {
                                                    inputStream.read(data);

                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                                String dataStr = Commands.bytesToHexString(data);
                                                Log.i(TAG, "inputStream = " +dataStr );
                                                boolean isFound = false;
                                                if (dataStr.startsWith("AA12")){
                                                    //String payloadlen = dataStr.substring(5,6);
                                                    //int len = Integer.valueOf(payloadlen,16);
                                                    if (dataStr.length() > 12){

                                                        String deviceNameId = dataStr.substring(8,10);
                                                        Log.i(TAG,"deviceNameId = "+deviceNameId);
                                                        if (deviceNameId.equalsIgnoreCase("c1")){
                                                            int len = Integer.valueOf(dataStr.substring(10,12),16) *2;
                                                            Log.i(TAG,"len = "+len);
                                                            if (dataStr.length()> len+19){
                                                                String productId = dataStr.substring(len+12,len+14);
                                                                Log.i(TAG,"productId = "+productId);
                                                                if (productId.equalsIgnoreCase("42")) {
                                                                    String pid = dataStr.substring(len + 14, len + 18);
                                                                    for (int j = 0;j <productList.length;j++){
                                                                        if (productList[j].equalsIgnoreCase(pid)){
                                                                            //myHandler.sendEmptyMessage(MSG_INQURY_A2DP);
                                                                            Log.i(TAG,"pid = "+pid);
                                                                            isFound = true;
                                                                            break;
                                                                        }
                                                                    }

                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                                /*if (isFound){
                                                    break;
                                                }*/
                                               // myHandler.sendEmptyMessage(MSG_INQURY_A2DP);
                                            }
                                        }
                                    }).start();
                                }
                            }
                        } catch (IOException e) {
                            Log.i(TAG,"socket connect exception");
                            e.printStackTrace();
                        }

                    }
                }
                //the one paired (and disconnected) speaker is returned here
                int[] statesToCheck = {BluetoothA2dp.STATE_DISCONNECTED};
                List<BluetoothDevice> disconnectedDevices = mBluetoothSpeaker.getDevicesMatchingConnectionStates(statesToCheck);
                Log.i(TAG, "disconnectedDevices = " + disconnectedDevices.toString());
                if (disconnectedDevices != null && disconnectedDevices.size() >0) {
                    BluetoothDevice btSpeaker = disconnectedDevices.get(0);
                }
                myHandler.sendEmptyMessage(MSG_UPDATE_LIST);
            }
            mBluetoothAdapter.closeProfileProxy(profile,proxy);
        }

        public void onServiceDisconnected(int profile) {
            if (profile == BluetoothProfile.A2DP) {
                mBluetoothSpeaker = null;
                Log.i(TAG,"onServiceDisconnected xxxxx");
            }
        }
    };

    public void sendCmd(byte[] cmd)
    {

        if(outputStream==null) return;
        try {
            outputStream.write(cmd);
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String[] productList = {"0023","0024","0026","0x1EBC"};



    private MyHandler  myHandler =  new MyHandler();
    private class MyHandler extends Handler{

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case MSG_START_A2DP: {
                    A2DPOpen();
                    break;
                }
                case MSG_INQURY_A2DP:{
                    try {
                        if (socket != null) {
                            socket.close();
                        }
                        socket = null;
                        Log.i(TAG,"socket close");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case MSG_UPDATE_LIST: {
                    sendCmd(Commands.cmds[5]);
                    break;
                }
                default: {
                    break;
                }
            }
        }
    };

    private List<String> getData() {

        List<String> data = new ArrayList<String>();
        data.add("测试数据1");
        data.add("测试数据2");
        data.add("测试数据3");
        data.add("测试数据4");

        return data;
    }

}
