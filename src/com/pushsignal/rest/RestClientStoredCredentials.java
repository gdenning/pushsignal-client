package com.pushsignal.rest;

import android.content.Context;

import com.pushsignal.Constants;

public class RestClientStoredCredentials extends RestClient {
	private static RestClientStoredCredentials instance;

	public static RestClientStoredCredentials getInstance(final Context context) {
		if (instance == null) {
			instance = new RestClientStoredCredentials(context);
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
