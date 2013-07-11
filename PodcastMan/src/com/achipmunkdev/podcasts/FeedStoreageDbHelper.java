package com.achipmunkdev.podcasts;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.achipmunkdev.podcasts.DatabaseConstants.Constants;

public class FeedStoreageDbHelper extends SQLiteOpenHelper{
	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "FeedEntriesCache.db";
	
	
	public FeedStoreageDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		
	}
	public void onCreate(SQLiteDatabase db){
		db.execSQL(Constants.SQL_CREATE_ENTRIES_TABLE);
		
	}
	public void onUpgrade(SQLiteDatabase db,int oldVersion, int newVersion){
		db.execSQL(Constants.SQL_DELETE_ENTRIES);
		onCreate(db);
	}
	
}
