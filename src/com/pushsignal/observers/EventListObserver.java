package com.pushsignal.observers;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.os.Handler;

import com.pushsignal.AppUserDevice;
import com.pushsignal.observers.ObserverData.ActionTypeEnum;
import com.pushsignal.xml.simple.EventDTO;
import com.pushsignal.xml.simple.UserDTO;

/**
 * Observer class for updating the eventList when a change notification is received from the service.
 */
public class EventListObserver implements Observer {
	private Handler handler;
	private List<EventDTO> events;

	public EventListObserver(final Handler handler, final List<EventDTO> events) {
		this.handler = handler;
		this.events = events;
	}

	@Override
	public void update(final Observable observable, final Object data) {
		final ObserverData observerData = (ObserverData) data;
		if (observerData.getObjectType() == ObserverData.ObjectTypeEnum.EVENT) {
			if (observerData.getActionType() == ObserverData.ActionTypeEnum.CREATED) {
				// Add event to the list
				final EventDTO passedEvent = (EventDTO) observerData.getObjectData();
				events.add(passedEvent);
				handler.sendEmptyMessage(0);
			} else if (observerData.getActionType() == ActionTypeEnum.MODIFIED) {
				// Update event name and description
				final EventDTO passedEvent = (EventDTO) observerData.getObjectData();
				final UserDTO userMe = AppUserDevice.getInstance().getUser();
				for (final EventDTO event : events) {
					if (event.getEventId() == passedEvent.getEventId()) {
						if (passedEvent.isMember(userMe)) {
							event.setName(passedEvent.getName());
							event.setDescription(passedEvent.getDescription());
							event.setTriggerPermission(passedEvent.getTriggerPermission());
							event.setPublicFlag(passedEvent.isPublicFlag());
							event.setMembers(passedEvent.getMembers());
							event.setLastTriggeredDateInMilliseconds(passedEvent.getLastTriggeredDateInMilliseconds());
						} else {
							events.remove(event);
						}
						handler.sendEmptyMessage(0);
						break;
					}
				}
			} else if (observerData.getActionType() == ActionTypeEnum.DELETED) {
				// Remove event from the list
				final long eventId = (Long) observerData.getObjectData();
				for (final EventDTO event : events) {
					if (event.getEventId() == eventId) {
						events.remove(event);
						handler.sendEmptyMessage(0);
						break;
					}
				}
			}
		}
	}
}
