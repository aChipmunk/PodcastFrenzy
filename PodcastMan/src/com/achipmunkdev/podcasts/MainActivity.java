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
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.app.LoaderManager;

import com.achipmunkdev.podcasts.DatabaseConstants.Constants;
import com.achipmunkdev.podcasts.AddedFeedsDbHelper;
import com.achipmunkdev.podcasts.MediaControlsFragment.ButtonIds;
import com.achipmunkdev.podcasts.contentprovider.EntriesContentProvider;
import com.achipmunkdev.podcasts.MediaControlsFragment;
import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndEnclosure;
import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndEntry;
import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndFeed;
import com.google.code.rome.android.repackaged.com.sun.syndication.fetcher.FeedFetcher;
import com.google.code.rome.android.repackaged.com.sun.syndication.fetcher.FetcherException;
import com.google.code.rome.android.repackaged.com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;
import com.google.code.rome.android.repackaged.com.sun.syndication.io.FeedException;

public class MainActivity extends Activity  implements LoaderManager.LoaderCallbacks<Cursor>, EntriesListFragment.OnDataPass, MediaControlsFragment.OnPassClick{
	
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
    
    private LoaderManager lm = null;
    MediaPlayer mediaPlayer = new MediaPlayer();
    
    private EntriesListFragment entriesListFragment; 
    
    private MediaControlsFragment mediaControlFragment = new MediaControlsFragment();
    private Handler handler = new Handler();
    private ProgressDialog dialog; 
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	dialog = new ProgressDialog(MainActivity.this);
    	handler.removeCallbacks(moveSeekBarThread);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        entriesListFragment = (EntriesListFragment) getFragmentManager().findFragmentById(R.id.listFragment);
        
        
        mCallbacks = this;
        lm = getLoaderManager();
        lm.initLoader(0, null, mCallbacks);
        
        fillData();
        
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
        //new distinctFeedQuery().execute();
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        
        mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
            @Override
        	public void onPrepared(MediaPlayer mp) {
                mp.start();
                handler.postDelayed(moveSeekBarThread, 100);
            }
        });
        
        
        new distinctFeedQuery().execute();
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
    		getContentResolver().delete(EntriesContentProvider.CONTENT_URI, EntriesContentProvider.DROP_FULL_TABLE, null);
    		new cacheAllEntries().execute();
    		//lm.restartLoader(0, null, mCallbacks);
    		//new distinctFeedQuery().execute();
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
    public void onEntryListItemSelected(long id) {
    	Uri singleEntryUri = Uri.parse(EntriesContentProvider.CONTENT_URI + "/" + id);
    	Cursor cursor = this.getContentResolver().query(singleEntryUri, new String[]{Constants._ID, Constants.COLUMN_NAME_CONTENT_URL}, null, null, null);
    	if (cursor.getCount() > 0 && cursor != null){
    		cursor.moveToFirst();
    		String url = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_NAME_CONTENT_URL));
    		//mediaPlayer.create(this, Uri.parse(url));
    		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    		try{
    			mediaPlayer.setDataSource(url);
    	        mediaPlayer.prepareAsync();
    	        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
    	        fragmentTransaction.add(R.id.main_activity_linear_layout, mediaControlFragment);
    	        fragmentTransaction.commit();
    	        mediaControlFragment.setMediaPlaying(true);
    		}
    		catch (Exception e){
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
    	entriesListFragment.setAdapter(adapter);
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
						EntryValues.put(Constants.COLUMN_NAME_DESCRIPTION, Html.fromHtml(entry.getDescription().getValue()).toString());
						//EntryValues.put(Constants.COLUMN_NAME_CATAGORY, entry.getCategories().get(0).toString());
						EntryValues.put(Constants.COLUMN_NAME_DATE, entry.getPublishedDate().getTime());
						EntryValues.put(Constants.COLUMN_NAME_AUTHOR, entry.getAuthor());
						//EntryValues.put(Constants.COLUMN_NAME_CONTENT_URL, entry.getLink());
						try{
							SyndEnclosure enc = (SyndEnclosure) entry.getEnclosures().get(0);
							EntryValues.put(Constants.COLUMN_NAME_CONTENT_URL, enc.getUrl());
						}catch (Exception e){
							Log.d("PODCASTFRENZY", "error with enc url");
						}
						
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
		entriesListFragment.cursorUpdate(data);
		adapter.swapCursor(data);
		adapter.notifyDataSetChanged();
	}


	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		entriesListFragment.cursorUpdate(null);
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
    			listOfFeeds.clear();
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

	@Override
	public void OnMediaButtonClick(int id, int seekPosition) {
		
		switch(id) {
		case ButtonIds.FORWARD:
			
			break;
		case ButtonIds.PLAYPAUSE:
			if (mediaPlayer.isPlaying()){
				mediaPlayer.pause();
				mediaControlFragment.setMediaPlaying(false);
			}
			else{
				mediaPlayer.start();
				handler.post(moveSeekBarThread);
				mediaPlayer.seekTo(seekPosition);
				mediaControlFragment.setMediaPlaying(true);
				//mediaControlFragment.setSeekBarPositionMax(mediaPlayer.getCurrentPosition(), mediaPlayer.getDuration());
				
			}
			break;
		case ButtonIds.REWIND:
			
			break;
		case ButtonIds.SEEKBAR:
			if (mediaPlayer.isPlaying()){
				mediaPlayer.seekTo(seekPosition);
			}
		}
	}
	private Runnable moveSeekBarThread = new Runnable() {

		@Override
		public void run() {
			if(mediaPlayer.isPlaying()){
				int mediaPos_new = mediaPlayer.getCurrentPosition();
		        int mediaMax_new = mediaPlayer.getDuration();
		        mediaControlFragment.setSeekBarPositionMax(mediaPos_new, mediaMax_new);
		        handler.postDelayed(moveSeekBarThread, 100);
			}
		}
	};
	
	public class cacheAllEntries extends AsyncTask<String, Void, Cursor>{
		

		@Override
		protected void onPreExecute() {
			//dialog = new ProgressDialog(MainActivity.this);
			dialog.setMessage("Loading your feeds...");
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.setIndeterminate(true);
		    dialog.show();
    	}

		@Override
		protected Cursor doInBackground(String...strings) {
			return getAllFeeds();
		}
		@Override
    	protected void onPostExecute(Cursor cursor){
			
			cursor.moveToFirst();
			String url = null;
			while (cursor.isAfterLast() == false) {
				url = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_NAME_FEED_URL));
				GetAndWriteEntriesToDatabase(url);
				dialog.setMessage(cursor.getString(cursor.getColumnIndex(Constants.COLUMN_NAME_FEED_TITLE)));
				if(cursor.isAfterLast() == false){
					cursor.moveToNext();
				}
			}						
			cursor.close();
			dialog.setMessage("finishing up");
			getLoaderManager().restartLoader(0, null, MainActivity.this);
	    	new distinctFeedQuery().execute();
	    	if (dialog.isShowing()) {
	            dialog.dismiss();
	        }
		}
	}
	public Cursor getAllFeeds(){
		AddedFeedsDbHelper userFeedsHelper = new AddedFeedsDbHelper(getBaseContext());
		SQLiteDatabase DB = userFeedsHelper.getWritableDatabase();
		return DB.query(true, Constants.USER_ADDED_FEEDS, new String[]{Constants._ID, Constants.COLUMN_NAME_FEED_URL,  Constants.COLUMN_NAME_FEED_TITLE}, null, null, null, null, null, null);
		
	}

	
}
