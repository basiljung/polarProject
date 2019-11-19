package com.example.polarapp.preferencesmanager;

import android.content.Context;
import android.content.SharedPreferences;

public class DevicePreferenceManager {
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private Context context;
    private static final String CONNECTED_DEVICE = "connected_device";
    private static final String DEVICES_COUNT = "devices_count";
    private static final String ID_DEVICE_CONNECTED = "id_connected";
    private static final String BATTERY_LEVEL = "battery_level";

    public DevicePreferenceManager(Context context) {
        this.context = context;
        sp = context.getSharedPreferences(CONNECTED_DEVICE, Context.MODE_PRIVATE);
        editor = sp.edit();
        if (sp.getInt(DEVICES_COUNT, -1) == -1) {
            editor.putInt(DEVICES_COUNT, 0);
            editor.commit();
        }
    }

    public int getConnectedDevices() {
        return sp.getInt(DEVICES_COUNT, 0);
    }

    public void setConnectedDevices(int devices) {
        editor.putInt(DEVICES_COUNT, devices);
        editor.commit();
    }

    public int getBatteryLevel() {
        return sp.getInt(BATTERY_LEVEL, 0);
    }

    public void setBatteryLevel(int level) {
        editor.putInt(BATTERY_LEVEL, level);
        editor.commit();
    }

    public String getID() {
        return sp.getString(ID_DEVICE_CONNECTED, "");
    }

    public void setID(String id) {
        editor.putString(ID_DEVICE_CONNECTED, id);
        editor.commit();
    }

}
