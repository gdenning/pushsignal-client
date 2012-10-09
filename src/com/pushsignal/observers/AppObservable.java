package com.pushsignal.observers;

import java.util.Observable;

public class AppObservable extends Observable {
	private static AppObservable ourInstance = new AppObservable();

	public static AppObservable getInstance() {
		return ourInstance;
	}

	private AppObservable() {
	}

	@Override
	public void notifyObservers(final Object observerData) {
		setChanged();

		// Notify observers asynchronously
		final Thread t = new Thread() {
			@Override
			public void run() {
				AppObservable.super.notifyObservers(observerData);
			}
		};
		t.start();
	}
}
