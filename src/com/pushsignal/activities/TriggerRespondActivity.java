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
import com.pushsignal.rest.RestClient;
import com.pushsignal.rest.RestClientStoredCredentials;
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

		final RestClient restClient = RestClientStoredCredentials.getInstance(this);
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
				try {
					restClient.ackTrigger(trigger.getTriggerId());
					NotificationHandler.cancelTriggerNotification(notificationManager, trigger.getTriggerId());
					launchTriggerViewer(trigger);
					finish();
				} catch (Exception ex) {
					NotificationHandler.showError(v.getContext(), ex);
				}
			}
		});
		mIgnoreButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				Log.d(Constants.CLIENT_LOG_TAG, "mIgnoreButton clicked (Trigger:" + trigger.getTriggerId() + ")");
				try {
					restClient.silenceTrigger(trigger.getTriggerId());
					NotificationHandler.cancelTriggerNotification(notificationManager, trigger.getTriggerId());
					finish();
				} catch (Exception ex) {
					NotificationHandler.showError(v.getContext(), ex);
				}
			}
		});
	}

    /**
     * Launches the TriggerViewer activity to show information about a particular trigger.
     */
    private void launchTriggerViewer(TriggerDTO trigger) {
        Intent i = new Intent(this, TriggerViewerActivity.class);
        i.putExtra("triggerId", trigger.getTriggerId());
        startActivity(i);
    }
}
