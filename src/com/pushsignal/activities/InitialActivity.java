package com.pushsignal.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.pushsignal.Constants;

public class InitialActivity extends Activity {

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		Log.v(Constants.CLIENT_LOG_TAG, "PushSignal InitialActivity Created");
		super.onCreate(savedInstanceState);
		final SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
		final String email = settings.getString("email", null);
		if (email == null) {
			launchSignUp();
		} else {
			launchMain();
		}
		finish();
	}

	/**
	 * Launches the Main activity.
	 */
	private void launchMain() {
		final Intent i = new Intent(this, MainActivity.class);
		startActivity(i);
	}

	/**
	 * Launches the SignUp activity.
	 */
	private void launchSignUp() {
		final Intent i = new Intent(this, SignUpActivity.class);
		startActivity(i);
	}
}
