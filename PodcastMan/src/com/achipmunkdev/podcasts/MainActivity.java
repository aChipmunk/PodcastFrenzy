package com.achipmunkdev.podcasts;

import java.io.IOException;
import java.net.URL;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import com.achipmunkdev.podcasts.R;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;

import com.achipmunkdev.podcasts.FeedListAdapter;
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
    	
    	
    	final class NetworkTime extends AsyncTask<String, Void, SyndFeed> {
    		
    		@Override
    		protected SyndFeed doInBackground(String...urls) {
    			try {
    				return retrieveFeed(urls[0]);
    				//FeedListAdapter myListAdapter = new FeedListAdapter(MainActivity.this, aFeed);
    				
    				
    			} catch (Exception e) {
    				throw new RuntimeException(e);
    				//return null;
    			}
    		}
    		@Override
    		protected void onPostExecute(SyndFeed result) {
    	   
    			feedToListAdapter = new FeedListAdapter(activity, result);
    			mainListView.setAdapter(feedToListAdapter);
    		}
    	}
    	new NetworkTime().execute(url);
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
    
}
