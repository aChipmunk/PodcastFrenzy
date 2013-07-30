package com.achipmunkdev.podcasts.contentprovider;

import java.util.Arrays;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.content.ContentResolver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.achipmunkdev.podcasts.FeedStoreageDbHelper;
import com.achipmunkdev.podcasts.DatabaseConstants.Constants;



public class EntriesContentProvider extends ContentProvider {
	
	private FeedStoreageDbHelper database;
	
	private static final int ENTRIES = 10;
	private static final int ENTRY_ID = 20;
	
	private static final String AUTHORITY = "com.achipmunkdev.podcasts.contentprovider";
	private static final String BASE_PATH = "entris";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/entries";
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/entry";
	
	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		sURIMatcher.addURI(AUTHORITY, BASE_PATH, ENTRIES);
		sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", ENTRY_ID);
	}
	
	@Override
	public boolean onCreate() {
		database = new FeedStoreageDbHelper(getContext());
		return false;
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase writeDB = database.getWritableDatabase();
		int rowsDeleted = 0;
		
		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
		case ENTRIES:
			rowsDeleted = writeDB.delete(Constants.TABLE_NAME_ENTRIES, selection, selectionArgs);
			break;
		case ENTRY_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsDeleted = writeDB.delete(Constants.TABLE_NAME_ENTRIES, Constants._ID + "=" +id, null);
			}
			else {
				rowsDeleted = writeDB.delete(Constants.TABLE_NAME_ENTRIES, Constants._ID + "=" + id + "AND" + selection, selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsDeleted;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase writeDB = database.getWritableDatabase();
		//int rowsDeleted = 0;
		long id = 0;
		
		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
		case ENTRIES:
			id = writeDB.insert(Constants.TABLE_NAME_ENTRIES, null, values);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return Uri.parse(BASE_PATH + "/" + id);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		
		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
		case ENTRIES:
			break;
		case ENTRY_ID:
			queryBuilder.appendWhere(Constants._ID + "=" + uri.getLastPathSegment());
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		
		SQLiteDatabase db = database.getWritableDatabase();
		Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase writeDB = database.getWritableDatabase();
		int rowsUpdated = 0;
		
		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
		case ENTRIES:
			rowsUpdated = writeDB.update(Constants.TABLE_NAME_ENTRIES, values, selection, selectionArgs);
			break;
		case ENTRY_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)){
				rowsUpdated = writeDB.update(Constants.TABLE_NAME_ENTRIES, values, Constants._ID + "=" + id, null);
			}
			else {
				rowsUpdated = writeDB.update(Constants.TABLE_NAME_ENTRIES, values, Constants._ID + "=" + id + "AND" + selection, selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		} 
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdated;
	}

}
