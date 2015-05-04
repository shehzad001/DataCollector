package com.example.pressurealtitude;

import android.support.v7.app.ActionBarActivity;
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.URL;
//import java.net.URLConnection;

//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;

//import org.w3c.dom.Node;
//import org.w3c.dom.NodeList;

//import android.renderscript.Element;








//import android.provider.DocumentsContract.Document;
//import android.util.Log;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Service;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
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

public class PressureSensor extends ActionBarActivity{
	
	
	
	
	
	
	
	


	
/*
* This code explains how to retrieve sea level atmospheric pressure information from a given longitude and latitude using WeatherBug’s API.
*/
/*
	@SuppressLint("InlinedApi")
	private float sea_level_pressure = SensorManager.PRESSURE_STANDARD_ATMOSPHERE;
	
	public static final String TAG_PRESSURE = "aws:pressure";
	public static final String TAG_LONGITUDE = "aws:longitude";
	public static final String TAG_LATITUDE = "aws:latitude";

	protected static final String TAG = null;
	

	private void updatePressure(){

		Thread task = new Thread(new Runnable(){

			public void run(){
				sea_level_pressure = GetRefPressure(longitude, latitude);
				Log.d(TAG, "updated! " + sea_level_pressure);
			} } );
		
		task.start();
	} // this function is


	@SuppressLint({ "DefaultLocale", "InlinedApi" })
	public float GetRefPressure(double longitude, double latitude){

		InputStream is = null;
		float pressure = 0.0f;

		try{
			String strUrl = String.format("http://(insert your API Key).api.wxbug.net/getLiveWeatherRSS.aspx?ACode=(insert your API)&lat=%f&long=%f&UnitType=1&OutputType=1", latitude, longitude);

			URL text = new URL(strUrl);
			Log.d(TAG, text.toString() );
	
			URLConnection connection = text.openConnection();
			connection.setReadTimeout(30000);
			connection.setConnectTimeout(30000);
	
			is = connection.getInputStream();
	
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder domParser = dbf.newDocumentBuilder();
			org.w3c.dom.Document xmldoc = domParser.parse(is);
			Element root = (Element) xmldoc.getDocumentElement();
	
			pressure = Float.parseFloat(getTagValue(TAG_PRESSURE, root) );
	
			float lon = Float.parseFloat(getTagValue(TAG_LONGITUDE, root) );
			float lat = Float.parseFloat(getTagValue(TAG_LATITUDE, root) );
		} 
		
		catch (Exception e){
			Log.e(TAG, "Error in network call", e);
			pressure = SensorManager.PRESSURE_STANDARD_ATMOSPHERE;
		} 
		
		finally{
			try{
				if(is != null)
				is.close();
			} 
			catch (IOException e){
				e.printStackTrace();
			}
		}

		return pressure;
	}
	

	public String getTagValue(String sTag, Element eElement){

		NodeList nlList = ((org.w3c.dom.Document) eElement).getElementsByTagName(sTag).item(0).getChildNodes();
		Node nValue = (Node)nlList.item(0);

		return nValue.getNodeValue();
	}
*/	
	
	
	
	
	
	
	
	


	
	private static SensorManager mSensorManager = null;
	private static Sensor mPressure;
	
	static TextView tvPress;
	static TextView tvAltd;
	static TextView tvP0, tvP1;
	static TextView tvAltdDiff;
	static TextView tvLevel;
	static TextView tvUpperThreh;
	static TextView tvMinDelay;
	static TextView tvMean;
	static TextView tvStdDev;
	static TextView tvLastBufferValue;
			
	static DecimalFormat accValue = new DecimalFormat("###,###.##");
	static float pressureValue = 0.0f;
	static float currentHeight = 0.0f;
	static float initialHeight = 0.0f;
	static float fltvP0 = 0.0f;
	static float fltvP1 = 0.0f;
	static float altitude_difference;
	static float flUpperThreh = (float) constants.min_lower_bound;

	static boolean startClickFlag;
	static int rateChangedFlag;
	static int intlevel = 0;
	
	static float meanPressure = 0;
	static float pressureStdDev = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pressure_sensor);

		//create instance of sensor manager and get system service to interact with Sensor
		mSensorManager = (SensorManager) getSystemService(Service.SENSOR_SERVICE);
		mPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
		
		tvPress = (TextView)findViewById(R.id.textViewPress);
		tvAltd = (TextView)findViewById(R.id.textViewAltd);
		tvP0 = (TextView)findViewById(R.id.textViewP0);
		tvP1 = (TextView)findViewById(R.id.textViewP1);
		tvAltdDiff = (TextView)findViewById(R.id.textViewAltdDiff);
		tvLevel = (TextView) findViewById(R.id.textViewLevelChanged);
		tvUpperThreh = (TextView) findViewById(R.id.textViewUpperThres);
		tvMinDelay = (TextView) findViewById(R.id.textViewMinDelay);
		
		tvUpperThreh.setText("" + accValue.format(constants.min_lower_bound));

		tvMean = (TextView) findViewById(R.id.textViewMean);
		tvStdDev = (TextView) findViewById(R.id.textViewStdDev);
		tvLastBufferValue = (TextView) findViewById(R.id.textViewLastBufferValue);

		rateChangedFlag = 0;
		tvMinDelay.setText("" + rateChangedFlag);
		
// To check any value on display...
//		tvP1.setText("" + flUpperThreh);
		
		TimerTask rateChangeTask = new TimerTask(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				
				mSensorManager.unregisterListener(mSensorListener);
				
				for(int i=0; i<128; i++){
					meanPressure += buffer[i];
				}
				meanPressure /= buffer_size;
				
				float var = 0;
				for(int j=0; j<128 ; j++){
					var += ((meanPressure - buffer[j]) * (meanPressure - buffer[j]));
				}
				var /= buffer_size;
				pressureStdDev = (float) Math.sqrt(var);
				
				// Again Register the Sensor with sampling rate equal to 1 Hz 
				rateChangedFlag = 1;
				mSensorManager.registerListener(mSensorListener,mPressure,1000000); // 1000000u sec = 1 sec
				
//				tvMinDelay.setText("" + rateChangedFlag);
			}
			
		};
		
		/*----------	T I M E R	---------------------*/
		
		Timer initialCalculation = new Timer();
		initialCalculation.schedule(rateChangeTask, 22000); 	// for 5 seconds
	}
	

	public final static class Debug{
	    private Debug (){}

	    public static void out (Object msg){
	        Log.i ("info", msg.toString ());
	    }
	}
	
	public static void startClick(View v){	
//		mSensorManager.registerListener(mSensorListener,mPressure,SensorManager.SENSOR_DELAY_NORMAL);
		tvP0.setText("" + pressureValue);
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
		tvP1.setText("" + pressureValue);
		
		fltvP0 = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, Float.parseFloat(tvP0.getText().toString()));
		fltvP1 = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, Float.parseFloat(tvP1.getText().toString()));

		altitude_difference = Math.abs(fltvP1 - fltvP0);
		tvAltdDiff.setText("" + accValue.format(altitude_difference));
		startClickFlag = false;					// Set flag to 0
	}


// Make a user defined Threshold strictly ranging from 2.8m - 5m
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
		mSensorManager.registerListener(mSensorListener,mPressure,SensorManager.SENSOR_DELAY_GAME);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// unregister listener
		mSensorManager.unregisterListener(mSensorListener);
	}
	
	
	static int counter = 0;
	static final int buffer_size = 128;
	static float[] buffer = new float[buffer_size];
	static int i = buffer_size - 1;

	private static SensorEventListener mSensorListener = new SensorEventListener(){

		@SuppressLint("NewApi")
		@Override
		public void onSensorChanged(SensorEvent event) {
			
			// if you use this listener as listener of only one sensor (ex, Pressure) , then you don' t need to check sensor type
			if( Sensor.TYPE_PRESSURE == event.sensor.getType() ){
				pressureValue = event.values[0];
				tvPress.setText("" + accValue.format(pressureValue));
				currentHeight = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, pressureValue);
				tvAltd.setText("" + accValue.format(currentHeight));

//				tvMinDelay.setText("" + event.sensor.getMinDelay());
				
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
				
				switch(rateChangedFlag){
					case 1:	// at Rate equals 1 Hz
							tvMinDelay.setText("" + rateChangedFlag);
							tvMean.setText("" + meanPressure);
							tvStdDev.setText("" + pressureStdDev);
							tvLastBufferValue.setText("" + buffer[i]);
							tvMinDelay.setText("" + rateChangedFlag);
							
							if(i <= 26){
								i = buffer_size - 1;
							}
							i--;
							break;

					case 0: // at Rate equals 0.02 equivalent to 50 samples per second (scroll for 5 seconds)
							if(counter >= 128){
								counter = 0;
							}
							
							buffer[counter] = pressureValue;
							Debug.out ("" + buffer[counter]);
							counter++;
							break;
					
					default: break;
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
		getMenuInflater().inflate(R.menu.pressure_sensor, menu);
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
