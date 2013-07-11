package com.achipmunkdev.podcasts;

import java.io.IOException;
import java.net.URL;

import android.os.AsyncTask;

import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndFeed;
import com.google.code.rome.android.repackaged.com.sun.syndication.fetcher.FeedFetcher;
import com.google.code.rome.android.repackaged.com.sun.syndication.fetcher.FetcherException;
import com.google.code.rome.android.repackaged.com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;
import com.google.code.rome.android.repackaged.com.sun.syndication.io.FeedException;

public class FeedRetriever {
	static Boolean isActuallyAFeed = null;
	
	public boolean checkFeed(String url){
		
		new NetworkTime().execute(url);
		return isActuallyAFeed;
	}
	private final class NetworkTime extends AsyncTask<String, Void, SyndFeed> {
		
		@Override
		protected SyndFeed doInBackground(String...urls) {
			try {
				return retrieveFeed(urls[0]);
			} catch (Exception e) {
				return null;
				//throw new RuntimeException(e);
			}
		}
		@Override
		protected void onPostExecute(SyndFeed result) {
			if (result != null){
				
			}
			else{
				isActuallyAFeed = false;
			}
		}
	}
	private SyndFeed retrieveFeed( final String feedUrl )
	        throws IOException, FeedException, FetcherException
	    {
	        FeedFetcher feedFetcher = new HttpURLFeedFetcher();
	        return feedFetcher.retrieveFeed( new URL( feedUrl ) );
	    }

}
