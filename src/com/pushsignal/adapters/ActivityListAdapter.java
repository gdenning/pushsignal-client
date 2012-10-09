package com.pushsignal.adapters;

import java.util.List;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pushsignal.R;
import com.pushsignal.xml.simple.ActivityDTO;
import com.pushsignal.xml.simple.EventInviteDTO;

public class ActivityListAdapter extends ArrayAdapter<Pair<EventInviteDTO, ActivityDTO>> {

	private final LayoutInflater inflater;

	public ActivityListAdapter(final Context context, final int textViewResourceId, final List<Pair<EventInviteDTO, ActivityDTO>> items, final LayoutInflater inflater) {
		super(context, textViewResourceId, items);
		this.inflater = inflater;
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		View row = convertView;
		if (row == null) {
			row = inflater.inflate(R.layout.activity_list_item, parent, false);
		}
		final Pair<EventInviteDTO, ActivityDTO> item = getItem(position);
		if (item != null) {
			final ImageView activityImage = (ImageView) row.findViewById(R.id.activityImage);
			final TextView activitySender = (TextView) row.findViewById(R.id.activitySender);
			final TextView activityMembersOrPoints = (TextView) row.findViewById(R.id.activityMembersOrPoints);
			final TextView activityName = (TextView) row.findViewById(R.id.activityName);

			final EventInviteDTO eventInvite = item.first;
			final ActivityDTO activity = item.second;

			if (eventInvite != null) {
				activityImage.setImageResource(R.drawable.invite);
				activitySender.setText(eventInvite.getUser().getName());
				if (eventInvite.getEvent().getMembers().size() == 1) {
					activityMembersOrPoints.setText("1 member");
				} else {
					activityMembersOrPoints.setText(eventInvite.getEvent().getMembers().size() + " members");
				}
				activityName.setText(eventInvite.getEvent().getName());
			} else if (activity != null) {
				activityImage.setImageResource(R.drawable.activity);
				activitySender.setText("Points earned!");
				if (activity.getPoints() == 1) {
					activityMembersOrPoints.setText("+1 point");
				} else {
					activityMembersOrPoints.setText("+" + activity.getPoints() + " points");
				}
				activityName.setText(activity.getDescription());
			}
		}
		return row;
	}
}
