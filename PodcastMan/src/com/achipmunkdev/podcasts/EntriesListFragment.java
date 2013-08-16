package com.achipmunkdev.podcasts;

import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.app.Activity;
import android.app.ListFragment;

public class EntriesListFragment extends ListFragment {

    public static final String entryQuerySelection = null;
    private SimpleCursorAdapter adapter;
    OnDataPass dataPasser;
    
    MediaPlayer mediaPlayer = new MediaPlayer();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {   
        return inflater.inflate(R.layout.entries_listview_fragment, null);       
    }
    @Override
    public void onAttach(Activity a) {
        super.onAttach(a);
        dataPasser = (OnDataPass) a;
    }
    
    public void cursorUpdate(Cursor cursor){
    	if (adapter != null){
    		adapter.swapCursor(cursor);
    		adapter.notifyDataSetChanged();
    	}
    	else{
    		Log.d(getTag(), "Please use setAdapter(simpleCursorAdapter) first");
    	}
    }
    public void setAdapter(SimpleCursorAdapter passedAdapter){
    	adapter = passedAdapter;
    	setListAdapter(adapter);
    }
    @Override
    public void onListItemClick(ListView l, View v, int position, long id){
    	dataPasser.onEntryListItemSelected(id);
    	
    }
    public interface OnDataPass {
    	public void onEntryListItemSelected(long id);
    }

}
