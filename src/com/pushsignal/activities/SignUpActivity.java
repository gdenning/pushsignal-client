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
import com.pushsignal.xml.simple.UserDTO;

public class SignUpActivity extends Activity {
	private EditText mEmail;
	private EditText mName;
	private Button mCreateAccountButton;
	private Button mExistingAccountButton;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sign_up);

		// Obtain handles to UI objects
		mEmail = (EditText) findViewById(R.id.emailEditor);
		mName = (EditText) findViewById(R.id.nameEditor);
		mCreateAccountButton = (Button) findViewById(R.id.createAccountButton);
		mExistingAccountButton = (Button) findViewById(R.id.existingAccountButton);

		mEmail.setText(AppUserDevice.getInstance().getGoogleUsername(this));
		
		// Register handler for UI elements
		mCreateAccountButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				Log.d(Constants.CLIENT_LOG_TAG, "mCreateAccountButton clicked");
				try {
					final RestClient restClient = RestClientStoredCredentials.getInstance();
					final UserDTO user = restClient.createAccount(
							mEmail.getText().toString(),
							mName.getText().toString(),
							"");

					// Save email and password in SharedPreferences
					final SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
					final SharedPreferences.Editor editor = settings.edit();
					editor.putString("email", mEmail.getText().toString());
					editor.putString("password", user.getPassword());
					editor.commit();
					
					launchMain();
					finish();
				} catch (final Exception ex) {
					NotificationHandler.showError(v.getContext(), ex);
				}
			}
		});

		mExistingAccountButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				Log.d(Constants.CLIENT_LOG_TAG, "mExistingAccountButton clicked");
				launchLogIn();
				finish();
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

	/**
	 * Launches the LogIn activity.
	 */
	private void launchLogIn() {
		final Intent i = new Intent(this, LogInActivity.class);
		startActivity(i);
	}
}
