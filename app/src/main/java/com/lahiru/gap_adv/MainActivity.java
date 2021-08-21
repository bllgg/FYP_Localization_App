package com.lahiru.gap_adv;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseData;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "BLE ADV";

    protected static byte seq_num = 0;
    protected static final short dev_id = 0x00FF;
    TextView acc_text, gyro_text, mag_text;

    /////////////////////////////////////////////////////////////////////////////

    private SensorManager sensorManager;
    private Sensor sensor;
    private List<android.hardware.Sensor> list_acc, list_gyro, list_mag;

    short acc_x, acc_y, acc_z, gyr_x, gyr_y, gyr_z, mag_x, mag_y, mag_z;

    SensorEventListener sel_gyro = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onSensorChanged(SensorEvent event) {
            float[] values = event.values;

            double x_value, y_value, z_value;
            x_value = values[0]; // - 0.005752;
            y_value = values[1]; // - 0.002984;
            z_value = values[2]; // + 0.000311;
            gyr_x = gyro_revert(x_value);
            gyr_y = gyro_revert(y_value);
            gyr_z = gyro_revert(z_value);

            gyro_text.setText("GYROSCOPE\nx axis: " + values[0] + "\ny axis: " + values[1] + "\nz axis: " + values[2]);
        }
    };

    SensorEventListener sel_mag = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onSensorChanged(SensorEvent event) {
            float[] values = event.values;

            mag_x = mag_revert(values[0]);
            mag_y = mag_revert(values[1]);
            mag_z = mag_revert(values[2]);

            mag_text.setText("MAGNETOMETER\nx axis: " + values[0] + "\ny axis: " + values[1] + "\nz axis: " + values[2]);
        }
    };

    SensorEventListener sel_acc = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onSensorChanged(SensorEvent event) {
            float[] values = event.values;

            double x_value, y_value, z_value;
            x_value = values[0]; // 1.008 * values[0] - 0.1803;
            y_value = values[1]; // 1.011 * values[1] + 0.492;
            z_value = values[2]; // 0.999 * values[2] - 0.3397;

            acc_x = acc_revert(x_value);
            acc_y = acc_revert(y_value);
            acc_z = acc_revert(z_value);

            acc_text.setText("ACCELERATION\nx axis: " + values[0] + "\ny axis: " + values[1] + "\nz axis: " + values[2]);
//            acc_text.setText("ACCELERATION\nx axis: "+x_value+" "+acc_x+"\ny axis: "+y_value+" "+acc_y+"\nz axis: "+z_value+" "+acc_z);
        }
    };
    ////////////////////////////////////////////////////////////////////////////

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final String LOG_TAG = "App_LOG";

        ////////////////////////////////////////////////////////////////////////////////////
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        assert sensorManager != null;
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        acc_text = findViewById(R.id.acc_txt);
        gyro_text = findViewById(R.id.gyro_txt);
        mag_text = findViewById(R.id.mag_txt);

        list_mag = sensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
        list_acc = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        list_gyro = sensorManager.getSensorList(Sensor.TYPE_GYROSCOPE);

        if (list_acc.size() > 0) {
            sensorManager.registerListener(sel_acc, (Sensor) list_acc.get(0), SensorManager.SENSOR_DELAY_NORMAL); // in here we register the sensors to the variables.
        } else {
            Toast.makeText(getBaseContext(), "Error: No Accelerometer.", Toast.LENGTH_LONG).show();
        }

        if (list_mag.size() > 0) {
            sensorManager.registerListener(sel_mag, (Sensor) list_mag.get(0), SensorManager.SENSOR_DELAY_UI); // in here we register the sensors to the variables.
        } else {
            Toast.makeText(getBaseContext(), "Error: No Magnetometer.", Toast.LENGTH_LONG).show();
        }

        if (list_gyro.size() > 0) {
            sensorManager.registerListener(sel_gyro, (Sensor) list_gyro.get(0), SensorManager.SENSOR_DELAY_UI);  // here also we register the sensors to the variables.
        } else {
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
                .setInterval(AdvertisingSetParameters.INTERVAL_LOW)
                .setTxPowerLevel(AdvertisingSetParameters.TX_POWER_HIGH)
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
                Log.i(LOG_TAG, "onAdvertisingSetStarted(): txPower:" + txPower + " , status: " + status);
                currentAdvertisingSet[0] = advertisingSet;
                if (advertisingSet == null) {
                    Log.i(LOG_TAG, "Adverticing set is null");

                } else {
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

                        byte[] service_data = {(byte) (mag_x), (byte) (mag_y >> 8), (byte) (mag_y), (byte) (mag_z >> 8), (byte) (mag_z)};

                        byte[] data_array = {(byte) (mag_x >> 8), (byte) (gyr_z), (byte) (gyr_z >> 8), (byte) (gyr_y), (byte) (gyr_y >> 8), (byte) (gyr_x), (byte) (gyr_x >> 8), (byte) (acc_z), (byte) (acc_z >> 8), (byte) (acc_y), (byte) (acc_y >> 8), (byte) (acc_x), (byte) (acc_x >> 8), (byte) (dev_id), (byte) (dev_id >> 8), seq_num};
                        ByteBuffer bb = ByteBuffer.wrap(data_array);
                        long f_l = bb.getLong();
                        long s_l = bb.getLong();
                        UUID uuid_byte = new UUID(f_l, s_l);
                        ParcelUuid l = new ParcelUuid(uuid_byte);
                        currentAdvertisingSet[0].setAdvertisingData(new AdvertiseData.Builder().addServiceData(l, service_data).setIncludeTxPowerLevel(false).build());

                        seq_num += 1;
                        try {
                            synchronized (this) {
                                this.wait(100);
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
        if (list_gyro.size() > 0) {
            sensorManager.unregisterListener(sel_gyro);
        }

        if (list_mag.size() > 0) {
            sensorManager.unregisterListener(sel_mag);
        }

        if (list_acc.size() > 0) {
            sensorManager.unregisterListener((sel_acc));
        }
        super.onStop();
    }

    static short acc_revert(double data) {
        short res;
        short reverese_res;
        res = (short) ((data / 9.81) * (32768.0 / 16.0));
//        res = 0;
        reverese_res = (short) ((short) ((res >> 8) & 0xff) | ((res & 0xff) << 8));
        return reverese_res;
    }

    static short gyro_revert(double data) {
        short res;
        short reverese_res;
        res = (short) ((data * (180 / Math.PI)) * (32768.0 / 500.0));
        reverese_res = (short) ((short) ((res >> 8) & 0xff) | ((res & 0xff) << 8));
        return reverese_res;
    }

    static short mag_revert(float data) {
        short res;
        short reverese_res;
        int resu = (int) (data * 10);
        res = (short) resu;
        reverese_res = (short) ((short) ((res >> 8) & 0xff) | ((res & 0xff) << 8));
        return reverese_res;
    }
}
