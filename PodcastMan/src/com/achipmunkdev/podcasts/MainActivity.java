package com.achipmunkdev.podcasts;

import java.io.IOException;
import java.net.URL;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.achipmunkdev.podcasts.R;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;

import com.achipmunkdev.podcasts.FeedListAdapter;
import com.achipmunkdev.podcasts.DatabaseConstants.Constants;
import com.achipmunkdev.podcasts.FeedRetriever;
import com.achipmunkdev.podcasts.AddedFeedsDbHelper;
import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndFeed;
import com.google.code.rome.android.repackaged.com.sun.syndication.fetcher.FeedFetcher;
import com.google.code.rome.android.repackaged.com.sun.syndication.fetcher.FetcherException;
import com.google.code.rome.android.repackaged.com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;
import com.google.code.rome.android.repackaged.com.sun.syndication.io.FeedException;

public class MainActivity extends Activity {
	
	EditText urlEditText = null;
	private static final int DIALOG_ALERT = 1;
	public FeedListAdapter feedToListAdapter;
	public ListView mainListView;
	public SyndFeed aFeed;
	public String feedUrl1 = "http://feeds.wnyc.org/radiolab?format=xml";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mainListView = (ListView) findViewById(R.id.main_list);

        getView(MainActivity.this, feedUrl1);
        
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
    		getView(MainActivity.this, feedUrl1);
    		
    	}
    	return false;
    }
    
    public void getView(final Activity activity, String url){
    	
    	final class ShowFeedFromUrl extends AsyncTask<String, Void, SyndFeed> {
    		
    		@Override
    		protected SyndFeed doInBackground(String...urls) {
    			try {
    				return retrieveFeed(urls[0]);
    				//FeedListAdapter myListAdapter = new FeedListAdapter(MainActivity.this, aFeed);
    				
    				
    			} catch (Exception e) {
    				return null;
    				//throw new RuntimeException(e);
    				//return null;
    			}
    		}
    		@Override
    		protected void onPostExecute(SyndFeed result) {
    			if (result != null){
    				feedToListAdapter = new FeedListAdapter(activity, result);
    				mainListView.setAdapter(feedToListAdapter);
    			}
    			else{
    				Toast.makeText(activity, "bad url, try again", Toast.LENGTH_LONG).show();
    			}
    		}
    	}
    	new ShowFeedFromUrl().execute(url);
    	
    	
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
    					AddNewFeed(feedUrl1);
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
    public void AddNewFeed(final String url){
    	final class AddNewFeedTask extends AsyncTask<String, Void, SyndFeed> {
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
    				WriteFeedUrlToDatabase(url);
    				feedToListAdapter = new FeedListAdapter(MainActivity.this, result);
    				mainListView.setAdapter(feedToListAdapter);
    				
    			}
    			else{
    				Toast.makeText(MainActivity.this, "bad url, try again", Toast.LENGTH_LONG).show();
    			}
    		}
    	}
    	new AddNewFeedTask().execute(url);
    }
    public void WriteFeedUrlToDatabase(final String url){
    	final class WriteFeedUrlToDatabaseNetwork extends AsyncTask<String, Void, SQLiteDatabase> {
			@Override
			protected SQLiteDatabase doInBackground(String... params) {			
				AddedFeedsDbHelper userFeedsHelper = new AddedFeedsDbHelper(getBaseContext());
				//SQLiteDatabase ListOfFeeds = 
				return userFeedsHelper.getWritableDatabase();
				//return null;
			}
    		@Override
    		protected void onPostExecute(SQLiteDatabase ListOfFeeds){
    			ContentValues FeedUrlValue = new ContentValues();
    			FeedUrlValue.put(Constants.COLUMN_NAME_FEED_URL, url);
			
    			ListOfFeeds.insert(Constants.USER_ADDED_FEEDS, null, FeedUrlValue);
    			Toast.makeText(MainActivity.this, "Feed Add Successful", Toast.LENGTH_LONG).show();
    		}
    	}
    	new WriteFeedUrlToDatabaseNetwork().execute();
    }
}
