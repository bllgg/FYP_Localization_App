package com.lahiru.gap_adv;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.AdvertisingSet;
import android.bluetooth.le.AdvertisingSetCallback;
import android.bluetooth.le.AdvertisingSetParameters;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "BLE ADV";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && bluetoothAdapter.isMultipleAdvertisementSupported())
        {
            BluetoothLeAdvertiser advertiser = bluetoothAdapter.getBluetoothLeAdvertiser();

            AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
            //Define a service UUID according to your needs
            dataBuilder.addServiceUuid(ParcelUuid.fromString("231def14-fcdb-4f59-96ad-02f49c4972c1"));
            dataBuilder.setIncludeDeviceName(true);
            dataBuilder.setIncludeTxPowerLevel(true);


            AdvertiseSettings.Builder settingsBuilder = new AdvertiseSettings.Builder();
            settingsBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER);
            settingsBuilder.setTimeout(0);
            settingsBuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);

            //Use the connectable flag if you intend on opening a Gatt Server
            //to allow remote connections to your device.
            settingsBuilder.setConnectable(false);

            AdvertiseCallback advertiseCallback=new AdvertiseCallback() {
                @Override
                public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                    super.onStartSuccess(settingsInEffect);
                    Log.i(TAG, "onStartSuccess: ");
                }

                @Override
                public void onStartFailure(int errorCode) {
                    super.onStartFailure(errorCode);
                    Log.e(TAG, "onStartFailure: "+errorCode );
                }
            };
            advertiser.startAdvertising(settingsBuilder.build(),dataBuilder.build(),advertiseCallback);
            //advertiser.stopAdvertising(advertiseCallback);
            //dataBuilder.addServiceUuid(ParcelUuid.fromString("fed8d908-3931-4d1c-94ca-b63e8e056815"));
            //advertiser.startAdvertising(settingsBuilder.build(),dataBuilder.build(),advertiseCallback);
        }*/

        final String LOG_TAG= "App_LOG";
        ////////////////////////////////////////////////////////////////////////////////////
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (!adapter.isLeCodedPhySupported()) {
            Log.i(LOG_TAG, "BLE Periodic Advertising supported!");
        }
        ////////////////////////////////////////////////////////////////////////////////////
        BluetoothLeAdvertiser advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();
        AdvertisingSetParameters parameters = (new AdvertisingSetParameters.Builder())
                .setLegacyMode(true) // True by default, but set here as a reminder.
                .setConnectable(false)
                .setInterval(AdvertisingSetParameters.INTERVAL_HIGH)
                .setTxPowerLevel(AdvertisingSetParameters.TX_POWER_HIGH)
                .build();
        AdvertiseData data = (new AdvertiseData.Builder()).setIncludeDeviceName(true).build();

        final AdvertisingSet[] currentAdvertisingSet = new AdvertisingSet[1];
        //final AdvertisingSet[] currentAdvertisingSet = {null};
        AdvertisingSetCallback callback = new AdvertisingSetCallback() {
            @Override
            public void onAdvertisingSetStarted(AdvertisingSet advertisingSet, int txPower, int status) {
                Log.i(LOG_TAG, "onAdvertisingSetStarted(): txPower:" + txPower + " , status: "
                        + status);
                currentAdvertisingSet[0] = advertisingSet;
                if (advertisingSet == null){
                    Log.i(LOG_TAG, "Adverticing set is null");

                }
                else {
                    Log.i(LOG_TAG, "Advertising data is not null");
                    //currentAdvertisingSet[0].setAdvertisingData(new AdvertiseData.Builder().addServiceData(ParcelUuid.fromString("ebc17e63-9c59-4037-a929-35d5000956dd"),"example".getBytes()).setIncludeDeviceName(true).setIncludeTxPowerLevel(true).build());
                    //currentAdvertisingSet[0].setAdvertisingData(new AdvertiseData.Builder().addServiceUuid(ParcelUuid.fromString("ebc17e63-9c59-4037-a929-35d5000956dd")).setIncludeDeviceName(true).setIncludeTxPowerLevel(true).build());
                }
            }

            @Override
            public void onAdvertisingDataSet(AdvertisingSet advertisingSet, int status) {
                Log.i(LOG_TAG, "onAdvertisingDataSet() :status:" + status);
            }

            @Override
            public void onScanResponseDataSet(AdvertisingSet advertisingSet, int status) {
                Log.i(LOG_TAG, "onScanResponseDataSet(): status:" + status);
            }

            @Override
            public void onAdvertisingSetStopped(AdvertisingSet advertisingSet) {
                Log.i(LOG_TAG, "onAdvertisingSetStopped():");
            }
        };
        advertiser.startAdvertisingSet(parameters, data, null, null, null, callback);

        Thread mThread = new Thread() {
            @Override
            public void run() {
                // Perform thread commands...
                boolean name = true;
                int num = 0;
                while (true) {
                    if (currentAdvertisingSet[0] != null) {
                        num%=10;
                        currentAdvertisingSet[0].setAdvertisingData(new AdvertiseData.Builder().addServiceUuid(ParcelUuid.fromString("dbc17e63-9c59-4037-a929-35d5000956d"+String.valueOf(num))).setIncludeDeviceName(name).setIncludeTxPowerLevel(true).build());
                        name = !name;
                        num += 1;
                        try {
                            synchronized (this) {
                                this.wait(2000);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            synchronized (this) {
                                this.wait(2000);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        mThread.start();
        //currentAdvertisingSet[0].setAdvertisingData(new AdvertiseData.Builder().setIncludeDeviceName(true).setIncludeTxPowerLevel(true).build());
    }
}
