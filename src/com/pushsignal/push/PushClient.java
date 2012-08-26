package com.pushsignal.push;

import android.content.Context;

public interface PushClient {

	/**
	 * Connect to the server or register for server notifications.
	 * @param context service context
	 */
	void connectOrRegister(Context context);
	
	/**
	 * Disconnect from the server.
	 */
	void disconnect();
	
	/**
	 * Determine if push client is connected.
	 */
	boolean isConnected();
	
	/**
	 * Send a keepalive message to the server.
	 */
	void sendKeepAlive();
}
