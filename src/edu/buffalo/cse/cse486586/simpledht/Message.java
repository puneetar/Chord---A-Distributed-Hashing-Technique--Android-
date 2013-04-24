package edu.buffalo.cse.cse486586.simpledht;

import java.io.Serializable;
import java.util.HashMap;


public class Message implements Serializable{

	public static final int MSG_CONNECT=0;
	public static final int MSG_SET_NODE=1;
	public static final int MSG_QUERY=2;
	
	public static final int MSG_INSERT=3;
	public static final int MSG_L_DUMP=4;
	public static final int MSG_G_DUMP=5;
	public static final int MSG_QUERY_REPLY=6;
	
	private int type;
	private String orgPort;
	private String fromPort;
	private String predecessor;
	private String successor;
	private String value;
	private String key;
	
	
	
	private Message(int t,String orgP,String fromP,String pred,String succ,String val, String key){
		this.type=t;
		this.orgPort=orgP;
		this.fromPort=fromP;
		this.predecessor=pred;
		this.successor=succ;
		this.value=val;
		this.key=key;
		
	}
	
	private Message(int t){
		this(t,null,null,null,null,null,null);
	}
	
	
	public static Message getMsgConnect(String orgP) {
		return new Message(MSG_CONNECT,orgP,null,null,null,null,null);
	}

	public static Message getMsgSetNode(String pred,String succ) {
		return new Message(MSG_SET_NODE,SimpleDhtProvider.TELE,SimpleDhtProvider.TELE,pred,succ,null,null);
	}
	
	public static Message getMsgInsrt(String key,String val) {
		return new Message(MSG_INSERT, SimpleDhtProvider.TELE, SimpleDhtProvider.TELE, null, null, val, key);
	}

	public static Message getMsgQuery(String key) {
		return new Message(MSG_QUERY, SimpleDhtProvider.TELE, SimpleDhtProvider.TELE, null, null, null, key);
	}
	
	public static Message getMsgQueryReply(String key,String val){
		return new Message(MSG_QUERY_REPLY, SimpleDhtProvider.TELE, SimpleDhtProvider.TELE, null, null, val, key);
	}

	public static int getMsgLDump() {
		return MSG_L_DUMP;
	}

	public static Message getMsgGDump() {
		return new Message(MSG_G_DUMP, SimpleDhtProvider.TELE, SimpleDhtProvider.TELE, null, null, null,null);
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getOrgPort() {
		return orgPort;
	}

	public void setOrgPort(String orgPort) {
		this.orgPort = orgPort;
	}

	public String getFromPort() {
		return fromPort;
	}

	public void setFromPort(String fromPort) {
		this.fromPort = fromPort;
	}

	public String getPredecessor() {
		return predecessor;
	}

	public void setPredecessor(String predecessor) {
		this.predecessor = predecessor;
	}

	public String getSuccessor() {
		return successor;
	}

	public void setSuccessor(String successor) {
		this.successor = successor;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}




	
	
}
