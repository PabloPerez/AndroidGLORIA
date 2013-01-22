package com.example.gloriaproject;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockExpandableListActivity;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;

import android.app.ExpandableListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.view.View;

import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Toast;

/*
 * Demonstrates expandable lists backed by a Simple Map-based adapter
 */

public class Experimentos extends SherlockExpandableListActivity  {
	private static final String NAME = "NAME";
	private static final String DESCRIPTION = "DESCRIPTION";
	String[] experimentos = new String[3];
	String[] observatorios = new String[3];
	String[] telescopios = new String[3];

	

	int ESTADO = 0;
	String Experimento = null;
	String Observatorio = null;
	String Telescopio = null;

	private ExpandableListAdapter mAdapter;
	ExpandableListView exView;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getSupportActionBar().setTitle(" Experiment");
		
		experimentos=getIntent().getExtras().getStringArray("Experimentos");
		setList();
		exView=getExpandableListView();
		exView.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				//Toast.makeText(Experimentos.this,"parent="+ groupPosition+"  child=="+childPosition, Toast.LENGTH_SHORT).show();
				switch(groupPosition){
				case 0:
					ESTADO = groupPosition+1;
					Experimento = experimentos[childPosition];
					new GetTelescopios().execute();
					break;
				case 1:
					ESTADO = groupPosition+1;
					Telescopio = telescopios[childPosition];
					Intent i = new Intent(Experimentos.this, Main.class);
					Bundle bundle = new Bundle();
					bundle.putStringArray("Experimentos",experimentos);
					i.putExtras(bundle);
					startActivity(i);
					break;
				}
				return false;
			}
		});
	}

	private void setList(){
			experimentos=getIntent().getExtras().getStringArray("Experimentos");
			List<Map<String, String>> groupData = new ArrayList<Map<String, String>>();
			List<List<Map<String, String>>> childData = new ArrayList<List<Map<String, String>>>();
			Map<String, String> curGroupMap = new HashMap<String, String>();
			groupData.add(curGroupMap);
			
			curGroupMap.put(NAME, "EXPERIMENT");
			curGroupMap.put(DESCRIPTION, "Choose experiment");
			List<Map<String, String>> children = new ArrayList<Map<String, String>>();
			for (int j = 0; j < experimentos.length-1; j++) {
				Map<String, String> curChildMap = new HashMap<String, String>();
				children.add(curChildMap);
				curChildMap.put(NAME, experimentos[j].toString());
			}
			childData.add(children);
			
			if (ESTADO >0){
			Map<String, String> curGroupMap2 = new HashMap<String, String>();
			groupData.add(curGroupMap2);
			curGroupMap2.put(NAME, "TELESCOPE");
			curGroupMap2.put(DESCRIPTION, "Choose telescope");
			children = new ArrayList<Map<String, String>>();
			for (int j = 0; j < telescopios.length-1; j++) {
				Map<String, String> curChildMap = new HashMap<String, String>();
				children.add(curChildMap);
				curChildMap.put(NAME, telescopios[j].toString());
			}
			childData.add(children);
		}
			/*if (ESTADO >0){
				Map<String, String> curGroupMap2 = new HashMap<String, String>();
				groupData.add(curGroupMap2);
				curGroupMap2.put(NAME, "OBSERVATORIO");
				curGroupMap2.put(DESCRIPTION, "Elige el observatorio que deseas");
				children = new ArrayList<Map<String, String>>();
				for (int j = 0; j < observatorios.length-1; j++) {
					Map<String, String> curChildMap = new HashMap<String, String>();
					children.add(curChildMap);
					curChildMap.put(NAME, observatorios[j].toString());
				}
				childData.add(children);
			}
			if (ESTADO >1){
				Map<String, String> curGroupMap2 = new HashMap<String, String>();
				groupData.add(curGroupMap2);
				curGroupMap2.put(NAME, "Telescopio");
				curGroupMap2.put(DESCRIPTION, "Elige el telescopio que deseas");
				children = new ArrayList<Map<String, String>>();
				for (int j = 0; j < telescopios.length-1; j++) {
					Map<String, String> curChildMap = new HashMap<String, String>();
					children.add(curChildMap);
					curChildMap.put(NAME, telescopios[j].toString());
				}
				childData.add(children);
			}*/

		// Set up our adapter
		mAdapter = new SimpleExpandableListAdapter(
				this,
				groupData,
				android.R.layout.simple_expandable_list_item_2,
				new String[] { NAME, DESCRIPTION },
				new int[] { android.R.id.text1, android.R.id.text2 },
				childData,
				android.R.layout.simple_expandable_list_item_2,
				new String[] { NAME, DESCRIPTION },
				new int[] { android.R.id.text1, android.R.id.text2 }
				);
		setListAdapter(mAdapter);
	}
	
	private class GetObservatorios extends AsyncTask<Void, Void, Boolean> {
		private ProgressDialog Dialog = new ProgressDialog(Experimentos.this);
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
			setRequestedOrientation(prevOrientation);
			observatorios[0]= "montegancedo";
			observatorios[1]="tenerife";
			
			try
			{
				if(Dialog.isShowing())
				{
					Dialog.dismiss();
				}
				// do your Display and data setting operation here
			}
			catch(Exception e){}
			
			setList();
		}
		protected void onPreExecute() {
			Dialog.setMessage("Obteniendo Observatorios.....");
			Dialog.show();
		}
	}
	
	private class GetTelescopios extends AsyncTask<Void, Void, Boolean> {
		private ProgressDialog Dialog = new ProgressDialog(Experimentos.this);
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

			//SystemClock.sleep(1000);
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
			setRequestedOrientation(prevOrientation);
			telescopios[0]= "telescopio 1";
			telescopios[1]="telescopio 2";
			
			try
			{
				if(Dialog.isShowing())
				{
					Dialog.dismiss();
				}
				// do your Display and data setting operation here
			}
			catch(Exception e){}
			
			setList();
		}
		protected void onPreExecute() {
			Dialog.setMessage("Getting Telescopes.....");
			Dialog.show();
		}
		
	}

}
