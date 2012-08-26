package com.pushsignal.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
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
import com.pushsignal.adapters.EventListAdapter;
import com.pushsignal.observers.AppObservable;
import com.pushsignal.observers.PublicEventListObserver;
import com.pushsignal.rest.RestClient;
import com.pushsignal.rest.RestClientStoredCredentials;
import com.pushsignal.xml.simple.EventDTO;

public class PublicEventListActivity extends Activity {
	private static final int PROGRESS_DIALOG = 0;
	
	private List<EventDTO> eventList;
	
	private EventListAdapter adapter;

	private ListView eventListView;

	private ProgressDialog progressDialog;
	
	private final Handler handleEventsChanged = new Handler() {
		@Override
		public void handleMessage(final Message msg) {
			adapter.notifyDataSetChanged();
		}
	};

	private final Handler handleEventsLoaded = new Handler() {
		@Override
		public void handleMessage(final Message msg) {
    		adapter = new EventListAdapter(PublicEventListActivity.this, R.layout.event_list_item, eventList, getLayoutInflater());
    		eventListView.setAdapter(adapter);
            AppObservable.getInstance().addObserver(new PublicEventListObserver(handleEventsChanged, eventList));
			dismissDialog(PROGRESS_DIALOG);
		}
	};

	private final Handler handleError = new Handler() {
		@Override
		public void handleMessage(final Message msg) {
			NotificationHandler.showError(PublicEventListActivity.this, (Exception) msg.obj);
			dismissDialog(PROGRESS_DIALOG);
		}
	};
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_list);
		
		// Obtain handles to UI objects
		eventListView = (ListView) findViewById(R.id.eventList);
		
		// Register handler for UI elements
		eventListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(final AdapterView<?> parent, final View view, final int position,
					final long id) {
				Log.d(Constants.CLIENT_LOG_TAG, "mEventList clicked");
				launchEventViewer(eventList.get(position));
			}
		});

		refreshList();
	}

	private void refreshList() {
		final RestClient restClient = RestClientStoredCredentials.getInstance(this);

		// Create a separate thread to retrieve events
		final Thread t = new Thread() {
            public void run() {
        		try {
        			eventList = new ArrayList<EventDTO>(restClient.getPublicEvents().getEvents());
            		handleEventsLoaded.sendEmptyMessage(0);
        		} catch (final Exception ex) {
        			handleError.sendMessage(Message.obtain(handleError, 0, ex));
        		}
            }
        };
        t.start();

        // Show progress dialog
		showDialog(PROGRESS_DIALOG);
	}

    protected Dialog onCreateDialog(int id) {
        switch(id) {
        case PROGRESS_DIALOG:
            progressDialog = new ProgressDialog(this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("Loading public events...");
            return progressDialog;
        default:
            return null;
        }
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

	/**
     * Launches the EventViewer activity to show information about a particular event.
     */
    private void launchEventViewer(EventDTO event) {
        Intent i = new Intent(this, EventViewerActivity.class);
        i.putExtra("event", event);
        startActivity(i);
    }
}