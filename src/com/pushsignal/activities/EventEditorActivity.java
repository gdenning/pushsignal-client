package com.pushsignal.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
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
import com.pushsignal.NotificationHandler;
import com.pushsignal.R;
import com.pushsignal.TriggerPermissionEnum;
import com.pushsignal.observers.AppObservable;
import com.pushsignal.observers.ObserverData;
import com.pushsignal.observers.ObserverData.ActionTypeEnum;
import com.pushsignal.observers.ObserverData.ObjectTypeEnum;
import com.pushsignal.rest.RestClient;
import com.pushsignal.rest.RestClientStoredCredentials;
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

		final RestClient restClient = RestClientStoredCredentials.getInstance(this);

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
				public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
					if (mNameSpinner.getSelectedItem() != null && mNameSpinner.getSelectedItem().equals("Custom")) {
						mNameEditor.setVisibility(View.VISIBLE);
						mNameEditor.requestFocus();
					} else {
						mNameEditor.setVisibility(View.GONE);
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
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
				String eventName = getEventName();

				// Validation
				if (eventName == null) {
					NotificationHandler.showError(EventEditorActivity.this, "Event name is required.");
					return;
				}
				
				// Execute event
				final AppObservable notifier = AppObservable.getInstance();
				if (event == null) {
					// Create new event
					try {
						event = restClient.createEvent(eventName, mDescription.getText().toString(),
								getTriggerPermission(), mPublicCheckBox.isChecked());
						notifier.notifyObservers(new ObserverData(
								ObjectTypeEnum.EVENT,
								ActionTypeEnum.CREATED,
								event));
						launchEventViewer(event);
						resetForm();
					} catch (final Exception ex) {
						event = null;
						NotificationHandler.showError(EventEditorActivity.this, ex);
					}
				} else {
					// Modify existing event
					try {
						event = restClient.updateEvent(event.getEventId(), eventName, mDescription.getText().toString(),
								getTriggerPermission(), mPublicCheckBox.isChecked());
						notifier.notifyObservers(new ObserverData(
								ObjectTypeEnum.EVENT,
								ActionTypeEnum.MODIFIED,
								event));
						NotificationHandler.showInfo(EventEditorActivity.this, "Event updated successfully");
						finish();
					} catch (final Exception ex) {
						NotificationHandler.showError(EventEditorActivity.this, ex);
					}
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
		int index = mTriggerableBySpinner.getSelectedItemPosition();
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
}
