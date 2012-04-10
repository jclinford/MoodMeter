package org.cs286.proj3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DBHelp extends SQLiteOpenHelper
{

	private static final int db_version = 3;                      // Variable for setting the Database Version
	public static final String Server_Name = "name";
	public static final String name = "user";          // Local variable for persistence ID
	private static final String DATABASE_NAME = "MoodDb";  // Local variable for DbName
	protected static final String Table_Name = "Persist";		  // Local variable for table name
	private SQLiteDatabase db;
	private DBHelp dbHelper;	

	/** Called when the DB setup is to be done
	 * @param context 
	 * 			the context of the activity class  
	 */
	public DBHelp(Context context) 
	{
		super(context, DATABASE_NAME, null, db_version);
	}
	
	/**
     * This method will create the table 
     * @param SQLiteDatabase
     * 			Assists to create table in the database
     */
	@Override
	public void onCreate(SQLiteDatabase db) 
	{
		db.execSQL(DbCreate);
		
	}
	
	public void open() throws SQLException 
	{
		db = dbHelper.getWritableDatabase();
	}
	
	public void close() 
	{
		dbHelper.close();

	}
	
	public void updateDb(String sName, String uName)
	{
		ContentValues cv = new ContentValues();
		cv.put(Server_Name, sName);
		cv.put(name, uName);
	    dbHelper.getWritableDatabase().update(Table_Name, cv, null, null);
		//dbHelper.getWritableDatabase().execSQL("Update "+Table_Name+" SET "+Server_Name +" , "+name+" = "+uName+" ;");
	}

	// Database creation sql statement
	private static final String DbCreate = " CREATE TABLE IF NOT EXISTS "
			+ Table_Name + " ( " + Server_Name + "TEXT PRIMARY KEY);";
	
	public String readPersistence()
	{
		int state = 0;
		String tempString = null;
		Cursor c = dbHelper.getReadableDatabase().rawQuery("Select "+Server_Name +" , "+ name +" FROM "+Table_Name, null);
		for (int i = 0; i < c.getCount(); i++) {
			c.moveToNext();
			tempString = (c.getString(0).concat(" : ").concat(c.getString(1)));
		}
		return tempString;
	}
	/**
     * This method allows you to update the database schema
     * @param SQLiteDatabase
     * 			Assists to update the database version
     * @param oldVersion
     * 			provides the details of the current version of the dB
     * @param newVersion
     * 			provides the details of the new version of the dB
     */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{
		db.execSQL("DROP TABLE IF EXISTS List1 ");
		//db.execSQL("DROP TABLE IF EXISTS Persist ");
		
		this.onCreate(db);
	}

}
