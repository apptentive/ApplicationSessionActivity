package com.apptentive.android.example;

import android.os.Bundle;
import com.apptentive.android.application.ApplicationSessionActivity;

/**
 * @author Sky Kelsey
 */
public class ChildExampleActivityApplication extends ApplicationSessionActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.child);
	}
}
