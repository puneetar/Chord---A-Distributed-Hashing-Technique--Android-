package edu.buffalo.cse.cse486586.simpledht;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class Node {

	private Socket socket;
	private ObjectOutputStream oostream;
	private ObjectInputStream oistream;
	
	Node(Socket s,ObjectOutputStream o,ObjectInputStream i){
		socket=s;
		oostream=o;
		oistream=i;
	}

	public Socket getSocket() {
		return socket;
	}

	public ObjectOutputStream getOostream() {
		return oostream;
	}

	public ObjectInputStream getOistream() {
		return oistream;
	}

}