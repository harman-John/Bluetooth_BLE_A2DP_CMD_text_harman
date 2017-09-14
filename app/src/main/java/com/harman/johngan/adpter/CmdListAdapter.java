package com.harman.johngan.adpter;

import android.content.Context;
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

public class CmdListAdapter extends BaseAdapter {

    private List<String>  mList;
    private Context mContext;
    public CmdListAdapter(Context context, List<String> list){
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
    public String getItem(int position) {
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
        View layout = View.inflate(mContext, R.layout.layout_func_item, null);
        TextView devName = (TextView) layout.findViewById(R.id.function_name);
        devName.setText(mList.get(position));
        return layout;
    }
}
