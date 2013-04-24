package edu.buffalo.cse.cse486586.simpledht;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;




import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;

public class SimpleDhtProvider extends ContentProvider {

	int port;
	public static String IP ="10.0.2.2",TELE=null,AVDNAME=null,PREDTELE=null,SUCCTELE=null;
	public static int REG_PORT=11108,PORT=0,PRED_PORT=0,SUCC_PORT=0;
	public static String HASH=null,PREDHASH=null,SUCCHASH=null;
	public static ContentResolver myContentResolver;
	public static Uri myUri;
	public static final String KEY_FIELD = "key";
	public static final String VALUE_FIELD = "value";


	public static final Uri DB_URI = Uri.parse("content://edu.buffalo.cse.cse486586.simpledht.provider");
	public static Context context;

	SQLiteDatabase sql_db;
	public static Database data_base;

	//private static Node regNode,predNode,succNode;


	@Override
	public boolean onCreate() {
		getContext().deleteDatabase(Database.DATABASE_NAME);

	//	System.out.println("Content provider is called");
		context=getContext();
	//	System.out.println("in onCreate : going to create database");
		data_base = new Database(context);
		data_base.getReadableDatabase();


		myUri = buildUri("content", "edu.buffalo.cse.cse486586.simpledht.provider");
		myContentResolver = context.getContentResolver();


		TelephonyManager tel =(TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		TELE = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);

		PREDTELE=SUCCTELE=TELE;
		PORT=PRED_PORT=SUCC_PORT=getPort(TELE);
		try {
			HASH=PREDHASH=SUCCHASH=genHash(TELE);
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		}

		new AsyncTask<Void, Void, Void>(){
			@Override
			protected Void doInBackground(Void... params) {
				try {

					try{
						ServerSocket serverSocket = new ServerSocket (10000);
						new Server().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR , serverSocket);
					}catch(Exception e){
						Log.v("ERROR", "In server creation");

					}

					if(PORT!=REG_PORT){

						Socket socket=new Socket(IP, REG_PORT);
					//	System.out.println("I am in connection registration ");
						Node n=new Node(socket, new ObjectOutputStream(socket.getOutputStream()), new ObjectInputStream(socket.getInputStream()));

						n.getOostream().writeObject(Message.getMsgConnect(TELE));
						Message m=(Message)n.getOistream().readObject();

						setPredSucc(m);

						socket.close();
					}
					else
						System.out.println("this is avd0");
				} catch (StreamCorruptedException e) {
					Log.v("ERROR", "connection reigtration");
					e.printStackTrace();
				} catch (UnknownHostException e) {
					Log.v("ERROR", "connection reigtration");
					e.printStackTrace();
				} catch (IOException e) {
					Log.v("ERROR", "connection reigtration");
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					Log.v("ERROR", "connection reigtration");
					e.printStackTrace();
				} 
				return null;
			}
		}.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);

		return false;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {

		try {
			String key=values.getAsString(Database.KEY);
			String val=values.getAsString(Database.VALUE);

			if(PORT==SUCC_PORT&&PORT==PRED_PORT){

				sql_db=data_base.getWritableDatabase();
				sql_db.replace(Database.TABLE_NAME, null, values);
				myContentResolver.notifyChange(uri, null);


			}
			else if(key.startsWith("$force$")){

				ContentValues cv = new ContentValues();
				cv.put(SimpleDhtProvider.KEY_FIELD, key.substring(7));
				cv.put(SimpleDhtProvider.VALUE_FIELD, val);

				//System.out.println("inserting in my database");
				sql_db=data_base.getWritableDatabase();
				sql_db.replace(Database.TABLE_NAME, null, cv);
				myContentResolver.notifyChange(uri, null);

			}
			else{
				if(genHash(key).compareTo(HASH)>=0){
					if(HASH.compareTo(SUCCHASH)>0){
						key="$force$"+key;
						sendMessage(SUCC_PORT, Message.getMsgInsrt(key, val));
					}
					else
						sendMessage(SUCC_PORT, Message.getMsgInsrt(key, val));
				}
				else{
					if(genHash(key).compareTo(PREDHASH)<0){
						if(HASH.compareTo(PREDHASH)<0){
							//							ContentValues cv = new ContentValues();
							//							cv.put(SimpleDhtProvider.KEY_FIELD, key);
							//							cv.put(SimpleDhtProvider.VALUE_FIELD, val);
							//							//System.out.println("inserting in my database");
							sql_db=data_base.getWritableDatabase();
							sql_db.replace(Database.TABLE_NAME, null, values);
							myContentResolver.notifyChange(uri, null);
						}
						else
							sendMessage(PRED_PORT, Message.getMsgInsrt(key, val));
					}
					else{
						
						sql_db=data_base.getWritableDatabase();
						sql_db.replace(Database.TABLE_NAME, null, values);
						myContentResolver.notifyChange(uri, null);		

					}
				}
			}
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			Log.v("ERROR", "Exception ");
			e.printStackTrace();
		}
		return null;
	}



	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		if(selection.equalsIgnoreCase("$LDUMP$")){
			//
			SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
			queryBuilder.setTables(Database.TABLE_NAME);
			sql_db = data_base.getReadableDatabase();
			Cursor cursor = null;
			cursor = queryBuilder.query(sql_db, projection, null,null, null, null, sortOrder);

			cursor.setNotificationUri(myContentResolver, uri);
			return cursor;

		}else if(selection.equalsIgnoreCase("$GDUMP$")){
			//
			SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
			queryBuilder.setTables(Database.TABLE_NAME);
			sql_db = data_base.getReadableDatabase();

			MatrixCursor matCursor=new MatrixCursor(new String[]{Database.KEY,Database.VALUE});

			Cursor cursor = null;
			cursor = queryBuilder.query(sql_db, projection, null,null, null, null, sortOrder);
			while(cursor.moveToNext()){

				int keyIndex = cursor.getColumnIndex(SimpleDhtProvider.KEY_FIELD);
				int valueIndex = cursor.getColumnIndex(SimpleDhtProvider.VALUE_FIELD);

				String key = cursor.getString(keyIndex);
				String value= cursor.getString(valueIndex);

				matCursor.addRow(new Object[]{key,value});

			}


			if(PORT==SUCC_PORT&&PORT==PRED_PORT){

				


			}
			else if(PRED_PORT==SUCC_PORT){

				try {
					Socket socket=new Socket(SimpleDhtProvider.IP, PRED_PORT);
					//System.out.println("send message: "+port);
					Node n=new Node(socket, new ObjectOutputStream(socket.getOutputStream()), new ObjectInputStream(socket.getInputStream()));

					n.getOostream().writeObject(Message.getMsgGDump());	
					ArrayList<Message> ret=(ArrayList<Message>)n.getOistream().readObject();
					socket.close();

					Iterator<Message> it=ret.iterator();
					while(it.hasNext()){
						Message m=it.next();
						matCursor.addRow(new Object[]{m.getKey(),m.getValue()});
					}

				} catch (StreamCorruptedException e) {
					Log.v("ERROR", "Exception ");
					e.printStackTrace();
				} catch (UnknownHostException e) {
					Log.v("ERROR", "Exception ");
					e.printStackTrace();
				} catch (IOException e) {
					Log.v("ERROR", "Exception ");
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					Log.v("ERROR", "Exception ");
					e.printStackTrace();
				}

			}
			else{

				try {
					Socket socket=new Socket(SimpleDhtProvider.IP, PRED_PORT);
					//System.out.println("send message: "+port);
					Node n=new Node(socket, new ObjectOutputStream(socket.getOutputStream()), new ObjectInputStream(socket.getInputStream()));

					n.getOostream().writeObject(Message.getMsgGDump());	
					ArrayList<Message> ret=(ArrayList<Message>)n.getOistream().readObject();
					socket.close();

					Iterator<Message> it=ret.iterator();
					while(it.hasNext()){
						Message m=it.next();
						matCursor.addRow(new Object[]{m.getKey(),m.getValue()});
					}

					Socket socket1=new Socket(SimpleDhtProvider.IP, SUCC_PORT);
				//	System.out.println("send message: "+port);
					Node n1=new Node(socket1, new ObjectOutputStream(socket1.getOutputStream()), new ObjectInputStream(socket1.getInputStream()));

					n1.getOostream().writeObject(Message.getMsgGDump());	
					ArrayList<Message> ret1=(ArrayList<Message>)n1.getOistream().readObject();
					socket1.close();

					Iterator<Message> it1=ret1.iterator();
					while(it1.hasNext()){
						Message m=it1.next();
						matCursor.addRow(new Object[]{m.getKey(),m.getValue()});
					}

				} catch (StreamCorruptedException e) {
					Log.v("ERROR", "Exception ");
					e.printStackTrace();
				} catch (UnknownHostException e) {
					Log.v("ERROR", "Exception ");
					e.printStackTrace();
				} catch (IOException e) {
					Log.v("ERROR", "Exception ");
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					Log.v("ERROR", "Exception ");
					e.printStackTrace();
				}

			}


			cursor.setNotificationUri(myContentResolver, uri);
			return matCursor;

		}else{
			SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
			queryBuilder.setTables(Database.TABLE_NAME);
			sql_db = data_base.getReadableDatabase();
			Cursor cursor = null;
			cursor = queryBuilder.query(sql_db, projection, Database.KEY + "=?", new String[] {selection}, null, null, sortOrder);
			if(cursor.moveToFirst()){
				cursor.setNotificationUri(myContentResolver, uri);
				return cursor;

			}else{
				Message m=sendGetMessage(SUCC_PORT, Message.getMsgQuery(selection));
				MatrixCursor matCursor=new MatrixCursor(new String[]{Database.KEY,Database.VALUE});
				matCursor.addRow(new Object[]{m.getKey(),m.getValue()});
				//System.out.println("Query else block");
				cursor.setNotificationUri(myContentResolver, uri);
				return matCursor;
			}
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		return 0;
	}

	public static String genHash(String input) throws NoSuchAlgorithmException {
		MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
		byte[] sha1Hash = sha1.digest(input.getBytes());
		Formatter formatter = new Formatter();
		for (byte b : sha1Hash) {
			formatter.format("%02x", b);
		}
		return formatter.toString();
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	private void sendMessage(int port,Message m) {

		try {
			Socket socket=new Socket(SimpleDhtProvider.IP, port);
			//System.out.println("send message: "+port);
			Node n=new Node(socket, new ObjectOutputStream(socket.getOutputStream()), new ObjectInputStream(socket.getInputStream()));

			n.getOostream().writeObject(m);		
			socket.close();

		} catch (StreamCorruptedException e) {
			Log.v("ERROR", "Exception ");
			e.printStackTrace();
		} catch (UnknownHostException e) {
			Log.v("ERROR", "Exception ");
			e.printStackTrace();
		} catch (IOException e) {
			Log.v("ERROR", "Exception ");
			e.printStackTrace();
		}
	}

	private Message sendGetMessage(int port,Message m) {

		Message ret=null;
		try {
			Socket socket=new Socket(SimpleDhtProvider.IP, port);
		//	System.out.println("send message: "+port);
			Node n=new Node(socket, new ObjectOutputStream(socket.getOutputStream()), new ObjectInputStream(socket.getInputStream()));

			n.getOostream().writeObject(m);	
			ret=(Message)n.getOistream().readObject();
			socket.close();

		} catch (StreamCorruptedException e) {
			Log.v("ERROR", "Exception ");
			e.printStackTrace();
		} catch (UnknownHostException e) {
			Log.v("ERROR", "Exception ");
			e.printStackTrace();
		} catch (IOException e) {
			Log.v("ERROR", "Exception ");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			Log.v("ERROR", "Exception ");
			e.printStackTrace();
		}

		return ret;
	}
	private Uri buildUri(String scheme, String authority) {
		Uri.Builder uriBuilder = new Uri.Builder();
		uriBuilder.authority(authority);
		uriBuilder.scheme(scheme);
		return uriBuilder.build();
	}

	public static void setPredSucc(Message m) {

		try {
			PREDTELE=m.getPredecessor();
			PRED_PORT=getPort(PREDTELE);
			PREDHASH=genHash(PREDTELE);
		//	System.out.println("SETTING PRED to :"+PREDTELE);
			SUCCTELE=m.getSuccessor();
			SUCC_PORT=getPort(SUCCTELE);
			SUCCHASH=genHash(SUCCTELE);
		//	System.out.println("SETTING SECC to :"+SUCCTELE);
		} catch (NoSuchAlgorithmException e) {
			Log.v("ERROR", "Exception ");
			e.printStackTrace();
		}
	}

	public static int getPort(String no){
		if(no.equalsIgnoreCase("5554"))
			return 11108;
		else if(no.equalsIgnoreCase("5556"))
			return 11112;
		else if(no.equalsIgnoreCase("5558"))
			return 11116;
		else{
			System.out.println("UNABLE TO GET PORT");
			return 0;
		}	
	}

	public static String getTele(String port){
		if(port.equalsIgnoreCase("11108"))
			return "5554";
		else if(port.equalsIgnoreCase("11112"))
			return "5556";
		else if(port.equalsIgnoreCase("11116"))
			return "5558";
		else{
			System.out.println("UNABLE TO GET TELE");
			return "0";
		}	
	}
}




