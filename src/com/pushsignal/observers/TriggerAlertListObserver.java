package com.pushsignal.observers;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.os.Handler;

import com.pushsignal.observers.ObserverData.ObjectTypeEnum;
import com.pushsignal.xml.simple.TriggerAlertDTO;

/**
 * Observer class for updating the status and modifiedDate on the triggerAlert for
 * this class's trigger object when a change notification is received from the service.
 */
public class TriggerAlertListObserver implements Observer {
	private Handler handler;
	private List<TriggerAlertDTO> triggerAlerts;

	public TriggerAlertListObserver(final Handler handler, final List<TriggerAlertDTO> triggerAlerts) {
		this.handler = handler;
		this.triggerAlerts = triggerAlerts;
	}

	@Override
	public void update(final Observable observable, final Object data) {
		final ObserverData observerData = (ObserverData) data;
		if (observerData.getObjectType() == ObjectTypeEnum.TRIGGER_ALERT) {
			// Update triggerAlert status and modifiedDate
			final TriggerAlertDTO alert = (TriggerAlertDTO) observerData.getObjectData();
			for (final TriggerAlertDTO alertIter : triggerAlerts) {
				if (alert.getTriggerAlertId() == alertIter.getTriggerAlertId()) {
					alertIter.setStatus(alert.getStatus());
					handler.sendEmptyMessage(0);
				}
			}
		}
	}
}
