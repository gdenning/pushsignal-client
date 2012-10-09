package com.pushsignal.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ListView;
import android.widget.TextView;

import com.pushsignal.R;
import com.pushsignal.adapters.TriggerAlertListAdapter;
import com.pushsignal.asynctasks.RestCallAsyncTask;
import com.pushsignal.observers.AppObservable;
import com.pushsignal.observers.TriggerAlertListObserver;
import com.pushsignal.rest.RestClient;
import com.pushsignal.xml.simple.TriggerAlertDTO;
import com.pushsignal.xml.simple.TriggerDTO;

public class TriggerViewerActivity extends Activity {
	private List<TriggerAlertDTO> triggerAlerts;

	private TriggerAlertListAdapter adapter;

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

		// Obtain handles to UI objects
		mEventName = (TextView) findViewById(R.id.eventName);
		mDescription = (TextView) findViewById(R.id.eventDescription);
		mMemberList = (ListView) findViewById(R.id.membersList);

		// Reload the trigger so that we have the latest triggerAlert records
		final Long triggerId = getIntent().getExtras().getLong("triggerId");
		new LoadTriggerAsyncTask(this).execute(triggerId);
	}

	private class LoadTriggerAsyncTask extends RestCallAsyncTask<Long> {
		private TriggerDTO trigger;

		public LoadTriggerAsyncTask(final Context context) {
			super(context);
		}

		@Override
		protected void doRestCall(final RestClient restClient, final Long... params) throws Exception {
			final Long triggerId = params[0];
			trigger = restClient.getTrigger(triggerId);
		}

		@Override
		protected void onSuccess(final Context context) {
			triggerAlerts = new ArrayList<TriggerAlertDTO>(trigger.getTriggerAlerts());
			adapter = new TriggerAlertListAdapter(context, R.layout.trigger_alert_list_item, triggerAlerts, trigger, getLayoutInflater());

			mEventName.setText(trigger.getEvent().getName());
			mDescription.setText(trigger.getEvent().getDescription());
			mMemberList.setAdapter(adapter);

			final AppObservable eventNotifier = AppObservable.getInstance();
			eventNotifier.addObserver(new TriggerAlertListObserver(handler, triggerAlerts));
		}

	}
}
