package edu.buffalo.cse.cse486586.simpledht;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class Database extends SQLiteOpenHelper {
	
	public static final String DATABASE_NAME="datab";
	public static final String TABLE_NAME="key_table";
	public static final String KEY="key";
	public static final String VALUE="value";

	private final String create_query="create table if not exists "+TABLE_NAME+"("+KEY+" text primary key, "+VALUE+" text)";
	
	public Database(Context context, String name, CursorFactory factory,
			int version) {
		super(context, DATABASE_NAME, factory, version);
		
	}
	
	public Database(Context context) {
		this(context, DATABASE_NAME, null, 1);
		
	}
	@Override
	public void onCreate(SQLiteDatabase db) {
		//Log.v("ERROR","Creating the database SQLite DB");
		db.execSQL(create_query);

	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {


	}

}
