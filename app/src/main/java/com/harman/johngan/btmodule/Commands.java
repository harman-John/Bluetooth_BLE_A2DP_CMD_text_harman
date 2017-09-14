package com.harman.johngan.btmodule;

import java.util.ArrayList;

/**
 * Created by Johngan on 09/05/2017.
 */

public class Commands {

    private final static byte HEAD = (byte) 0xaa;



    private static byte[] REQ_DEV_INFO = new byte[]
    {
            HEAD, //identifier
            (byte) 0x11, // cmd Id and sub cmd id
            (byte) 0x00, // len
    };

    private static byte[] REQ_DEVICE_LED_BRIGHTNESS = new byte[]
    {
            HEAD, //identifier
            (byte) 0x56, // cmd Id and sub cmd id
            (byte) 0x00,
    };
    private static byte[] CHECK_ROLE = new byte[]
    {
            HEAD, //identifier
            (byte) 0x17, // cmd Id and sub cmd id
            (byte) 0x00,
    };
    private static byte[] LED_BRIGHTNESS_HEAD = new byte[]
    {
            HEAD, //identifier
            (byte) 0x55, // cmd Id and sub cmd id
            (byte) 0x01,
    };
    private static byte[] SET_DEVICE_LED_BRIGHTNESS_00 = new byte[]
    {
            HEAD, //identifier
            (byte) 0x55, // cmd Id and sub cmd id
            (byte) 0x01,
            (byte) 0x00,
    };
    private static byte[] IDENTY_DEV = new byte[]
    {
            HEAD, //identifier
            (byte) 0x31, // cmd Id and sub cmd id
            (byte) 0x01,
            (byte) 0x00,
    };
    private static byte[] SET_DEVICE_LED_BRIGHTNESS_80 = new byte[]
    {
            HEAD, //identifier
            (byte) 0x55, // cmd Id and sub cmd id
            (byte) 0x01,
            (byte) 0x80,
    };

    private static byte[] SET_DEVICE_LED_BRIGHTNESS_FF = new byte[]
    {
            HEAD, //identifier
            (byte) 0x55, // cmd Id and sub cmd id
            (byte) 0x01,
            (byte) 0xFF,
    };

    private static final byte[] REQ_DEVICE_PID =new byte[]
    {
            HEAD,
            (byte)0x13,
            (byte)0x02,
            (byte)0x00,
            (byte)0x42
    };

    private static final byte[] REQ_DEVICE_VER =new byte[]
    {
            HEAD,
            (byte)0x41,
            (byte)0x00
    };

    public static byte[][] cmds = new byte[][]{
            REQ_DEV_INFO,
            REQ_DEVICE_LED_BRIGHTNESS,
            CHECK_ROLE,
            LED_BRIGHTNESS_HEAD,
            IDENTY_DEV,
            REQ_DEVICE_PID,
            REQ_DEVICE_VER,
    };


    public final static String[] CMDLISTS = {
            "ReqDevInfo",
            "ReqDevLedBrightness",
            "CheckRole",
            "SetDevLedBrightness",
            "IdentyDev",
            "ReqDevicePid",
            "ReqDeviceVersion"
    };

    public static String bytesToHexString(byte[] data){
        String result="";
        for (int i = 0; i < data.length; i++) {
            result+=Integer.toHexString((data[i] & 0xFF) | 0x100).toUpperCase().substring(1, 3);
        }
        return result;
    }



}
