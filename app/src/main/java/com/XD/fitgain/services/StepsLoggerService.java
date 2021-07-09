package com.XD.fitgain.services;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;

//Este es el servicio que se encarga de obtener los pasos del usuario haciendo uso de los sensores
//del dispositivo (aceleracion en x,y,z). Para saber si ha realizado un paso se obtiene la magnitud
//de los componentes en x,y,z luego se comprar con una constante de tolerancia que en nuestro caso
//hemos colocado 4, entre menos sea mas sensible sera al detectar los pasos.

public class StepsLoggerService extends Service implements SensorEventListener {
    private static final String DEBUG_TAG = "StepsLoggerService";

    private SensorManager sensorManager = null;
    private Sensor sensor = null;

    private double MagnitudePrevious = 0;
    private Integer stepCount = 0;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensor,
                SensorManager.SENSOR_DELAY_NORMAL);


        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event != null) {
            float x_acceleration = event.values[0];
            float y_acceleration = event.values[1];
            float z_acceleration = event.values[2];

            double Magnitude = Math.sqrt(x_acceleration * x_acceleration + y_acceleration * y_acceleration + z_acceleration * z_acceleration);
            double MagnitudeDelta = Magnitude - MagnitudePrevious;

            MagnitudePrevious = Magnitude;

            if (MagnitudeDelta > 4) {
                stepCount++;
            }

            saveSteps(stepCount);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void saveSteps(Integer stepCount) {

        Intent intent = new Intent();
        intent.setAction("STEPS_CHANGED");
        intent.putExtra("steps", stepCount);
        sendBroadcast(intent);
    }
}
