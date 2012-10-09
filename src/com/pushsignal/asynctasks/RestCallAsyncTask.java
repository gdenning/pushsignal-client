package com.pushsignal.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.pushsignal.Constants;
import com.pushsignal.NotificationDisplay;
import com.pushsignal.rest.RestClient;
import com.pushsignal.rest.RestClientStoredCredentials;

public abstract class RestCallAsyncTask<T> extends AsyncTask<T, Void, Void> {
	private final Context context;
	private Exception exceptionOccurred;

	protected RestCallAsyncTask(final Context context) {
		this.context = context;
	}

	protected abstract void doRestCall(final RestClient restClient, final T... params) throws Exception;

	protected abstract void onSuccess(final Context context);

	protected void onException(final Context context, final Exception ex) {
		Log.e(Constants.CLIENT_LOG_TAG, exceptionOccurred.getMessage());
		NotificationDisplay.showError(context, exceptionOccurred.getMessage());
	}

	@Override
	protected Void doInBackground(final T... params) {
		final RestClient restClient = RestClientStoredCredentials.getInstance(context);
		try {
			doRestCall(restClient, params);
		} catch (final Exception ex) {
			exceptionOccurred = ex;
		}
		return null;
	}

	@Override
	protected void onPostExecute(final Void result) {
		if (exceptionOccurred != null) {
			onException(context, exceptionOccurred);
			return;
		}
		onSuccess(context);
	}
}
