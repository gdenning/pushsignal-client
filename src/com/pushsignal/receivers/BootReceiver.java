package com.pushsignal.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.pushsignal.services.PushSignalService;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(final Context context, final Intent intent) {
		if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
			final Intent i = new Intent(context, PushSignalService.class);
			context.startService(i);
		}
	}
}
