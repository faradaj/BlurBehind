package com.faradaj.blurbehind.sample;

import android.app.Activity;
import android.os.Bundle;
import com.faradaj.blurbehind.BlurBehind;

public class BlurredActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_blurred);

		BlurBehind.setBackground(this);
	}
}
