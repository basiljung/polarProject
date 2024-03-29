package com.example.polarapp.polar;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import com.example.polarapp.preferencesmanager.DevicePreferencesManager;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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

public class PolarSDK extends Application {

    private String TAG = "PolarSDK_API";
    private PolarBleApi api;
    private String DEVICE_ID;
    private Disposable scanDisposable;
    private boolean isConnecting = false;
    private CountDownLatch latch;
    private DevicePreferencesManager devicePreferencesManager;

    private CallbackInterfaceDevices callbackInterfaceDevices = null;

    public interface CallbackInterfaceDevices {
        void scanDevice(PolarDeviceInfo polarDeviceInfo);

        void deviceConnected(boolean ok);

        void deviceDisconnected(boolean ok);

        void batteryDataReceived(int batteryLevel);
    }

    public void setCallbackInterfaceDevices(CallbackInterfaceDevices cb) {
        this.callbackInterfaceDevices = cb;
    }

    private CallbackInterfaceActivity callbackInterfaceActivity = null;

    public interface CallbackInterfaceActivity {
        void hrUpdateData(int hr);
    }

    public void setCallbackInterfaceActivity(CallbackInterfaceActivity cb) {
        this.callbackInterfaceActivity = cb;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startAPI();
        latch = new CountDownLatch(1);
        devicePreferencesManager = new DevicePreferencesManager(getBaseContext());
        if (devicePreferencesManager.getConnectedDevices() == 1) {
            connectDevice(devicePreferencesManager.getDeviceID(), true);
        }
    }

    public void startAPI() {
        DEVICE_ID = "";
        api = PolarBleApiDefaultImpl.defaultImplementation(getBaseContext(),
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
                isConnecting = false;
            }

            @Override
            public void deviceConnecting(PolarDeviceInfo s) {
                Log.d(TAG, "Device connecting " + s.deviceId);
                isConnecting = true;
                latch.countDown();
            }

            @Override
            public void deviceDisconnected(PolarDeviceInfo s) {
                Log.d(TAG, "Device disconnected " + s);
                devicePreferencesManager.setConnectedDevices(0);
                devicePreferencesManager.setDeviceID("");
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
                callbackInterfaceDevices.batteryDataReceived(i);
            }

            @Override
            public void hrNotificationReceived(String s, PolarHrData polarHrData) {
                Log.d(TAG, "HR " + polarHrData.hr);

                if (callbackInterfaceActivity != null) {
                    callbackInterfaceActivity.hrUpdateData(polarHrData.hr);
                }
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
        if (scanDisposable == null) {
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
        } else {
            scanDisposable.dispose();
            scanDisposable = null;
        }
    }

    public void connectDevice(String device_id, boolean isStart) {
        if (isStart) {
            new TryToConnect().execute(device_id);
        } else {
            try {
                api.connectToDevice(device_id);
                callbackInterfaceDevices.deviceConnected(true);
            } catch (PolarInvalidArgument polarInvalidArgument) {
                polarInvalidArgument.printStackTrace();
                callbackInterfaceDevices.deviceConnected(false);
            }
        }
    }

    public void disconnectDevice(String device_id) {
        try {
            api.disconnectFromDevice(device_id);
            callbackInterfaceDevices.deviceDisconnected(true);
        } catch (PolarInvalidArgument polarInvalidArgument) {
            polarInvalidArgument.printStackTrace();
            callbackInterfaceDevices.deviceDisconnected(false);
        }

    }

    public void onPauseEntered() {
        api.backgroundEntered();
    }

    public void onResumeEntered() {
        api.foregroundEntered();
    }

    public void onDestroyEntered() {
        api.shutDown();
    }

    class TryToConnect extends AsyncTask<String, Void, Void> {

        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected Void doInBackground(String... args) {
            try {
                api.connectToDevice(args[0]);
                Log.d("PolarSDK_API", "Waiting to connect");
                latch.await(5, TimeUnit.SECONDS);
                if (isConnecting == true) {
                    Log.d("PolarSDK_API", "Successfull");
                    Log.d("PolarSDK_API", "Device " + devicePreferencesManager.getDeviceID() + " connected");
                } else {
                    Log.d("PolarSDK_API", "Device " + devicePreferencesManager.getDeviceID() + " not connected :(");
                    devicePreferencesManager.setDeviceID("");
                    devicePreferencesManager.setConnectedDevices(0);
                }
            } catch (PolarInvalidArgument polarInvalidArgument) {
                polarInvalidArgument.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
        }
    }
}
