package com.harman.johngan.activity;

import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.harman.johngan.R;
import com.harman.johngan.adpter.CmdListAdapter;
import com.harman.johngan.application.MyApplication;
import com.harman.johngan.btmodule.Commands;
import com.harman.johngan.btmodule.Model;
import com.harman.johngan.constants.Constants;
import com.harman.johngan.dialog.MyDialog;
import com.harman.johngan.entity.DeviceItem;
import com.harman.johngan.listener.ModelLisener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BLECmdActivity extends AppCompatActivity {
    private final static String TAG = Constants.PRETAG+BLECmdActivity.class.getSimpleName();
    private ListView mListview;
    private TextView mTextView;

    private CmdListAdapter cmdListAdapter;
    List<String> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blecmd);
        Log.i(TAG,"onCreate");
        initView();
        initData();
    }

    private void initData(){

        if (list == null){
            list = new ArrayList<>();
            for( int i = 0;i<Commands.CMDLISTS.length;i++) {
                list.add(Commands.CMDLISTS[i]);
            }
        }
        DeviceItem deviceItem = MyApplication.getCunrrentItem();

        cmdListAdapter = new CmdListAdapter(this,list);
        mListview.setAdapter(cmdListAdapter);

        boolean found = false;
        for (Map.Entry<String, Model> entry : MyApplication.getConnectedMap().entrySet()) {
            if (entry.getKey().equals(deviceItem.btDevice.getAddress())){
                found = true;
            }
        }
        if (!found) {
            Model devModel = new Model(this);
            devModel.addLisener(new ModelLisener() {
                @Override
                public void onStateConnected(BluetoothDevice device) {
                    Log.i(TAG,"onStateConnected");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setStatus(Color.RED);
                        }
                    });
                }

                @Override
                public void onStateDisconnected(BluetoothDevice device) {
                    Log.i(TAG,"onStateDisconnected");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setStatus(Color.RED);
                        }
                    });
                }

                @Override
                public void onServicesDiscovered(BluetoothDevice device) {
                    Log.i(TAG,"onServicesDiscovered");

                }

                @Override
                public void onCharacteristicWrite(String writeCmd) {
                }

                @Override
                public void onCharacteristicChanged(final String receiveCmd) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MyDialog.dialog(BLECmdActivity.this,receiveCmd);
                        }
                    });
                }
            });
            devModel.connectGatt(MyApplication.getCunrrentItem());
            MyApplication.getConnectedMap().put(MyApplication.getCunrrentItem().btDevice.getAddress(), devModel);
        }else{
            setStatus(Color.RED);
        }
    }

    private void setStatus(int color){

        SpannableStringBuilder builder = new SpannableStringBuilder(MyApplication.getCunrrentItem().connectStr);
        BackgroundColorSpan redSpan = new BackgroundColorSpan(color);
        builder.setSpan(redSpan, 0, MyApplication.getCunrrentItem().connectStr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mTextView.setText(MyApplication.getCunrrentItem().btDevice.getName()+": "+builder.toString());
    }

    private void initView(){
        mTextView = (TextView) findViewById(R.id.device_status);
        mListview = (ListView) findViewById(R.id.listview_cmd);
        mListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (MyApplication.getCunrrentItem().connectStr.equals(getString(R.string.disconnected))){
                    setStatus(Color.RED);
                    return;
                }
                Model model = MyApplication.getConnectedMap().get(MyApplication.getCunrrentItem().btDevice.getAddress());
                if (position == 3) {
                    byte[] value = new byte[]{
                            (byte)0xFF,
                    };
                    setLedBrightness(value);
                }else{
                    model.sendCmd(Commands.cmds[position]);
                }
            }
        });
    }

    private byte[] setLedBrightness(byte[] value){
        byte[] command =new byte[4];
        System.arraycopy(Commands.cmds[3],0,command,0, Commands.cmds[3].length);
        System.arraycopy(value,0,command, Commands.cmds[3].length,1);
        Log.i(TAG,"commands   = "+ Arrays.toString(command));
        return command;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
