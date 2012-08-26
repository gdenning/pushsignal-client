package com.pushsignal.observers;

import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.os.Handler;

import com.pushsignal.observers.ObserverData.ActionTypeEnum;
import com.pushsignal.observers.ObserverData.ObjectTypeEnum;
import com.pushsignal.xml.simple.EventMemberDTO;

/**
 * Observer class for updating the status and modifiedDate on the triggerAlert for
 * this class's trigger object when a change notification is received from the service.
 */
public class EventMemberListObserver implements Observer {
	private Handler handler;
	private List<EventMemberDTO> eventMembers;

	public EventMemberListObserver(final Handler handler, final List<EventMemberDTO> eventMembers) {
		this.handler = handler;
		this.eventMembers = eventMembers;
	}

	@Override
	public void update(final Observable observable, final Object data) {
		final ObserverData observerData = (ObserverData) data;
		if (observerData.getObjectType() == ObjectTypeEnum.EVENT_MEMBER) {
			if (observerData.getActionType() == ActionTypeEnum.CREATED) {
				// Add eventMember to the list
				final EventMemberDTO eventMember = (EventMemberDTO) observerData.getObjectData();
				eventMembers.add(eventMember);
				handler.sendEmptyMessage(0);
			} else if (observerData.getActionType() == ActionTypeEnum.DELETED) {
				// Remove eventMember from the list
				final long eventMemberId = (Long) observerData.getObjectData();
				final Iterator<EventMemberDTO> iter = eventMembers.iterator();
				while (iter.hasNext()) {
					final EventMemberDTO iterEventMember = iter.next();
					if (iterEventMember.getEventMemberId() == eventMemberId) {
						eventMembers.remove(iterEventMember);
						handler.sendEmptyMessage(0);
					}
				}
			}
		}
	}
}
