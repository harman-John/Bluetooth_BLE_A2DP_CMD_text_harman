package com.harman.johngan.application;

import android.app.Application;

import com.harman.johngan.btmodule.Model;
import com.harman.johngan.entity.DeviceItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Johngan on 12/09/2017.
 */

public class MyApplication extends Application
{

    private static Map<String,Model> mapDevConnected;
    private static DeviceItem cunrrentItem;
    @Override
    public void onCreate() {
        super.onCreate();
        if (cunrrentItem == null){
            cunrrentItem = new DeviceItem();
        }
        if (mapDevConnected == null){
            mapDevConnected = new HashMap<>();
        }
    }

    public static void setCunrrentItem(DeviceItem item){
        cunrrentItem = item;
    }

    public static DeviceItem getCunrrentItem(){
        return cunrrentItem;
    }

    public static void setConnectedMap(Map<String,Model> map){
        mapDevConnected = map;
    }

    public static Map<String,Model> getConnectedMap(){
        return mapDevConnected;
    }
}
