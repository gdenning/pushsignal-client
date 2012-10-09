package com.pushsignal.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.pushsignal.Constants;
import com.pushsignal.NotificationDisplay;
import com.pushsignal.R;
import com.pushsignal.adapters.ActivityListAdapter;
import com.pushsignal.asynctasks.RestCallAsyncTask;
import com.pushsignal.observers.ActivityListObserver;
import com.pushsignal.observers.AppObservable;
import com.pushsignal.observers.ObserverData;
import com.pushsignal.observers.ObserverData.ActionTypeEnum;
import com.pushsignal.observers.ObserverData.ObjectTypeEnum;
import com.pushsignal.rest.RestClient;
import com.pushsignal.xml.simple.ActivityDTO;
import com.pushsignal.xml.simple.EventInviteDTO;

public class ActivityListActivity extends Activity {
	private static final int PROGRESS_DIALOG = 0;

	private List<Pair<EventInviteDTO, ActivityDTO>> activityList;

	private ActivityListAdapter adapter;

	private ListView activityListView;

	private ProgressDialog progressDialog;

	private final Handler handleActivitiesChanged = new Handler() {
		@Override
		public void handleMessage(final Message msg) {
			adapter.notifyDataSetChanged();
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);

		// Obtain handles to UI objects
		activityListView = (ListView) findViewById(R.id.activityList);

		// Register handler for UI elements
		activityListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(final AdapterView<?> parent, final View view, final int position,
					final long id) {
				Log.d(Constants.CLIENT_LOG_TAG, "activityListView clicked");

				final Pair<EventInviteDTO, ActivityDTO> activityPair = activityList.get(position);

				if (activityPair.first != null) {
					final EventInviteDTO invite = activityPair.first;
					final CharSequence[] dialogOptions = {"Accept", "Decline", "Cancel"};
					final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
					builder.setTitle("Invitation to " + invite.getEvent().getName() + ":");
					builder.setItems(dialogOptions, new DialogInterface.OnClickListener() {
						public void onClick(final DialogInterface dialog, final int item) {
							switch (item) {
							case 0: // Accept
								new AcceptInviteAsyncTask(view.getContext()).execute(invite.getEventInviteId());
								break;
							case 1: // Decline
								new DeclineInviteAsyncTask(view.getContext()).execute(invite.getEventInviteId());
								break;
							case 2: // Cancel
								break;
							}
							dialog.dismiss();
						}
					});
					builder.show();
				}
			}
		});

		new RefreshListAsyncTask(this).execute();
	}

	private List<Pair<EventInviteDTO, ActivityDTO>> generateActivityTupleList(final Set<EventInviteDTO> invites, final Set<ActivityDTO> activities) {
		final List<Pair<EventInviteDTO, ActivityDTO>> results = new ArrayList<Pair<EventInviteDTO, ActivityDTO>>();
		for (final EventInviteDTO invite : invites) {
			results.add(new Pair<EventInviteDTO, ActivityDTO>(invite, null));
		}
		for (final ActivityDTO activity : activities) {
			results.add(new Pair<EventInviteDTO, ActivityDTO>(null, activity));
		}
		return results;
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.refresh_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.refresh:
			Log.d(Constants.CLIENT_LOG_TAG, "refresh menu button clicked");
			new RefreshListAsyncTask(this).execute();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected Dialog onCreateDialog(final int id) {
		switch (id) {
		case PROGRESS_DIALOG:
			progressDialog = new ProgressDialog(this);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setMessage("Loading activities...");
			return progressDialog;
		default:
			return null;
		}
	}

	private class AcceptInviteAsyncTask extends RestCallAsyncTask<Long> {

		private Long eventInviteId;

		public AcceptInviteAsyncTask(final Context context) {
			super(context);
		}

		@Override
		protected void doRestCall(final RestClient restClient, final Long... params) throws Exception {
			eventInviteId = params[0];
			restClient.acceptInvite(eventInviteId);
		}

		@Override
		protected void onSuccess(final Context context) {
			AppObservable.getInstance().notifyObservers(new ObserverData(ObjectTypeEnum.EVENT_INVITE, ActionTypeEnum.DELETED, eventInviteId));
		}
	}

	private class DeclineInviteAsyncTask extends RestCallAsyncTask<Long> {

		private Long eventInviteId;

		public DeclineInviteAsyncTask(final Context context) {
			super(context);
		}

		@Override
		protected void doRestCall(final RestClient restClient, final Long... params) throws Exception {
			eventInviteId = params[0];
			restClient.declineInvite(eventInviteId);
		}

		@Override
		protected void onSuccess(final Context context) {
			AppObservable.getInstance().notifyObservers(new ObserverData(ObjectTypeEnum.EVENT_INVITE, ActionTypeEnum.DELETED, eventInviteId));
		}
	}

	private class RefreshListAsyncTask extends RestCallAsyncTask<Void> {

		public RefreshListAsyncTask(final Context context) {
			super(context);
		}

		@Override
		protected void onPreExecute() {
			// Show progress dialog
			showDialog(PROGRESS_DIALOG);
		}

		@Override
		protected void doRestCall(final RestClient restClient, final Void... params) throws Exception {
			activityList = generateActivityTupleList(
					restClient.getAllInvites().getEventInvites(),
					restClient.getAllActivities().getActivities());
		}

		@Override
		protected void onSuccess(final Context context) {
			adapter = new ActivityListAdapter(context, R.layout.activity_list_item, activityList, getLayoutInflater());
			activityListView.setAdapter(adapter);
			AppObservable.getInstance().addObserver(new ActivityListObserver(handleActivitiesChanged, activityList));
			dismissDialog(PROGRESS_DIALOG);
		}

		@Override
		protected void onException(final Context context, final Exception ex) {
			Log.e(Constants.CLIENT_LOG_TAG, ex.getMessage());
			NotificationDisplay.showError(context, ex.getMessage());
			dismissDialog(PROGRESS_DIALOG);
		}
	}
}
