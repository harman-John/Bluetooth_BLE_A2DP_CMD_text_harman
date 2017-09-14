package com.harman.johngan.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.harman.johngan.R;

/**
 * Created by Johngan on 14/09/2017.
 */

public class MyDialog {

    public static void dialog(Context context,String content) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(content);
        builder.setTitle(R.string.receive_cmd);
        builder.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
}
