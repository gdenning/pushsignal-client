package com.pushsignal.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.pushsignal.AppUserDevice;
import com.pushsignal.Constants;
import com.pushsignal.NotificationHandler;
import com.pushsignal.R;
import com.pushsignal.asynctasks.RestCallAsyncTask;
import com.pushsignal.rest.RestClient;

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
				new LoginAsyncTask(v.getContext()).execute(email, password);
			}
		});
		
		mForgotPasswordButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				Log.d(Constants.CLIENT_LOG_TAG, "mForgotPasswordButton clicked");
				final String email = mEmail.getText().toString();
				new ResetPasswordAsyncTask(v.getContext()).execute(email);
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
	
	private final class LoginAsyncTask extends AsyncTask<String, Void, Void> {
		private final Context context;
		private Exception exceptionOccurred;

		private LoginAsyncTask(final Context context) {
			this.context = context;
		}

		@Override
		protected Void doInBackground(final String... params) {
			final String email = params[0];
			final String password = params[1];
			try {
				// Test the login
				RestClient restClient = new RestClient(email, password);
				restClient.getAllEvents();
			} catch (final Exception ex) {
				exceptionOccurred = ex;
			}
			return null;
		}

		@Override
		protected void onPostExecute(final Void result) {
			if (exceptionOccurred != null) {
				Log.e(Constants.CLIENT_LOG_TAG, exceptionOccurred.getMessage());
				NotificationHandler.showError(context, "Unable to login - incorrect email or password");
				return;
			}
			
			// Save email and password in SharedPreferences
			final SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
			final SharedPreferences.Editor editor = settings.edit();
			editor.putString("email", mEmail.getText().toString());
			editor.putString("password", mPassword.getText().toString());
			editor.commit();
			
			launchMain();
			finish();
		}
	}

	private final class ResetPasswordAsyncTask extends AsyncTask<String, Void, Void> {
		private final Context context;
		private Exception exceptionOccurred;

		private ResetPasswordAsyncTask(final Context context) {
			this.context = context;
		}

		@Override
		protected Void doInBackground(final String... params) {
			final String email = params[0];
			try {
				// Test the login
				RestClient restClient = new RestClient();
				restClient.resetAccountPassword(email);
			} catch (final Exception ex) {
				exceptionOccurred = ex;
			}
			return null;
		}

		@Override
		protected void onPostExecute(final Void result) {
			if (exceptionOccurred != null) {
				Log.e(Constants.CLIENT_LOG_TAG, exceptionOccurred.getMessage());
				NotificationHandler.showError(context, exceptionOccurred.getMessage());
				return;
			}
			NotificationHandler.showInfo(context,
					"Password has been reset - check your email for new password");
		}
	}
}
