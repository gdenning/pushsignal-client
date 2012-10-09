package com.pushsignal.observers;

import java.util.Observable;
import java.util.Observer;

import android.os.Handler;

import com.pushsignal.observers.ObserverData.ObjectTypeEnum;

/**
 * Observer class that simply calls the passed handler when a notification of the passed type is received.
 */
public class BasicObserver implements Observer {
	private final Handler handler;
	private final ObjectTypeEnum objectType;

	public BasicObserver(final Handler handler, final ObjectTypeEnum objectType) {
		this.handler = handler;
		this.objectType = objectType;
	}

	@Override
	public void update(final Observable observable, final Object data) {
		final ObserverData observerData = (ObserverData) data;
		if (observerData.getObjectType() == objectType) {
			handler.sendEmptyMessage(0);
		}
	}
}
