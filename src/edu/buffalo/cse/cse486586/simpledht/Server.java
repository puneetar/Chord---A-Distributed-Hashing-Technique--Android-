package edu.buffalo.cse.cse486586.simpledht;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;



import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

public class Server extends AsyncTask<ServerSocket, String, Void> {

	@Override
	protected Void doInBackground(ServerSocket... sockets) {
		ServerSocket serverSocket = sockets[0];
		Socket socket=null;

		try{
			while(true){
				//Log.v("STARTING SERVER", "123");
				socket = serverSocket.accept();
				//Log.v("Server accepting a connection", "123");

				Node n=new Node(socket, new ObjectOutputStream(socket.getOutputStream()), new ObjectInputStream(socket.getInputStream()));
				//Data.in_conn.add(n);

				new ServerImpl().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,n);
				//	socket.close();
			}
		}
		catch(IOException e){
			Log.v("ERROR", "In server creation");
			//e.printStackTrace();
		}
		return null;
	}

	protected void onProgressUpdate(String... strings ){
		super.onProgressUpdate(strings[0]);
		//	TextView textView = (TextView) findViewById(R.id.textView1);
		//textView.append(strings[0] + "\n");
		Log.v("Server reading message", strings[0]);
		return;
	}
}


class ServerImpl extends AsyncTask<Node, String, Void> {

	private Socket socket;
	private ObjectOutputStream oostream;
	private ObjectInputStream oistream;
	private Message message;
	static int count;
	//private MonitorDatabase monitor;

	//static int counter=0;
	@Override
	protected Void doInBackground(Node... node) {
		//	monitor=new MonitorDatabase();
		//Socket serverSocket = sockets[0];
		socket=node[0].getSocket();
		oostream=node[0].getOostream();
		oistream=node[0].getOistream();


		try {
			message = (Message) oistream.readObject();
			if (message.getType() == Message.MSG_CONNECT) {
				//System.out.println("In connect Message");
				connect(message);
				socket.close();

			} else if (message.getType() == Message.MSG_SET_NODE) {
			//	System.out.println("In Set_node message");
				SimpleDhtProvider.setPredSucc(message);
				socket.close();

			} else if (message.getType() == Message.MSG_INSERT) {
			//	System.out.println("In insert message");
				insert(message);
				socket.close();

			} else if (message.getType() == Message.MSG_QUERY) {
			//	System.out.println("In query message");
				query(message);
				socket.close();

			} else if (message.getType() == Message.MSG_L_DUMP) {
			//	System.out.println("In L_Dump message");

			} else if (message.getType() == Message.MSG_G_DUMP) {
			//	System.out.println("In G_Dump message");
				gDump(message);
				socket.close();

			} else {
				System.out.println(" message type cannot be found ***");
			}

		} catch (OptionalDataException e) {
			e.printStackTrace();
			System.out.println("SERVER IMPL class : error in read object");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.out.println("SERVER IMPL class : error in read object");
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("SERVER IMPL class : error in read object");
		}

		return null;
	}

	private void gDump(Message m) {
		// TODO Auto-generated method stub
		Cursor cursor = SimpleDhtProvider.myContentResolver.query(SimpleDhtProvider.myUri, null, "$LDUMP$", null, null);
		ArrayList<Message> arr=new ArrayList<Message>();
		
		while(cursor.moveToNext()){

			int keyIndex = cursor.getColumnIndex(SimpleDhtProvider.KEY_FIELD);
			int valueIndex = cursor.getColumnIndex(SimpleDhtProvider.VALUE_FIELD);

			String key = cursor.getString(keyIndex);
			String value= cursor.getString(valueIndex);

			//matCursor.addRow(new Object[]{key,value});
			arr.add(Message.getMsgInsrt(key, value));
		}
		
		try {
				oostream.writeObject(arr);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private void query(Message m) {
	
		Cursor resultCursor = SimpleDhtProvider.myContentResolver.query(SimpleDhtProvider.myUri, null, m.getKey(), null, null);
		try {
			if(resultCursor.moveToFirst()){
			//	System.out.println("yes i have a row");
			}
			oostream.writeObject(Message.getMsgQueryReply(resultCursor.getString(0), resultCursor.getString(1)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void insert(Message m) {
		ContentValues cv = new ContentValues();
		cv.put(SimpleDhtProvider.KEY_FIELD, message.getKey());
		cv.put(SimpleDhtProvider.VALUE_FIELD, message.getValue());
		System.out.println("**in method insert ");
		SimpleDhtProvider.myContentResolver.insert(SimpleDhtProvider.myUri, cv);
	}

	private void connect(Message m){
		
		try {
			if(SimpleDhtProvider.PORT==SimpleDhtProvider.SUCC_PORT && SimpleDhtProvider.PORT==SimpleDhtProvider.PRED_PORT){
				SimpleDhtProvider.setPredSucc(Message.getMsgSetNode(m.getOrgPort(), m.getOrgPort()));
				oostream.writeObject(Message.getMsgSetNode(SimpleDhtProvider.TELE,SimpleDhtProvider.TELE));
			}
			else{
				if(m.getOrgPort().equalsIgnoreCase("5556")){
					oostream.writeObject(Message.getMsgSetNode(""+5558, ""+5554));
					sendMessage(SimpleDhtProvider.getPort(""+5558), Message.getMsgSetNode(""+5554, ""+5556));
					
				}
				else if(m.getOrgPort().equalsIgnoreCase("5558")){
					oostream.writeObject(Message.getMsgSetNode(""+5554, ""+5556));
					sendMessage(SimpleDhtProvider.getPort(""+5556), Message.getMsgSetNode(""+5558, ""+5554));
				}
				else{
					System.out.println("ERROR: In connect method in SERVER");
				}
				SimpleDhtProvider.setPredSucc(Message.getMsgSetNode(""+5556, ""+5558));
			}
		}  catch (IOException e) {
			e.printStackTrace();
		}
	}		
		
	private void sendMessage(int port,Message m) {

		try {
			Socket socket=new Socket(SimpleDhtProvider.IP, port);
			//System.out.println("send message");
			Node n=new Node(socket, new ObjectOutputStream(socket.getOutputStream()), new ObjectInputStream(socket.getInputStream()));
			
			n.getOostream().writeObject(m);		
			socket.close();
			
		} catch (StreamCorruptedException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private Message sendGetMessage(int port,Message m) {

		Message ret=null;
		try {
			Socket socket=new Socket(SimpleDhtProvider.IP, port);
			//System.out.println("send message: "+port);
			Node n=new Node(socket, new ObjectOutputStream(socket.getOutputStream()), new ObjectInputStream(socket.getInputStream()));
			
			n.getOostream().writeObject(m);	
			ret=(Message)n.getOistream().readObject();
			socket.close();
			
		} catch (StreamCorruptedException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return ret;
	}
	
}
