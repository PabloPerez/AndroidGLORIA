package com.example.gloriaproject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class Dome extends SherlockFragment 
//implements OnTouchListener
{


	AsyncTask PlayDome = null;
	ProgressBar ProgressBar;
	ImageView Dome;
	Boolean Running = true;
	Boolean DomeOpen = false;


	private static final String NAMESPACE = "http://ccd.teleoperation.services.gs.gloria.eu/";
	private static String URL="http://192.168.1.129:8088/CCD"; 
	private static String METHOD_NAME = "startContinueMode";
	private static final String SOAP_ACTION =  "http://ccd.teleoperation.services.gs.gloria.eu/startContinueMode";



	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View fragmentView = inflater.inflate(R.layout.activity_dome, container, false);

		Dome = (ImageView) fragmentView.findViewById(R.id.DomeImage);
		Dome.setVisibility(View.INVISIBLE);

		ProgressBar = (ProgressBar)fragmentView.findViewById(R.id.DomeProgressBar);
		//progressBar.setVisibility(View.GONE);
		
		PlayDome = new Play().execute();

		Dome.setOnLongClickListener(new OnLongClickListener(){
			@Override
			public boolean onLongClick(View v) {
				if (DomeOpen)
				{
					//CloseDome();
					DomeOpen = false;
					Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Closing Dome", Toast.LENGTH_SHORT);
					toast.show();
					SherlockFragmentActivity Activity = (SherlockFragmentActivity) getActivity();
					Activity.getSupportActionBar().setTitle(" Dome Status: Closed");
				}
				else
				{
					//OpenDome();
					DomeOpen = true;
					Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Opening Dome", Toast.LENGTH_SHORT);
					toast.show();
					SherlockFragmentActivity Activity = (SherlockFragmentActivity) getActivity();
					Activity.getSupportActionBar().setTitle(" Dome Status: Open");
				}
				return false;
			}

		});

		setRetainInstance(true);
		return fragmentView;
	}

	public Bitmap getRemoteImage(final URL aURL) {
		try {
			final URLConnection conn = aURL.openConnection();
			conn.connect();
			final BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
			final Bitmap bm = BitmapFactory.decodeStream(bis);
			bis.close();
			return bm;
		} catch (IOException e) {}
		return null;
	}

	private class Play extends AsyncTask<Void, Void, Boolean> {

		Bitmap bitmap = Bitmap.createBitmap(1, 1, Config.ARGB_8888);

		@Override
		protected Boolean doInBackground(Void... voids) {
			// Make a copy of the runLength Integer		      
			// Run you loop to update the imageView
			Log.d("DEBUG","PLAY - DOINB");
			//String id = getDomeURL();		
			String id = "http://tornasol.datsi.fi.upm.es/catalejo/imgout.jpg";		

			if (id != null)
			{
				//String imgURL = getImageURL(id);

				while (Running){

					// Log what the function is doing		       
					// Call the progress function, pass the current loop iteration
						try {
						bitmap = getRemoteImage(new URL(id));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					 
					publishProgress();

					// Sleep for 500ms
					SystemClock.sleep(5000);
				}
			}
			return true;
		}
		// This function is used to send progress back 
		//   to the UI during doInBackground
		@Override
		protected void onProgressUpdate(Void...voids){
			Log.d("DEBUG","PLAY - ONPROGRESS");
			if(Running) {
				// Log what the functions is doing
				ImageView DomeImage = (ImageView)getView().findViewById(R.id.DomeImage);
				DomeImage.setImageBitmap(bitmap);
				Dome.setVisibility(View.VISIBLE);
			}
			ProgressBar.setVisibility(View.GONE);
		}

		// This function is called when doInBackground is done
		@Override
		protected void onPostExecute(Boolean b){
			// Log what the functions is doing
			Log.d("DEBUG","PLAY - ONPOST");

		}     
		@Override
		protected void onPreExecute() {
			Log.d("DEBUG","PLAY - ONPRE");
			Running=true;

			if (Running)
			{
				Log.d("DEBUG","RUNNING");
			}

		}
		@Override
		protected void onCancelled() {
			if (Running)
			{
				Log.d("DEBUG","CANCELLED");
			}
			Running = false;
		}


	}



	@Override
	public void onStop() {
		super.onStop();
		//check the state of the task
		if(PlayDome != null && PlayDome.getStatus() == Status.RUNNING)
			//down.cancel(true);
			Running=false;
	}

	public void startExposition()
	{
		Vibrator vibrator = (Vibrator)getActivity().getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(150); // 50 is time in ms
	}


	static public String startContinueMode()
	{
		Log.d("DEBUG","STARTCONT");
		String id = null;
		METHOD_NAME = "startContinueMode";
		SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11); 
		envelope.implicitTypes = true;
		envelope.dotNet = false;	
		envelope.setOutputSoapObject(request);
		Log.i("request",envelope.bodyOut.toString());
		HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);

		try {
			androidHttpTransport.call(SOAP_ACTION, envelope);
			SoapObject resSoap =(SoapObject)envelope.getResponse();
			id = resSoap.getPropertyAsString("message");
			Log.d("DEBUG","STARTCONT RES- "+resSoap.toString());
		} catch (Exception exception) {
			Log.d("DEBUG","STARTCONT EXC - "+exception.toString());
		}
		return id;
	}

	public String getImageURL(String id)
	{
		Log.d("DEBUG","GETIMAGEURL");
		SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
		request.addProperty("arguments", id);
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11); 
		envelope.implicitTypes = true;
		envelope.dotNet = false;	
		envelope.setOutputSoapObject(request);
		Log.i("request",envelope.bodyOut.toString());
		HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);

		try {
			androidHttpTransport.call(SOAP_ACTION, envelope);
			SoapObject resSoap =(SoapObject)envelope.getResponse();
			id = resSoap.getPropertyAsString("message");
			Log.d("DEBUG","GETIMAGEURL RES- "+resSoap.toString());
		} catch (Exception exception) {
			Log.d("DEBUG","GETIMAGEURL EXC - "+exception.toString());
		}
		return id;
	}

}