package com.pushsignal.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pushsignal.R;
import com.pushsignal.xml.simple.EventDTO;

public class EventListAdapter extends ArrayAdapter<EventDTO> {

	private final LayoutInflater inflater;

	public EventListAdapter(final Context context, final int textViewResourceId, final List<EventDTO> items, final LayoutInflater inflater) {
		super(context, textViewResourceId, items);
		this.inflater = inflater;
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		View row = convertView;
		if (row == null) {
			row = inflater.inflate(R.layout.event_list_item, parent, false);
		}
		final EventDTO event = getItem(position);
		if (event != null) {
			final ImageView eventImage = (ImageView) row.findViewById(R.id.eventImage);
			final TextView eventOwner = (TextView) row.findViewById(R.id.eventOwner);
			final TextView eventMemberCount = (TextView) row.findViewById(R.id.eventMemberCount);
			final TextView eventName = (TextView) row.findViewById(R.id.eventName);
			if (event.isPublicFlag()) {
				eventImage.setImageResource(R.drawable.event_public);
			} else {
				eventImage.setImageResource(R.drawable.event_private);
			}
			eventOwner.setText(event.getOwner().getName());
			if (event.getMembers().size() == 1) {
				eventMemberCount.setText("1 member");
			} else {
				eventMemberCount.setText(event.getMembers().size() + " members");
			}
			eventName.setText(event.getName());
		}
		return row;
	}
}
