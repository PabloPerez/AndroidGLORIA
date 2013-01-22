package com.example.gloriaproject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Bitmap.Config;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.v4.view.ViewPager;
import android.text.format.Time;
import android.util.FloatMath;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class Telescope extends SherlockFragment 
//implements OnTouchListener
{

	// We can be in one of these 3 states
	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	int mode = NONE;
	PointF start = new PointF();
	PointF mid = new PointF();
	float oldDist = 1f;
	AsyncTask down = null;
	static AsyncTask<String, Void, Boolean> cap = null;
	boolean guardar = false;
	static ProgressBar progressBar;
	//ImageView Image;
	ImageView Play;
	ImageView Settings;
	ImageView MenuDown;
	ImageView MenuUp;
	static boolean AutMode = false;
	Integer BrightnessM = 0;
	Integer Brightness = 0;
	String movimiento = new String();
	Matrix matrix = new Matrix();
	Matrix savedMatrix = new Matrix();
	boolean isMenuShowing = false;
	TouchImageView Image; 
	boolean running= true;
	static SherlockFragmentActivity thisactivity;


	float actualX =  0;
	float actualY =  0;
	float moveX=0;
	float moveY=0;

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
		View fragmentView = inflater.inflate(R.layout.activity_telescope, container, false);
		Image = (TouchImageView) fragmentView.findViewById(R.id.imageView1);
		Image.setMaxZoom(10f);
		Play = (ImageView) fragmentView.findViewById(R.id.imageView2);
		Settings = (ImageView) fragmentView.findViewById(R.id.imageView3);
		MenuDown = (ImageView) fragmentView.findViewById(R.id.imageView9);
		MenuUp = (ImageView) fragmentView.findViewById(R.id.imageView11);
		View l1 = fragmentView.findViewById(R.id.linearLayout9); 
		l1.bringToFront();
		l1.setVisibility(View.INVISIBLE);
		isMenuShowing = false;
		//Image.setVisibility(View.GONE);
		progressBar = (ProgressBar)fragmentView.findViewById(R.id.progressBar1);
		//progressBar.getIndeterminateDrawable().setColorFilter(0xFFFF0000, android.graphics.PorterDuff.Mode.MULTIPLY);
		progressBar.setVisibility(View.GONE);

		thisactivity = (SherlockFragmentActivity) getActivity();
		cap = new getImageCap();

		Play.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!AutMode)
				{
					Image.setVisibility(View.GONE);
					AutMode=true;
					down = new Play().execute();
					Play.setImageDrawable(getResources().getDrawable(R.drawable.pause_button));

				}
				else
				{
					Play.setImageDrawable(getResources().getDrawable(R.drawable.play_button));
					progressBar.setVisibility(View.GONE);
					AutMode=false;
					running=false;
				}

			}
		});	

		MenuDown.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isMenuShowing)
				{
					Animation mAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_bottom);
					View l1 = getView().findViewById(R.id.linearLayout9); 
					l1.startAnimation(mAnim);
					l1.setVisibility(View.INVISIBLE);
					isMenuShowing = false;
				}
			}
		});

		MenuUp.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!isMenuShowing)
				{
					Animation mAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_top);
					View l1 = getView().findViewById(R.id.linearLayout9); 
					l1.bringToFront();
					l1.startAnimation(mAnim);
					l1.setVisibility(View.VISIBLE);
					isMenuShowing = true;
				}
			}
		});


		Settings.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final SharedPreferences prefs =getActivity().getSharedPreferences("GLORIAPrefs",Context.MODE_PRIVATE);
				BrightnessM= prefs.getInt("Brightness", BrightnessM);
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				// Get the layout inflater
				LayoutInflater inflater = getActivity().getLayoutInflater();
				final View layout = inflater.inflate(R.layout.dialog, null);
				// Inflate and set the layout for the dialog
				// Pass null as the parent view because its going in the dialog layout
				builder.setView(layout)
				.setCancelable(true)
				.setPositiveButton("Update Values",
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {				
						SharedPreferences.Editor editor = prefs.edit();
						editor.putInt("Brightness", BrightnessM);
						editor.commit();
						new updateValues().execute();
					}
				})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						dialog.cancel();
					}
				});

				// create alert dialog
				AlertDialog alertDialog = builder.create();
				SeekBar seekBar = (SeekBar)layout.findViewById(R.id.seekBar1);
				seekBar.setMax(200);
				seekBar.setProgress(Brightness);
				final TextView BrightnessT = (TextView)layout.findViewById(R.id.textView1);
				BrightnessT.setText("Brightness: "+String.valueOf(Brightness));
				seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
					@Override
					public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
						BrightnessT.setText("Brightness: "+String.valueOf(progress));
						BrightnessM = progress;
					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {

					}

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {

					}
				});
				alertDialog.show();

			}
		});	
		//Image.setOnTouchListener(this);
		setRetainInstance(true);
		return fragmentView;

	}

	public static Bitmap getRemoteImage(final URL aURL) {
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
			//String id = startContinueMode();		
			String id = "A";		

			if (id != null)
			{
				//String imgURL = getImageURL(id);
				//startContinueMode();

				while (running){

					// Log what the function is doing		       
					// Call the progress function, pass the current loop iteration
					String imgURL= "http://www.readthehook.com/files/cam/cam.jpg";
					
							try {
								bitmap = getRemoteImage(new URL(imgURL));
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

					publishProgress();
				/*	if (guardar)
					{

						File folder = new File(Environment.getExternalStorageDirectory()+"/Pictures/GLORIA/");
						if(!folder.exists()) folder.mkdirs();

						try {
							FileOutputStream fos = new FileOutputStream(new File(Environment.getExternalStorageDirectory()+"/Pictures/GLORIA/file.png"));
							bitmap.compress(CompressFormat.PNG, 100, fos);
							fos.flush();
							fos.close();
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						Log.d("DEBUG","COMPARTO");
						guardar = false;

						Intent share = new Intent(Intent.ACTION_SEND);
						share.setType("image/png");

						share.putExtra(Intent.EXTRA_STREAM,
								Uri.parse("file:///sdcard/Pictures/GLORIA/file.png"));

						startActivity(Intent.createChooser(share, "Comparte tu imagen!"));
					}*/
					// Sleep for 500ms
					SystemClock.sleep(3000);
				}
			}
			return true;
		}
		// This function is used to send progress back 
		//   to the UI during doInBackground
		@Override
		protected void onProgressUpdate(Void...voids){
			Log.d("DEBUG","PLAY - ONPROGRESS");
			if(running) {
				// Log what the functions is doing
				ImageView imageview1 = (ImageView)getView().findViewById(R.id.imageView1);
				imageview1.setImageBitmap(bitmap);
				//imageview1.setImageDrawable(getResources().getDrawable(R.drawable.back_image));
				Image.setVisibility(View.VISIBLE);
			}
			progressBar.setVisibility(View.GONE);
		}

		// This function is called when doInBackground is done
		@Override
		protected void onPostExecute(Boolean b){
			// Log what the functions is doing
			Log.d("DEBUG","PLAY - ONPOST");
			progressBar.setVisibility(View.GONE);

		}     
		@Override
		protected void onPreExecute() {
			Log.d("DEBUG","PLAY - ONPRE");
			progressBar.setVisibility(View.VISIBLE);
			running=true;

			if (running)
			{
				Log.d("DEBUG","RUNNING");
			}

		}
		@Override
		protected void onCancelled() {
			if (running)
			{
				Log.d("DEBUG","CANCELLED");
			}
			//running = false;
		}


	}

	/*@Override
	public boolean onTouch(View arg0, MotionEvent event) {
		float totalScale;
		//TextView gesto = (TextView) getView().findViewById(R.id.textView1);
		dumpEvent(event);
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			savedMatrix.set(matrix);
			start.set(event.getX(), event.getY());
			Log.d("GESTO", "mode=DRAG" );
			mode = DRAG;
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
			Log.d("GESTO", movimiento);

			if (mode ==DRAG)
			{
				actualX = actualX + moveX;
				actualY = actualY + moveY;

				/*if (movimiento == "right")
				{

					Toast toast1 =
							Toast.makeText(getActivity().getApplicationContext(),"Moving mount right", Toast.LENGTH_SHORT);
					toast1.show();	
				}
				if (movimiento == "left")
				{
					Toast toast1 =
							Toast.makeText(getActivity().getApplicationContext(),"Moving mount left", Toast.LENGTH_SHORT);
					toast1.show();	
				}
				if (movimiento == "down")
				{
					Toast toast1 =
							Toast.makeText(getActivity().getApplicationContext(),"Moving mount down", Toast.LENGTH_SHORT);
					toast1.show();	
				}
				if (movimiento == "up")
				{
					Toast toast1 =
							Toast.makeText(getActivity().getApplicationContext(),"Moving mount up", Toast.LENGTH_SHORT);
					toast1.show();	
				}
			}
			mode = NONE;
			Log.d("GESTO", "POINTER UP" );
			break;
		case MotionEvent.ACTION_MOVE:
			if (mode == DRAG) {
				if((event.getX()-start.x>0)&&((event.getX()-start.x)>Math.abs(event.getY() - start.y)))
				{
					movimiento = "right";
				}
				if((event.getX()-start.x<0)&&(Math.abs(event.getX()-start.x)>Math.abs(event.getY() - start.y)))
				{
					movimiento = "left";
				}
				if((event.getY()-start.y>0)&&((event.getY()-start.y)>Math.abs(event.getX() - start.x)))
				{
					movimiento = "down";
			}
				if((event.getY()-start.y<0)&&(Math.abs(event.getY()-start.y)>Math.abs(event.getX() - start.x)))
				{
					movimiento = "up";

				}
			if (mode == DRAG) {
				matrix.set(savedMatrix);
				moveX = event.getX()-start.x;
				if ((actualX + moveX)<0){moveX=0;};
				moveY = event.getY()-start.y;
				if ((actualY + moveY)<0){moveY=0;};
				matrix.postTranslate(moveX,moveY);
				Log.d("GESTO", "Actual X: "+String.valueOf(actualX)+ " Actual Y: " +String.valueOf(actualY)+ " Movimiento X: "+String.valueOf(moveX)+" MovimientoY: " +String.valueOf(moveY));
				//gesto.setText(movimiento);

			}
			else if (mode == ZOOM) {
				float newDist = spacing(event);
				Log.d("EVENTOS", "newDist=" + newDist);
				if (newDist > 10f) {
					float scale = newDist / oldDist;
					movimiento ="ZOOM ESCALA " +scale;
					Log.d("GESTO", movimiento );
					//gesto.setText(movimiento);
				}
			}
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			oldDist = spacing(event);
			Log.d("EVENTOS", "oldDist=" + oldDist);
			if (oldDist > 10f) {
				midPoint(mid, event);
				mode = ZOOM;
				Log.d("EVENTOS", "mode=ZOOM" );
			}
			break;
		}
		Image.setImageMatrix(matrix);
		return false;
	}*/

	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	private void midPoint(PointF point, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}

	private void dumpEvent(MotionEvent event) {
		String names[] = { "DOWN" , "UP" , "MOVE" , "CANCEL" , "OUTSIDE" ,
				"POINTER_DOWN" , "POINTER_UP" , "7?" , "8?" , "9?" };
		StringBuilder sb = new StringBuilder();
		int action = event.getAction();
		int actionCode = action & MotionEvent.ACTION_MASK;
		sb.append("event ACTION_" ).append(names[actionCode]);
		if (actionCode == MotionEvent.ACTION_POINTER_DOWN
				|| actionCode == MotionEvent.ACTION_POINTER_UP) {
			sb.append("(pid " ).append(
					action >> MotionEvent.ACTION_POINTER_INDEX_SHIFT);
			sb.append(")" );
		}
		sb.append("[" );
		for (int i = 0; i < event.getPointerCount(); i++) {
			sb.append("#" ).append(i);
			sb.append("(pid " ).append(event.getPointerId(i));
			sb.append(")=" ).append((int) event.getX(i));
			sb.append("," ).append((int) event.getY(i));
			if (i + 1 < event.getPointerCount())
				sb.append(";" );
		}
		sb.append("]" );
		Log.d("EVENTOS", sb.toString());
	}

	@Override
	public void onStop() {
		super.onStop();
		//check the state of the task
		if(down != null && down.getStatus() == Status.RUNNING)
			//down.cancel(true);
			running=false;
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

	public void setBrightness (int Bright)
	{
		Log.d("DEBUG","SETBRIGHTNESS");
		String id = null;
		METHOD_NAME = "setBrightness";
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
			Log.d("DEBUG","SETBRIGHTNESS RES- "+resSoap.toString());
		} catch (Exception exception) {
			Log.d("DEBUG","SETBRIGHTNESS EXC - "+exception.toString());
		}
	}

	private class updateValues extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... voids) {
			Log.d("DEBUG","UPDATE - DOINB");
			int BCopy;


			if (Brightness != BrightnessM)
			{
				BCopy = BrightnessM;
				Brightness = BCopy;
				setBrightness(BrightnessM);
			}

			return true;
		}
		// This function is used to send progress back 
		//   to the UI during doInBackground
		@Override
		protected void onProgressUpdate(Void...voids){
		}

		// This function is called when doInBackground is done
		@Override
		protected void onPostExecute(Boolean b){
			Log.d("DEBUG","UPDATE - ONPOST");

			if (AutMode)
			{
				progressBar.setVisibility(View.GONE);
			}

		}     
		@Override
		protected void onPreExecute() {
			Log.d("DEBUG","UPDATE - ONPRE");

			if (AutMode)
			{
				progressBar.setVisibility(View.VISIBLE);
			}

		}
	}

	private static class getImageCap extends AsyncTask<String, Void, Boolean> {
		String URL;
		@Override
		protected Boolean doInBackground(String... string) {

			File folder = new File(Environment.getExternalStorageDirectory()+"/"+string[0]);
			if(!folder.exists()) folder.mkdirs();
			URL = string[0];	
			Log.d("DEBUG","GETIMAGECAP - DOINB: "+URL);
			publishProgress();
			try {
				Calendar c = Calendar.getInstance(); 
				String filename= ""+c.get(Calendar.YEAR)+c.get(Calendar.MONTH)+c.get(Calendar.DAY_OF_MONTH)+c.get(Calendar.HOUR)+c.get(Calendar.MINUTE)+c.get(Calendar.SECOND)+".png";
				FileOutputStream fos = new FileOutputStream(new File(Environment.getExternalStorageDirectory()+"/"+URL+"/"+filename));
				Log.d("DEBUG","GETIMAGECAP - DOINB: /Pictures/GLORIA/"+filename);
				//String imgURL = getImageURL(id);
				String imgURL = "http://tornasol.datsi.fi.upm.es/catalejo/imgout.jpg";
				Bitmap bitmap = getRemoteImage(new URL(imgURL));
				bitmap.compress(CompressFormat.PNG, 100, fos);
				fos.flush();
				fos.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.d("DEBUG","GETIMAGECAP - DOINB: ERROR");
				e.printStackTrace();
			}
			return true;
		}

		protected void onProgressUpdate(Void...voids){
			Log.d("DEBUG","GETIMAGECAP - ONPROGRESS");
			progressBar.setVisibility(View.VISIBLE);
			Toast toast1 = Toast.makeText(thisactivity.getApplicationContext(),"Saving image to: "+URL, Toast.LENGTH_SHORT);
			toast1.show();	
		}

		// This function is called when doInBackground is done	
		@Override
		protected void onPostExecute(Boolean b){
			Log.d("DEBUG","GETIMAGECAP - ONPOST");
			cap= new getImageCap();
			progressBar.setVisibility(View.GONE);
		}     
	}

	public static void takeImageCap() {
		progressBar.setVisibility(View.VISIBLE);
		final SharedPreferences prefs =thisactivity.getSharedPreferences("GLORIAPrefs",Context.MODE_PRIVATE);
		Boolean remember = prefs.getBoolean("RememberGallery", false);
		String route = prefs.getString("GalleryRoute", "default");

		if (!remember)
		{	
			final SharedPreferences.Editor editor = prefs.edit();
			AlertDialog.Builder builder = new AlertDialog.Builder(thisactivity);
			// Get the layout inflater
			LayoutInflater inflater = thisactivity.getLayoutInflater();
			final View layout = inflater.inflate(R.layout.dialog_saveimage, null);
			// Inflate and set the layout for the dialog
			// Pass null as the parent view because its going in the dialog layout
			builder.setView(layout)
			.setCancelable(true)
			.setPositiveButton("Save",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					EditText saveto = (EditText)layout.findViewById(R.id.saveTo);
					if(saveto.getText().length()!= 0){			
						Log.d("DEBUG","GETCAP "+saveto.getText());
						String url = saveto.getText().toString();
	
						editor.putString("GalleryRoute", url);
						editor.commit();
						cap.execute(url);
					}
					else
					{
						Log.d("DEBUG","GETCAP DEFAULT");
						SharedPreferences.Editor editor = prefs.edit();
						editor.putString("GalleryRoute", "Pictures/GLORIA");
						editor.commit();
						cap.execute("Pictures/GLORIA");
					}
					if(((CheckBox) layout.findViewById(R.id.doNotAsk)).isChecked())
					{
						editor.putBoolean("RememberGallery", true);
						editor.commit();
					}
				}
			})
			.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					dialog.cancel();
				}
			});

			// create alert dialog
			AlertDialog alertDialog = builder.create();
			progressBar.setVisibility(View.GONE);
			alertDialog.show();
		}
		else
		{
			cap.execute(route);	
		}
	}

}