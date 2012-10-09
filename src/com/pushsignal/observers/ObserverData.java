package com.pushsignal.observers;

public class ObserverData {
	private final ObjectTypeEnum objectType;
	private final ActionTypeEnum actionType;
	private final Object objectData;

	public ObserverData(final ObjectTypeEnum objectType,
			final ActionTypeEnum actionType,
			final Object objectData) {
		this.objectType = objectType;
		this.actionType = actionType;
		this.objectData = objectData;
	}

	public ObjectTypeEnum getObjectType() {
		return objectType;
	}

	public ActionTypeEnum getActionType() {
		return actionType;
	}

	public Object getObjectData() {
		return objectData;
	}

	public enum ActionTypeEnum {
		CREATED,
		MODIFIED,
		DELETED
	}

	public enum ObjectTypeEnum {
		SERVER_MESSAGE,
		SERVER_REGISTRATION_ID,
		USER_DEVICE,
		EVENT,
		EVENT_MEMBER,
		EVENT_INVITE,
		ACTIVITY,
		TRIGGER_ALERT
	}
}
