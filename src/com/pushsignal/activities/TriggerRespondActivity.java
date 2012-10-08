package com.pushsignal.activities;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.pushsignal.Constants;
import com.pushsignal.NotificationHandler;
import com.pushsignal.R;
import com.pushsignal.asynctasks.RestCallAsyncTask;
import com.pushsignal.rest.RestClient;
import com.pushsignal.xml.simple.TriggerDTO;

public class TriggerRespondActivity extends Activity {
	private TriggerDTO trigger;

	private NotificationManager notificationManager;

	private TextView mEventName;
	private TextView mDescription;
	private Button mAcknowledgeButton;
	private Button mIgnoreButton;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.trigger_respond);

		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		
		// Obtain handles to UI objects
		mEventName = (TextView) findViewById(R.id.eventName);
		mDescription = (TextView) findViewById(R.id.eventDescription);
		mAcknowledgeButton = (Button) findViewById(R.id.acknowledge);
		mIgnoreButton = (Button) findViewById(R.id.ignore);

		trigger = (TriggerDTO) getIntent().getExtras().getSerializable("trigger");
		mEventName.setText(trigger.getEvent().getName());
		mDescription.setText(trigger.getEvent().getDescription());
	
		// Register handler for UI elements
		mAcknowledgeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				Log.d(Constants.CLIENT_LOG_TAG, "mAcknowledgeButton clicked (Trigger:" + trigger.getTriggerId() + ")");
				new AckTriggerAsyncTask(v.getContext()).execute(trigger.getTriggerId());
			}
		});
		mIgnoreButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				Log.d(Constants.CLIENT_LOG_TAG, "mIgnoreButton clicked (Trigger:" + trigger.getTriggerId() + ")");
				new SilenceTriggerAsyncTask(v.getContext()).execute(trigger.getTriggerId());
			}
		});
	}

	/**
	 * Launches the TriggerViewer activity to show information about a
	 * particular trigger.
	 */
	private void launchTriggerViewer(TriggerDTO trigger) {
		Intent i = new Intent(this, TriggerViewerActivity.class);
		i.putExtra("triggerId", trigger.getTriggerId());
		startActivity(i);
	}
	
	private class AckTriggerAsyncTask extends RestCallAsyncTask<Long> {

		public AckTriggerAsyncTask(final Context context) {
			super(context);
		}

		@Override
		protected void doRestCall(RestClient restClient, Long... params) throws Exception {
			Long triggerId = params[0];
			restClient.ackTrigger(triggerId);
		}

		@Override
		protected void onSuccess(Context context) {
			NotificationHandler.cancelTriggerNotification(notificationManager, trigger.getTriggerId());
			launchTriggerViewer(trigger);
			finish();
		}
	}

	private class SilenceTriggerAsyncTask extends RestCallAsyncTask<Long> {

		public SilenceTriggerAsyncTask(final Context context) {
			super(context);
		}

		@Override
		protected void doRestCall(RestClient restClient, Long... params) throws Exception {
			Long triggerId = params[0];
			restClient.silenceTrigger(triggerId);
		}

		@Override
		protected void onSuccess(Context context) {
			NotificationHandler.cancelTriggerNotification(notificationManager, trigger.getTriggerId());
			finish();
		}
	}
}
