package com.pushsignal.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.pushsignal.AppUserDevice;
import com.pushsignal.Constants;
import com.pushsignal.NotificationHandler;
import com.pushsignal.R;
import com.pushsignal.rest.RestClient;
import com.pushsignal.rest.RestClientStoredCredentials;

public class LogInActivity extends Activity {
	private EditText mEmail;
	private EditText mPassword;
	private Button mLogInButton;
	private Button mForgotPasswordButton;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.log_in);

		// Obtain handles to UI objects
		mEmail = (EditText) findViewById(R.id.emailEditor);
		mPassword = (EditText) findViewById(R.id.passwordEditor);
		mLogInButton = (Button) findViewById(R.id.loginButton);
		mForgotPasswordButton = (Button) findViewById(R.id.forgotPasswordButton);
		
		mEmail.setText(AppUserDevice.getInstance().getGoogleUsername(this));

		// Register handler for UI elements
		mLogInButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				Log.d(Constants.CLIENT_LOG_TAG, "mCreateAccountButton clicked");
				final String email = mEmail.getText().toString();
				final String password = mPassword.getText().toString();
				try {
					// Test the login
					RestClient restClient = new RestClient(email, password);
					restClient.getAllEvents();

					// Save email and password in SharedPreferences
					final SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
					final SharedPreferences.Editor editor = settings.edit();
					editor.putString("email", mEmail.getText().toString());
					editor.putString("password", mPassword.getText().toString());
					editor.commit();
					
					launchMain();
					finish();
				} catch (final Exception ex) {
					NotificationHandler.showError(v.getContext(), "Unable to login - incorrect email or password");
				}
			}
		});
		
		mForgotPasswordButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(Constants.CLIENT_LOG_TAG, "mForgotPasswordButton clicked");
				final String email = mEmail.getText().toString();
				RestClient restClient = RestClientStoredCredentials.getInstance();
				try {
					restClient.resetAccountPassword(email);
					NotificationHandler.showInfo(v.getContext(), "Password has been reset - check your email for new password");
				} catch (Exception e) {
					NotificationHandler.showError(v.getContext(), e);
				}
			}
		});
	}

	/**
	 * Launches the Main activity.
	 */
	private void launchMain() {
		final Intent i = new Intent(this, MainActivity.class);
		startActivity(i);
	}
}
