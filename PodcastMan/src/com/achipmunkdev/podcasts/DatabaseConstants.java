package com.achipmunkdev.podcasts;

import android.provider.BaseColumns;

public class DatabaseConstants {
	
	public static abstract class Constants implements BaseColumns {
		public static final String TABLE_NAME_ENTRIES = "entries";
		public static final String USER_ADDED_FEEDS = "userfeeds";
		public static final String COLUMN_NAME_ENTRY_ID = "entryid";
		public static final String COLUMN_NAME_TITLE = "title";
		public static final String COLUMN_NAME_DESCRIPTION = "description";
		public static final String COLUMN_NAME_CATEGORY = "catagory";
		public static final String COLUMN_NAME_DATE = "date";
		public static final String COLUMN_NAME_AUTHOR = "author";
		public static final String COLUMN_NAME_CONTENT_URL = "url";
		public static final String COLUMN_NAME_FEED_URL = "feedurl";
		public static final String COLUMN_NAME_MEDIA_TYPE = "mediatype"; 
		public static final String COMMA_SEP = ",";
		public static final String TEXT_TYPE = " TEXT";
		public static final String INT_TYPE = " INTEGER";
		public static final String SQL_CREATE_ENTRIES_TABLE = 
				"CREATE TABLE " + TABLE_NAME_ENTRIES + " (" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
				COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP + 
				COLUMN_NAME_DESCRIPTION + TEXT_TYPE + COMMA_SEP + 
				COLUMN_NAME_CATEGORY + TEXT_TYPE + COMMA_SEP + 
				COLUMN_NAME_DATE + INT_TYPE + COMMA_SEP + 
				COLUMN_NAME_AUTHOR + TEXT_TYPE + COMMA_SEP + 
				COLUMN_NAME_CONTENT_URL + TEXT_TYPE + COMMA_SEP + 
				COLUMN_NAME_MEDIA_TYPE + TEXT_TYPE + 
				" )";
		
		public static final String SQL_DELETE_ENTRIES = 
				"DROP TABLE IF EXISTS " + TABLE_NAME_ENTRIES;
		
		public static final String SQL_CREATE_USER_ADDED_FEEDS_TABLE = 
				"CREATE TABLE " + USER_ADDED_FEEDS + " ( " + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
				COLUMN_NAME_FEED_URL + TEXT_TYPE + COMMA_SEP +
				COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
				COLUMN_NAME_CATEGORY + TEXT_TYPE +
				")";
		
				
		
	}

}
