package com.pushsignal.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;

import com.pushsignal.AppUserDevice;
import com.pushsignal.R;
import com.pushsignal.xml.simple.TriggerAlertDTO;
import com.pushsignal.xml.simple.TriggerDTO;

public class TriggerAlertListAdapter extends ArrayAdapter<TriggerAlertDTO> {

	private final TriggerDTO trigger;
	private final LayoutInflater inflater;

	public TriggerAlertListAdapter(final Context context, final int textViewResourceId, final List<TriggerAlertDTO> items, final TriggerDTO trigger, final LayoutInflater inflater) {
		super(context, textViewResourceId, items);
		this.trigger = trigger;
		this.inflater = inflater;
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		View row = convertView;
		if (row == null) {
			row = inflater.inflate(R.layout.trigger_alert_list_item, parent, false);
		}
		final TriggerAlertDTO alert = getItem(position);
		if (alert != null) {
			final AppUserDevice appUserDevice = AppUserDevice.getInstance();
			final ImageView statusImage = (ImageView) row.findViewById(R.id.alertImage);
			final TextView usernameText = (TextView) row.findViewById(R.id.alertEntryText);
			final Chronometer timeSinceAck = (Chronometer) row.findViewById(R.id.timeSinceAck);
			if (alert.getUser().equals(trigger.getUser())) {
				statusImage.setImageResource(R.drawable.invite_member_signal);
				timeSinceAck.setVisibility(View.VISIBLE);
				timeSinceAck.setBase(appUserDevice.calculateMillisecondsSinceBootForServerDate(alert.getModifiedDateInMilliseconds()));
				timeSinceAck.start();
			} else if (alert.getStatus().equals("ACKNOWLEDGED")) {
				statusImage.setImageResource(R.drawable.invite_member_ack);
				timeSinceAck.setVisibility(View.VISIBLE);
				timeSinceAck.setBase(appUserDevice.calculateMillisecondsSinceBootForServerDate(alert.getModifiedDateInMilliseconds()));
				timeSinceAck.start();
			} else {
				statusImage.setImageResource(R.drawable.invite_member_no_reply);
				timeSinceAck.setVisibility(View.INVISIBLE);
			}
			usernameText.setText(alert.getUser().getName());
		}
		return row;
	}
}
