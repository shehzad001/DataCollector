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
import android.widget.TextView;
import android.annotation.SuppressLint;
import android.app.Service;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
//import android.provider.DocumentsContract.Document;
//import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class PressureSensor extends ActionBarActivity{



	
	

	
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
//	Button btStart, btFinish;
	
			
	static float pressure_value = 0.0f;
	static float fltvP0 = 0.0f;
	static float fltvP1 = 0.0f;

	
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
//		btStart = (Button)findViewById(R.id.buttonStart);
//		btStart = (Button)findViewById(R.id.buttonFinish);
		
	}
	

	public static void startClick(View v){	
//		mSensorManager.registerListener(mSensorListener,mPressure,SensorManager.SENSOR_DELAY_NORMAL);
		tvP0.setText("" + pressure_value);
	}

	@SuppressLint({ "NewApi", "InlinedApi" })
	public static void finishClick(View v){
		float altitude_difference;
		
//		mSensorManager.unregisterListener(mSensorListener);
		tvP1.setText("" + pressure_value);
		fltvP1 = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, Float.parseFloat(tvP1.getText().toString()));
		fltvP0 =  SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, Float.parseFloat(tvP0.getText().toString()));
		altitude_difference = fltvP1 - fltvP0;
		tvAltdDiff.setText("" + altitude_difference);
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
			
			float height = 0.0f;

			// if you use this listener as listener of only one sensor (ex, Pressure) , then you don' t need to check sensor type
			if( Sensor.TYPE_PRESSURE == event.sensor.getType() ){
				pressure_value = event.values[0];
				tvPress.setText("" + pressure_value);
				height = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, pressure_value);
				tvAltd.setText("" + height);
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
