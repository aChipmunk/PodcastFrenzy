package com.achipmunkdev.podcasts;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.achipmunkdev.podcasts.DatabaseConstants.Constants;

public class AddedFeedsDbHelper extends SQLiteOpenHelper{
	public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "UserCreatedData.db";
	
	public AddedFeedsDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
    
    public void onCreate(SQLiteDatabase db){
    	//db.execSQL("CREATE TABLE " + Constants.TABLE_NAME_ENTRIES + " (" + Constants._ID + " INTEGER PRIMARY KEY," +
		//		Constants.COLUMN_NAME_FEED_URL + Constants.TEXT_TYPE + 
		//		")");
    	db.execSQL(Constants.SQL_CREATE_USER_ADDED_FEEDS_TABLE);
    	Log.w("PodFrenzy", "making sql table");
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
    	db.execSQL("DROP TABLE IF EXISTS Constants.USER_ADDED_FEEDS");
    	onCreate(db);
    }
	

}
