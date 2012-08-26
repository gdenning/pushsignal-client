package com.pushsignal.push;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.pushsignal.Constants;
import com.pushsignal.observers.AppObservable;
import com.pushsignal.observers.ObserverData;
import com.pushsignal.observers.ObserverData.ObjectTypeEnum;
import com.pushsignal.services.PushSignalService;

public class PushClientC2dmImpl extends BroadcastReceiver implements PushClient {

	private static final String C2DM_ERROR_EXTRA = "error";
	private static final String APP_SENDER_ID = "gdenning@gmail.com";
	private static final String C2DM_MESSAGE_EXTRA = "message";
	private static final String C2DM_REGISTRATION_EXTRA = "registration_id";

	private AppObservable appObservable = AppObservable.getInstance();

	private static long backoffTimeMs = 1000;
	
	@Override
	public void onReceive(final Context context, final Intent intent) {
		if (intent.getAction().equals("com.google.android.c2dm.intent.REGISTRATION")) {
			handleRegistration(context, intent);
		} else if (intent.getAction().equals("com.google.android.c2dm.intent.RECEIVE")) {
			handleMessage(context, intent);
		}
	}

	private void handleRegistration(final Context context, final Intent intent) {
		final String registrationId = intent.getStringExtra(C2DM_REGISTRATION_EXTRA);
		final String error = intent.getStringExtra(C2DM_ERROR_EXTRA);
		if (error != null) {
			Log.e(Constants.SERVICE_LOG_TAG, "C2DM registration error: " + error);
            if ("SERVICE_NOT_AVAILABLE".equals(error)) {
                Log.d(Constants.SERVICE_LOG_TAG, "Scheduling registration retry, backoff = " + backoffTimeMs);
                Intent retryIntent = new Intent(context, PushSignalService.class);
                retryIntent.setAction(Constants.ACTION_REGISTER);
                PendingIntent retryPIntent = PendingIntent.getService(context, 
                        0 /*requestCode*/, retryIntent, 0 /*flags*/);
                
                AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                am.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + backoffTimeMs, retryPIntent);

                // Next retry should wait longer.
                backoffTimeMs *= 2;
            } 
		} else if (intent.getStringExtra("unregistered") != null) {
			Log.i(Constants.SERVICE_LOG_TAG, "Unregistered from C2DM");
			backoffTimeMs = 1000;
		} else if (registrationId != null) {
			Log.i(Constants.SERVICE_LOG_TAG, "Registered to C2DM: " + registrationId);
			backoffTimeMs = 1000;
			final ObserverData observerData = new ObserverData(
					ObjectTypeEnum.SERVER_REGISTRATION_ID,
					null,
					registrationId);
			appObservable.notifyObservers(observerData);
		}
	}

	private void handleMessage(final Context context, final Intent intent) {
		final String message = intent.getStringExtra(C2DM_MESSAGE_EXTRA);
		final ObserverData observerData = new ObserverData(
				ObjectTypeEnum.SERVER_MESSAGE,
				null,
				message);
		appObservable.notifyObservers(observerData);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void connectOrRegister(final Context context) {
        final Intent registrationIntent = new Intent("com.google.android.c2dm.intent.REGISTER");
		registrationIntent.putExtra("app", PendingIntent.getBroadcast(context, 0, new Intent(), 0));
		registrationIntent.putExtra("sender", APP_SENDER_ID);
		context.startService(registrationIntent);
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
}
