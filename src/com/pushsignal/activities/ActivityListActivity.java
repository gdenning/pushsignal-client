package com.pushsignal.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
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
import com.pushsignal.NotificationHandler;
import com.pushsignal.R;
import com.pushsignal.adapters.ActivityListAdapter;
import com.pushsignal.observers.AppObservable;
import com.pushsignal.observers.ActivityListObserver;
import com.pushsignal.observers.ObserverData;
import com.pushsignal.observers.ObserverData.ActionTypeEnum;
import com.pushsignal.observers.ObserverData.ObjectTypeEnum;
import com.pushsignal.rest.RestClient;
import com.pushsignal.rest.RestClientStoredCredentials;
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

	private final Handler handleActivitiesLoaded = new Handler() {
		@Override
		public void handleMessage(final Message msg) {
			adapter = new ActivityListAdapter(ActivityListActivity.this, R.layout.activity_list_item, activityList, getLayoutInflater());
			activityListView.setAdapter(adapter);
            AppObservable.getInstance().addObserver(new ActivityListObserver(handleActivitiesChanged, activityList));
			dismissDialog(PROGRESS_DIALOG);
		}
	};
	
	private final Handler handleError = new Handler() {
		@Override
		public void handleMessage(final Message msg) {
			NotificationHandler.showError(ActivityListActivity.this, (Exception) msg.obj);
			dismissDialog(PROGRESS_DIALOG);
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
					AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
					builder.setTitle("Invitation to " + invite.getEvent().getName() + ":");
					builder.setItems(dialogOptions, new DialogInterface.OnClickListener() {
					    public void onClick(DialogInterface dialog, int item) {
							final RestClient restClient = RestClientStoredCredentials.getInstance(ActivityListActivity.this);
					    	switch (item) {
					    	case 0: // Accept
					    		try {
									restClient.acceptInvite(invite.getEventInviteId());
									AppObservable.getInstance().notifyObservers(new ObserverData(ObjectTypeEnum.EVENT_INVITE, ActionTypeEnum.DELETED, invite.getEventInviteId()));
								} catch (Exception ex) {
									NotificationHandler.showError(view.getContext(), ex);
								}
					    		break;
					    	case 1: // Decline
					    		try {
									restClient.declineInvite(invite.getEventInviteId());
									AppObservable.getInstance().notifyObservers(new ObserverData(ObjectTypeEnum.EVENT_INVITE, ActionTypeEnum.DELETED, invite.getEventInviteId()));
								} catch (Exception ex) {
									NotificationHandler.showError(view.getContext(), ex);
								}
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
		
		refreshList();
	}
	
	private void refreshList() {
		final RestClient restClient = RestClientStoredCredentials.getInstance(this);

		// Create a separate thread to retrieve activities
		final Thread t = new Thread() {
            public void run() {
        		try {
        			activityList = generateActivityTupleList(
        					restClient.getAllInvites().getEventInvites(),
        					restClient.getAllActivities().getActivities());
            		handleActivitiesLoaded.sendEmptyMessage(0);
        		} catch (final Exception ex) {
        			handleError.sendMessage(Message.obtain(handleError, 0, ex));
        		}
            }
        };
        t.start();

        // Show progress dialog
		showDialog(PROGRESS_DIALOG);
	}
	
	private List<Pair<EventInviteDTO, ActivityDTO>> generateActivityTupleList(Set<EventInviteDTO> invites, Set<ActivityDTO> activities) {
		List<Pair<EventInviteDTO, ActivityDTO>> results = new ArrayList<Pair<EventInviteDTO, ActivityDTO>>();
		for (EventInviteDTO invite : invites) {
			results.add(new Pair<EventInviteDTO, ActivityDTO>(invite, null));
		}
		for (ActivityDTO activity : activities) {
			results.add(new Pair<EventInviteDTO, ActivityDTO>(null, activity));
		}
		return results;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.refresh_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.refresh:
			Log.d(Constants.CLIENT_LOG_TAG, "refresh menu button clicked");
			refreshList();
	    	return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}

    protected Dialog onCreateDialog(int id) {
        switch(id) {
        case PROGRESS_DIALOG:
            progressDialog = new ProgressDialog(this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("Loading activities...");
            return progressDialog;
        default:
            return null;
        }
    }
}