package com.achipmunkdev.podcasts;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Toast;

import com.achipmunkdev.podcasts.R;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.app.LoaderManager;

import com.achipmunkdev.podcasts.FeedListAdapter;
import com.achipmunkdev.podcasts.DatabaseConstants.Constants;
import com.achipmunkdev.podcasts.FeedRetriever;
import com.achipmunkdev.podcasts.AddedFeedsDbHelper;
import com.achipmunkdev.podcasts.FeedStoreageDbHelper;
import com.achipmunkdev.podcasts.contentprovider.EntriesContentProvider;
import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndEntry;
import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndFeed;
import com.google.code.rome.android.repackaged.com.sun.syndication.fetcher.FeedFetcher;
import com.google.code.rome.android.repackaged.com.sun.syndication.fetcher.FetcherException;
import com.google.code.rome.android.repackaged.com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;
import com.google.code.rome.android.repackaged.com.sun.syndication.io.FeedException;

public class MainActivity extends ListActivity  implements LoaderManager.LoaderCallbacks<Cursor>{
	
	private LoaderManager.LoaderCallbacks<Cursor> mCallbacks;
	private Uri entriesURI;
	private SimpleCursorAdapter adapter;
	EditText urlEditText = null;
	private static final int DIALOG_ALERT = 1;
	public FeedListAdapter feedToListAdapter;
	public SyndFeed aFeed;
	public String feedUrl1 = "http://feeds.wnyc.org/radiolab?format=xml";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //mainListView = (ListView) findViewById(R.id.list);
        registerForContextMenu(getListView());
        
        mCallbacks = this;
        LoaderManager lm = getLoaderManager();
        lm.initLoader(0, null, mCallbacks);
        //getView(MainActivity.this, feedUrl1); 
        fillData();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
    	switch (item.getItemId()){
    	case R.id.action_add_feed:
    		showDialog(DIALOG_ALERT);
    		//startActivity(new Intent(this, AddFeedActivity.class));
    		
    		//startActivity(new Intent(this, AddFeedActivity.class));
    		return true;
    	case R.id.action_refresh:
    		//getView(MainActivity.this, feedUrl1);
    		fillData();
    	}
    	return false;
    }
    private void fillData(){
    	adapter = new SimpleCursorAdapter(this, R.layout.entries_item, null, 
    			new String[]{Constants.COLUMN_NAME_TITLE, Constants.COLUMN_NAME_AUTHOR, Constants.COLUMN_NAME_DATE, Constants.COLUMN_NAME_DESCRIPTION}, 
    			new int[] { R.id.title, R.id.author, R.id.date, R.id.desc }, 0);
    	adapter.setViewBinder(new ViewBinder() {
    		public boolean setViewValue(View aView, Cursor aCursor, int aColumnIndex){
    			if (aColumnIndex == 3){
    				Long UTCDate = aCursor.getLong(aColumnIndex);
    				TextView dateTextView = (TextView) aView;
    				String readableDate = new SimpleDateFormat("yyy-MM-dd").format(new Date(UTCDate));
    				dateTextView.setText("Date: " + readableDate);
    				return true;
    			}
    			else{
    				return false;
    			}
    		}
    	});
    	setListAdapter(adapter);
    	//getLoaderManager().initLoader(0, null, this);
    }

    private SyndFeed retrieveFeed( final String feedUrl )
	        throws IOException, FeedException, FetcherException
	    {
	        FeedFetcher feedFetcher = new HttpURLFeedFetcher();
	        return feedFetcher.retrieveFeed( new URL( feedUrl ) );
	    }
    @Override
    protected Dialog onCreateDialog(int id) {
    	//commented code allows for multiple dialogues to be created! YAY!
    	//switch(id){
    	//case DIALOG_ALERT:
    		Builder builder = new AlertDialog.Builder(this);
    		LayoutInflater inflater = this.getLayoutInflater();
    		View v = inflater.inflate(R.layout.add_feed_layout, null);
    		urlEditText = (EditText)v.findViewById(R.id.feed_input_field);
    		
    		builder.setView(v);
    		builder.setPositiveButton(R.string.submit_button_text, new DialogInterface.OnClickListener(){
    				@Override
    				public void onClick(DialogInterface dialog, int id){
    					feedUrl1 = urlEditText.getText().toString();
    					new CheckFeedExistanceThenAddFeed(feedUrl1).execute(feedUrl1);
    					urlEditText.setText("");
    					dialog.dismiss();
    				}
    				
    			});
    		builder.setNegativeButton(R.string.cancle_button_text, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						urlEditText.setText("");
						dialog.dismiss();
					}
				});
    		return builder.create();
    		
    	
    	//}
    	
    }
    public void WriteFeedUrlToDatabase(final ContentValues feedValues){
    	final class WriteFeedUrlToDatabaseTask extends AsyncTask<String, Void, SQLiteDatabase> {
			@Override
			protected SQLiteDatabase doInBackground(String... params) {			
				AddedFeedsDbHelper userFeedsHelper = new AddedFeedsDbHelper(getBaseContext());
				return userFeedsHelper.getWritableDatabase();
			}
    		@Override
    		protected void onPostExecute(SQLiteDatabase ListOfFeeds){
    			ListOfFeeds.insert(Constants.USER_ADDED_FEEDS, null, feedValues);
    			Toast.makeText(MainActivity.this, "Feed Add Successful", Toast.LENGTH_LONG).show();
    		}
    	}
    	new WriteFeedUrlToDatabaseTask().execute();
    }
    public void GetAndWriteEntriesToDatabase(String theUrl){

    	final class GetEntries extends AsyncTask<String, Void, SyndFeed> {
			@Override
			protected SyndFeed doInBackground(String... url) {
				try{
					return retrieveFeed(url[0]);
				}
				catch (Exception e){
					return null;
				}
			}
			@Override
			protected void onPostExecute(SyndFeed result){
				if (result != null){
					for (Iterator i = result.getEntries().iterator(); i.hasNext();){
						SyndEntry entry = (SyndEntry) i.next();
						ContentValues EntryValues = new ContentValues();
						EntryValues.put(Constants.COLUMN_NAME_TITLE, entry.getTitle());
						EntryValues.put(Constants.COLUMN_NAME_DESCRIPTION, entry.getDescription().getValue());
						//EntryValues.put(Constants.COLUMN_NAME_CATAGORY, entry.getCategories().get(0).toString());
						EntryValues.put(Constants.COLUMN_NAME_DATE, entry.getPublishedDate().getTime());
						EntryValues.put(Constants.COLUMN_NAME_AUTHOR, entry.getAuthor());
						EntryValues.put(Constants.COLUMN_NAME_CONTENT_URL, entry.getLink());
						EntryValues.put(Constants.COLUMN_NAME_MEDIA_TYPE, entry.getUri());
						
						entriesURI = getContentResolver().insert(EntriesContentProvider.CONTENT_URI, EntryValues);
						//new WriteEntrytoDatabase(EntryValues).execute();
					}
				}
			}	
    	}
    	new GetEntries().execute(theUrl);
    }
    final class CheckFeedExistanceThenAddFeed extends AsyncTask<String, Void, SQLiteDatabase>{
    	String myUrl= null;
    	CheckFeedExistanceThenAddFeed(String feedURL){
    		myUrl = feedURL;
    	}
		@Override
		protected SQLiteDatabase doInBackground(String... feedUrls) {
			AddedFeedsDbHelper userFeedsHelper = new AddedFeedsDbHelper(getBaseContext());
			return userFeedsHelper.getReadableDatabase();
		}
    	@Override
    	protected void onPostExecute(SQLiteDatabase resultDb){
    		String[] projection = {
    				Constants.COLUMN_NAME_TITLE
    		};
    		Cursor dbCursor = resultDb.query(
    				Constants.USER_ADDED_FEEDS,
    				projection,
    				Constants.COLUMN_NAME_FEED_URL + "=?",
    				new String[] {myUrl},
    				null,
    				null,
    				null
    				);
    		if(dbCursor!=null && dbCursor.getCount()>0){
    			dbCursor.moveToFirst();
    			Toast.makeText(MainActivity.this, "Feed" + dbCursor.getString(dbCursor.getColumnIndexOrThrow(Constants.COLUMN_NAME_TITLE)) + " is already added!" , Toast.LENGTH_LONG).show();
    		}
    		else{
    			new AddNewFeedTask(myUrl).execute(myUrl);
    		}
    	}
    }
    final class AddNewFeedTask extends AsyncTask<String, Void, SyndFeed> {
    	String url = null;
    	AddNewFeedTask(String feedURL){
			url = feedURL;
		}
    	@Override
		protected SyndFeed doInBackground(String...urls) {
			try {
				return retrieveFeed(urls[0]);
				
			} catch (Exception e) {
				return null;
			}
		}
		@Override
		protected void onPostExecute(SyndFeed result) {
			if (result != null){
				ContentValues FeedUrlValue = new ContentValues();
    			FeedUrlValue.put(Constants.COLUMN_NAME_FEED_URL, url);
    			FeedUrlValue.put(Constants.COLUMN_NAME_TITLE, result.getTitle());
    			if (result.getCategories()!= null && result.getCategories().size() > 0 ){
    				FeedUrlValue.put(Constants.COLUMN_NAME_CATEGORY, result.getCategories().get( 0 ).toString());
    			}
    			
				WriteFeedUrlToDatabase(FeedUrlValue);
				//feedToListAdapter = new FeedListAdapter(MainActivity.this, result);
				//mainListView.setAdapter(feedToListAdapter);
				GetAndWriteEntriesToDatabase(url);
				
			}
			else{
				Toast.makeText(MainActivity.this, "bad url, try again", Toast.LENGTH_LONG).show();
			}
		}
	}
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] projection = new String[]{Constants._ID, Constants.COLUMN_NAME_TITLE, Constants.COLUMN_NAME_AUTHOR, Constants.COLUMN_NAME_DATE, Constants.COLUMN_NAME_DESCRIPTION};
    	CursorLoader cursorLoader = new CursorLoader(this, EntriesContentProvider.CONTENT_URI, projection, null, null, null);
    	return cursorLoader;
	}


	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		adapter.swapCursor(data);
	}


	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);
		
	}
}
