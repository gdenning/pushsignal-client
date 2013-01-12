package com.pushsignal;

import java.net.UnknownHostException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.pushsignal.activities.ActivityListActivity;
import com.pushsignal.activities.TriggerRespondActivity;
import com.pushsignal.xml.simple.EventInviteDTO;
import com.pushsignal.xml.simple.TriggerDTO;

public class NotificationDisplay {

	private final static int MIN_TRIGGER_ID = 0;
	private final static int MIN_INVITE_ID = 1000000;
	private final static int MODULO = 1000000;

	/**
	 * Log the error and display a toast message.
	 * 
	 * @param context
	 * @param errorMessage
	 */
	public static void showError(final Context context, final Exception ex) {
		if (ex.getMessage() == null || ex instanceof UnknownHostException) {
			showError(context, ex.toString());
		} else {
			showError(context, ex.getMessage());
		}
	}

	/**
	 * Log the error and display a toast message.
	 * 
	 * @param context
	 * @param errorMessage
	 */
	public static void showError(final Context context, final String errorMessage) {
		Log.e(Constants.CLIENT_LOG_TAG, errorMessage);
		final Toast toast = Toast.makeText(context, errorMessage, Toast.LENGTH_LONG);
		toast.show();
	}

	/**
	 * Display a toast message.
	 * 
	 * @param context
	 * @param errorMessage
	 */
	public static void showInfo(final Context context, final String message) {
		final Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
		toast.show();
	}

	/**
	 * Display the topbar notification for a trigger.
	 *
	 * @param notificationManager
	 * @param context
	 * @param trigger
	 */
	public static void showNotification(final NotificationManager notificationManager,
			final Context context, final TriggerDTO trigger) {
		final int icon = R.drawable.ic_stat_signal;
		final CharSequence tickerText = trigger.getEvent().getName() + ": " + trigger.getMessage();
		final long when = System.currentTimeMillis();

		final Notification n = new Notification(icon, tickerText, when);
		n.defaults |= Notification.DEFAULT_VIBRATE;
		n.defaults |= Notification.DEFAULT_LIGHTS;
		n.defaults |= Notification.DEFAULT_SOUND;
		//n.flags |= Notification.FLAG_INSISTENT;
		//n.sound = Uri.withAppendedPath(Audio.Media.INTERNAL_CONTENT_URI, "5");
		n.flags |= Notification.FLAG_SHOW_LIGHTS;
		n.ledARGB = 0xff00ff00;
		n.ledOnMS = 300;
		n.ledOffMS = 1000;

		final Intent intent = new Intent(context, TriggerRespondActivity.class);
		// setData method is required so that Android will differentiate between intents
		intent.setData(Uri.fromParts("trigger", String.valueOf(trigger.getTriggerId()), null));
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("trigger", trigger);
		final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

		n.setLatestEventInfo(context,
				trigger.getEvent().getName(),
				trigger.getMessage(),
				pendingIntent);

		notificationManager.notify((int) (trigger.getTriggerId() % MODULO) + MIN_TRIGGER_ID, n);
	}

	/**
	 * Cancel the topbar notification for a trigger.
	 *
	 * @param notificationManager
	 * @param trigger
	 */
	public static void cancelTriggerNotification(final NotificationManager notificationManager,
			final long triggerId) {
		notificationManager.cancel((int) (triggerId % MODULO) + MIN_TRIGGER_ID);
	}

	/**
	 * Display the topbar notification for an invite.
	 *
	 * @param notificationManager
	 * @param context
	 * @param trigger
	 */
	public static void showNotification(final NotificationManager notificationManager,
			final Context context, final EventInviteDTO invite) {
		final int icon = R.drawable.ic_stat_happy;
		final CharSequence tickerText = "Invite to " + invite.getEvent().getName();
		final long when = System.currentTimeMillis();

		final Notification n = new Notification(icon, tickerText, when);
		n.defaults |= Notification.DEFAULT_LIGHTS;
		n.defaults |= Notification.DEFAULT_SOUND;
		n.flags |= Notification.FLAG_SHOW_LIGHTS;
		n.flags |= Notification.FLAG_AUTO_CANCEL;
		n.ledARGB = 0xff00ff00;
		n.ledOnMS = 300;
		n.ledOffMS = 1000;

		final Intent intent = new Intent(context, ActivityListActivity.class);
		// setData method is required so that Android will differentiate between intents
		intent.setData(Uri.fromParts("invite", String.valueOf(invite.getEventInviteId()), null));
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("invite", invite);
		final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

		n.setLatestEventInfo(context,
				"Invite to " + invite.getEvent().getName(),
				invite.getEvent().getDescription(),
				pendingIntent);

		notificationManager.notify((int) (invite.getEventInviteId() % MODULO) + MIN_INVITE_ID, n);
	}

	/**
	 * Cancel the topbar notification for an invite.
	 *
	 * @param notificationManager
	 * @param trigger
	 */
	public static void cancelInviteNotification(final NotificationManager notificationManager,
			final long eventInviteId) {
		notificationManager.cancel((int) (eventInviteId % MODULO) + MIN_INVITE_ID);
	}
}
