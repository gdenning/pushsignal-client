package com.pushsignal.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.pushsignal.Constants;
import com.pushsignal.NotificationDisplay;
import com.pushsignal.R;
import com.pushsignal.TriggerPermissionEnum;
import com.pushsignal.asynctasks.RestCallAsyncTask;
import com.pushsignal.observers.AppObservable;
import com.pushsignal.observers.ObserverData;
import com.pushsignal.observers.ObserverData.ActionTypeEnum;
import com.pushsignal.observers.ObserverData.ObjectTypeEnum;
import com.pushsignal.rest.RestClient;
import com.pushsignal.xml.simple.EventDTO;
import com.pushsignal.xml.simple.EventMemberDTO;

public class EventEditorActivity extends Activity {
	private EventDTO event;
	private List<EventMemberDTO> eventMembers;
	private ArrayAdapter<EventMemberDTO> adapter;

	private TextView mNameCaption;
	private Spinner mNameSpinner;
	private EditText mNameEditor;
	private Spinner mTriggerableBySpinner;
	private CheckBox mPublicCheckBox;
	private EditText mDescription;
	private ListView mMembersList;
	private Button mSaveButton;
	private TextView mMembersCaption;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_editor);

		// Obtain handles to UI objects
		mNameCaption = (TextView) findViewById(R.id.nameCaption);
		mNameSpinner = (Spinner) findViewById(R.id.nameSpinner);
		mNameEditor = (EditText) findViewById(R.id.nameEditor);
		mDescription = (EditText) findViewById(R.id.descriptionEditor);
		mTriggerableBySpinner = (Spinner) findViewById(R.id.triggerableBySpinner);
		mPublicCheckBox = (CheckBox) findViewById(R.id.publicCheckBox);
		mMembersList = (ListView) findViewById(R.id.membersList);
		mSaveButton = (Button) findViewById(R.id.saveButton);
		mMembersCaption = (TextView) findViewById(R.id.membersCaption);

		if (getIntent().getExtras() != null && getIntent().getExtras().containsKey("event")) {
			event = (EventDTO) getIntent().getExtras().getSerializable("event");
		}

		if (event == null) {
			// Format view for creating new event
			mNameSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(final AdapterView<?> parent, final View view, final int pos, final long id) {
					if (mNameSpinner.getSelectedItem() != null && mNameSpinner.getSelectedItem().equals("Custom")) {
						mNameEditor.setVisibility(View.VISIBLE);
						mNameEditor.requestFocus();
					} else {
						mNameEditor.setVisibility(View.GONE);
					}
				}

				@Override
				public void onNothingSelected(final AdapterView<?> arg0) {
					// Do nothing
				}
			});

			mMembersCaption.setVisibility(View.GONE);
			mMembersList.setVisibility(View.GONE);
		} else {
			// Format view for editing existing event
			eventMembers = new ArrayList<EventMemberDTO>(event.getMembers());
			adapter = new ArrayAdapter<EventMemberDTO>(this, R.layout.simple_list_item, R.id.rowText, eventMembers);

			mNameCaption.setVisibility(View.GONE);
			mNameSpinner.setVisibility(View.GONE);

			// Populate activity with event properties
			mNameEditor.setText(event.getName());
			mDescription.setText(event.getDescription());
			mTriggerableBySpinner.setSelection(TriggerPermissionEnum.valueOf(event.getTriggerPermission()).ordinal());
			mPublicCheckBox.setChecked(event.isPublicFlag());
			mMembersList.setAdapter(adapter);
		}

		// Register handler for UI elements
		mSaveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				Log.d(Constants.CLIENT_LOG_TAG, "mSaveButton clicked");

				// Retrieve event name
				final String eventName = getEventName();

				// Validation
				if (eventName == null) {
					NotificationDisplay.showError(EventEditorActivity.this, "Event name is required.");
					return;
				}

				// Execute event
				if (event == null) {
					// Create new event
					new CreateEventAsyncTask(EventEditorActivity.this).execute(
							eventName,
							mDescription.getText().toString(),
							getTriggerPermission(),
							mPublicCheckBox.isChecked());
				} else {
					// Modify existing event
					new UpdateEventAsyncTask(EventEditorActivity.this).execute(
							event.getEventId(),
							eventName,
							mDescription.getText().toString(),
							getTriggerPermission(),
							mPublicCheckBox.isChecked());
				}
			}
		});
	}

	private String getEventName() {
		String eventName = null;
		if (event == null) {
			if (mNameSpinner.getSelectedItem() != null) {
				if (mNameSpinner.getSelectedItem().equals("Custom")) {
					eventName = mNameEditor.getText().toString();
				} else {
					eventName = (String) mNameSpinner.getSelectedItem();
				}
			}
		} else {
			eventName = mNameEditor.getText().toString();
		}
		return eventName;
	}

	private String getTriggerPermission() {
		final int index = mTriggerableBySpinner.getSelectedItemPosition();
		return TriggerPermissionEnum.values()[index].name();
	}

	private void resetForm() {
		mNameSpinner.setSelection(0);
		mNameEditor.setText("");
		mDescription.setText("");
		mTriggerableBySpinner.setSelection(0);
		mPublicCheckBox.setChecked(false);
		event = null;
	}

	/**
	 * Launches the EventViewer activity to show information about a particular event.
	 */
	private void launchEventViewer(final EventDTO event) {
		final Intent i = new Intent(this, EventViewerActivity.class);
		i.putExtra("event", event);
		startActivity(i);
	}

	private class CreateEventAsyncTask extends RestCallAsyncTask<Object> {
		final AppObservable notifier = AppObservable.getInstance();

		public CreateEventAsyncTask(final Context context) {
			super(context);
		}

		@Override
		protected void doRestCall(final RestClient restClient, final Object... params) throws Exception {
			final String eventName = (String) params[0];
			final String description = (String) params[1];
			final String triggerPermission = (String) params[2];
			final Boolean isPublic = (Boolean) params[3];
			event = restClient.createEvent(eventName, description, triggerPermission, isPublic);
		}

		@Override
		protected void onSuccess(final Context context) {
			notifier.notifyObservers(new ObserverData(
					ObjectTypeEnum.EVENT,
					ActionTypeEnum.CREATED,
					event));
			launchEventViewer(event);
			resetForm();
		}

		@Override
		protected void onException(final Context context, final Exception ex) {
			event = null;
			NotificationDisplay.showError(context, ex);
		}
	}

	private class UpdateEventAsyncTask extends RestCallAsyncTask<Object> {
		final AppObservable notifier = AppObservable.getInstance();

		public UpdateEventAsyncTask(final Context context) {
			super(context);
		}

		@Override
		protected void doRestCall(final RestClient restClient, final Object... params) throws Exception {
			final Long eventId = (Long) params[0];
			final String eventName = (String) params[1];
			final String description = (String) params[2];
			final String triggerPermission = (String) params[3];
			final Boolean isPublic = (Boolean) params[4];
			event = restClient.updateEvent(eventId, eventName, description, triggerPermission, isPublic);
		}

		@Override
		protected void onSuccess(final Context context) {
			notifier.notifyObservers(new ObserverData(
					ObjectTypeEnum.EVENT,
					ActionTypeEnum.MODIFIED,
					event));
			NotificationDisplay.showInfo(EventEditorActivity.this, "Event updated successfully");
			finish();
		}
	}
}
