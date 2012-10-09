package com.pushsignal.observers;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.os.Handler;

import com.pushsignal.observers.ObserverData.ActionTypeEnum;
import com.pushsignal.xml.simple.EventDTO;

/**
 * Observer class for updating the eventList when a change notification is received from the service.
 */
public class PublicEventListObserver implements Observer {
	private final Handler handler;
	private final List<EventDTO> events;

	public PublicEventListObserver(final Handler handler, final List<EventDTO> events) {
		this.handler = handler;
		this.events = events;
	}

	@Override
	public void update(final Observable observable, final Object data) {
		final ObserverData observerData = (ObserverData) data;
		if (observerData.getObjectType() == ObserverData.ObjectTypeEnum.EVENT) {
			if (observerData.getActionType() == ActionTypeEnum.MODIFIED) {
				// Update event name and description
				final EventDTO passedEvent = (EventDTO) observerData.getObjectData();
				for (final EventDTO event : events) {
					if (passedEvent.getEventId() == event.getEventId()) {
						event.setName(passedEvent.getName());
						event.setDescription(passedEvent.getDescription());
						event.setTriggerPermission(passedEvent.getTriggerPermission());
						event.setPublicFlag(passedEvent.isPublicFlag());
						event.setMembers(passedEvent.getMembers());
						handler.sendEmptyMessage(0);
					}
				}
			}
		}
	}
}
