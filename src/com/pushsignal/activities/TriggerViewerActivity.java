package com.pushsignal.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ListView;
import android.widget.TextView;

import com.pushsignal.NotificationHandler;
import com.pushsignal.R;
import com.pushsignal.adapters.TriggerAlertListAdapter;
import com.pushsignal.observers.AppObservable;
import com.pushsignal.observers.TriggerAlertListObserver;
import com.pushsignal.rest.RestClient;
import com.pushsignal.rest.RestClientStoredCredentials;
import com.pushsignal.xml.simple.TriggerAlertDTO;
import com.pushsignal.xml.simple.TriggerDTO;

public class TriggerViewerActivity extends Activity {
	private List<TriggerAlertDTO> triggerAlerts;

	TriggerAlertListAdapter adapter;

	private TextView mEventName;
	private TextView mDescription;
	private ListView mMemberList;

	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(final Message msg) {
			adapter.notifyDataSetChanged();
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.trigger_viewer);

		final RestClient restClient = RestClientStoredCredentials.getInstance(this);

		// Obtain handles to UI objects
		mEventName = (TextView) findViewById(R.id.eventName);
		mDescription = (TextView) findViewById(R.id.eventDescription);
		mMemberList = (ListView) findViewById(R.id.membersList);

		// Reload the trigger so that we have the latest triggerAlert records
		final Long triggerId = getIntent().getExtras().getLong("triggerId");
		try {
			final TriggerDTO trigger = restClient.getTrigger(triggerId);
			triggerAlerts = new ArrayList<TriggerAlertDTO>(trigger.getTriggerAlerts());
			adapter = new TriggerAlertListAdapter(this, R.layout.trigger_alert_list_item, triggerAlerts, trigger, getLayoutInflater());

			mEventName.setText(trigger.getEvent().getName());
			mDescription.setText(trigger.getEvent().getDescription());
			mMemberList.setAdapter(adapter);

			final AppObservable eventNotifier = AppObservable.getInstance();
			eventNotifier.addObserver(new TriggerAlertListObserver(handler, triggerAlerts));
		} catch (final Exception ex) {
			NotificationHandler.showError(this, ex);
		}
	}
}
