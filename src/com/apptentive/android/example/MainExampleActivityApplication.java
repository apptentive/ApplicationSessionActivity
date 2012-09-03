package com.apptentive.android.example;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.apptentive.android.application.ApplicationSessionActivity;

public class MainExampleActivityApplication extends ApplicationSessionActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Pass a listener in so we can be notified of session starts.
		setOnSessionStartedListener(new SessionStartedListener() {
			public void onSessionStarted() {
				Log.e("ApplicationSessionExample", "Starting session.");
			}
		});

		// Pass a listener in so we can be notified of session stops.
		setOnSessionStoppedListener(new SessionStoppedListener() {
			public void onSessionStopped() {
				Log.e("ApplicationSessionExample", "Stopping session.");
			}
		});
	}

	/**
	 * Referenced from main.xml
	 * @param view
	 */
	public void launchChildActivity(View view) {
		Intent intent = new Intent();
		intent.setClass(this, ChildExampleActivityApplication.class);
		startActivity(intent);
	}
}
