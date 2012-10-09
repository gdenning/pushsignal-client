package com.pushsignal.activities;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TabHost;
import android.widget.TextView;

import com.pushsignal.AppUserDevice;
import com.pushsignal.R;
import com.pushsignal.observers.AppObservable;
import com.pushsignal.observers.BasicObserver;
import com.pushsignal.observers.ObserverData.ObjectTypeEnum;
import com.pushsignal.services.PushSignalService;

public class MainActivity extends TabActivity {

	private TextView usernameTextView;
	private TextView pointsTextView;

	private final Handler handleDeviceRegistered = new Handler() {
		@Override
		public void handleMessage(final Message msg) {
			displayStatusBar();
		}
	};

	private void displayStatusBar() {
		usernameTextView = (TextView) findViewById(R.id.username);
		pointsTextView = (TextView) findViewById(R.id.points);

		final AppUserDevice appUserDevice = AppUserDevice.getInstance();
		if (appUserDevice != null && appUserDevice.getUser() != null) {
			usernameTextView.setText(appUserDevice.getUser().getName());
			pointsTextView.setText(appUserDevice.getUser().getPoints() + " points");
		}
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		final AppUserDevice appUserDevice = AppUserDevice.getInstance();
		if (appUserDevice != null && appUserDevice.getUser() != null) {
			displayStatusBar();
		}

		AppObservable.getInstance().addObserver(new BasicObserver(handleDeviceRegistered, ObjectTypeEnum.USER_DEVICE));

		final Resources res = getResources(); // Resource object to get Drawables
		final TabHost tabHost = getTabHost();  // The activity TabHost
		TabHost.TabSpec spec;  // Resusable TabSpec for each tab
		Intent intent;  // Reusable Intent for each tab

		// Initialize create event tab
		intent = new Intent().setClass(this, EventEditorActivity.class);
		spec = tabHost.newTabSpec("create")
				.setIndicator("Create", res.getDrawable(R.drawable.ic_tab_signal))
				.setContent(intent);
		tabHost.addTab(spec);

		// Initialize event list tab
		intent = new Intent().setClass(this, EventListActivity.class);
		spec = tabHost.newTabSpec("eventList")
				.setIndicator("Events", res.getDrawable(R.drawable.ic_tab_tags))
				.setContent(intent);
		tabHost.addTab(spec);

		// Initialize public events tab
		intent = new Intent().setClass(this, PublicEventListActivity.class);
		spec = tabHost.newTabSpec("publicList")
				.setIndicator("Public", res.getDrawable(R.drawable.ic_tab_planet))
				.setContent(intent);
		tabHost.addTab(spec);

		// Initialize activities tab
		intent = new Intent().setClass(this, ActivityListActivity.class);
		spec = tabHost.newTabSpec("activityList")
				.setIndicator("Activity", res.getDrawable(R.drawable.ic_tab_happy))
				.setContent(intent);
		tabHost.addTab(spec);

		tabHost.setCurrentTab(0);

		// Start the service
		startService();
	}

	private void startService() {
		final Thread t = new Thread() {
			@Override
			public void run() {
				final Intent i = new Intent(MainActivity.this, PushSignalService.class);
				startService(i);
			}
		};
		t.start();
	}
}
