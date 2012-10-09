package com.pushsignal.observers;

import java.util.Observable;
import java.util.Observer;

import android.os.Handler;

import com.pushsignal.xml.simple.EventDTO;

public class EventObserver implements Observer {
	private final Handler handler;
	private final EventDTO event;

	public EventObserver(final Handler handler, final EventDTO event) {
		this.handler = handler;
		this.event = event;
	}

	@Override
	public void update(final Observable observable, final Object data) {
		final ObserverData observerData = (ObserverData) data;
		if (observerData.getObjectType() == ObserverData.ObjectTypeEnum.EVENT &&
				observerData.getActionType() == ObserverData.ActionTypeEnum.MODIFIED) {
			final EventDTO passedEvent = (EventDTO) observerData.getObjectData();
			event.setName(passedEvent.getName());
			event.setDescription(passedEvent.getDescription());
			event.setTriggerPermission(passedEvent.getTriggerPermission());
			event.setPublicFlag(passedEvent.isPublicFlag());
			event.setMembers(passedEvent.getMembers());
			event.setLastTriggeredDateInMilliseconds(passedEvent.getLastTriggeredDateInMilliseconds());
			handler.sendEmptyMessage(0);
		}
	}
}
