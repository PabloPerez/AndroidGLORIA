package com.example.gloriaproject;

import java.net.MalformedURLException;
import java.net.URL;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuInflater;

public class Login extends SherlockActivity {

	private static final String NAMESPACE = "http://user.repository.services.gs.gloria.eu/";
	private static String URL="https://venus.datsi.fi.upm.es:8443/Repositories/services/UserRepositoryPort?wsdl"; 
	private static String METHOD_NAME = "authenticateUser";
	private static final String SOAP_ACTION =  "http://user.repository.services.gs.gloria.eu/authenticateUser";

	String[] experimentos= new String[3];

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		experimentos[0]= "sun";
		experimentos[1]= "solar";


		final Button login = (Button) findViewById(R.id.button1);
		login.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				new Authentication().execute(); 

			}

		});
	}

	private class Authentication extends AsyncTask<Void, Void, Boolean> {
		private ProgressDialog Dialog = new ProgressDialog(Login.this);
		int prevOrientation = getRequestedOrientation();

		@Override
		protected Boolean doInBackground(Void... voids) {

			if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			} else if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			} else {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
			}				


			Log.d("DEBUG","AUTHENTICATEUSER");
			SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

			Element[] header = new Element[1];
			header[0] = new Element().createElement(NAMESPACE,"AuthHeader");
			Element usernametoken = new Element().createElement(NAMESPACE, "UsernameToken");
			usernametoken.setAttribute("NAMESPACE", "Id", "UsernameToken-1");
			header[0].addChild(Node.ELEMENT,usernametoken);
			Element username = new Element().createElement(NAMESPACE, "username_tag");
			username.addChild(Node.TEXT, "pablo-tfc");
			header[0].addChild(Node.ELEMENT, username);
			Element password = new Element().createElement(NAMESPACE, "password_tag");
			password.addChild(Node.TEXT, "p@bl0-tfc");
			header[0].addChild(Node.ELEMENT, password);

			request.addProperty("name", "pablo-tfc");
			request.addProperty("password", "p@bl0-tfc");
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11); 
			envelope.implicitTypes = true;
			envelope.dotNet = false;	
			envelope.setOutputSoapObject(request);
			envelope.headerOut = header; 

			Log.d("DEBUG",envelope.bodyOut.toString());
			HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);

			try {
				androidHttpTransport.call(SOAP_ACTION, envelope);
				SoapObject resSoap =(SoapObject)envelope.getResponse();
				Log.d("DEBUG","AUTHENTICATEUSER RES- "+resSoap.toString());
			} catch (Exception exception) {
				Log.d("DEBUG","AUTHENTICATEUSER EXC - "+exception.toString());
			}

			//SystemClock.sleep(3000);
			publishProgress();
			return true;

		}
		// This function is used to send progress back 
		//   to the UI during doInBackground
		@Override
		protected void onProgressUpdate(Void...voids){

			// Log what the functions is doing

		}

		// This function is called when doInBackground is done
		@Override
		protected void onPostExecute(Boolean b){
			Intent i = new Intent(Login.this, Experimentos.class);
			Bundle bundle = new Bundle();
			bundle.putStringArray("Experimentos",experimentos);
			i.putExtras(bundle);               
			setRequestedOrientation(prevOrientation);
			/*try
			{
				if(Dialog.isShowing())
				{
					Dialog.dismiss();
				}
				// do your Display and data setting operation here
			}
			catch(Exception e){}*/
			startActivity(i);
		}
		protected void onPreExecute() {
			Dialog.setMessage("Authenticating.....");
			Dialog.show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_login, menu);
		return true;
	}
}
