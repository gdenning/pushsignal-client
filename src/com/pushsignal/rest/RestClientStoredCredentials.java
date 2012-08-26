package com.pushsignal.rest;

import android.content.Context;

import com.pushsignal.Constants;

public class RestClientStoredCredentials extends RestClient {
	private static boolean credentialsSet;
	private static RestClientStoredCredentials instance;
	
	public static RestClientStoredCredentials getInstance() {
		if (instance == null || credentialsSet == true) {
			instance = new RestClientStoredCredentials();
			credentialsSet = false;
		}
		return instance;
	}

	public static RestClientStoredCredentials getInstance(final Context context) {
		if (instance == null || credentialsSet == false) {
			instance = new RestClientStoredCredentials(context);
			credentialsSet = true;
		}
		return instance;
	}
	
	private RestClientStoredCredentials() {
		super();
	}

	private RestClientStoredCredentials(final Context context) {
		super(
				context.getSharedPreferences(Constants.PREFS_NAME, 0).getString("email", ""),
				context.getSharedPreferences(Constants.PREFS_NAME, 0).getString("password", "")
		);
	}

}
