package com.example.polarapp.devices;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.polarapp.R;

import java.util.ArrayList;
import java.util.List;

import polar.com.sdk.api.model.PolarDeviceInfo;

public class DevicesArrayAdapter extends ArrayAdapter<PolarDeviceInfo> {
    private Context mContext;
    private List<PolarDeviceInfo> polarDeviceInfoList;

    public DevicesArrayAdapter(Context context, ArrayList<PolarDeviceInfo> list) {
        super(context, 0, list);
        this.mContext = context;
        this.polarDeviceInfoList = list;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.devices_list,parent,false);

        PolarDeviceInfo currentInfo = polarDeviceInfoList.get(position);

        TextView polarDeviceName = listItem.findViewById(R.id.polarDeviceName);
        polarDeviceName.setText(currentInfo.name);

        TextView polarDeviceID = listItem.findViewById(R.id.polarDeviceID);
        polarDeviceID.setText(currentInfo.deviceId);

        TextView polarDeviceAddress = listItem.findViewById(R.id.polarDeviceAddress);
        polarDeviceAddress.setText(currentInfo.address);

        ImageView polarDeviceImage = listItem.findViewById(R.id.sensorImage);
        if (currentInfo.name.toLowerCase().contains("oh1")) {
            polarDeviceImage.setImageResource(R.drawable.polaroh1);
        } else {
            polarDeviceImage.setImageResource(R.drawable.polarh10);
        }

        return listItem;
    }
}