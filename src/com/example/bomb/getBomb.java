package com.example.bomb;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;

public class getBomb extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
        setContentView(R.layout.getbomb);
        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);         
//      long[] pattern = {1000, 200, 1000, 2000, 1200};
//      vibe.vibrate(pattern, 0);
      vibe.vibrate(20000);
	}

}
