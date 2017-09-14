package com.harman.johngan.adpter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.harman.johngan.R;
import com.harman.johngan.entity.DeviceItem;

import java.util.List;

/**
 * Created by Johngan on 12/09/2017.
 */

public class DeviceListAdapter extends BaseAdapter {

    private List<DeviceItem>  mList;
    private Context mContext;
    public DeviceListAdapter(Context context, List<DeviceItem> list){
        mList = list;
        mContext = context;
    }

    @Override
    public int getCount() {
        if (mList == null) {
            return 0;
        }
        return mList.size();
    }

    @Override
    public DeviceItem getItem(int position) {
        if (mList == null) {
            return null;
        }
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View layout = View.inflate(mContext, R.layout.layout_devlist_item, null);
        TextView devName = (TextView) layout.findViewById(R.id.deviceName);
        TextView devMac = (TextView) layout.findViewById(R.id.deviceMac);
        TextView devConnected = (TextView) layout.findViewById(R.id.deviceConnected);
        devName.setText(mList.get(position).btDevice.getName());
        devMac.setText(mList.get(position).btDevice.getAddress());
        if (mList.get(position).connectStr.equals(mContext.getString(R.string.connected))){
            devConnected.setTextColor(Color.RED);
        }
        devConnected.setText(mList.get(position).connectStr);
        return layout;
    }
}
