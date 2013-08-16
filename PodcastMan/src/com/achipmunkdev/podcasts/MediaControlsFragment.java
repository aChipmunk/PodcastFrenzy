package com.achipmunkdev.podcasts;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import com.achipmunkdev.podcasts.R;

public class MediaControlsFragment extends Fragment{
	ImageButton forward;
	ImageButton playPause;
	ImageButton rewind;
	View view;
	public class ButtonIds{
		public static final int REWIND = 10;
		public static final int FORWARD = 20;
		public static final int PLAYPAUSE = 30;
	}
	
	
	OnPassClick clickPasser;
	
	public interface OnPassClick {
	    public void OnMediaButtonClick(int id);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		view = inflater.inflate(R.layout.audio_controll_small, container, false);
		
		
		
		return view;
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		forward = (ImageButton) view.findViewById(R.id.forward);
		playPause = (ImageButton) view.findViewById(R.id.play_pause);
		rewind = (ImageButton) view.findViewById(R.id.rewind);
		playPause.setImageResource(R.drawable.av_pause);
		//setMediaPlaying(true);
		
		forward.setOnClickListener(new View.OnClickListener() {
		      @Override
		      public void onClick(View v) {
		    	  passClick(ButtonIds.FORWARD);
		      }
		    });
		playPause.setOnClickListener(new View.OnClickListener() {
		      @Override
		      public void onClick(View v) {
		    	  passClick(ButtonIds.PLAYPAUSE);
		      }
		    });
		rewind.setOnClickListener(new View.OnClickListener() {
		      @Override
		      public void onClick(View v) {
		    	  passClick(ButtonIds.REWIND);
		      }
		    });
	}
	@Override
	public void onAttach(Activity a) {
	    super.onAttach(a);
	    clickPasser = (OnPassClick) a;
	}
	public void passClick(int id) {
	    clickPasser.OnMediaButtonClick(id);
	}
	public void setMediaPlaying(Boolean booleon){
		playPause = (ImageButton) view.findViewById(R.id.play_pause);
		if (booleon){
			playPause.setImageResource(R.drawable.av_pause);
		}
		else{
			playPause.setImageResource(R.drawable.av_play);
		}
	}
}
