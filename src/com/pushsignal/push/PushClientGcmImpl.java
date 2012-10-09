package com.pushsignal.push;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;
import com.pushsignal.Constants;
import com.pushsignal.observers.AppObservable;
import com.pushsignal.observers.ObserverData;
import com.pushsignal.observers.ObserverData.ObjectTypeEnum;

public class PushClientGcmImpl extends GCMBaseIntentService implements PushClient {

	private static final String APP_SENDER_ID = "372424089937";
	private static final String GCM_MESSAGE_EXTRA = "message";

	private final AppObservable appObservable = AppObservable.getInstance();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void connectOrRegister(final Context context) {
		// See http://developer.android.com/guide/google/gcm/gs.html#
		GCMRegistrar.checkDevice(context);
		GCMRegistrar.checkManifest(context);
		final String regId = GCMRegistrar.getRegistrationId(context);
		if (regId.equals("")) {
			GCMRegistrar.register(context, APP_SENDER_ID);
		} else {
			Log.v(Constants.CLIENT_LOG_TAG, "Already registered");
			final ObserverData observerData = new ObserverData(
					ObjectTypeEnum.SERVER_REGISTRATION_ID,
					null,
					regId);
			appObservable.notifyObservers(observerData);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void disconnect() {
		// Do nothing
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isConnected() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sendKeepAlive() {
		// Do nothing
	}

	@Override
	protected void onError(final Context context, final String errorId) {
		Log.e(Constants.SERVICE_LOG_TAG, "GCM registration error: " + errorId);
	}

	@Override
	protected void onMessage(final Context context, final Intent intent) {
		final String message = intent.getStringExtra(GCM_MESSAGE_EXTRA);
		final ObserverData observerData = new ObserverData(
				ObjectTypeEnum.SERVER_MESSAGE,
				null,
				message);
		appObservable.notifyObservers(observerData);
	}

	@Override
	protected void onRegistered(final Context context, final String regId) {
		Log.i(Constants.SERVICE_LOG_TAG, "Registered to GCM: " + regId);
		final ObserverData observerData = new ObserverData(
				ObjectTypeEnum.SERVER_REGISTRATION_ID,
				null,
				regId);
		appObservable.notifyObservers(observerData);
	}

	@Override
	protected void onUnregistered(final Context context, final String regId) {
		Log.i(Constants.SERVICE_LOG_TAG, "Unregistered from GCM");
	}
}
