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

import java.nio.ByteBuffer;
import java.util.UUID;

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
                .setTxPowerLevel(AdvertisingSetParameters.TX_POWER_MAX)
                .build();

        /*
        * int: Bluetooth LE Advertising interval, in 0.625ms unit. Valid range is from 160 (100ms) to 16777215 (10,485.759375 s). Recommended values are: AdvertisingSetParameters#INTERVAL_LOW, AdvertisingSetParameters#INTERVAL_MEDIUM, or AdvertisingSetParameters#INTERVAL_HIGH.
        * */
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
                byte net_id = (byte)0xaa;
                short acc_x = 0x1122;
                short acc_y = 0x3344;
                short acc_z = 0x5566;
                short gyr_x = 0x1122;
                short gyr_y = 0x3344;
                short gyr_z = 0x5566;
                short mag_x = 0x1122;
                short mag_y = 0x3344;
                short mag_z = 0x5566;
                while (true) {
                    if (currentAdvertisingSet[0] != null) {
                        num%=10;
                        //byte[] service_data = {net_id, (byte)(acc_x >> 8), (byte)(acc_x), (byte)(acc_y >> 8), (byte)(acc_y), (byte)(acc_z >> 8), (byte)(acc_z), (byte)(mag_x), (byte)(mag_x), (byte)(mag_y), (byte)(mag_y), (byte)(mag_z), (byte)(mag_z), (byte)(gyr_x), (byte)(gyr_x), (byte)(gyr_y), (byte)(gyr_y), (byte)(gyr_z), (byte)(gyr_z)};
                        //byte[] service_uuid = {0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x11, 0x22};
                        byte[] service_data = {(byte)(mag_y >> 8), (byte)(mag_y), (byte)(mag_z >> 8), (byte)(mag_z), (byte)(gyr_x >> 8), (byte)(gyr_x), (byte)(gyr_y >> 8), (byte)(gyr_y), (byte)(gyr_z >> 8), (byte)(gyr_z),5,7,10};//, (byte)(gyr_z >> 8), (byte)(gyr_z)};
                        //currentAdvertisingSet[0].setAdvertisingData(new AdvertiseData.Builder().addServiceUuid(ParcelUuid.fromString("ddcc7766-9955-4433-aa22-33dd009955"+String.valueOf(num)+String.valueOf(num))).setIncludeDeviceName(name).setIncludeTxPowerLevel(true).build());
                        //currentAdvertisingSet[0].setAdvertisingData(new AdvertiseData.Builder().addServiceUuid(ParcelUuid.fromString("ddcc7766-9955-4433-aa22-33dd009955"+String.valueOf(num)+String.valueOf(num))).setIncludeTxPowerLevel(true).build());
                        //currentAdvertisingSet[0].setAdvertisingData(new AdvertiseData.Builder().addServiceData(ParcelUuid.fromString((byte)(mag_x >> 8)+(byte)mag_x+"ddcc7766-9955-4433-aa22-33dd0099"), service_data).setIncludeTxPowerLevel(true).build());

                        byte[] b = {0,1,2,3,4,5,6,7,8,9,9,1,2,3,4,5};
                        ByteBuffer bb = ByteBuffer.wrap(b);
                        long f_l = bb.getLong();
                        long s_l = bb.getLong();
                        UUID k = new UUID(f_l, s_l);
                        ParcelUuid l = new ParcelUuid(k);
                        currentAdvertisingSet[0].setAdvertisingData(new AdvertiseData.Builder().addServiceData(l, service_data).setIncludeTxPowerLevel(false).build());
                        //currentAdvertisingSet[0].setAdvertisingData(new AdvertiseData.Builder().addServiceData(ParcelUuid.fromString((acc_x >> 8)+(acc_x)+(acc_y >> 8)+(acc_y)+(acc_z >> 8)+(acc_z)+(mag_x >> 8)+(mag_x)+"-9955-4433-aa22-33dd009955cc"), service_data).setIncludeTxPowerLevel(true).build());

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
