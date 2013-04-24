package edu.buffalo.cse.cse486586.simpledht;


import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class SimpleDhtMainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {


	//	System.out.println("activity is called");

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_simple_dht_main);

		TextView tv = (TextView) findViewById(R.id.textView1);
		tv.setMovementMethod(new ScrollingMovementMethod());

		findViewById(R.id.button3).setOnClickListener(
				new OnTestClickListener(tv, getContentResolver()));

		//For L-Dump
		findViewById(R.id.button1).setOnClickListener(
				new OnClickListener(){
					@Override
					public void onClick(View arg0) {
						new AsyncTask<Void, String, Void>(){

							@Override
							protected Void doInBackground(Void... params) {

								Cursor resultCursor = SimpleDhtProvider.myContentResolver.query(SimpleDhtProvider.myUri, null,"$LDUMP$", null, null);
								while(resultCursor.moveToNext()){

									int keyIndex = resultCursor.getColumnIndex(SimpleDhtProvider.KEY_FIELD);
									int valueIndex = resultCursor.getColumnIndex(SimpleDhtProvider.VALUE_FIELD);

									String key = resultCursor.getString(keyIndex);
									String value= resultCursor.getString(valueIndex);

									//	System.out.println("PRINTING: "+"< "+key+" : "+value+" >");
									publishProgress("< "+key+" : "+value+" >\n");	
								}
								return null;
							}

							protected void onProgressUpdate(String... strings ){
								super.onProgressUpdate(strings[0]);
								TextView tv1 = (TextView) findViewById(R.id.textView1);
								tv1.append(strings[0]);
							}
						}.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
					}
				});

		//For G-Dump
		findViewById(R.id.button2).setOnClickListener(
				new OnClickListener(){
					@Override
					public void onClick(View arg0) {
						new AsyncTask<Void, String, Void>(){

							@Override
							protected Void doInBackground(Void... params) {
								
								Cursor resultCursor = SimpleDhtProvider.myContentResolver.query(SimpleDhtProvider.myUri, null,"$GDUMP$", null, null);
								while(resultCursor.moveToNext()){

									int keyIndex = resultCursor.getColumnIndex(SimpleDhtProvider.KEY_FIELD);
									int valueIndex = resultCursor.getColumnIndex(SimpleDhtProvider.VALUE_FIELD);

									String key = resultCursor.getString(keyIndex);
									String value= resultCursor.getString(valueIndex);

									//	System.out.println("PRINTING: "+"< "+key+" : "+value+" >");
									publishProgress("< "+key+" : "+value+" >\n");	
								}
								return null;
							}

							protected void onProgressUpdate(String... strings ){
								super.onProgressUpdate(strings[0]);
								TextView tv1 = (TextView) findViewById(R.id.textView1);
								tv1.append(strings[0]);
							}
						}.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
					}
				});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_simple_dht_main, menu);
		return true;
	}

	protected void onDestroy (){
		this.deleteDatabase(Database.DATABASE_NAME);
		SimpleDhtProvider.context.deleteDatabase(Database.DATABASE_NAME);
	}


}
