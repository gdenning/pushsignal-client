package com.pushsignal.activities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.pushsignal.AppUserDevice;
import com.pushsignal.Constants;
import com.pushsignal.NotificationDisplay;
import com.pushsignal.R;
import com.pushsignal.asynctasks.RestCallAsyncTask;
import com.pushsignal.observers.AppObservable;
import com.pushsignal.observers.EventObserver;
import com.pushsignal.rest.RestClient;
import com.pushsignal.xml.simple.EventDTO;
import com.pushsignal.xml.simple.EventMemberDTO;
import com.pushsignal.xml.simple.TriggerDTO;
import com.pushsignal.xml.simple.UserDTO;

public class EventViewerActivity extends Activity {
	private static final int PICK_CONTACT_REQUEST = 0;

	private EventDTO event;
	private List<EventMemberDTO> eventMembers;
	private ArrayAdapter<EventMemberDTO> adapter;

	private TextView mEventName;
	private TextView mDescription;
	private TextView mLastTriggered;
	private ListView mMemberList;
	private ImageButton mTriggerButton;

	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(final Message msg) {
			mEventName.setText(event.getName());
			mDescription.setText(event.getDescription());
			mLastTriggered.setText(timeAgo(event.getLastTriggeredDateInMilliseconds()));
			eventMembers = new ArrayList<EventMemberDTO>(event.getMembers());
			adapter = new ArrayAdapter<EventMemberDTO>(EventViewerActivity.this, R.layout.simple_list_item, R.id.rowText, eventMembers);
			mMemberList.setAdapter(adapter);
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_viewer);

		// Obtain handles to UI objects
		mEventName = (TextView) findViewById(R.id.eventName);
		mDescription = (TextView) findViewById(R.id.eventDescription);
		mLastTriggered = (TextView) findViewById(R.id.lastTriggered);
		mMemberList = (ListView) findViewById(R.id.membersList);
		mTriggerButton = (ImageButton) findViewById(R.id.triggerButton);

		event = (EventDTO) getIntent().getExtras().getSerializable("event");
		mEventName.setText(event.getName());
		mDescription.setText(event.getDescription());
		mLastTriggered.setText(timeAgo(event.getLastTriggeredDateInMilliseconds()));
		eventMembers = new ArrayList<EventMemberDTO>(event.getMembers());
		adapter = new ArrayAdapter<EventMemberDTO>(this, R.layout.simple_list_item, R.id.rowText, eventMembers);
		mMemberList.setAdapter(adapter);
		final UserDTO userMe = AppUserDevice.getInstance().getUser();
		if (event.getTriggerPermission().equals("OWNER_ONLY") && !event.getOwner().equals(userMe)) {
			mTriggerButton.setVisibility(View.INVISIBLE);
		}
		if (event.getTriggerPermission().equals("ALL_MEMBERS") && !event.isMember(userMe)) {
			mTriggerButton.setVisibility(View.INVISIBLE);
		}
		if (event.getTriggerPermission().equals("URL_ONLY")) {
			mTriggerButton.setVisibility(View.INVISIBLE);
		}

		// Register handler for UI elements
		mTriggerButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				Log.d(Constants.CLIENT_LOG_TAG, "mTriggerButton clicked");
				showTriggerEventDialog();
			}
		});

		final AppObservable eventNotifier = AppObservable.getInstance();
		eventNotifier.addObserver(new EventObserver(handler, event));
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		final MenuInflater inflater = getMenuInflater();
		final UserDTO userMe = AppUserDevice.getInstance().getUser();
		if (userMe == null) {
			NotificationDisplay.showError(this,
					"PushSignal did not register properly.  Unable to show event actions at this time.");
			return false;
		}
		if (event.isMember(userMe)) {
			inflater.inflate(R.menu.event_member_menu, menu);
		} else {
			inflater.inflate(R.menu.event_non_member_menu, menu);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.join_event:
			Log.d(Constants.CLIENT_LOG_TAG, "join_event menu button clicked");
			new JoinEventAsyncTask(this).execute(event.getEventId());
			return true;
		case R.id.edit_event:
			Log.d(Constants.CLIENT_LOG_TAG, "edit_event menu button clicked");
			launchEventEditor(event);
			return true;
		case R.id.invite_friend:
			Log.d(Constants.CLIENT_LOG_TAG, "invite_friend menu button clicked");
			launchContactPicker();
			return true;
		case R.id.leave_delete_event:
			Log.d(Constants.CLIENT_LOG_TAG,
					"leave_delete_event menu button clicked");
			final UserDTO userMe = AppUserDevice.getInstance().getUser();
			if (event.getOwner().equals(userMe)) {
				showDeleteEventDialog();
			} else {
				showLeaveEventDialog();
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void showLeaveEventDialog() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(EventViewerActivity.this);
		builder.setMessage("Leave '" + event.getName() + "'?")
		.setCancelable(false)
		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int id) {
				new LeaveEventAsyncTask(EventViewerActivity.this).execute(event.getEventId());
			}
		})
		.setNegativeButton("No", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int id) {
				dialog.cancel();
			}
		});
		builder.show();
	}

	private void showDeleteEventDialog() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(EventViewerActivity.this);
		builder.setMessage("Delete '" + event.getName() + "'? (All members will lose this event)")
		.setCancelable(false)
		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int id) {
				new DeleteEventAsyncTask(EventViewerActivity.this).execute(event.getEventId());
			}
		})
		.setNegativeButton("No", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int id) {
				dialog.cancel();
			}
		});
		builder.show();
	}

	private void showTriggerEventDialog() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(EventViewerActivity.this);
		builder.setMessage("Trigger '" + event.getName() + "'?")
		.setCancelable(false)
		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int id) {
				new CreateTriggerAsyncTask(EventViewerActivity.this).execute(event.getEventId());
			}
		})
		.setNegativeButton("No", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int id) {
				dialog.cancel();
			}
		});
		builder.show();
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
		if (requestCode == PICK_CONTACT_REQUEST) {
			if (resultCode == RESULT_OK) {
				final Uri contactData = intent.getData();
				for (final String emailAddress : getEmailAddressesForContact(contactData)) {
					new CreateInviteAsyncTask(this).execute(event.getEventId(), emailAddress);
				}
			}
		}
	}

	/**
	 * Return a set of email addresses for the passed contact Uri.
	 * 
	 * @param contactUri
	 * @return Set of email address strings.
	 */
	private Set<String> getEmailAddressesForContact(final Uri contactUri) {
		final Set<String> results = new HashSet<String>();
		final Cursor contactCursor =  managedQuery(
				contactUri,
				null,
				null,
				null,
				null);
		if (contactCursor.moveToFirst()) {
			final String contactId = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts._ID));
			// Find Email Addresses
			final Cursor emailCursor = managedQuery(
					ContactsContract.CommonDataKinds.Email.CONTENT_URI,
					null,
					ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + contactId,
					null,
					null);
			while (emailCursor.moveToNext()) {
				final String emailAddress = emailCursor.getString(
						emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
				results.add(emailAddress);
			}
		}

		return results;
	}

	private void launchContactPicker() {
		final Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
		startActivityForResult(intent, PICK_CONTACT_REQUEST);
	}

	/**
	 * Launches the EventViewer activity to show information about a particular event.
	 */
	private void launchEventEditor(final EventDTO event) {
		final Intent i = new Intent(this, EventEditorActivity.class);
		i.putExtra("event", event);
		startActivity(i);
	}

	/**
	 * Launches the TriggerViewer activity to show information about a particular trigger.
	 */
	private void launchTriggerViewer(final TriggerDTO trigger) {
		final Intent i = new Intent(this, TriggerViewerActivity.class);
		i.putExtra("triggerId", trigger.getTriggerId());
		startActivity(i);
	}

	private String timeAgo(final Long lastTriggeredDateInMilliseconds) {
		if (lastTriggeredDateInMilliseconds == null) {
			return "Never";
		}
		final long millisecondsAgo = SystemClock.elapsedRealtime() - AppUserDevice.getInstance().calculateMillisecondsSinceBootForServerDate(lastTriggeredDateInMilliseconds);
		if (millisecondsAgo < 0) {
			return "Unknown - not yet registered";
		} else if (millisecondsAgo < 60000) {
			return "Less than a minute ago";
		} else if (millisecondsAgo < 3600000) {
			return millisecondsAgo / 60000 + " minutes ago";
		} else if (millisecondsAgo < 86400000) {
			return millisecondsAgo / 3600000 + " hours ago";
		} else {
			return millisecondsAgo / 86400000 + " days ago";
		}
	}

	private class LeaveEventAsyncTask extends RestCallAsyncTask<Long> {

		public LeaveEventAsyncTask(final Context context) {
			super(context);
		}

		@Override
		protected void doRestCall(final RestClient restClient, final Long... params) throws Exception {
			final Long eventId = params[0];
			restClient.leaveEvent(eventId);
		}

		@Override
		protected void onSuccess(final Context context) {
			NotificationDisplay.showInfo(context, "Left event successfully.");
			finish();
		}
	}

	private class DeleteEventAsyncTask extends RestCallAsyncTask<Long> {

		public DeleteEventAsyncTask(final Context context) {
			super(context);
		}

		@Override
		protected void doRestCall(final RestClient restClient, final Long... params) throws Exception {
			final Long eventId = params[0];
			restClient.deleteEvent(eventId);
		}

		@Override
		protected void onSuccess(final Context context) {
			NotificationDisplay.showInfo(context, "Deleted event successfully.");
			finish();
		}
	}

	private class JoinEventAsyncTask extends RestCallAsyncTask<Long> {

		public JoinEventAsyncTask(final Context context) {
			super(context);
		}

		@Override
		protected void doRestCall(final RestClient restClient, final Long... params) throws Exception {
			final Long eventId = params[0];
			restClient.joinEvent(eventId);
		}

		@Override
		protected void onSuccess(final Context context) {
			NotificationDisplay.showInfo(context, "Successfully joined event.");
			finish();
		}
	}

	private class CreateTriggerAsyncTask extends RestCallAsyncTask<Long> {

		private TriggerDTO trigger;

		public CreateTriggerAsyncTask(final Context context) {
			super(context);
		}

		@Override
		protected void doRestCall(final RestClient restClient, final Long... params) throws Exception {
			final Long eventId = params[0];
			trigger = restClient.createTrigger(eventId);
		}

		@Override
		protected void onSuccess(final Context context) {
			launchTriggerViewer(trigger);
			finish();
		}
	}

	private class CreateInviteAsyncTask extends RestCallAsyncTask<Object> {

		public CreateInviteAsyncTask(final Context context) {
			super(context);
		}

		@Override
		protected void doRestCall(final RestClient restClient, final Object... params) throws Exception {
			final Long eventId = (Long) params[0];
			final String emailAddress = (String) params[1];
			restClient.createInvite(eventId, emailAddress);
		}

		@Override
		protected void onSuccess(final Context context) {
			NotificationDisplay.showInfo(context, "Invite sent successfully.");
		}
	}
}
