package com.pushsignal.observers;

import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.os.Handler;
import android.util.Pair;

import com.pushsignal.observers.ObserverData.ActionTypeEnum;
import com.pushsignal.observers.ObserverData.ObjectTypeEnum;
import com.pushsignal.xml.simple.ActivityDTO;
import com.pushsignal.xml.simple.EventInviteDTO;

public class ActivityListObserver implements Observer {
	private Handler handler;
	private List<Pair<EventInviteDTO, ActivityDTO>> activities;

	public ActivityListObserver(final Handler handler, final List<Pair<EventInviteDTO, ActivityDTO>> activities) {
		this.handler = handler;
		this.activities = activities;
	}

	@Override
	public void update(final Observable observable, final Object data) {
		final ObserverData observerData = (ObserverData) data;
		if (observerData.getObjectType() == ObjectTypeEnum.EVENT_INVITE) {
			if (observerData.getActionType() == ActionTypeEnum.CREATED) {
				// Add item to the list
				final Pair<EventInviteDTO, ActivityDTO> activityPair = new Pair<EventInviteDTO, ActivityDTO>((EventInviteDTO) observerData.getObjectData(), null);
				activities.add(activityPair);
				handler.sendEmptyMessage(0);
			} else if (observerData.getActionType() == ActionTypeEnum.DELETED) {
				// Remove item from the list
				final long eventInviteId = (Long) observerData.getObjectData();
				final Iterator<Pair<EventInviteDTO, ActivityDTO>> iter = activities.iterator();
				while (iter.hasNext()) {
					final Pair<EventInviteDTO, ActivityDTO> iterActivityPair = iter.next();
					if (iterActivityPair.first != null && iterActivityPair.first.getEventInviteId() == eventInviteId) {
						activities.remove(iterActivityPair);
						handler.sendEmptyMessage(0);
						break;
					}
				}
			}
		} else if (observerData.getObjectType() == ObjectTypeEnum.ACTIVITY) {
			if (observerData.getActionType() == ActionTypeEnum.CREATED) {
				// Add item to the list
				final Pair<EventInviteDTO, ActivityDTO> activityPair = new Pair<EventInviteDTO, ActivityDTO>(null, (ActivityDTO) observerData.getObjectData());
				activities.add(activityPair);
				handler.sendEmptyMessage(0);
			} else if (observerData.getActionType() == ActionTypeEnum.DELETED) {
				// Remove item from the list
				final long activityId = (Long) observerData.getObjectData();
				final Iterator<Pair<EventInviteDTO, ActivityDTO>> iter = activities.iterator();
				while (iter.hasNext()) {
					final Pair<EventInviteDTO, ActivityDTO> iterActivityPair = iter.next();
					if (iterActivityPair.first != null && iterActivityPair.second.getActivityId() == activityId) {
						activities.remove(iterActivityPair);
						handler.sendEmptyMessage(0);
						break;
					}
				}
			}
		}
	}
}
