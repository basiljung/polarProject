package com.example.polarapp.polar;

import android.content.Context;
import android.util.Log;
import android.view.View;

import java.util.List;
import java.util.UUID;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import polar.com.sdk.api.PolarBleApi;
import polar.com.sdk.api.PolarBleApiCallback;
import polar.com.sdk.api.PolarBleApiDefaultImpl;
import polar.com.sdk.api.errors.PolarInvalidArgument;
import polar.com.sdk.api.model.PolarDeviceInfo;
import polar.com.sdk.api.model.PolarHrData;

public class PolarSDK {

    private Context context;
    private String TAG = "Polar_HRActivity";
    private PolarBleApi api;
    private String DEVICE_ID;
    private Disposable scanDisposable;

    private CallbackInterfaceDevices callbackInterfaceDevices;

    public interface CallbackInterfaceDevices {
        void scanDevice(PolarDeviceInfo polarDeviceInfo);
        void deviceConnected(boolean ok, View v);
    }

    public PolarSDK(Context context, CallbackInterfaceDevices cb) {
        this.callbackInterfaceDevices = cb;
        this.context = context;
    }

    public void startAPI() {

        DEVICE_ID = "";

        api = PolarBleApiDefaultImpl.defaultImplementation(context,
                PolarBleApi.ALL_FEATURES);

        api.setApiCallback(new PolarBleApiCallback() {
            @Override
            public void blePowerStateChanged(boolean b) {
                Log.d(TAG, "BluetoothStateChanged " + b);
            }

            @Override
            public void deviceConnected(PolarDeviceInfo s) {
                Log.d(TAG, "Device connected " + s.deviceId);
                DEVICE_ID = s.deviceId;
            }

            @Override
            public void deviceConnecting(PolarDeviceInfo s) {
                Log.d(TAG, "Device connecting " + s.deviceId);
            }

            @Override
            public void deviceDisconnected(PolarDeviceInfo s) {
                Log.d(TAG, "Device disconnected " + s);
                DEVICE_ID = "";
            }

            @Override
            public void ecgFeatureReady(String s) {
                Log.d(TAG, "ECG Feature ready " + s);
            }

            @Override
            public void accelerometerFeatureReady(String s) {
                Log.d(TAG, "ACC Feature ready " + s);
            }

            @Override
            public void ppgFeatureReady(String s) {
                Log.d(TAG, "PPG Feature ready " + s);
            }

            @Override
            public void ppiFeatureReady(String s) {
                Log.d(TAG, "PPI Feature ready " + s);
            }

            @Override
            public void biozFeatureReady(String s) {
                Log.d(TAG, "BIOZ Feature ready " + s);
            }

            @Override
            public void hrFeatureReady(String s) {
                Log.d(TAG, "HR Feature ready " + s);
            }

            @Override
            public void disInformationReceived(String s, UUID u, String s1) {
                if (u.equals(UUID.fromString("00002a28-0000-1000-8000-00805f9b34fb"))) {
                    Log.d(TAG, "Firmware: " + s + " " + s1.trim());
                }
            }

            @Override
            public void batteryLevelReceived(String s, int i) {
                Log.d(TAG, "Battery level " + s + " " + i);
            }

            @Override
            public void hrNotificationReceived(String s, PolarHrData polarHrData) {
                Log.d(TAG, "HR " + polarHrData.hr);
                List<Integer> rrsMs = polarHrData.rrsMs;
                String msg = polarHrData.hr + "\n";
                for (int i : rrsMs) {
                    msg += i + ",";
                }
                if (msg.endsWith(",")) {
                    msg = msg.substring(0, msg.length() - 1);
                }
            }

            @Override
            public void polarFtpFeatureReady(String s) {
                Log.d(TAG, "Polar FTP ready " + s);
            }
        });
    }

    public void scanDevices() {
        if(scanDisposable == null) {
            scanDisposable = api.searchForDevice().observeOn(AndroidSchedulers.mainThread()).subscribe(
                    new Consumer<PolarDeviceInfo>() {
                        @Override
                        public void accept(PolarDeviceInfo polarDeviceInfo) {
                            callbackInterfaceDevices.scanDevice(polarDeviceInfo);
                        }
                    },
                    new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) {
                            Log.d("MyApp", throwable.getLocalizedMessage());
                        }
                    },
                    new Action() {
                        @Override
                        public void run() {
                            Log.d("MyApp", "complete");
                        }
                    }
            );
        } else{
            scanDisposable.dispose();
            scanDisposable = null;
        }
    }

    public void connectDevice(String device_id, View v) {
        try {
            api.connectToDevice(device_id);
            callbackInterfaceDevices.deviceConnected(true, v);
        } catch (PolarInvalidArgument polarInvalidArgument) {
            polarInvalidArgument.printStackTrace();
            callbackInterfaceDevices.deviceConnected(false, v);
        }
    }
}
