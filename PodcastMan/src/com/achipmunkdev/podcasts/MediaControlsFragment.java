package com.achipmunkdev.podcasts;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.achipmunkdev.podcasts.R;

public class MediaControlsFragment extends Fragment{
	ImageButton forward;
	ImageButton playPause;
	ImageButton rewind;
	SeekBar seekBar;
	View view;
	TextView currentText;
	TextView maxText;
	public class ButtonIds{
		public static final int REWIND = 10;
		public static final int FORWARD = 20;
		public static final int PLAYPAUSE = 30;
		public static final int SEEKBAR = 40;
	}
	
	
	OnPassClick clickPasser;
	
	public interface OnPassClick {
	    public void OnMediaButtonClick(int id, int seekPosition);
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
		seekBar = (SeekBar) view.findViewById(R.id.seekbar);
		currentText = (TextView) view.findViewById(R.id.curr_position);
		maxText = (TextView) view.findViewById(R.id.max_position);
		
		playPause.setImageResource(R.drawable.av_pause);
		
		//setMediaPlaying(true);
		
		forward.setOnClickListener(new View.OnClickListener() {
		      @Override
		      public void onClick(View v) {
		    	  passClick(ButtonIds.FORWARD, seekBar.getProgress());
		      }
		    });
		playPause.setOnClickListener(new View.OnClickListener() {
		      @Override
		      public void onClick(View v) {
		    	  passClick(ButtonIds.PLAYPAUSE, seekBar.getProgress());
		      }
		    });
		rewind.setOnClickListener(new View.OnClickListener() {
		      @Override
		      public void onClick(View v) {
		    	  passClick(ButtonIds.REWIND, seekBar.getProgress());
		      }
		    });
		seekBar.setOnSeekBarChangeListener(onSeekChangeListener);
	}
	@Override
	public void onAttach(Activity a) {
	    super.onAttach(a);
	    clickPasser = (OnPassClick) a;
	}
	public void passClick(int id, int seekbarPosition) {
	    clickPasser.OnMediaButtonClick(id, seekbarPosition);
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
	OnSeekBarChangeListener onSeekChangeListener = new OnSeekBarChangeListener() {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			if (fromUser) {
                passClick(ButtonIds.SEEKBAR, progress);
                //seekBar.setProgress(progress);
                setSeekBarPositionMax(progress, 0);
            }	
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			
			
		}
		
	};
	public void setSeekBarListener(boolean yesNo){
		if (yesNo){
			seekBar.setOnSeekBarChangeListener(onSeekChangeListener);
			seekBar.setProgress(0);
		}
		else {
			seekBar.setOnSeekBarChangeListener(null);
		}
	}
	public void setSeekBarPositionMax(int position, int max){
		int[] maxHMMSS = new int[]{max/(1000*60*60), max/(1000*60), (max/1000)%60};
		int[] positionHMMSS = new int[]{position/(1000*60*60), position/(1000*60), (position/1000)%60};
		
		if (max != 0){
			seekBar.setMax(max);
			maxText.setText(timeFormatter(maxHMMSS));
		}
		seekBar.setProgress(position);
		currentText.setText(timeFormatter(positionHMMSS));
		
	}
	public String timeFormatter(int[] HMMSS){
		String readable = "";
		if (HMMSS[0] > 0){
			readable = Integer.toString(HMMSS[0]) + ":";
		}
		if ((HMMSS[1]<10 && HMMSS[0]>0) || HMMSS[1] == 0){
			readable = null;
			readable = "0" + Integer.toString(HMMSS[1]) + ":";
		}
		else{
			readable = readable + Integer.toString(HMMSS[1]) + ":";
		}
		if (HMMSS[2]< 10){
			readable = readable + "0" + Integer.toString(HMMSS[2]);
		}
		else{
			readable = readable + Integer.toString(HMMSS[2]);
		}
		return readable;
	}
}
