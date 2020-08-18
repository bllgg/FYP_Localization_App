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
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "BLE ADV";

    protected static byte seq_num = 0;
    protected static final short dev_id = 0x00FF;


    /////////////////////////////////////////////////////////////////////////////

    private SensorManager sensorManager;
    private Sensor sensor;
    private List list_acc, list_gyro, list_mag;

    short acc_x, acc_y, acc_z, gyr_x, gyr_y, gyr_z, mag_x, mag_y, mag_z;

    SensorEventListener sel_gyro = new SensorEventListener(){
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        @Override
        public void onSensorChanged(SensorEvent event) {
            float[] values = event.values;

            gyr_x = gyro_revert(values[0]);
            gyr_y = gyro_revert(values[1]);
            gyr_z = gyro_revert(values[2]);
        }
    };

    SensorEventListener sel_mag = new SensorEventListener(){
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        @Override
        public void onSensorChanged(SensorEvent event) {
            float[] values = event.values;

            mag_x = mag_revert(values[0]);
            mag_y = mag_revert(values[1]);
            mag_z = mag_revert(values[2]);
        }
    };

    SensorEventListener sel_acc = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        @Override
        public void onSensorChanged(SensorEvent event) {
            float[] values = event.values;
            
            acc_x = acc_revert(values[0]);
            acc_y = acc_revert(values[1]);
            acc_z = acc_revert(values[2]);
        }
    };
    ////////////////////////////////////////////////////////////////////////////

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final String LOG_TAG= "App_LOG";

        ////////////////////////////////////////////////////////////////////////////////////
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        list_mag = sensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
        list_acc = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        list_gyro = sensorManager.getSensorList(Sensor.TYPE_GYROSCOPE);

        if(list_acc.size()>0){
            sensorManager.registerListener(sel_acc, (Sensor) list_acc.get(0), SensorManager.SENSOR_DELAY_FASTEST); // in here we register the sensors to the variables.
        }else{
            Toast.makeText(getBaseContext(), "Error: No Accelerometer.", Toast.LENGTH_LONG).show();
        }

        if(list_mag.size()>0){
            sensorManager.registerListener(sel_mag, (Sensor) list_mag.get(0), SensorManager.SENSOR_DELAY_FASTEST); // in here we register the sensors to the variables.
        }else{
            Toast.makeText(getBaseContext(), "Error: No Magnetometer.", Toast.LENGTH_LONG).show();
        }

        if(list_gyro.size()>0){
            sensorManager.registerListener(sel_gyro, (Sensor) list_gyro.get(0), SensorManager.SENSOR_DELAY_FASTEST);  // here also we register the sensors to the variables.
        }else{
            Toast.makeText(getBaseContext(), "Error: No Gyroscope.", Toast.LENGTH_LONG).show();
        }

        ////////////////////////////////////////////////////////////////////////////////////

        ////////////////////////////////////////////////////////////////////////////////////
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();                  //
        if (!adapter.isLeCodedPhySupported()) {                                           //
            Log.i(LOG_TAG, "BLE Periodic Advertising supported!");                   //
        }                                                                                 //
        ////////////////////////////////////////////////////////////////////////////////////
        BluetoothLeAdvertiser advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();
        AdvertisingSetParameters parameters = (new AdvertisingSetParameters.Builder())
                .setLegacyMode(true) // True by default, but set here as a reminder.
                .setConnectable(false)
                .setInterval(AdvertisingSetParameters.INTERVAL_HIGH)
                .setTxPowerLevel(AdvertisingSetParameters.TX_POWER_MEDIUM)
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

                while (true) {
                    if (currentAdvertisingSet[0] != null) {

                        byte[] service_data = {(byte)(mag_x), (byte)(mag_y >> 8), (byte)(mag_y), (byte)(mag_z >> 8), (byte)(mag_z)};

                        byte[] b = {(byte)(mag_x >> 8), (byte)(gyr_z), (byte)(gyr_z >> 8), (byte)(gyr_y), (byte)(gyr_y >> 8), (byte)(gyr_x), (byte)(gyr_x >> 8), (byte)(acc_z), (byte)(acc_z >> 8), (byte)(acc_y), (byte)(acc_y >> 8), (byte)(acc_x), (byte)(acc_x >> 8), (byte)(dev_id), (byte)(dev_id >> 8), seq_num};
                        ByteBuffer bb = ByteBuffer.wrap(b);
                        long f_l = bb.getLong();
                        long s_l = bb.getLong();
                        UUID uuid_byte = new UUID(f_l, s_l);
                        ParcelUuid l = new ParcelUuid(uuid_byte);
                        currentAdvertisingSet[0].setAdvertisingData(new AdvertiseData.Builder().addServiceData(l, service_data).setIncludeTxPowerLevel(false).build());

                        seq_num += 1;
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

    }

    @Override
    protected void onStop() {
        if (list_gyro.size()>0){
            sensorManager.unregisterListener(sel_gyro);
        }

        if (list_mag.size()>0){
            sensorManager.unregisterListener(sel_mag);
        }

        if (list_acc.size() > 0){
            sensorManager.unregisterListener((sel_acc));
        }
        super.onStop();
    }

    static short acc_revert(float data) {
        short res;
        res = (short) ((data) * (32768.0 / 16.0));
        return res;
    }

    static short gyro_revert(float data){
        short res;
        res = (short) ((data) * (32768.0 / 250.0));
        return res;
    }

    static short mag_revert(float data) {
        short res;
        res = (short) ((data) * (32768.0 / 300.0));
        return res;
    }
}
