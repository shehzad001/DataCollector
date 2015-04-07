package com.example.pressureaccelerator;

import java.text.DecimalFormat;

import android.support.v7.app.ActionBarActivity;
import android.annotation.SuppressLint;
import android.app.Service;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

class constants{
	public static final double std_ceiling_height = 2.6; 
	public static final double std_floor_slab = 0.2;
	public static final double min_lower_bound = std_floor_slab + std_ceiling_height;
	public static final int max_upper_bound = 5;
}

public class PressAccActivity extends ActionBarActivity {
	
	private static SensorManager mSensorManager = null;
	private static Sensor mPressure;
	
	static TextView tvPress;
	static TextView tvAltd;
	static TextView tvP0, tvP1;
	static TextView tvAltdDiff;
	static TextView tvLevel;
	static TextView tvUpperThreh;
	
			
	static DecimalFormat accValue = new DecimalFormat("###,###.##");
	static float pressureValue = 0.0f;
	static float currentHeight = 0.0f;
	static float initialHeight = 0.0f;
	static float fltvP0 = 0.0f;
	static float fltvP1 = 0.0f;
	static float altitude_difference;
	static float flUpperThreh = (float) constants.min_lower_bound;

	static boolean startClickFlag;
	static int intlevel = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_press_acc);

		//create instance of sensor manager and get system service to interact with Sensor
		mSensorManager = (SensorManager) getSystemService(Service.SENSOR_SERVICE);
		mPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		
		tvPress = (TextView)findViewById(R.id.textViewPress);
		tvAltd = (TextView)findViewById(R.id.textViewAltd);
		tvP0 = (TextView)findViewById(R.id.textViewP0);
		tvP1 = (TextView)findViewById(R.id.textViewP1);
		tvAltdDiff = (TextView)findViewById(R.id.textViewAltdDiff);
		tvLevel = (TextView) findViewById(R.id.textViewLevelChanged);
		tvUpperThreh = (TextView) findViewById(R.id.textViewUpperThres);
		
		tvUpperThreh.setText("" + accValue.format(constants.min_lower_bound));

// To check any value on display...
//		tvP1.setText("" + flUpperThreh);
	}

	public static void startClick(View v){	
//		mSensorManager.registerListener(mSensorListener,mPressure,SensorManager.SENSOR_DELAY_NORMAL);
		tvP0.setText("" + accValue.format(pressureValue));
		initialHeight = currentHeight;			// Saving the current altitude

		intlevel = 0;
		tvLevel.setText("" + intlevel);
		altitude_difference = 0;
		tvAltdDiff.setText("" + altitude_difference);
		startClickFlag = true;					// Set flag to 1
	}

	@SuppressLint({ "NewApi", "InlinedApi" })
	public static void finishClick(View v){		
//		mSensorManager.unregisterListener(mSensorListener);
		tvP1.setText("" + accValue.format(pressureValue));
		
		fltvP1 = Float.parseFloat(tvP1.getText().toString());
		fltvP0 = Float.parseFloat(tvP0.getText().toString());
//		fltvP1 = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, Float.parseFloat(tvP1.getText().toString()));
//		fltvP0 =  SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, Float.parseFloat(tvP0.getText().toString()));
		altitude_difference = Math.abs(fltvP1 - fltvP0);
		tvAltdDiff.setText("" + accValue.format(altitude_difference));
		startClickFlag = false;					// Set flag to 0
	}


// Make a user defined Threshold thing strictly ranging from 2.8m - 5m
// than incorporate this to calculate the height of levels !!!

	public static void upperThres(View v){
		String strUpperThreh;
		
		strUpperThreh = tvUpperThreh.getText().toString();
		flUpperThreh = Float.parseFloat(strUpperThreh);

		switch(v.getId()){
			case R.id.buttonMinusThres:
				if(flUpperThreh > 2.8){
					flUpperThreh -= 0.1;
					tvUpperThreh.setText("" + accValue.format(flUpperThreh));
				} break;
				// else display toast notification !
			case R.id.buttonPlusThres:
				if(flUpperThreh < 5){
					flUpperThreh += 0.1;
					tvUpperThreh.setText("" + accValue.format(flUpperThreh));
				} break;
				// else display toast notification !
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		// register this class as a listener for the Pressure Sensor
		mSensorManager.registerListener(mSensorListener,mPressure,SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	protected void onPause() {
		// unregister listener
		super.onPause();
		mSensorManager.unregisterListener(mSensorListener);
	}

	
	private static SensorEventListener mSensorListener = new SensorEventListener(){

		@SuppressLint("NewApi")
		@Override
		public void onSensorChanged(SensorEvent event) {
			
			// if you use this listener as listener of only one sensor (ex, Pressure) , then you don' t need to check sensor type
			if( Sensor.TYPE_ACCELEROMETER == event.sensor.getType() ){
				pressureValue = event.values[1];
				tvPress.setText("" + accValue.format(pressureValue));
				currentHeight = event.values[1];
//				currentHeight = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, pressureValue);
				tvAltd.setText("" + accValue.format(currentHeight));
				
				if(startClickFlag){
					if(currentHeight > (initialHeight + flUpperThreh)){
						initialHeight = currentHeight;
						intlevel += 1;
						tvLevel.setText("" + (intlevel));
					}
					else if(currentHeight < initialHeight - flUpperThreh){
						initialHeight = currentHeight;
						intlevel -= 1;
						tvLevel.setText("" + (intlevel));
					}
				}
			}
		}
		
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
			
		}
		
	};
	
	
	
	
	
	
	
	


	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.press_acc, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
