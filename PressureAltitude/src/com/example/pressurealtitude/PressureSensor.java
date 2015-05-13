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
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Service;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
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
	
	public static final double alpha = 0.5;	
	public static final double gamma = 0.5;	

	public static final int period_in_msec = 1000;
	public static final int delay_in_msec = 1000;
	public static final int mainBuffer_size = 14;			// aprox. 14-17 samples in 3 sec(depending on sensor events), therefore rolling back
															// aprox. 3-6 samples in 1 sec(depending on sensor events), therefore rolling back
	public static final int tempBuffer_size = 3;			// Don't know why 5 instead of 3 ???
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
	static int intlevel = 0;
	
	static int rateChangedFlag;	
	// Creating the Handler object
	Handler rateChangedHandler = new Handler();
	static float bestEstimationValue;


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
		
		// Initializing...
		for(int i = 0; i<constants.mainBuffer_size; i++){
			mainBuffer[i] = 0;
		}
		for(int i = 0; i<constants.tempBuffer_size; i++){
			tempBuffer[i] = 0;
		}
		

		/*----------	T I M E R	T A S K		---------------------*/

		TimerTask rateChangeTask = new TimerTask(){

			private int j;
			private float[] soomthenedBuffer = new float[constants.mainBuffer_size - 1];

			@Override
			public void run() {
				// TODO Auto-generated method stub
//				mSensorManager.unregisterListener(mSensorListener);
				
				// Raising the flag so that sensor event will not overwrite the values in mainBuffer...
				rateChangedFlag = 1;
				Debug.out("\n/*----------	S M O O T H I N G	---------------------*/");
				

				/*----------	S M O O T H I N G	---------------------*/

				for(j = 0; j<constants.mainBuffer_size-1; j++){
					soomthenedBuffer[j] = 0;
				}
				
				// calculating initial values...
				soomthenedBuffer[0] = mainBuffer[1];
				bestEstimationValue = mainBuffer[1] - mainBuffer[0];
				
				for(j = 1; j<constants.mainBuffer_size-1; j++){
					soomthenedBuffer[j] = (float) (constants.alpha * mainBuffer[j+1] + 
							constants.alpha * (soomthenedBuffer[j-1] + bestEstimationValue) );
					bestEstimationValue = (float) (constants.gamma * (soomthenedBuffer[j] - soomthenedBuffer[j-1]) +
							constants.gamma * bestEstimationValue); 
				}

				// debuging...
				Debug.out ("on [0] " + mainBuffer[0]);

				// copy data to mainBuffer...
				for(j = 0; j<constants.mainBuffer_size-1; j++){
					mainBuffer[j+1] = soomthenedBuffer[j];  
					Debug.out ("on [" + (j+1) + "] - " + mainBuffer[j+1]);
				}
				

				// Defining local variables...
/*				float meanPressure = 0;
				float pressureStdDev = 0;
				
				// calculate the parameters: Mean and SD
				meanPressure = 0;
				for(j=0; j<constants.mainBuffer_size; j++){
					meanPressure += mainBuffer[j];
				}
				meanPressure /= constants.mainBuffer_size;
				
				float var = 0;
				for(j=0; j<constants.mainBuffer_size; j++){
					var += ((meanPressure - mainBuffer[j]) * (meanPressure - mainBuffer[j]));
				}
				var /= constants.mainBuffer_size;
				pressureStdDev = (float) Math.sqrt(var);
*/				
				
				// Debug...
//				Don't know why it is not setting text on textViews ???
/*				tvMean.setText("" + meanPressure);
				tvStdDev.setText("" + pressureStdDev);
				tvMinDelay.setText("" + rateChangedFlag);
*/
				Debug.out("\n/*----------	T E M P - B U F F E R - S T A R T E D	----------*/");


				/*----------	T I M E R	T A S K		---------------------*/

				TimerTask postAfterOneSec = new TimerTask() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						
						// post on a Thread for periodic execution...
						// Execute a runnable task as soon as possible
						rateChangedHandler.post(runnableCode);
					}
				};
				

				/*----------	T I M E R	---------------------*/

				Timer oneSec = new Timer();
				oneSec.schedule(postAfterOneSec, constants.period_in_msec);

				// Again Register the Sensor with sampling rate equal to 1 Hz 
//				mSensorManager.registerListener(mSensorListener,mPressure,1000000); // 1000000u sec = 1 sec
			}
			
		};

		
		/*----------	T I M E R	---------------------*/
		
		Timer initialCalculation = new Timer();
		initialCalculation.schedule(rateChangeTask, constants.period_in_msec * 3); 	// for 3 seconds --- having (14-15) sample values...
}
	

	@Override
	protected void onResume() {
		super.onResume();
		// register this class as a listener for the Pressure Sensor
		mainCounter = 0;
		tempCounter = 0;
		mSensorManager.registerListener(mSensorListener,mPressure,SensorManager.SENSOR_DELAY_FASTEST);
	}

	
	@Override
	protected void onPause() {
		super.onPause();

		// Removes pending code execution
		rateChangedHandler.removeCallbacks(runnableCode);				

		// unregister listener
		mSensorManager.unregisterListener(mSensorListener);
	}
	

	// Define the task to be run here
	private Runnable runnableCode = new Runnable() {

		private int i;
	    private int counter = 0;
	    private float median;
		private float meanPressure = 0;
		private float pressureStdDev = 0;
		private float soomthenedValue = 0;
	    private float[] b = new float[tempBuffer.length];
	    
		@Override
	    public void run() {
	      // Do something here

			Log.e("Handlers", "Called");

			// Debug...
			Debug.out("Mean = " + meanPressure);
			Debug.out("SD = " + pressureStdDev);
			tvMean.setText("" + meanPressure);
			tvStdDev.setText("" + pressureStdDev);

			// Calculate Median...
			System.arraycopy(tempBuffer, 0, b, 0, b.length);
			Arrays.sort(b);

			if (tempBuffer.length % 2 == 0){
				median =  (float)((b[(b.length / 2) - 1] + b[b.length / 2]) / 2.0);
			} 
			else{
				median =  b[b.length / 2];
			}
			
			
			/*----------	S M O O T H I N G	---------------------*/
			
			if(mainCounter > 0){
				soomthenedValue = (float) (constants.alpha * median + 
						constants.alpha * (mainBuffer[mainCounter - 1] + bestEstimationValue) );
				bestEstimationValue = (float) (constants.gamma * (soomthenedValue - mainBuffer[mainCounter - 1]) +
						constants.gamma * bestEstimationValue); 

				// Debug...
				Debug.out ("PrevValue at [" + (mainCounter - 1) + "] = " + mainBuffer[mainCounter - 1]);
				Debug.out ("Median = " + median);
				Debug.out ("soomthenedValue = " + soomthenedValue);
			}
			else{
				soomthenedValue = (float) (constants.alpha * median + 
						constants.alpha * (mainBuffer[constants.mainBuffer_size - 1] + bestEstimationValue) );
				bestEstimationValue = (float) (constants.gamma * (soomthenedValue - mainBuffer[constants.mainBuffer_size - 1]) +
						constants.gamma * bestEstimationValue); 

				// Debug...
				Debug.out ("PrevValue at [" + (constants.mainBuffer_size - 1) + "] = " + mainBuffer[constants.mainBuffer_size - 1]);
				Debug.out ("Median = " + median);
				Debug.out ("soomthenedValue = " + soomthenedValue);
			}
			

			//Putting it at the tail of mainBuffer...
			if(mainCounter >= constants.mainBuffer_size){
				mainCounter = 0;			// Roll back...
			}

			mainBuffer[mainCounter] = soomthenedValue;
			Debug.out ("MainCounter at [" + mainCounter + "] = " + mainBuffer[mainCounter]);
			mainCounter++;
			
			// calculate the parameters: Mean and SD
			meanPressure = 0;
			for(i=0; i<constants.mainBuffer_size; i++){
				meanPressure += mainBuffer[i];
			}
			meanPressure /= constants.mainBuffer_size;
			
			float var = 0;
			for(i=0; i<constants.mainBuffer_size; i++){
				var += ((meanPressure - mainBuffer[i]) * (meanPressure - mainBuffer[i]));
			}
			var /= constants.mainBuffer_size;
			pressureStdDev = (float) Math.sqrt(var);
			
			// Repeat this runnable code block again every 1 sec, hence periodic execution...
			rateChangedHandler.postDelayed(runnableCode, constants.delay_in_msec);

			// Debug...
			if(counter >= 1){
				// unregister listener
				mSensorManager.unregisterListener(mSensorListener);
				// Removes pending code execution
				rateChangedHandler.removeCallbacks(runnableCode);				
			}
			counter++;
		}
		
		
	};
	
	
	public final static class Debug{
	    private Debug (){}

	    public static void out (Object msg){
	        Log.i ("info", msg.toString ());
	    }
	}

	
	static int mainCounter = 0;
	static float[] mainBuffer = new float[constants.mainBuffer_size];

	static int tempCounter = 0;
	static float[] tempBuffer = new float[constants.tempBuffer_size];
	
	private static SensorEventListener mSensorListener = new SensorEventListener(){

		private long[] eventTime = new long[constants.mainBuffer_size];
		private int[] eventAcc = new int[constants.mainBuffer_size];

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
							if(tempCounter >= constants.tempBuffer_size){
								tempCounter = 0;			// Roll back...
							}
							
							tempBuffer[tempCounter] = event.values[0];
							eventTime[tempCounter] = event.timestamp;
							eventAcc[tempCounter] = event.accuracy;
							Debug.out ("on [" + tempCounter + "] - " + tempBuffer[tempCounter] + " time interval " + eventTime[tempCounter] + " with Acc = " + eventAcc[tempCounter]);
							tempCounter++;
							
							// Debug...
							tvMinDelay.setText("" + rateChangedFlag);
							break;

					case 0: // at Rate equals 0.02 equivalent to 50 samples per second (scroll for 5 seconds)
							// [all 4 types of delay options are not working], therefore
							// using 3 sec timer to fill the buffer, giving appox. (14-15) sample values...
							if(mainCounter >= constants.mainBuffer_size){
								mainCounter = 0;			// Roll back...
							}
							
							mainBuffer[mainCounter] = event.values[0];
							eventTime[mainCounter] = event.timestamp;
							eventAcc[mainCounter] = event.accuracy;
							Debug.out ("on [" + mainCounter + "] - " + mainBuffer[mainCounter] + " time interval " + eventTime[mainCounter] + " with Acc = " + eventAcc[mainCounter]);
							mainCounter++;
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
				if(flUpperThreh > constants.min_lower_bound){
					flUpperThreh -= 0.1;
					tvUpperThreh.setText("" + accValue.format(flUpperThreh));
				} break;
				// else display toast notification !
			case R.id.buttonPlusThres:
				if(flUpperThreh < constants.max_upper_bound){
					flUpperThreh += 0.1;
					tvUpperThreh.setText("" + accValue.format(flUpperThreh));
				} break;
				// else display toast notification !
		}
	}
	
	
	
	
	
	
	


	

	
	
	
	
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
