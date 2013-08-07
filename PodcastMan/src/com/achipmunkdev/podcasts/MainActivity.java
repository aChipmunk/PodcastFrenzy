package com.achipmunkdev.podcasts;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.app.LoaderManager;

import com.achipmunkdev.podcasts.DatabaseConstants.Constants;
import com.achipmunkdev.podcasts.AddedFeedsDbHelper;
import com.achipmunkdev.podcasts.contentprovider.EntriesContentProvider;
import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndEntry;
import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndFeed;
import com.google.code.rome.android.repackaged.com.sun.syndication.fetcher.FeedFetcher;
import com.google.code.rome.android.repackaged.com.sun.syndication.fetcher.FetcherException;
import com.google.code.rome.android.repackaged.com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;
import com.google.code.rome.android.repackaged.com.sun.syndication.io.FeedException;

public class MainActivity extends ListActivity  implements LoaderManager.LoaderCallbacks<Cursor>{
	
	private String entryQuerySelection = null;
	private ArrayList<String> entryQuerySelectionArgs = new ArrayList<String>();
	
	private LoaderManager.LoaderCallbacks<Cursor> mCallbacks;
	private SimpleCursorAdapter adapter;
	EditText urlEditText = null;
	private static final int DIALOG_ALERT = 1;
	public SyndFeed aFeed;
	
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
    private ListView mDrawerList;
    private ArrayList<String> listOfFeeds = new ArrayList<String>();
    private Uri entriesURI = null;
    
    private LoaderManager lm = null;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        registerForContextMenu(getListView());
        
        mCallbacks = this;
        lm = getLoaderManager();
        lm.initLoader(0, null, mCallbacks);
        
        fillData();
        //listOfFeeds.add("fioogle");
        //String[] arrayOfFeeds = (String[]) listOfFeeds.toArray();
        
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close){
        	public void onDrawerClosed(View view){
        		getActionBar().setTitle(R.string.app_name);
        	}
        	public void onDrawerOpened(View drawerView){
        		getActionBar().setTitle(R.string.drawer_open);
        	}
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        new distinctFeedQuery().execute();
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
       
    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
    	if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
    	switch (item.getItemId()){
    	case R.id.action_add_feed:
    		showDialog(DIALOG_ALERT);
    		return true;
    	case R.id.action_refresh:
    		lm.restartLoader(0, null, mCallbacks);
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    }
    
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        	TextView tv = (TextView)view.findViewById(R.id.simple_drawer_item);
        	entryQuerySelectionArgs.add(tv.getText().toString());
        	entryQuerySelection = Constants.COLUMN_NAME_FEED_TITLE + "= '" + tv.getText().toString() +"'" ;
        	lm.restartLoader(0, null, mCallbacks);
        	mDrawerLayout.closeDrawer(mDrawerList);
        }
    }
    @Override 
    public void onListItemClick(ListView l, View v, int position, long id) {
    	Uri singleEntryUri = Uri.parse(EntriesContentProvider.CONTENT_URI + "/" + id);
    	Cursor cursor = getContentResolver().query(singleEntryUri, new String[]{Constants._ID, Constants.COLUMN_NAME_CONTENT_URL}, null, null, null);
    	if (cursor.getCount() > 0 && cursor != null){
    		cursor.moveToFirst();
    		String url = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_NAME_CONTENT_URL));
    		MediaPlayer mediaPlayer = new MediaPlayer();
    		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    		try{
    			mediaPlayer.setDataSource(url);
        		mediaPlayer.prepare();
        		mediaPlayer.start();
    		}
    		catch (Exception e){
    			Toast.makeText(MainActivity.this, "Sorry, that post could not be found", Toast.LENGTH_LONG).show();
    		}
    	}
    	
    }
    
    private void fillData(){
    	adapter = new SimpleCursorAdapter(this, R.layout.entries_item, null, 
    			new String[]{Constants.COLUMN_NAME_ENTRY_TITLE, Constants.COLUMN_NAME_AUTHOR, Constants.COLUMN_NAME_DATE, Constants.COLUMN_NAME_DESCRIPTION}, 
    			new int[] { R.id.title, R.id.author, R.id.date, R.id.desc }, 0);
    	adapter.setViewBinder(new ViewBinder() {
    		public boolean setViewValue(View aView, Cursor aCursor, int aColumnIndex){
    			if (aColumnIndex == 4){
    				Long UTCDate = aCursor.getLong(aColumnIndex);
    				TextView dateTextView = (TextView) aView;
    				String readableDate = new SimpleDateFormat("MMM dd, yyyy", Locale.US).format(new Date(UTCDate));
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
    					String feedUrl = urlEditText.getText().toString();
    					new CheckFeedExistanceThenAddFeed(feedUrl).execute(feedUrl);
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
						EntryValues.put(Constants.COLUMN_NAME_ENTRY_TITLE, entry.getTitle());
						EntryValues.put(Constants.COLUMN_NAME_FEED_TITLE, result.getTitle());
						EntryValues.put(Constants.COLUMN_NAME_DESCRIPTION, entry.getDescription().getValue());
						//EntryValues.put(Constants.COLUMN_NAME_CATAGORY, entry.getCategories().get(0).toString());
						EntryValues.put(Constants.COLUMN_NAME_DATE, entry.getPublishedDate().getTime());
						EntryValues.put(Constants.COLUMN_NAME_AUTHOR, entry.getAuthor());
						EntryValues.put(Constants.COLUMN_NAME_CONTENT_URL, entry.getLink());
						EntryValues.put(Constants.COLUMN_NAME_MEDIA_TYPE, entry.getUri());
						
						Uri entriesURI = getContentResolver().insert(EntriesContentProvider.CONTENT_URI, EntryValues);
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
    				Constants.COLUMN_NAME_FEED_TITLE
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
    			Toast.makeText(MainActivity.this, "Feed" + dbCursor.getString(dbCursor.getColumnIndexOrThrow(Constants.COLUMN_NAME_FEED_TITLE)) + " is already added!" , Toast.LENGTH_LONG).show();
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
    			FeedUrlValue.put(Constants.COLUMN_NAME_FEED_TITLE, result.getTitle());
    			if (result.getCategories()!= null && result.getCategories().size() > 0 ){
    				FeedUrlValue.put(Constants.COLUMN_NAME_CATEGORY, result.getCategories().get( 0 ).toString());
    			}
    			
				WriteFeedUrlToDatabase(FeedUrlValue);
				GetAndWriteEntriesToDatabase(url);
			}
			else{
				Toast.makeText(MainActivity.this, "bad url, try again", Toast.LENGTH_LONG).show();
			}
		}
	}
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {		
		String[] projection = new String[]{Constants._ID, Constants.COLUMN_NAME_ENTRY_TITLE, Constants.COLUMN_NAME_FEED_TITLE, Constants.COLUMN_NAME_AUTHOR, Constants.COLUMN_NAME_DATE, Constants.COLUMN_NAME_DESCRIPTION};
		//String[] selectionArgs = entryQuerySelectionArgs.toArray(new String[entryQuerySelectionArgs.size()]);
		return new CursorLoader(this, EntriesContentProvider.CONTENT_URI, projection, entryQuerySelection, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		adapter.swapCursor(data);
		adapter.notifyDataSetChanged();
	}


	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);
		adapter.notifyDataSetChanged();
		
	}
    final class distinctFeedQuery extends AsyncTask<String, Void, SQLiteDatabase>{
		@Override
		protected SQLiteDatabase doInBackground(String... selection) {
			AddedFeedsDbHelper userFeedsHelper = new AddedFeedsDbHelper(getBaseContext());
			return userFeedsHelper.getReadableDatabase();
		}
    	@Override
    	protected void onPostExecute(SQLiteDatabase resultDb){
    		new updateDrawer().execute(resultDb);
    	}
    }
    final class updateDrawer extends AsyncTask<SQLiteDatabase, Void, Cursor>{
    	@Override
		protected Cursor doInBackground(SQLiteDatabase... db){
    		//return db[0].rawQuery("SELECT " + Constants.COLUMN_NAME_TITLE + " FROM " + Constants.USER_ADDED_FEEDS, null);
    		return db[0].query(Constants.USER_ADDED_FEEDS, new String[]{Constants._ID, Constants.COLUMN_NAME_FEED_TITLE}, null, null, Constants.COLUMN_NAME_FEED_TITLE, null, null, null);
    	}
    	@Override
    	protected void onPostExecute(Cursor dbCursor){
    		if(dbCursor!=null && dbCursor.getCount()>0){
    			dbCursor.moveToFirst();
    			listOfFeeds.add(dbCursor.getString(dbCursor.getColumnIndex(Constants.COLUMN_NAME_FEED_TITLE)));
    			for(dbCursor.moveToFirst(); dbCursor.moveToNext(); dbCursor.isAfterLast()){
    				listOfFeeds.add(dbCursor.getString(dbCursor.getColumnIndex(Constants.COLUMN_NAME_FEED_TITLE)));
    			}	
    		}
    		else{
    			listOfFeeds.clear();
    			listOfFeeds.add("No Feeds! Try adding some.");
    		}
        	mDrawerList.setAdapter(new ArrayAdapter<String>(MainActivity.this, R.layout.drawer_item, listOfFeeds));
    	}
    }
}
