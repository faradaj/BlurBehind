package com.faradaj.blurbehind.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.faradaj.blurbehind.BlurBehind;

public class MainActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		final Button dummyButton = (Button) findViewById(R.id.dummy_button);

		dummyButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				Runnable runnable = new Runnable() {
					@Override
					public void run() {
						Intent intent = new Intent(MainActivity.this, BlurredActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

						startActivity(intent);
					}
				};
				BlurBehind.execute(MainActivity.this, runnable);
			}
		});
	}
}
