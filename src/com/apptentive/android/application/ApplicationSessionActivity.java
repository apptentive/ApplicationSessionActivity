/*
 * Copyright (C) 2012 Apptentive, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.apptentive.android.application;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

/**
 * @author Sky Kelsey
 * @see <a href="https://twitter.com/skykelsey">@skykelsey</a>
 * @see <a href="https://twitter.com/apptentive">@apptentive</a>
 */
public abstract class ApplicationSessionActivity extends Activity {

	private static final String TAG = "ApplicationSessionActivity";
	private static final String KEY_APP_IN_BACKGROUND = "AppInBackground";
	private static final String KEY_APP_SESSION_ACTIVE = "AppSessionActive";
	private static final String KEY_MAIN_ACTIVITY_NAME = "MainActivityName";

	private SharedPreferences prefs;

	private static SessionStartedListener sessionStartedListener;
	private static SessionStoppedListener sessionStoppedListener;


	/**
	 * Used to listen for Session starts.
	 */
	public interface SessionStartedListener {
		public void onSessionStarted();
	}

	/**
	 * Used to listen for Session stops.
	 */
	public interface SessionStoppedListener {
		public void onSessionStopped();
	}

	/**
	 * Sets the SessionStartedListener for this Activity.
	 */
	protected static void setOnSessionStartedListener(SessionStartedListener sessionStartedListener) {
		ApplicationSessionActivity.sessionStartedListener = sessionStartedListener;
	}

	/**
	 * Sets the SessionStoppedListener for this Activity.
	 */
	protected static void setOnSessionStoppedListener(SessionStoppedListener sessionStoppedListener) {
		ApplicationSessionActivity.sessionStoppedListener = sessionStoppedListener;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		prefs = getSharedPreferences(TAG, MODE_PRIVATE);

		// Pretend we came from background so a new session will start.
		if(isCurrentActivityMainActivity(this)) {
			prefs.edit().putBoolean(KEY_APP_IN_BACKGROUND, true).commit();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		startSession();
	}

	@Override
	protected void onStop() {
		super.onStop();
		// Stopping because the Application was backgrounded because the HOME key was pressed, or another application was
		// switched to.
		if(isApplicationBroughtToBackground(this)) {
			prefs.edit().putBoolean(KEY_APP_IN_BACKGROUND, true).commit();
			endSession();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Stopping because the BACK key was pressed from just the home Activity.
		if(isCurrentActivityMainActivity(this)) {
			endSession();
			sessionStartedListener = null;
			sessionStoppedListener = null;
		}
	}

	private void startSession() {
		boolean comingFromBackground = prefs.getBoolean(KEY_APP_IN_BACKGROUND, true);

		if(comingFromBackground) {
			boolean activeSession = prefs.getBoolean(KEY_APP_SESSION_ACTIVE, false);
			if(!activeSession) {
				if(sessionStartedListener != null) {
					sessionStartedListener.onSessionStarted();
				}
			} else {
				Log.e(TAG, "Error: Starting session, but a session is already active.");
			}
			prefs.edit().putBoolean(KEY_APP_SESSION_ACTIVE, true).putBoolean(KEY_APP_IN_BACKGROUND, false).commit();
		}
	}

	private void endSession() {
		boolean activeSession = prefs.getBoolean(KEY_APP_SESSION_ACTIVE, false);
		if(activeSession) {
			if(sessionStoppedListener != null) {
				sessionStoppedListener.onSessionStopped();
			}
		} else {
			Log.e(TAG, "Error: Ending session, but no session is active.");
		}
		prefs.edit().putBoolean(KEY_APP_SESSION_ACTIVE, false).commit();
	}


	/**
	 * Call this in the onStop() method of an Activity. Tells you if the Activity is stopping
	 * because the Application is going to the background, or because of some other reason. Other reasons include the app
	 * exiting, or a new Activity in the same Application starting.
	 * @param activity The Activity from which this method is called.
	 * @return <p>true - if the Application is  stopping to go to the background.</p>
	 *         <p>false - for any other reason the app is stopping.</p>
	 */
	private static boolean isApplicationBroughtToBackground(final Activity activity) {
		ActivityManager activityManager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> tasks = null;
		try {
			tasks = activityManager.getRunningTasks(1);
		} catch (SecurityException e) {
			Log.e(TAG, "Missing required permission: \"android.permission.GET_TASKS\".", e);
			return false;
		}
		if (tasks != null && !tasks.isEmpty()) {
			ComponentName topActivity = tasks.get(0).topActivity;
			try {
				PackageInfo pi = activity.getPackageManager().getPackageInfo(activity.getPackageName(), PackageManager.GET_ACTIVITIES);
				for (ActivityInfo activityInfo : pi.activities) {
					if(topActivity.getClassName().equals(activityInfo.name)) {
						return false;
					}
				}
			} catch( PackageManager.NameNotFoundException e) {
				Log.e(TAG, "Package name not found: " + activity.getPackageName());
				return false; // Never happens.
			}
		}
		return true;
	}

	/**
	 * Tells you whether the currentActivity is the main Activity of the app. In order for this to work, it must be called
	 * from the main Activity first. One way to enforce this rule is to call it in the main Activity's onCreate().
	 * @param currentActivity The Activity from which this method is called.
	 * @return true iff currentActivity is the Application's main Activity.
	 */
	private boolean isCurrentActivityMainActivity(Activity currentActivity) {
		String currentActivityName = currentActivity.getComponentName().getClassName();
		String mainActivityName = prefs.getString(KEY_MAIN_ACTIVITY_NAME, null);
		// The first time this runs, it will be from the main Activity, guaranteed.
		if(mainActivityName == null) {
			mainActivityName = currentActivityName;
			prefs.edit().putString(KEY_MAIN_ACTIVITY_NAME, mainActivityName).commit();
		}
		return currentActivityName != null && currentActivityName.equals(mainActivityName);
	}
}
