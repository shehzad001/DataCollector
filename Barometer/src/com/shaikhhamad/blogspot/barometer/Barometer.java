package com.shaikhhamad.blogspot.barometer;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.app.Activity;
import android.app.Service;
import android.widget.TextView;

public class Barometer extends Activity implements SensorEventListener{

	//SensorManager lets you access the device's sensors
	//declare Variables
	TextView TVAirPressure;
	private SensorManager mSensorManager;
	private Sensor mPressure;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_barometer);

		//create instance of sensor manager and get system service to interact with Sensor
		mSensorManager = (SensorManager) getSystemService(Service.SENSOR_SERVICE);
		mPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);

		//get textviews
		TVAirPressure = (TextView)findViewById(R.id.TVAirPressure);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// register this class as a listener for the Pressure Sensor
		mSensorManager.registerListener(this,mPressure,SensorManager.SENSOR_DELAY_NORMAL);
	}

	// called when sensor value have changed
	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {
			float values = event.values[0];
			TVAirPressure.setText("" + values);
		}

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// Do something here if sensor accuracy changes.
	}

	@Override
	protected void onPause() {
		// unregister listener
		super.onPause();
		mSensorManager.unregisterListener(this);
	}
}
