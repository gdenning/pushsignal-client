package com.pushsignal.services;

import java.util.Observable;
import java.util.Observer;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.provider.Settings.Secure;
import android.util.Log;

import com.pushsignal.AppUserDevice;
import com.pushsignal.Constants;
import com.pushsignal.NotificationDisplay;
import com.pushsignal.observers.AppObservable;
import com.pushsignal.observers.ObserverData;
import com.pushsignal.observers.ObserverData.ActionTypeEnum;
import com.pushsignal.observers.ObserverData.ObjectTypeEnum;
import com.pushsignal.push.PushClient;
import com.pushsignal.push.PushClientGcmImpl;
import com.pushsignal.rest.RestClient;
import com.pushsignal.rest.RestClientStoredCredentials;
import com.pushsignal.xml.simple.ActivityDTO;
import com.pushsignal.xml.simple.EventDTO;
import com.pushsignal.xml.simple.EventInviteDTO;
import com.pushsignal.xml.simple.TriggerAlertDTO;
import com.pushsignal.xml.simple.TriggerDTO;
import com.pushsignal.xml.simple.UserDeviceDTO;

public class PushSignalService extends Service implements Observer {
	private RestClient restClient;
	private PushClient pushClient;
	private UserDeviceDTO userDevice;

	// Notification manager to displaying arrived push notifications
	private NotificationManager notificationManager;

	private String deviceId;
	private String registrationId;

	private static final String CLIENT_TYPE = "GCM";

	/**
	 * Notification from the pushClient about an incoming message or connection lost situation.
	 */
	@Override
	public void update(final Observable observable, final Object data) {
		final ObserverData observerData = (ObserverData) data;
		if (observerData.getObjectType() == ObjectTypeEnum.SERVER_MESSAGE) {
			handleMessage((String) observerData.getObjectData());
		} else if (observerData.getObjectType() == ObjectTypeEnum.SERVER_REGISTRATION_ID) {
			registrationId = (String) observerData.getObjectData();
			try {
				Log.d(Constants.SERVICE_LOG_TAG, "Registering device with server");
				userDevice = restClient.registerDevice(CLIENT_TYPE, deviceId, registrationId);
				AppUserDevice.getInstance().setUserDevice(userDevice);
				final AppObservable notifier = AppObservable.getInstance();
				notifier.notifyObservers(new ObserverData(
						ObjectTypeEnum.USER_DEVICE,
						ActionTypeEnum.MODIFIED,
						userDevice));
			} catch (final Exception ex) {
				Log.w(Constants.SERVICE_LOG_TAG, "Unable to register device: " + ex.toString());
			}
		}
	}

	/**
	 * Handler for incoming messages.
	 * 
	 * @param message The message received from the push server.
	 */
	private void handleMessage(final String message) {
		Log.d(Constants.SERVICE_LOG_TAG, "Got message: " + message);

		final String values[] = message.split(":");
		if ("TRIGGER_ALERT".equals(values[0])) {
			try {
				final TriggerAlertDTO alert = restClient.getTriggerAlert(Long.parseLong(values[1]));
				final TriggerDTO trigger = restClient.getTrigger(alert.getTriggerId());
				if (alert.getUser().equals(userDevice.getUser())) {
					NotificationDisplay.showNotification(notificationManager, this, trigger);
				} else {
					Log.w(Constants.SERVICE_LOG_TAG, "Received trigger notification for a different user");
				}
			} catch (final Exception ex) {
				Log.e(Constants.SERVICE_LOG_TAG, "Unable to retrieve trigger alert: " + ex.getMessage());
			}
		} else if ("TRIGGER_ACK".equals(values[0]) || "TRIGGER_SILENCE".equals(values[0])) {
			try {
				final TriggerAlertDTO alert = restClient.getTriggerAlert(Long.parseLong(values[1]));
				if (alert.getUser().equals(userDevice.getUser())) {
					NotificationDisplay.cancelTriggerNotification(notificationManager, alert.getTriggerId());
				}
				final AppObservable notifier = AppObservable.getInstance();
				notifier.notifyObservers(new ObserverData(
						ObjectTypeEnum.TRIGGER_ALERT,
						ActionTypeEnum.MODIFIED,
						alert));
			} catch (final Exception ex) {
				Log.e(Constants.SERVICE_LOG_TAG, "Unable to retrieve trigger alert: " + ex.getMessage());
			}
		} else if ("INVITE".equals(values[0])) {
			try {
				final EventInviteDTO invite = restClient.getInvite(Long.parseLong(values[1]));
				NotificationDisplay.showNotification(notificationManager, this, invite);
				final AppObservable notifier = AppObservable.getInstance();
				notifier.notifyObservers(new ObserverData(
						ObjectTypeEnum.EVENT_INVITE,
						ActionTypeEnum.CREATED,
						invite));
			} catch (final Exception ex) {
				Log.e(Constants.SERVICE_LOG_TAG, "Unable to retrieve invite: " + ex.getMessage());
			}
		} else if ("EVENT_CHANGED".equals(values[0])) {
			try {
				final EventDTO event = restClient.getEvent(Long.parseLong(values[1]));
				final AppObservable notifier = AppObservable.getInstance();
				notifier.notifyObservers(new ObserverData(
						ObjectTypeEnum.EVENT,
						ActionTypeEnum.MODIFIED,
						event));
			} catch (final Exception ex) {
				Log.e(Constants.SERVICE_LOG_TAG, "Unable to retrieve event: " + ex.getMessage());
			}
		} else if ("EVENT_DELETED".equals(values[0])) {
			final Long eventId = Long.parseLong(values[1]);
			final AppObservable notifier = AppObservable.getInstance();
			notifier.notifyObservers(new ObserverData(
					ObjectTypeEnum.EVENT,
					ActionTypeEnum.DELETED,
					eventId));
		} else if ("POINTS_CHANGED".equals(values[0])) {
			final Long points = Long.parseLong(values[1]);
			if (userDevice != null) {
				userDevice.getUser().setPoints(points);
				final AppObservable notifier = AppObservable.getInstance();
				notifier.notifyObservers(new ObserverData(
						ObjectTypeEnum.USER_DEVICE,
						ActionTypeEnum.MODIFIED,
						userDevice));
			}
		} else if ("ACTIVITY_CREATED".equals(values[0])) {
			try {
				final ActivityDTO activity = restClient.getActivity(Long.parseLong(values[1]));
				final AppObservable notifier = AppObservable.getInstance();
				notifier.notifyObservers(new ObserverData(
						ObjectTypeEnum.ACTIVITY,
						ActionTypeEnum.CREATED,
						activity));
			} catch (final Exception ex) {
				Log.e(Constants.SERVICE_LOG_TAG, "Unable to retrieve activity: " + ex.getMessage());
			}
		}
	}

	@Override
	public void onCreate() {
		Log.d(Constants.SERVICE_LOG_TAG, "Service created");
		restClient = RestClientStoredCredentials.getInstance(this);
		AppObservable.getInstance().addObserver(this);
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		deviceId = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);
		pushClient = new PushClientGcmImpl();

		Log.d(Constants.SERVICE_LOG_TAG, "Attempting to register with C2DM");
		pushClient.connectOrRegister(this);
	}

	@Override
	public void onDestroy() {
		Log.d(Constants.SERVICE_LOG_TAG, "Service destroyed");
	}

	@Override
	public int onStartCommand(final Intent intent, final int flags, final int startId) {
		if (intent != null && intent.getAction() != null) {
			if (intent.getAction().equals(Constants.ACTION_REGISTER) && !isRegistered()) {
				Log.d(Constants.SERVICE_LOG_TAG, "Attempting to register with C2DM");
				pushClient.connectOrRegister(this);
			}
		}
		return START_STICKY;
	}

	@Override
	public IBinder onBind(final Intent intent) {
		return null;
	}

	private boolean isRegistered() {
		return registrationId != null;
	}
}
