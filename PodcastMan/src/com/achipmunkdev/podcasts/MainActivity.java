package com.achipmunkdev.podcasts;

import java.io.IOException;
import java.net.URL;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.ListView;
import com.achipmunkdev.podcasts.R;

import com.achipmunkdev.podcasts.FeedListAdapter;
import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndFeed;
import com.google.code.rome.android.repackaged.com.sun.syndication.fetcher.FeedFetcher;
import com.google.code.rome.android.repackaged.com.sun.syndication.fetcher.FetcherException;
import com.google.code.rome.android.repackaged.com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;
import com.google.code.rome.android.repackaged.com.sun.syndication.io.FeedException;

public class MainActivity extends Activity {
	
	public FeedListAdapter feedToListAdapter;
	public ListView mainListView;
	public SyndFeed aFeed;
	public final String feedUrl1 = "http://feeds.wnyc.org/radiolab?format=xml";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mainListView = (ListView) findViewById(R.id.main_list);
        
        //feedToListAdapter = new FeedListAdapter(MainActivity.this);
        //mainListView.setAdapter(feedToListAdapter);
        //new NetworkTime().execute();
        getView(MainActivity.this, feedUrl1);
        
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
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
    
}
